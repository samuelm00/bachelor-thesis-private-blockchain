package p2pServer.communicationHandler;

import communicationHandler.SendRequestHandler;
import envelope.BlockWithValidation;
import envelope.Envelope;
import envelope.EnvelopeType;
import marshaller.P2PMarshaller;
import model.Block;
import model.HashableEntity;
import model.MerkleNode;
import model.Tweet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SendRequestHandlerTests {

  private void sendAndReceiveData(HashableEntity entity, EnvelopeType envelopeType) {
    Envelope<HashableEntity> envelope = new Envelope<>(entity, envelopeType);
    try (ServerSocket serverSocket = new ServerSocket(10_000)) {

      // send block to other socket
      Socket socketToSendDataTo = new Socket("localhost", 10_000);
      SendRequestHandler.sendData(serverSocket.accept(), envelope);

      // receive block from other socket
      InputStream inputStream = socketToSendDataTo.getInputStream();
      String xmlString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

      // close connections
      inputStream.close();
      socketToSendDataTo.close();

      // unmarshall message
      P2PMarshaller<HashableEntity> marshaller = new P2PMarshaller<>();

      Assertions.assertEquals(envelope, marshaller.unmarshal(new StringReader(xmlString)));
    } catch (IOException e) {
      e.printStackTrace();
      Assertions.fail();
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail();
    }
  }

  @Test
  public void testSendBlock() {
    Block block = new Block("testHash", "hashOfMerkleNode", System.currentTimeMillis());
    sendAndReceiveData(block, EnvelopeType.BLOCK_RECEIVE);
  }

  @Test
  public void testSendTweet() {
    Tweet tweet = new Tweet("testTweet", "testUser", "Signature", System.currentTimeMillis());
    sendAndReceiveData(tweet, EnvelopeType.TWEET_RECEIVE);
  }

  @Test
  public void testSendBlockWithValidation() {
    Tweet tweet = new Tweet("testTweet", "testUser", "Signature", System.currentTimeMillis());
    BlockWithValidation blockWithValidation = new BlockWithValidation("testHash", "hashOfMerkleNode",
      System.currentTimeMillis(), true, "publicKeyValidator", List.of(tweet));
    sendAndReceiveData(blockWithValidation, EnvelopeType.BLOCK_RECEIVE);
  }

  @Test
  public void testSendMerkleNode() {
    Tweet tweet = new Tweet("testTweet", "testUser", "Signature", System.currentTimeMillis());
    MerkleNode left = new MerkleNode(null, null, tweet);
    MerkleNode right = new MerkleNode(null, null, tweet);
    MerkleNode merkleNode = new MerkleNode(left, right, tweet);

    sendAndReceiveData(merkleNode, EnvelopeType.BLOCK_VALIDATION_REQUEST);
  }
}
