package marshaller;

import connection.ConnectionDetails;
import envelope.BlockWithValidation;
import envelope.Envelope;
import model.Block;
import model.MerkleNode;
import model.Tweet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * This class handles the marshalling and unmarshalling of the Messages that are sent vie the p2p-network.
 * It uses the JAXB library to do so.
 *
 * @param <T>
 * @see <a href="https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/JAXBContext.html">JAXB documentation</a>
 */
public class P2PMarshaller<T> {
  private final JAXBContext jaxbContext;

  public P2PMarshaller() throws JAXBException {
    this.jaxbContext = JAXBContext.newInstance(Envelope.class, MerkleNode.class, Block.class, Tweet.class,
      BlockWithValidation.class, ConnectionDetails.class);
  }

  /**
   * Marshals the provided envelope into a xml-string.
   *
   * @param data the envelope to marshal
   * @return the marshalled envelope as a xml-string
   */
  public String marshal(Envelope<T> data) throws JAXBException, IOException {
    StringWriter writer = new StringWriter();
    Marshaller jaxbP2PMarshaller = jaxbContext.createMarshaller();
    jaxbP2PMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    jaxbP2PMarshaller.marshal(data, writer);
    String xmlString = writer.toString();
    writer.close();
    return xmlString;
  }

  /**
   * Unmarshalls the provided xml-string into an envelope.
   *
   * @param stringReader the reader that is used to read the xml-string
   * @return the unmarshalled envelope
   */
  @SuppressWarnings("unchecked")
  public Envelope<T> unmarshal(StringReader stringReader) throws JAXBException {
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    Envelope<T> response = (Envelope<T>) jaxbUnmarshaller.unmarshal(stringReader);
    stringReader.close();
    return response;
  }
}
