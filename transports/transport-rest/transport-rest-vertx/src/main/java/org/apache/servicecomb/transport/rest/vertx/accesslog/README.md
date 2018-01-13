# transport-rest-vertx Access Log

## Enable access log printing

To enable access log printing, you can config access log in microservice.yaml like below:
```yaml
servicecomb:
  accesslog:
    enabled: true
    pattern: "%h - - %t %r %s %B"
```

***Access Log Configurations in microservice.yaml***

| Config Item | Range of Value | Default Value | Meaning |
| :---------- | :------------- | :------------ | :------ |
| servicecomb.accesslog.enabled | true/false | false | print access log if true |
| servicecomb.accesslog.pattern | a string field representing log pattern | "%h - - %t %r %s %B" | see details of ***Currently Supported Elements*** below |

> ***Caution:***
> - all of the configuration items above can be omitted, in this case, default value will take effect.

## Supported log elements

***Currently Supported Elements***

| Element | Apache | W3C | Comment |
| ----|------|------------| --------|
| Method | %m  | cs-method | |
| Status | %s  | sc-status | |
| Duration s | %T  | - |  |
| Duration ms | %D  | - | |
| Remote Host | %h  | - |  |
| Local Host | %v  | - |  |
| Local port | %p  | - |  |
| Bytes Written v1 | %B | - | Zero Bytes written as 0 |
| Bytes Written v2 | %b | - | Zero Bytes written as - |
| First line of request | %r  | - | |
| URI path only | %U | cs-uri-stem | |
| Query only | %q | cs-uri-query | |
| URI path incl query | - | cs-uri | |
| Version / Protocol | %H | - | |
| Datetime Apache | %t | - | Logs by default the request timestamp using format 'EEE, dd MMM yyyy HH:mm:ss zzz', Locale English and Timezone GMT  |
| Datetime Apache Configurable v1 | %{PATTERN}t | - | Specify the format pattern, by default it is used Locale English and Timezone GMT |
| Datetime Apache Configurable v2 | %{PATTERN\|TIMEZONE\|LOCALE}t | - | Specify format pattern, timezone and locale |
| Incoming Headers | %{IDENTIFIER}i  | - | If not found - will be logged |
| Outgoing Response Headers | %{IDENTIFIER}o  | - | If not found - will be logged |
| Cookie | %{IDENTIFIER}c  | - | If not found - will be logged |

## Access log file settings

Access log will be written in a separate log file named `access.log` located in the same directory with common logs.

Default access log printer is based on Log4j, users can override access log file configuration in their `log4j.properties` file.

***Common access log file configuration items***

| Configuration Item | Default Value | Meaning |
| :----------------- | :------------ | :------ |
| paas.logs.accesslog.dir | ${paas.logs.dir} | access log output directory |
| paas.logs.accesslog.file | access.log | access log file name |