## 概念阐述

如果需要将该微服务加载到web容器中启动运行时，需要新建一个servlet工程包装一下，该servlet工程，根据需要，可以不写或写少量的引导代码即可。

## 开发示例

参考“开发服务提供者” -&gt; “通信协议" -&gt; "REST over Servlet"章节。

## 注意事项

Restful调用应该与web容器中其他静态资源调用（比如html、js等等）隔离开来，所以webroot后一段应该还有一层关键字，比如上面web.xml中举的例子（/test/rest）中的rest。

以tomcat为例，默认每个war包都有不同的webroot，这个webroot需要是basePath的前缀，比如webroot为test，则该微服务所有的契约都必须以/test打头。

当微服务加载在web容器中，并直接使用web容器开的http、https端口时，因为是使用的web容器的通信通道，所以需要满足web容器的规则。

## 



