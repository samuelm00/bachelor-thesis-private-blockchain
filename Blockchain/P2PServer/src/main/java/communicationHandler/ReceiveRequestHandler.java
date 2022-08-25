package communicationHandler;

import callback.P2PCallback;
import connection.ConnectionDetails;
import envelope.BlockWithValidation;
import envelope.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import marshaller.P2PMarshaller;
import model.HashableEntity;
import model.Tweet;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ReceiveRequestHandler<T extends HashableEntity> implements Runnable {
  private final Socket socket;
  private final P2PCallback callback;
  private final P2PMarshaller<T> marshaller;
  private final List<ConnectionDetails> connections;

  public ReceiveRequestHandler(Socket socket, P2PCallback callback, List<ConnectionDetails> connections) throws JAXBException {
    this.socket = socket;
    this.callback = callback;
    this.marshaller = new P2PMarshaller<>();
    this.connections = connections;
  }

  /**
   * Unmarshal the message from the input stream using {@link P2PMarshaller}
   *
   * @param inputStream the input stream to read from
   * @return the unmarshalled envelope
   */
  private Envelope<T> unmarshalMessage(InputStream inputStream) throws IOException, JAXBException {
    byte[] bytes = inputStream.readAllBytes();
    String xmlString = new String(bytes, StandardCharsets.UTF_8);
    return marshaller.unmarshal(new StringReader(xmlString));
  }

  /**
   * Handle the message depending on the type of the envelope
   *
   * @param envelope the envelope to handle
   * @return the result of the handling
   */
  private Optional<? extends Envelope<? extends HashableEntity>> handleMessage(Envelope<T> envelope) {
    switch (envelope.getType()) {
      case BLOCK_RECEIVE:
        BlockWithValidation blockWithValidation = (BlockWithValidation) envelope.getData();
        return callback.onBlockReceived(new Envelope<>(blockWithValidation, envelope.getType()));
      case TWEET_RECEIVE:
        Tweet tweet = (Tweet) envelope.getData();
        return callback.onTweetReceived(new Envelope<>(tweet, envelope.getType()));
      case PRIMARY_NODE_SELECTION:
        ConnectionDetails primaryNodeConnectionDetails = (ConnectionDetails) envelope.getData();
        return callback.onPrimaryNodeSelected(new Envelope<>(primaryNodeConnectionDetails, envelope.getType()));
      default:
        return Optional.empty();
    }
  }

  /**
   * Send the message to all other validation nodes that are connected with the p2p network
   *
   * @param envelope the envelope to send
   */
  private void sendToAllConnections(Envelope<T> envelope) {
    log.info("Connections: {}", connections);
    connections.parallelStream().forEach(connection -> {
      try {
        Socket socket = new Socket(connection.getIp(), connection.getPort());
        SendRequestHandler.sendData(socket, envelope);
        socket.close();
        log.info("Envelope {}, send to {}:{}", envelope.getData(), connection.getIp(), connection.getPort());
      } catch (Exception e) {
        log.error("Error while sending data to {}", connection.getIp());
      }
    });
  }

  @Override
  public void run() {
    try (InputStream inputStream = socket.getInputStream()) {
      log.info("Receiving message from {}", socket.getRemoteSocketAddress());
      Envelope<T> envelope = unmarshalMessage(inputStream);

      var result = handleMessage(envelope);
      result.ifPresent(value -> sendToAllConnections((Envelope<T>) value));
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
