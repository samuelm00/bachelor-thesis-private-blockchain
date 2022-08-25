package validation;

import datastructure.MerkleTree;
import envelope.BlockWithValidation;
import lombok.extern.slf4j.Slf4j;
import repository.BlockRepository;

@Slf4j
public class BlockValidator {
  private final BlockRepository repository;

  public BlockValidator(BlockRepository repository) {
    this.repository = repository;
  }

  /**
   * Validates a block by checking if the tweets are valid and if the merkleRoot is correct.
   *
   * @param block The block to validate.
   * @return True if the block is valid, false otherwise.
   */
  public boolean validateBlock(BlockWithValidation block) {
    boolean areTweetsValid = block.getTweets().stream().allMatch(TweetValidator::isTweetValid);

    MerkleTree merkleTree = new MerkleTree();
    merkleTree.build(block.getTweets());
    boolean isMerkleTreeValid = merkleTree.getRoot().getHash().equals(block.getHashOfMerkleTreeRoot());

    boolean previousHashKnown = repository.findByHash(block.getPreviousHash()).isPresent();

    log.info("Block valid? Merkle tree {}, tweets {}, previous hash known {}", isMerkleTreeValid, areTweetsValid,
      previousHashKnown);

    return isMerkleTreeValid && areTweetsValid && previousHashKnown;
  }
}
