package p2pServer.communicationHandler;

import callback.P2PCallback;
import communicationHandler.ReceiveRequester;
import communicationHandler.SendRequestHandler;
import envelope.Envelope;
import envelope.EnvelopeType;
import model.HashableEntity;
import model.Tweet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import p2pServer.callback.ValidationCallbackHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ReceiveRequesterTests {
  private final List<Envelope<Tweet>> tweetsQueue = new ArrayList<>();
  private final ExecutorService handlerReceiveRequester = Executors.newFixedThreadPool(1);
  private final ExecutorService handlerSendRequester = Executors.newFixedThreadPool(3);
  private final int port = 10_000;
  private ServerSocket serverSocket;
  private ReceiveRequester receiveRequester;

  public void awaitTerminationAfterShutdown(ExecutorService threadPool) {
    threadPool.shutdown();
    try {
      if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
        threadPool.shutdownNow();
      }
    } catch (InterruptedException ex) {
      threadPool.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  private List<Callable<Boolean>> getCallablesToSend(List<Socket> sockets, Envelope<? extends HashableEntity> envelope) {
    List<Callable<Boolean>> callables = new ArrayList<>(sockets.size());
    for (Socket socket : sockets) {
      Callable<Boolean> callable = () -> {
        try {
          SendRequestHandler.sendData(socket, envelope);
          return true;
        } catch (Exception e) {
          e.printStackTrace();
          return false;
        }
      };
      callables.add(callable);
    }
    return callables;
  }

  @BeforeEach
  public void initReceiveRequester() throws IOException {
    this.serverSocket = new ServerSocket(port);
    P2PCallback callback = new ValidationCallbackHandler(tweetsQueue, 3, null, null, null, null);
    ReceiveRequester receiveRequester = new ReceiveRequester(serverSocket, callback, new ArrayList<>());
    this.receiveRequester = receiveRequester;
    this.handlerReceiveRequester.submit(receiveRequester);
  }

  @AfterEach
  public void shutdownReceiveRequester() throws IOException, InterruptedException {
    this.receiveRequester.terminate();
    this.handlerReceiveRequester.shutdownNow();
    this.handlerSendRequester.shutdownNow();
    this.handlerReceiveRequester.awaitTermination(10, TimeUnit.SECONDS);
    this.serverSocket.close();
    this.tweetsQueue.clear();
  }

  @Test
  public void testReceiveTweet() throws InterruptedException {
    Envelope<Tweet> envelope = new Envelope<>(new Tweet("test", "publicKeyValidationNode", "signature", System.currentTimeMillis()), EnvelopeType.TWEET_RECEIVE);
    try (Socket socket = new Socket("localhost", port)) {
      SendRequestHandler.sendData(socket, envelope);
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail();
    }
    Thread.sleep(1_000);
    handlerReceiveRequester.shutdownNow();
    Assertions.assertEquals(1, tweetsQueue.size());
    Assertions.assertEquals(envelope, tweetsQueue.get(0));
  }

  @Test
  public void testReceiveTweetMultithreaded() throws InterruptedException, IOException {
    Envelope<Tweet> envelope = new Envelope<>(new Tweet("test", "publicKeyValidationNode", "signature", System.currentTimeMillis()), EnvelopeType.TWEET_RECEIVE);
    try (Socket socket1 = new Socket("localhost", port); Socket socket2 = new Socket("localhost", port)) {
      List<Callable<Boolean>> callables = getCallablesToSend(List.of(socket1, socket2), envelope);

      // invoke callables to send data to the sockets
      var futures = handlerSendRequester.invokeAll(callables);
      awaitTerminationAfterShutdown(handlerSendRequester);

      // check if all callables have been executed successfully
      Assertions.assertTrue(futures.stream().allMatch(future -> {
        try {
          return future.get();
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
          return false;
        }
      }));
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail();
    }

    Thread.sleep(1_000);
    handlerReceiveRequester.shutdownNow();

    // since we add the same tweet to the queue 2 times, there should still be only one tweet in the queue
    Assertions.assertEquals(1, tweetsQueue.size());
    Assertions.assertEquals(envelope, tweetsQueue.get(0));
  }
}
