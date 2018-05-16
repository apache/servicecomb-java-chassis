## 场景描述

当服务消费者调用服务提供者的服务时，需要注册服务契约。消费者有两种方式获取提供者的服务契约，一种是从服务的提供方离线获取契约文件，手工配置到项目中；另一种是从服务中心自动下载契约。

## 配置说明

> 说明：服务契约的获取方式与服务消费者的开发方式是无关的，用户可以任意组合使用。

### 配置依赖服务

服务消费者需要在microservice.yaml文件中配置依赖的provider，示例配置如下：

```yaml
servicecomb:
  # other configurations omitted
  references:
    springmvc:
      version-rule: 0.0.1
```

> version-rule是版本匹配规则，有四种格式：
>
> * 精确版本匹配：例如`version-rule: 0.0.1`，表示只匹配版本号为0.0.1的服务提供者
> * 后续版本匹配：例如`version-rule: 1.0.0+`，表示匹配版本号大于或等于1.0.0的服务提供者
> * 最新版本：`version-rule: latest`，表示匹配最新版本的服务提供者
> * 版本范围：例如`1.0.0-2.0.2`，表示匹配版本号在1.0.0至2.0.2之间的服务提供者，包含1.0.0和2.0.2
>
> 此配置项默认为`latest`

### 手工配置服务契约

服务消费者的开发者在线下拿到服务提供者的契约，配置到消费者工程的特定目录下。服务契约在项目中的存放目录与[定义服务契约](/build-provider/define-contract.md)的配置说明部分相同。

microservice目录下的每一个目录代表一个微服务，微服务目录下的每一个yaml文件代表一个schema契约，文件名就是schemaId。applications目录下存放需要指明appId的服务契约，用于跨app调用等场景。目录结构如下所示：

```
resources
  - microservices
      - serviceName            # 微服务名
          - schemaId.yaml      # schema接口的契约
  - applications
      - appId                  # 应用ID
          - serviceName        # 微服务名
              - schemaId.yaml  # schema接口的契约
```

### 从服务中心自动下载契约

服务消费者也可以不用显式地将契约存放在项目目录中，当程序启动时，ServiceComb框架会自动根据microservice.yaml文件中配置的服务提供者的微服务名称和版本号，从服务中心自动下载契约信息。

