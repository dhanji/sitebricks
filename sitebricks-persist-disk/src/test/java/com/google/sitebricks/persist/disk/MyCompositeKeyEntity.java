package com.google.sitebricks.persist.disk;

import com.google.sitebricks.persist.Indexed;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * A test entity to roundtrip.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Entity
public class MyCompositeKeyEntity {
  @Id
  private CompositeKey id;

  @Indexed
  private String name;
  private int age;

  public CompositeKey getId() {
    return id;
  }

  public void setId(CompositeKey id) {
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

    MyCompositeKeyEntity myEntity = (MyCompositeKeyEntity) o;

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

  public static class CompositeKey implements Serializable {
    private String keyPart1;
    private String keyPart2;

    public String getKeyPart1() {
      return keyPart1;
    }

    public void setKeyPart1(String keyPart1) {
      this.keyPart1 = keyPart1;
    }

    public String getKeyPart2() {
      return keyPart2;
    }

    public void setKeyPart2(String keyPart2) {
      this.keyPart2 = keyPart2;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || !(o instanceof CompositeKey)) return false;

      CompositeKey that = (CompositeKey) o;

      if (keyPart1 != null ? !keyPart1.equals(that.keyPart1) : that.keyPart1 != null) return false;
      if (keyPart2 != null ? !keyPart2.equals(that.keyPart2) : that.keyPart2 != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = keyPart1 != null ? keyPart1.hashCode() : 0;
      result = 31 * result + (keyPart2 != null ? keyPart2.hashCode() : 0);
      return result;
    }

    @Override
    public String toString() {
      return keyPart1 + keyPart2;
    }
  }
}
