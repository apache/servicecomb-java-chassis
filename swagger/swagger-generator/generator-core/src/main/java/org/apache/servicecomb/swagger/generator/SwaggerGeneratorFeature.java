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
package org.apache.servicecomb.swagger.generator;

public class SwaggerGeneratorFeature {
  private static ThreadLocal<SwaggerGeneratorFeature> featureThreadLocal = new ThreadLocal<>();

  public static ThreadLocal<SwaggerGeneratorFeature> getFeatureThreadLocal() {
    return featureThreadLocal;
  }

  public static boolean isLocalExtJavaClassInVendor() {
    SwaggerGeneratorFeature feature = featureThreadLocal.get();
    return feature != null ? feature.extJavaClassInVendor : true;
  }

  // packageName and extJavaInVender is unnecessary, new invocation mechanism not depend them
  // just remain them for compatible
  private String packageName = "gen.swagger";

  private boolean extJavaClassInVendor = true;

  private boolean extJavaInterfaceInVendor = true;

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public boolean isExtJavaClassInVendor() {
    return extJavaClassInVendor;
  }

  public void setExtJavaClassInVendor(boolean extJavaClassInVendor) {
    this.extJavaClassInVendor = extJavaClassInVendor;
  }

  public boolean isExtJavaInterfaceInVendor() {
    return extJavaInterfaceInVendor;
  }

  public void setExtJavaInterfaceInVendor(boolean extJavaInterfaceInVendor) {
    this.extJavaInterfaceInVendor = extJavaInterfaceInVendor;
  }
}
