package io.servicecomb.foundation.advance.async;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AsyncMethod {

  /**
   * The command group key is used for grouping together commands such as for reporting,
   * alerting, dashboards or team/library ownership.
   * <p/>
   * default => the runtime class name of annotated method
   *
   * @return group key
   */
  String groupKey() default "";

  /**
   * Hystrix command key.
   * <p/>
   * default => the name of annotated method. for example:
   * <code>
   *     ...
   *     @HystrixCommand
   *     public User getUserById(...)
   *     ...
   *     the command name will be: 'getUserById'
   * </code>
   *
   * @return command key
   */
  String commandKey() default "";

  /**
   * The thread-pool key is used to represent a
   * HystrixThreadPool for monitoring, metrics publishing, caching and other such uses.
   *
   * @return thread pool key
   */
  String threadPoolKey() default "";

  /**
   * Specifies a method to process fallback logic.
   * A fallback method should be defined in the same class where is HystrixCommand.
   * Also a fallback method should have same signature to a method which was invoked as hystrix command.
   * for example:
   * <code>
   *      @HystrixCommand(fallbackMethod = "getByIdFallback")
   *      public String getById(String id) {...}
   *
   *      private String getByIdFallback(String id) {...}
   * </code>
   * Also a fallback method can be annotated with {@link com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand}
   * <p/>
   * default => see {@link com.netflix.hystrix.contrib.javanica.command.GenericCommand#getFallback()}
   *
   * @return method name
   */
  String fallbackMethod() default "";

}
