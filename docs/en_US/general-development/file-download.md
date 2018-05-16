文件下载，当前只在vertx rest通道中可用，servlet rest的支持后续会增加。

# 一、Producer

## 1.下载普通文件

```
return new File(......);
```

## 2.下载临时文件

本场景下，需要根据请求参数动态创建临时文件，下载完成后，需要将临时文件删除

```
return new FilePart(file).setDeleteAfterFinished(true);
```

## 3.下载org.springframework.core.io.Resource

因为resource不一定表示文件下载，所以需要通过swagger annotation（@ApiResponse）标识这是一个文件下载场景

以ByteArrayResource为例说明：

```
@GetMapping(path = "/resource")
@ApiResponses({
  @ApiResponse(code = 200, response = File.class, message = "")
})
public Resource resource() {
  ……
  return new ByteArrayResource(bytes) {
    @Override
    public String getFilename() {
      return "resource.txt";
    }
  };
}
```

上例中，因为ByteArrayResource没有文件名的概念，所以需要实现Resource的getFilename方法，也可以通过ResponseEntity进行包装：

```
@GetMapping(path = "/resource")
@ApiResponses({
  @ApiResponse(code = 200, response = File.class, message = "")
})
public ResponseEntity<Resource> resource() {
  ……
  return ResponseEntity
      .ok()
      .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename= resource.txt")
      .body(resource);
}
```

## 4.下载InputStream

因为InputStream不一定表示文件下载，所以需要通过swagger annotation（@ApiResponse）标识这是一个文件下载场景

有的场景下，资源并不保存在本地，比如保存在OBS云服务中，而OBS资源是以InputStream方式输出的

```
return ResponseEntity
    .ok()
    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename= resource.txt")
    .body(stream);
```

在下载完成后，ServiceComb会自动关闭stream，开发人员不必再关注

## 5.文件类型判定

只要没有通过ResponseEntity直接设置HttpHeaders.CONTENT\_TYPE，ServiceComb都会尝试通过File、Part、Resource中的文件名后缀进行自动判定。

ServiceComb使用java的mime type机制进行文件类型判定，如果业务场景中的文件后缀无法被识别，ServiceComb会默认处理为application/octet-stream

如果这不满足要求，假设文件后缀为xyz，期望文件类型为application/file-xyz，以下方式任选一种均可解决：

### 1）通过Java的mime type机制扩展

在META-INF目录下，创建mime.types文件，其内容为：

```
application/file-xyz xyz
```

### 2）在业务代码中通过Part指定

```
return new FilePart(null, file).contentType("application/file-xyz");
```

### 3）在业务代码中通过ResponseEntity指定

```
return ResponseEntity
    .ok()
    .header(HttpHeaders.CONTENT_TYPE, "application/file-xyz")
    .body(……);
```

## 6.文件名

只要没有通过ResponseEntity直接设置HttpHeaders.CONTENT\_DISPOSITION，ServiceComb都会尝试通过File、Part、Resource中的文件名生成HttpHeaders.CONTENT\_DISPOSITION，假设文件名为file.txt，则生成的数据如下：

```
Content-Disposition: attachment;filename=file.txt;filename*=utf-8’’file.txt
```

不仅仅生成filename，还生成了filename\*，这是因为如果文件名中出现了中文、空格，并且filename正确地做了encode，ie、chrome都没有问题，但是firefox直接将encode后的串当作文件名直接使用了。firefox按照[https://tools.ietf.org/html/rtf6266](https://tools.ietf.org/html/rtf6266)，只对filename\*进行解码。

如果业务代码中直接设置Content-Disposition，需要自行处理多浏览器支持的问题。

# 二、Consumer

消费端统一使用org.apache.servicecomb.foundation.vertx.http.ReadStreamPart处理文件下载。

## 1.透明RPC

```
public interface ……{
  ReadStreamPart download1(……);
  ReadStreamPart download2(……);
}
```

## 2.RestTemplate

以get为例：

```
ReadStreamPart part = restTemplate.getForObject(url, ReadStreamPart.class);
```

## 3.从ReadStreamPart读取数据

ReadStreamPart提供了一系列方法，将数据流保存为本地数据：

```
org.apache.servicecomb.foundation.vertx.http.ReadStreamPart.saveAsBytes()
org.apache.servicecomb.foundation.vertx.http.ReadStreamPart.saveAsString()
org.apache.servicecomb.foundation.vertx.http.ReadStreamPart.saveToFile(String)
org.apache.servicecomb.foundation.vertx.http.ReadStreamPart.saveToFile(File, OpenOptions)
```

注意：

* 在得到ReadStreamPart实例时，并没有完成文件内容的下载，调用save系列方法才开始真正从网络上读取文件数据。

* 如果使用saveAsBytes、saveAsString，数据是直接保存在内存中的，如果下载的文件很大，会内存撑爆的风险。

* save系列方法，返回的都是CompletableFuture对象：

  * 如果要阻塞等待下载完成，通过future.get\(\)即可
  * 如果通过future.whenComplete进行异步回调处理，要注意回调是发生在网络线程中的，此时需要遵守reactive的线程规则。



