package io.servicecomb.loadbalance;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

import com.google.common.collect.Lists;
import com.netflix.client.RetryHandler;
import com.netflix.client.Utils;

public class LoadBalanceRetryHandler implements RetryHandler {

  private final int retrySameServer;
  private final int retryNextServer;
  private final boolean retryEnabled;

  private List<Class<? extends Throwable>> retryOnSameExceptions = Lists
      .newArrayList(new Class[]{SocketTimeoutException.class, ConnectException.class});

  private List<Class<? extends Throwable>> circuitRelated = Lists
      .newArrayList(new Class[]{SocketException.class, SocketTimeoutException.class});

  LoadBalanceRetryHandler(int retrySameServer, int retryNextServer, boolean retryEnabled) {
    this.retrySameServer = retrySameServer;
    this.retryNextServer = retryNextServer;
    this.retryEnabled = retryEnabled;
  }

  public void setRetryOnSameExceptions(List<Class<? extends Throwable>> retryOnSameExceptions) {
    if (retryOnSameExceptions != null && retryOnSameExceptions.size() != 0) {
      this.retryOnSameExceptions.addAll(retryOnSameExceptions);
    }
  }

  public List<Class<? extends Throwable>> getRetryOnSameExceptions() {
    return retryOnSameExceptions;
  }

  public boolean removeRetryOnSameException(Class<? extends Throwable> exception) {
    return retryOnSameExceptions.remove(exception);
  }

  @Override
    public boolean isRetriableException(Throwable e, boolean sameServer) {
    return retryEnabled && (!sameServer || Utils.isPresentAsCause(e, retryOnSameExceptions));
  }

  @Override
  public boolean isCircuitTrippingException(Throwable e) {
    return Utils.isPresentAsCause(e, circuitRelated);
  }

  @Override
  public int getMaxRetriesOnSameServer() {
    return retrySameServer;
  }

  @Override
  public int getMaxRetriesOnNextServer() {
    return retryNextServer;
  }


}
