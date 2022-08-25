package model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Getter
  @Setter
  private String password;

  @Getter
  @Column(unique = true, nullable = false, length = 2048)
  private String publicKey;

  public User(String publicKey, String password) {
    this.password = password;
    this.publicKey = publicKey;
  }
}
