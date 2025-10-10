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
package org.apache.servicecomb.metrics.core.publish;

import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.CONFIG_LATENCY_DISTRIBUTION;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.DEFAULT_METRICS_WINDOW_TIME;
import static org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig.METRICS_WINDOW_TIME;

import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.metrics.core.InvocationMetersInitializer;
import org.apache.servicecomb.metrics.core.publish.model.DefaultPublishModel;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.vertx.core.json.Json;

import org.skyscreamer.jsonassert.JSONAssert;

public class TestInvocationPublishModelFactory {
  EventBus eventBus = new EventBus();

  // not step mode.
  MeterRegistry meterRegistry = new SimpleMeterRegistry();

  InvocationMetersInitializer invocationMetersInitializer = new InvocationMetersInitializer();

  Invocation invocation = Mockito.mock(Invocation.class);

  InvocationStageTrace invocationStageTrace = Mockito.mock(InvocationStageTrace.class);

  Response response = Mockito.mock(Response.class);

  InvocationType invocationType;

  Environment environment = Mockito.mock(Environment.class);

  @Test
  public void createDefaultPublishModel() throws Exception {
    Mockito.when(environment.getProperty(METRICS_WINDOW_TIME, int.class, DEFAULT_METRICS_WINDOW_TIME))
        .thenReturn(DEFAULT_METRICS_WINDOW_TIME);
    Mockito.when(environment.getProperty(
            CONFIG_LATENCY_DISTRIBUTION_MIN_SCOPE_LEN, int.class, 7))
        .thenReturn(7);
    Mockito.when(environment.getProperty(CONFIG_LATENCY_DISTRIBUTION, String.class)).thenReturn("0,1,100");

    invocationMetersInitializer.init(meterRegistry, eventBus, new MetricsBootstrapConfig(environment));
    prepareInvocation();

    PublishModelFactory factory = new PublishModelFactory(meterRegistry.getMeters());
    DefaultPublishModel model = factory.createDefaultPublishModel();

    String expect = """
        {
            "operationPerfGroups" : {
              "groups" : {
                "rest" : {
                  "200" : {
                    "transport" : "rest",
                    "status" : "200",
                    "operationPerfs" : [ {
                      "operation" : "m.s.o",
                      "stages" : {
                        "consumer-encode" : {
                          "totalRequests" : 1.0,
                          "msTotalTime" : 1.0000000000000002E-6,
                          "msMaxLatency" : 1.0000000000000002E-6
                        },
                        "prepare" : {
                          "totalRequests" : 1.0,
                          "msTotalTime" : 1.0000000000000002E-6,
                          "msMaxLatency" : 1.0000000000000002E-6
                        },
                        "wait" : {
                          "totalRequests" : 1.0,
                          "msTotalTime" : 1.0000000000000002E-6,
                          "msMaxLatency" : 1.0000000000000002E-6
                        },
                        "total" : {
                          "totalRequests" : 1.0,
                          "msTotalTime" : 1.4E-5,
                          "msMaxLatency" : 1.4E-5
                        },
                        "consumer-send" : {
                          "totalRequests" : 1.0,
                          "msTotalTime" : 1.0000000000000002E-6,
                          "msMaxLatency" : 1.0000000000000002E-6
                        },
                        "connection" : {
                          "totalRequests" : 1.0,
                          "msTotalTime" : 1.0000000000000002E-6,
                          "msMaxLatency" : 1.0000000000000002E-6
                        },
                        "consumer-decode" : {
                          "totalRequests" : 1.0,
                          "msTotalTime" : 1.0000000000000002E-6,
                          "msMaxLatency" : 1.0000000000000002E-6
                        }
                      },
                      "latencyDistribution" : [ 1, 1, 1 ]
                    } ],
                    "summary" : {
                      "operation" : "",
                      "stages" : {
                        "consumer-encode" : {
                          "totalRequests" : 1.0,
                          "msTotalTime" : 1.0000000000000002E-6,
                          "msMaxLatency" : 1.0000000000000002E-6
                        },
                        "prepare" : {
                          "totalRequests" : 1.0,
                          "msTotalTime" : 1.0000000000000002E-6,
                          "msMaxLatency" : 1.0000000000000002E-6
                        },
                        "total" : {
                          "totalRequests" : 1.0,
                          "msTotalTime" : 1.4E-5,
                          "msMaxLatency" : 1.4E-5
                        },
                        "wait" : {
                          "totalRequests" : 1.0,
                          "msTotalTime" : 1.0000000000000002E-6,
                          "msMaxLatency" : 1.0000000000000002E-6
                        },
                        "consumer-send" : {
                          "totalRequests" : 1.0,
                          "msTotalTime" : 1.0000000000000002E-6,
                          "msMaxLatency" : 1.0000000000000002E-6
                        },
                        "connection" : {
                          "totalRequests" : 1.0,
                          "msTotalTime" : 1.0000000000000002E-6,
                          "msMaxLatency" : 1.0000000000000002E-6
                        },
                        "consumer-decode" : {
                          "totalRequests" : 1.0,
                          "msTotalTime" : 1.0000000000000002E-6,
                          "msMaxLatency" : 1.0000000000000002E-6
                        }
                      },
                      "latencyDistribution" : [ 1, 1, 1 ]
                    }
                  }
                }
              }
            }
          }
        """;
    JSONAssert.assertEquals(Json.encodePrettily(Json.decodeValue(expect, Object.class)),
        Json.encodePrettily(model.getConsumer()), false);

    expect = """
        {
          "operationPerfGroups" : {
            "groups" : {
              "rest" : {
                "200" : {
                  "transport" : "rest",
                  "status" : "200",
                  "operationPerfs" : [ {
                    "operation" : "m.s.o",
                    "stages" : {
                      "consumer-encode" : {
                        "totalRequests" : 1.0,
                        "msTotalTime" : 1.0000000000000002E-6,
                        "msMaxLatency" : 1.0000000000000002E-6
                      },
                      "prepare" : {
                        "totalRequests" : 1.0,
                        "msTotalTime" : 1.0000000000000002E-6,
                        "msMaxLatency" : 1.0000000000000002E-6
                      },
                      "wait" : {
                        "totalRequests" : 1.0,
                        "msTotalTime" : 1.0000000000000002E-6,
                        "msMaxLatency" : 1.0000000000000002E-6
                      },
                      "total" : {
                        "totalRequests" : 1.0,
                        "msTotalTime" : 1.4E-5,
                        "msMaxLatency" : 1.4E-5
                      },
                      "consumer-send" : {
                        "totalRequests" : 1.0,
                        "msTotalTime" : 1.0000000000000002E-6,
                        "msMaxLatency" : 1.0000000000000002E-6
                      },
                      "connection" : {
                        "totalRequests" : 1.0,
                        "msTotalTime" : 1.0000000000000002E-6,
                        "msMaxLatency" : 1.0000000000000002E-6
                      },
                      "consumer-decode" : {
                        "totalRequests" : 1.0,
                        "msTotalTime" : 1.0000000000000002E-6,
                        "msMaxLatency" : 1.0000000000000002E-6
                      }
                    },
                    "latencyDistribution" : [ 1, 1, 1 ]
                  } ],
                  "summary" : {
                    "operation" : "",
                    "stages" : {
                      "consumer-encode" : {
                        "totalRequests" : 1.0,
                        "msTotalTime" : 1.0000000000000002E-6,
                        "msMaxLatency" : 1.0000000000000002E-6
                      },
                      "prepare" : {
                        "totalRequests" : 1.0,
                        "msTotalTime" : 1.0000000000000002E-6,
                        "msMaxLatency" : 1.0000000000000002E-6
                      },
                      "total" : {
                        "totalRequests" : 1.0,
                        "msTotalTime" : 1.4E-5,
                        "msMaxLatency" : 1.4E-5
                      },
                      "wait" : {
                        "totalRequests" : 1.0,
                        "msTotalTime" : 1.0000000000000002E-6,
                        "msMaxLatency" : 1.0000000000000002E-6
                      },
                      "consumer-send" : {
                        "totalRequests" : 1.0,
                        "msTotalTime" : 1.0000000000000002E-6,
                        "msMaxLatency" : 1.0000000000000002E-6
                      },
                      "connection" : {
                        "totalRequests" : 1.0,
                        "msTotalTime" : 1.0000000000000002E-6,
                        "msMaxLatency" : 1.0000000000000002E-6
                      },
                      "consumer-decode" : {
                        "totalRequests" : 1.0,
                        "msTotalTime" : 1.0000000000000002E-6,
                        "msMaxLatency" : 1.0000000000000002E-6
                      }
                    },
                    "latencyDistribution" : [ 1, 1, 1 ]
                  }
                }
              }
            }
          }
        }
        """;
    JSONAssert.assertEquals(Json.encodePrettily(Json.decodeValue(expect, Object.class)),
        Json.encodePrettily(model.getProducer()), false);
  }

