## 概念阐述

服务提供者的启动过程分为初始化Log4j、bean加载（包括配置加载）和服务注册三部分。

* 初始化Log4j

`Log4jUtils`默认会从`classpath\*:config/base/log4j.properties`和`classpath\*:config/log4j.properties`两个路径读取log4j配置并合并，将其传递给Log4j的`PropertyConfigurator`方法初始化Log4j。如果优先级最高的配置文件是在磁盘上，且其所在目录有写权限，则将合并的配置输出到该目录，以便于维护时观察生效的参数。

* bean加载

`BeanUtils`默认会从`classpath\*:META-INF/spring/\*.bean.xml`路径加载配置文件，并将其传递给Spring框架的`ClassPathXmlApplicationContext`完成应用上下文加载。在这个过程中会加载foundation-config模块的bean，完成配置加载。

* 服务注册

在Spring context加载完成后，`org.apache.servicecomb.core.CseApplicationListener`会加载handler配置、加载provider的schema信息，最后将微服务信息注册到服务中心。

> **说明：**  
> ServiceComb的配置项分为三个层次：配置中心、环境变量、本地文件，三者优先级依次递减，高优先级中的配置项覆盖低优先级同名配置项。存放在配置中心的配置项可以在运行时修改。



