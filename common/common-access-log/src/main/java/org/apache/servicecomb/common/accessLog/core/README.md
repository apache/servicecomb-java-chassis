## 概念阐述

ServiceComb提供了基于Vert.x的access log 和 request log功能。当用户使用REST over Vertx通信方式时，可以通过简单的配置启用access log打印功能。当用户 client 端进行远程调用时，可以通过简单的配置启用request log打印功能

## 场景描述

1. 用户在调试服务时可能需要开启access log。在使用REST over servlet通信方式的情况下，可以使用web容器的access log功能；而在使用REST over Vertx通信方式的情况下，可以使用ServiceComb提供的一套access log功能。

2. 用户想要跟踪，记录客户端远程调用信息， 可以开启 request log。request log 同时支持记录 rest 和 highway 远程调用方式。

## 配置说明

### 启用Access Log and Request Log

用户需要在microservice.yaml文件中增加配置以启用access log 和 request log，配置示例如下：

```yaml
servicecomb:
  accesslog:
    enabled: true  ## server 端 启用access log
    pattern: "%h - - %t %r %s %B %D" ##  server 端 自定义 access log 日志格式
    request:
      enabled: true  ## client 端开启 request log
      pattern: "%h %SCB-transport - - %t %r %s %D" ## client 端自定义 request log 日志格式
```

_**Access log & Request log 配置项说明**_

| 配置项 | 取值范围 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| servicecomb.accesslog.enabled | true/false | **false** | 如果为true则启用access log，否则不启用 |
| servicecomb.accesslog.pattern | 表示打印格式的字符串 | **"%h - - %t %r %s %B %D"** |  配置项见_**日志元素说明表**_ |
| servicecomb.accesslog.request.enabled | true/false | **false** | 如果为true则启用request log，否则不启用 |
| servicecomb.accesslog.request.pattern | 表示打印格式的字符串 | **"%h %SCB-transport - - %t %r %s %D"** |  配置项见_**日志元素说明表**_ |

> _**说明：**_
>
> * 以上两个配置项均可省略，若省略则使用默认值。

### 日志格式配置

目前可用的日志元素配置项见 ***日志元素说明表(Apache & W3C)*** 和 ***日志元素说明表(ServiceComb)*** 。

_**日志元素说明表 (Apache & W3C)**_

