## 场景描述

用户通过简单的配置即可启用TLS通信，以保障数据的传输安全。

## 外部服务通信配置

与外部服务通信相关的配置写在microservice.yaml文件中。

* 服务中心、配置中心TLS通信配置  
  微服务与服务中心、配置中心的连接可以通过将http改为https启用TLS通信，配置示例如下：

  ```yaml
  servicecomb:
    service:
      registry:
        address: https://127.0.0.1:30100
    config:
      client:
        serverUri: https://127.0.0.1:30103
  ```

* 服务提供者启用TLS通信  
  服务提供者在配置服务监听地址时，可以通过在地址后面追加`?sslEnabled=true`开启TLS通信，示例如下：

  ```yaml
  servicecomb:
    rest:
      address: 0.0.0.0:8080?sslEnabled=true
    highway:
      address: 0.0.0.0:7070?sslEnabled=true
  ```

## 证书配置

证书配置项写在microservice.yaml文件中，支持统一制定证书，也可以添加tag进行更细粒度的配置，有tag的配置会覆盖全局配置，配置格式如下：

```
ssl.[tag].[property]
```
常见的tag如下表：   

| 项目 | tag |
| :--- | :--- |
| 服务中心 | sc.consumer |
| 配置中心 | cc.consumer |
| 看板中心 | mc.consumer |
| Rest服务端 | rest.provider |
| Highway服务端 | highway.provider |
| Rest客户端 | rest.consumer|
| Highway客户端 | highway.consumer|
| auth客户端 | apiserver.consumer|
一般不需要配置tag，正常情况分为三类：1、连接内部服务 2、作为服务端 3、作为客户端 所以如果这三类要求的证书不一致，那么需要使用tag来区分

证书配置项见表1 证书配置项说明表。  
**表1 证书配置项说明表**

| 配置项 | 默认值 | 取值范围 | 是否必选 | 含义 | 注意 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| ssl.engine| jdk | - | 否 | ssl协议，提供jdk/openssl选择 | 默认为jdk |
| ssl.protocols | TLSv1.2 | - | 否 | 协议列表 | 使用逗号分隔 |
| ssl.ciphers | TLS\_ECDHE\_RSA\_WITH\_AES\_256\_GCM\_SHA384,<br/>TLS\_RSA\_WITH\_AES\_256\_GCM\_SHA384,<br/>TLS\_ECDHE\_RSA\_WITH\_AES\_128\_GCM\_SHA256,<br/>TLS\_RSA\_WITH\_AES\_128\_GCM\_SHA256 | - | 否 | 算法列表 | 使用逗号分隔 |
| ssl.authPeer | true | - | 否 | 是否认证对端 | - |
| ssl.checkCN.host | true | - | 否 | 是否对证书的CN进行检查 | 该配置项只对Consumer端，并且使用http协议有效，即Consumser端使用rest通道有效。对于Provider端、highway通道等无效。检查CN的目的是防止服务器被钓鱼，参考标准定义：[https://tools.ietf.org/html/rfc2818。](https://tools.ietf.org/html/rfc2818。) |
| ssl.trustStore | trust.jks | - | 否 | 信任证书文件 | - |
| ssl.trustStoreType | JKS | - | 否 | 信任证书类型 | - |
| ssl.trustStoreValue | - | - | 否 | 信任证书密码 | - |
| ssl.keyStore | server.p12 | - | 否 | 身份证书文件 | - |
| ssl.keyStoreType | PKCS12 | - | 否 | 身份证书类型 | - |
| ssl.keyStoreValue | - | - | 否 | 身份证书密码 | - |
| ssl.crl | revoke.crl | - | 否 | 吊销证书文件 | - |
| ssl.sslCustomClass | - | org.apache.servicecomb.foundation.ssl.SSLCustom的实现类 | 否 | SSLCustom类的实现，用于开发者转换密码、文件路径等。 | - |

> **说明**：
>
> * 默认的协议算法是高强度加密算法，JDK需要安装对应的策略文件，参考：[http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)。 您可以在配置文件配置使用非高强度算法。
> * 微服务消费者，可以针对不同的提供者指定证书（当前证书是按照HOST签发的，不同的提供者都使用一份证书存储介质，这份介质同时给微服务访问服务中心和配置中心使用）。

## 示例代码

microservice.yaml文件中启用TLS通信的配置示例如下：
```yaml
servicecomb:
  service:
    registry:
      address: https://127.0.0.1:30100
  config:
    client:
      serverUri: https://127.0.0.1:30103
  rest:
    address: 0.0.0.0:8080?sslEnabled=true
  highway:
    address: 0.0.0.0:7070?sslEnabled=true

#########SSL options
ssl.protocols: TLSv1.2
ssl.authPeer: true
ssl.checkCN.host: true

#########certificates config
ssl.trustStore: trust.jks
ssl.trustStoreType: JKS
ssl.trustStoreValue: Changeme_123
ssl.keyStore: server.p12
ssl.keyStoreType: PKCS12
ssl.keyStoreValue: Changeme_123
ssl.crl: revoke.crl
ssl.sslCustomClass: org.apache.servicecomb.demo.DemoSSLCustom
```



