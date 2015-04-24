package com.github.gaborfeher.grantmaster.logic.entities;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Currency implements Serializable {
  @Id
  private String code;
  
  public Currency() {
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
  
  @Override
  public String toString() {
    return code;
  }

  @Override
  public boolean equals(Object other) {
    return other != null && (other instanceof Currency) && ((Currency)other).getCode().equals(getCode());
  }

  @Override
  public int hashCode() {
    return code.hashCode();
  }
}
