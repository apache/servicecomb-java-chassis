package io.servicecomb.foundation.advance.async;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.contrib.javanica.utils.AopUtils;

import io.servicecomb.swagger.invocation.context.ContextUtils;
import io.servicecomb.swagger.invocation.context.InvocationContext;
import io.servicecomb.swagger.invocation.exception.ExceptionFactory;

/**
 * AspectJ aspect to process methods which annotated with {@link AsyncMethod} annotation.
 */
@Aspect
public class AsyncHystrixAspect {

  @Pointcut("@annotation(io.servicecomb.foundation.advance.async.AsyncMethod)")
  public void asyncMethodAnnotationPointcut() {
  }

  @Around("asyncMethodAnnotationPointcut()&&@annotation(asyncMethod)")
  public Object methodsAnnotatedWithAsyncMethod(final ProceedingJoinPoint joinPoint, final AsyncMethod asyncMethod)
      throws Throwable {
    CompletableFuture future = new CompletableFuture();
    InvocationContext invocationContext = ContextUtils.getAndRemoveInvocationContext();
    HystrixCommand.Setter setter = HystrixCommand.Setter
        .withGroupKey(HystrixCommandGroupKey.Factory.asKey(asyncMethod.groupKey())).andCommandKey(
            HystrixCommandKey.Factory.asKey(asyncMethod.commandKey()))
        .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(asyncMethod.threadPoolKey()));
    AsyncHystrixCommand asyncHystrixCommand = new AsyncHystrixCommand(setter, joinPoint, asyncMethod,
        invocationContext);
    asyncHystrixCommand.observe().subscribe(future::complete, future::completeExceptionally);

    ContextUtils.setAsyncFuture(future);
    return null;
  }

  private static final class AsyncHystrixCommand extends HystrixCommand<Object> {
    final ProceedingJoinPoint joinPoint;

    final InvocationContext invocationContext;

    final AsyncMethod asyncMethod;

    protected AsyncHystrixCommand(HystrixCommand.Setter setter, final ProceedingJoinPoint joinPoint,
        final AsyncMethod asyncMethod, final InvocationContext invocationContext) {
      super(setter);
      this.joinPoint = joinPoint;
      this.invocationContext = invocationContext;
      this.asyncMethod = asyncMethod;
    }

    @Override
    protected Object run() throws Exception {
      ContextUtils.setInvocationContext(invocationContext);
      try {
        try {
          return joinPoint.proceed();
        } catch (Throwable throwable) {
          throw ExceptionFactory.convertProducerException(throwable);
        }
      } finally {
        ContextUtils.removeInvocationContext();
      }
    }

    @Override
    protected Object getFallback() {
      if (StringUtils.isNotBlank(asyncMethod.fallbackMethod())) {
        Method fallbackMethod = AopUtils.getMethodFromTarget(joinPoint, asyncMethod.fallbackMethod());
        fallbackMethod.setAccessible(true);
        try {
          return fallbackMethod.invoke(joinPoint.getTarget());
        } catch (IllegalAccessException e) {
          throw ExceptionFactory.convertProducerException(e);
        } catch (InvocationTargetException e) {
          throw ExceptionFactory.convertProducerException(e);
        }
      } else {
        throw ExceptionFactory.createProducerException("No fall back method.");
      }
    }
  }
}
