## Release Notes

## Apache ServiceComb Java-Chassis (Incubating) 1.0.0-m1

### Major improvements:

 - Java Chassis can now use Apollo as configuration center. Users can now
change configurations like load balancing policy and those changes will
come into effect on the fly.
See [here](http://servicecomb.incubator.apache.org/users/dynamic-config/) for more
details.

 - Metrics was re-factored. We now uses events for collecting invocation data
instead of Hystrix. This reduces the performance penalty of computing
metrics.
Metrics can now be fetched via '/metrics' using HTTP.
See [here](http://servicecomb.incubator.apache.org/users/metrics-in-1.0.0-m1/) for
more details.

### Other Noticeable Changes:

- The Java Chassis libraries are now under group "org.apache.servicecomb".
- We provide out of the box metrics support now. Prometheus is supported.
- Configuration center was re-factored and moved out from foundation.
Support for Apollo was added.
- Users can now use Object type for calling services.
- Users can now use Generics for calling services.
- Better integration with Spring MVC.
- Upgraded to zipkin2 internally, Java Chassis can now work with zipkin
server v1 and v2.
- We are in the process of supporting reactive programming. Pojo consumer
and provider now supports CompletableFuture.

### For more detailed information please checkout [here](https://issues.apache.org/jira/secure/ReleaseNote.jspa?projectId=12321626&version=12342351)
