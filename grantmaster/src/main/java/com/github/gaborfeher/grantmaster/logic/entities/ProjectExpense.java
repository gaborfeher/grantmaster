/*
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

@Entity
public class ProjectExpense extends EntityBase implements  Serializable {
  @Id
  @GeneratedValue
  private long id;
  
  @ManyToOne(optional = false)
  @JoinColumn(nullable = false)
  private Project project;
  
  @NotNull(message="%ValidationErrorPaymentDateEmpty")
  @Column(nullable = false)
  @Temporal(TemporalType.DATE)
  private LocalDate paymentDate;
  
  /**
   * The report in which this expense will be included.
   */
  @NotNull(message="%ValidationErrorReportEmpty")
  @ManyToOne(optional = false)
  @JoinColumn(nullable = false)
  private ProjectReport report;
  
  private String accountNo;
  private String partnerName;
  
  @NotNull(message="%ValidationErrorBudgetCategoryEmpty")
  @ManyToOne(optional = false)
  @JoinColumn(nullable = false)
  private BudgetCategory budgetCategory;
  
  @NotNull(message="%ValidationErrorExpenseOriginalAmount")
  @DecimalMin(value="0.00", message="%ValidationErrorExpenseOriginalAmount")
  @Column(nullable = false, scale = 10, precision = 25)
  private BigDecimal originalAmount;
  
  @ManyToOne(optional = false)
  @JoinColumn(nullable = false)
  private Currency originalCurrency;
  
  /**
   * This list describes the money used for paying this expense: income
   * sources and income amounts. In normal mode, exchange rate, accounting
   * currency amount and grant currency amount can be computed from this date.
   * In override mode this should be empty.
   */
  @OneToMany(mappedBy="expense", cascade=CascadeType.ALL, orphanRemoval = true)
  private List<ExpenseSourceAllocation> sourceAllocations;
  
  /**
   * There are two modes of specifying an expense: normal and override.
   * If this field is null, then the mode is normal.
   * If this field is not null, then the mode is override, and this field
   * specifies the exchange rate, and accountingCurrencyAmountOverride specifies
   * the accounting currency amount.
   */
  private BigDecimal exchangeRateOverride;  // optional

  /**
   * See exchangeRateOverride.
   */
  private BigDecimal accountingCurrencyAmountOverride;  // optional

  private String comment1;
  
  private String comment2;
  
  public ProjectExpense() {
  }
  
  @Override
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDate getPaymentDate() {
    return paymentDate;
  }

  public void setPaymentDate(LocalDate paymentDate) {
    this.paymentDate = paymentDate;
  }

  public String getAccountNo() {
    return accountNo;
  }

  public void setAccountNo(String accountNo) {
    this.accountNo = accountNo;
  }

  public String getPartnerName() {
    return partnerName;
  }

  public void setPartnerName(String partnerName) {
    this.partnerName = partnerName;
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

  public BigDecimal getOriginalAmount() {
    return originalAmount;
  }

  public void setOriginalAmount(BigDecimal originalAmount) {
    this.originalAmount = originalAmount;
  }

  public Currency getOriginalCurrency() {
    return originalCurrency;
  }

  public void setOriginalCurrency(Currency originalCurrency) {
    this.originalCurrency = originalCurrency;
  }

  public List<ExpenseSourceAllocation> getSourceAllocations() {
    return sourceAllocations;
  }

  public void setSourceAllocations(List<ExpenseSourceAllocation> sources) {
    this.sourceAllocations = sources;
  }

  public String getComment1() {
    return comment1;
  }

  public void setComment1(String comment1) {
    this.comment1 = comment1;
  }

  public String getComment2() {
    return comment2;
  }

  public void setComment2(String comment2) {
    this.comment2 = comment2;
  }

  public ProjectReport getReport() {
    return report;
  }

  public void setReport(ProjectReport report) {
    this.report = report;
  }

  public BigDecimal getExchangeRateOverride() {
    return exchangeRateOverride;
  }

  public void setExchangeRateOverride(BigDecimal exchangeRateOverride) {
    this.exchangeRateOverride = exchangeRateOverride;
  }

  public BigDecimal getAccountingCurrencyAmountOverride() {
    return accountingCurrencyAmountOverride;
  }

  public void setAccountingCurrencyAmountOverride(BigDecimal accountingCurrencyAmountOverride) {
    this.accountingCurrencyAmountOverride = accountingCurrencyAmountOverride;
  }
}
