package com.rokin.celltracker;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomUtil {
  private static final Logger log = LoggerFactory.getLogger(RandomUtil.class);

  private static String[] secureRndNames = new String[] {
      System.getProperty("boticelli.securerandom"), "SHA1PRNG", "IBMSecureRandom" };

  private RandomUtil() {

  }

  /**
   * Creates a new secure random number generator. The following secure random algorithm names are
   * tried:
   * <ul>
   * <li>The value of system property "boticelli.securerandom", if set.</li>
   * <li>"SHA1PRNG"</li>
   * <li>"IBMSecureRandom" (available if running in the IBM JRE)</li>
   * </ul>
   */
  private static SecureRandom createSecureRandom() {
    SecureRandom secureRnd = null;
    for (int i = 0; i < secureRndNames.length; i++) {
      try {
        if (secureRndNames[i] != null) {
          secureRnd = SecureRandom.getInstance(secureRndNames[i]);
          break;
        }
      } catch (NoSuchAlgorithmException nsae) {
        log.debug("no secure random algorithm named \"" + secureRndNames[i] + "\"", nsae);
      }
    }
    if (secureRnd == null) {
      throw new IllegalStateException(
          "no secure random algorithm found. (tried " + Arrays.asList(secureRndNames) + ")");
    }
    secureRnd.setSeed(System.currentTimeMillis());
    return secureRnd;
  }

  /**
   * Generates a secure random word with the given length consisting of uppercase and lowercase
   * letters and numbers.
   * 
   * @param len
   *          Amount of random characters to generate
   * @return random Word containing letters and numbers.
   */
  public static String createWord(int len) {
    return createWord(len, null);
  }

  /**
   * Generates a secure random word with the given length.
   * 
   * @param len
   *          Amount of random characters to generate
   * @param alphabet
   *          Alphabet to generate from.
   * @return random Word containing letters and numbers.
   */
  public static String createWord(int len, char[] alphabet) {
    SecureRandom random = createSecureRandom();
    if (alphabet == null) {
      alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
    }
    StringBuilder out = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      out.append(alphabet[random.nextInt(alphabet.length)]);
    }
    return out.toString();
  }
}
