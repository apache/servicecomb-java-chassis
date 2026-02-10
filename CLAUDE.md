# CLAUDE.md - AI Assistant Guide for ServiceComb Java Chassis

## Project Overview

Apache ServiceComb Java Chassis is a microservices SDK for rapid development of microservices in Java, providing service registration, service discovery, dynamic routing, and service management. Current version is **3.3.0-SNAPSHOT** (Java Chassis 3).

- **GroupId:** `org.apache.servicecomb`
- **ArtifactId:** `java-chassis`
- **License:** Apache License 2.0
- **Java:** OpenJDK 17 required
- **Parent POM:** `org.apache:apache:34`

## Build Commands

```bash
# Standard build (compile + unit tests)
mvn clean install

# Quick build (skip checks)
mvn clean install -DskipTests -Dcheckstyle.skip -Dspotbugs.skip=true

# Build with integration tests (requires Docker)
mvn clean install -Pdocker -Pit

# Build with code coverage
mvn clean install -Pjacoco -Pcoverage

# Full CI build (matches GitHub Actions)
mvn clean install -Dcheckstyle.skip -Dspotbugs.skip=true -B -Pdocker -Pjacoco -Pit -Pcoverage

# Run only checkstyle
mvn -B -Pit install -DskipTests -Dspotbugs.skip=true checkstyle:check

# Run only SpotBugs
mvn -B -Pit -DskipTests clean verify spotbugs:spotbugs

# Build a single module (e.g., core)
mvn clean install -pl core -am

# OWASP dependency security check (slow, manual only)
mvn verify -Powasp-dependency-check
```

**Required tools:** JDK 17 (Temurin), Maven 3.9.9+

## Module Structure

```
java-chassis/
├── dependencies/          # Centralized dependency version management (BOM)
├── parents/               # Parent POM with test dependency defaults
├── foundations/           # Foundation libraries (9 modules)
│   ├── foundation-spi        # Service Provider Interface framework
│   ├── foundation-vertx       # Vert.x integration
│   ├── foundation-common      # Common utilities (Jackson, Spring, Guava)
│   ├── foundation-config      # Configuration management
│   ├── foundation-metrics     # Metrics abstractions
│   ├── foundation-ssl         # SSL/TLS handling
│   ├── foundation-test-scaffolding  # Test utilities
│   ├── foundation-protobuf    # Protocol Buffers support
│   └── foundation-registry    # Service registry abstractions
├── core/                  # Core runtime engine (invocation, filters, bootstrap)
├── swagger/               # OpenAPI spec generation and invocation
├── providers/             # Programming model implementations
│   ├── provider-springmvc     # Spring MVC @RequestMapping
│   ├── provider-pojo          # Plain POJO with @ServiceSchemaInterface
│   ├── provider-jaxrs         # JAX-RS @Path
│   └── provider-rest-common   # Shared REST infrastructure
├── transports/            # Network transport protocols
│   ├── transport-rest         # HTTP/REST (Vert.x-based)
│   ├── transport-highway      # Custom high-performance RPC protocol
│   └── transport-common       # Shared transport infrastructure
├── handlers/              # Request/response processing pipeline
│   ├── handler-loadbalance        # Client-side load balancing
│   ├── handler-governance         # Service governance (routing, retries)
│   ├── handler-fault-injection    # Chaos engineering
│   ├── handler-flowcontrol-qps    # Rate limiting
│   ├── handler-router             # Dynamic routing
│   ├── handler-publickey-auth     # Public key authentication
│   └── handler-tracing-zipkin     # Zipkin distributed tracing
├── service-registry/      # Pluggable registry backends (8 implementations)
│   ├── registry-service-center    # Apache ServiceComb Service Center
│   ├── registry-consul            # HashiCorp Consul
│   ├── registry-zookeeper         # Apache ZooKeeper
│   ├── registry-nacos             # Alibaba Nacos
│   ├── registry-etcd              # ETCD
│   ├── registry-lightweight       # In-memory lightweight
│   ├── registry-local             # Single-JVM local
│   └── registry-zero-config       # Zero-configuration (multicast)
├── dynamic-config/        # Dynamic configuration sources (7 implementations)
│   ├── config-apollo, config-nacos, config-consul
│   ├── config-zookeeper, config-etcd, config-kie, config-cc
├── spring-boot/           # Spring Boot starters
│   └── spring-boot-starters/
│       ├── java-chassis-spring-boot-starter-standalone
│       └── java-chassis-spring-boot-starter-servlet
├── edge/                  # Edge service / API gateway
├── metrics/               # Metrics (Micrometer integration)
├── tracing/               # Distributed tracing (Zipkin/Brave)
├── governance/            # Service governance policies
├── solutions/             # Solution templates
├── clients/               # HTTP clients for registries and config centers
├── common/                # Common modules (access-log, protobuf, rest)
├── huawei-cloud/          # Huawei Cloud integrations
├── demo/                  # Integration test demos (19+ projects, activated with -Pit)
├── coverage-reports/      # Aggregated coverage reports
├── ci/                    # CI configuration (checkstyle, spotbugs configs)
├── etc/                   # IDE code style configs (Google Java Style)
└── distribution/          # Release packaging (activated with -Prelease)
```

### Dependency Flow

```
dependencies/ (BOM - version management)
  └─> parents/default/ (inherits BOM, adds test deps)
       └─> All modules inherit from parents/default
            foundations → core → providers/transports/handlers → spring-boot starters
```

## Key Dependencies

