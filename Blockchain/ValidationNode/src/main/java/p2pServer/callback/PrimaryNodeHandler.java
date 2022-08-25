package p2pServer.callback;

import blockBuilder.BlockBuilder;
import connection.ConnectionDetails;
import datastructure.Tuple;
import envelope.BlockWithValidation;
import envelope.Envelope;
import envelope.EnvelopeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Block;
import model.Tweet;
import validation.BlockValidator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class PrimaryNodeHandler {
  private final List<Envelope<Tweet>> tweetQueue;
  private final List<Tuple<ConnectionDetails, BlockWithValidation>> pendingBlocks;
  private final ConnectionDetails nodeConnectionDetails;
  private final BlockBuilder blockBuilder;
  private final BlockValidator blockValidator;

  /**
   * @param publicKeyOfPrimaryNode the public that should be checked if it is the primary node
   * @return true if the publicKey is the same as the publicKey of the primary node, false otherwise
   */
  private boolean isCurrentNodePrimary(String publicKeyOfPrimaryNode) {
    return this.nodeConnectionDetails.getPublicKey().equals(publicKeyOfPrimaryNode);
  }

  /**
   * @param timestampOfNodeSelection the timestamp of the node selection
   * @return list with the tweets that were created before the timestamp of the node selection
   */
  private List<Tweet> getTweetsForBlockValidation(Long timestampOfNodeSelection) {
    return tweetQueue
      .stream()
      .filter(envelope -> envelope.getTimestamp() < timestampOfNodeSelection)
      .map(Envelope::getData)
      .collect(Collectors.toList());
  }

  /**
   * @param tweets the tweets that should be included in the block
   * @return a block with the tweets and a validation
   */
  private BlockWithValidation buildBlock(List<Tweet> tweets) {
    Block block = blockBuilder.build(tweets);

    var blockWithValidation = new BlockWithValidation(block, true,
      this.nodeConnectionDetails.getPublicKey(), tweets);

    if (blockValidator.validateBlock(blockWithValidation)) {
      log.info("Block successfully mined and ready to be checked by other nodes, blockHash: {}", block.getHash());
      return blockWithValidation;
    }

    log.info("Block could not be mined because it is not valid, blockHash: {}", block.getHash());
    return new BlockWithValidation(block, false, this.nodeConnectionDetails.getPublicKey(), tweets);
  }

  /**
   * @param payload the payload that contains the {@link ConnectionDetails} of the primary node
   * @return the {@link BlockWithValidation} that was mined by the primary node
   */
  public Optional<Envelope<BlockWithValidation>> onPrimaryNodeSelected(Envelope<ConnectionDetails>
                                                                         payload) {
    assert (payload.getType() == EnvelopeType.PRIMARY_NODE_SELECTION);

    List<Tweet> tweets = getTweetsForBlockValidation(payload.getTimestamp());

    if (tweets.size() == 0) {
      log.info("No tweets to be included in the block");
      return Optional.empty();
    }

    // if current node was not selected as primary node return and wait for the primary node to send a block
    if (!isCurrentNodePrimary(payload.getData().getPublicKey())) {
      synchronized (this) {
        log.info("Adding pending block for node {}", payload.getData().getPublicKey());
        // add the primary node to the list of nodes that have not yet mined their assigned block
        pendingBlocks.add(new Tuple<>(payload.getData(), null));
        // remove all old tweets from the queue
        tweetQueue.removeIf(tweetEnvelope -> tweets.contains(tweetEnvelope.getData()));
      }
      return Optional.empty();
    }

    log.info("Node {} is selected as primary node", this.nodeConnectionDetails.getPublicKey());
    log.info("Mining new block with {} tweets", tweets.size());

    BlockWithValidation blockWithValidation = buildBlock(tweets);

    synchronized (this) {
      tweetQueue.removeIf(tweetEnvelope -> tweets.contains(tweetEnvelope.getData()));
      pendingBlocks.add(new Tuple<>(this.nodeConnectionDetails, blockWithValidation));
    }

    return Optional.of(new Envelope<>(blockWithValidation, EnvelopeType.BLOCK_RECEIVE));
  }
}
