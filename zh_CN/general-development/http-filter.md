某些场景中，业务使用http而不是https，作为网络传输通道，此时为了防止被伪造或篡改请求，需要提供consumer、producer之间对http码流的签名功能。

签名功能使用org.apache.servicecomb.common.rest.filter.HttpClientFilter和org.apache.servicecomb.common.rest.filter.HttpServerFilter接口来承载，建议http码流强相关的逻辑使用这里的Filter机制，而契约参数相关逻辑使用Handler机制。

关于Filter接口的使用，可以参考[demo-signature](https://github.com/ServiceComb/ServiceComb-Java-Chassis/tree/master/demo/demo-signature)。



# 1.概述

Filter机制使用Java标准的SPI机制加载。

HttpClientFilter、HttpServerFilter都各自允许加载多个：

* 各实例之间的执行顺序由getOrder的返回值决定

* 如果getOrder返回值相同，则相应的实例顺序随机决定

无论是request，还是response，读取body码流，都使用getBodyBytes\(\)，返回值可能为null（比如get调用的场景），如果不为null，对应的码流长度，通过getBodyBytesLength\(\)获取。

# 2.HttpClientFilter

系统内置2个HttpClientFilter，扩展功能时注意order值不要冲突：

* org.apache.servicecomb.provider.springmvc.reference.RestTemplateCopyHeaderFilter, order值为Integer.MIN\_VALUE

* org.apache.servicecomb.transport.rest.client.http.DefaultHttpClientFilter, order值为Integer.MAX\_VALUE

## 2.1原型

```
public interface HttpClientFilter {
  int getOrder();

  void beforeSendRequest(Invocation invocation, HttpServletRequestEx requestEx);

  // if finished, then return a none null response
  // if return a null response, then sdk will call next filter.afterReceive
  Response afterReceiveResponse(Invocation invocation, HttpServletResponseEx responseEx);
}
```

## 2.2 beforeSendRequest

用于在已经生成码流之后，发送请求之前，根据url、header、query、码流计算签名，并设置到header中去\(requestEx.setHeader\)。

从入参invocation中可以获取本次调用的各种元数据以及对象形式的参数（码流是根据这些参数生成的）。

## 2.3 afterReceiveResponse

用于在从网络收到应答后，根据header、码流计算签名，并与header中的签名对比。如果签名不对，直接构造一个Response

作为返回值，只要不是返回NULL，则框架会中断对其他HttpClientFilter的调用。

# 3 HttpServerFilter

## 3.1原型

```
public interface HttpServerFilter {
  int getOrder();

  default boolean needCacheRequest(OperationMeta operationMeta) {
    return false;
  }

  // if finished, then return a none null response
  // if return a null response, then sdk will call next filter.afterReceiveRequest
  Response afterReceiveRequest(Invocation invocation, HttpServletRequestEx requestEx);

  // invocation maybe null
  void beforeSendResponse(Invocation invocation, HttpServletResponseEx responseEx);
}
```

## 3.2 needCacheRequest

与HttpClientFilter不同的是，增加了决定是否缓存请求的功能。

这是因为ServiceComb不仅仅能使用standalone的方式运行，也能运行于web容器（比如tomcat），在servlet的实现上，请求码流只能读取一次，并且不一定支持reset（比如tomcat），RESTful框架需要执行反序列化，需要读取body码流，签名逻辑也需要读取body码流，如果使用默认的处理，必然有一方功能无法实现。

所以运行于web容器场景时，所有HttpServerFilter，只要有一个返回需要缓存请求，则body码流会被复制保存起来，以支持重复读取。

入参是本次请求对应的元数据，业务可以针对该请求决定是否需要缓存请求。

## 3.3 afterReceiveRequest

在收到请求后，根据url、header、query、码流计算签名，并与header中的签名对比,如果签名不对，直接构造一个Response作为返回值，只要不是返回NULL，则框架会中断对其他HttpClientFilter的调用。

## 3.4 beforeSendResponse

在发送应答之前，根据header、码流计算签名，并设置到header中去。

因为可能invocation还没来得及构造，调用流程已经出错，所以入参invocation可能是null。



