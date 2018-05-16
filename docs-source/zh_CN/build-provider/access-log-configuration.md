## 概念阐述

ServiceComb提供了基于Vert.x的access log功能。当用户使用REST over Vertx通信方式时，可以通过简单的配置启用access log打印功能。

## 场景描述

用户在调试服务时可能需要开启access log。在使用REST over servlet通信方式的情况下，可以使用web容器的access log功能；而在使用REST over Vertx通信方式的情况下，可以使用ServiceComb提供的一套access log功能。

## 配置说明

### 启用Access Log

用户需要在microservice.yaml文件中增加配置以启用access log，配置示例如下：

```yaml
servicecomb:
  accesslog:
    enabled: true  ## 启用access log
```

_**Access log 配置项说明**_

| 配置项 | 取值范围 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| servicecomb.accesslog.enabled | true/false | false | 如果为true则启用access log，否则不启用 |
| servicecomb.accesslog.pattern | 表示打印格式的字符串 | "%h - - %t %r %s %B" | 配置项见_**日志元素说明表**_ |

> _**说明：**_
>
> * 以上两个配置项均可省略，若省略则使用默认值。

### 日志格式配置

目前可用的日志元素配置项见_**日志元素说明表**_。

_**日志元素说明表**_

| 元素名称 | Apache日志格式 | W3C日志格式 | 说明 |
| :--- | :--- | :--- | :--- |
| Method | %m | cs-method |  |
| Status | %s | sc-status |  |
| Duration s | %T | - |  |
| Duration ms | %D | - |  |
| Remote Host | %h | - |  |
| Local Host | %v | - |  |
| Local port | %p | - |  |
| Bytes Written v1 | %B | - | 如果消息体长度为零则打印"0" |
| Bytes Written v2 | %b | - | 如果消息体长度为零则打印"-" |
| First line of request | %r | - | 包含HTTP Method、Uri、Http版本三部分内容 |
| URI path only | %U | cs-uri-stem |  |
| Query only | %q | cs-uri-query |  |
| URI path incl query | - | cs-uri |  |
| Version / Protocol | %H | - |  |
| Datetime Apache | %t | - | 按照默认设置打印时间戳，格式为"EEE, dd MMM yyyy HH:mm:ss zzz"，语言为英文，时区为GMT |
| Datetime Apache Configurable v1 | %{PATTERN}t | - | 按照指定的格式打印时间戳，语言为英文，时区为GMT |
| Datetime Apache Configurable v2 | %{PATTERN&#124;TIMEZONE&#124;LOCALE}t | - | 按照指定的格式、语言、时区打印时间戳。允许省略其中的某部分配置（但两个分隔符号"&#124;"不可省略）。 |
| Incoming Headers | %{IDENTIFIER}i | - | 如果没有找到指定的header，则打印"-" |
| Outgoing Response Headers | %{IDENTIFIER}o | - | 如果没有找到指定的header，则打印"-" |
| Cookie | %{IDENTIFIER}c | - | 如果没有找到指定的cookie，则打印"-" |
| TraceId | - | - | ServiceComb框架提供的TraceId打印元素，占位符格式为"%SCB-traceId" |

### 日志输出文件配置

Access log的日志打印实现框架默认采用Log4j，并提供了一套默认的日志文件配置。用户可以在自己定义的log4j.properties文件中覆写这些配置。用户可配置的日志文件配置项见下表。

_**日志文件配置项**_

| 配置项 | 默认值 | 含义 | 说明 |
| :--- | :--- | :--- | :--- |
| paas.logs.accesslog.dir | ${paas.logs.dir} | 日志文件输出目录 | 与普通日志输出到同一个目录中 |
| paas.logs.accesslog.file | access.log | 日志文件名 |  |
| log4j.appender.access.MaxBackupIndex | 10 | 最大保存的日志滚动文件个数 |  |
| log4j.appender.access.MaxFileSize | 20MB | 日志文件最大体积 | 正在记录的文件达到此大小时触发日志滚动存储 |
| log4j.appender.access.logPermission | rw------- | 日志文件权限 |  |

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

#### 3. 配置access log组件的logger

由于ServiceComb提供的日志打印组件是获取名为`accesslog`的logger来打印access log的，因此将日志实现框架从Log4j切换为logback的关键就是提供一个名为`accesslog`，并为其配置好日志输出文件。以下是access log在logback配置文件中的配置示例（本示例仅展示access log相关的配置，其他日志配置均省略）：

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

  <!-- 提供一个名为"accesslog"的logger供access log打印组件使用 -->
  <logger name="accesslog" level="INFO" additivity="false">
    <appender-ref ref="ACCESSLOG" />
  </logger>
</configuration>
```

## 示例代码

### microservice.yaml文件中的配置

```yaml
## other configurations omitted
servicecomb:
  accesslog:
    enabled: true  ## 启用access log
    pattern: "%h - - %t %r %s %B"  ## 自定义日志格式
```

### log4j.properties文件中的配置

```properties
# access log configuration item
paas.logs.accesslog.dir=../logs/
paas.logs.accesslog.file=access.log
# access log File appender
log4j.appender.access.MaxBackupIndex=10
log4j.appender.access.MaxFileSize=20MB
log4j.appender.access.logPermission=rw-------
```



