package communicationHandler;

import callback.P2PCallback;
import connection.ConnectionDetails;
import lombok.extern.slf4j.Slf4j;
import model.HashableEntity;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ReceiveRequester implements Runnable {
  private final ServerSocket serverSocket;
  private final P2PCallback callback;
  private final ExecutorService handlers;
  private final List<ConnectionDetails> connections;

  private volatile boolean isRunning = true;

  public ReceiveRequester(ServerSocket serverSocket, P2PCallback callback, List<ConnectionDetails> connections) {
    this.serverSocket = serverSocket;
    this.callback = callback;
    this.connections = connections;
    this.handlers = Executors.newFixedThreadPool(10);
  }

  public boolean terminate() throws InterruptedException {
    isRunning = false;
    handlers.shutdown();
    return handlers.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
  }

  @Override
  public void run() {
    log.info("P2P Server is ready to receive requests on {}:{}", serverSocket.getInetAddress().getHostName(),
      serverSocket.getLocalPort());
    while (isRunning) {
      try {
        handlers.submit(new ReceiveRequestHandler<HashableEntity>(serverSocket.accept(), callback, connections));
      } catch (JAXBException | IOException e) {
        e.printStackTrace();
      }
    }
  }
}
