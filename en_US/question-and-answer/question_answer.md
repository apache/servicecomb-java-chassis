# 问题描述：如何自定义某个Java方法对应的REST接口里的HTTP Status Code？

** 解决方法：**

对于正常的返回值，可以通过SwaggerAnnotation实现，例如：

```java
@ApiResponse(code = 300, response = String.class, message = "")
public int test(int x) {
  return 100;
}
```

对于异常的返回值，可以通过抛出自定义的InvocationException实现，例如：、

```java
    public String testException(int code) {
        String strCode = String.valueOf(code);
        switch (code) {
            case 200:
                return strCode;
            case 456:
                throw new InvocationException(code, strCode, strCode + " error");
            case 556:
                throw new InvocationException(code, strCode, Arrays.asList(strCode + " error"));
            case 557:
                throw new InvocationException(code, strCode, Arrays.asList(Arrays.asList(strCode + " error")));
            default:
                break;
        }

        return "not expected";
    }
```

# 问题描述： 如何定制自己微服务的日志配置

** 解决方法：**  
ServiceComb不绑定日志器，只是使用了slf4j，用户可以自由选择log4j/log4j2/logback等等。ServiceComb提供了一个log4j的扩展，在标准log4j的基础上，支持log4j的properties文件的增量配置。

* 默认以规则："classpath\*:config/log4j.properties"加载配置文件
* 实际会搜索出classpath中所有的`config/log4j.properties和config/log4j.*.properties`, 从搜索出的文件中切出`\*`的部分，进行alpha排序，然后按顺序加载，最后合成的文件作为log4j的配置文件。
* 如果要使用ServiceComb的log4j扩展，则需要调用Log4jUtils.init，否则完全按标准的日志器的规则使用。

# 问题描述： 当服务配置了多个transport的时候，在运行时是怎么选择使用哪个transport的？

** 解决方法：**

* ServiceComb的consumer、transport、handler、producer之间是解耦的，各功能之间通过契约定义联合在一起工作的，即：  
  consumer使用透明rpc，还是springmvc开发与使用highway，还是RESTful在网络上传输没有关系与producer是使用透明rpc，还是jaxrs，或者是springmvc开发，也没有关系handler也不感知，业务开发方式以及传输方式

* consumer访问producer，在运行时的transport选择上，总规则为：  
  consumer的transport与producer的endpoint取交集，如果交集后，还有多个transport可选择，则轮流使用

分解开来，存在以下场景：

* 当一个微服务producer同时开放了highway以及RESTful的endpoint
  * consumer进程中只部署了highway transport jar，则只会访问producer的highway endpoint
  * consumer进程中只部署了RESTful transport jar，则只会访问producer的RESTful endpoint
  * consumer进程中，同时部署了highway和RESTful transport jar，则会轮流访问producer的highway、RESTful endpoint

如果，此时consumer想固定使用某个transport访问producer，可以在consumer进程的microservice.yaml中配置，指定transport的名称:

```
servicecomb:
  references:
    <service_name>:
      transport: highway
```

* 当一个微服务producer只开放了highway的endpoint

  * consumer进程只部署了highway transport jar，则正常使用higway访问
  * consumer进程只部署了RESTful transport jar，则无法访问
  * consumer进程同时部署了highway和RESTful transport jar，则正常使用highway访问

* 当一个微服务producer只开放了RESTful的endpoint

  * consumer进程只部署了highway transport jar，则无法访问
  * consumer进程只部署了RESTful transport jar，则正常使用RESTful访问
  * consumer进程同时部署了highway和RESTful transport jar，则正常使用RESTful访问

# 问题描述： swagger body参数类型定义错误，导致服务中心注册的内容没有类型信息

**现象描述:**

定义如下接口，将参数放到body传递

```
/testInherate:
    post:
      operationId: "testInherate"
      parameters:
      - in: "body"
        name: "xxxxx"
        required: false
        type: string
      responses:
        200:
          description: "response of 200"
          schema:
            $ref: "#/definitions/ReponseImpl"
```

采用上面方式定义接口。在服务注册以后，从服务中心查询下来的接口type: string 丢失，变成了：

```
/testInherate:
    post:
      operationId: "testInherate"
      parameters:
      - in: "body"
        name: "xxxxx"
        required: false
      responses:
        200:
          description: "response of 200"
          schema:
            $ref: "#/definitions/ReponseImpl"
```

如果客户端没有放置swagger，还会报告如下异常：

Caused by: java.lang.ClassFormatError: Method "testInherate" in class ? has illegal signature "

**解决方法：**

