package callback;

import datastructure.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockValidationCounter {
  /**
   * Key (MAP): Hash of the block, Value: List of nodes that have sent a validation message for this block.
   * Key(Tuple): Public key of the node, Value(Tuple): true, false (whether it thinks the block is valid or not)
   */
  private final Map<String, List<Tuple<String, Boolean>>> blockValidationResponses = new HashMap<>();
  private final int nrOfValidators;

  public BlockValidationCounter(int nrOfValidators) {
    this.nrOfValidators = nrOfValidators;
  }

  /**
   * Adds a new node to the list of nodes that have sent a validation message for this block.
   *
   * @param blockHash Hash of the block (if already exists, nothing happens)
   * @param response  Tuple containing the public key of the node and whether it thinks the block is valid or not
   */
  public void addBlockValidationResponse(String blockHash, Tuple<String, Boolean> response) {
    if (blockValidationResponses.containsKey(blockHash)) {
      List<Tuple<String, Boolean>> responses = blockValidationResponses.get(blockHash);
      if (!responses.contains(response)) {
        List<Tuple<String, Boolean>> newResponses = new ArrayList<>(responses);
        newResponses.add(response);
        blockValidationResponses.put(blockHash, newResponses);
      }
    } else {
      blockValidationResponses.put(blockHash, List.of(response));
    }
  }

  /**
   * @param blockHash Hash of the block
   * @return true if enough nodes have validated this block with true
   */
  public boolean isBlockConsideredValid(String blockHash) {
    if (!blockValidationResponses.containsKey(blockHash)) {
      return false;
    }
    long nrOfConfirmingResponses = blockValidationResponses
      .get(blockHash)
      .stream()
      .filter(Tuple::getValue)
      .count();
    return nrOfConfirmingResponses >= (nrOfValidators / 2) + 1;
  }

  /**
   * @param blockHash Hash of the block
   * @return true if enough nodes validated this block with false otherwise false
   */
  public boolean isBlockConsideredInvalid(String blockHash) {
    if (!blockValidationResponses.containsKey(blockHash)) {
      return false;
    }
    long nrOfConfirmingResponses = blockValidationResponses
      .get(blockHash)
      .stream()
      .filter(tuple -> !tuple.getValue())
      .count();
    return nrOfConfirmingResponses < (nrOfValidators / 2) + 1;
  }

  /**
   * @param blockHash Hash of the block
   * @param publicKey Public key of the node
   * @return true if the node has sent a validation message for this block false otherwise
   */
  public boolean alreadyValidated(String blockHash, String publicKey) {
    return blockValidationResponses.get(blockHash).stream().anyMatch(tuple -> tuple.getKey().equals(publicKey));
  }

  /**
   * removes all validation responses for the given block
   *
   * @param blockHash Hash of the block
   */
  public void clearResponses(String blockHash) {
    blockValidationResponses.remove(blockHash);
  }

  /**
   * @param blockHash Hash of the block
   * @return the list of the nodes that have sent a validation message for this block
   */
  public List<Tuple<String, Boolean>> getBlockValidationResponses(String blockHash) {
    return blockValidationResponses.get(blockHash);
  }
}
