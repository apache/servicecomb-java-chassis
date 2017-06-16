package io.servicecomb.serviceregistry.definition;

public interface DefinitionConst {
    String appIdKey = "APPLICATION_ID";

    String serviceDescriptionKey = "service_description";

    String nameKey = "name";

    String qulifiedServiceNameKey = serviceDescriptionKey + "." + nameKey;

    String defaultAppId = "default";
}
