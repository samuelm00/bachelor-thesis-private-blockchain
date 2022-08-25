package util.encoder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EncoderTests {

  @Test
  public void testSimpleConversion() {
    String string = "Hello World";
    String hex = Encoder.encodeBytes(string.getBytes());
    String hexEncoded = new String(Encoder.decodeString(hex));
    Assertions.assertEquals(hexEncoded, string);
  }

  @Test
  public void testComplexConversion() {
    String string = "1234567890-=!@#%^&*()_+qwertyuiop[]asdfghjkl;'zxcvbnm,./QWERTYUIOP{}ASDFGHJKL:\"ZXCVBNM<>?";
    String hex = Encoder.encodeBytes(string.getBytes());
    String hexEncoded = new String(Encoder.decodeString(hex));
    Assertions.assertEquals(hexEncoded, string);
  }
}

