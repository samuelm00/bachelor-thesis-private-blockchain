package util.hash;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HashUtilTests {

  @Test
  public void testHash() {
    String stringToHash = "Hello World!";
    Assertions.assertTrue(HashUtil.hash(stringToHash).length() > 0);
  }

  @Test
  public void testEntityHash() {
    String stringToHash = "Hello World!";
    Assertions.assertTrue(HashUtil.hashEntity(() -> stringToHash).length() > 0);
  }
}
