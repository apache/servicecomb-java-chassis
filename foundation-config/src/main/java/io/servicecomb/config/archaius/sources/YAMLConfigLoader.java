package io.servicecomb.config.archaius.sources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class YAMLConfigLoader extends AbstractConfigLoader {
    protected String orderKey = "config-order";

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ConfigModel load(URL url) throws IOException {
        Yaml yaml = new Yaml();

        try (InputStream inputStream = url.openStream()) {
            ConfigModel configModel = new ConfigModel();
            configModel.setUrl(url);
            configModel.setConfig(yaml.loadAs(inputStream, Map.class));

            Object objOrder = configModel.getConfig().get(orderKey);
            if (objOrder != null) {
                if (Integer.class.isInstance(objOrder)) {
                    configModel.setOrder((int) objOrder);
                } else {
                    configModel.setOrder(Integer.parseInt(String.valueOf(objOrder)));
                }
            }
            return configModel;
        }
    }
}
