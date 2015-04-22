/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.logic.entities;

import java.io.Serializable;
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
        @UniqueConstraint(columnNames={"budgetCategory_id", "project_id"}))
public class ProjectBudgetLimit implements EntityBase, Serializable {
  @Id
  @GeneratedValue
  private int id;
  
  private Double budget;
  
  private Double budgetPercentage;
  
  @ManyToOne
  @JoinColumn(nullable = false)
  private BudgetCategory budgetCategory;
  
  @ManyToOne
  @JoinColumn(nullable = false)
  private Project project;


  public ProjectBudgetLimit() {
  }

  public Double getBudgetGrantCurrency() {
    return budget;
  }

  public void setBudgetGrantCurrency(Double budget) {
    this.budget = budget;
  }

  public BudgetCategory getBudgetCategory() {
    return budgetCategory;
  }

  public void setBudgetCategory(BudgetCategory budgetCategory) {
    this.budgetCategory = budgetCategory;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  /**
   * @return the budgetPercentage
   */
  public Double getBudgetPercentage() {
    return budgetPercentage;
  }

  /**
   * @param budgetPercentage the budgetPercentage to set
   */
  public void setBudgetPercentage(Double budgetPercentage) {
    this.budgetPercentage = budgetPercentage;
  }

  public Integer getId() {
    return id;
  }
}
