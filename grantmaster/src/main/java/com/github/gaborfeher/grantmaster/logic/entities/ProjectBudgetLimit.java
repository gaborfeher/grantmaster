package com.github.gaborfeher.grantmaster.logic.entities;

import com.github.gaborfeher.grantmaster.framework.base.EntityBase;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

@Entity
@Table(
    uniqueConstraints=
        @UniqueConstraint(columnNames={"budgetCategory_id", "project_id"}))
public class ProjectBudgetLimit extends EntityBase implements  Serializable {
  @Id
  @GeneratedValue
  private Long id;
  
  @Column(nullable = true, scale = 10, precision = 25)
  private BigDecimal budgetGrantCurrency;
  
  @Column(nullable = true, scale = 10, precision = 25)
  private BigDecimal budgetPercentage;
  
  @NotNull(message = "%ValidationErrorBudgetCategoryEmpty")
  @ManyToOne
  @JoinColumn(nullable = false)
  private BudgetCategory budgetCategory;
  
  @ManyToOne
  @JoinColumn(nullable = false)
  private Project project;
    
  public ProjectBudgetLimit() {
  }
  
  @AssertTrue(message="%ValidationErrorBudgetLimits")
  private boolean isValid() {
    if (budgetGrantCurrency == null && budgetPercentage == null) {
      return false;
    }
    if (budgetGrantCurrency != null &&
        budgetGrantCurrency.compareTo(BigDecimal.ZERO) < 0) {
      return false;
    }
    if (budgetPercentage != null &&
        budgetPercentage.compareTo(BigDecimal.ZERO) < 0) {
      return false;
    }
    return true;
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

  public BigDecimal getBudgetPercentage() {
    return budgetPercentage;
  }

  public void setBudgetPercentage(BigDecimal budgetPercentage) {
    this.budgetPercentage = budgetPercentage;
  }

  @Override
  public Long getId() {
    return id;
  }
}
