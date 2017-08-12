package io.servicecomb.transport.rest.servlet;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicProperty;
import com.netflix.config.DynamicPropertyFactory;

import mockit.Deencapsulation;

// copy this everywhere, :(
public class ArchaiusUtils {
    public static void resetConfig() {
        Deencapsulation.setField(ConfigurationManager.class, "instance", null);
        Deencapsulation.setField(ConfigurationManager.class, "customConfigurationInstalled", false);
        Deencapsulation.setField(DynamicPropertyFactory.class, "config", null);
        Deencapsulation.setField(DynamicPropertyFactory.class, "initializedWithDefaultConfig", false);
        Deencapsulation.setField(DynamicProperty.class, "dynamicPropertySupportImpl", null);
    }
}
