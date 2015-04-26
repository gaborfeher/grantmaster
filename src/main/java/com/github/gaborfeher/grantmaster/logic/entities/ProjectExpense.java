package com.github.gaborfeher.grantmaster.logic.entities;

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
import javax.persistence.Transient;

@Entity
public class ProjectExpense extends EntityBase implements  Serializable {
  @Id
  @GeneratedValue
  private long id;
  
  @ManyToOne(optional = false)
  @JoinColumn(nullable = false)
  private Project project;
  
  @Column(nullable = false)
  @Temporal(TemporalType.DATE)
  private LocalDate paymentDate;
  
  /**
   * The report in which this expense will be included.
   */
  @ManyToOne(optional = false)
  @JoinColumn(nullable = false)
  private ProjectReport report;
  
  private String accountNo;
  private String partnerName;
  
  @ManyToOne(optional = false)
  @JoinColumn(nullable = false)
  private BudgetCategory budgetCategory;
  
  @Column(nullable = false, scale = 10, precision = 25)
  private BigDecimal originalAmount;
  
  @ManyToOne(optional = false)
  @JoinColumn(nullable = false)
  private Currency originalCurrency;
  
  /**
   * This list describes the money used for paying this expense: income
   * sources and income amounts.
   */
  @OneToMany(mappedBy="expense", cascade=CascadeType.ALL, orphanRemoval = true)
  private List<ExpenseSourceAllocation> sourceAllocations;

  private String comment1;
  
  private String comment2;
  
  @Transient
  private BigDecimal accountingCurrencyAmount;
  
  @Transient
  private BigDecimal accountingCurrencyAmountNotEdited;
  
  @Transient
  private BigDecimal grantCurrencyAmount;
  
  @Transient
  private BigDecimal exchangeRate;
  
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

  public BigDecimal getAccountingCurrencyAmount() {
    return accountingCurrencyAmount;
  }

  public void setAccountingCurrencyAmount(BigDecimal accountingCurrencyAmount) {
    this.accountingCurrencyAmount = accountingCurrencyAmount;
  }

  public BigDecimal getGrantCurrencyAmount() {
    return grantCurrencyAmount;
  }

  public void setGrantCurrencyAmount(BigDecimal grantCurrencyAmount) {
    this.grantCurrencyAmount = grantCurrencyAmount;
  }

  public BigDecimal getExchangeRate() {
    return exchangeRate;
  }

  public void setExchangeRate(BigDecimal exchangeRate) {
    this.exchangeRate = exchangeRate;
  }

  public BigDecimal getAccountingCurrencyAmountNotEdited() {
    return accountingCurrencyAmountNotEdited;
  }

  public void setAccountingCurrencyAmountNotEdited(BigDecimal accountingCurrencyAmountNotEdited) {
    this.accountingCurrencyAmountNotEdited = accountingCurrencyAmountNotEdited;
  }

  public ProjectReport getReport() {
    return report;
  }

  public void setReport(ProjectReport report) {
    this.report = report;
  }
}
