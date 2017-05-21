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

package io.servicecomb.common.rest.locator;

import java.util.Collections;

import io.servicecomb.common.rest.definition.RestOperationMeta;
import org.junit.Assert;
import org.junit.Test;

import com.huawei.paas.cse.core.definition.MicroserviceMeta;
import com.huawei.paas.cse.core.definition.OperationMeta;
import com.huawei.paas.cse.core.exception.CommonExceptionData;
import com.huawei.paas.cse.core.exception.InvocationException;

public class TestLocator {
    @Test
    public void testServicePathManager() {
        MicroserviceMeta msm = new MicroserviceMeta("app:ms");
        ServicePathManager spm = new ServicePathManager(msm);

        RestOperationMeta rom = createRestOperatonMeta("GET", "abc/{id}");
        spm.addResource(rom);

        rom = createRestOperatonMeta("GET", "abc/{id}/xxx");
        spm.addResource(rom);

        Assert.assertEquals("abc/{id}", spm.getDynamicPathOperationList().get(0).getAbsolutePath());
        spm.sortPath();
        Assert.assertEquals("abc/{id}/xxx", spm.getDynamicPathOperationList().get(0).getAbsolutePath());

        spm.printService();
        spm.doPrintService();
    }

    @Test
    public void testLocateDynamic() {
        MicroserviceMeta msm = new MicroserviceMeta("app:ms");
        ServicePathManager spm = new ServicePathManager(msm);

        RestOperationMeta rom = createRestOperatonMeta("GET", "abc/{id}");
        spm.addResource(rom);

        try {
            spm.locateOperation("abc/10", "PUT");
        } catch (InvocationException e) {
            Assert.assertEquals("Method Not Allowed", ((CommonExceptionData) e.getErrorData()).getMessage());
        }

        OperationLocator locator = spm.locateOperation("abc/10", "GET");
        Assert.assertEquals("10", locator.getPathVarMap().get("id"));
    }

    @Test
    public void testLocateStatic() {
        MicroserviceMeta msm = new MicroserviceMeta("app:ms");
        ServicePathManager spm = new ServicePathManager(msm);

        RestOperationMeta rom = createRestOperatonMeta("GET", "abc/");
        spm.addResource(rom);

        rom = createRestOperatonMeta("POST", "abc/");
        spm.addResource(rom);

        try {
            spm.addResource(rom);
        } catch (Throwable e) {
            Assert.assertEquals("operation with url abc/, method POST is duplicated", e.getMessage());
        }

        Assert.assertEquals(1, spm.getStaticPathOperationMap().size());
        Assert.assertEquals(2, spm.getStaticPathOperationMap().get("abc/").values().size());

        try {
            spm.locateOperation("abcd", "GET");
        } catch (InvocationException e) {
            Assert.assertEquals("Not Found", ((CommonExceptionData) e.getErrorData()).getMessage());
        }

        try {
            spm.locateOperation("abc/", "PUT");
        } catch (InvocationException e) {
            Assert.assertEquals("Method Not Allowed", ((CommonExceptionData) e.getErrorData()).getMessage());
        }

        OperationLocator locator = spm.locateOperation("abc/", "GET");
        Assert.assertEquals(Collections.emptyMap(), locator.getPathVarMap());

        locator.locate(spm, "abc/", "POST");
        Assert.assertEquals(Collections.emptyMap(), locator.getPathVarMap());
    }

    protected RestOperationMeta createRestOperatonMeta(String httpMethod, String path) {
        OperationMeta om = new OperationMeta();
        om.setHttpMethod(httpMethod);

        RestOperationMeta rom = new RestOperationMeta();
        rom.setOperationMeta(om);
        rom.setAbsolutePath(path);
        return rom;
    }

}
