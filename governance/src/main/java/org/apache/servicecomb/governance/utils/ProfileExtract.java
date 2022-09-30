package org.apache.servicecomb.governance.utils;

public interface ProfileExtract {
    String errorMessageForNotImplements = " didn't implement interface org.apache.servicecomb.governance.utils.ProfileExtract";
    String errorMessageForAbstractClass = " should be a instantiable class rather than abstract class or other else";
    String extractProfile(Object request);
}
