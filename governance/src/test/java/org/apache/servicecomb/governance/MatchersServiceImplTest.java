package org.apache.servicecomb.governance;

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.profileclasses.service.MockConfigurationForProfile;
import org.apache.servicecomb.governance.service.MatchersService;
import org.apache.servicecomb.governance.utils.ProfileExtract;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {GovernanceConfiguration.class, MockConfiguration.class, MockConfigurationForProfile.class})
public class MatchersServiceImplTest {

    private MatchersService matchersService;

    @Autowired
    public void setMatchersService(MatchersService matchersService) {
        this.matchersService = matchersService;
    }

    @Test
    public void test_should_pass_when_value_class_empty() {
        GovernanceRequest request = new GovernanceRequest();
        request.setUri("/hello");
        Assert.assertTrue(this.matchersService.checkMatch(request,"demo-allOperation"));
        Assert.assertTrue(this.matchersService.checkMatch(request,"demo-profileExtraction-profileClass"));
        Assert.assertTrue(this.matchersService.checkMatch(request,"demo-profileExtraction-profileValue"));
        Assert.assertTrue(this.matchersService.checkMatch(request,"demo-profileExtraction-emptyProfileValue"));
        Assert.assertTrue(this.matchersService.checkMatch(request,"demo-profileExtraction-emptyProfileClass"));
    }

    @Test
    public void test_should_pass_when_value_class_match() {
        GovernanceRequest request = new GovernanceRequest();
        request.setUri("/hello");
        Assert.assertTrue(this.matchersService.checkMatch(request,"demo-profileExtraction-classValueMatch"));
    }

    @Test
    public void test_should_throw_exception_when_class_not_found() {
        GovernanceRequest request = new GovernanceRequest();
        request.setUri("/hello");
        try {
            this.matchersService.checkMatch(request,"demo-profileExtraction-classNotFound");
        } catch (Exception e) {
            Assert.assertTrue(e.getCause() instanceof ClassNotFoundException);
        }
    }

    @Test
    public void test_should_pass_when_multiple_value() {
        GovernanceRequest request = new GovernanceRequest();
        request.setUri("/hello");
        Assert.assertTrue(this.matchersService.checkMatch(request,"demo-profileExtraction-multipleValues"));
    }

    @Test
    public void test_should_throw_exception_when_not_implements_interface() {
        GovernanceRequest request = new GovernanceRequest();
        request.setUri("/hello");
        try {
            this.matchersService.checkMatch(request,"demo-profileExtraction-notImplementExtraction");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains(ProfileExtract.errorMessageForNotImplements));
        }
    }

    @Test
    public void test_should_throw_exception_when_not_abstract_class() {
        GovernanceRequest request = new GovernanceRequest();
        request.setUri("/hello");
        try {
            this.matchersService.checkMatch(request,"demo-profileExtraction-abstractClass");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains(ProfileExtract.errorMessageForAbstractClass));
        }
    }

    @Test
    public void test_should_throw_exception_when_annotation_class() {
        GovernanceRequest request = new GovernanceRequest();
        request.setUri("/hello");
        Assert.assertTrue(this.matchersService.checkMatch(request,"demo-profileExtraction-annotationClass"));
    }

}
