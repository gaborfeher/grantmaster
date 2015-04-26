/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.logic.entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Project extends EntityBase implements  Serializable {
  @Id
  @GeneratedValue
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @ManyToOne
  @JoinColumn(nullable = false)
  private Currency grantCurrency;

  @ManyToOne
  @JoinColumn(nullable = false)
  private Currency accountCurrency;
  
  @ManyToOne
  @JoinColumn(nullable = false)
  private BudgetCategory incomeType;

  @OneToMany(mappedBy="project")
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
