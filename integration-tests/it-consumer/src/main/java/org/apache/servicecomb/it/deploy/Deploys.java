/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.it.deploy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Deploys {
  private static final Logger LOGGER = LoggerFactory.getLogger(Deploys.class);

  private static final String DEFAULT_MICROSERVICE_VERSION = "1.0.0";

  private String pomVersion;

  private ServiceCenterDeploy serviceCenter;

  private MicroserviceDeploy edge;

  private MicroserviceDeploy baseProducer;

  private MicroserviceDeploy baseHttp2CProducer;

  private MicroserviceDeploy baseHttp2Producer;

  private MicroserviceDeploy springBoot2StandaloneProducer;

  private MicroserviceDeploy springBoot2ServletProducer;

  private MicroserviceDeploy zuul;

  public ServiceCenterDeploy getServiceCenter() {
    return serviceCenter;
  }

  public MicroserviceDeploy getEdge() {
    return edge;
  }

  public MicroserviceDeploy getZuul() {
    return zuul;
  }

  public MicroserviceDeploy getBaseProducer() {
    return baseProducer;
  }

  public MicroserviceDeploy getBaseHttp2Producer() {
    return baseHttp2Producer;
  }

  public MicroserviceDeploy getBaseHttp2CProducer() {
    return baseHttp2CProducer;
  }

  public MicroserviceDeploy getSpringBoot2StandaloneProducer() {
    return springBoot2StandaloneProducer;
  }

  public MicroserviceDeploy getSpringBoot2ServletProducer() {
    return springBoot2ServletProducer;
  }

  public void init() throws Throwable {
    initPomVersion();
    LOGGER.info("test version: {}", pomVersion);

    serviceCenter = new ServiceCenterDeploy();
    initEdge();
    initBaseProducer();
    initBaseHttp2CProducer();
    initBaseHttp2Producer();
    initSpringBoot2StandaloneProducer();
    initSpringBoot2ServletProducer();
//    initZuul();
  }

  public void setPomVersion(String pomVersion) {
    this.pomVersion = pomVersion;
  }

  private void initPomVersion() throws Throwable {
    // already set manually
    if (pomVersion != null) {
      return;
    }

    // already package to jar
    pomVersion = Deploys.class.getPackage().getImplementationVersion();
    if (pomVersion != null) {
      return;
    }

    // run in ide
    MavenXpp3Reader reader = new MavenXpp3Reader();
    Model model = reader.read(new FileReader("pom.xml"));
    pomVersion = model.getVersion();
    if (pomVersion != null) {
      return;
    }

    if (model.getParent() == null) {
      throw new IllegalStateException("can not find pom ServiceComb version");
    }

    pomVersion = model.getParent().getVersion();
    if (pomVersion != null) {
      return;
    }

    throw new IllegalStateException("can not find pom ServiceComb version");
  }

  private void initDeployDefinition(DeployDefinition deployDefinition) {
    deployDefinition.init();
    doInitDeployDefinition(deployDefinition);
    LOGGER.info("definition of {} is: {}", deployDefinition.getDeployName(), deployDefinition);
  }

  private void doInitDeployDefinition(DeployDefinition deployDefinition) {
    // absolute path or relate to current work dir
    File cmd = new File(deployDefinition.getCmd());
    if (cmd.exists() && cmd.isFile()) {
      deployDefinition.setCmd(cmd.getAbsolutePath());
      return;
    }

    // run in ide
    File workDir = new File("integration-tests");
    if (initDeployDefinition(deployDefinition, workDir)) {
      return;
    }

    // run "mvn install"
    workDir = new File("..").getAbsoluteFile();
    if (initDeployDefinition(deployDefinition, workDir)) {
      return;
    }

    throw new IllegalStateException(String
        .format("can not find deploy cmd, work dir: %s, definition: %s", new File("").getAbsolutePath(),
            deployDefinition));
  }

  private boolean initDeployDefinition(DeployDefinition deployDefinition, File workDir) {
    // deployDefinition.getCmd() is it-edge/target/it-edge-1.0.0.jar
    File cmd = new File(workDir, deployDefinition.getCmd());
    if (cmd.exists() && cmd.isFile()) {
      deployDefinition.setCmd(cmd.getAbsolutePath());
      return true;
    }

    // deployDefinition.getCmd() is it-edge
    // change it-edge to it-edge/it-edge-1.0.0.jar
    cmd = new File(workDir, String.format("%s/target/%s-%s.jar",
        deployDefinition.getCmd(),
        deployDefinition.getCmd(),
        pomVersion));
    if (cmd.exists()) {
      try {
        deployDefinition.setCmd(cmd.getCanonicalPath());
      } catch (IOException e) {
        throw new IllegalStateException("Failed to getCanonicalPath of " + cmd.getAbsolutePath(), e);
      }
      return true;
    }

    return false;
  }

  private void initBaseProducer() {
    MicroserviceDeployDefinition definition = new MicroserviceDeployDefinition();
    definition.setDeployName("baseProducer");
    definition.setCmd("it-producer");
    definition.setArgs(new String[] {});
    definition.setAppId("integration-test");
    definition.setMicroserviceName("it-producer");
    definition.setVersion(DEFAULT_MICROSERVICE_VERSION);

    initDeployDefinition(definition);

    baseProducer = new MicroserviceDeploy(definition);
  }

  private void initBaseHttp2Producer() {
    MicroserviceDeployDefinition definition = new MicroserviceDeployDefinition();
    definition.setDeployName("baseHttp2Producer");
    definition.setCmd("it-producer");
    definition.setArgs(new String[] {});
    URL urlServer = Thread.currentThread().getContextClassLoader().getResource("certificates/server.p12");
    URL urlTrust = Thread.currentThread().getContextClassLoader().getResource("certificates/trust.jks");
    if (urlServer != null && urlTrust != null) {
      definition.setArgs(new String[] {"-Dservicecomb.rest.address=0.0.0.0:0?sslEnabled=true&protocol=http2",
          "-Dservicecomb.highway.address=0.0.0.0:0?sslEnabled=true",
          "-Dserver.p12=" + urlServer.getPath(),
          "-Dtrust.jks=" + urlTrust.getPath()
      });
    }
    definition.setAppId("integration-test");
    definition.setMicroserviceName("it-producer");
    definition.setVersion(DEFAULT_MICROSERVICE_VERSION);

    initDeployDefinition(definition);

    baseHttp2Producer = new MicroserviceDeploy(definition);
  }

  private void initBaseHttp2CProducer() {
    MicroserviceDeployDefinition definition = new MicroserviceDeployDefinition();
    definition.setDeployName("baseHttp2CProducer");
    definition.setCmd("it-producer");
    definition.setArgs(new String[] {"-Dservicecomb.rest.address=0.0.0.0:0?protocol=http2"});
    definition.setAppId("integration-test");
    definition.setMicroserviceName("it-producer");
    definition.setVersion(DEFAULT_MICROSERVICE_VERSION);

    initDeployDefinition(definition);

    baseHttp2CProducer = new MicroserviceDeploy(definition);
  }

  private void initSpringBoot2ServletProducer() {
    MicroserviceDeployDefinition definition = new MicroserviceDeployDefinition();
    definition.setDeployName("springBoot2ServletProducer");
    definition.setCmd("it-producer-deploy-springboot2-servlet");
    definition.setArgs(new String[] {});
    definition.setAppId("integration-test");
    definition.setMicroserviceName("it-producer-deploy-springboot2-servlet");
    definition.setVersion(DEFAULT_MICROSERVICE_VERSION);

    initDeployDefinition(definition);

    springBoot2ServletProducer = new MicroserviceDeploy(definition);
  }

  private void initSpringBoot2StandaloneProducer() {
    MicroserviceDeployDefinition definition = new MicroserviceDeployDefinition();
    definition.setDeployName("springBoot2StandaloneProducer");
    definition.setCmd("it-producer-deploy-springboot2-standalone");
    definition.setArgs(new String[] {});
    definition.setAppId("integration-test");
    definition.setMicroserviceName("it-producer-deploy-springboot2-standalone");
    definition.setVersion(DEFAULT_MICROSERVICE_VERSION);

    initDeployDefinition(definition);

    springBoot2StandaloneProducer = new MicroserviceDeploy(definition);
  }

  private void initEdge() {
    MicroserviceDeployDefinition definition = new MicroserviceDeployDefinition();
    definition.setDeployName("edge");
    definition.setCmd("it-edge");
    definition.setArgs(new String[] {});
    definition.setAppId("integration-test");
    definition.setMicroserviceName("it-edge");
    definition.setVersion(DEFAULT_MICROSERVICE_VERSION);

    initDeployDefinition(definition);

    edge = new MicroserviceDeploy(definition);
  }

  //  private void initZuul() {
  //    MicroserviceDeployDefinition zuulDefinition = new MicroserviceDeployDefinition();
  //    zuulDefinition.setDeployName("zuul");
  //    zuulDefinition.setCmd("it-zuul");
  //
  //    initDeployDefinition(zuulDefinition);
  //
  //    zuul = new MicroserviceDeploy(zuulDefinition);
  //  }
}
