package com.github.gaborfeher.grantmaster.logic.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
    uniqueConstraints=
        @UniqueConstraint(columnNames={"name"}))
public class BudgetCategory implements EntityBase, Serializable {
  @Id
  @GeneratedValue
  private Integer id;
  
  @Column(nullable = false)
  private String name;
  
  public static enum Direction {
    PAYMENT,
    INCOME;
  }
  @Column(nullable = false)
  private Direction direction;
  
  @Column(nullable = true)
  private String groupName;

  public BudgetCategory() {
  }
  
  public BudgetCategory(Direction direction, String groupName, String name) {
    this.direction = direction;
    this.groupName = groupName;
    this.name = name;
  }
  
  public Direction getDirection() {
    return direction;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }
  
  @Override
  public String toString() {
    return name;
  }
}
