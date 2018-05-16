## 场景描述

ServiceComb提供了基于Ribbon的负载均衡方案，用户可以通过配置文件配置负载均衡策略，当前支持随机、顺序、基于响应时间的权值等多种负载均衡路由策略。

## 配置说明

负载均衡策略在mocroservice.yaml文件中配置，配置项为`servicecomb.loadbalance.[MicroServiceName].[property name]`，其中若省略MicroServiceName，则为全局配置；若指定MicroServiceName，则为针对特定微服务的配置。

表1-1配置项说明

| 配置项 | 默认值 | 取值范围 | 是否必选 | 含义 | 注意 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| servicecomb.loadbalance.strategy.name | RoundRobin | RoundRobin（轮询）<br/>Random（随机）<br/>WeightedResponse（服务器响应时间权值）<br/>SessionStickiness（会话保持） | 否 | 负载均衡路由策略 | - |
| servicecomb.loadbalance.SessionStickinessRule.sessionTimeoutInSeconds | 30 | Integer | 否 | 客户端闲置时间，超过限制后选择后面的服务器。 | 暂不支持微服务配置。e.g. servicecomb.loadbalance.SessionStickinessRule.sessionTimeoutInSeconds，不能配置为servicecomb.loadbalance.DemoService.SessionStickinessRule.sessionTimeoutInSeconds |
| servicecomb.loadbalance.SessionStickinessRule.successiveFailedTimes | 5 | Integer | 否 | 客户端失败次数，超过后会切换服务器 | 暂不支持微服务配置 |
| servicecomb.loadbalance.retryEnabled | FALSE | Boolean | 否 | 负载均衡捕获到服务调用异常，是否进行重试 | - |
| servicecomb.loadbalance.retryOnNext | 0 | Integer | 否 | 尝试新的服务器的次数 | - |
| servicecomb.loadbalance.retryOnSame | 0 | Integer | 否 | 同一个服务器尝试的次数 | - |
| servicecomb.loadbalance.isolation.enabled | FALSE | Boolean | 否 | 是否开启故障实例隔离功能 | - |
| servicecomb.loadbalance.isolation.enableRequestThreshold | 20 | Integer | 否 | 当实例的调用总次数达到该值时开始进入隔离逻辑门槛 | - |
| servicecomb.loadbalance.isolation.continuousFailureThreshold | - | Integer | 否 | 当请求实例连续出错达到此阈值时触发实例隔离 | 若配置了此项则覆盖实例故障百分比的配置，否则按照实例故障百分比触发隔离。<br/>由于按请求错误率触发实例隔离在请求次数较多时不易触发也不易恢复，因此建议使用此配置项代替实例故障百分比配置。<br/>请求实例成功时会将连续错误次数请零以保证实例快速恢复。 |
| servicecomb.loadbalance.isolation.errorThresholdPercentage | 20 | Integer，区间为\(0,100\] | 否 | 实例故障隔离错误百分比 | - |
| servicecomb.loadbalance.isolation.singleTestTime | 10000 | Integer | 否 | 故障实例单点测试时间 | 单位为ms |
| servicecomb.loadbalance.transactionControl.policy | org.apache.servicecomb.loadbalance.filter.SimpleTransactionControlFilter | - | 否 | 动态路由分流策略 | 框架提供了简单的分流机制，开发者也可以实现自定义的分流过滤策略 |
| servicecomb.loadbalance.transactionControl.options | - | key/value pairs | 否 | 针对SimpleTransactionControlFilter分流策略的配置项，可添加任意项过滤标签 | - |

## 示例代码

负载均衡策略配置在src\main\resources\microservice.yaml文件中。

配置处理链：

```yaml
servicecomb:
  # other configurations omitted
  handler:
    chain:
      Consumer:
        default: loadbalance
```

增加路由策略：

```yaml
servicecomb:
  # other configurations omitted
  loadbalance:
    strategy:
      name: RoundRobin
```

## 自定义路由策略

用户可以在ServiceComb提供的路由策略框架下根据业务需要，通过编程的方式来开发路由策略。实施步骤如下：

* 实现接口`com.netflix.loadbalancer.IRule`中定义的接口方法。  
  路由选择逻辑在`public Server choose(Object key)`方法中实现。LoadBalancerStats是一个封装了负载均衡器当前运行状态的一个结构。通过获取stats中各个实例的运行指标，在choose方法中，判定将当前请求路由到哪个实例上进行处理。处理风格可以参照`org.apache.servicecomb.loadbalance.SessionStickinessRule`。
* 定义一个负载均衡规则工厂类，实现接口`ExtensionsFactory`。<br/>
其中，`boolean isSupport(String key, String value)`方法用于确定此工厂是否支持microservice.yaml文件中配置的规则（key值为`Configuration.PROP_RULE_STRATEGY_NAME`，value值为自定义的规则名称）；`IRule createLoadBalancerRule(String ruleName)`方法用于获取对应的规则。具体实现方式参考`org.apache.servicecomb.loadbalance.RuleNameExtentionsFactory`，该实现类需要打上`@Component`注解以保证能够被引用。
* 通过SDK配置该负载均衡策略，假如是`AbcRule`。则配置如下：<br/>
```yaml
  servicecomb:
    loadbalance:
      strategy:
        name: AbcRule
```

