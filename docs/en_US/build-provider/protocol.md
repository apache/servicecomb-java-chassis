### 概念阐述

ServiceComb实现了两种网络通道，包括REST和Highway，均支持TLS加密传输。其中，REST网络通道将服务以标准RESTful形式发布，调用端兼容直接使用http client使用标准RESTful形式进行调用。

### 注意事项

参数和返回值的序列化：

当前REST通道的body参数只支持application/json序列化方式，如果要向服务端发送form类型的参数，那么需要在调用端构造好application/json格式的body，不能直接以multipart/form-data格式传递form类型参数。

当前REST通道返回值支持application/json和text/plain两种格式，服务提供者通过produces声明可提供序列化能力，服务消费者通过请求的Accept头指明返回值序列化方式，默认返回application/json格式的数据。

