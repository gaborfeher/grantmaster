/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.logic.entities;

import com.github.gaborfeher.grantmaster.framework.base.EntityBase;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Project extends EntityBase implements  Serializable {
  @Id
  @GeneratedValue
  private Long id;

  @NotNull(message = "%ValidationErrorNameEmpty")
  @Size(min = 1, message="%ValidationErrorNameEmpty")
  @Column(nullable = false, unique = true)
  private String name;

  @NotNull(message = "%ValidationErrorGrantCurrencyEmpty")
  @ManyToOne
  @JoinColumn(nullable = false)
  private Currency grantCurrency;

  @NotNull(message = "%ValidationErrorAccountingCurrencyEmpty")
  @ManyToOne
  @JoinColumn(nullable = false)
  private Currency accountCurrency;
  
  @NotNull(message = "%ValidationErrorIncomeCategoryEmpty")
  @ManyToOne
  @JoinColumn(nullable = false)
  private BudgetCategory incomeType;

  @OneToMany(mappedBy = "project")
  private List<ProjectReport> reports;
  
  public Project() {
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
    return getName();
  }

  public Currency getGrantCurrency() {
    return grantCurrency;
  }

  public void setGrantCurrency(Currency grantCurrency) {
    this.grantCurrency = grantCurrency;
  }

  public Currency getAccountCurrency() {
    return accountCurrency;
  }

  public void setAccountCurrency(Currency accountCurrency) {
    this.accountCurrency = accountCurrency;
  }

  public BudgetCategory getIncomeType() {
    return incomeType;
  }

  public void setIncomeType(BudgetCategory incomeType) {
    this.incomeType = incomeType;
  }

  public List<ProjectReport> getReports() {
    return reports;
  }

  public void setReports(List<ProjectReport> reports) {
    this.reports = reports;
  }
}
