package config.runner;

import communicationHandler.SendRequestHandler;
import config.provider.ValidationNodeConnectionProvider;
import connection.ConnectionDetails;
import envelope.Envelope;
import envelope.EnvelopeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrimaryNodeSelectionRunner implements CommandLineRunner {
  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  private final ValidationNodeConnectionProvider connectionProvider;
  private int currentCount = 0;

  private void selectPrimaryNode() {
    int currentPrimaryNode = currentCount % connectionProvider.getConnections().size();
    ConnectionDetails primaryConnectionDetails = connectionProvider.getConnections().get(currentPrimaryNode);

    connectionProvider
      .getConnections()
      .parallelStream()
      .forEach(connection -> {
        try {
          Socket socket = new Socket(connection.getIp(), connection.getPort());
          SendRequestHandler.sendData(socket, new Envelope<>(primaryConnectionDetails, EnvelopeType.PRIMARY_NODE_SELECTION));
          socket.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
  }

  @Override
  public void run(String... args) {
    executorService.scheduleAtFixedRate(() -> {
      log.info("Primary node selection started");
      try {
        selectPrimaryNode();
      } catch (Exception e) {
        e.printStackTrace();
      }
      currentCount++;
    }, 0, 5, java.util.concurrent.TimeUnit.SECONDS);
  }
}
