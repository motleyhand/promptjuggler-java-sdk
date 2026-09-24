package com.promptjuggler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorsTest {

  private static final String UUID1 = "550e8400-e29b-41d4-a716-446655440000";

  @Test
  void translatesNon2xxIntoApiError() {
    try (
        MockServer server = new MockServer().respond(404, "{\"error\":\"Prompt run not found\"}")) {
      ApiError error = assertThrows(ApiError.class, () -> server.client().getPromptRun(UUID1));
      assertEquals(404, error.statusCode());
      assertEquals("Prompt run not found", error.getMessage());
    }
  }

  @Test
  void apiErrorIsAPromptJugglerException() {
    try (MockServer server = new MockServer().respond(500, "{\"error\":\"boom\"}")) {
      assertThrows(PromptJugglerException.class, () -> server.client().getPromptRun(UUID1));
    }
  }

  @Test
  void wrapsUndecodableSuccessBodyInDecodeError() {
    String revision = "{\"id\":\"" + UUID1 + "\",\"promptId\":\"" + UUID1
        + "\",\"memory\":\"stateless\",\"provider\":\"openai\",\"model\":\"gpt-4o\","
        + "\"modelParams\":{},\"responseFormat\":{\"type\":\"text\"},\"messages\":[],"
        + "\"tools\":\"not-a-list\"}";
    try (MockServer server = new MockServer().respond(200, revision)) {
      assertThrows(DecodeError.class, () -> server.client().getPrompt("greeting", "production"));
    }
  }

  @Test
  void wrapsUndecodableUploadResponseInDecodeError() throws Exception {
    File file = Files.writeString(Files.createTempFile("pj-upload", ".txt"), "abc").toFile();
    try (MockServer server = new MockServer().respond(200, "{\"not\":\"a list\"}")) {
      assertThrows(DecodeError.class,
          () -> server.client().uploadDocuments("product-docs", List.of(file)));
    }
  }

  @Test
  void wrapsConnectionFailureInNetworkError() {
    // Nothing is listening on port 1 — the request never gets a response.
    PromptJuggler client = new PromptJuggler("test-key", "http://127.0.0.1:1");
    assertThrows(NetworkError.class, () -> client.getPromptRun(UUID1));
  }
}
