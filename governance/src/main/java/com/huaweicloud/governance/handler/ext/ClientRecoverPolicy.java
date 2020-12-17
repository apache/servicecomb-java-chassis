package com.huaweicloud.governance.handler.ext;

public interface ClientRecoverPolicy<T> {

  T apply(Throwable th);
}
