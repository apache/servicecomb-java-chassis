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

package org.apache.servicecomb.registry.discovery;

public abstract class AbstractGroupDiscoveryFilter extends AbstractDiscoveryFilter {
  @Override
  public boolean isGroupingFilter() {
    return true;
  }

  protected abstract String groupsSizeParameter();

  protected abstract String contextParameter();

  protected abstract String groupPrefix();

  @Override
  protected String findChildName(DiscoveryContext context, DiscoveryTreeNode parent) {
    Integer level = context.getContextParameter(contextParameter());
    Integer groups = parent.attribute(groupsSizeParameter());

    String group;
    if (level == null) {
      group = groupPrefix() + 1;
      if (groups > 1) {
        context.pushRerunFilter();
        context.putContextParameter(contextParameter(), 1);
      }
      return group;
    }

    level = level + 1;
    group = groupPrefix() + level;

    if (level < groups) {
      context.pushRerunFilter();
      context.putContextParameter(contextParameter(), level);
    }
    return group;
  }
}
