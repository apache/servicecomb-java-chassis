package org.apache.servicecomb.swagger.generator.core;

import java.util.List;

/**
 * Created by Administrator on 2019/8/25.
 */
public class MyEndpoint2 implements IMyService {
  @Override
  public PersonBean hello(PersonBean a) {
    return null;
  }

  @Override
  public PersonBean[] helloBody(PersonBean[] a) {
    return new PersonBean[0];
  }

  @Override
  public List<PersonBean> helloList(List<PersonBean> a) {
    return null;
  }

  @Override
  public PersonBean actual() {
    return null;
  }
}
