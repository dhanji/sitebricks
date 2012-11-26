package com.google.sitebricks.persist.redis;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Entity
public class Parameter {
  @Id
  public final String name;
  public final String value;

  public Parameter(String name, String value) {
    this.name = name;
    this.value = value;
  }
}
