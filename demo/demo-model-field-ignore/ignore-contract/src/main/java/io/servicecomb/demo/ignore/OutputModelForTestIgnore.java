package io.servicecomb.demo.ignore;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class OutputModelForTestIgnore {
  @JsonIgnore
  private String outputId = null;
  private String inputId = null;
  private String content = null;

  public String getOutputId() {
    return this.outputId;
  }

  public void setOutputId(String outputId) {
    this.outputId = outputId;
  }

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

  public OutputModelForTestIgnore() {
  }

  public OutputModelForTestIgnore(String outputId, String inputId, String content) {
    this.outputId = outputId;
    this.inputId = inputId;
    this.content = content;
  }
}
