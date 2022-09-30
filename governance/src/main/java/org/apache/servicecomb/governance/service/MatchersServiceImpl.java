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

package org.apache.servicecomb.governance.service;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.marker.RequestProcessor;
import org.apache.servicecomb.governance.marker.TrafficMarker;
import org.apache.servicecomb.governance.properties.MatchProperties;
import org.apache.servicecomb.governance.utils.ProfileExtract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class MatchersServiceImpl implements MatchersService, ApplicationContextAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(MatchersServiceImpl.class);
  private final RequestProcessor requestProcessor;

  private final MatchProperties matchProperties;

  private ApplicationContext applicationContext;


  public MatchersServiceImpl(RequestProcessor requestProcessor, MatchProperties matchProperties) {
    this.requestProcessor = requestProcessor;
    this.matchProperties = matchProperties;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  public boolean checkMatch(GovernanceRequest governanceRequest, String key) {
    Map<String, TrafficMarker> parsedEntity = matchProperties.getParsedEntity();

    TrafficMarker trafficMarker = parsedEntity.get(key);

    if (trafficMarker == null) {
      return false;
    }

    return checkProfileMatch(trafficMarker, governanceRequest) && trafficMarker.checkMatch(governanceRequest, requestProcessor);
  }

  private boolean verifyProfileValue(String profileValues, String profileValue) {
     return Stream.of(profileValues.split(",")).collect(Collectors.toSet()).contains(profileValue);
  }

  public boolean checkProfileMatch(TrafficMarker trafficMarker, GovernanceRequest governanceRequest) {
    String profileExtractionClassName = trafficMarker.getProfileExtractClass();
    String profileValues = trafficMarker.getProfileValues();
    if (!StringUtils.isEmpty(profileExtractionClassName) && !StringUtils.isEmpty(profileValues)) {
      ProfileExtract profileExtract =  generateExtractionClass(profileExtractionClassName);
      String profileValue = profileExtract.extractProfile(governanceRequest.getSourceRequest());
      return verifyProfileValue(profileValues, profileValue);
    }
    return true;
  }

  public ProfileExtract generateExtractionClass(String profileExtractionClassName) {
    if (applicationContext.containsBean(profileExtractionClassName)) {
      Object extractObject = applicationContext.getBean(profileExtractionClassName);
      if (!(extractObject instanceof ProfileExtract)) {
        LOGGER.error("{} {}", profileExtractionClassName, ProfileExtract.errorMessageForNotImplements);
        throw new RuntimeException(profileExtractionClassName + ProfileExtract.errorMessageForNotImplements);
      }
      return (ProfileExtract)extractObject;
    }

    Class<?> extractionHandlerClass = null;
    try {
      extractionHandlerClass = Class.forName(profileExtractionClassName);
    } catch (ClassNotFoundException e) {
      LOGGER.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

    if (!ProfileExtract.class.isAssignableFrom(extractionHandlerClass)) {
      LOGGER.error("{} {}", profileExtractionClassName, ProfileExtract.errorMessageForNotImplements);
      throw new RuntimeException(profileExtractionClassName + ProfileExtract.errorMessageForNotImplements);
    }
    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(extractionHandlerClass);
    BeanDefinitionRegistry registry = (BeanDefinitionRegistry) applicationContext;
    registry.registerBeanDefinition(profileExtractionClassName, builder.getBeanDefinition());
    try {
      ProfileExtract extractObject  = (ProfileExtract)applicationContext.getBean(profileExtractionClassName);
      return  extractObject;
    } catch (BeansException e) {
      LOGGER.error(e.getMessage(), e);
      throw new RuntimeException(profileExtractionClassName + ProfileExtract.errorMessageForAbstractClass, e);
    }
  }

}
