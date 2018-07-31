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

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Deploys {
  private static final Logger LOGGER = LoggerFactory.getLogger(Deploys.class);

  private static final String DEFAULT_MICROSERVICE_VERSION = "1.0.0";

  private String pomVersion;

  public MicroserviceDeploy edge;

  public MicroserviceDeploy baseProducer;

  public MicroserviceDeploy zuul;

  public MicroserviceDeploy getEdge() {
    return edge;
  }

  public MicroserviceDeploy getZuul() {
    return zuul;
  }

  public MicroserviceDeploy getBaseProducer() {
    return baseProducer;
  }

  public void init() throws Throwable {
    initPomVersion();
    LOGGER.info("test version: {}", pomVersion);

    initEdge();
    initBaseProducer();
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
    MicroserviceDeployDefinition baseProducerDefinition = new MicroserviceDeployDefinition();
    baseProducerDefinition.setDeployName("baseProducer");
    baseProducerDefinition.setCmd("it-producer");
    baseProducerDefinition.setArgs(new String[] {});
    baseProducerDefinition.setAppId("integration-test");
    baseProducerDefinition.setMicroserviceName("it-producer");
    baseProducerDefinition.setVersion(DEFAULT_MICROSERVICE_VERSION);

    initDeployDefinition(baseProducerDefinition);

    baseProducer = new MicroserviceDeploy(baseProducerDefinition);
  }

  private void initEdge() {
    MicroserviceDeployDefinition edgeDefinition = new MicroserviceDeployDefinition();
    edgeDefinition.setDeployName("edge");
    edgeDefinition.setCmd("it-edge");
    edgeDefinition.setArgs(new String[] {});
    edgeDefinition.setAppId("integration-test");
    edgeDefinition.setMicroserviceName("it-edge");
    edgeDefinition.setVersion(DEFAULT_MICROSERVICE_VERSION);

    initDeployDefinition(edgeDefinition);

    edge = new MicroserviceDeploy(edgeDefinition);
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
