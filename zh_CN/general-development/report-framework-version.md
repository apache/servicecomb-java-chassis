## 概念阐述

为方便治理，使用ServiceComb进行开发，会将当前使用的ServiceComb版本号上报至服务中心，并且支持其他框架集成ServiceComb时上报其他框架的版本号。

## 示例代码


步骤1 首先实现开源框架ServiceComb的Versions接口，实现该接口下的loadVersion方法，即可将版本名称和版本号作为键值对返回

```
public class MyVersion implements Versions{
  @Override
  public Map<String, String> loadVersion() {
    Map<String, String> map = new HashMap<>();
    map.put("My", this.getClass().getPackage().getImplementationVersion());
    return map;
  }
}
```

步骤2 为了使用SPI机制让该返回对象被ServiceComb读取到，需要在META-INF中增加services文件夹，并在其中增加一个文件，以所实现接口x.x.x.Versions\(带包名\)为名，以具体实现类x.x.x.CseVersion\(带包名\)为内容

当服务注册到ServiceCenter时，会携带所有版本号信息

```
{
  "serviceId": "xxx",
  "appId": "xxx",
  "registerBy": "SDK",
  "framework": {
    "name": "servicecomb-java-chassis",
    "version": "My:x.x.x;ServiceComb:x.x.x"
  } 
}
```

* 备注

上报的版本号可以自定义，也可以从pom或jar包的MANIFEST.MF里读取，如果使用.class.getPackage\(\).getImplementationVersion\(\)从MANIFEST.MF获取版本号，则需要在pom文件中把maven-jar-plugin的archive元素addDefaultImplementationEntries和addDefaultSpecificationEntries设置为true

```
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-jar-plugin</artifactId>
  <configuration>
    <archive>
      <manifest>
        <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
        <addDefaultSpecificationEntries>true</addDefaultSpecificationEntries>
      </manifest>
    </archive>
  </configuration>
</plugin>
```





