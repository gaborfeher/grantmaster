/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import java.sql.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 *
 * @author gabor
 */
public class ProjectSourceWrapper extends EntityWrapper {
  private ProjectSource source;
  private double usedAccountingCurrencyAmount;
  
  public ProjectSourceWrapper(ProjectSource source, double usedAccountingCurrencyAmount) {
    this.source = source;
    this.usedAccountingCurrencyAmount = usedAccountingCurrencyAmount;
  }

  /**
   * @return the source
   */
  public ProjectSource getSource() {
    return source;
  }

  public void setSource(ProjectSource source) {
    this.source = source;
  }

  public Double getUsedAccountingCurrencyAmount() {
    return usedAccountingCurrencyAmount;
  }

 // public void setUsedAccountingCurrencyAmount(Double usedAccountingCurrencyAmount) {
 //   this.usedAccountingCurrencyAmount = usedAccountingCurrencyAmount;
 // }
  
  public double getUsedGrantCurrencyAmount() {
    return usedAccountingCurrencyAmount / source.getExchangeRate();
  }
  
  public Integer getId() {
    return source.getId();
  }

  public double getGrantCurrencyAmount() {
    return source.getAmount();
  }
  
  public void setGrantCurrencyAmount(Double amount) {
    source.setAmount(amount);
  }
  
  public double getAccountingCurrencyAmount() {
    return source.getAmount() * source.getExchangeRate();
  }
  
  public double getRemainingGrantCurrencyAmount() {
    return getGrantCurrencyAmount() - getUsedGrantCurrencyAmount();
  }
  
  public double getRemainingAccountingCurrencyAmount() {
    return getAccountingCurrencyAmount() - getUsedAccountingCurrencyAmount();
  }
  
  public Double getExchangeRate() {
    return source.getExchangeRate();
  }
  
  public void setExchangeRate(Double exchangeRate) {
    source.setExchangeRate(exchangeRate);
  }
  
  public Date getAvailabilityDate() {
    return source.getAvailabilityDate();
  }
  
  public void setAvailabilityDate(Date date) {
    source.setAvailabilityDate(date);
  }
  
  public static List<ProjectSourceWrapper> getProjectSources(Project project) {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    TypedQuery<ProjectSourceWrapper> query = em.createQuery("SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectSourceWrapper(s, COALESCE(SUM(a.accountingCurrencyAmount), 0.0)) FROM ProjectSource s LEFT OUTER JOIN ExpenseSourceAllocation a ON a.source = s WHERE s.project = :project GROUP BY s ORDER BY s.availabilityDate", ProjectSourceWrapper.class);
    query.setParameter("project", project);
    return query.getResultList();
  }
  
  static void removeProjectSources(Project project) {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    em.createQuery("DELETE FROM ExpenseSourceAllocation a WHERE a IN (SELECT a FROM ExpenseSourceAllocation a, ProjectSource s WHERE a.source = s AND s.project = :project)").
        setParameter("project", project).
        executeUpdate();
    em.createQuery("DELETE FROM ProjectSource s WHERE s.project = :project").
        setParameter("project", project).
        executeUpdate();
  }

  @Override
  protected Object getEntity() {
    return source;
  }
  
}
