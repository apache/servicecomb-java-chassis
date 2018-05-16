开发者可以直接使用华为公有云提供的服务中心进行开发工作。使用服务中心，开发者需要注册华为云账号，并在"我的凭据”里面生成AK/SK信息用于访问认证控制。如何接入华为云的详细信息请参考“[华为公有云上部署](/start/deployment-on-cloud.md)”。

本章节重点介绍连接服务中心一些常见的异常及其排查方法。

# \#1

* 异常消息

{"errorCode":"401002","errorMessage":"Request unauthorized","detail":"Invalid request, header is invalid, ak sk or project is empty."}

* 问题原因

AK、SK没有正确设置和携带到请求头里面。

* 排查方法

检查项目中是否依赖如下认证模块。（间接依赖也是可以的，比如依赖cse-solution-service-engine\)

```
<groupId>com.huawei.paas.cse</groupId>
<artifactId>foundation-auth</artifactId>
```

检查microservice.yaml中的ak/sk配置是否正确，accessKey和secretKey是否填写错误，一般secretKey长度比accessKey长。

```
servicecomb:
  credentials:
    accessKey: your access key
    secretKey: your serect key
    akskCustomCipher: default
```

可以登陆华为云，在“我的凭证”里面查询到accessKey信息，secretKey由用户自己保存，无法查询。如果忘记相关凭证，可以删除凭证信息，生成新的凭证信息。

# \#2

* 异常消息

{"errorCode":"401002","errorMessage":"Request unauthorized","detail":"Get service token from iam proxy failed,{\"error\":\"validate ak sk error\"}"}

* 问题原因

AK、SK不正确。

* 排查方法

检查microservice.yaml中的ak/sk配置是否正确。可以登陆华为云，在“我的凭证”里面查询到accessKey信息，secretKey由用户自己保存，无法查询。如果忘记相关凭证，可以删除凭证信息，生成新的凭证信息。

# \#3

* 异常消息

{"errorCode":"401002","errorMessage":"Request unauthorized","detail":"Get service token from iam proxy failed,{\"error\":\"get project token from iam failed. error:http post failed, statuscode: 400\"}"}

* 问题原因

Project名称不正确。

* 排查方法

检查配置项servicecomb.credentials.project的值是否正确，在“我的凭证”里面查询正确的Project名称。如果没有这个配置项，默认会根据服务中心的域名进行判断。当域名也不包含合法的Project名称的时候，需要增加这个配置项，保证其名称是“我的凭证”里面合法的Project名称。

# \#4

* 异常消息

{"errorCode":"400001","errorMessage":"Invalid parameter\(s\)","detail":"Version validate failed, rule: {Length: 64,Length: ^\[a-zA-Z0-9\_

[\\-.\]\*$}](\\-.]*$})

"}

* 问题原因

使用新版本SDK连接服务中心的老版本。

* 排查方法

检查服务中心的版本。可以从华为云官网下载最新版本的服务中心，或者从ServiceComb官网下载最新版本的服务中心。

