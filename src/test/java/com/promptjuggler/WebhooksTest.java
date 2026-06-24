package com.promptjuggler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class WebhooksTest {

  private static final String SECRET = "whsec_test";
  private static final String PAYLOAD = "{\"event\":\"promptrun.finished\",\"id\":\"run1\"}";
  private static final long TS = 1_700_000_000L;

  private static String header(String payload, String secret, long timestamp) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    byte[] result = mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8));
    StringBuilder hex = new StringBuilder();
    for (byte b : result) {
      hex.append(String.format("%02x", b));
    }
    return "t=" + timestamp + ",v1=" + hex;
  }

  @Test
  void acceptsCorrectlySignedPayload() throws Exception {
    assertTrue(Webhooks.verifySignature(PAYLOAD, header(PAYLOAD, SECRET, TS), SECRET, 300, TS));
  }

  @Test
  void rejectsTamperedPayload() throws Exception {
    assertFalse(
        Webhooks.verifySignature(PAYLOAD + " ", header(PAYLOAD, SECRET, TS), SECRET, 300, TS));
  }

  @Test
  void rejectsWrongSecret() throws Exception {
    assertFalse(
        Webhooks.verifySignature(PAYLOAD, header(PAYLOAD, SECRET, TS), "whsec_wrong", 300, TS));
  }

  @Test
  void rejectsExpiredTimestamp() throws Exception {
    assertFalse(
        Webhooks.verifySignature(PAYLOAD, header(PAYLOAD, SECRET, TS), SECRET, 300, TS + 301));
  }

  @Test
  void acceptsTimestampAtEdgeOfTolerance() throws Exception {
    assertTrue(
        Webhooks.verifySignature(PAYLOAD, header(PAYLOAD, SECRET, TS), SECRET, 300, TS + 300));
  }

  @Test
  void rejectsMalformedHeader() {
    assertFalse(Webhooks.verifySignature(PAYLOAD, "not-a-signature", SECRET, 300, TS));
  }

  @Test
  void rejectsNullHeader() {
    // Servlet-style getHeader returns null for an absent header — reject, don't throw.
    assertFalse(Webhooks.verifySignature(PAYLOAD, null, SECRET));
  }
}
