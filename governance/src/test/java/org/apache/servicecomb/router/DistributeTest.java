package org.apache.servicecomb.router;


import org.apache.servicecomb.router.distribute.RouterDistributor;
import org.apache.servicecomb.router.model.PolicyRuleItem;
import org.apache.servicecomb.router.model.RouteItem;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/*.xml", initializers = ConfigDataApplicationContextInitializer.class)
public class DistributeTest {
  private static final String TARGET_SERVICE_NAME = "test_server";
  private RouterDistributor<ServiceIns,ServiceIns> testDistributor;
  private RouterFilter routerFilter;
  private RouterDistributor<ServiceIns, ServiceIns> routerDistributor;


  @Autowired
  public void setTestDistributor(RouterDistributor<ServiceIns, ServiceIns> testDistributor) {
    this.testDistributor = testDistributor;
  }
  @Autowired
  public void setRouterFilter(RouterFilter routerFilter) {
      this.routerFilter = routerFilter;
  }
  @Autowired
  public void setRouterDistributor(RouterDistributor<ServiceIns, ServiceIns> routerDistributor) {
      this.routerDistributor = routerDistributor;
  }

  @Test
    public void testDistribute(){

    PolicyRuleItem policyRuleItem = initPolicyRuleItem();
    List<ServiceIns> list = initServiceList();
    HashMap<String, String> header = new HashMap<>();
    List<ServiceIns> listOfServers = routerFilter.getFilteredListOfServers(list, TARGET_SERVICE_NAME, header, routerDistributor);
    Assert.assertNotNull(listOfServers);
  }


   PolicyRuleItem initPolicyRuleItem(){
    /* 1. 模拟初始化RouteRules
    *  2.
    * */
    PolicyRuleItem policyRuleItem = new PolicyRuleItem();
    policyRuleItem.setPrecedence(2);
    RouteItem routeItem1 = new RouteItem();
    RouteItem routeItem2 = new RouteItem();
    routeItem1.setWeight(20);
    routeItem2.setWeight(80);
    HashMap<String, String> tags1 = new HashMap<>();
    HashMap<String, String> tags2 = new HashMap<>();
    tags1.put("x-group","red");
    tags2.put("x-group","green");
    routeItem1.setTags(tags1);
    routeItem1.initTagItem();
    routeItem2.setTags(tags2);
    routeItem2.initTagItem();
    ArrayList<RouteItem> routeItemArrayList = new ArrayList<>();
    routeItemArrayList.add(routeItem1);
    routeItemArrayList.add(routeItem2);
    policyRuleItem.setRoute(routeItemArrayList);
    System.out.println(policyRuleItem);
    return policyRuleItem;
  }

    List<ServiceIns> initServiceList(){
        ServiceIns serviceIns1 = new ServiceIns("01", "test_server");
        ServiceIns serviceIns2 = new ServiceIns("02", "test_server");
        serviceIns1.setVersion("1.0");
        serviceIns2.setVersion("2.0");
        serviceIns1.addTags("x-group","red");
        serviceIns2.addTags("x-group","green");
        List<ServiceIns> list = new ArrayList<>();
        list.add(serviceIns1);
        list.add(serviceIns2);
        System.out.println(list);
        return list;
    }

}
