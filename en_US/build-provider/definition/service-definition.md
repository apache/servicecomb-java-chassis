### 概念阐述

服务定义信息是微服务的身份标识，它定义了服务从属于哪个应用，以及名字和版本。服务定义信息中也可以有扩展信息，用于定义服务的属性元数据。

### 场景描述

当用户定义新的微服务或修改微服务的基本信息时，会涉及到服务定义信息的创建和修改操作。

### 配置说明

本章节介绍涉及microservice.yaml文件的以下配置项，文件在项目中的存放路径为src\main\resources\microservice.yaml。

表1-1配置项说明

| 配置项 | 默认值 | 取值范围 | 是否必选 | 含义 | 注意 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| APPLICATION\_ID | - | - | 是 | 应用名 | - |
| service\_description.name | - | - | 是 | 微服务名 | 应确保app内部唯一。微服务名支持数字、大小写字母和"-"、"\_"、"."三个特殊字符，但是不能以特殊字符作为首尾字符，命名规范为：^\[a-zA-Z0-9\]+$\|^\[a-zA-Z0-9\]\[a-zA-Z0-9\_-.\]\*\[a-zA-Z0-9\]$。 |
| service\_description.version | - | - | 是 | 微服务版本号 | - |
| service\_description.description | - | - | 否 | 微服务描述 |  |
| service\_description.properties | - | - | 否 | 微服务元数据配置（通过microservice.yaml文件进行配置） | - |
| service\_description.propertyExtendedClass | - | - | 否 | 微服务元数据配置（通过实现接口PropertyExtended进行配置） | 接口返回的配置会覆盖配置文件中key相同的配置。 |
| instance\_description.properties | - | - | 否 | 服务实例元数据配置（通过microservice.yaml文件进行配置） |  |
| instance\_description.propertyExtendedClass | - | - | 否 | 微服务元数据配置（通过实现接口PropertyExtended进行配置） | 同service\_description.propertyExtendedClass |

> 说明：
>
> * 服务的元数据会随服务一同注册到服务中心，如需修改，则要连同服务version一起变更。若想保持服务version不变，则需要通过服务管理中心统一变更元数据。
> * 默认情况下，微服务只支持同一个app内部的服务调用。可在微服务的properties中配置allowCrossApp=true属性，开启可被跨app访问权限。
> * 虽然微服务名、契约名中可以使用"."字符，但是不推荐在命名中使用"."。这是由于ServiceComb使用的配置文件是yaml格式的，"."符号用于分割配置项名称，如果微服务名、契约名中也包含了"."可能会导致一些支持微服务、契约级别的配置无法正确被识别。

### 示例代码

```yaml
APPLICATION_ID: helloTest # 应用名
service_description: # 服务描述
  name: helloServer # 微服务名称
  version: 0.0.1 # 服务版本号
  properties: # 元数据
    key1: value1
    key2: value2
  description: This is a description about the microservice # 微服务描述
  propertyExtentedClass: org.apache.servicecomb.serviceregistry.MicroServicePropertyExtendedStub
instance_description: #实例描述
  properties: #元数据
    key3: value3
  propertyExtentedClass: org.apache.servicecomb.serviceregistry.MicroServicePropertyExtendedStub
```



