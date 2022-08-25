package datastructure;

import model.Tweet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class MerkleTreeTests {
  private final List<Tweet> testTweets = new ArrayList<>();

  private void generateTestData(int nrOfMessages) {
    for (long i = 0; i < nrOfMessages; i++) {
      Tweet tweet = new Tweet("Test Message" + i,
        "Signature" + i, "fdjskljfd", System.currentTimeMillis());
      tweet.setId(i);
      testTweets.add(tweet);
    }
  }

  @Test
  public void merkleTreeConstructionEvenNrOfMessages() {
    generateTestData(10);
    MerkleTree merkleTree = new MerkleTree();
    merkleTree.build(testTweets);

    for (Tweet tweet : testTweets) {
      Assertions.assertTrue(merkleTree.containsTweet(tweet.getId()));
    }

    for (long i = 10; i < 20; i++) {
      Assertions.assertFalse(merkleTree.containsTweet(i));
    }
  }

  @Test
  public void merkleTreeConstructionOddNrOfMessages() {
    generateTestData(15);
    MerkleTree merkleTree = new MerkleTree();
    merkleTree.build(testTweets);

    for (Tweet tweet : testTweets) {
      Assertions.assertTrue(merkleTree.containsTweet(tweet.getId()));
    }

    for (long i = 15; i < 25; i++) {
      Assertions.assertFalse(merkleTree.containsTweet(i));
    }
  }

  @Test
  public void merkleTreeBranchingEven() {
    generateTestData(10);
    MerkleTree merkleTree = new MerkleTree();
    merkleTree.build(testTweets);

    MerkleTree branch = merkleTree.getBranchForTweetId(testTweets.get(0).getId());
    Assertions.assertTrue(branch.containsTweet(testTweets.get(0).getId()));
  }

  @Test
  public void merkleTreeBranchingOdd() {
    generateTestData(15);
    MerkleTree merkleTree = new MerkleTree();
    merkleTree.build(testTweets);

    MerkleTree branch = merkleTree.getBranchForTweetId(testTweets.get(0).getId());
    Assertions.assertTrue(branch.containsTweet(testTweets.get(0).getId()));
  }

  @Test
  public void validateMerkleTree() {
    generateTestData(10);
    MerkleTree merkleTree = new MerkleTree();
    merkleTree.build(testTweets);
    Assertions.assertTrue(merkleTree.validateTree(this.testTweets));

    generateTestData(10);
    Assertions.assertFalse(merkleTree.validateTree(this.testTweets));
  }
}
