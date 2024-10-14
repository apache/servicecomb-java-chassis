package org.apache.servicecomb.registry.etcd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class MuteExceptionUtil {

    // 定义两个函数式接口
    interface FunctionWithException<T, R> {
        R apply(T t) throws Exception;
    }

    interface FunctionWithDoubleParam<T1, T2, R> {
        R apply(T1 t1, T2 t2) throws Exception;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MuteExceptionUtil.class);

    // Builder类，用来自定义日志信息
    public static class MuteExceptionUtilBuilder {

        private String logMessage;

        private Object[] customMessageParams;

        // 构建器模式的基本方法，支持可变参数
        public MuteExceptionUtilBuilder withLog(String message, Object... params) {
            this.logMessage = message;
            this.customMessageParams = params;
            return this;
        }

        // 获取日志信息，优先使用用户自定义的日志
        private String getLogMessage(String defaultMessage) {
            return logMessage != null ? logMessage : defaultMessage;
        }

        // 执行带异常处理的Function
        public <T, R> R executeFunction(FunctionWithException<T, R> function, T t) {
            try {
                return function.apply(t);
            } catch (Exception e) {
                LOGGER.error(getLogMessage("execute Function failure..."), e);
                return null;
            }
        }

        // 执行带异常处理的Supplier
        public <T> T executeSupplier(Supplier<T> supplier) {
            try {
                return supplier.get();
            } catch (Exception e) {
                LOGGER.error(getLogMessage("execute Supplier failure..."), e);
                return null;
            }
        }

        // 执行带异常处理的CompletableFuture
        public <T> T executeCompletableFuture(CompletableFuture<T> completableFuture) {
            try {
                return completableFuture.get();
            } catch (Exception e) {
                LOGGER.error(getLogMessage("execute CompletableFuture failure..."), e);
                return null;
            }
        }

        // 执行带两个参数的Function
        public <T1, T2, R> R executeFunctionWithDoubleParam(FunctionWithDoubleParam<T1, T2, R> function, T1 t1, T2 t2) {
            try {
                return function.apply(t1, t2);
            } catch (Exception e) {
                LOGGER.error(getLogMessage("execute FunctionWithDoubleParam failure..."), e);
                return null;
            }
        }
    }

    // 提供静态方法来创建 Builder
    public static MuteExceptionUtilBuilder builder() {
        return new MuteExceptionUtilBuilder();
    }

}