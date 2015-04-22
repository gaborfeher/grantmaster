package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import com.github.gaborfeher.grantmaster.ui.ControllerBase;
import java.sql.Date;
import java.util.List;
import javax.persistence.EntityManager;

public class ProjectSourceWrapper extends EntityWrapper {
  private ProjectSource source;
  private double usedAccountingCurrencyAmount;
  
  public ProjectSourceWrapper(ProjectSource source, double usedAccountingCurrencyAmount) {
    this.source = source;
    this.usedAccountingCurrencyAmount = usedAccountingCurrencyAmount;
  }

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
  
  public Double getUsedGrantCurrencyAmount() {
    if (source.getExchangeRate() <= 0.0) {
      return null;
    }
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
  
  public Double getRemainingGrantCurrencyAmount() {
    if (source.getExchangeRate() <= 0.0) {
      return null;
    }
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
  
  public static List<ProjectSourceWrapper> getProjectSources(
      Project project, Date filterStartDate, Date filterEndDate, ControllerBase parent) {
    MyQuery<ProjectSourceWrapper> query = EntityWrapper.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectSourceWrapper(s, COALESCE(SUM(a.accountingCurrencyAmount), 0.0)) " +
            "FROM ProjectSource s LEFT OUTER JOIN ExpenseSourceAllocation a ON a.source = s " +
            "WHERE s.project = :project " +
            "  AND (:filterStartDate IS NULL OR s.availabilityDate >= :filterStartDate) " +
            "  AND (:filterEndDate IS NULL OR s.availabilityDate <= :filterEndDate) " +
            "GROUP BY s " +
            "ORDER BY s.availabilityDate, s.id", ProjectSourceWrapper.class);
    query.setParameter("project", project);
    query.setParameter("filterStartDate", filterStartDate);
    query.setParameter("filterEndDate", filterEndDate);
    return query.getResultList(parent);
  }
  
  static void removeProjectSources(EntityManager em, Project project) {
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

  @Override
  public void persist() {
    DatabaseConnectionSingleton.getInstance().runInTransaction(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        em.persist(source);
        ProjectExpenseWrapper.updateExpenseAllocations(em, source.getProject(), source.getAvailabilityDate());
        return true;
      }

      @Override
      public void onSuccess() {
        getParent().refresh();
      }
      
    });
  }
  
}
