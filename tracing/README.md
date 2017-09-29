# Customized Tracing
Customized tracing with [Zipkin](http://zipkin.io/) is supported to allow users to add tracing spans at points of
interest.

## Set Up Customized Tracing
1. Include the following dependency
```
    <dependency>
      <groupId>io.servicecomb</groupId>
      <artifactId>tracing-zipkin</artifactId>
    </dependency>
```
2. Enable tracing with annotation `@EnableZipkinTracing` on your application entry or configuration
```
@SpringBootApplication
@EnableZipkinTracing
public class ZipkinSpanTestApplication {
  public static void main(String[] args) {
    SpringApplication.run(ZipkinSpanTestApplication.class);
  }
}
```
3. Add new span to the point of interest with annotation `@Span`
```
@Component
public class SlowRepoImpl implements SlowRepo {
  private static final Logger logger = LoggerFactory.getLogger(SlowRepoImpl.class);

  private final Random random = new Random();

  @Span
  @Override
  public String crawl() throws InterruptedException {
    logger.info("in /crawl");
    Thread.sleep(random.nextInt(200));
    return "crawled";
  }
}
```

That's it!
