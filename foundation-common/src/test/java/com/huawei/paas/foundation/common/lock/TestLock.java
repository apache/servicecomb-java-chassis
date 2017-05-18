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

import org.junit.Assert;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockito.Mockito;

import com.huawei.paas.foundation.common.lock.DistributedLock.Listener;

public class TestLock {

    @Test
    public void testLockException() {
        LockException oLockException = new LockException();
        Assert.assertEquals("com.huawei.paas.foundation.common.lock.LockException",
                oLockException.getClass().getName());

        oLockException = new LockException("error");
        Assert.assertEquals("error", oLockException.getMessage());

        oLockException = new LockException("throwableError", new Throwable());
        Assert.assertEquals("throwableError", oLockException.getMessage());
    }

    /**
     * Test AbstractDistributedLock
     * @throws InterruptedException 
     */
    @Test
    public void testAbstractDistributedLock() throws InterruptedException {
        AbstractDistributedLock oLock = new AbstractDistributedLock() {

            @Override
            protected boolean hasDistLock() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            protected boolean doUnlock() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            protected boolean doTryLock(long timeout, TimeUnit unit) throws InterruptedException {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            protected boolean doTryLock() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            protected boolean doLockInterruptibly() throws InterruptedException {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            protected boolean doLock() {
                // TODO Auto-generated method stub
                return true;
            }
        };
        oLock.setVerbose(true);
        oLock.setListener(Mockito.mock(Listener.class));
        oLock.lock();
        Assert.assertEquals(true, oLock.isVerbose());
        Assert.assertEquals(false, oLock.isLocked());
        Assert.assertEquals(false, oLock.isHeldByCurrentThread());
        Assert.assertNotEquals(null, oLock.getListener());
        oLock.lockInterruptibly();
        Assert.assertEquals(true, oLock.tryLock());
        Assert.assertEquals(true, oLock.tryLock(1, TimeUnit.MICROSECONDS));
        Assert.assertEquals(true, oLock.tryLock());
        Assert.assertEquals(false, oLock.isLocked());
        oLock.unlock();
        Assert.assertEquals(false, oLock.isLocked());
        oLock.setListener(null);
        Assert.assertEquals(null, oLock.getListener());
        try {
            oLock.lockInterruptibly();
            oLock.tryLock(23455, TimeUnit.MINUTES);
            Assert.assertEquals(false, oLock.isLocked());
        } catch (Exception e) {
            Assert.assertEquals(true, false);
        }
        try {
            oLock.newCondition();
        } catch (Throwable e) {
            Assert.assertEquals("java.lang.UnsupportedOperationException", e.getClass().getName());
        }

    }

}
