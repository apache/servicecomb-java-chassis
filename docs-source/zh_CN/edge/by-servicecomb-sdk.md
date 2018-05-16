# 一、功能范围

Edge Service作为整个微服务系统对外的接口，向最终用户提供服务，接入RESTful请求，转发给内部微服务。Edge Service以开发框架的形式提供，其中的服务映射、请求解析、加密解密、鉴权等逻辑需要业务通过扩展点自行开发。

Edge Service本身也是一个微服务，需遵守所有微服务开发的规则，其本身部署为多实例，前端需使用负载均衡装置进行负载分发。

页面开发、页面模板等表现层，不在Edge Service框架范围内，使用自行使用合适的技术去承载。但是Edge Service也提供了足够的灵活性，可以使用vertx API实现非ServiceComb SDK开发的微服务的请求转发，可以查看专题文章中示例。

# 二、特性

## 1.部署、启动无耦合

Edge Service与转发目标微服务之间，没有部署、启动顺序上的要求，仅仅只要求转发请求时，目标微服务已经部署，并且已经启动。

## 2.自动根据能力集合进行路由

假设某微服务，兼容规划为所有高版本必须兼容低版本，部署了以下版本实例：

* 1.0.0，提供了operation1

* 1.1.0，提供了operation1、operation2

Edge Service在转发operation1时，会自动使用1.0.0+的规则来过滤实例

Edge Service在转发operation2时，会自动使用1.1.0+的规则来过滤实例

以上过程用户不必做任何干预，全自动完成，以避免将新版本的operation转发到旧版本的实例中去。

## 3.治理

路由转发基于ServiceComb sdk，是标准的ServiceComb consumer机制，自动支持所有consumer端的治理功能。

# 三、部署示例

![](/assets/deployment.png)

# 四、工作模式

## 1.reactive \(默认\)

Edge Service默认工作于高性能的reactive模式，此模式要求工作于Edge Service转发流程中的业务代码不能有任何的阻塞操作，包括不限于：

* 远程同步调用，比如同步查询数据库、同步调用微服务，或是同步查询远程缓存等等

* 任何的sleep调用

* 任何的wait调用

* 超大的循环

Edge Service的底层是基于netty的vertx，以上约束即是netty的reactive模式约束。

![](/assets/reactive.png)

## 2.线程池

如果业务模型无法满足reactive要求，则需要使用线程池模式。

此时需要在Edge Service的microservice.yaml中配置：

```
servicecomb:
  executors:
    default: servicecomb.executor.groupThreadPool
```

这里的servicecomb.executor.groupThreadPool是ServiceComb内置的默认线程池对应的spring bean的beanId；业务可以定制自己的线程池，并声明为一个bean，其beanId也可以配置到这里。

![](/assets/threadPool.png)

## 五、调用流程

蓝色背景部分在Eventloop线程中执行

黄色背景部分：

* 如果工作于reactive模式，则直接在Eventloop线程执行

* 如果工作于线程池模式，则在线程池的线程中执行

![](/assets/workFlow.png)

## 六、开发手册

请参考github上的edge service demo：

