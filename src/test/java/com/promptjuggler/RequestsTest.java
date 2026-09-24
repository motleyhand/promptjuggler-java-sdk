package com.promptjuggler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptjuggler.client.ApiClient;
import com.promptjuggler.client.model.HttpCall;
import com.promptjuggler.client.model.Model;
import com.promptjuggler.client.model.PromptRevision;
import com.promptjuggler.client.model.TextFormat;
import com.promptjuggler.client.model.VersionRef;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RequestsTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String UUID1 = "550e8400-e29b-41d4-a716-446655440000";
  private static final String UUID2 = "550e8400-e29b-41d4-a716-446655440001";

  private static final String REVISION = "{\"id\":\"" + UUID1 + "\",\"promptId\":\"" + UUID2
      + "\",\"memory\":\"stateless\",\"provider\":\"openai\",\"model\":\"gpt-4o\","
      + "\"modelParams\":{},\"responseFormat\":{\"type\":\"text\"},\"messages\":[],\"tools\":[]}";
  private static final String RUN_RESPONSE =
      "{\"id\":\"" + UUID1 + "\",\"thread\":\"" + UUID2 + "\"}";

  private static JsonNode body(MockServer.Captured call) throws Exception {
    return MAPPER.readTree(call.body());
  }

  @Test
  void getPromptSendsGetWithBearer() throws Exception {
    try (MockServer server = new MockServer().respond(200, REVISION)) {
      server.client().getPrompt("greeting", "production");
      MockServer.Captured call = server.firstCall();
      assertEquals("GET", call.method());
      assertEquals("/api/v1/prompts/greeting/production", call.path());
      assertEquals("Bearer test-key", call.headers().getFirst("Authorization"));
    }
  }

  @Test
  void getPromptAcceptsIntVersion() throws Exception {
    try (MockServer server = new MockServer().respond(200, REVISION)) {
      server.client().getPrompt("greeting", 42);
      assertEquals("/api/v1/prompts/greeting/42", server.firstCall().path());
    }
  }

  // The API adds fields and enum values without a major version, so a published client must
  // decode a response carrying ones it doesn't know.
  @Test
  void getPromptDecodesFieldsAndEnumValuesNewerThanTheSdk() throws Exception {
    String revision = "{\"id\":\"" + UUID1 + "\",\"promptId\":\"" + UUID2
        + "\",\"memory\":\"stateless\",\"provider\":\"openai\",\"model\":\"gpt-9\","
        + "\"modelParams\":{\"reasoningEffort\":\"ultra\"},"
        + "\"responseFormat\":{\"type\":\"text\",\"addedLater\":1},\"messages\":[],"
        + "\"tools\":[{\"type\":\"http\",\"name\":\"lookup\",\"url\":\"https://example.com\","
        + "\"method\":\"QUERY\",\"paramsSchema\":\"{}\",\"failFast\":false}],\"addedLater\":true}";
    try (MockServer server = new MockServer().respond(200, revision)) {
      PromptRevision prompt = server.client().getPrompt("greeting", "production");
      assertEquals(Model.UNKNOWN_DEFAULT_OPEN_API, prompt.getModel());
      assertInstanceOf(TextFormat.class, prompt.getResponseFormat().getActualInstance());
      HttpCall tool =
          assertInstanceOf(HttpCall.class, prompt.getTools().get(0).getActualInstance());
      assertEquals(HttpCall.MethodEnum.UNKNOWN_DEFAULT_OPEN_API, tool.getMethod());
    }
  }

  @Test
  void runRejectsUnknownPriorityBeforeSending() {
    try (MockServer server = new MockServer().respond(200, RUN_RESPONSE)) {
      RunOptions options = RunOptions.builder().priority("urgent").build();
      assertThrows(IllegalArgumentException.class,
          () -> server.client().runPrompt("greeting", "production", Map.of(), options));
      assertThrows(IllegalArgumentException.class,
          () -> server.client().runWorkflow("onboarding", "production", Map.of(), options));
      assertTrue(server.calls.isEmpty());
    }
  }

  @Test
  void runPromptPostsInputsOnly() throws Exception {
    try (MockServer server = new MockServer().respond(200, RUN_RESPONSE)) {
      server.client().runPrompt("greeting", "production", Map.of("name", "Ada"));
      MockServer.Captured call = server.firstCall();
      assertEquals("POST", call.method());
      assertEquals("/api/v1/prompts/greeting/production/runs", call.path());
      assertEquals("Ada", body(call).get("inputs").get("name").asText());
    }
  }

  @Test
  void runPromptSerializesOptionsAndArrayMetadata() throws Exception {
    try (MockServer server = new MockServer().respond(200, RUN_RESPONSE)) {
      RunOptions options = RunOptions.builder().priority("onsite").environment("staging")
          .envVars(Map.of("MY_API_KEY", "sk-x"))
          .metadata(Map.of("tags", List.of("a", "b"), "user_id", "42")).channel("support").build();
      server.client().runPrompt("greeting", 1, Map.of("topic", "AI safety"), options);

      JsonNode b = body(server.firstCall());
      assertEquals("onsite", b.get("priority").asText());
      assertEquals("staging", b.get("environment").asText());
      assertEquals("sk-x", b.get("envVars").get("MY_API_KEY").asText());
      assertEquals("support", b.get("channel").asText());
      assertEquals("42", b.get("metadata").get("user_id").asText());
      assertEquals("a", b.get("metadata").get("tags").get(0).asText());
      assertEquals(2, b.get("metadata").get("tags").size());
    }
  }

  @Test
  void builderTreatsNullAsUnset() {
    // A nullable value can be forwarded to any setter without a guard; null means "unset".
    // thread(String) also exercises the null guard around UUID.fromString.
    RunOptions options = RunOptions.builder().priority(null).thread((String) null).environment(null)
        .envVars(null).metadata(null).channel(null).build();

    assertNull(options.priority);
    assertNull(options.thread);
    assertNull(options.environment);
    assertNull(options.envVars);
    assertNull(options.metadata);
    assertNull(options.channel);
  }

  @Test
  void getPromptRunGetsByUuid() throws Exception {
    String run = "{\"id\":\"" + UUID1
        + "\",\"status\":\"completed\",\"createdAt\":\"2026-01-01T00:00:00Z\"}";
    try (MockServer server = new MockServer().respond(200, run)) {
      server.client().getPromptRun(UUID1);
      assertEquals("/api/v1/promptruns/" + UUID1, server.firstCall().path());
    }
  }

  @Test
  void runWorkflowPostsToWorkflowRuns() throws Exception {
    try (MockServer server = new MockServer().respond(200, RUN_RESPONSE)) {
      server.client().runWorkflow("onboarding", "production", Map.of("email", "a@b.com"));
      MockServer.Captured call = server.firstCall();
      assertEquals("POST", call.method());
      assertEquals("/api/v1/workflows/onboarding/production/runs", call.path());
    }
  }

  @Test
  void getKnowledgeBaseGetsBySlug() throws Exception {
    String kb = "{\"id\":\"" + UUID1 + "\",\"slug\":\"product-docs\",\"status\":\"ready\","
        + "\"documentCount\":0,\"chunkCount\":0,\"documents\":[]}";
    try (MockServer server = new MockServer().respond(200, kb)) {
      server.client().getKnowledgeBase("product-docs");
      assertEquals("/api/v1/knowledge-bases/product-docs", server.firstCall().path());
    }
  }

  @Test
  void createStreamTokenPostsToThread() throws Exception {
    String url = "https://stream.promptjuggler.com/stream/" + UUID1;
    String token =
        "{\"token\":\"jwt-value\",\"expiresAt\":\"2026-01-01T00:00:00Z\",\"url\":\"" + url + "\"}";
    try (MockServer server = new MockServer().respond(200, token)) {
      var response = server.client().createStreamToken(UUID1);
      MockServer.Captured call = server.firstCall();
      assertEquals("POST", call.method());
      assertEquals("/api/v1/threads/" + UUID1 + "/stream-token", call.path());
      assertEquals("Bearer test-key", call.headers().getFirst("Authorization"));
      assertEquals("jwt-value", response.getToken());
      assertEquals(url, response.getUrl());
    }
  }

  @Test
  void versionRefDeserializesNumericIdOrTag() throws Exception {
    // A prompt's tools reference a revision by number or tag (VersionRef.idOrTag). With a
    // numeric ref, the oneOf wrapper double-matched (Integer + coerced String) and threw,
    // breaking getPrompt; idOrTag is normalized to string in the Java pipeline. Use the SDK's
    // own mapper so this exercises the same coercion config getPrompt uses.
    ObjectMapper mapper = new ApiClient().getObjectMapper();
    VersionRef ref =
        mapper.readValue("{\"definitionId\":\"" + UUID2 + "\",\"idOrTag\":1}", VersionRef.class);
    assertEquals("1", ref.getIdOrTag());
  }

  @Test
  void deleteKnowledgeDocumentDeletesByUuid() throws Exception {
    try (MockServer server = new MockServer().respond(204, "")) {
      server.client().deleteKnowledgeDocument(UUID1);
      MockServer.Captured call = server.firstCall();
      assertEquals("DELETE", call.method());
      assertEquals("/api/v1/knowledge-documents/" + UUID1, call.path());
      assertTrue(call.headers().getFirst("Authorization").startsWith("Bearer "));
    }
  }
}
