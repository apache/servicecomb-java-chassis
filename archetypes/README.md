## ServiceComb Java Chassis Archetypes
### What's maven archetypes
From http://maven.apache.org/guides/introduction/introduction-to-archetypes.html :

>In short, Archetype is a Maven project templating toolkit. An archetype is defined as an original pattern or model from which all other things of the same kind are made. The name fits as we are trying to provide a system that provides a consistent means of generating Maven projects. Archetype will help authors create Maven project templates for users, and provides users with the means to generate parameterized versions of those project templates.

### What we provide
1. business-service-jaxrs  
  Archetype for create a common microservice using jaxrs provider.

more coming soon.. 

### How to build these archetypes
We use **business-service-jaxrs** as an example :
```bash
cd archetypes/business-service-jaxrs
mvn archetype:create-from-project
#Wait until successed, archetype will be generated at target/generated-sources/archetype
cd target/generated-sources/archetype
#Install archetype to your local maven repository
mvn install
```
*Notice: The archetype ArtifactId of business-service-jaxrs is business-service-jaxrs-**archetype.***

We will publish these archetypes to maven center repository since 1.0.0-m2.

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