package com.github.gaborfeher.grantmaster.logic.entities;

import java.io.Serializable;
import java.sql.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

// http://svanimpe.be/blog/properties-jpa.html

@Entity
public class ProjectExpense implements Serializable {
  @Id
  @GeneratedValue
  private int id;
  
  @ManyToOne(optional = false)
  @JoinColumn(nullable = false)
  private Project project;
  
  @Column(nullable = false)
  private Date paymentDate;
  
  private String accountNo;
  private String partnerName;
  
  @ManyToOne(optional = false)
  @JoinColumn(nullable = false)
  private BudgetCategory budgetCategory;
  
  @Column(nullable = false)
  private Double originalAmount;
  
  @ManyToOne(optional = false)
  @JoinColumn(nullable = false)
  private Currency originalCurrency;
  
  @OneToMany(mappedBy="expense", cascade=CascadeType.ALL, orphanRemoval = true)
  private List<ExpenseSourceAllocation> sourceAllocations;

  private String comment1;
  
  private String comment2;
  
  public ProjectExpense() {
  }

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return the paymentDate
   */
  public Date getPaymentDate() {
    return paymentDate;
  }

  /**
   * @param paymentDate the paymentDate to set
   */
  public void setPaymentDate(Date paymentDate) {
    this.paymentDate = paymentDate;
  }

  /**
   * @return the accountNo
   */
  public String getAccountNo() {
    return accountNo;
  }

  /**
   * @param accountNo the accountNo to set
   */
  public void setAccountNo(String accountNo) {
    this.accountNo = accountNo;
  }

  /**
   * @return the partnerName
   */
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

  /**
   * @param projectId the projectId to set
   */
  public void setProject(Project project) {
    this.project = project;
  }

  /**
   * @return the originalAmount
   */
  public Double getOriginalAmount() {
    return originalAmount;
  }

  /**
   * @param originalAmount the originalAmount to set
   */
  public void setOriginalAmount(Double originalAmount) {
    this.originalAmount = originalAmount;
  }

  /**
   * @return the originalCurrency
   */
  public Currency getOriginalCurrency() {
    return originalCurrency;
  }

  /**
   * @param originalCurrency the originalCurrency to set
   */
  public void setOriginalCurrency(Currency originalCurrency) {
    this.originalCurrency = originalCurrency;
  }

  /**
   * @return the sources
   */
  public List<ExpenseSourceAllocation> getSourceAllocations() {
    return sourceAllocations;
  }

  /**
   * @param sources the sources to set
   */
  public void setSourceAllocations(List<ExpenseSourceAllocation> sources) {
    this.sourceAllocations = sources;
  }

  /**
   * @return the comment1
   */
  public String getComment1() {
    return comment1;
  }

  /**
   * @param comment1 the comment1 to set
   */
  public void setComment1(String comment1) {
    this.comment1 = comment1;
  }

  /**
   * @return the comment2
   */
  public String getComment2() {
    return comment2;
  }

  /**
   * @param comment2 the comment2 to set
   */
  public void setComment2(String comment2) {
    this.comment2 = comment2;
  }


}
