package org.apache.servicecomb.governance.mockclasses;

import org.apache.servicecomb.governance.mockclasses.service.MockProfileClassUserService;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.utils.CustomMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ClassWithAnnotation  implements CustomMatch {
    @Autowired
    MockProfileClassUserService service;

    @Override
    public boolean matchRequest(GovernanceRequest request, String parameters) {
        String profileValue = service.getUser();
        return Stream.of(parameters.split(",")).collect(Collectors.toSet()).contains(profileValue);
    }
}
