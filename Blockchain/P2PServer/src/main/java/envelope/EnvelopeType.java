package envelope;

public enum EnvelopeType {
  BLOCK_VALIDATION_REQUEST("BLOCK_VALIDATION_REQUEST"), BLOCK_RECEIVE("BLOCK_RECEIVE"),
  TWEET_RECEIVE("TWEET_RECEIVE"), PRIMARY_NODE_SELECTION("PRIMARY_NODE_SELECTION");

  private final String type;

  EnvelopeType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
