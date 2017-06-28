# Code First

It's the sample of code-first

You can write microservice without swagger contract.


## how to write server code 

Just write codes without swagger contract. When the server is starting, it will build the contract and register the service to  the service center.

```java
@RpcSchema(schemaId = "codeFirst")
public class CodeFirstPojo {
    public int reduce(int a, int b) {
        return a - b;
    }

    public Person sayHello(Person user) {
        user.setName("hello " + user.getName());
        return user;
    }

    public String saySomething(String prefix, Person user) {
        return prefix + " " + user.getName();
    }
    
}
```

## How to run sample

### 1. Start the ServiceComb/Service Center

[how to start the service center](http://servicecomb.io/docs/start-sc/)

### 2.Start the Microservice server

```bash
mvn test -Pserver
```

### 3.Start the Microservice client

```bash
mvn test -Pclient
```