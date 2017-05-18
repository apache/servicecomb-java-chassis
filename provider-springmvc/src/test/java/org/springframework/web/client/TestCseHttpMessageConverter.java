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

package org.springframework.web.client;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.huawei.paas.cse.provider.common.MockUtil;
import com.huawei.paas.cse.provider.springmvc.reference.CseClientHttpRequest;

public class TestCseHttpMessageConverter{

    /**
     * Test Config
     */
    @Test
    public void testAll() {
        MockUtil.getInstance().mockReflectionUtils();
        MockUtil.getInstance().mockCseClientHttpRequest();
        CseHttpMessageConverter lCseHttpMessageConverter=new CseHttpMessageConverter();
        lCseHttpMessageConverter.canWrite(null,null);
        lCseHttpMessageConverter.getSupportedMediaTypes();
        try {
            lCseHttpMessageConverter.read(this.getClass(),null);
        }
        catch (HttpMessageNotReadableException e) {
            // TODO Auto-generated catch block
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
        }
        try {
            HttpOutputMessage httpOutputMessage=Mockito.mock(CseClientHttpRequest.class);
            lCseHttpMessageConverter.write(null,null,httpOutputMessage);
        }
        catch (HttpMessageNotWritableException | IOException e) {
            // TODO Auto-generated catch block
        }
        
        Assert.assertEquals(true, lCseHttpMessageConverter.canRead(null,null));
    }

}
