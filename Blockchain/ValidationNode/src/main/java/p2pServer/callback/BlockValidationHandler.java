package p2pServer.callback;

import callback.BlockValidationCounter;
import connection.ConnectionDetails;
import datastructure.MerkleTree;
import datastructure.Tuple;
import envelope.BlockWithValidation;
import envelope.Envelope;
import envelope.EnvelopeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Block;
import model.Tweet;
import repository.BlockRepository;
import repository.MerkleNodeRepository;
import repository.TweetRepository;
import validation.BlockValidator;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class BlockValidationHandler {
  private final List<Envelope<Tweet>> tweetQueue;
  private final List<Tuple<ConnectionDetails, BlockWithValidation>> pendingBlocks;
  private final ConnectionDetails nodeConnectionDetails;
  private final BlockValidationCounter blockValidationCounter;
  private final BlockValidator blockValidator;
  private final BlockRepository blockRepository;
  private final MerkleNodeRepository merkleNodeRepository;
  private final TweetRepository tweetRepository;

  /**
   * @param hash the hash of the block that should be checked if it is already in the blockchain
   * @return true if the block is already in the blockchain, false otherwise
   */
  private boolean isBlockAlreadyInLedger(String hash) {
    return blockRepository.findByHash(hash).isPresent();
  }

  /**
   * @param tweets the tweets that should be included in the merkle tree
   */
  private void saveMerkleTreeToLedger(List<Tweet> tweets) {
    MerkleTree merkleTree = new MerkleTree();
    merkleTree.buildAndSaveMerkleTree(tweets, merkleNodeRepository::save);
    log.info("Merkle Tree successfully added to ledger, {}", merkleTree.getRoot().getHash());
  }

  /**
   * @param tweets the tweets that should be saved to the ledger
   * @return the tweets that were successfully saved to the ledger
   */
  private List<Tweet> saveTweets(List<Tweet> tweets) {
    return tweetRepository.saveAll(tweets);
  }

  /**
   * Adds the block to the ledger
   *
   * @param blockWithValidation the block to add
   */
  private synchronized void addBlockToLedger(BlockWithValidation blockWithValidation) {
    Block blockToAdd = new Block(blockWithValidation.getPreviousHash(), blockWithValidation.getHashOfMerkleTreeRoot(),
      blockWithValidation.getTimeStamp());

    if (isBlockAlreadyInLedger(blockWithValidation.getHash())) {
      log.info("Block already exists in ledger");
      return;
    }

    blockRepository.save(blockToAdd);

    List<Tweet> tweets = saveTweets(blockWithValidation.getTweets());
    log.info("Tweets successfully added to ledger, {}", tweets.size());

    saveMerkleTreeToLedger(tweets);
  }

  /**
   * @param publicKeyOfBlockCreator the public key of the node that created the block
   * @return true if the block comes from the node that created it (or if there already exists a response of the primary node), false otherwise
   */
  private boolean isBlockComingFromNonPrimaryNode(String publicKeyOfBlockCreator) {
    return !this.pendingBlocks.get(0).getKey().getPublicKey().equals(publicKeyOfBlockCreator) &&
      this.pendingBlocks.get(0).getValue() == null;
  }

  /**
   * @param envelope the envelope that contains the block that should be validated
   * @return a block with validation
   */
  private BlockWithValidation validateBlock(Envelope<BlockWithValidation> envelope) {
    boolean isBlockValid = blockValidator.validateBlock(envelope.getData());

    log.info("Block {} validation result: {}", envelope.getData().getHash(), isBlockValid);

    BlockWithValidation blockWithValidation = new BlockWithValidation(envelope.getData());
    blockWithValidation.setValid(isBlockValid);
    blockWithValidation.setPublicKeyOfValidator(this.nodeConnectionDetails.getPublicKey());
    return blockWithValidation;
  }

  /**
   * @param blockEnvelope envelope with the block what will be added to the ledger
   */
  private synchronized void handleBlockIsValid(Envelope<BlockWithValidation> blockEnvelope) {
    addBlockToLedger(blockEnvelope.getData());

    blockValidationCounter.clearResponses(blockEnvelope.getData().getHash());
    this.pendingBlocks.remove(0);
    blockEnvelope.getData().getTweets().forEach(tweet ->
      tweetQueue.removeIf(envelope -> envelope.getData().equals(tweet)));
  }

  /**
   * @param blockEnvelope envelope with the block that will not be included in the ledger because
   *                      the validation nodes don't consider the block valid.
   */
  private synchronized void handleBLockIsInValid(Envelope<BlockWithValidation> blockEnvelope) {
    blockValidationCounter.clearResponses(blockEnvelope.getData().getHash());
    this.pendingBlocks.remove(0);
    blockEnvelope.getData().getTweets().forEach(tweet ->
      tweetQueue.removeIf(envelope -> envelope.getData().equals(tweet)));
  }

  /**
   * @param blockEnvelope the envelope that contains a validated block by another node
   * @return a block with validation if the block was not yet validated by this node
   */
  public Optional<Envelope<BlockWithValidation>> onBlockReceived
  (Envelope<BlockWithValidation> blockEnvelope) {
    assert (blockEnvelope.getType() == EnvelopeType.BLOCK_RECEIVE);

    Envelope<BlockWithValidation> response = null;

    log.info("Received block validation response, hash {}, from {}", blockEnvelope.getData().getHash(),
      blockEnvelope.getData().getPublicKeyOfValidator());

    if (this.pendingBlocks.isEmpty()) {
      log.info("No pending blocks therefore ignore the incoming block validation response");
      return Optional.empty();
    }

    // if we receive a block from a validator that it not the primary node before the primary node has finished we ignore it
    if (isBlockComingFromNonPrimaryNode(blockEnvelope.getData().getPublicKeyOfValidator())) {
      log.info("Block {} is coming from {}, a validator that is not the primary node (primary node = {}), ignoring it",
        blockEnvelope.getData().getPublicKeyOfValidator(), blockEnvelope.getData().getHash(), this.pendingBlocks.get(0).getKey().getPublicKey());
      return Optional.empty();
    }

    synchronized (this) {
      blockValidationCounter.addBlockValidationResponse(blockEnvelope.getData().getHash(),
        new Tuple<>(blockEnvelope.getData().getPublicKeyOfValidator(), blockEnvelope.getData().isValid()));
    }

    // validate block if not yet validated by this node
    if (!blockValidationCounter.alreadyValidated(blockEnvelope.getData().getHash(),
      this.nodeConnectionDetails.getPublicKey())) {

      BlockWithValidation blockWithValidation = validateBlock(blockEnvelope);
      response = new Envelope<>(blockWithValidation, EnvelopeType.BLOCK_RECEIVE);

      synchronized (this) {
        blockValidationCounter.addBlockValidationResponse(blockEnvelope.getData().getHash(),
          new Tuple<>(this.nodeConnectionDetails.getPublicKey(), blockWithValidation.isValid()));
      }
    }

    if (blockValidationCounter.isBlockConsideredValid(blockEnvelope.getData().getHash())) {
      log.info("Block is valid and will be added to the ledger, hash {}", blockEnvelope.getData().getHash());
      handleBlockIsValid(blockEnvelope);
    }

    if (blockValidationCounter.isBlockConsideredInvalid(blockEnvelope.getData().getHash())) {
      log.info("Block is invalid and will not be added to the ledger, hash {}", blockEnvelope.getData().getHash());
      handleBLockIsInValid(blockEnvelope);
    }

    return Optional.ofNullable(response);
  }
}
