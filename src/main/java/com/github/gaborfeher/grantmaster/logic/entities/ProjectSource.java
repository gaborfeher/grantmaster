package com.github.gaborfeher.grantmaster.logic.entities;

import com.github.gaborfeher.grantmaster.core.Utils;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import org.eclipse.persistence.annotations.CascadeOnDelete;

@Entity
public class ProjectSource extends EntityBase implements  Serializable {
  @Id
  @GeneratedValue
  private Long id;
  
  @ManyToOne
  @JoinColumn(nullable = false)
  @CascadeOnDelete
  private Project project;

  /**
   * The report in which this income source will be included.
   */
  @ManyToOne(optional = false)
  @JoinColumn(nullable = false)
  private ProjectReport report;  
  
  @Column(nullable = false, scale = 10, precision = 25)
  private BigDecimal grantCurrencyAmount;
      
  @Column(nullable = false, scale = 10, precision = 25)
  private BigDecimal exchangeRate;

  @Column(nullable = false)
  @Temporal(TemporalType.DATE)
  private LocalDate availabilityDate;
  
  @Transient
  private BigDecimal usedAccountingCurrencyAmount;
  
  @Transient
  private BigDecimal accountingCurrencyAmount;
  
  @Transient
  private BigDecimal remainingAccountingCurrencyAmount;
  
  @Transient
  private BigDecimal usedGrantCurrencyAmount;
  
  @Transient
  private BigDecimal remainingGrantCurrencyAmount;
  
  public ProjectSource() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public BigDecimal getGrantCurrencyAmount() {
    return grantCurrencyAmount;
  }

  public void setGrantCurrencyAmount(BigDecimal amount) {
    this.grantCurrencyAmount = amount;
  }

  public BigDecimal getExchangeRate() {
    return exchangeRate;
  }

  public void setExchangeRate(BigDecimal exchangeRate) {
    this.exchangeRate = exchangeRate;
  }

  public LocalDate getAvailabilityDate() {
    return availabilityDate;
  }

  public void setAvailabilityDate(LocalDate availabilityDate) {
    this.availabilityDate = availabilityDate;
  }

  public BigDecimal getUsedAccountingCurrencyAmount() {
    return usedAccountingCurrencyAmount;
  }

  public void setUsedAccountingCurrencyAmount(BigDecimal usedAccountingCurrencyAmount) {
    this.usedAccountingCurrencyAmount = usedAccountingCurrencyAmount;
  }

  public BigDecimal getAccountingCurrencyAmount() {
    return accountingCurrencyAmount;
  }

  public void setAccountingCurrencyAmount(BigDecimal accountingCurrencyAmount) {
    this.accountingCurrencyAmount = accountingCurrencyAmount;
  }

  public BigDecimal getRemainingAccountingCurrencyAmount() {
    return remainingAccountingCurrencyAmount;
  }

  public void setRemainingAccountingCurrencyAmount(BigDecimal remainingAccountingCurrencyAmount) {
    this.remainingAccountingCurrencyAmount = remainingAccountingCurrencyAmount;
  }

  public BigDecimal getUsedGrantCurrencyAmount() {
    return usedGrantCurrencyAmount;
  }

  public void setUsedGrantCurrencyAmount(BigDecimal usedGrantCurrencyAmount) {
    this.usedGrantCurrencyAmount = usedGrantCurrencyAmount;
  }

  public BigDecimal getRemainingGrantCurrencyAmount() {
    return remainingGrantCurrencyAmount;
  }

  public void setRemainingGrantCurrencyAmount(BigDecimal remainingGrantCurrencyAmount) {
    this.remainingGrantCurrencyAmount = remainingGrantCurrencyAmount;
  }

  public ProjectReport getReport() {
    return report;
  }

  public void setReport(ProjectReport report) {
    this.report = report;
  }
  
}
