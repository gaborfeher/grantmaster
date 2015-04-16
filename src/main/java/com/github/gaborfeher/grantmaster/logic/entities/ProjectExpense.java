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
import org.eclipse.persistence.annotations.CascadeOnDelete;

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
  private ExpenseType expenseType;
  
  @Column(nullable = false)
  private Double originalAmount;
  
  @ManyToOne(optional = false)
  @JoinColumn(nullable = false)
  private Currency originalCurrency;
  
  @OneToMany(mappedBy="expense", cascade=CascadeType.ALL, orphanRemoval = true)
  private List<ExpenseSourceAllocation> sourceAllocations;

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

  /**
   * @param partnerName the partnerName to set
   */
  public void setPartnerName(String partnerName) {
    this.partnerName = partnerName;
  }

  /**
   * @return the expenseType
   */
  public ExpenseType getExpenseType() {
    return expenseType;
  }

  /**
   * @param expenseType the expenseType to set
   */
  public void setExpenseType(ExpenseType expenseType) {
    this.expenseType = expenseType;
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


}
