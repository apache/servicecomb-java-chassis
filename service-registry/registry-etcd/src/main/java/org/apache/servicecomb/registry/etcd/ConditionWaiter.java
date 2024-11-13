package org.apache.servicecomb.registry.etcd;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ConditionWaiter<T> {
    private final AtomicReference<T> dataReference;
    private final AtomicBoolean isComplete;
    private final long sleepDuration;
    private final TimeUnit timeUnit;
    private final ExecutorService executorService;

    public ConditionWaiter(T initialData, long sleepDuration, TimeUnit timeUnit) {
        this.dataReference = new AtomicReference<>(initialData);
        this.isComplete = new AtomicBoolean(false);
        this.sleepDuration = sleepDuration;
        this.timeUnit = timeUnit;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public T waitForCompletion() {
        while (!isComplete.get()) {
            SleepUtil.sleep(sleepDuration, timeUnit);
        }
        return dataReference.get();
    }

    public void setData(T newData) {
        dataReference.set(newData);
    }

    public void executeTaskAsync(Callable<T> task) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException("Task execution failed", e);
            }
        }, executorService).thenAccept(result -> {
            setData(result);
            isComplete.set(true);
        });
    }

    public static class SleepUtil {
        public static void sleep(long duration, TimeUnit timeUnit) {
            try {
                timeUnit.sleep(duration);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted during sleep!");
            }
        }
    }
}