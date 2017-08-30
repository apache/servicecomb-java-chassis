package com.huawei.paas.cse.handler.stats.monitor;

/**
 * 启动器，确保DataFactory只能启动一次
 * @author w00293972
 */
public class MonitorStarter {

    public void start() {
        DataFactory.getInstance().start();
    }
}
