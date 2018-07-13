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

package org.apache.servicecomb.serviceregistry.discovery;

public abstract class AbstractDiscoveryFilter implements DiscoveryFilter {
  @Override
  public DiscoveryTreeNode discovery(DiscoveryContext context, DiscoveryTreeNode parent) {
    if (!parent.childrenInited()) {
      synchronized (parent) {
        if (!parent.childrenInited()) {
          init(context, parent);
          parent.childrenInited(true);
        }
      }
    }

    String childName = findChildName(context, parent);
    return parent.child(childName);
  }

  protected abstract void init(DiscoveryContext context, DiscoveryTreeNode parent);

  protected abstract String findChildName(DiscoveryContext context, DiscoveryTreeNode parent);
}
