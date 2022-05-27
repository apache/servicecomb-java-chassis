# 使用微服务引擎专业版的测试用例

* 首先登陆华为云华南区，获取 AK/SK。 测试机器设置环境变量： CREDENTIALS_AK、CREDENTIALS_SK。
* 在配置中心增加如下配置：
  * consumer.yaml

```yaml
cse:
  v1:
    test:
      foo: foo
      dynamicString: a,b
      dynamicArray:
        - m
        - n
```

* 依次启动 provider、provider-canary、consumer、gateway
* 在配置中心增加如下配置：
  * cse.v1.test.bar: bar

* 执行 tests-client 里面的集成测试用例
