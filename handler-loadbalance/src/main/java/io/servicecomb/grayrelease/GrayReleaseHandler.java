package io.servicecomb.grayrelease;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.handler.impl.AbstractHandler;
import io.servicecomb.loadbalance.Configuration;
import io.servicecomb.loadbalance.LoadbalanceHandler;
import io.servicecomb.swagger.invocation.AsyncResponse;

/**
 * 灰度发布handler
 */
public class GrayReleaseHandler extends AbstractHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadbalanceHandler.class);

    //灰度发布策略
    private String policy = null;

    @Override
    public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
		// 读取规则
		String p = Configuration.INSTANCE.getGrayreleaseRuleClassName(invocation.getMicroserviceName(),
				invocation.getMicroserviceQualifiedName());
		this.policy = p;
		if (this.policy == null) {
			invocation.next(asyncResp);
		}
		IGrayReleaseFilter rule = createGrayReleaseFilterRule(invocation);
		if (rule != null) {
			rule.filterRule();
		}
		invocation.next(asyncResp);
    }

    private IGrayReleaseFilter createGrayReleaseFilterRule(Invocation invocation) {
        AbstractGrayReleaseFilter absRule = null;
        try {
            absRule = (AbstractGrayReleaseFilter) Class
                    .forName(policy, true, Thread.currentThread().getContextClassLoader()).newInstance();
            absRule.init(invocation);
            LOGGER.info("Using GrayReleaseFilterRule rule [{}] for service [{},{}].",
                    policy,
                    invocation.getMicroserviceName(),
                    invocation.getConfigTransportName());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            LOGGER.warn("GrayReleaseFilterRule rule [{}] is incorrect.", policy);
        }
        return absRule;
    }

}
