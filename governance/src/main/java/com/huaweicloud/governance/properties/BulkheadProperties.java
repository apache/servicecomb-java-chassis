package com.huaweicloud.governance.properties;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.huaweicloud.governance.event.DynamicConfigListener;
import com.huaweicloud.governance.policy.BulkheadPolicy;

@Component
public class BulkheadProperties implements GovProperties<BulkheadPolicy> {
  @Autowired
  SerializeCache<BulkheadPolicy> cache;

  @Override
  public Map<String, BulkheadPolicy> covert() {
    return cache.get(DynamicConfigListener.loadData(DynamicConfigListener.getBulkheadData()), BulkheadPolicy.class);
  }
}
