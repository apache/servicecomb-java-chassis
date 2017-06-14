/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.demo.discovery.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.web.client.RestTemplate;

import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.demo.TestMgr;
import io.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import io.servicecomb.springboot.starter.provider.EnableServiceComb;

@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
@EnableServiceComb
public class DiscoveryZuulProxy {

	private static RestTemplate restTemplate;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(DiscoveryZuulProxy.class, args);
		runIT();
	}

	private static void runIT() throws Exception {
		restTemplate = RestTemplateBuilder.create();
		new DiscoveryZuulProxy().testInstances(restTemplate);
		TestMgr.summary();
	}

	private void testInstances(RestTemplate template) {
		String port = DynamicPropertyFactory.getInstance().getStringProperty("server.port", "9989").get();
		try {
			String testService = "http://127.0.0.1:8069/controller/sayhi?name=world";
			String directesult = template.getForObject(testService, String.class);
			System.out.println("Response from Micro-Service is  ---> " + directesult);
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error in querying Micro-service directly.");
		}

		String urlPrefix = "http://127.0.0.1:" + port + "/discoverytest/controller/sayhi?name=world";
		String result = template.getForObject(urlPrefix, String.class);
		System.out.println("Response from ZuulProxy  is  ---> " + result);
		TestMgr.check("\"hi world [world]\"", result);
	}
}
