## ServiceComb Java Chassis Archetypes
### What's maven archetypes
From http://maven.apache.org/guides/introduction/introduction-to-archetypes.html :

>In short, Archetype is a Maven project templating toolkit. An archetype is defined as an original pattern or model from which all other things of the same kind are made. The name fits as we are trying to provide a system that provides a consistent means of generating Maven projects. Archetype will help authors create Maven project templates for users, and provides users with the means to generate parameterized versions of those project templates.

### What we provide
1. business-service-jaxrs
  
  Archetype for create a common microservice using jaxrs provider.

2. business-service-springmvc

  Archetype for create a common microservice using springmvc provider.
  
3. business-service-pojo

  Archetype for create a common microservice using pojo provider.
  
4. business-service-spring-boot-starter

  Archetype for create a common microservice using spring-boot-starter provider.

### How to build these archetypes
We use **business-service-jaxrs** as an example :

```bash
cd archetypes
mvn install
```

### How to use these archetypes to generate a project
We use **business-service-jaxrs** as an example :
#### Generate via IntelliJ IDEA  
New Project(Module) -> Maven -> Check "Create from archetype" -> Add Archetype... -> fill *GroupId* with value "org.apache.servicecomb.archetypes", fill *ArtifactId* with value "business-service-jaxrs-archetype", fill *Version* with current archetype version -> select this archetype that had listed, do next steps.

#### Generate via Eclipse  
New Project(Module) -> Maven -> Next -> Add Archetype... -> fill *GroupId* with value "org.apache.servicecomb.archetypes", fill *ArtifactId* with value "business-service-jaxrs-archetype", fill *Version* with current archetype version -> select this archetype that had listed, do next steps.

#### Generate via command

```bash
mvn archetype:generate -DarchetypeGroupId=org.apache.servicecomb.archetypes -DarchetypeArtifactId=business-service-jaxrs-archetype -DarchetypeVersion=${archetype-version}
```

In console Interactive mode, input your GroupId, ArtifactId and Version of new project(module), after a while the new project will be generated.

*Notice: We will publish these archetypes to maven center repository since 1.0.0-m2, if you would like to use an archetype from an unreleased version, must use `archetypeRepository` option in the version 2.4 of archetype-plugin in order to set maven repository to apache snapshot groups: *

```bash
mvn org.apache.maven.plugins:maven-archetype-plugin:2.4:generate -DarchetypeGroupId=org.apache.servicecomb.archetypes -DarchetypeArtifactId=business-service-jaxrs-archetype -DarchetypeVersion=1.0.0-SNAPSHOT -DarchetypeRepository=https://repository.apache.org/content/groups/snapshots-group
```