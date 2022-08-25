package envelope;

import lombok.Getter;
import lombok.Setter;
import model.Block;
import model.Tweet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "blockWithValidation")
@XmlAccessorType(XmlAccessType.NONE)
public class BlockWithValidation extends Block {
  @Getter
  @Setter
  @XmlElement(name = "isValid")
  private boolean isValid;

  @Getter
  @Setter
  @XmlElement(name = "publicKeyOfValidator")
  private String publicKeyOfValidator;

  @Getter
  @Setter
  @XmlElement(name = "tweets")
  private List<Tweet> tweets;

  public BlockWithValidation(String previousHash, String data, long timeStamp, boolean isValid, String publicKeyOfValidator, List<Tweet> tweets) {
    super(previousHash, data, timeStamp);
    assert (publicKeyOfValidator != null);
    this.isValid = isValid;
    this.publicKeyOfValidator = publicKeyOfValidator;
    this.tweets = tweets;
  }

  public BlockWithValidation(Block block, boolean isValid, String publicKeyOfValidator, List<Tweet> tweets) {
    super(block.getPreviousHash(), block.getHashOfMerkleTreeRoot(), block.getTimeStamp());
    this.isValid = isValid;
    this.publicKeyOfValidator = publicKeyOfValidator;
    this.tweets = tweets;
  }

  public BlockWithValidation(BlockWithValidation blockWithValidation) {
    super(blockWithValidation.getPreviousHash(), blockWithValidation.getHashOfMerkleTreeRoot(), blockWithValidation.getTimeStamp());
    this.isValid = blockWithValidation.isValid;
    this.publicKeyOfValidator = blockWithValidation.publicKeyOfValidator;
    this.tweets = blockWithValidation.tweets;
  }

  public BlockWithValidation() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    BlockWithValidation that = (BlockWithValidation) o;
    return isValid == that.isValid && Objects.equals(publicKeyOfValidator, that.publicKeyOfValidator);
  }
}
