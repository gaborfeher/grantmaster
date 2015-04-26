package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class ProjectSourceWrapper extends EntityWrapper<ProjectSource> {
  public ProjectSourceWrapper(ProjectSource source, BigDecimal usedAccountingCurrencyAmount) {
    super(source);
    if (source.getExchangeRate() == null || source.getGrantCurrencyAmount() == null) {
      return;
    }
    source.setUsedAccountingCurrencyAmount(usedAccountingCurrencyAmount);
    source.setAccountingCurrencyAmount(source.getGrantCurrencyAmount().multiply(source.getExchangeRate(), Utils.MC));
    source.setRemainingAccountingCurrencyAmount(source.getAccountingCurrencyAmount().subtract(source.getUsedAccountingCurrencyAmount(), Utils.MC));
    if (source.getExchangeRate().compareTo(BigDecimal.ZERO) > 0) {
      source.setUsedGrantCurrencyAmount(usedAccountingCurrencyAmount.divide(source.getExchangeRate(), Utils.MC));
      source.setRemainingGrantCurrencyAmount(source.getGrantCurrencyAmount().subtract(source.getUsedGrantCurrencyAmount(), Utils.MC));
    }
  }
  
  public BigDecimal getGrantCurrencyAmount() {
    return entity.getGrantCurrencyAmount();
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
  public boolean save(EntityManager em) {
    super.save(em);
    ProjectExpenseWrapper.updateExpenseAllocations(em, entity.getProject(), entity.getAvailabilityDate());
    return true;
  }
}
