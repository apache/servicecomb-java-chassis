package org.apache.servicecomb.demo.jaxbbean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"name", "role", "job"})
@XmlRootElement(name = "person")
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class JAXBPerson implements Serializable {
  private static final long serialVersionUID = -7127275268696924681L;

  private String name;

  private int age;

  private String role;

  private String weight;

  private JAXBJob job;

  public JAXBPerson() {
  }

  public JAXBPerson(String name, int age, String role, String weight) {
    this.name = name;
    this.age = age;
    this.role = role;
    this.weight = weight;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @XmlAttribute
  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  @XmlElement(nillable = true)
  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  @XmlTransient
  public String getWeight() {
    return weight;
  }

  public void setWeight(String weight) {
    this.weight = weight;
  }

  @XmlElement
  public JAXBJob getJob() {
    return job;
  }

  public void setJob(JAXBJob job) {
    this.job = job;
  }

  @Override
  public String toString() {
    return "Person{" +
        "name='" + name + '\'' +
        ", age=" + age +
        ", role='" + role + '\'' +
        ", job=" + job +
        '}';
  }
}
