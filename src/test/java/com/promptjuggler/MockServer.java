package com.promptjuggler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** A real local HTTP server that records requests and replies with a canned response. */
final class MockServer implements AutoCloseable {

  record Captured(String method, String path, Headers headers, String body) {}

  private final HttpServer server;
  final List<Captured> calls = new ArrayList<>();
  private volatile int status = 200;
  private volatile String responseBody = "{}";

  MockServer() {
    try {
      server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    server.createContext("/", exchange -> {
      byte[] requestBody = exchange.getRequestBody().readAllBytes();
      calls.add(new Captured(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
          exchange.getRequestHeaders(), new String(requestBody, StandardCharsets.UTF_8)));
      byte[] resp = responseBody.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      if (resp.length == 0) {
        exchange.sendResponseHeaders(status, -1);
      } else {
        exchange.sendResponseHeaders(status, resp.length);
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(resp);
        }
      }
      exchange.close();
    });
    server.start();
  }

  MockServer respond(int status, String body) {
    this.status = status;
    this.responseBody = body;
    return this;
  }

  String baseUrl() {
    return "http://127.0.0.1:" + server.getAddress().getPort();
  }

  PromptJuggler client() {
    return new PromptJuggler("test-key", baseUrl());
  }

  Captured firstCall() {
    return calls.get(0);
  }

  @Override
  public void close() {
    server.stop(0);
  }
}
