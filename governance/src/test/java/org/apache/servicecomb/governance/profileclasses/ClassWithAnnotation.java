package org.apache.servicecomb.governance.profileclasses;

import org.apache.servicecomb.governance.profileclasses.service.MockProfileClassUserService;
import org.apache.servicecomb.governance.utils.ProfileExtract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClassWithAnnotation  implements ProfileExtract {
    @Autowired
    MockProfileClassUserService service;

    @Override
    public String extractProfile(Object request) {
        return service.getUser();
    }
}
