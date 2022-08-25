package model;

import lombok.Getter;
import lombok.Setter;
import util.hash.HashUtil;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@Entity
@Table(name = "merkle_nodes")
@XmlRootElement(name = "merkle_node")
@XmlAccessorType(XmlAccessType.NONE)
public class MerkleNode implements HashableEntity {
  @Id
  @Getter
  @Setter
  @XmlElement(name = "hash", required = true)
  @Column(name = "hash", nullable = false, unique = true, length = 256)
  private String hash;

  @Getter
  @Setter
  @XmlElement(name = "left", type = MerkleNode.class)
  @OneToOne
  private MerkleNode left;

  @Getter
  @Setter
  @XmlElement(name = "right", type = MerkleNode.class)
  @OneToOne
  private MerkleNode right;

  @Getter
  @Setter
  @XmlElement(name = "tweet", type = Tweet.class, required = true)
  @OneToOne
  @JoinColumn(name = "tweet_id", referencedColumnName = "id")
  private Tweet tweet;

  public MerkleNode(MerkleNode left, MerkleNode right, Tweet data) {
    this.left = left;
    this.right = right;
    this.tweet = data;
    this.hash = HashUtil.hash(getStringToHash());
  }

  public MerkleNode() {
  }

  public boolean hasChildren() {
    return left != null || right != null;
  }

  public boolean containsTweet(Long tweetId) {
    if (tweetId == null || this.tweet == null) {
      return false;
    }
    return this.tweet.getId().equals(tweetId);
  }

  @Override
  public String getStringToHash() {
    if (hasChildren()) {
      return left.getHash() + right.getHash();
    }
    return tweet.getStringToHash();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MerkleNode that = (MerkleNode) o;
    return Objects.equals(hash, that.hash) && Objects.equals(left, that.left) && Objects.equals(right, that.right) && Objects.equals(tweet, that.tweet);
  }
}
