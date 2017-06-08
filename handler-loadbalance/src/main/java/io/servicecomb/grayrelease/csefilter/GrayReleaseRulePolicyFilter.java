package io.servicecomb.grayrelease.csefilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.servicecomb.grayrelease.GrayReleaseRulePolicy;

public class GrayReleaseRulePolicyFilter extends AbstractCseRuleGrayReleaseFilter {

    @Override
    protected String grayChooseForGroupIdByRules() {

        String chosenGroup = null;

        for (GrayReleaseRulePolicy grayReleaseRulePolicy : grayRules) {
            String groupId = grayReleaseRulePolicy.getGroupName();
            String policy = grayReleaseRulePolicy.getPolicy();

            Map<String, Object> grayRuleDetails = parseStrRule(policy);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> subDetailList =
                (List<Map<String, Object>>) grayRuleDetails.get(RULE_DETAILS_OBJECTLIST_NAME);

            int isCompareCount = 0;
            for (Map<String, Object> ruleDetail : subDetailList) {

                if (pramsCompare((String) ruleDetail.get(RULE_DETAILS_NAME_KEY),
                        (LogicSymbol) ruleDetail.get(OPERATOR_NAME),
                        (String) ruleDetail.get(RULE_DETAILS_VALUE_KEY))) {
                    isCompareCount++;
                }
            }

            RelateSymbol operator = (RelateSymbol) grayRuleDetails.get(OPERATOR_NAME);
            if (operator == null) {
                if (grayRuleDetails.size() >= 2) {
                    operator = RelateSymbol.And;
                } else {
                    operator = RelateSymbol.Or;
                }
            }
            if (operator == RelateSymbol.And) {
                if (isCompareCount == grayRuleDetails.size()) {
                    chosenGroup = groupId;
                    break;
                }
            } else if (operator == RelateSymbol.Or) {
                if (isCompareCount > 0) {
                    chosenGroup = groupId;
                    break;
                }
            }
        }
        return chosenGroup;
    }

    private boolean pramsCompare(String paramName, LogicSymbol operator, String standValue) {
        boolean isCompare = false;
        Object curValue = reqParams.get(paramName);
        if (curValue == null) {
            isCompare = false;
        } else {
            isCompare = compare(curValue, operator, standValue);
        }
        return isCompare;
    }

    private boolean compare(Object curValue, LogicSymbol operator, String standValue) {

        switch (operator) {
            case Larger:
                return Double.valueOf((String) curValue) > Double.valueOf(standValue);

            case Smaller:
                return Double.valueOf((String) curValue) < Double.valueOf(standValue);

            case Equal:
                return standValue.equals((String) curValue);

            case NotEqual:
                return !standValue.equals((String) curValue);

            case LargerOrEqual:
                return (Double.valueOf((String) curValue) >= Double.valueOf(standValue));

            case SmallerOrEqual:
                return (Double.valueOf((String) curValue) <= Double.valueOf(standValue));

            case Like:
                return isFormat((String) curValue, standValue);

            default:
                return false;
        }
    }

    private boolean isFormat(String curValue, String standValue) {
        boolean isFormat = false;
        List<String> standSubList = new ArrayList<String>();
        char[] standCh = standValue.toCharArray();
        int idx = 0;
        int bidx = 0;
        for (char tCh : standCh) {
            if (tCh == '*' || tCh == '?') {
                if (bidx < idx) {
                    String subStr = standValue.substring(bidx, idx);
                    standSubList.add(subStr);
                }
                standSubList.add(String.valueOf(tCh));
                bidx = idx + 1;
            }
            if (idx == standCh.length - 1 && bidx < standCh.length) {
                String subStr = standValue.substring(bidx, standCh.length);
                standSubList.add(subStr);
            }
            idx++;
        }
        StringBuffer comStr = new StringBuffer(curValue);
        for (int i = 0; i < standSubList.size(); i++) {
            String subStr = standSubList.get(i);
            if ((!"*".equals(subStr)) && (!"?".equals(subStr))) {
                int length = subStr.length();
                String comSubStr = comStr.substring(0, length);
                if (comSubStr.equals(subStr)) {
                    comStr.delete(0, length);
                } else {
                    return false;
                }
            } else if ("*".equals(subStr)) {
                int m = findNextContentSubStrIndex(standSubList, i);
                if (m == -1) {
                    comStr.delete(0, comStr.length());
                } else {
                    String str = standSubList.get(m);
                    if (comStr.indexOf(str) != -1) {
                        comStr.delete(0, comStr.lastIndexOf(str) + str.length());
                        i = m;
                    } else {
                        isFormat = false;
                        break;
                    }
                }
            } else if ("?".equals(subStr)) {
                comStr.delete(0, 1);
            }
        }
        if (!"".equals(comStr.toString())) {
            isFormat = false;
        } else {
            isFormat = true;
        }
        return isFormat;
    }

    private int findNextContentSubStrIndex(List<String> standSubList, int idx) {

        int indx = -1;
        for (int i = idx; i < standSubList.size(); i++) {
            String subStr = standSubList.get(i);
            if ((!"*".equals(subStr)) && (!"?".equals(subStr))) {

                return i;
            }
        }
        return indx;
    }

    @Override
    protected boolean isReqCompare() {
        return true;
    }

}
