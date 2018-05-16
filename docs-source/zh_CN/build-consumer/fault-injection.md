## 场景描述

用户在consumer端使用故障注入，可以设置发往指定微服务的请求的时延和错误及其触发概率。

## 注意事项

延迟注入请求的延迟时间统一为毫秒级别

## 配置说明

故障注入配置在microservice.yaml文件中，相关配置项见下表。要开启服务消费者端的故障注入，还需要在处理链中配置消费端故障注入handler，配置示例如下：

```yaml
servicecomb:
  handler:
    chain:
      Consumer:
        default: loadbalance,fault-injection-consumer
```

故障注入配置项说明

\[scope\]代表故障注入的生效范围，可配置值包括全局配置\_global，或指定微服务的服务名\[ServiceName\]。

\[protocol\]代表使用的通信协议，可配置值包括rest或highway。

| 配置项 | 默认值 | 取值范围 | 是否必选 | 含义 | 注意 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| servicecomb.governance.Consumer.\[scope\].policy.fault.protocols.\[protocol\].delay.fixedDelay | 无 | \(0,9223372036854775807\]，长整形 | 否 | Consumer端发送延迟注入请求的延迟时间 | 目前时间单位是毫秒 |
| servicecomb.governance.Consumer.\[scope\].policy.fault.protocols.\[protocol\].delay.percent | 100 | \(0,100\]，整形 | 否 | Consumer端发送延迟注入请求的触发概率 |  |
| servicecomb.governance.Consumer.\[ServiceName\].schemas.\[schema\].policy.fault.protocols.\[protocol\].delay.fixedDelay | 无 | \(0,9223372036854775807\]，长整形 | 否 | Consumer端发送到对应schema的延迟注入请求的延迟时间 | 支持schema级别的配置 |
| servicecomb.governance.Consumer.\[ServiceName\].schemas.\[schema\].policy.fault.protocols.\[protocol\].delay.percent | 100 | \(0,100\]，整形 | 否 | Consumer端发送到对应schema的延迟注入请求的触发概率 | 支持schema级别的配置 |
| servicecomb.governance.Consumer.\[ServiceName\].schemas.\[schema\].operations.\[operation\].policy.fault.protocols.\[protocol\].delay.fixedDelay | 无 | \(0,9223372036854775807\]，长整形 | 否 | Consumer端发送到对应operation的延迟注入请求的延迟时间 | 支持operation级别的配置 |
| servicecomb.governance.Consumer.\[ServiceName\].schemas.\[schema\].operations.\[operation\].policy.fault.protocols.\[protocol\].delay.percent | 100 | \(0,100\]，整形 | 否 | Consumer端发送到对应operation的延迟注入请求的触发概率 | 支持operation级别的配置 |
| servicecomb.governance.Consumer.\[scope\].policy.fault.protocols.\[protocol\].abort.httpStatus | 无 | \(100,999\]，整形 | 否 | Consumer端发送错误注入请求的http错误码 |  |
| servicecomb.governance.Consumer.\[scope\].policy.fault.protocols.\[protocol\].abort.percent | 100 | \(0,100\]，整形 | 否 | Consumer端发送错误注入请求的触发概率 |  |
| servicecomb.governance.Consumer.\[ServiceName\].schemas.\[schema\].policy.fault.protocols.\[protocol\].abort.httpStatus | 无 | \(100,999\]，整形 | 否 | Consumer端发送到对应schema的错误注入请求的http错误码 | 支持schema级别的配置 |
| servicecomb.governance.Consumer.\[ServiceName\].schemas.\[schema\].policy.fault.protocols.\[protocol\].abort.percent | 100 | \(0,100\]，整形 | 否 | Consumer端发送到对应schema的错误注入请求的触发概率 | 支持schema级别的配置 |
| servicecomb.governance.Consumer.\[ServiceName\].schemas.\[schema\].operations.\[operation\].policy.fault.protocols.\[protocol\].abort.httpStatus | 无 | \(100,999\]，整形 | 否 | Consumer端发送到对应operation的错误注入请求的http错误码 | 支持operation级别的配置 |
| servicecomb.governance.Consumer.\[ServiceName\].schemas.\[schema\].operations.\[operation\].policy.fault.protocols.\[protocol\].abort.percent | 100 | \(0,100\]，整形 | 否 | Consumer端发送到对应operation的错误注入请求的触发概率 | 支持operation级别的配置 |

## 示例代码

```
servicecomb:
  governance:
    Consumer:
      _global:
        policy:
          fault:
            protocols:
              rest:
                delay:
                  fixedDelay: 5000
                  percent: 10
```

```
servicecomb:
  governance:
    Consumer:
      ServerFaultTest:
        schemas:
          schema:
            operations:
              operation:
                policy:
                  fault:
                    protocols:
                      rest:
                        abort:
                          httpStatus: 421
                          percent: 100
```