| 元素名称 | Apache日志格式 | W3C日志格式 | 说明 |
| :--- | :--- | :--- | :--- |
| HTTP method | %m | cs-method | - |
| HTTP status | %s | sc-status | - |
| Duration in second | %T | - | - |
| Duration in millisecond | %D | - | - |
| Remote hostname | %h | - | - |
| Local hostname | %v | - | - |
| Local port | %p | - | - |
| Size of response | %B | - | 如果消息体长度为零则打印"0" |
| Size of response | %b | - | 如果消息体长度为零则打印"-" |
| First line of request | %r | - | 包含HTTP Method、Uri、Http版本三部分内容 |
| URI path | %U | cs-uri-stem | - |
| Query string | %q | cs-uri-query | - |
| URI path and query string | - | cs-uri | - |
| Request protocol | %H | - | - |
| Datetime the request is received | %t | - | 按照默认设置打印时间戳，格式为"EEE, dd MMM yyyy HH:mm:ss zzz"，语言为英文，时区为GMT |
| Configurable datetime the request is received | %{PATTERN}t | - | 按照指定的格式打印时间戳，语言为英文，时区为GMT |
| Configurable datetime the request is received | %{PATTERN&#124;TIMEZONE&#124;LOCALE}t | - | 按照指定的格式、语言、时区打印时间戳。允许省略其中的某部分配置（但两个分隔符号"&#124;"不可省略）。 |
| Request header | %{VARNAME}i | - | 如果没有找到指定的header，则打印"-" |
| Response header | %{VARNAME}o | - | 如果没有找到指定的header，则打印"-" |
| Cookie | %{VARNAME}C | - | 如果没有找到指定的cookie，则打印"-" |

_**日志元素说明表(ServiceComb)**_

| Element | Placeholder | Comment |
| :----   | :---------- | :------ |
| TraceId | %SCB-traceId | 打印ServiceComb生成的trace id，找不到则打印"-" |
| Invocation Context | %{VARNAME}SCB-ctx | 打印key为`VARNAME`的invocation context值，找不到则打印"-" |
| Transport Method | %SCB-transport | 打印当前调用的 **transport method** 。 `rest` 或者 `highway`|

_**Access log 与 Request log的日志元素对比**_

| 元素名称 | Apache&W3C日志格式 | access log |access log 说明 | request log | request log说明 |
| --- | --- | --- | --- | --- | --- |
| HTTP method | %m & cs-method | support | - | support | - |
| HTTP status | %s & sc-status | support | - | support | - |
| Duration in second | %T | support | - | support | - |
| Duration in millisecond | %D | support | - | support | - |
| Remote hostname | %h |  support | - | support | - |
| Local hostname | %v | support | - | support | - |
| Local port | %p | support | - | support | - |
| Size of response | %B | support | 如果消息体长度为零则打印"0" | unsupported| - |
| Size of response | %b | support | 如果消息体长度为零则打印"-" | unsupported| - |
| First line of request |  %r | support | 包含HTTP Method、Uri、Http版本三部分内容| support | 包含HTTP Method、Uri、Http版本三部分内容|
|URI path| %U & cs-uri-stem | support | - | support | - |
|Query string | %q & cs-uri-query | support | - | support | - |
|URI path and query string | cs-uri  |  support | - | support | - |
| Request protocol |  %H | support | - | support | - |
| Datetime of the request | %t | support | 收到请求的时间。默认格式为"EEE, dd MMM yyyy HH:mm:ss zzz"，语言为英文，时区为GMT | support | 发送请求的时间。默认格式为"EEE, dd MMM yyyy HH:mm:ss zzz"，语言为英文，时区为GMT|
|Configurable datetime the request of the request | %{PATTERN}t | support | 收到请求的时间。按照指定的格式打印时间戳，语言为英文，时区为GMT | support | 发送请求的时间。按照指定的格式打印时间戳，语言为英文，时区为GMT|
|Configurable datetime the request of the request | %{PATTERN&#124;TIMEZONE&#124;LOCALE}t | support | 收到请求的时间。按照指定的格式、语言、时区打印时间戳。允许省略其中的某部分配置（但两个分隔符号"&#124;"不可省略）。| support | 发送请求的时间。按照指定的格式、语言、时区打印时间戳。允许省略其中的某部分配置（但两个分隔符号"&#124;"不可省略）。|
|Request header | %{VARNAME}i | support | 如果没有找到指定的header，则打印"-" | support | 如果没有找到指定的header，则打印"-" | 
|Response header | %{VARNAME}o | support | 如果没有找到指定的header，则打印"-"| support | 如果没有找到指定的header，则打印"-" |
|Cookie | %{VARNAME}C | support |  如果没有找到指定的cookie，则打印"-" | support |  如果没有找到指定的cookie，则打印"-" | 
|TraceId | %SCB-traceId | support |打印ServiceComb生成的trace id，找不到则打印"-" | support |打印ServiceComb生成的trace id，找不到则打印"-" |
|Invocation Context | %{VARNAME}SCB-ctx | support | 打印key为`VARNAME`的invocation context值，找不到则打印"-" |support | 打印key为`VARNAME`的invocation context值，找不到则打印"-" |
|transport method | %SCB-transport | unsupported | 只支持 rest 形式调用 | support | 调用使用的transport method|


### 日志输出文件配置

Access log的日志打印实现框架默认采用Log4j，并提供了一套默认的日志文件配置。用户可以在自己定义的log4j.properties文件中覆写这些配置。用户可配置的日志文件配置项见下表。

_**日志文件配置项**_

| 配置项 | 默认值 | 含义 | 说明 |
| :--- | :--- | :--- | :--- |
| paas.logs.accesslog.dir | ${paas.logs.dir} | 日志文件输出目录 | 与普通日志输出到同一个目录中 |
| paas.logs.accesslog.file | access.log | 日志文件名 | - |
| log4j.appender.access.MaxBackupIndex | 10 | 最大保存的日志滚动文件个数 | - |
| log4j.appender.access.MaxFileSize | 20MB | 日志文件最大体积 | 正在记录的文件达到此大小时触发日志滚动存储 |
| log4j.appender.access.logPermission | rw------- | 日志文件权限 | - |
| paas.logs.requestlog.dir | ${paas.logs.dir} | 日志文件输出目录 | 与普通日志输出到同一个目录中 |
| paas.logs.requestlog.file | request.log | 日志文件名 | - |
| log4j.appender.request.MaxBackupIndex | 10 | 最大保存的日志滚动文件个数 | - |
| log4j.appender.request.MaxFileSize | 20MB | 日志文件最大体积 | 正在记录的文件达到此大小时触发日志滚动存储 |
| log4j.appender.request.logPermission | rw------- | 日志文件权限 | - |

> _**注意：**_  
> 由于ServiceComb的日志打印功能只依赖slf4j的接口，因此用户可以选择其他日志打印框架，选择其他日志打印框架时需要用户自行配置日志文件输出选项。

### 日志实现框架切换为logback

> 针对采用logback作为日志打印框架的项目，需要将日志打印框架依赖从Log4j改为logback并添加部分配置以使access log功能正常生效。

#### 1. 排除Log4j依赖

在将日志实现框架切换为logback之前，需要检查项目的依赖，从中排除掉Log4j相关的依赖项。在项目中运行maven命令`dependency:tree`，找出其中依赖了Log4j的ServiceComb组件，在其`<dependency>`依赖项中添加如下配置：

```xml
<exclusion>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-log4j12</artifactId>
</exclusion>
```

#### 2. 添加logback依赖

在pom文件中添加logback的依赖项：

```xml
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-api</artifactId>
</dependency>
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-classic</artifactId>
</dependency>
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-core</artifactId>
</dependency>
```

#### 3. 配置access log & request log 组件的logger

由于ServiceComb提供的日志打印组件是获取名为`accesslog` & `requestlog` 的logger来打印access log  和 request log ，因此将日志实现框架从Log4j切换为logback的关键就是提供一个名为`accesslog` & `requestlog` 的 Logger，并为其配置好日志输出文件。以下是 **access log** & **request log** 在logback配置文件中的配置示例（本示例仅展示access log & request log相关的配置，其他日志配置均省略）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <!-- 用户可根据需要自定义appender -->
  <appender name="ACCESSLOG" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>./logs/access.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>./logs/access-%d{yyyy-MM-dd}.log</fileNamePattern>
    </rollingPolicy>
    <!-- 注意：由于access log的内容是在代码中完成格式化的，因此这里只需输出message即可，无需添加额外的格式 -->
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
  </appender>
  <appender name="REQUESTLOG" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>./logs/request.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>./logs/access-%d{yyyy-MM-dd}.log</fileNamePattern>
    </rollingPolicy>
    <!-- 注意：由于request log的内容是在代码中完成格式化的，因此这里只需输出message即可，无需添加额外的格式 -->
    <encoder>
      <pattern>%msg%n</pattern>
    </encoder>
  </appender>

  <!-- 提供一个名为"accesslog"的logger供access log打印组件使用 -->
  <logger name="accesslog" level="INFO" additivity="false">
    <appender-ref ref="ACCESSLOG" />
  </logger>
  <!-- 提供一个名为"requestlog"的logger供request log打印组件使用 -->
  <logger name="requestlog" level="INFO" additivity="false">
    <appender-ref ref="REQUESTLOG" />
  </logger>

</configuration>
```

### 自定义扩展Access Log & Request Log

用户可以利用ServiceComb提供的AccessLogItem扩展机制，定制自己的AccessLogItem， 我们把 Request Log 也当做一种 Access Log。

#### 相关类说明

1.  **AccessLogItem**

```java
public interface AccessLogItem<T> {
  // 从Server端获取信息，打印 Access Log 日志
  default void appendServerFormattedItem(ServerAccessLogEvent accessLogEvent, StringBuilder builder) {
  }

  // 从Client 端获取信息， 打印 Request Log
  default void appendClientFormattedItem(InvocationFinishEvent clientLogEvent, StringBuilder builder) {
  }
}
```

> **AccessLogItem** 的定义如上所示
>
> * **Server 端** 每收到一个请求，会触发一次 Access Log 日志打印。**Client 端** 每次对外远程调用结束，会触发一次 Request Log 日志打印。  
> 
> * 每次日志打印，SDK 都会遍历有效的 `AccessLogItem` ，调用对应的方法获取此Item生成的Log片段，并将全部片段拼接成一条Log打印到日志文件中。 


2. **VertxRestAccessLogItemMeta**

  ```java
  // pattern占位符前缀
  protected String prefix;
  // pattern占位符后缀
  protected String suffix;
  // 优先级序号
  protected int order;
  // AccessLogItem构造器
  protected AccessLogItemCreator<RoutingContext> accessLogItemCreator;
  ```
  
`VertxRestAccessLogItemMeta` 包含如上属性，它定义了ServiceComb如何解析pattern字符串以获得特定的AccessLogItem。
  
*  如果用户想要定义一个占位符为`%user-defined`的`AccessLogItem`，则需要声明一个`VertxRestAccessLogItemMeta`的子类，设置prefix="%user-defined"，suffix=null，当`AccessLogPatternParser`解析到"%user-defined"时，从此meta类中取得`AccessLogItemCreator`创建对应的`AccessLogItem`。**注意**：由于"%user-defined"占位符中没有变量部分，因此调用`AccessLogItemCreator`传入的配置参数为null。
  
*  如果用户想要定义一个占位符为`%{VARNAME}user-defined`的`AccessLogItem`，则声明的`VertxRestAccessLogItemMeta`子类中，设置prefix="%{"，suffix="}user-defined"，当`AccessLogPatternParser`解析到"%{VARNAME}user-defined"时，会截取出"VARNAME"作为配置参数传入`AccessLogItemCreator`，创建一个`AccessLogItem`。

  `VertxRestAccessLogItemMeta`有一个子类`CompositeVertxRestAccessLogItemMeta`，当用户需要定义多个AccessLogItem时，可以将多个`VertxRestAccessLogItemMeta`聚合到`CompositeVertxRestAccessLogItemMeta`中。Parser加载到类型为`CompositeVertxRestAccessLogItemMeta`的AccessLogItemMeta时，会调用其`getAccessLogItemMetas()`方法获得一组AccessLogItemMeta。`VertxRestAccessLogItemMeta`使用SPI机制加载，而`CompositeVertxRestAccessLogItemMeta`可以让用户只在SPI配置文件中配置一条记录就加载多条meta信息，给了用户更灵活的选择。

3. **AccessLogItemCreator**

  ```java
  public interface AccessLogItemCreator<T> {
    // 接收配置值，返回一个AccessLogItem。如果AccessLogItem的占位符没有可变的配置值部分，则此方法会接收到null。
    AccessLogItem<T> createItem(String config);
  }
  ```

  用户通过设置在自定义的`VertxRestAccessLogItemMeta`中的`AccessLogItemCreator`实例化自己的`AccessLogItem`。由于这是一个函数式接口，当`AccessLogItem`的初始化方式较简单时，可以直接使用Lambda表达式定义Creator，以简化开发。

#### AccessLogItemMeta的匹配规则

AccessLogItemMeta加载进Parser后，会进行一次排序。Parser解析pattern串时会从前到后匹配meta list，总的匹配规则如下：

1. 优先匹配高优先级的meta。

2. 优先匹配有后缀的meta，当匹配上多个有后缀meta时，取前后缀相距最小的一个。

3. 优先匹配占位符长的meta，例如有两个meta，"%abc"和"%a"，如果匹配中了"%abc"则直接返回，不再匹配"%a"。

#### 示例说明

1. 扩展自定义AccessLogItem

首先用户需要`AccessLogItem`接口实现自己的item：
 
```java
  public class UserDefinedAccessLogItem implements AccessLogItem<RoutingContext> {
    private String config;

    public UserDefinedAccessLogItem(String config) {
      this.config = config;
    }

    @Override
    public void appendServerFormattedItem(ServerAccessLogEvent accessLogEvent, StringBuilder builder) {
    builder.append("user-defined--server-")
        .append(accessLogEvent.getRoutingContext().response().getStatusCode())
        .append("-")
        .append(config);
    }
    
    @Override
    public void appendClientFormattedItem(InvocationFinishEvent clientLogEvent, StringBuilder builder) {
    builder.append("user-server-defined-")
        .append(clientLogEvent.getResponse().getStatus())
        .append("-")
        .append(config);
    }
  }
 ```

2. 定义AccessLogItem的meta类

  继承`VertxRestAccessLogItemMeta`或`CompositeVertxRestAccessLogItemMeta`类，定义AccessLogItem的前后缀等信息：
  
```java
public class UserDefinedCompositeExtendedAccessLogItemMeta extends CompositeVertxRestAccessLogItemMeta {
private static final List<VertxRestAccessLogItemMeta> META_LIST = new ArrayList<>();

static {
  META_LIST.add(new VertxRestAccessLogItemMeta("%{", "}user-defined", UserDefinedAccessLogItem::new));
}

@Override
public List<VertxRestAccessLogItemMeta> getAccessLogItemMetas() {
  return META_LIST;
}
}
```

3. 配置SPI加载文件

  在`resources/META-INF/services/`目录下定义一个名为"org.apache.servicecomb.transport.rest.vertx.accesslog.parser.VertxRestAccessLogItemMeta"的文件，将上一步中定义的meta类完整类名填写到该文件中，供Parser加载meta类。

4. 配置 Access Log的 pattern

```yaml
# 服务端配置
servicecomb:
  accesslog:
    enabled: true ## 应用作为服务端，开启 Access log
    pattern: "%{param}user-defined" ## Access log 日志格式
    request:   
      enabled: true  ## 应用作为客户端，开启 Request log
      pattern: "%{param}user-defined" ## Request log 日志格式
```

以服务端为例， 运行服务触发Access Log打印，假设请求返回状态码是 200，则可以看到Access Log打印内容为 "`user-defined--server-200-param`"。

## 示例代码

### microservice.yaml文件中的配置

```yaml
## other configurations omitted
servicecomb:
  accesslog:
    enabled: true ## 应用作为服务端，开启 Access log
    pattern: "%h - - %t %r %s %B %D" ## Access log 日志格式
    request:   
      enabled: true  ## 应用作为客户端，开启 Request log
      pattern: "%h %SCB-transport - - %t %r %s %D" ## Request log 日志格式
    
```

### log4j.properties文件中的配置

```properties
# log configuration item
paas.logs.dir=../logs/
paas.logs.accesslog.file=access.log
paas.logs.requestlog.file=request.log
# access log File appender
log4j.appender.access.MaxBackupIndex=10
log4j.appender.access.MaxFileSize=20MB
log4j.appender.access.logPermission=rw-------
# request log File appender
log4j.appender.request.MaxBackupIndex=10
log4j.appender.request.MaxFileSize=20MB
log4j.appender.request.logPermission=rw-------
```
