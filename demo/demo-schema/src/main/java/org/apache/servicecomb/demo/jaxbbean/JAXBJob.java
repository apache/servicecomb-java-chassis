package org.apache.servicecomb.demo.jaxbbean;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "job")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class JAXBJob {
  private String name;

  private String content;

  public JAXBJob() {
  }

  public JAXBJob(String name, String content) {
    this.name = name;
    this.content = content;
  }

  @Override
  public String toString() {
    return "Job{" + "name'" + name + '\'' + ", content='" + content + '\'' + '}';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
