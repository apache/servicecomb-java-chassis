## 场景描述

用户使用域名连接华为公有云或者三方系统时，需要使用到域名解析系统。在不同的系统、不同的框架使用的域名解析机制都可能不太一样。所以我们有必要提供一个统一的配置入口，以便开发运维人员可以自定义DNS解析机制，而不完全受制于系统配置。

## DNS配置

DNS配置项写在microservice.yaml文件中，支持统一制定证书，也可以添加tag进行更细粒度的配置，有tag的配置会覆盖全局配置，配置格式如下：

```
addressResolver.[tag].[property]
```

常见的tag如下表：   

| 项目 | tag |
| :--- | :--- |
| 服务中心 | sc.consumer |
| 配置中心 | cc.consumer |
| 看板中心 | mc.consumer |
| 用户自定义 | self.tag |

各个properties详细说明（设置Vertx DNS解析）    

``` yaml
addressResolver:
  servers: 8.8.8.8,8.8.4.4   #对应Linux /etc/resolv.conf的nameserver,DNS服务器地址，支持配置多个，以逗号隔开
  ndots: 1                   #对应linux /etc/resolv.conf里面的options: ndots, 作用就是如果给的域名里面包含的点的个数少于该阈值，那么DNS解析的时候就会默认加上searchDomains的值，这个必须和searchDomains搭配使用，Linux默认为1，华为公有云PAAS（包含容器）默认是4
  searchDomains: a,b,c       #对应linux /etc/resolv.conf里面的search，和ndots搭配使用，如果当前域名的点个数少于设置值，解析时就会把这些值添加到域名后面一起解析，比如ndots设置的为4，当前域名为servicecomb.cn-north-1.myhwclouds.com，只有三个点，那么解析的时候就会自动加上servicecomb.cn-north-1.myhwclouds.com.a去解析，没解析出来在用servicecomb.cn-north-1.myhwclouds.com.b，直到能最后解析出来
  optResourceEnabled: true   #optional record is automatically included in DNS queries
  cacheMinTimeToLive: 0      #最小缓存时间
  cacheMaxTimeToLive: 10000  #最大缓存时间
  cacheNegativeTimeToLive: 0 #DNS解析失败后，下次重试的等待时间
  queryTimeout: 5000         #查询超时时间
  maxQueries: 4              #查询次数
  rdFlag: true               #设置DNS递归查询
  rotateServers: true        #设置是否支持轮询
```
## 示例

```java
VertxOptions vertxOptions = new VertxOptions();
vertxOptions.setAddressResolverOptions(AddressResolverConfig.getAddressResover("self.tag"));
Vertx vertx = VertxUtils.getOrCreateVertxByName("registry", vertxOptions);
// this has to set the client options
HttpClientOptions httpClientOptions = createHttpClientOptions();
ClientPoolManager<HttpClientWithContext> clientMgr = new ClientPoolManager<>(vertx, new HttpClientPoolFactory(httpClientOptions));
clientMgr.findThreadBindClientPool().runOnContext(httpClient -> {
    // do some http request
});
```