  protected void prepareInvocation() {
    Mockito.when(invocationStageTrace.calcTotal()).thenReturn(14L);
    Mockito.when(invocationStageTrace.calcPrepare()).thenReturn(1L);
    Mockito.when(invocationStageTrace.calcConnection()).thenReturn(1L);
    Mockito.when(invocationStageTrace.calcConsumerEncodeRequest()).thenReturn(1L);
    Mockito.when(invocationStageTrace.calcConsumerSendRequest()).thenReturn(1L);
    Mockito.when(invocationStageTrace.calcWait()).thenReturn(1L);
    Mockito.when(invocationStageTrace.calcConsumerDecodeResponse()).thenReturn(1L);

    invocationType = InvocationType.CONSUMER;

    Mockito.when(invocation.getInvocationType()).thenReturn(invocationType);
    Mockito.when(invocation.isConsumer()).thenReturn(InvocationType.CONSUMER.equals(invocationType));
    Mockito.when(invocation.getRealTransportName()).thenReturn(CoreConst.RESTFUL);
    Mockito.when(invocation.getMicroserviceQualifiedName()).thenReturn("m.s.o");
    Mockito.when(invocation.getInvocationStageTrace()).thenReturn(invocationStageTrace);
    Mockito.when(response.getStatusCode()).thenReturn(200);

    InvocationFinishEvent finishEvent = new InvocationFinishEvent(invocation, response);
    eventBus.post(finishEvent);

    invocationType = InvocationType.PROVIDER;
    Mockito.when(invocation.getInvocationType()).thenReturn(invocationType);
    eventBus.post(finishEvent);
  }
}
