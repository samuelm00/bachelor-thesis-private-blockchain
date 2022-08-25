package model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.hash.HashUtil;

public class MerkleNodeTests {
  @Test
  public void testMerkleNode() {
    Tweet testTweet = new Tweet("Test Messsage",
      "sjdfksdjflkasdj", "jdfklsj", System.currentTimeMillis());
    Tweet testTweet2 = new Tweet("Test Messsage",
      "sjdfksdjflkasdj", "kdlfjsjfdl", System.currentTimeMillis());

    MerkleNode left = new MerkleNode(null, null, testTweet);
    Assertions.assertEquals(left.getHash(), HashUtil.hashEntity(testTweet));

    MerkleNode right = new MerkleNode(null, null, testTweet2);
    Assertions.assertEquals(right.getHash(), HashUtil.hashEntity(testTweet2));

    MerkleNode node = new MerkleNode(left, right, null);
    Assertions.assertEquals(left, node.getLeft());
    Assertions.assertEquals(right, node.getRight());
    Assertions.assertEquals(node.getHash(), HashUtil.hashEntity(node));
  }
}
