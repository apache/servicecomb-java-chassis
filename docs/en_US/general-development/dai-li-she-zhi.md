## 背景

作为一名开发者，在公司开发环境，可能是通过公司代理网络接入到因特网。如果调试服务时还必须依赖网上资源，比如直接连接华为共有云服务中心，那么就必须配置代理。

配置方式，在microservice.yaml文件增加proxy配置：

```yaml
servicecomb:
  proxy:
    enable: true            #是否开启代理
    host: yourproxyaddress  #代理地址
    port: 80                #代理端口
    username: yourname      #用户名
    passwd: yourpassword    #密码
```

**注意：当前仅支持连接服务中心、配置中心、服务看板支持代理，如果对接其他三方服务，可以读取这个配置，自行配置代理，vertx httpclient支持代理设置，例如：**

```java
    HttpClientOptions httpClientOptions = new HttpClientOptions();
    if (isProxyEnable()) {
      ProxyOptions proxy = new ProxyOptions();
      proxy.setHost("host");
      proxy.setPort(port);
      proxy.setUsername("username");
      proxy.setPassword("passwd");
      httpClientOptions.setProxyOptions(proxy);
    }
```



