# 使用微服务引擎2.0的测试用例

* 参考：https://support.huaweicloud.com/devg-cse/cse_devg_0036.html 安装微服务引擎2.0

* 设置环境变量:
  * PAAS_CSE_SC_ENDPOINT: 注册中心的地址
  * PAAS_CSE_CC_ENDPOINT: 配置中心的地址

* 依次启动 provider、consumer、gateway

* 在配置中心增加如下配置：
* 应用级配置：consumer.yaml。类型为 yaml。

```yaml
cse:
  v2:
    test:
      foo: foo
```

  * 自定义配置：priority1.yaml。label信息： public=default 。类型为 yaml。
```yaml
cse:
  v2:
    test:
      priority: v1
      common: common
```

  * 自定义配置：priority1.yaml。label信息： public=default,extra=default 。类型为 yaml。
```yaml
cse:
  v2:
    test:
      priority: v1
      extra: common
```

  * 应用级配置：priority2.yaml。类型为 yaml。
```yaml
cse:
  v2:
    test:
      priority: v2
```

  * 服务级配置：priority3.yaml，微服务性选择consumer。类型为 yaml。
```yaml
cse:
  v2:
    test:
      priority: v3
```

* 应用级配置：consumerApp.yaml，应用选择demo-java-chassis-cse-v2。类型为 yaml。
```yaml
cse:
  v2:
    test:
      priority1: v1
```

* 服务级配置：consumerService.yaml，微服务性选择consumer。类型为 yaml。
```yaml
cse:
  v2:
    test:
      priority1: v2
```

  * 应用级配置： cse.v2.test.bar: bar 。 类型为 text。

* 执行 tests-client 里面的集成测试用例 （成功）

* 修改
  * priority1.yaml。label信息： public=default 。类型为 yaml。
```yaml
cse:
  v2:
    test:
      priority: v4
```

* 执行 tests-client 里面的集成测试用例 （成功）

* 修改
  * 应用级priority3.yaml。
```yaml
cse:
  v2:
    test:
      priority: v5
```

* 执行 tests-client 里面的集成测试用例 （失败）

* 修改
  * 应用级priority3.yaml。label信息：
```yaml
cse:
  v2:
    test:
      priority: v3
```

* 执行 tests-client 里面的集成测试用例 （成功）
* 修改
  * 应用级consumerApp.yaml。
```yaml
cse:
  v2:
    test:
      priority1: v10
```

* 执行 tests-client 里面的集成测试用例 （成功）
* 修改
  * 服务级配置：consumerService.yaml。
```yaml
cse:
  v2:
    test:
      priority1: v20
```

* 执行 tests-client 里面的集成测试用例 （成功）

* 修改
  * 版本级配置：consumerIns.yaml。
```yaml
cse:
  v2:
    test:
      priority1: v30
```

* 执行 tests-client 里面的集成测试用例 （失败）
