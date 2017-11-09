package io.servicecomb.common.rest.codec.param;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestRestClientRequestImpl {
  @Mocked
  HttpClientRequest request;

  @Test
  public void testForm() throws Exception {
    RestClientRequestImpl restClientRequest = new RestClientRequestImpl(request);
    restClientRequest.addForm("abc", "Hello");
    restClientRequest.addForm("def", "world");
    restClientRequest.addForm("ghi", null);
    Buffer buffer = restClientRequest.getBodyBuffer();
    Assert.assertEquals(buffer.toString(), "abc=Hello&def=world&");
  }

  @Test
  public void testCookie() throws Exception {
    HttpClientRequest request = new MockUp<HttpClientRequest>() {

      MultiMap map = new CaseInsensitiveHeaders();

      @Mock
      public HttpClientRequest putHeader(CharSequence key, CharSequence val) {
        map.add(key, val);
        return null;
      }

      @Mock
      public MultiMap headers() {
        return map;
      }
    }.getMockInstance();
    RestClientRequestImpl restClientRequest = new RestClientRequestImpl(request);
    restClientRequest.addCookie("sessionid", "abcdefghijklmnopqrstuvwxyz");
    restClientRequest.addCookie("region", "china-north");
    restClientRequest.write(Buffer.buffer("I love servicecomb"));
    restClientRequest.end();
    Buffer buffer = restClientRequest.getBodyBuffer();
    Assert.assertEquals("I love servicecomb", buffer.toString());
    Assert.assertEquals("sessionid=abcdefghijklmnopqrstuvwxyz; region=china-north; ", 
        restClientRequest.request.headers().get(HttpHeaders.COOKIE));
  }
}
