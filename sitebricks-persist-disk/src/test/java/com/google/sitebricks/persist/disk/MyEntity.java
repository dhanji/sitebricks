package com.google.sitebricks.persist.disk;

import com.google.sitebricks.persist.Indexed;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * A test entity to roundtrip.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Entity
public class MyEntity {
  @Id
  private Integer id;

  @Indexed
  private String name;
  private int age;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MyEntity myEntity = (MyEntity) o;

    if (age != myEntity.age) return false;
    if (id != null ? !id.equals(myEntity.id) : myEntity.id != null) return false;
    if (name != null ? !name.equals(myEntity.name) : myEntity.name != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + age;
    return result;
  }
}
