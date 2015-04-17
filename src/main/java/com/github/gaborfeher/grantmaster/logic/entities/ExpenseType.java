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
public class ExpenseType implements Serializable {
  @Id
  @GeneratedValue
  private Integer id;
  
  @Column(nullable = false)
  private String name;

  /**
   * @return the direction
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * @param direction the direction to set
   */
  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  /**
   * @return the groupName
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * @param groupName the groupName to set
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }
  
  public static enum Direction {
    PAYMENT,
    INCOME;
  }
  @Column(nullable = false)
  private Direction direction;
  
  @Column(nullable = true)
  private String groupName;

  public ExpenseType() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getId() {
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
