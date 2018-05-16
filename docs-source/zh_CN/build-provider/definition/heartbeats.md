## 场景描述

当微服务实例注册到服务中心后，微服务需要定时向服务中心发送心跳。若服务中心在一定时间内没有收到心跳信息，则会注销此实例。

## 涉及API

* `org.apache.servicecomb.serviceregistry.client.ServiceRegistryClient`：服务中心客户端

## 配置说明

`ServiceRegistryClient`提供了发送心跳的方法`heartbeat`，用户直接调用即可，示例代码如下：

```java
public static void main(String[] args) throws Exception {
    // 首先需要注册微服务和实例……
    // 发送心跳，不然实例会消失
    while (true) {
        System.out.println("heartbeat sended:" + client.heartbeat(service2.getServiceId(), instance.getInstanceId()));
        Thread.sleep(3000);
    }
}
```



