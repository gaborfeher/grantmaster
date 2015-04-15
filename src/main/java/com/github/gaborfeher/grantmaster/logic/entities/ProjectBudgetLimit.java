/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.logic.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
    uniqueConstraints=
        @UniqueConstraint(columnNames={"expenseType_id", "project_id"}))
public class ProjectBudgetLimit implements Serializable {
  @Id
  @GeneratedValue
  private int id;
  
  @Column(nullable = false)
  private double budget;
  
  @ManyToOne
  @JoinColumn(nullable = false)
  private ExpenseType expenseType;
  
  @ManyToOne
  @JoinColumn(nullable = false)
  private Project project;


  public ProjectBudgetLimit() {
  }

  public double getBudget() {
    return budget;
  }

  public void setBudget(double budget) {
    this.budget = budget;
  }


  public ExpenseType getExpenseType() {
    return expenseType;
  }

  public void setExpenseType(ExpenseType expenseType) {
    this.expenseType = expenseType;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }


}
