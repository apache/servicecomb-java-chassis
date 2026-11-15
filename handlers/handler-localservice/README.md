<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

# handler-localservice

Local service instance priority scheduling for Apache ServiceComb Java Chassis (3.x).

## Overview

The `handler-localservice` module provides local service instance priority scheduling to optimize
inter-service communication when services are deployed on the same host. This reduces network
overhead and improves performance for co-located services.

### Key Features

- **Provider Side**: HTTP server listening on `127.0.0.1` when the external listen address does not
  already cover loopback (see [Local Listener](#local-listener))
- **Consumer Side**: Load balance filter that prioritizes local instances while keeping global
  traffic distribution balanced
- **Zero Intrusion**: implemented purely with SPI extensions (`BootListener`,
  `ServerListFilterExt`, `VertxHttpDispatcher`), no framework code is modified

### How It Works

```
┌─────────────────────────────────────────────────────────────┐
│                        Service Instance                      │
│  ┌─────────────────┐           ┌─────────────────────────┐  │
│  │  Main HTTP Port │           │   Local HTTP Port       │  │
│  │  (specific IP)  │           │   (127.0.0.1 only)      │  │
│  │    :8080        │           │      :8080              │  │
│  └────────▲────────┘           └───────────▲─────────────┘  │
│           │                                │                 │
│           │         Local Request          │                 │
│           │    (from same host)            │                 │
│           │                                │                 │
│  ┌────────┴────────────────────────────────┴──────────────┐ │
│  │                    Consumer Service                     │ │
│  │   LocalServicePriorityFilter → Local Instance First    │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Global Load Balancing Algorithm

The `LocalServicePriorityFilter` uses a topology-aware weighted random selection algorithm that
balances local preference with global traffic distribution.

Constraint: each host has at most 1 Consumer instance and at most 1 Provider instance.

Given `C` = number of Consumer instances, `P` = number of Provider instances of the target service:

| Scenario | Condition | P1 (local) | P2 (colocated with other consumers) | P3 (non-colocated) |
|----------|-----------|------------|--------------------------------------|--------------------|
| `C >= P` | P1 exists | `1.0` | 0 | 0 |
| `C >= P` | no P1     | 0          | `1/P - 1/C` each | `1/P` each |
| `C < P`  | P1 exists | `C/P`      | 0 | `(1 - C/P) / P3.count` each |
| `C < P`  | no P1     | 0          | 0 | `1/P` each |

Weights are fractions of this Consumer's traffic. On each request a weighted random choice is made:

- P1 is selected → the endpoint is rewritten to `rest://127.0.0.1:port` (always plain HTTP; the
  provider side guarantees the loopback port serves plain HTTP whenever P1 is advertised, see
  [Automatic Correction](#automatic-correction-of-supportlocalservice))
- otherwise → a remote instance from P2/P3 is selected

### Weight Cache

Weights are cached per provider service (`appId:serviceName`) and recalculated when:

- the provider instance list changes (instance id set validation), or
- the service topology changes. A `DiscoveryManager.InstanceChangeListener` invalidates
  provider service caches on change events, and **clears all caches when the consumer service
  itself changes** (the weight distribution depends on the full consumer topology).

## Local Listener

`LocalServiceBootListener` starts an additional HTTP server on `127.0.0.1` reusing the Router
captured from `RestServerVerticle` by `RouterHolderDispatcher`.

When the REST listen address is wildcard bound (e.g. `0.0.0.0:8080`), plain HTTP or TLS, connections
to `127.0.0.1:8080` are already served by the main server, so **no additional listener is started**.
For a wildcard TLS binding the loopback port is TLS as well; the provider side auto-corrects the
declared `supportLocalService` property in that case (see
[Automatic Correction](#automatic-correction-of-supportlocalservice)). Local listeners are only
started for specific-IP listen addresses, and they always serve plain HTTP.

If the Router can not be captured, the local listener is skipped with a warning; it is never
rebuilt from scratch, to avoid duplicating `RestServerVerticle` mount logic.

## Automatic Correction of supportLocalService

The consumer side rewrites local (P1) endpoints to plain HTTP `rest://127.0.0.1:port`, so a provider
may only advertise `supportLocalService=true` when its loopback port serves plain HTTP.

When the REST address does **not** guarantee plain HTTP on `127.0.0.1` — a TLS wildcard binding,
or the local listener could not start — and the instance properties declare
`supportLocalService=true`, the listener automatically corrects the property to `false` before
registration publishes it. The correction runs at the `AFTER_TRANSPORT` boot event, i.e. after
`RegistrationManager.init()` (declared properties loaded) and before `RegistrationManager.run()`
(registration published), so consumers only ever discover the corrected value.

This makes a mis-declared TLS provider fail safe: local priority is simply not used, instead of
consumers calling a TLS port with plain HTTP.

## Requirements

- **Transport**: REST over Vert.x only (Highway protocol not supported)
- **Java Chassis**: 3.x
- **Spring Boot starter integration** (BootListener instances are collected via Spring injection)

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>org.apache.servicecomb</groupId>
  <artifactId>handler-localservice</artifactId>
</dependency>
```

## Configuration

| Key | Default | Description |
|-----|---------|-------------|
| `servicecomb.localservice.enabled` | `false` | Enable provider local listener (also controls the Spring configuration) |
| `servicecomb.loadbalance.filter.localService.enabled` | `true` | Enable consumer local priority filter |

### Instance Properties

Mark instances that support local communication, and (recommended) declare the host identity:

```yaml
servicecomb:
  rest:
    address: 0.0.0.0:8080
  service:
    properties:
      supportLocalService: "true"
      # Java Chassis 3.x has no hostName field on instances.
      # Set the same host identity on every instance of a host to make
      # colocated detection reliable.
      hostName: "host-1"
```

> **Important**: configure these keys under `servicecomb.service.properties.*`, not under
> `servicecomb.instance.properties.*`. In Java Chassis 3.x, the registration implementations
> (service-center, local, etcd, consul, zookeeper) fill the published instance properties from
> the service level properties (`BootStrapProperties.readServiceProperties`); only the built-in
> lightweight registry reads `servicecomb.instance.properties.*` — that is why the
> `demo-zeroconfig-registry` (which uses the built-in lightweight registry) declares them at
> the instance level. Using the service level keys works uniformly across all other registry
> implementations; when in doubt, set both.

Host identity resolution order in `LocalServicePriorityFilter`:

1. instance property `hostName`
2. host parsed from the first endpoint

All instances of one service must use the same domain of identifiers (all hostnames or all IPs).

## Consumer Configuration

```yaml
servicecomb:
  loadbalance:
    filter:
      localService:
        enabled: true
```

The `LocalServicePriorityFilter` runs with order 250, after the zone-aware discovery filters, and
returns a single selected server to the load balancer rule.

## Failure Behavior

When a provider claims `supportLocalService` but its local listener is not reachable, calls to
`127.0.0.1` fail until the framework isolation mechanism removes the instance. Make sure the
local listener is healthy before advertising the instance property.

The loopback plain HTTP precondition, however, is enforced automatically: a provider that cannot
serve plain HTTP on `127.0.0.1` (e.g. TLS wildcard binding, local listener failed to start) never
publishes `supportLocalService=true` — it is corrected to `false` before registration (see
[Automatic Correction](#automatic-correction-of-supportlocalservice)). Consumers then simply
skip the local priority for that provider instead of calling a non plain HTTP port with plain HTTP.

## Local Address Detection

`LocalAddressCache` distinguishes two semantics:

- **Instance matching**: loopback addresses (127.0.0.1, ::1, localhost) are NOT local, because
  they exist on all machines. Only real interface addresses identify a local instance.
- **Loopback detection**: `isLoopbackAddress` explicitly identifies loopback addresses.

## SPI Registration

```
META-INF/services/org.apache.servicecomb.loadbalance.ServerListFilterExt
META-INF/services/org.apache.servicecomb.transport.rest.vertx.VertxHttpDispatcher
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

Note: in Java Chassis 3.x `BootListener` instances are Spring beans, registered by
`LocalServiceConfiguration` through the auto-configuration file above. There is no
`META-INF/services` registration for `BootListener`.

## Limitations

- **REST Only**: only supports REST over Vert.x transport, not Highway protocol
- **Same Host**: local optimization only works when consumer and provider are on the same host
- **Topology assumption**: at most one consumer and one provider instance per host

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](../../LICENSE) for details.
