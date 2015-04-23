package com.github.gaborfeher.grantmaster.logic.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.eclipse.persistence.annotations.CascadeOnDelete;

@Entity
public class ProjectSource implements EntityBase, Serializable {
  @Id
  @GeneratedValue
  private int id;
  
  @ManyToOne
  @JoinColumn(nullable = false)
  @CascadeOnDelete
  private Project project;

  @Column(nullable = false, scale = 10, precision = 25)
  private BigDecimal grantCurrencyAmount;
      
  @Column(nullable = false, scale = 10, precision = 25)
  private BigDecimal exchangeRate;

  @Column(nullable = false)
  @Temporal(TemporalType.DATE)
  private LocalDate availabilityDate;
  
  public ProjectSource() {
  }

  /**
   * @return the id
   */
  public Integer getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return the project
   */
  public Project getProject() {
    return project;
  }

  /**
   * @param project the project to set
   */
  public void setProject(Project project) {
    this.project = project;
  }

  /**
   * @return the amount
   */
  public BigDecimal getGrantCurrencyAmount() {
    return grantCurrencyAmount;
  }

  /**
   * @param amount the amount to set
   */
  public void setGrantCurrencyAmount(BigDecimal amount) {
    this.grantCurrencyAmount = amount;
  }

  /**
   * @return the exchangeRate
   */
  public BigDecimal getExchangeRate() {
    return exchangeRate;
  }

  /**
   * @param exchangeRate the exchangeRate to set
   */
  public void setExchangeRate(BigDecimal exchangeRate) {
    this.exchangeRate = exchangeRate;
  }

  /**
   * @return the availabilityDate
   */
  public LocalDate getAvailabilityDate() {
    return availabilityDate;
  }

  /**
   * @param availabilityDate the availabilityDate to set
   */
  public void setAvailabilityDate(LocalDate availabilityDate) {
    this.availabilityDate = availabilityDate;
  }
  
}
