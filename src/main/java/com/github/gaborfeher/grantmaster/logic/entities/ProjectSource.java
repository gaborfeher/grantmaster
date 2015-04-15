package com.github.gaborfeher.grantmaster.logic.entities;

import java.io.Serializable;
import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.eclipse.persistence.annotations.CascadeOnDelete;

@Entity
public class ProjectSource implements Serializable {
  @Id
  @GeneratedValue
  private int id;
  
  @ManyToOne
  @JoinColumn(nullable = false)
  @CascadeOnDelete
  private Project project;

  @Column(nullable = false)
  private double amount;
      
  @Column(nullable = false)
  private double exchangeRate;

  @Column(nullable = false)
  private Date availabilityDate;
  
  public ProjectSource() {
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
  public double getAmount() {
    return amount;
  }

  /**
   * @param amount the amount to set
   */
  public void setAmount(Double amount) {
    this.amount = amount;
  }

  /**
   * @return the exchangeRate
   */
  public double getExchangeRate() {
    return exchangeRate;
  }

  /**
   * @param exchangeRate the exchangeRate to set
   */
  public void setExchangeRate(Double exchangeRate) {
    this.exchangeRate = exchangeRate;
  }

  /**
   * @return the availabilityDate
   */
  public Date getAvailabilityDate() {
    return availabilityDate;
  }

  /**
   * @param availabilityDate the availabilityDate to set
   */
  public void setAvailabilityDate(Date availabilityDate) {
    this.availabilityDate = availabilityDate;
  }
  
}