[https://github.com/ServiceComb/ServiceComb-Java-Chassis/tree/master/demo/demo-edge](https://github.com/ServiceComb/ServiceComb-Java-Chassis/tree/master/demo/demo-edge)

该demo包含以下工程：

* authentication：微服务：鉴权服务器
* edge-service
* hiboard-business-1.0.0微服务：business，1.0.0版本，operation add
* hiboard-business-1.1.0微服务：business，1.1.0版本，operation add/dec
* hiboard-business-2.0.0微服务：business，2.0.0版本，operation add/dec
* hiboard-consumer作为一个普通的httpclient，而不是servicecomb consumer
* hiboard-model非微服务，仅仅是一些公共的model

通过edge-service访问微服务business的不同版本，并确认是由正确的实例处理的。

## 1.注册Dispatcher

实现接口org.apache.servicecomb.transport.rest.vertx.VertxHttpDispatcher，或从org.apache.servicecomb.edge.core.AbstractEdgeDispatcher继承，实现自己的dispatcher功能。

实现类通过java标准的SPI机制注册到系统中去。

Dispatcher需要实现2个方法：

* ### getOrder

Dispatcher需要向vertx注入路由规则，路由规则之间是有优先级顺序关系的。

系统中所有的Dispatcher按照getOrder的返回值按从小到大的方式排序，按顺序初始化。

如果2个Dispatcher的getOrder返回值相同，则2者的顺序不可预知。

* ### init

init方法入参为vertx框架中的io.vertx.ext.web.Router，需要通过该对象实现路由规则的定制。

可以指定满足要求的url，是否需要处理cookie、是否需要处理body、使用哪个自定义方法处理收到的请求等等

更多路由规则细节请参考vertx官方文档：[vertx路由机制](http://vertx.io/docs/vertx-web/java/#_routing_by_exact_path)

_提示：_

_多个Dispatcher可以设置路由规则，覆盖到相同的url。_

_假设Dispatcher A和B都可以处理同一个url，并且A优先级更高，则：_

* _如果A处理完，既没应答，也没有调用RoutingContext.next\(\)，则属于bug，本次请求挂死了_

* _如果A处理完，然后调用了RoutingContext.next\(\)，则会将请求转移给B处理_

## 2.转发请求

注册路由时，指定了使用哪个方法来处理请求（下面使用onRequest来指代该方法），在onRequest中实现转发逻辑。

方法原型为：

```
void onRequest(RoutingContext context)
```

系统封装了org.apache.servicecomb.edge.core.EdgeInvocation来实现转发功能，至少需要准备以下参数：

* microserviceName，业务自行制定规则，可以在url传入，或是根据url查找等等

* context，即onRequest的入参

* path，转发目标的url

* httpServerFilters，Dispatcher父类已经初始化好的成员变量

```
 EdgeInvocation edgeInvocation = new EdgeInvocation();
 edgeInvocation.init(microserviceName, context, path, httpServerFilters);
 edgeInvocation.edgeInvoke();
```

edgeInvoke调用内部，会作为ServiceComb标准consumer去转发调用。

作为标准consumer，意味着ServiceComb所有标准的治理能力在这里都是生效的。

## 3.设置兼容规则

不同的业务可能有不同的兼容规划，servicecomb默认的兼容规则，要求所有新版本兼容旧版本。如果满足这个要求，则不必做任何特殊的设置。

还有一种典型的规划：

* 1.0.0-2.0.0内部兼容，url为/microserviceName/v1/….的形式

* 2.0.0-3.0.0内部兼容，url为/microserviceName/v2/….的形式

  ……

各大版本之间不兼容

此时，开发人员需要针对EdgeInvocation设置兼容规则：

```
private CompatiblePathVersionMapper versionMapper = new CompatiblePathVersionMapper();

……

edgeInvocation.setVersionRule(versionMapper.getOrCreate(pathVersion).getVersionRule());
```

versionMapper的作用是将v1或是v2这样的串，转为1.0.0-2.0.0或2.0.0-3.0.0这样的兼容规则。

**注意：**

接口不兼容会导致非常多的问题。java chassis要求高版本服务兼容低版本服务，只允许增加接口不允许删除接口。在增加接口后，必须增加微服务的版本号。在开发阶段，接口变更频繁，开发者往往忘记这个规则。当这个约束被打破的时候，需要清理服务中心微服务的信息，并重启微服务和Edge Service\(以及依赖于该微服务的其他服务\)。否则可能出现请求转发失败等情况。

## 4.鉴权

Edge Service是系统的边界，对于很多请求需要执行鉴权逻辑。

基于标准的ServiceComb机制，可以通过handler来实现这个功能。

最简单的示意代码如下：

```
public class AuthHandler implements Handler {
 private Auth auth;

 public AuthHandler() {
 auth = Invoker.createProxy("auth", "auth", Auth.class);
 }
……

 @Override
 public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
 if (!auth.auth("")) {
 asyncResp.consumerFail(new InvocationException(Status.UNAUTHORIZED, (Object) "auth failed"));
 return;
 }

 LOGGER.debug("auth success.");
 invocation.next(asyncResp);
 }
}
```

Auth表示是鉴权微服务提供的接口，Invoker.createProxy\("auth", "auth", Auth.class\)是透明RPC开发模式中consumer的底层api，与@ReferenceRpc是等效，只不过不需要依赖spring bean机制。

Auth接口完全由业务定义，这里只是一个示例。

Handler开发完成后，配置到edge service的microservice.yaml中：

```
servicecomb:
  handler:
    chain:
      Consumer:
        default: auth,……
        service:
          auth: ……
```

这个例子，表示转发请求给所有的微服务都必须经过鉴权，但是调用鉴权微服务时不需要鉴权。

