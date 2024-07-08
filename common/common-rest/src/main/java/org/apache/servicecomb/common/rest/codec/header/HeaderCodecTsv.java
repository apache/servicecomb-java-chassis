package org.apache.servicecomb.common.rest.codec.header;

public class HeaderCodecTsv extends HeaderCodecWithDelimiter {
  public static final String CODEC_NAME = "tsv";

  public static final String DELIMITER = "\t";

  public HeaderCodecTsv() {
    super(CODEC_NAME, DELIMITER, DELIMITER);
  }
}