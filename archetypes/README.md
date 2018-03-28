## ServiceComb Java Chassis Archetypes
### What's maven archetypes
From http://maven.apache.org/guides/introduction/introduction-to-archetypes.html :

"*In short, Archetype is a Maven project templating toolkit. An archetype is defined as an original pattern or model from which all other things of the same kind are made. The name fits as we are trying to provide a system that provides a consistent means of generating Maven projects. Archetype will help authors create Maven project templates for users, and provides users with the means to generate parameterized versions of those project templates.*"

"*Using archetypes provides a great way to enable developers quickly in a way consistent with best practices employed by your project or organization. Within the Maven project, we use archetypes to try and get our users up and running as quickly as possible by providing a sample project that demonstrates many of the features of Maven, while introducing new users to the best practices employed by Maven. In a matter of seconds, a new user can have a working Maven project to use as a jumping board for investigating more of the features in Maven. We have also tried to make the Archetype mechanism additive, and by that we mean allowing portions of a project to be captured in an archetype so that pieces or aspects of a project can be added to existing projects. A good example of this is the Maven site archetype. If, for example, you have used the quick start archetype to generate a working project, you can then quickly create a site for that project by using the site archetype within that existing project. You can do anything like this with archetypes.*"

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