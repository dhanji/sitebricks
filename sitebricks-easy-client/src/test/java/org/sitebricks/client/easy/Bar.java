package org.sitebricks.client.easy;

import java.util.Date;

public class Bar {
  private String time;

  public Bar() {
    setTime(new Date().toString());
  }

  public void setTime(final String time) {
    this.time = time;
  }

  public String getTime() {
    return time;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((time == null) ? 0 : time.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Bar other = (Bar) obj;
    if (time == null) {
      if (other.time != null) {
        return false;
      }
    } else if (!time.equals(other.time)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Bar [time=" + time + "]";
  }

}
