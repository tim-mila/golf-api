package com.alimmit.golf.id;

import java.math.BigInteger;
import java.security.SecureRandom;

public abstract class IdGenerator {

  private final SecureRandom secureRandom;

  protected IdGenerator() {
    secureRandom = new SecureRandom();
  }

  public abstract String generate();

  protected String generate(int length) {

    // Get current timestamp in milliseconds
    long timestamp = System.currentTimeMillis();

    // Combine timestamp and secure random bytes
    byte[] randomBytes = new byte[length / 2 + 1]; // Roughly half the desired length for byte array
    secureRandom.nextBytes(randomBytes);

    // XOR the timestamp with some of the random bytes for mixing
    for (int i = 0; i < Math.min(8, randomBytes.length); i++) {
      randomBytes[i] = (byte) (((randomBytes[i]) & 0xffL) ^ (timestamp >> (i * 8)));
    }

    // Convert the byte array to a hexadecimal string
    String combinedString = new BigInteger(1, randomBytes).toString(16);

    // Ensure the string has the desired length by padding or truncating
    if (combinedString.length() < length) {
      // Pad with leading zeros if too short
      return String.format(String.format("%%0%dd", length), new BigInteger(combinedString, 16));
    } else if (combinedString.length() > length) {
      // Truncate if too long
      return combinedString.substring(0, length);
    } else {
      return combinedString;
    }
  }
}
