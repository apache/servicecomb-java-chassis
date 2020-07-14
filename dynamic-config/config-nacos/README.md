#config-nacos
How to use:
1.Download base model from http://start.servicecomb.io and add maven dependence:

<dependency>
    <groupId>org.apache.servicecomb</groupId>
    <artifactId>config-nacos</artifactId>
    <version>${project.version}</version>
</dependency>

2.Start nacos console model(git url:https://github.com/alibaba/nacos) ,both account and password is "nacos",after that add properties(example):
Data ID: example
Group: DEFAULT_GROUP
JSON:
    {
    "nacos":"666"
    }

3.In base model,add info in microservice.yaml:
servicecomb:
  nacos:
    serverAddr: 127.0.0.1:8848
    group: DEFAULT_GROUP
    dataId: example.yaml
    namespace: 6a39260c-b834-456c-b52b-2981fb437c59

4.Then add blow code and start base model,you will get properties(If properties on nacos has changed, you can also get new value):
@RestSchema(schemaId = "nacos")
@RequestMapping(path = "/")
public class NacosImpl {
    /**
    **get properties from nacos
    */
    @GetMapping(path = "/config")
    @Responsebody
    public String config() throws Exception{
        final String config = DynamicPropertyFactory.getInstance()
                        .getStringProperty("nacos","").getValue();
        return JSON.toJSONString(config);
    }
}