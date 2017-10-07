package io.servicecomb.samples.pojo.provider;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.servicecomb.provider.pojo.RpcSchema;
import io.servicecomb.samples.common.schema.HelloAsync;
import io.servicecomb.samples.common.schema.models.Person;

@RpcSchema(schemaId = "helloasync")
public class HelloAsyncImpl implements HelloAsync {

  private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(8);

  @Override
  public CompletableFuture<Person> sayHi(String name) {
    Person person = new Person();
    CompletableFuture<Person> result = new CompletableFuture<Person>();
    person.setName("Hello" + name);
    executorService.schedule(() -> result.complete(person), 2000, TimeUnit.MILLISECONDS);
    return result;
  }

  @Override
  public CompletableFuture<String> sayHello(Person person) {
    CompletableFuture<String> result = new CompletableFuture<String>();
    executorService.schedule(() -> result.complete("Hello person " + person.getName()), 2000, TimeUnit.MILLISECONDS);
    return result;
  }
}
