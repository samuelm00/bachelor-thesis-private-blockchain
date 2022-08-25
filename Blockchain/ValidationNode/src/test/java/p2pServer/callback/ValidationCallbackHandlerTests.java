package p2pServer.callback;

import connection.ConnectionDetails;
import envelope.Envelope;
import envelope.EnvelopeType;
import model.Tweet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ValidationCallbackHandlerTests {
  private List<Envelope<Tweet>> tweetQueue;
  private ValidationCallbackHandler validationCallbackHandler;

  @BeforeEach
  public void init() {
    tweetQueue = new ArrayList<>();
    ConnectionDetails connectionDetails = new ConnectionDetails(10, 12, "localhost", "localhost");
    validationCallbackHandler = new ValidationCallbackHandler(tweetQueue, 5, connectionDetails, null, null, null);
  }

  private Envelope<Tweet> getSimpleTweetEnvelope(EnvelopeType type) {
    return new Envelope<>(new Tweet("tweet", "key", "signature",
      System.currentTimeMillis()), type);
  }

  @Test
  public void testOnTweetReceive() {
    Envelope<Tweet> tweetEnvelope = getSimpleTweetEnvelope(EnvelopeType.TWEET_RECEIVE);
    validationCallbackHandler.onTweetReceived(tweetEnvelope);
    Envelope<Tweet> tweetEnvelope2 = getSimpleTweetEnvelope(EnvelopeType.TWEET_RECEIVE);
    tweetEnvelope2.getData().setContent("tweet2");
    validationCallbackHandler.onTweetReceived(tweetEnvelope2);
    Assertions.assertEquals(2, tweetQueue.size());
  }

  @Test
  public void testOnTweetValidationMultithreaded() throws InterruptedException {
    Envelope<Tweet> tweetEnvelope = getSimpleTweetEnvelope(EnvelopeType.TWEET_RECEIVE);
    ExecutorService executorService = Executors.newFixedThreadPool(3);

    Thread thread1 = new Thread(() -> validationCallbackHandler.onTweetReceived(tweetEnvelope));
    Thread thread2 = new Thread(() -> validationCallbackHandler.onTweetReceived(tweetEnvelope));
    Thread thread3 = new Thread(() -> validationCallbackHandler.onTweetReceived(tweetEnvelope));

    executorService.execute(thread1);
    executorService.execute(thread2);
    executorService.execute(thread3);

    executorService.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS);

    Assertions.assertEquals(1, tweetQueue.size());
  }
}
