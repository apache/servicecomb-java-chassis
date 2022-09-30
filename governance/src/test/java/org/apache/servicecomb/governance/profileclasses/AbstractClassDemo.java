package org.apache.servicecomb.governance.profileclasses;

import org.apache.servicecomb.governance.utils.ProfileExtract;

public abstract class AbstractClassDemo implements ProfileExtract {

    @Override
    public String extractProfile(Object request) {
        return "bill";
    }
}
