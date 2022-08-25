package model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@Entity
@Table(name = "tweets")
@XmlRootElement(name = "tweet")
@XmlAccessorType(XmlAccessType.NONE)
public class Tweet implements HashableEntity {
  @Id
  @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
  @Getter
  @Setter
  @XmlElement(name = "id")
  private Long id;

  @Getter
  @Setter
  @XmlElement(name = "content", required = true)
  @Column(length = 10_000)
  private String content;

  @Getter
  @Setter
  @XmlElement(name = "publicKeyOfCreator", required = true)
  @Column(nullable = false, length = 2048)
  private String publicKeyOfCreator;

  /**
   * The signature as a Base64 encoded string.
   * The signature is calculated with the private key of the sender.
   */
  @Getter
  @Setter
  @XmlElement(name = "signature", required = true)
  @Column(nullable = false, length = 2048)
  private String signature;

  @Getter
  @Setter
  @XmlElement(name = "timeStamp", required = true)
  private long timestamp;

  public Tweet(String message, String publicKeyOfCreator, String signature, long timestamp) {
    this.content = message;
    this.signature = signature;
    this.timestamp = timestamp;
    this.publicKeyOfCreator = publicKeyOfCreator;
  }

  public Tweet() {
  }

  @Override
  public String getStringToHash() {
    return content + signature + timestamp;
  }

  @Override
  public String toString() {
    return "Tweet{" +
      "id='" + id + '\'' +
      ", content='" + content + '\'' +
      ", publicKeyOfCreator='" + publicKeyOfCreator + '\'' +
      ", signature='" + signature + '\'' +
      ", timestamp=" + timestamp +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Tweet tweet = (Tweet) o;
    return tweet.getStringToHash().equals(this.getStringToHash());
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, signature, timestamp);
  }
}
