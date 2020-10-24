package org.apache.servicecomb.transport.rest.vertx;

import io.vertx.core.Handler;
import org.apache.servicecomb.foundation.common.utils.SPIOrder;

public interface HttpServerExceptionHandler extends Handler<Throwable>, SPIOrder {
}