| Dependency | Version | Purpose |
|---|---|---|
| Spring Boot | 3.3.5 | Application framework |
| Spring Framework | 6.1.10 | Core framework |
| Vert.x | 4.5.14 | Async/event-driven transport |
| Jackson | 2.18.3 | JSON/XML serialization |
| Netty | 4.1.119.Final | Network I/O |
| Protocol Buffers | 3.23.4 | Binary serialization |
| Micrometer | 1.14.6 | Metrics |
| Brave | 6.2.0 | Distributed tracing |
| Resilience4j | 1.7.0 | Fault tolerance |

## Testing

### Frameworks
- **JUnit 5** (Jupiter 5.12.2) - primary; JUnit 4 (4.13.2) supported via Vintage engine
- **Mockito** 5.15.2 - mocking
- **AssertJ** 3.27.3 - fluent assertions
- **Awaitility** 4.3.0 - async testing
- **JMockit** 1.34 - advanced mocking (used in some modules)

### Conventions
- Unit tests: `src/test/java/` with `*Test.java` naming
- Integration tests: `*IT.java` suffix, run with Failsafe plugin
- Integration test demos live in `demo/` module (activated with `-Pit` profile)
- Docker-based integration tests require `-Pdocker` profile
- Surefire runs with 2 forks, alphabetical ordering

### Running Tests
```bash
# Unit tests only
mvn test

# Unit + integration tests
mvn verify -Pit

# Skip tests entirely
mvn install -DskipTests

# Single test class
mvn test -pl core -Dtest=SomeTest

# Ignore test failures
mvn install -Dmaven.test.failure.ignore=true
```

## Code Style & Quality

### Style Rules
- **Google Java Style** - IDE configs in `etc/` (Eclipse and IntelliJ formatters)
- **Checkstyle** config: `ci/checkstyle/checkstyle.xml`
- **SpotBugs** exclusions: `ci/spotbugs/exclude.xml`

### Key Checkstyle Rules
- No tab characters (spaces only)
- No trailing whitespace
- Newline at end of file
- No star imports (`import java.util.*` forbidden)
- No unused or redundant imports
- Braces required around all `if/else/for/while/do` blocks
- `else` on same line as closing brace: `} else {`
- Local variables: camelCase (`^[a-z][a-zA-Z0-9]*$`)
- Long literals use uppercase L (e.g., `100L` not `100l`)
- Switch statements must have default clause
- Whitespace required around operators and keywords
- `Throwables.propagate()` is banned (deprecated)
- Suppression: use `// CHECKSTYLE.OFF` / `// CHECKSTYLE.ON` comments

### Package Naming
- Root package: `org.apache.servicecomb`
- Sub-packages follow module structure:
  - `org.apache.servicecomb.core.*` - Core runtime
  - `org.apache.servicecomb.foundation.*` - Foundation libraries
  - `org.apache.servicecomb.provider.*` - Providers (springmvc, pojo, jaxrs)
  - `org.apache.servicecomb.transport.*` - Transports
  - `org.apache.servicecomb.registry.*` - Registry implementations
  - `org.apache.servicecomb.demo.*` - Demo applications

## CI/CD (GitHub Actions)

Workflows in `.github/workflows/`:

| Workflow | Trigger | What it does |
|---|---|---|
| `maven.yml` | Push/PR to master | Full build + tests + coverage (60min timeout) |
| `checkstyle.yml` | PR to master | Checkstyle validation |
| `spotbugs.yml` | PR to master | SpotBugs static analysis |
| `rat_check.yml` | PR to master | Apache license header verification |
| `linelint.yml` | PR to master | Line format checking |
| `typo_check.yml` | PR to master | Typo detection (`.typos.toml` config) |

## Maven Profiles

| Profile | Purpose |
|---|---|
| `-Pit` | Include `demo/` module for integration tests |
| `-Pdocker` | Enable Docker-based integration tests |
| `-Pjacoco` | Activate JaCoCo code coverage |
| `-Pcoverage` | Include coverage-reports module |
| `-Prelease` | Release build (GPG signing, javadoc, sources, distribution) |
| `-Pdocker-machine` | Docker Machine support |
| `-Powasp-dependency-check` | OWASP CVE scanning (slow, manual) |

## PR/Contribution Guidelines

- File a [JIRA issue](https://issues.apache.org/jira/browse/SCB) before starting work (except trivial typos)
- PR title format: `[SCB-XXX] Fixes bug in <description>`
- Each commit should have a meaningful subject and body
- Run `mvn clean install -Pit` before submitting
- All files must include Apache License 2.0 header (enforced by RAT)
- Large contributions require Apache ICLA

## Key Architecture Concepts

- **SPI-based plugin architecture**: Registries, config sources, transports, and handlers are all pluggable via `foundation-spi`
- **Filter chain**: Request/response processing uses a filter pipeline in `core`
- **Multiple programming models**: Same service can be exposed via Spring MVC annotations, JAX-RS annotations, or plain POJO interfaces
- **Dual transport**: REST (HTTP via Vert.x) and Highway (custom high-performance binary RPC)
- **Invocation model**: `org.apache.servicecomb.core.invocation` handles the full request lifecycle
- **Schema-driven**: Services are defined by OpenAPI 3.0 schemas; `swagger/` modules handle generation and invocation

## Common Pitfalls

- Java 17 module system requires `--add-opens` flags for tests (already configured in surefire)
- The `demo/` module is only included with `-Pit` profile
- Docker must be running for integration tests (`-Pdocker`)
- Checkstyle and SpotBugs run by default in the build; skip with `-Dcheckstyle.skip -Dspotbugs.skip=true` for faster iteration
- The `dependencies/default/pom.xml` manages all third-party versions; do not declare versions in individual module POMs
