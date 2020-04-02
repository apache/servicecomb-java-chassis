/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.it.schema;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonView;

public class PersonViewModel {
  public interface Summary {
  }

  public interface SummaryWithDetails extends Summary {
  }

  @JsonView(Summary.class)
  private String name;

  @JsonView(SummaryWithDetails.class)
  private int age;

  @JsonView(Summary.class)
  private String emails;

  @JsonView(SummaryWithDetails.class)
  private String telephone;

  private double rate;

  public String getName() {
    return name;
  }

  public PersonViewModel setName(String name) {
    this.name = name;
    return this;
  }

  public int getAge() {
    return age;
  }

  public PersonViewModel setAge(int age) {
    this.age = age;
    return this;
  }

  public String getEmails() {
    return emails;
  }

  public PersonViewModel setEmails(String emails) {
    this.emails = emails;
    return this;
  }

  public String getTelephone() {
    return telephone;
  }

  public PersonViewModel setTelephone(String telephone) {
    this.telephone = telephone;
    return this;
  }

  public double getRate() {
    return rate;
  }

  public PersonViewModel setRate(double rate) {
    this.rate = rate;
    return this;
  }

  public static PersonViewModel generatePersonViewModel() {
    return new PersonViewModel().setAge(12)
        .setEmails("xxx@servicecomb.com")
        .setName("servicecomb")
        .setRate(99.9)
        .setTelephone("xxx10--xx");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PersonViewModel that = (PersonViewModel) o;
    return age == that.age &&
        Double.compare(that.rate, rate) == 0 &&
        Objects.equals(name, that.name) &&
        Objects.equals(emails, that.emails) &&
        Objects.equals(telephone, that.telephone);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, age, emails, telephone, rate);
  }

  @Override
  public String toString() {
    return "PersonViewModel{" +
        "name='" + name + '\'' +
        ", age=" + age +
        ", emails='" + emails + '\'' +
        ", telephone='" + telephone + '\'' +
        ", rate=" + rate +
        '}';
  }
}
