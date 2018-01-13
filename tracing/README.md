# Customized Tracing
Customized tracing with [Zipkin](http://zipkin.io/) is supported to allow users to add tracing spans at points of
interest.

## Set Up Customized Tracing
1. Include the following dependency
```
    <dependency>
      <groupId>org.apache.servicecomb</groupId>
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

## Reported Span Data
Customized tracing span includes two pieces of data:
* span name - annotated method name
* call.path - annotated method signature

e.g. the example `SlowRepoImpl` in the previous section reports the following span

| key | value |
| --- | --- |
| span name | crawl |
| call.path	| public abstract java.lang.String org.apache.servicecomb.tests.tracing.SlowRepo.crawl() throws java.lang.InterruptedException |

## Constraints
* Customized tracing with annotation only supports method calls in the request thread.
* Classes with `@Span` must be a spring managed bean. If you want to do load-time weaving for non spring beans,
you have to do it manually according to this [answer](https://stackoverflow.com/questions/41383941/load-time-weaving-for-non-spring-beans-in-a-spring-application).
