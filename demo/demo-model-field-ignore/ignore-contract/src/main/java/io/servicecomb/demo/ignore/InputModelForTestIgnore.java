package io.servicecomb.demo.ignore;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class InputModelForTestIgnore {
  @JsonIgnore
  private String inputId = null;

  private String content = null;

  public String getInputId() {
    return this.inputId;
  }

  public void setInputId(String inputId) {
    this.inputId = inputId;
  }

  public String getContent() {
    return this.content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public InputModelForTestIgnore() {
  }

  public InputModelForTestIgnore(String inputId, String content) {
    this.inputId = inputId;
    this.content = content;
  }
}
