## 场景描述

实例级故障隔离功能允许在微服务的部分实例调用失败时，停止向故障实例发送请求，从而达到隔离故障实例的功能。

## 配置说明

实例级故障隔离功能集成在负载均衡功能中，负载均衡策略的配置见[负载均衡策略](/build-provider/configuration/lb-strategy.md)。负载均衡策略中，与实例级故障隔离相关的配置项见下表。

| 配置项 | 默认值 | 取值范围 | 是否必选 | 含义 | 注意 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| servicecomb.loadbalance.isolation.enabled | false | Boolean | 否 | 是否开启故障实例隔离功能 | - |
| servicecomb.loadbalance.isolation.continuousFailureThreshold | - | Integer | 否 | 当请求实例连续出错达到此阈值时触发实例隔离 | 若配置了此项则覆盖实例故障百分比的配置，否则按照实例故障百分比触发隔离。<br/>由于按请求错误率触发实例隔离在请求次数较多时不易触发也不易恢复，因此建议使用此配置项代替实例故障百分比配置。<br/>请求实例成功时会将连续错误次数请零以保证实例快速恢复。 |
| servicecomb.loadbalance.isolation.enableRequestThreshold | 20 | Integer | 否 | 当实例的调用总次数达到该值时开始进入隔离逻辑门槛 | - |
| servicecomb.loadbalance.isolation.errorThresholdPercentage | 20 | Integer，区间为\(0,100\] | 否 | 实例故障隔离错误百分比 | - |
| servicecomb.loadbalance.isolation.singleTestTime | 10000 | Integer | 否 | 故障实例单点测试时间 | - |

## 示例代码

```yaml
servicecomb:
  # other configuration omitted
  loadbalance:
    isolation:
      enabled: true
      singleTestTime: 5000
      continuousFailureThreshold: 5 # 调用某实例连续5次出错则隔离此实例
    strategy:
      name: RoundRobin
```
