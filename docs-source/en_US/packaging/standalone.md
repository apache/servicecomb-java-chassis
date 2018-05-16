## 概念阐述

一个Standalone的容器，以简单的Main加载Spring启动，因为服务通常不需要Tomcat/JBoss等Web容器的特性，没必要用Web容器去加载服务。微框架提供了standalone部署运行模式，服务容器只是一个简单的Main方法，并加载一个简单的Spring容器，用于暴露服务。

## 操作步骤

* **步骤1** 编写Main函数，初始化日志和加载服务配置，内容如下：

```java
import com.huawei.paas.foundation.common.utils.BeanUtils;
import com.huawei.paas.foundation.common.utils.Log4jUtils;
public class MainServer {
public static void main(String[] args) throws Exception {
　Log4jUtils.init(); # 日志初始化
　BeanUtils.init();  # Spring bean初始化
 }
}
```

* **步骤2** 运行MainServer即可启动该微服务进程，向外暴露服务。

## 注意事项

如果使用的是rest网络通道，需要将pom中的transport改为使用cse-transport-rest-vertx包

