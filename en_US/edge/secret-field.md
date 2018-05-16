## 场景描述

由于HTTP协议的非安全性，在网络中传输的数据能轻易被各种抓包工具监听。在实际应用中，业务对应用或服务间传输的敏感数据有较高的安全要求，这类数据需要特别的加密保护（业务不同对算法要求不同），这样即使内容被截获，也可以保护其中的敏感数据不被轻易获取。

## 解决方法

服务间的通信离开不序列化和反序列化，对于上述的场景，使用jackson类库提供的 @JsonSerialize 和 @JsonDeserialize 注解功能，对敏感数据定制序列化和反序列化方法，并在定制化的方法中实现加解密功能。

注解描述参考：在 [https://github.com/FasterXML/jackson-databind/wiki](https://github.com/FasterXML/jackson-databind/wiki) 中查找对应版本的Javadocs

## 示例

1.对 Person 对象中的 name 属性，通过注解设定使用特定的序列化和反序列化方法。注：此处演示如何使用，不涉及加解密相关。

```
public class Person {
  private int usrId;

  //指定数据 name 使用特定的序列化和反序列化方法
  @JsonSerialize(using = SecretSerialize.class)
  @JsonDeserialize(using = SecretDeserialize.class)
  private String name;

  public int getUsrId() {
    return usrId;
  }

  public void setUsrId(int usrId) {
    this.usrId = usrId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "Person{" +
        "usrId=" + usrId +
        ", name='" + name + '\'' +
        '}';
  }
}
```

2.定义 SecretSerialize 类 和 SecretDeserialize 类，并重写其方法

```
public class SecretSerialize extends JsonSerializer<String> {

  //重写 name 的序列化方法，可在此实现用户定制的加解密或其他操作
  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException, JsonProcessingException {
    //在数据 name 后增加4个特定字符
    value = value + " &#@";

    //执行序列化操作
    gen.writeString(value);
  }
}

public class SecretDeserialize extends JsonDeserializer<String> {

  //重写 name 的反序列化方法，与serialize序列化方法匹配，按用户定制的规则获取真实数据
  @Override
  public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    //获取反序列化数据，除去4个特定字符，获取真实的 name
    String value = p.getValueAsString();
    value = value.substring(0, value.length() - 4);
    return value;
  }
}
```



