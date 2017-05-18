/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.paas.foundation.common.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年5月11日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public abstract class AbstractDistributedLock implements DistributedLock {
    /**
     * verbose
     */
    protected volatile boolean verbose;

    /**
     * listener
     */
    protected volatile Listener listener;

    protected final ReentrantLock localLock = new ReentrantLock();

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @return boolean
     * @see [类、类#方法、类#成员]
     */
    protected abstract boolean doLock();

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @return boolean
     * @see [类、类#方法、类#成员]
     */
    protected abstract boolean doUnlock();

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @return boolean
     * @see [类、类#方法、类#成员]
     */
    protected abstract boolean doTryLock();

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @return boolean
     * @see [类、类#方法、类#成员]
     */
    protected abstract boolean hasDistLock();

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param timeout long
     * @param unit TimeUnit
     * @return boolean
     * @throws InterruptedException InterruptedException;
     * @see [类、类#方法、类#成员]
     */
    protected abstract boolean doTryLock(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @return boolean
     * @throws InterruptedException InterruptedException;
     * @see [类、类#方法、类#成员]
     */
    protected abstract boolean doLockInterruptibly() throws InterruptedException;

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @return boolean
     * @see [类、类#方法、类#成员]
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param verbose boolean
     * @see [类、类#方法、类#成员]
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @return boolean
     * @see [类、类#方法、类#成员]
     */
    public boolean isLocked() {
        return this.localLock.isLocked() && hasDistLock();
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @return boolean
     * @see [类、类#方法、类#成员]
     */
    public boolean isHeldByCurrentThread() {
        return this.localLock.isHeldByCurrentThread() && hasDistLock();
    }

    @Override
    public Listener getListener() {
        return this.listener;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void lock() {
        this.localLock.lock();
        if (this.localLock.getHoldCount() > 1) {
            return;
        }

        boolean succeed = false;
        try {
            doLock();
            succeed = true;
        } finally {
            if (!succeed) {
                this.localLock.unlock();
            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        this.localLock.lockInterruptibly();
        if (this.localLock.getHoldCount() > 1) {
            return;
        }

        boolean succeed = false;
        try {
            doLockInterruptibly();
            succeed = true;
        } finally {
            if (!succeed) {
                this.localLock.unlock();
            }
        }
    }

    @Override
    public boolean tryLock() {
        if (!this.localLock.tryLock()) {
            return false;
        }
        if (this.localLock.getHoldCount() > 1) {
            return true;
        }

        boolean succeed = false;
        try {
            succeed = doTryLock();
        } finally {
            if (!succeed) {
                this.localLock.unlock();
            }
        }
        return succeed;
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        final long mark = System.nanoTime();
        if (!this.localLock.tryLock(timeout, unit)) {
            return false;
        }
        if (this.localLock.getHoldCount() > 1) {
            return true;
        }

        boolean succeed = false;
        try {
            timeout = TimeUnit.NANOSECONDS.convert(timeout, unit) - (System.nanoTime() - mark);
            if (timeout >= 0) {
                succeed = doTryLock(timeout, TimeUnit.NANOSECONDS);
            }
        } finally {
            if (!succeed) {
                this.localLock.unlock();
            }
        }
        return succeed;
    }

    @Override
    public void unlock() {
        if (!this.localLock.isHeldByCurrentThread()) {
            return;
        }

        if (this.localLock.getHoldCount() > 1) {
            return;
        }

        try {
            doUnlock();
        } finally {
            this.localLock.unlock();
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }
}
