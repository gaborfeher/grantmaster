package com.github.gaborfeher.grantmaster.logic.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
    uniqueConstraints=
        @UniqueConstraint(columnNames={"budgetCategory_id", "project_id"}))
public class ProjectBudgetLimit implements EntityBase, Serializable {
  @Id
  @GeneratedValue
  private int id;
  
  @Column(nullable = true, scale = 10, precision = 25)
  private BigDecimal budgetGrantCurrency;
  
  @Column(nullable = true, scale = 10, precision = 25)
  private BigDecimal budgetPercentage;
  
  @ManyToOne
  @JoinColumn(nullable = false)
  private BudgetCategory budgetCategory;
  
  @ManyToOne
  @JoinColumn(nullable = false)
  private Project project;
    
  public ProjectBudgetLimit() {
  }

  public BigDecimal getBudgetGrantCurrency() {
    return budgetGrantCurrency;
  }

  public void setBudgetGrantCurrency(BigDecimal budget) {
    this.budgetGrantCurrency = budget;
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
  public BigDecimal getBudgetPercentage() {
    return budgetPercentage;
  }

  /**
   * @param budgetPercentage the budgetPercentage to set
   */
  public void setBudgetPercentage(BigDecimal budgetPercentage) {
    this.budgetPercentage = budgetPercentage;
  }

  public Integer getId() {
    return id;
  }
}
