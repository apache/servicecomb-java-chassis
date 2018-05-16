## 概念阐述

分布式调用链追踪提供了服务间调用的时序信息，但服务内部的链路调用信息对开发者同样重要，如果能将两者合二为一，就能提供更完整的调用链，更容易定位错误和潜在性能问题。

## 前提条件

* 使用自定义打点功能需要首先配置并启用Java Chassis微服务调用链。

## 注意事项

* 使用`@Span`注释的自定义打点功能只支持和Java Chassis调用请求同一线程的方法调用。
* 添加`@Span`注释的方法必须是Spring管理的Bean，否则需要按这里[提到的方法](https://stackoverflow.com/questions/41383941/load-time-weaving-for-non-spring-beans-in-a-spring-application)配置。

## 自定义调用链打点

该功能集成了Zipkin，提供`@Span`注释为需要追踪的方法自定义打点。Java Chassis将自动追踪所有添加`@Span`注释的方法，把每个方法的本地调用信息与服务间调用信息连接起来。

## 使用步骤:

### 添加依赖

基于 ServiceComb Java Chassis 的微服务只需要添加如下依赖到 pom.xml：

```xml
    <dependency>
      <groupId>org.apache.servicecomb</groupId>
      <artifactId>tracing-zipkin</artifactId>
    </dependency>
```

### 启用自定义打点功能 {#配置追踪处理和数据收集}

在应用入口或Spring配置类上添加`@EnableZipkinTracing`注释：

```java
@SpringBootApplication
@EnableZipkinTracing
public class ZipkinSpanTestApplication {
  public static void main(String[] args) {
    SpringApplication.run(ZipkinSpanTestApplication.class);
  }
}
```

### 定制打点

在需要定制打点的方法上添加`@Span`注释：

```java
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

就这样，通过使用`@Span`注释，我们启动了基于 Zipkin 的自定义打点功能。

## 定制上报的数据

通过自定义打点上报的调用链包含两条数据：

* **span name** 默认为当前注释的方法全名。
* **call.path** 默认为当前注释的方法签名。

例如，上述例子`SlowRepoImp`里上报的数据如下：

| key | value |
| :--- | :--- |
| span name | crawl |
| call.path | public abstract java.lang.String org.apache.servicecomb.tests.tracing.SlowRepo.crawl\(\) throws java.lang.InterruptedException |

如果需要定制上报的数据内容，可以传入自定义的参数：

```java
  public static class CustomSpanTask {
    @Span(spanName = "transaction1", callPath = "startA")
    public String invoke() {
      return "invoke the method";
    }
  }
```



