package org.apache.servicecomb.registry.discovery;

public abstract class AbstractGroupDiscoveryFilter extends AbstractDiscoveryFilter {
  @Override
  public boolean isGroupingFilter() {
    return true;
  }

  abstract protected String groupsSizeParameter();

  abstract protected String contextParameter();

  abstract protected String groupPrefix();

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
