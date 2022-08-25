package util.encoder;

import java.util.Base64;

public class Encoder {
  /**
   * Utility method to convert a byte array to an encoded string.
   *
   * @param bytes a hashed value that need to be converted to a hex string
   * @return an encoded string representation of the input byte array
   */
  public static String encodeBytes(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }

  /**
   * Utility method to convert an input string to a byte array.
   *
   * @param input string that need to be converted to a byte array
   * @return a byte array representation of the input string
   */
  public static byte[] decodeString(String input) {
    return Base64.getDecoder().decode(input);
  }
}
