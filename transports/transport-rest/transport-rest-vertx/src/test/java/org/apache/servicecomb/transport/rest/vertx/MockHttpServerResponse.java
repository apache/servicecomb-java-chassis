/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.transport.rest.vertx;

import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.HostAndPort;

class MockHttpServerResponse implements HttpServerResponse {
  boolean responseEnded;

  Map<String, String> responseHeader = new HashMap<>(1);

  int responseStatusCode;

  String responseStatusMessage;

  String responseChunk;

  @Override
  public HttpServerResponse putHeader(String name, String value) {
    responseHeader.put(name, value);
    return this;
  }

  @Override
  public HttpServerResponse setStatusCode(int statusCode) {
    responseStatusCode = statusCode;
    return this;
  }

  @Override
  public HttpServerResponse setStatusMessage(String statusMessage) {
    responseStatusMessage = statusMessage;
    return this;
  }

  @Override
  public Future<Void> end() {
    responseEnded = true;
    return Future.succeededFuture();
  }

  @Override
  public Future<Void> end(String chunk) {
    responseEnded = true;
    responseChunk = chunk;
    return Future.succeededFuture();
  }

  @Override
  public HttpServerResponse exceptionHandler(Handler<Throwable> handler) {
    return null;
  }

  @Override
  public Future<Void> write(Buffer data) {
    return Future.succeededFuture();
  }

  @Override
  public HttpServerResponse setWriteQueueMaxSize(int maxSize) {
    return null;
  }

  @Override
  public boolean writeQueueFull() {
    return false;
  }

  @Override
  public HttpServerResponse drainHandler(Handler<Void> handler) {
    return null;
  }

  @Override
  public int getStatusCode() {
    return 0;
  }

  @Override
  public String getStatusMessage() {
    return null;
  }

  @Override
  public HttpServerResponse setChunked(boolean chunked) {
    return null;
  }

  @Override
  public boolean isChunked() {
    return false;
  }

  @Override
  public MultiMap headers() {
    return null;
  }

  @Override
  public HttpServerResponse putHeader(CharSequence name, CharSequence value) {
    return null;
  }

  @Override
  public HttpServerResponse putHeader(String name, Iterable<String> values) {
    return null;
  }

  @Override
  public HttpServerResponse putHeader(CharSequence name, Iterable<CharSequence> values) {
    return null;
  }

  @Override
  public MultiMap trailers() {
    return null;
  }

  @Override
  public HttpServerResponse putTrailer(String name, String value) {
    return null;
  }

  @Override
  public HttpServerResponse putTrailer(CharSequence name, CharSequence value) {
    return null;
  }

  @Override
  public HttpServerResponse putTrailer(String name, Iterable<String> values) {
    return null;
  }

  @Override
  public HttpServerResponse putTrailer(CharSequence name, Iterable<CharSequence> value) {
    return null;
  }

  @Override
  public HttpServerResponse closeHandler(Handler<Void> handler) {
    return null;
  }

  @Override
  public HttpServerResponse endHandler(Handler<Void> handler) {
    return null;
  }

  @Override
  public Future<Void> writeHead() {
    return null;
  }

  @Override
  public Future<Void> write(String chunk, String enc) {
    return Future.succeededFuture();
  }


  @Override
  public Future<Void> write(String chunk) {
    return Future.succeededFuture();
  }

  @Override
  public Future<Void> writeContinue() {
    return null;
  }

  @Override
  public Future<Void> writeEarlyHints(MultiMap headers) {
    return Future.succeededFuture();
  }

  @Override
  public Future<Void> end(String chunk, String enc) {
    return Future.succeededFuture();
  }


  @Override
  public Future<Void> end(Buffer chunk) {
    return Future.succeededFuture();
  }


  @Override
  public Future<Void> sendFile(String filename, long offset, long length) {
    return Future.succeededFuture();
  }

  @Override
  public Future<Void> sendFile(FileChannel channel, long offset, long length) {
    return null;
  }

  @Override
  public boolean ended() {
    return false;
  }

  @Override
  public boolean closed() {
    return false;
  }

  @Override
  public boolean headWritten() {
    return false;
  }

  @Override
  public HttpServerResponse headersEndHandler(Handler<Void> handler) {
    return null;
  }

  @Override
  public HttpServerResponse bodyEndHandler(Handler<Void> handler) {
    return null;
  }

  @Override
  public long bytesWritten() {
    return 0;
  }

  @Override
  public int streamId() {
    return 0;
  }

  @Override
  public Future<HttpServerResponse> push(HttpMethod httpMethod, HostAndPort hostAndPort, String s, MultiMap multiMap) {
    return null;
  }

  @Override
  public Future<Void> reset(long code) {
    return null;
  }

  @Override
  public Future<Void> writeCustomFrame(int type, int flags, Buffer payload) {
    return null;
  }

  @Override
  public HttpServerResponse addCookie(Cookie cookie) {
    return null;
  }

  @Override
  public @Nullable Cookie removeCookie(String name, boolean invalidate) {
    return null;
  }

  @Override
  public Set<Cookie> removeCookies(String name, boolean invalidate) {
    return null;
  }

  @Override
  public @Nullable Cookie removeCookie(String name, String domain, String path, boolean invalidate) {
    return null;
  }
}
