package main;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class CheckResultEvent {

  public enum StateEnum {
    OK, // check has passed successfully.
    SUSPICIOUS, // check has found something suspicious.
    ERROR, // check has failed in a technical way.
    IGNORED; // check has not been executed, due to some pre-conditions.

    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }

  private StateEnum state;

  /** The name of the check. */
  private String name;

  /** Result details from the check. */
  private String details;

  public String getName() {
    return name;
  }

  public CheckResultEvent setName(String name) {
    this.name = name;
    return this;
  }

  public StateEnum getState() {
    return state;
  }

  public CheckResultEvent setState(StateEnum status) {
    this.state = status;
    return this;
  }

  public String getDetails() {
    return details;
  }

  public CheckResultEvent setDetails(String details) {
    this.details = details;
    return this;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
