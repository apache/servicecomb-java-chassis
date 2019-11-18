package org.apache.servicecomb.router.exception;

/**
 * @Author GuoYl123
 * @Date 2019/11/4
 **/
public class RouterIllegalParamException extends RuntimeException {

  public RouterIllegalParamException(String message) {
    super(message);
  }

  public RouterIllegalParamException(String message, Throwable cause) {
    super(message, cause);
  }
}
