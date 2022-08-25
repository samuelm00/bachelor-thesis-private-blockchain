package util.key;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAKeyImporter {
  /**
   * Imports a public key from a string.
   *
   * @param publicKey The public key in byte[] format.
   * @param algorithm The algorithm to use
   * @return The public key.
   * @throws NoSuchAlgorithmException If the algorithm is not supported.
   * @throws InvalidKeySpecException  If the key is not valid.
   */
  public static RSAPublicKey importPublicKey(byte[] publicKey, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
    KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
    return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
  }

  /**
   * Imports a private key from a string.
   *
   * @param privateKey The private key in byte[] format.
   * @param algorithm  The algorithm to use
   * @return The private key.
   * @throws NoSuchAlgorithmException If the algorithm is not supported.
   * @throws InvalidKeySpecException  If the key is not valid.
   */
  public static RSAPrivateKey importPrivateKey(byte[] privateKey, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
    KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
    return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
  }
}
