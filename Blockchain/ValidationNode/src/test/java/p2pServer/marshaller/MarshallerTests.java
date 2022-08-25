package p2pServer.marshaller;

import envelope.BlockWithValidation;
import envelope.Envelope;
import envelope.EnvelopeType;
import marshaller.P2PMarshaller;
import model.Block;
import model.MerkleNode;
import model.Tweet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;


public class MarshallerTests {

  @Test
  public void testBlockMarshalling() {
    Block block = new Block("testHash", "hashOfMerkleNode", System.currentTimeMillis());
    try {
      P2PMarshaller<Block> marshaller = new P2PMarshaller<>();
      Envelope<Block> envelope = new Envelope<>(block, EnvelopeType.BLOCK_RECEIVE);
      String xmlString = marshaller.marshal(envelope);
      Assertions.assertEquals(envelope, marshaller.unmarshal(new StringReader(xmlString)));
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail();
    }
  }

  @Test
  public void testMerkleNodeMarshalling() {
    Tweet tweet = new Tweet("testTweet", "testUser", "testHash", System.currentTimeMillis());
    MerkleNode right = new MerkleNode(null, null, tweet);
    MerkleNode left = new MerkleNode(null, null, tweet);
    MerkleNode merkleNode = new MerkleNode(left, right, tweet);
    try {
      P2PMarshaller<MerkleNode> marshaller = new P2PMarshaller<>();
      Envelope<MerkleNode> envelope = new Envelope<>(merkleNode, EnvelopeType.BLOCK_RECEIVE);
      String xmlString = marshaller.marshal(envelope);
      Assertions.assertEquals(envelope, marshaller.unmarshal(new StringReader(xmlString)));
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail();
    }
  }

  @Test
  public void testTweetMarshalling() {
    Tweet tweet = new Tweet("testTweet", "testUser", "testHash", System.currentTimeMillis());
    try {
      P2PMarshaller<Tweet> marshaller = new P2PMarshaller<>();
      Envelope<Tweet> envelope = new Envelope<>(tweet, EnvelopeType.TWEET_RECEIVE);
      String xmlString = marshaller.marshal(envelope);
      Assertions.assertEquals(envelope, marshaller.unmarshal(new StringReader(xmlString)));
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail();
    }
  }

  @Test
  public void testBlockWithValidationMarshalling() {
    Tweet tweet = new Tweet("testTweet", "testUser", "testHash", System.currentTimeMillis());
    BlockWithValidation blockWithValidation = new BlockWithValidation("testHash", "hashOfMerkleNode", System.currentTimeMillis(),
      true, "publicKey", List.of(tweet));
    try {
      P2PMarshaller<BlockWithValidation> marshaller = new P2PMarshaller<>();
      Envelope<BlockWithValidation> envelope = new Envelope<>(blockWithValidation, EnvelopeType.BLOCK_RECEIVE);
      String xmlString = marshaller.marshal(envelope);
      Assertions.assertEquals(envelope, marshaller.unmarshal(new StringReader(xmlString)));
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail();
    }
  }
}
