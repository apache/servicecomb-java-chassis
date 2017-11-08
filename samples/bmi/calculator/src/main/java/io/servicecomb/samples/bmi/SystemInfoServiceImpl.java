/*
 *  Copyright 2017 Huawei Technologies Co., Ltd
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.servicecomb.samples.bmi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

@Profile("!v2")
@Service
public class SystemInfoServiceImpl implements SystemInfoService {

  /**
   * {@inheritDoc}
   */
  @Override
  public BMIViewObject getInstanceInfo() {

    MicroserviceInstance instance = RegistryUtils.getMicroserviceInstance();
    String processId = instance.getInstanceId().substring(0, 12);  

    Date date = new Date(); 
    DateFormat format = new SimpleDateFormat("HH:mm:ss");
    String callTime = format.format(date);
    
    BMIViewObject bmiViewObject = new BMIViewObject();
    bmiViewObject.setCallTime(callTime);
    bmiViewObject.setProcessId(processId);
    
    return bmiViewObject;
    
  }
  
}
