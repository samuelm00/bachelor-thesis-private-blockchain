package util.hash;

import model.HashableEntity;
import util.encoder.Encoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class that provides simple util functions for hashing.
 */
public class HashUtil {

  /**
   * Handles the hashing of a string.
   *
   * @param input String that should be hashed
   * @return the hash of the input string in hex format
   */
  public static String hash(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes());
      return Encoder.encodeBytes(hash);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      System.out.println("Error: " + e.getMessage());
      return null;
    }
  }

  /**
   * Utility method to get the hash of a block.
   *
   * @param hashableEntity that needs to be hashed
   * @return the hash of the block in hex format
   */
  public static String hashEntity(HashableEntity hashableEntity) {
    return HashUtil.hash(hashableEntity.getStringToHash());
  }
}
