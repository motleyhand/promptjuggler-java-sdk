package com.promptjuggler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
