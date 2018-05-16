## 场景描述

用户在consumer端使用限流策略，可以限制发往指定微服务的请求的频率。

## 注意事项

参考3.7.2-限流策略。

## 配置说明

限流策略配置在microservice.yaml文件中，相关配置项见下表。要开启服务消费者端的限流策略，还需要在处理链中配置消费端限流handler，配置示例如下：

```yaml
servicecomb:
  handler:
    chain:
      Consumer:
        default: qps-flowcontrol-consumer
```

QPS流控配置项说明

| 配置项 | 默认值 | 取值范围 | 是否必选 | 含义 | 注意 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| servicecomb.flowcontrol.Consumer.qps.enabled | true | Boolean | 否 | 是否启用Consumer流控 | - |
| servicecomb.flowcontrol.Consumer.qps.limit.\[ServiceName\].\[Schema\].\[operation\] | 2147483647  \(max int\) | \(0,2147483647\]，整形 | 否 | 每秒钟允许的请求数 | 支持microservice、schema、operation三个级别的配置 |



