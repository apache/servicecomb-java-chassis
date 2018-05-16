文件上传，当前只在vertx rest通道中可用，servlet rest的支持后续会增加。

文件上传使用标准的http form格式，可与浏览器的上传直接对接。

## Producer：

在jaxrs或springmvc开发模式下可用：

* 支持servlet定义的javax.servlet.http.Part类型，

* Springmvc模式下，也支持org.springframework.web.multipart.MultipartFile

两种数据类型：

* 功能是一致的，MultipartFile的底层也是Part

* 可以混合使用，比如第一个参数是Part，第二个参数是MultipartFile

注意：

* 先配置文件上传临时目录，默认为null不支持文件上传，文件上传请求Content-Type必须为multipart/form-data

* 同名参数只支持一个文件

* 支持一次传输多个不同参数名的文件

* 通过MultipartFile或Part打开流后，记得关闭，否则上传的临时文件会无法删除，最终导致上传临时目录被撑爆

* 可以直接使用@RequestPart传递普通参数

Springmvc模式下的代码样例：

```java
@PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA)
public String fileUpload(@RequestPart(name = "file1") MultipartFile file1, @RequestPart(name = "file2") Part file2, @RequestPart String param1) {
  ……
}
```

### 配置说明：

| 配置项 | 默认值 | 取值范围 | 含义 |
| :--- | :--- | :--- | :--- |
| servicecomb.uploads.directory | null |  | 上传的临时文件保存在哪个目录，**默认值null表示不支持文件上传** |
| servicecomb.uploads.maxSize | -1 |  | http body的最大允许大小，默认值-1表示无限制 |

## Consumer：

支持以下数据类型：

* java.io.File

* javax.servlet.http.Part

* java.io.InputStream

* org.springframework.core.io.Resource

使用InputStream时，因为是流的方式，此时没有客户端文件名的概念，所以producer获取客户端文件名会得到null

如果既要使用内存数据，又想让producer可以获取客户端文件名，可以使用resource类型，继承org.springframework.core.io.ByteArrayResource，且override getFilename即可。

### 透明RPC代码样例：

```java
interface UploadIntf {
  String upload(File file);
}
```

获得接口引用后，直接调用即可：

```java
String result = uploadIntf.upload(file);
```

### RestTemplate代码样例：

```java
Map<String, Object> map = new HashMap<>();
map.put("file", new FileSystemResource("a file path!"));
map.put("param1", "test");
HttpHeaders headers = new HttpHeaders();
headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
HttpEntity<Map<String, Object>> entry = new HttpEntity<>(map, headers);

String reseult = template.postForObject(
    url,
    entry,
    String.class);
```



