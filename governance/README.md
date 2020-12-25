# About Deployment Module

Governance module provides an abstraction on how to describe governance instructions for commonly used
different microservice frameworks, like Java Chassis, Go Chassis, Spring Cloud, Dubbo, etc. 

This abstraction is based on traffic marking, here is a configuration example.

```yaml
servicecomb:
  matchGroup:
    matchGroup0: |
      matches:
        - apiPath:
            exact: "/hello"
          name: match0
  rateLimiting:
    rateLimiting0: |
      rules:
        match: matchGroup0.match0
      rate: 1
```

First define a traffic marking rule (match group) `servicecomb.matchGroup.matchGroup0` and detailed matches
specification. Then define governance rules for any marking rule. Marking rule id is `matchGroup0.match0`. 