定义body参数的类型的时候，需要使用schema，不能直接使用type。

```
/testInherate:
    post:
      operationId: "testInherate"
      parameters:
      - in: "body"
        name: "request"
        required: false
        schema:
          type: string
      responses:
        200:
          description: "response of 200"
          schema:
            $ref: "#/definitions/ReponseImpl"
```

# 问题描述：微服务框架服务调用是否使用长连接

** 解决方法：**

http使用的是长连接（有超时时间），highway方式使用的是长连接（一直保持）。

# 问题描述：服务断连服务中心注册信息是否自动删除

** 解决方法：**

服务中心心跳检测到服务实例不可用，只会移除服务实例信息，服务的静态数据不会移除。


# 问题描述：微服务框架如何实现数据多个微服务间透传

** 解决方法：**

透传数据塞入：

```java
CseHttpEntity<xxxx.class> httpEntity = new CseHttpEntity<>(xxx);
//透传内容
httpEntity.addContext("contextKey","contextValue");
ResponseEntity<String> responseEntity = RestTemplateBuilder.create().exchange("cse://springmvc/springmvchello/sayhello",HttpMethod.POST,httpEntity,String.class);
```

透传数据获取：

```java
@Override
@RequestMapping(path="/sayhello",method = RequestMethod.POST)
public String sayHello(@RequestBody Person person,InvocationContext context){
    //透传数据获取
    context.getContext();
    return "Hello person " + person.getName();
}
```

# 问题描述：微服务框架服务如何自定义返回状态码

** 解决方法：**

```java
@Override
@RequestMapping(path = "/sayhello",method = RequestMethod.POST)
public String sayHello(@RequestBody Person person){
    InvocationContext context = ContextUtils.getInvocationContext();
    //自定义状态码
    context.setStatus(Status.CREATED);
    return "Hello person "+person.getName();
}
```

# 问题描述： body Model部分暴露

** 解决方法：**

一个接口对应的body对象中，可能有一些属性是内部的，不想开放出去，生成schema的时候不要带出去，使用：

```java
@ApiModelProperty(hidden = true)
```

# 问题描述：框架获取远端consumer的地址

** 解决方法：**

如果使用http rest方式（使用transport-rest-vertx依赖）可以用下面这种方式获取：

```java
HttpServletRequest request = (HttpServletRequest) invocation.getHandlerContext().get(RestConst.REST_REQUEST);
String host = request.getRemoteHost();
```

实际场景是拿最外层的地址，所以应该是LB传入到edgeservice，edgeService再放到context外下传递。


# 问题描述：对handler描述

** 解决方法：**

consumer默认的handler是simpleLB，没有配置的时候handler链会使用这个，如果配置了handler，里面一定要包含lb的handler，否则调用报错，需要在文档里面进行说明。


# 问题描述：netty版本问题

** 解决方法：**

netty3和netty4是完全不同的三方件，因为坐标跟package都不相同，所以可以共存，但是要注意小版本问题，小版本必须使用的版本。

# 问题描述：服务超时设置

** 解决方法：**

在微服务描述文件（microservice.yaml）中添加如下配置：

```
servicecomb:
  request:
    timeout: 30000
```

# 问题描述：服务治理的处理链顺序是否有要求？

**解决方法：**

处理链的顺序不同，那么系统工作行为也不同。 下面列举一下常见问题。

1、loadbalance和bizkeeper-consumer

这两个顺序可以随机组合。但是行为是不一样的。

当loadbalance在前面的时候，那么loadbalance提供的重试功能会在bizkeeper-consumer抛出异常时发生，比如超时等。但是如果已经做了fallbackpolicy配置，比如returnnull，那么loadbalance则不会重试。

如果loadbalance在后面，那么loadbalance的重试会延长超时时间，即使重试成功，如果bizkeeper-consumer设置的超时时间不够，那么最终的调用结果也是失败。

2、tracing-consumer，sla-consumer，tracing-provider，sla-provider

这些处理链建议放到处理链的最开始位置，保证成功、失败的情况都可以记录日志（由于记录日志需要IP等信息，对于消费者，只能放到loadbalance后面）。

如果不需要记录客户端返回的异常，则可以放到末尾，只关注网络层返回的错误。但是如果bizkeeper-consumer等超时提前返回的话，则可能不会记录日志。

3、建议的顺序

Consumer: loadbalance, tracing-consumer, sla-consumer, bizkeeper-consumer

Provider: tracing-provider, sla-provider, bizkeeper-provider

这种顺序能够满足大多数场景，并且不容易出现不可理解的错误。
