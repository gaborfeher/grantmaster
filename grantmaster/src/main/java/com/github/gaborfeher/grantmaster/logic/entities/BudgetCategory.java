/**
 * This file is a part of GrantMaster.
 * Copyright (C) 2015  Gábor Fehér <feherga@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.gaborfeher.grantmaster.logic.entities;

import com.github.gaborfeher.grantmaster.framework.base.EntityBase;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(
    uniqueConstraints=
        @UniqueConstraint(columnNames={"name"}))
public class BudgetCategory extends EntityBase implements Serializable {
  @Id
  @GeneratedValue
  private Long id;

  @NotNull(message="%ValidationErrorNameEmpty")
  @Size(min=1, message="%ValidationErrorNameEmpty")
  @Column(nullable = false)
  private String name;

  public static enum Direction {
    PAYMENT,
    INCOME;
  }
  @NotNull(message="%ValidationErrorDirectionEmpty")
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

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public String toString() {
    return name;
  }
}
