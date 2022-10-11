package org.apache.servicecomb.governance.mockclasses;

import org.apache.servicecomb.governance.utils.CustomMatch;
import org.apache.servicecomb.governance.marker.GovernanceRequest;

public class CustomMatchDemo implements CustomMatch {

    private CustomMatchDemo() {

    }

    @Override
    public boolean matchRequest(GovernanceRequest request, String parameters) {
        return true;
    }
}
