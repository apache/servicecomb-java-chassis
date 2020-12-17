package com.huaweicloud.governance.handler.ext;

public interface ServerRecoverPolicy<T> {

  T apply(Throwable th);
}
