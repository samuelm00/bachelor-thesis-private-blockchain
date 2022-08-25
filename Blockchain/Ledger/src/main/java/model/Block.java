package model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import util.hash.HashUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@Entity
@Table(name = "blocks")
@XmlRootElement(name = "block")
@XmlAccessorType(XmlAccessType.NONE)
public class Block implements HashableEntity {
  @Id
  @Getter
  @Setter
  @XmlElement(name = "hash", required = true)
  @Column(name = "hash", nullable = false, length = 256)
  private String hash;

  @Getter
  @Setter
  @XmlElement(name = "previousHash", required = true)
  @Column(name = "previousHash", length = 256)
  private String previousHash;

  @Getter
  @Setter
  @XmlElement(name = "hashOfMerkleTreeRoot", required = true)
  @Column(name = "hashOfMerkleTreeRoot", unique = true, length = 256)
  private String hashOfMerkleTreeRoot;

  @Getter
  @Setter
  @XmlElement(name = "timeStamp", required = true)
  private long timeStamp;

  public Block(String previousHash, String hashOfMerkleRoot, long timeStamp) {
    this.previousHash = previousHash;
    this.hashOfMerkleTreeRoot = hashOfMerkleRoot;
    this.timeStamp = timeStamp;
    this.hash = HashUtil.hashEntity(this);
  }

  public Block() {
  }

  @Override
  public String getStringToHash() {
    return previousHash + hashOfMerkleTreeRoot + timeStamp;
  }
}
