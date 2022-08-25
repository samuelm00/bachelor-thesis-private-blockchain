package callback;

import connection.ConnectionDetails;
import envelope.BlockWithValidation;
import envelope.Envelope;
import envelope.EnvelopeType;
import model.Tweet;

import java.util.Optional;

/**
 * Interface that contains a callback method for every @{@link EnvelopeType}
 */
public interface P2PCallback {
  /**
   * The callback method that is called when a tweet is received.
   * If the method returns an envelope then this envelope will be sent to all the other nodes.
   *
   * @param tweetEnvelope envelope contain {@link EnvelopeType#TWEET_RECEIVE}
   * @return the same value that was passed as argument
   */
  Optional<Envelope<Tweet>> onTweetReceived(Envelope<Tweet> tweetEnvelope);

  /**
   * The callback method that is called when another node send a block that was mined and needs verification.
   * If the method returns an envelope then this envelope will be sent to all the other nodes.
   *
   * @param blockEnvelope envelope contain {@link EnvelopeType#BLOCK_RECEIVE}
   * @return the same value that was passed as argument
   */
  Optional<Envelope<BlockWithValidation>> onBlockReceived(Envelope<BlockWithValidation> blockEnvelope);

  /**
   * The callback method that is called when the node was selected to mine a block.
   */
  Optional<Envelope<BlockWithValidation>> onPrimaryNodeSelected(Envelope<ConnectionDetails> primaryNodeConnectionDetails);
}
