package io.servicecomb.grayrelease.csefilter;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.servicecomb.grayrelease.GrayReleaseRulePolicy;

public class GrayReleaseRatePolicyFilter extends AbstractCseRuleGrayReleaseFilter {

    private Map<String, String> instanceChooser;

    private static final int RATE_TOTAL = 100;

    private static final String GRAY_RULE_TYPE_RATE = "rate";

    @Override
    protected boolean isReqCompare() {
        return false;
    }

    @Override
    public void fillGrayRules() {
        super.fillGrayRules();
        completeRules();
    }

    @Override
    public String grayChooseForGroupIdByRules() {
        createInstanceChooser();
        return chooseCompute();
    }

    private void completeRules() {
        int rateSum = 0;
        for (GrayReleaseRulePolicy grayReleaseRulePolicy : grayRules) {
            String percentageStr = grayReleaseRulePolicy.getPolicy();
            int percentage = Integer.parseInt(percentageStr);
            rateSum += percentage;
        }
        if (rateSum < RATE_TOTAL) {
            GrayReleaseRulePolicy grayReleaseRulePolicy = new GrayReleaseRulePolicy(
                    DEFAULT_INSTANCE_GROUP,
                    GRAY_RULE_TYPE_RATE,
                    String.valueOf(RATE_TOTAL - rateSum));
            grayRules.add(grayReleaseRulePolicy);

        }
    }

    private void createInstanceChooser() {
        int chooserIndex = 1;
        instanceChooser = new HashMap<String, String>();
        for (GrayReleaseRulePolicy grayReleaseRulePolicy : grayRules) {
            String percentageStr = grayReleaseRulePolicy.getPolicy();
            int percentage = Integer.parseInt(percentageStr);
            String chooserStr = new String();
            for (int i = 0; i < percentage; i++) {
                chooserStr = chooserStr + "||" + String.valueOf(chooserIndex);
                chooserIndex++;
            }
            instanceChooser.put(chooserStr, grayReleaseRulePolicy.getGroupName());
        }
    }

    private String chooseCompute() {
        String chosenGroup = null;
        Random random = new Random();
        String chooseStr = String.valueOf(random.nextInt(RATE_TOTAL));
        for (Map.Entry<String, String> entry : instanceChooser.entrySet()) {
            String chooserStr = entry.getKey();
            if (chooserStr.indexOf(chooseStr) != -1) {
                chosenGroup = entry.getValue();
                break;
            }
        }
        return chosenGroup;
    }

}
