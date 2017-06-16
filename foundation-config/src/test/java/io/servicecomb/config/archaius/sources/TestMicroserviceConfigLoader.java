package io.servicecomb.config.archaius.sources;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

public class TestMicroserviceConfigLoader {
    private ConfigModel createConfigModel(String protocol, int order, String file) throws MalformedURLException {
        ConfigModel configModel = new ConfigModel();
        configModel.setUrl(new URL(protocol, null, file));
        configModel.setOrder(order);
        return configModel;
    }

    @Test
    public void testSort() throws MalformedURLException {
        MicroserviceConfigLoader loader = new MicroserviceConfigLoader();

        loader.getConfigModelList().add(createConfigModel("file", 1, "f1"));
        loader.getConfigModelList().add(createConfigModel("jar", 1, "j1"));
        loader.getConfigModelList().add(createConfigModel("file", 0, "f2"));
        loader.getConfigModelList().add(createConfigModel("file", 0, "f3"));
        loader.getConfigModelList().add(createConfigModel("jar", 0, "j2"));
        loader.getConfigModelList().add(createConfigModel("file", 0, "f4"));
        loader.getConfigModelList().add(createConfigModel("jar", 0, "j3"));
        loader.getConfigModelList().add(createConfigModel("jar", 0, "j4"));

        loader.sort();

        StringBuilder sb = new StringBuilder();
        for (ConfigModel configModel : loader.getConfigModelList()) {
            sb.append(configModel.getUrl()).append(",");
        }
        Assert.assertEquals("jar:j2,jar:j3,jar:j4,jar:j1,file:f2,file:f3,file:f4,file:f1,", sb.toString());
    }
}
