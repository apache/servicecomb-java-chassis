package org.apache.servicecomb.governance.utils;

import org.apache.servicecomb.governance.marker.GovernanceRequest;

public interface CustomMatch {
    String errorMessageForNotImplements = " didn't implement interface org.apache.servicecomb.governance.utils.CustomMatch";
    String errorMessageForAbstractClass = " should be a instantiable class rather than abstract class or other else";
    boolean matchRequest(GovernanceRequest request, String parameters);
}
