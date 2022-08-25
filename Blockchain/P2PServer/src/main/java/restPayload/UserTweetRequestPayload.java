package restPayload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class UserTweetRequestPayload {
  @Getter
  @Setter
  private String publicKey;
}
