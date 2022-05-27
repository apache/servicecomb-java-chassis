This demo is an integration test for testing 

```yaml
servicecomb:
  service:
    registry:
      registerUrlPrefix: true
```

When this configuration is enabled, web context path is added to swagger, and consumer can 
invoke with context path

```yaml
template.getForObject(
  "cse://demo-register-url-prefix-server/hellodemo/register/url/prefix/getName?name=2",
   String.class)
```

This feature is not recommended for use by default, but for some backward capabilities.
