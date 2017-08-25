package io.servicecomb.demo.discovery.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.net.URI;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import io.servicecomb.springboot.starter.provider.EnableServiceComb;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = DiscoveryClientIT.DiscoveryTestApplication.class)
public class DiscoveryClientIT {

  @Autowired
  private DiscoveryClient discoveryClient;

  //	@Autowired
  //	private LoadBalancerClient client;

  private final RestTemplate restTemplate = new RestTemplate();

  @Test
  public void getsRemoteServiceFromDiscoveryClient() throws Exception {
    URI remoteUri = discoveryClient.getInstances("discoveryServer").get(0).getUri();

    assertThat(remoteUri).isNotNull();

    String response = restTemplate.getForObject(
        remoteUri.toString() + "/greeting/sayhello/{name}",
        String.class,
        "Mike");

    assertThat(response).isEqualTo("hello Mike");
  }

  @SpringBootApplication
  @EnableServiceComb
  @EnableDiscoveryClient
  static class DiscoveryTestApplication {

    public static void main(String[] args) throws Exception {
      SpringApplication.run(DiscoveryTestApplication.class, args);
    }
  }
}
