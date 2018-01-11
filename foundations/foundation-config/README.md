# Config Center设计思路
* Config Center本身可以以客户端服务发现的方式被每一个微服务发现和消费。
* 微服务与Config Center的Transport等与Service Center相似，逻辑可以重用。


## 配置策略
* SDK 出厂时的默认值： SDK 内部的： meta-inf/microservice.yaml  在 Annotation 上指定
* 业务开发人员的调整： meta-info/microservice.yaml 系列  （yaml 文件支持 import 能力）
* 业务部署人员的调整：<br>
 磁盘 microservice.yaml
环境变量， 及环境变量制定的 yaml 文件
* 运行时期的治理调整：   配置中心的文件、UI调整

## 需求描述
### 配置供给(Source)：
 * loadDefault()      :    SDK 出厂时的默认值
 * loadFile()           ： 业务开发人员的调整
 * loadEnv()           ： 业务部署人员的调整
 * loadRemote()     ： 运行时期的治理调整
#### 期望：
 * OS  Env
 * 磁盘上一个： microservice.yaml -->   import "file-name"
 * jar 里面有一系列： /META-INF/microservice.yaml --> import "file-name"
 * ConfigManager

### 配置消费：
* yaml/properties: 底层机制， 基本能力 get()
* @标注消费
* @Spring友好消费
* notify()   : callback 机制


### 需求：
1. SDK 本身依赖在里面， 以 cse 开头， 静态的元信息
2. Handle 链的信息
3. handler 的默认值
4. Transport/Provider 的配置信息， 例如线程数、默认限额等



# 如何使用
可以参考和调试工程中的UT代码以获取如何使用的详细信息

## 引入工程后，需要在系统启动的时候对配置系统进行初始化
如：

    YAMLConfigurationSource configSource = new YAMLConfigurationSource();
    DynamicConfiguration configuration = new DynamicConfiguration(configSource, new FixedDelayPollingScheduler());
    ConfigurationManager.install(configuration);

配置文件默认是classpath下的microservice.yaml, 但是可以通过环境变量传入其他文件，可以设置的环境变量为：

|变量|描述|
|---|---|
|cse.configurationSource.additionalUrls|配置文件的列表，以,分隔的多个包含具体位置的完整文件名|
|cse.configurationSource.defaultFileName|默认配置文件名|
|cse.config.client.refreshMode|应用配置的刷新方式，0为config-center主动push，1为client周期pull，默认为0|
|cse.config.client.refreshPort|config-center推送配置的端口|
|cse.config.client.tenantName|应用的租户名称|
|cse.config.client.serviceName|应用名称|
|cse.config.client.regUri|service-center访问地址，http(s)://{ip}:{port}，以,分隔多个地址(可选，若不配置，则使用cse.config.client.serverUri中的配置中心地址)|
|cse.config.client.serverUri|config-center访问地址，http(s)://{ip}:{port}，以,分隔多个地址(可选，当cse.config.client.regUri配置为空时该配置项才会生效)|

## 在程序中获取配置信息

举例：

    DynamicDoubleProperty myprop = DynamicPropertyFactory.getInstance().getDoubleProperty("trace.handler.sampler.percent", 0.1);

具体方法可参考[API DOC](https://netflix.github.io/archaius/archaius-core-javadoc/com/netflix/config/DynamicPropertyFactory.html)

## 处理配置变更
可以注册callback处理配置变更：

	 myprop.addCallback(new Runnable() {
	      public void run() {
	          // Handle configuration changes
	      }
	  });

## 进行配置项映射
有些情况下，我们要屏蔽我们使用的一些开源组件的配置并给用户提供我们自己的配置项。在这种情况下，可以通过classpath下的config.yaml进行映射定义：

	registry:
	  client:
	    serviceUrl:
	      defaultZone: eureka.client.serviceUrl.defaultZone

定义映射后，在配置装载的时候框架会默认进行映射，把以我们定义的配置项映射为开源组件可以认的配置项。

## TODO
* 接入Config-center，做PUSH模式的动态刷新
* 对接Spring Configuration实现对业务系统的配置
*  配置中心和服务中心以及整个微服务runtime的关系和后续演进策略
