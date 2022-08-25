package model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlockTests {

  @Test
  public void testBlockCreation() {
    Block block = new Block("jfdskfjasldjflsjdf", "jdfklajdsfjadljf", System.currentTimeMillis());
    Assertions.assertNotNull(block);
    Assertions.assertTrue(block.getHash().length() > 0);
  }
}
