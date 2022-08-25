package restPayload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class TweetRequestPayload {
  @Getter
  @Setter
  private String hashOfMerkleNode;
}
