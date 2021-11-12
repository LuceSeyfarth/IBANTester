package main;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class CheckEvent {

  public static final String HEADER_REQUESTER_SERVICE = "requester_service";

  private String url;

  private String fileType;

  public String getUrl() {
    return url;
  }

  public CheckEvent setUrl(String url) {
    this.url = url;
    return this;
  }

  public String getFileType() {
    return fileType;
  }

  public CheckEvent setFileType(String fileType) {
    this.fileType = fileType;
    return this;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
