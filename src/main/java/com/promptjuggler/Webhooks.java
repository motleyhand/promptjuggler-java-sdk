package com.promptjuggler;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Verifies PromptJuggler webhook signatures. */
public final class Webhooks {

    private Webhooks() {}

    /**
     * Verifies a webhook signature with the default 300-second replay tolerance.
     *
     * @param payload         the raw request body, exactly as received (verify before JSON parsing)
     * @param signatureHeader the {@code PromptJuggler-Signature} header value ({@code t=<ts>,v1=<hmac>})
     * @param secret          the webhook signing secret
     * @return whether {@code signatureHeader} is a valid signature for {@code payload}
     */
    public static boolean verifySignature(String payload, String signatureHeader, String secret) {
        return verifySignature(payload, signatureHeader, secret, 300, System.currentTimeMillis() / 1000);
    }

    /**
     * Verifies a webhook signature against an explicit tolerance and clock.
     *
     * <p>Recomputes the HMAC-SHA256 of {@code <timestamp>.<payload>}, compares it in constant time, and
     * rejects deliveries whose timestamp falls outside {@code tolerance} seconds of {@code now}.
     */
    public static boolean verifySignature(
            String payload, String signatureHeader, String secret, long tolerance, long now) {
        Long timestamp = null;
        String signature = null;
        for (String part : signatureHeader.split(",")) {
            int eq = part.indexOf('=');
            if (eq < 0) {
                continue;
            }
            String key = part.substring(0, eq).trim();
            String value = part.substring(eq + 1).trim();
            if (key.equals("t")) {
                try {
                    timestamp = Long.parseLong(value);
                } catch (NumberFormatException e) {
                    return false;
                }
            } else if (key.equals("v1")) {
                signature = value;
            }
        }
        if (timestamp == null || signature == null) {
            return false;
        }
        if (Math.abs(now - timestamp) > tolerance) {
            return false;
        }

        String expected = hmacSha256Hex(secret, timestamp + "." + payload);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
    }

    private static String hmacSha256Hex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(result.length * 2);
            for (byte b : result) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 is unavailable on this JVM", e);
        }
    }
}
