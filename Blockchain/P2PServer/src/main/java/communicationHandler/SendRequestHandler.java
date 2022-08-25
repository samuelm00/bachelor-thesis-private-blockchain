package communicationHandler;

import envelope.Envelope;
import lombok.extern.slf4j.Slf4j;
import marshaller.P2PMarshaller;

import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Slf4j
public class SendRequestHandler {
  /**
   * Send the data to the peer
   *
   * @param socket     the socket to send the data to
   * @param dataToSend the data to send
   */
  public static <T> void sendData(Socket socket, Envelope<T> dataToSend) throws Exception {
    try (OutputStream outputStream = socket.getOutputStream()) {
      P2PMarshaller<T> marshaller = new P2PMarshaller<>();
      String xmlString = marshaller.marshal(dataToSend);
      outputStream.write(xmlString.getBytes(StandardCharsets.UTF_8));
      outputStream.flush();
      log.info("Sent data to peer: {}:{} successfully", socket.getInetAddress().getHostName(), socket.getPort());
    } catch (Exception e) {
      throw new Exception("Error while sending data", e);
    }
  }
}
