package org.apache.servicecomb.governance;

import org.apache.servicecomb.governance.marker.CustomMatcher;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.marker.Matcher;
import org.apache.servicecomb.governance.marker.RequestProcessor;
import org.apache.servicecomb.governance.mockclasses.service.MockConfigurationForCustomMatcher;
import org.apache.servicecomb.governance.service.MatchersService;
import org.apache.servicecomb.governance.utils.CustomMatch;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {GovernanceConfiguration.class, MockConfiguration.class, MockConfigurationForCustomMatcher.class})
public class CustomMatchTest {

    private RequestProcessor requestProcessor;

    @Autowired
    public void setRequestProcessor(RequestProcessor requestProcessor) {
        this.requestProcessor = requestProcessor;
    }

    private Matcher generateMathcer(String customMatcherHandler, String customMatcherParameters) {
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
        Matcher mockMatcher = generateMathcer("", "");
        Assert.assertTrue(this.requestProcessor.match(request,mockMatcher));
        mockMatcher = generateMathcer("", "bill");
        Assert.assertTrue(this.requestProcessor.match(request,mockMatcher));
        mockMatcher = generateMathcer("classWithAnnotation", "");
        Assert.assertTrue(this.requestProcessor.match(request,mockMatcher));
    }

    @Test
    public void test_should_pass_when_value_class_match() {
        GovernanceRequest request = new GovernanceRequest();
        Matcher mockMatcher = generateMathcer("classWithAnnotation", "bill");
        Assert.assertTrue(this.requestProcessor.match(request,mockMatcher));
    }

    @Test
    public void test_should_throw_exception_when_class_not_found() {
        GovernanceRequest request = new GovernanceRequest();
        try {
            Matcher mockMatcher = generateMathcer("classWithAnnotationNotFound", "bill");
            this.requestProcessor.match(request,mockMatcher);
        } catch (Exception e) {
            Assert.assertTrue(e.getCause() instanceof ClassNotFoundException);
        }
    }

    @Test
    public void test_should_pass_when_multiple_value() {
        GovernanceRequest request = new GovernanceRequest();
        Matcher mockMatcher = generateMathcer("classWithAnnotation", "bill,bill2");
        Assert.assertTrue(this.requestProcessor.match(request,mockMatcher));
    }

    @Test
    public void test_should_throw_exception_when_not_implements_interface() {
        GovernanceRequest request = new GovernanceRequest();
        try {
            Matcher mockMatcher = generateMathcer("classNotImplments", "bill,bill2");
            this.requestProcessor.match(request,mockMatcher);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains(CustomMatch.errorMessageForNotImplements));
        }
    }
}
