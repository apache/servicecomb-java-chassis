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
| HTTP method | %m  | cs-method | |
| HTTP status | %s  | sc-status | |
| Duration in second | %T  | - | The time taken to serve the request, in seconds |
| Duration in millisecond | %D  | - | The time taken to serve the request, in millisecond |
| Remote hostname | %h  | - | |
| Local hostname | %v  | - |  |
| Local port | %p  | - |  |
| Size of response | %B | - | |
| Size of response | %b | - | In CLF format, i.e. "-" is written if response size is 0 |
| First line of request | %r  | - | |
| URI path | %U | cs-uri-stem | |
| Query string | %q | cs-uri-query | |
| URI path and query string | - | cs-uri | |
| Request protocol | %H | - | |
| Datetime the request was received | %t | - | Write in default format, i.e. pattern is "EEE, dd MMM yyyy HH:mm:ss zzz", Locale is US and Timezone is GMT |
| Configurable datetime the request was received | %{PATTERN\|TIMEZONE\|LOCALE}t | - | Write datetime in specified format pattern, timezone and locale. TIMEZONE and LOCALE can be omitted |
| Request Header | %{VARNAME}i  | - | '-' is written if not found |
| Response header | %{VARNAME}o  | - | '-' is written if not found |
| Cookie | %{VARNAME}C  | - | '-' is written if not found |
| TraceId | - | - | TraceId provided by ServiceComb，log format placeholder is "%SCB-traceId" |

## Access log file settings

Access log will be written in a separate log file named `access.log` located in the same directory with common logs.

Default access log printer is based on Log4j, users can override access log file configuration in their `log4j.properties` file.

***Common access log file configuration items***

| Configuration Item | Default Value | Meaning |
| :----------------- | :------------ | :------ |
| paas.logs.accesslog.dir | ${paas.logs.dir} | access log output directory |
| paas.logs.accesslog.file | access.log | access log file name |