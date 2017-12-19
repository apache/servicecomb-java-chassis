package io.servicecomb.common.rest.codec;

public enum QueryTypeEnum {

  multi(0, "multi"),
  csv(1, "csv"),
  ssv(2, "ssv"),
  tsv(3, "tsv"),
  pipes(4, "pipes");

  private int value;

  private String description;

  QueryTypeEnum(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public int value() {
    return value;
  }

  public String description() {
    return description;
  }

  public static QueryTypeEnum valueOf(int value) {
    for (QueryTypeEnum typeEnum : QueryTypeEnum.values()) {
      if (typeEnum.value == value) {
        return typeEnum;
      }
    }
    return null;
  }

}