## 过滤器机制

handler-loadbalance模块提供了Filter机制，用来过滤选择provider实例，ServiceComb提供的Filter有`IsolationServerListFilter`和`ZoneAwareServerListFilterExt`，用户也可自行扩展Filter。

关于`IsolationServerListFilter`的用法参见[实例级故障隔离](/build-consumer/instance-isolation.md)。

### 使用`ZoneAwareServerListFilterExt`选择provider实例

`ZoneAwareServerListFilterExt`使得consumer实例可以优先选择和自己处于同一region和zone的provider实例进行调用。

> 这里的region和zone是一般性的概念，用户可以自定确定其业务含义以便应用于资源隔离的场景中。关于华为云ServiceStage中的实例隔离层次关系，参见[微服务实例之间的逻辑隔离关系](/build-provider/definition/isolate-relationship.md)。

#### 1. provider端配置

启动两个provider实例，其中一个provider实例的microservice.yaml文件中增加如下配置：
```yaml
servicecomb:
  # config region and zone information
  datacenter:
    name: myDC
    region: my-Region
    availableZone: my-Zone
```

#### 2. consumer端配置

在consumer端的microservice.yaml文件中增加如下配置：
```yaml
servicecomb:
  loadbalance:
    # add zone aware filter
    serverListFilters: zoneAware
    serverListFilter:
      zoneAware:
        className: org.apache.servicecomb.loadbalance.filter.ZoneAwareServerListFilterExt
  # config region and zone information
  datacenter:
    name: myDC
    region: my-Region
    availableZone: my-Zone
```

#### 3. 使consumer调用provider

由于`ZoneAwareServerListFilterExt`的存在，consumer实例会优先选择和自己同一个region和availableZone下的provider实例进行调用。如果没有region和zone都相同的provider实例，则选择region相同的实例；如果还是没有符合条件的实例，则选择任一可用实例做调用。

### 开发自定义过滤器

用户可以开发自定义的过滤器来满足自己的业务场景，步骤如下：
- 实现`ServerListFilterExt`接口，过滤provider实例列表的逻辑在`List<T> getFilteredListOfServers(List<T> servers)`方法中实现。
- 确保编译的filter实现类在classpath下。
- 在microservice.yaml文件中增加filter配置

#### 示例开发

现在假设用户需要为每个consumer和provider实例配置各自的优先级，从低到高为0~9，高优先级的provider实例只可以调用平级或低优先级的consumer实例，以此为前提开发一个示例。

1. 开发一个Filter，实现`ServerListFilterExt`接口，代码如下：
```java
  package org.servicecombexam.loadbalance.filter;
  // import is omitted
  public class ExamFilter implements ServerListFilterExt {
    public static final String PRIORITY_LEVEL = "priorityLevel";
    @Override
    public List<Server> getFilteredListOfServers(List<Server> servers) {
      List<Server> result = new ArrayList<>();
      String priority = RegistryUtils.getMicroserviceInstance().getProperties().get(PRIORITY_LEVEL);
      for (Server server : servers) {
        if (priorityMatched(priority, server)) {
          result.add(server);
        }
      }
      return result;
    }
    /**
     * if the priority level is not specified, it will be regarded as 0
     * @return whether a server can be invoked by this consumer instance, according to priority level
     */
    private boolean priorityMatched(String priority, Server server) {
      String serverPriority = ((CseServer) server).getInstance().getProperties().get(PRIORITY_LEVEL);
      if (!StringUtils.isEmpty(priority) && !StringUtils.isEmpty(serverPriority)) {
        return priority.compareTo(serverPriority) >= 0;
      }
      if (StringUtils.isEmpty(serverPriority)) {
        return true;
      }

      return false;
    }
  }
```
2. 在provider的microservice.yaml文件中配置优先级等级：
```yaml
  instance_description:
    properties:
      # instance_description.properties下的是实例级配置项，本示例将优先级配置放在这里存储。实验中会启动三个实例，优先级分别为0~2
      priorityLevel: 2
```
3. 在consumer的microservice.yaml文件中配置优先级等级和Filter：
```yaml
  instance_description:
    properties:
      # consumer优先级等级为1，可以调用优先级为0或1的provider实例
      priorityLevel: 1
  servicecomb:
    loadbalance:
      serverListFilters: priorityFilter # filter名称，可以以','分隔配置多个filter
      serverListFilter:
        priorityFilter: # 此处配置的filter名称需要与servicecomb.loadbalance.serverListFilters中配置的相符
          className: org.servicecombexam.loadbalance.filter.ExamFilter # 自定义filter的类名
```

令consumer调用provider进行验证，可以发现只有优先级为0和1的provider实例会被调用，而优先级为2的provider实例不会被调用。
