package restPayload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class AuthRequestPayload {
  @Getter
  @Setter
  private String publicKey;

  @Getter
  @Setter
  private String password;

  public AuthRequestPayload(String publicKey, String password) {
    this.publicKey = publicKey;
    this.password = password;
  }
}
