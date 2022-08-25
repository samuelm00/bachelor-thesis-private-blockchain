package p2pServer.callback;

import callback.BlockValidationCounter;
import datastructure.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class BlockValidationCounterTests {

  @Test
  public void testAddBlockValidationResponse() {
    BlockValidationCounter counter = new BlockValidationCounter(5);
    String blockHash = "blockHash";
    Tuple<String, Boolean> response = new Tuple<>("myPublicKey", true);
    counter.addBlockValidationResponse(blockHash, response);

    Assertions.assertEquals(1, counter.getBlockValidationResponses(blockHash).size());
    Assertions.assertEquals(counter.getBlockValidationResponses(blockHash).get(0), response);

    counter.addBlockValidationResponse(blockHash, response);
    Assertions.assertEquals(1, counter.getBlockValidationResponses(blockHash).size());
  }

  @Test
  public void testIsBlockConsideredValid() {
    BlockValidationCounter counter = new BlockValidationCounter(5);
    String blockHash = "blockHash";

    for (int i = 0; i < 5; i++) {
      Tuple<String, Boolean> response = new Tuple<>("myPublicKey" + i, i < 3);
      counter.addBlockValidationResponse(blockHash, response);
    }

    Assertions.assertEquals(5, counter.getBlockValidationResponses(blockHash).size());
    Assertions.assertTrue(counter.isBlockConsideredValid(blockHash));
  }

  @Test
  public void testIsBlockValidFail() {
    BlockValidationCounter counter = new BlockValidationCounter(5);
    String blockHash = "blockHash";

    for (int i = 0; i < 5; i++) {
      Tuple<String, Boolean> response = new Tuple<>("myPublicKey" + i, i < 2);
      counter.addBlockValidationResponse(blockHash, response);
    }

    Assertions.assertEquals(5, counter.getBlockValidationResponses(blockHash).size());
    Assertions.assertFalse(counter.isBlockConsideredValid(blockHash));
  }

}
