package io.servicecomb.serviceregistry.definition;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.servicecomb.config.ConfigUtil;
import io.servicecomb.config.archaius.sources.ConfigModel;

public class MicroserviceDefinition {
    private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceDefinition.class);

    // microservice.yaml parent path
    private String rootPath;

    private List<ConfigModel> configModelList;

    private Configuration configuration;

    private String microserviceName;

    public String getMicroserviceName() {
        return microserviceName;
    }

    // do not care, if it is a real file, just get directory
    private String getParentPath(URL url) {
        File file = new File(url.getPath());
        return file.getParent();
    }

    public MicroserviceDefinition(List<ConfigModel> configModelList) {
        this.rootPath = getParentPath(configModelList.get(0).getUrl());
        this.configModelList = configModelList;
        this.configuration = ConfigUtil.createConfig(configModelList);
        this.microserviceName = configuration.getString(DefinitionConst.qulifiedServiceNameKey);

        // log paths first, even microserviceName is invalid, this can help user to find problems
        logConfigPath();

        // the configuration we used
        // when resolve placeholder failed
        // the result will remain ${var}......
        if (StringUtils.isEmpty(microserviceName) || microserviceName.indexOf("${") != -1) {
            throw new IllegalArgumentException("MicroserviceName " + microserviceName + " is invalid.");
        }
    }

    // microserviceName maybe null
    public void logConfigPath() {
        for (ConfigModel configModel : configModelList) {
            LOGGER.info("load microservice config, name={}, path={}",
                    microserviceName,
                    configModel.getUrl().toString());
        }
    }

    public String getRootPath() {
        return rootPath;
    }

    public List<ConfigModel> getConfigModelList() {
        return configModelList;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
