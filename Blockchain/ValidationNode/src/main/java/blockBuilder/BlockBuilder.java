package blockBuilder;

import datastructure.MerkleTree;
import model.Block;
import model.Tweet;
import repository.BlockRepository;

import java.util.List;
import java.util.Optional;

public class BlockBuilder {
  private final BlockRepository blockRepository;

  public BlockBuilder(BlockRepository blockRepository) {
    this.blockRepository = blockRepository;
  }

  /**
   * Builds a block with the given messages
   *
   * @param tweets the messages to be included in the block
   * @return the block
   */
  public Block build(List<Tweet> tweets) {
    MerkleTree merkleTree = new MerkleTree();
    merkleTree.build(tweets);
    return new Block(getPreviousHash(), merkleTree.getRoot().getHash(), System.currentTimeMillis());
  }

  /**
   * Gets the previous block hash
   *
   * @return the previous block hash
   */
  private String getPreviousHash() {
    Optional<Block> lastBlock = blockRepository.findFirstByOrderByTimeStampDesc();
    assert (lastBlock.isPresent());
    return lastBlock.get().getHash();
  }
}
