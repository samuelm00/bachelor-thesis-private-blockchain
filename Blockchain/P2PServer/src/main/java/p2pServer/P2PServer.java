package p2pServer;

import callback.P2PCallback;
import communicationHandler.ReceiveRequester;
import communicationHandler.SendRequestHandler;
import connection.ConnectionDetails;
import envelope.Envelope;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.Tweet;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class P2PServer {
  @Getter
  private final ServerSocket serverSocket;
  @Getter
  private final List<ConnectionDetails> connections;
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);
  @Getter
  private boolean alreadyRegistered = false;

  public P2PServer(ServerSocket serverSocket, List<ConnectionDetails> connections) {
    this.serverSocket = serverSocket;
    this.connections = connections;
  }

  /**
   * Listens for incoming messages and processes them based on the callback
   *
   * @param callback the callback to be used to process the incoming message
   */
  public void register(P2PCallback callback) {
    if (!alreadyRegistered) {
      alreadyRegistered = true;
      executorService.submit(new ReceiveRequester(serverSocket, callback, connections));
    } else {
      log.warn("This node is already registered in the P2P network");
    }
  }

  /**
   * Sends a message to all the connected peers
   *
   * @param envelope the message to be sent
   */
  public boolean sendTweetToOtherPeers(Envelope<Tweet> envelope) {
    return connections
      .parallelStream()
      .allMatch(connection -> {
        log.info("Sending tweet to {}:{}", connection.getIp(), connection.getPort());
        try {
          Socket socket = new Socket(connection.getIp(), connection.getPort());
          SendRequestHandler.sendData(socket, envelope);
          socket.close();
          return true;
        } catch (Exception e) {
          e.printStackTrace();
          log.error("Failed to send tweet to {}:{}", connection.getIp(), connection.getPort());
          return false;
        }
      });
  }
}
