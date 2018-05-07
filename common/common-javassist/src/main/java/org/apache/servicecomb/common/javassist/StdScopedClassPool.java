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
package org.apache.servicecomb.common.javassist;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.scopedpool.ScopedClassPool;
import javassist.scopedpool.ScopedClassPoolRepository;

/**
 * getCached of ScopedClassPool, not only search self and parent
 * but also search other ScopedClassPool in repository
 *
 * this is not what we want, so change getCached behavior
 */
public class StdScopedClassPool extends ScopedClassPool {
  protected StdScopedClassPool(ClassLoader cl, ClassPool src,
      ScopedClassPoolRepository repository, boolean isTemp) {
    super(cl, src, repository, isTemp);
  }

  @Override
  protected CtClass getCached(String classname) {
    return getCachedLocally(classname);
  }
}
