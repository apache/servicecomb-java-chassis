package io.servicecomb.grayrelease;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.core.Invocation;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

public abstract class AbstractGrayReleaseFilter implements IGrayReleaseFilter {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractGrayReleaseFilter.class);

    protected Invocation invocation;

    private Map<String, Map<String, MicroserviceInstance>> grayInstanceMap;

    /**
     * instance分组规则 map结构，key是groupId,value是分组规则对象
     */
    /*
     * protected Map<String, Object> groupRules;
     */
    protected List<GrayReleaseGroupPolicy> groupRules;

    protected static final String DEFAULT_INSTANCE_GROUP = "graydefaultgroup";

    protected static final String INSTANCE_GROUP_TAG_KEY = "tags";

    protected static final String INSTANCE_GROUP_VERSION_KEY = "version";

    protected static final String OPERATOR_NAME = "operator";

    protected static final String DEFAULT_MICROSERVICE_VERSION = "microservice.default.version";

    protected static final String GROUP_RULE_NAME_KEY = "name";

    protected static final String GROUP_RULE_RULE_KEY = "rule";

    protected static final String GRAY_RULE_GROUP_KEY = "group";

    protected static final String GRAY_RULE_TYPE_KEY = "type";

    protected static final String GRAY_RULE_POLICY_KEY = "policy";

    protected static final String NULL_STR = "NULL";

    /**
     * 灰度发布匹配规则 map结构，key是groupId,value是规则对象内容
     */
    /* protected Map<String, Object> rules; */
    protected List<GrayReleaseRulePolicy> grayRules;

    /**
     * 请求参数列表 map,key,value分别是参数名和参数对象值
     */
    protected Map<String, Object> reqParams;

    public enum InstanceScope {
        All, VersionRule, PartVersion
    }

    protected InstanceScope instanceScope = InstanceScope.All;

    protected List<String> versions;

    protected void setVersions(List<String> versions) {
        this.versions = versions;
    }

    public Invocation getInvocation() {
        return invocation;
    }

    public Map<String, Map<String, MicroserviceInstance>> getGrayInstanceMap() {
        return grayInstanceMap;
    }

    /**
     * 获取groupRules的值
     * 
     * @return 返回 groupRules
     */
    public List<GrayReleaseGroupPolicy> getGroupRules() {
        return groupRules;
    }

    /**
     * 获取grayRules的值
     * 
     * @return 返回 grayRules
     */
    public List<GrayReleaseRulePolicy> getGrayRules() {
        return grayRules;
    }

    /**
     * 对grayRules进行赋值
     * 
     * @param grayRules
     *            grayRules的新值
     */
    public void setGrayRules(List<GrayReleaseRulePolicy> grayRules) {
        this.grayRules = grayRules;
    }

    /**
     * 对groupRules进行赋值
     * 
     * @param groupRules
     *            groupRules的新值
     */
    public void setGroupRules(List<GrayReleaseGroupPolicy> groupRules) {
        this.groupRules = groupRules;
    }

    public void init(Invocation invocation) {
        this.invocation = invocation;
    }

    @Override
    public void filterRule() {
        try {
            // 从Config中读取分组规则
            fillGroupRules();
            if (groupRules == null) {
                return;
            }
            // 从Config中读取灰度规则
            fillGrayRules();
            if (grayRules == null) {
                return;
            }
            if (isReqCompare()) {
                fillReqParams();
            }

            String chosenGroup = null;
            try {
                defineInstanceScope();
                fillInstanceGroup();
                chosenGroup = grayChooseForGroupIdByRules();
            } catch (Exception e) {
                LOGGER.error(this.getClass().getName() + ".filterRule  error:" + e);
                chosenGroup = null;
            }
            updateInstanceCache(chosenGroup);
        } catch (Exception e) {
            LOGGER.error(this.getClass().getName() + ".filterRule  error:" + e);
            return;
        }
    }

    public Map<String, Object> getReqParams() {
        return reqParams;
    }

    // 灰度发布规则是否需要请求参数作匹配。
    // 例：只需要按照请求占比作灰度，则不需要请求参数参与，返回false即可。如需要依据某个参数作对比来作灰度，则需要返回true
    protected abstract boolean isReqCompare();

    /**
     * 填充灰度发布规则 将灰度发布规则写入rules字段，按map结构写入，key为groupId，value为规则内容对象
     */
    protected abstract void fillGrayRules();

    /**
     * 填充instance分组规则
     * 将instance分组规则写入groupRules字段。按map结构写入，key是groupId,value是分组规则对象
     */
    protected abstract void fillGroupRules();

    protected abstract void defineInstanceScope();

    protected void setInstanceScope(InstanceScope instanceScope) {
        this.instanceScope = instanceScope;
    }

    private void fillReqParams() {
        int size = invocation.getOperationMeta().getParamSize();
        Object[] values = invocation.getArgs();
        reqParams = new HashMap<String, Object>();
        for (int i = 0; i < size; i++) {
            reqParams.put(invocation.getOperationMeta().getParamName(i), values[i]);
        }
    }

    // 根据灰度发布规则、分组规则以及请求参数返回所匹配的GroupId
    protected abstract String grayChooseForGroupIdByRules();

    protected void fillInstanceGroup() {

        Map<String, Map<String, MicroserviceInstance>> instanceMap = null;

        switch (instanceScope) {

            case All:
                instanceMap = RegistryUtils.getInstanceVersionCacheManager().getOrCreateAllMap(invocation.getAppId(),
                        invocation.getMicroserviceName());
                break;

            case VersionRule:
                instanceMap =
                    RegistryUtils.getInstanceVersionCacheManager().getOrCreateVRuleMap(invocation.getAppId(),
                            invocation.getMicroserviceName(),
                            invocation.getMicroserviceVersionRule());
                break;

            case PartVersion:

                Map<String, Map<String, MicroserviceInstance>> allInstance =
                    RegistryUtils.getInstanceVersionCacheManager().getOrCreateAllMap(
                            invocation.getAppId(), invocation.getMicroserviceName());
                instanceMap = new HashMap<String, Map<String, MicroserviceInstance>>();
                for (String version : versions) {
                    instanceMap.put(version, allInstance.get(version));
                }
                break;

            default:
                instanceMap =
                    RegistryUtils.getInstanceVersionCacheManager().getOrCreateVRuleMap(invocation.getAppId(),
                            invocation.getMicroserviceName(),
                            invocation.getMicroserviceVersionRule());
                break;

        }
        groupInstance(instanceMap);
    }

    // 更新instance缓存 <功能详细描述>如果没有匹配到灰度规则，instance的范围按照versionrule的控制规则
    protected void updateInstanceCache(String chosenGroup) {
        Map<String, MicroserviceInstance> chosenMap;
        if (chosenGroup == null || "".equals(chosenGroup)) {
            chosenMap = null;
        } else {
            if (!DEFAULT_INSTANCE_GROUP.equals(chosenGroup)) {
                chosenMap = grayInstanceMap.get(chosenGroup);
            } else {
                Map<String, MicroserviceInstance> tmpChosenMap = new HashMap<String, MicroserviceInstance>();
                for (Map.Entry<String, Map<String, MicroserviceInstance>> insGroup : grayInstanceMap.entrySet()) {
                    String insGroupName = insGroup.getKey();
                    if (DEFAULT_INSTANCE_GROUP.equals(insGroupName)) {
                        tmpChosenMap.putAll(insGroup.getValue());
                    } else {
                        boolean isRemark = false;
                        for (GrayReleaseRulePolicy grayReleaseRulePolicy : grayRules) {
                            if (grayReleaseRulePolicy.getGroupName().equals(insGroupName)) {
                                isRemark = true;
                                break;
                            }
                        }
                        if (!isRemark) {
                            tmpChosenMap.putAll(insGroup.getValue());
                        }
                    }
                }
                chosenMap = tmpChosenMap;
            }

        }
        if (chosenMap == null) {
            Map<String, Map<String, MicroserviceInstance>> defaultInstanceVersionMap =
                RegistryUtils.getInstanceVersionCacheManager()
                        .getOrCreateVRuleMap(invocation.getAppId(),
                                invocation.getMicroserviceName(),
                                invocation.getMicroserviceVersionRule());
            Map<String, MicroserviceInstance> defaultInstanceMap = new HashMap<String, MicroserviceInstance>();
            for (Map.Entry<String, Map<String, MicroserviceInstance>> defaultInstanceVersion : defaultInstanceVersionMap
                    .entrySet()) {
                defaultInstanceMap.putAll(defaultInstanceVersion.getValue());
            }
            chosenMap = defaultInstanceMap;
        }

        // bug:取消灰度后，无法恢复，已经通知相关人员修改
        RegistryUtils.getInstanceCacheManager().updateInstanceMap(invocation.getAppId(),
                invocation.getMicroserviceName(),
                invocation.getMicroserviceVersionRule(),
                chosenMap);
    }

    private void groupInstance(Map<String, Map<String, MicroserviceInstance>> instanceMap) {
        grayInstanceMap = new HashMap<String, Map<String, MicroserviceInstance>>();
        for (Map.Entry<String, Map<String, MicroserviceInstance>> instanceVersionMap : instanceMap.entrySet()) {
            String version = instanceVersionMap.getKey();
            Map<String, MicroserviceInstance> instances = instanceVersionMap.getValue();
            for (Map.Entry<String, MicroserviceInstance> instanceEntry : instances.entrySet()) {
                String instanceId = instanceEntry.getKey();
                MicroserviceInstance instance = instanceEntry.getValue();
                if (DEFAULT_MICROSERVICE_VERSION.equals(version) && instance.getProperties().isEmpty()) {
                    if (grayInstanceMap.get(DEFAULT_INSTANCE_GROUP) == null) {
                        Map<String, MicroserviceInstance> defaultMap = new HashMap<String, MicroserviceInstance>();
                        defaultMap.put(instanceId, instance);
                        grayInstanceMap.put(DEFAULT_INSTANCE_GROUP, defaultMap);
                    } else {
                        Map<String, MicroserviceInstance> defaultMap = grayInstanceMap.get(DEFAULT_INSTANCE_GROUP);
                        defaultMap.put(instanceId, instance);
                    }
                } else {
                    String tags = NULL_STR;
                    if (instance.getProperties().get(INSTANCE_GROUP_TAG_KEY) != null
                            && !("".equals(instance.getProperties().get(INSTANCE_GROUP_TAG_KEY)))) {
                        tags = instance.getProperties().get(INSTANCE_GROUP_TAG_KEY);
                    }

                    String groupName = getGroupNameByGroupRule(tags, version);
                    if (grayInstanceMap.get(groupName) == null) {
                        Map<String, MicroserviceInstance> members = new HashMap<String, MicroserviceInstance>();
                        members.put(instanceId, instance);
                        grayInstanceMap.put(groupName, members);
                    } else {
                        Map<String, MicroserviceInstance> members = grayInstanceMap.get(groupName);
                        members.put(instanceId, instance);
                    }
                }
            }
        }
    }

    protected abstract String getGroupNameByGroupRule(String tag, String version);

}
