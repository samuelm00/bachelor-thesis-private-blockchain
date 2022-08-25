package p2pServer.callback;

import blockBuilder.BlockBuilder;
import callback.BlockValidationCounter;
import callback.P2PCallback;
import connection.ConnectionDetails;
import datastructure.Tuple;
import envelope.BlockWithValidation;
import envelope.Envelope;
import envelope.EnvelopeType;
import lombok.extern.slf4j.Slf4j;
import model.Tweet;
import repository.BlockRepository;
import repository.MerkleNodeRepository;
import repository.TweetRepository;
import validation.BlockValidator;

import java.util.*;


@Slf4j
public class ValidationCallbackHandler implements P2PCallback {
  private final BlockValidationHandler blockValidationHandler;
  private final PrimaryNodeHandler primaryNodeHandler;
  private final List<Envelope<Tweet>> tweetQueue;

  public ValidationCallbackHandler(List<Envelope<Tweet>> tweetQueue, int nrOfValidationNodes, ConnectionDetails nodeConnectionDetails,
                                   BlockRepository blockRepository, MerkleNodeRepository merkleNodeRepository, TweetRepository tweetRepository) {
    this.tweetQueue = tweetQueue;
    List<Tuple<ConnectionDetails, BlockWithValidation>> pendingBlocks = new ArrayList<>();
    BlockValidationCounter blockValidationCounter = new BlockValidationCounter(nrOfValidationNodes);
    BlockValidator blockValidator = new BlockValidator(blockRepository);
    BlockBuilder blockBuilder = new BlockBuilder(blockRepository);
    this.blockValidationHandler = new BlockValidationHandler(tweetQueue, pendingBlocks, nodeConnectionDetails,
      blockValidationCounter, blockValidator, blockRepository, merkleNodeRepository, tweetRepository);
    this.primaryNodeHandler = new PrimaryNodeHandler(tweetQueue, pendingBlocks, nodeConnectionDetails, blockBuilder,
      blockValidator);
  }

  /**
   * Adds the tweet to the queue, if not already added.
   * This method uses the {@link #tweetQueue}, that is shared between multiple threads. But thread
   * safety is guaranteed.
   *
   * @param tweetEnvelope the tweet to add
   * @return the envelope that was passed as argument
   */
  @Override
  public Optional<Envelope<Tweet>> onTweetReceived(Envelope<Tweet> tweetEnvelope) {
    assert (tweetEnvelope.getType() == EnvelopeType.TWEET_RECEIVE);
    synchronized (this) {
      if (!tweetQueue.contains(tweetEnvelope)) {
        tweetQueue.add(tweetEnvelope);
      }
    }
    log.info("Tweet successfully added to queue");
    return Optional.empty();
  }

  /**
   * Checks if enough validation nodes have validated the block.
   * If so, the block is added to the blockchain else we wait for the other responses.
   *
   * @param blockEnvelope envelope contains {@link EnvelopeType#BLOCK_RECEIVE}
   * @return empty optional
   */
  @Override
  public Optional<Envelope<BlockWithValidation>> onBlockReceived(Envelope<BlockWithValidation> blockEnvelope) {
    return blockValidationHandler.onBlockReceived(blockEnvelope);
  }

  /**
   * Called when a node is selected as primary node and should mine a new block.
   */
  @Override
  public Optional<Envelope<BlockWithValidation>> onPrimaryNodeSelected(Envelope<ConnectionDetails> payload) {
    return primaryNodeHandler.onPrimaryNodeSelected(payload);
  }
}
