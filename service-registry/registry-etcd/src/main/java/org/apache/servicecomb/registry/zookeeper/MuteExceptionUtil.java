package org.apache.servicecomb.registry.zookeeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class MuteExceptionUtil {

    interface FunctionWithException<T, R> {
        R apply(T t) throws Exception;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MuteExceptionUtil.class);

    public static <T, R> R executeFunction(FunctionWithException<T, R> function, T t) {
        try {
            return function.apply(t);
        } catch (Exception e) {
            LOGGER.error("execute Function failure...");
            return null;
        }
    }

    public static <T> T executeSupplier(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            LOGGER.error("execute Supplier failure...");
            return null;
        }
    }

    public static <T> T executeCompletableFuture(CompletableFuture<T> completableFuture) {
        try {
            return completableFuture.get();
        } catch (Exception e) {
            LOGGER.error("execute CompletableFuture failure...");
            return null;
        }
    }

}
