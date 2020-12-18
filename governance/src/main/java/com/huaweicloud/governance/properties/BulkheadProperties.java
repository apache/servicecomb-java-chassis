package com.huaweicloud.governance.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.huaweicloud.governance.policy.BulkheadPolicy;

@Component
public class BulkheadProperties extends GovProperties<BulkheadPolicy> {
  public static final String MATCH_BULKHEAD__KEY = "servicecomb.bulkhead";

  public BulkheadProperties() {
    super(MATCH_BULKHEAD__KEY);
  }

  @Override
  public Map<String, BulkheadPolicy> covert(Map<String, String> properties) {
    return parseEntity(properties, BulkheadPolicy.class);
  }
}
