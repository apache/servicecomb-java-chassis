## 场景描述

用户通过简单的配置即可启用服务间RSA认证，以保障服务接口的安全性。



## Consumer配置

* 在pom.xml中增加依赖：

  ```
   <dependency> 
      <groupId>org.apache.servicecomb</groupId> 
      <artifactId>handler-publickey-auth</artifactId> 
    </dependency>
  ```

* 在microservice.yaml中添加进处理链

  ```
  servicecomb:
   ......
   handler:
    chain:
     Consumer:
      default: auth-consumer
   ......
  ```

## Provider配置

* 在pom.xml中增加依赖：

  ```
   <dependency> 
      <groupId>org.apache.servicecomb</groupId> 
      <artifactId>handler-publickey-auth</artifactId> 
    </dependency>
  ```

* 在microservice.yaml中添加进处理链

  ```
  servicecomb:
   ......
   handler:
    chain:
     Provider:
      default: auth-provider
   ......
  ```



