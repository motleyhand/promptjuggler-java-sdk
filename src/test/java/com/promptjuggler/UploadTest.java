package com.promptjuggler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.promptjuggler.client.model.KnowledgeDocumentResponse;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class UploadTest {

  private static final String DOC = "{\"id\":\"550e8400-e29b-41d4-a716-446655440000\","
      + "\"status\":\"pending\",\"fileName\":\"manual.pdf\",\"bytes\":3,\"chunkCount\":0}";

  @Test
  void uploadSendsBracketedMultipartWithFilenames() throws Exception {
    Path dir = Files.createTempDirectory("pj-upload");
    File a = dir.resolve("manual.pdf").toFile();
    File b = dir.resolve("notes.txt").toFile();
    Files.writeString(a.toPath(), "abc");
    Files.writeString(b.toPath(), "hello");

    try (MockServer server = new MockServer().respond(200, "[" + DOC + "," + DOC + "]")) {
      List<KnowledgeDocumentResponse> docs =
          server.client().uploadDocuments("product-docs", List.of(a, b));

      assertEquals(2, docs.size());
      MockServer.Captured call = server.firstCall();
      assertEquals("POST", call.method());
      assertEquals("/api/v1/knowledge-bases/product-docs/documents", call.path());
      assertTrue(call.headers().getFirst("Content-Type").startsWith("multipart/form-data"));
      assertTrue(call.body().contains("name=\"files[0]\""), "expected files[0] field");
      assertTrue(call.body().contains("name=\"files[1]\""), "expected files[1] field");
      assertTrue(call.body().contains("filename=\"manual.pdf\""), "expected filename preserved");
      assertTrue(call.body().contains("filename=\"notes.txt\""), "expected filename preserved");
    }
  }
}
