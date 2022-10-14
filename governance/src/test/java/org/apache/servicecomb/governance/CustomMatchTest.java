/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.governance;

import org.apache.servicecomb.governance.marker.CustomMatcher;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.marker.Matcher;
import org.apache.servicecomb.governance.marker.RequestProcessor;
import org.apache.servicecomb.governance.mockclasses.service.MockConfigurationForCustomMatcher;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.Assertions;

@SpringBootTest
@ContextConfiguration(classes = {GovernanceConfiguration.class, MockConfiguration.class, MockConfigurationForCustomMatcher.class})
public class CustomMatchTest {

    private RequestProcessor requestProcessor;

    @Autowired
    public void setRequestProcessor(RequestProcessor requestProcessor) {
        this.requestProcessor = requestProcessor;
    }

    private Matcher generateMatcher(String customMatcherHandler, String customMatcherParameters) {
        CustomMatcher customMatcher = new CustomMatcher();
        customMatcher.setCustomMatcherHandler(customMatcherHandler);
        customMatcher.setCustomMatcherParameters(customMatcherParameters);
        Matcher matcher = new Matcher();
        matcher.setCustomMatcher(customMatcher);
        return matcher;
    }

    @Test
    public void test_should_pass_when_value_class_empty() {
        GovernanceRequest request = new GovernanceRequest();
        Matcher mockMatcher = generateMatcher("", "");
        Assert.assertTrue(this.requestProcessor.match(request, mockMatcher));
        mockMatcher = generateMatcher("", "bill");
        Assert.assertTrue(this.requestProcessor.match(request, mockMatcher));
        mockMatcher = generateMatcher("classWithAnnotation", "");
        Assert.assertTrue(this.requestProcessor.match(request, mockMatcher));
    }

    @Test
    public void test_should_pass_when_value_class_match() {
        GovernanceRequest request = new GovernanceRequest();
        Matcher mockMatcher = generateMatcher("classWithAnnotation", "bill");
        Assert.assertTrue(this.requestProcessor.match(request, mockMatcher));
    }

    @Test
    public void test_should_throw_exception_when_class_not_found() {
        GovernanceRequest request = new GovernanceRequest();
        try {
            Matcher mockMatcher = generateMatcher("classWithAnnotationNotFound", "bill");
            this.requestProcessor.match(request, mockMatcher);
            Assertions.fail("an exception is expected!");
        } catch (Exception e) {
            Assert.assertTrue(e.getCause() instanceof ClassNotFoundException);
        }
    }

    @Test
    public void test_should_pass_when_multiple_value() {
        GovernanceRequest request = new GovernanceRequest();
        Matcher mockMatcher = generateMatcher("classWithAnnotation", "bill,bill2");
        Assert.assertTrue(this.requestProcessor.match(request, mockMatcher));
    }

    @Test
    public void test_should_throw_exception_when_not_implements_interface() {
        GovernanceRequest request = new GovernanceRequest();
        try {
            Matcher mockMatcher = generateMatcher("classNotImplments", "bill,bill2");
            this.requestProcessor.match(request, mockMatcher);
            Assertions.fail("an exception is expected!");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains(RequestProcessor.errorMessageForNotImplements));
        }
    }
}
