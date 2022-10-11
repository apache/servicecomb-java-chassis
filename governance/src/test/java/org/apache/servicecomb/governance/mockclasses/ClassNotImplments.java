package org.apache.servicecomb.governance.mockclasses;

import org.springframework.stereotype.Component;
import org.apache.servicecomb.governance.marker.GovernanceRequest;

@Component
public class ClassNotImplments {
    public boolean matchRequest(GovernanceRequest request, String parameters) {
        return true;
    }
}
