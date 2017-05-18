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

package com.huawei.paas.cse.common.rest.loader.swagger;

public class TestSwaggerRestSchemaLoader {

    //	private SwaggerRestSchemaLoader swaggerRestSchemaLoader = null;
    //	private RestProducerConfig restSchemaMeta = null;
    //	private String restSchemaMetaName = "RestSchemaMetaName";
    //	@SuppressWarnings("unused")
    //	private RegisterManager<String, RestProducerConfig> schemaMgr = null;
    //	private RestOperationMeta restOperationMeta = new RestOperationMeta();
    //	private Swagger swagger = new Swagger();
    //
    //	@Before
    //	public void setUp() throws Exception {
    //		swaggerRestSchemaLoader = new SwaggerRestSchemaLoader();
    //		restSchemaMeta = new RestProducerConfig(restSchemaMetaName);
    //		schemaMgr = new RegisterManager<>("test");
    //	}
    //
    //	@After
    //	public void tearDown() throws Exception {
    //		swaggerRestSchemaLoader = null;
    //		restSchemaMeta = null;
    //		schemaMgr = null;
    //	}
    //
    //	@Test
    //	public void testCreateRestSchemaMeta() {
    //		boolean status = true;
    //		Swagger swagger = Mockito.mock(Swagger.class);
    //		MicroserviceMeta microserviceMeta = Mockito.mock(MicroserviceMeta.class);
    //		String schemaId = "schemaId";
    //		new MockUp<SchemaUtils>() {
    //			@Mock
    //			public Class<?> getJavaInterface(String schemaId, Swagger swagger) {
    //				try {
    //					return Class.forName("java.lang.String");
    //				} catch (ClassNotFoundException e) {
    //					/* Do not worry */
    //				}
    //				return null;
    //			}
    //		};
    //
    //		new MockUp<SchemaMeta>() {
    //			@Mock
    //			private void initOperations() throws Exception {
    //				/* do not worry */
    //			}
    //		};
    //		try {
    //			RestProducerConfig restSchemaMeta = swaggerRestSchemaLoader
    //					.createRestSchemaMeta(new SchemaMeta(swagger, microserviceMeta, schemaId));
    //			Assert.assertNotNull(restSchemaMeta);
    //		} catch (Exception e) {
    //			status = false;
    //		}
    //		Assert.assertTrue(status);
    //	}
    //
    //	@Test
    //	public void testLoadServiceDefine() {
    //		boolean status = true;
    //
    //		new MockUp<Swagger>() {
    //			@Mock
    //			public Map<String, Path> getPaths() {
    //				Map<String, Path> map = new HashMap<String, Path>();
    //				map.put("firstPath", new Path());
    //				return map;
    //			}
    //		};
    //
    //		new MockUp<Path>() {
    //			@Mock
    //			public Map<HttpMethod, Operation> getOperationMap() {
    //				Map<HttpMethod, Operation> result = new HashMap<HttpMethod, Operation>();
    //				result.put(HttpMethod.GET, new Operation());
    //				return result;
    //			}
    //		};
    //
    //		new MockUp<RestOperationMeta>() {
    //			@Mock
    //			public String getName() {
    //				return "Name";
    //			}
    //		};
    //
    //		Class<?> cls = null;
    //		try {
    //			cls = Class.forName("java.lang.String");
    //		} catch (ClassNotFoundException e) {
    //			status = false;
    //		}
    //		Assert.assertNotNull(swagger);
    //		Assert.assertNotNull(restSchemaMetaName);
    //		try {
    //			swaggerRestSchemaLoader.loadServiceDefine(restSchemaMeta, cls, swagger);
    //		} catch (Exception e) {
    //			status = false;
    //		}
    //
    //		Assert.assertTrue(status);
    //	}
    //
    //	@Test
    //	public void testScanOperation() {
    //		boolean status = true;
    //		Operation operation = new Operation();
    //		Class<?> cls = null;
    //		Assert.assertNotNull(restOperationMeta);
    //		Assert.assertNotNull(operation);
    //		Assert.assertNull(cls);
    //		try {
    //			swaggerRestSchemaLoader.scanOperation(restOperationMeta, operation, cls);
    //		} catch (Exception e) {
    //			status = false;
    //		}
    //		Assert.assertTrue(status);
    //	}
    //
    //	@Test
    //	public void testScanOperationName() {
    //		boolean status = true;
    //		Class<?> cls = null;
    //		Operation operation = new Operation();
    //		operation.setOperationId("indexOf");
    //		try {
    //			cls = Class.forName("java.lang.String");
    //		} catch (ClassNotFoundException e) {
    //			status = false;
    //		}
    //		Assert.assertNotNull(operation);
    //		Assert.assertNotNull(restOperationMeta);
    //		Assert.assertNotNull(cls);
    //		try {
    //			swaggerRestSchemaLoader.scanOperation(restOperationMeta, operation, cls);
    //		} catch (Exception e) {
    //			status = false;
    //		}
    //		Assert.assertTrue(status);
    //	}
    //
    //	@Test
    //	public void testScanOperationWithInvalidName() {
    //		boolean status = true;
    //		Class<?> cls = null;
    //		Operation operation = new Operation();
    //		operation.setOperationId("OperationId");
    //		try {
    //			cls = Class.forName("java.lang.String");
    //		} catch (ClassNotFoundException e) {
    //			status = false;
    //		}
    //		Assert.assertNotNull(operation);
    //		Assert.assertNotNull(restOperationMeta);
    //		try {
    //			swaggerRestSchemaLoader.scanOperation(restOperationMeta, operation, cls);
    //		} catch (Exception e) {
    //			status = false;
    //		}
    //		Assert.assertTrue(status);
    //	}
    //
    //	@Test
    //	public void testScanParameters() {
    //		boolean status = true;
    //		List<Parameter> paramList = new ArrayList<Parameter>();
    //		paramList.add(new BodyParameter());
    //		Method method = this.getClass().getMethods()[0];
    //		Assert.assertNotNull(paramList);
    //		Assert.assertNotNull(restOperationMeta);
    //		Assert.assertNotNull(method);
    //		try {
    //			swaggerRestSchemaLoader.scanParameters(restOperationMeta, paramList, method);
    //		} catch (Exception e) {
    //			status = false;
    //		}
    //		Assert.assertTrue(status);
    //	}
    //
    //	@Test
    //	public void testScanParametersWithEmptyList() {
    //		boolean status = true;
    //		List<Parameter> paramList = new ArrayList<Parameter>();
    //		paramList.add(new BodyParameter());
    //		Method method = null;
    //		try {
    //			method = RestParam.class.getMethod("setParamProcessor", ParamValueProcessor.class);
    //		} catch (NoSuchMethodException e) {
    //			status = false;
    //		} catch (SecurityException e) {
    //			status = false;
    //		}
    //		Assert.assertNotNull(paramList);
    //		Assert.assertNotNull(restOperationMeta);
    //		Assert.assertNotNull(method);
    //		try {
    //			swaggerRestSchemaLoader.scanParameters(restOperationMeta, paramList, method);
    //		} catch (Exception e) {
    //			status = false;
    //		}
    //		Assert.assertTrue(status);
    //	}
    //
    //	@Test
    //	public void testSetProduce() {
    //		boolean status = true;
    //		List<String> produces = new ArrayList<String>();
    //		produces.add("producesString");
    //		Assert.assertNotNull(restOperationMeta);
    //		Assert.assertNotNull(produces);
    //		try {
    //			swaggerRestSchemaLoader.setProduce(restOperationMeta, produces);
    //		} catch (Exception e) {
    //			status = false;
    //		}
    //		Assert.assertTrue(status);
    //	}
    //
    //	@Test
    //	public void testFindMethod() {
    //
    //		Class<?> cls = null;
    //		try {
    //			cls = Class.forName("java.lang.String");
    //		} catch (ClassNotFoundException e) {
    //			// Do not worry
    //		}
    //		Method method = swaggerRestSchemaLoader.findMethod(cls, "operationName");
    //		Assert.assertNull(method);
    //	}
    //
    //	@Test
    //	public void testFindMethodIndexOf() {
    //		Class<?> cls = null;
    //		try {
    //			cls = Class.forName("java.lang.String");
    //		} catch (ClassNotFoundException e) {
    //			// Do not worry
    //		}
    //		Method method = swaggerRestSchemaLoader.findMethod(cls, "indexOf");
    //		Assert.assertNotNull(method);
    //	}

}
