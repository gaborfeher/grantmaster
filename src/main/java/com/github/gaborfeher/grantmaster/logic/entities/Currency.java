package com.github.gaborfeher.grantmaster.logic.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Currency extends EntityBase implements Serializable {
  @Id
  @GeneratedValue
  private Long id;
  
  @Column(nullable = false, unique = true)
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
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
