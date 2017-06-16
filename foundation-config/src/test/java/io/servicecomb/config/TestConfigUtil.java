package io.servicecomb.config;

import java.util.List;

import org.apache.commons.configuration.AbstractConfiguration;
import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.config.archaius.sources.ConfigModel;
import io.servicecomb.config.archaius.sources.MicroserviceConfigLoader;

public class TestConfigUtil {
    @Test
    public void testCreateDynamicConfig() {
        AbstractConfiguration dynamicConfig = ConfigUtil.createDynamicConfig();
        MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader(dynamicConfig);
        List<ConfigModel> list = loader.getConfigModelList();
        Assert.assertEquals(1, list.size());
    }
}
