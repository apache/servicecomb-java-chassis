package io.servicecomb.config.archaius.sources;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.springframework.util.ResourceUtils;

public abstract class AbstractConfigLoader {
    protected List<ConfigModel> configModelList = new ArrayList<>();

    public List<ConfigModel> getConfigModelList() {
        return configModelList;
    }

    public void load(String resourceName) throws IOException {
        loadFromClassPath(resourceName);
    }

    protected void loadFromClassPath(String resourceName) throws IOException {
        List<URL> urlList = findURLFromClassPath(resourceName);
        for (URL url : urlList) {
            ConfigModel configModel = load(url);
            configModelList.add(configModel);
        }
    }

    protected abstract ConfigModel load(URL url) throws IOException;

    protected List<URL> findURLFromClassPath(String resourceName) throws IOException {
        List<URL> urlList = new ArrayList<>();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> urls = loader.getResources(resourceName);
        while (urls.hasMoreElements()) {
            urlList.add(urls.nextElement());
        }

        return urlList;
    }

    class ConfigModelWrapper {
        public ConfigModel model;

        public int addOrder;
    }

    // sort rule:
    // 1.files in jar
    // 2.smaller order
    // 3.add to list earlier
    protected void sort() {
        List<ConfigModelWrapper> list = new ArrayList<>(configModelList.size());
        for (int idx = 0; idx < configModelList.size(); idx++) {
            ConfigModelWrapper wrapper = new ConfigModelWrapper();
            wrapper.model = configModelList.get(idx);;
            wrapper.addOrder = idx;
            list.add(wrapper);
        }

        list.sort(this::doSort);

        for (int idx = 0; idx < configModelList.size(); idx++) {
            configModelList.set(idx, list.get(idx).model);
        }
    }

    protected int doSort(ConfigModelWrapper w1, ConfigModelWrapper w2) {
        ConfigModel m1 = w1.model;
        ConfigModel m2 = w2.model;
        boolean isM1Jar = ResourceUtils.isJarURL(m1.getUrl());
        boolean isM2Jar = ResourceUtils.isJarURL(m2.getUrl());
        if (isM1Jar != isM2Jar) {
            if (isM1Jar) {
                return -1;
            }

            return 1;
        }
        // min order load first
        int result = m1.getOrder() - m2.getOrder();
        if (result != 0) {
            return result;
        }

        return doFinalSort(w1, w2);
    }

    protected int doFinalSort(ConfigModelWrapper w1, ConfigModelWrapper w2) {
        return w1.addOrder - w2.addOrder;
    }
}
