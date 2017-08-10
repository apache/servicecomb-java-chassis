package io.servicecomb.demo.pojo.client;

import io.servicecomb.demo.server.Test;
import io.servicecomb.provider.pojo.RpcReference;
import org.springframework.stereotype.Component;

/**
 * Created by l00168639 on 2017/8/10.
 */
public class BeanRpcTest {
  @RpcReference(microserviceName = "pojo")
  private Test test;

  public BeanRpcTest() {
    System.out.println("init");
  }

  public void init() {
    new Thread() {
      public void run() {
        while(true) {
          try {
            System.out.println("XXXXXXXXXXXXXXXXXXXXXXXX" + test.getTestString(null));
            break;
          } catch (Exception e) {
            e.printStackTrace();
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e1) {
              e1.printStackTrace();
            }
            continue;
          }
        }
      }
    }.start();

  }
}
