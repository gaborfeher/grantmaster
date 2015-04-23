package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.EntityBase;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class ProjectSourceWrapper extends EntityWrapper {
  private ProjectSource source;
  
  public ProjectSourceWrapper(ProjectSource source, BigDecimal usedAccountingCurrencyAmount) {
    this.source = source;
    if (source.getExchangeRate() == null || source.getGrantCurrencyAmount() == null) {
      return;
    }
    computedValues.put("usedAccountingCurrencyAmount", usedAccountingCurrencyAmount);
    BigDecimal accountingCurrencyAmount = source.getGrantCurrencyAmount().multiply(source.getExchangeRate(), Utils.MC);
    computedValues.put("accountingCurrencyAmount", accountingCurrencyAmount);
    computedValues.put("remainingAccountingCurrencyAmount", accountingCurrencyAmount.subtract(usedAccountingCurrencyAmount, Utils.MC));
    if (source.getExchangeRate().compareTo(BigDecimal.ZERO) > 0) {
      BigDecimal usedGrantCurrencyAmount = usedAccountingCurrencyAmount.divide(source.getExchangeRate(), Utils.MC);
      computedValues.put("usedGrantCurrencyAmount", usedGrantCurrencyAmount);
      computedValues.put("remainingGrantCurrencyAmount", source.getGrantCurrencyAmount().subtract(usedGrantCurrencyAmount, Utils.MC));
    }
  }
  
  // TODO(gaborfeher): Eliminate below getters?
  
  public BigDecimal getRemainingAccountingCurrencyAmount() {
    return (BigDecimal) computedValues.get("remainingAccountingCurrencyAmount");
  }

  public BigDecimal getAccountingCurrencyAmount() {
    return (BigDecimal) computedValues.get("accountingCurrencyAmount");
  }
  
  public BigDecimal getGrantCurrencyAmount() {
    return source.getGrantCurrencyAmount();
  }
  
  public ProjectSource getSource() {
    return source;
  }

  public void setSource(ProjectSource source) {
    this.source = source;
  }
  
  public Integer getId() {
    return source.getId();
  }

  public static List<ProjectSourceWrapper> getProjectSources(
      EntityManager em, Project project, LocalDate filterStartDate, LocalDate filterEndDate) {
    TypedQuery<ProjectSourceWrapper> query = em.createQuery(
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
    return query.getResultList();
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
  protected EntityBase getEntity() {
    return source;
  }

  @Override
  public boolean save(EntityManager em) {
    super.save(em);
    ProjectExpenseWrapper.updateExpenseAllocations(em, source.getProject(), source.getAvailabilityDate());
    return true;
  }

  @Override
  protected void setEntity(EntityBase entity) {
    this.source = (ProjectSource) entity;
  }

}
