package org.apache.servicecomb.governance.profileclasses;

import org.apache.servicecomb.governance.utils.ProfileExtract;

public class ProfileExtractionDemo implements ProfileExtract {

    private ProfileExtractionDemo() {

    }

    @Override
    public String extractProfile(Object request) {
        return "bill";
    }
}
