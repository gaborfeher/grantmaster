package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.framework.base.EntityWrapper;
import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import java.math.BigDecimal;
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
      EntityManager em, Project project, ProjectReport filterReport) {
    return getProjectSources(em, project, filterReport, true);
  }
  
  public static List<ProjectSourceWrapper> getProjectSourceListForAllocation(EntityManager em, Project project) {
    return getProjectSources(em, project, null, false);
  }

  public static List<ProjectSourceWrapper> getProjectSources(
      EntityManager em, Project project, ProjectReport filterReport, boolean descending) {
    String sortString = descending ? " DESC" : "";
    TypedQuery<ProjectSourceWrapper> query = em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectSourceWrapper(s, COALESCE(SUM(a.accountingCurrencyAmount), 0.0)) " +
            "FROM ProjectSource s LEFT OUTER JOIN ExpenseSourceAllocation a ON a.source = s LEFT OUTER JOIN ProjectReport r ON s.report = r " +
            "WHERE s.project = :project " +
            "  AND (:filterReportId IS NULL OR s.report.id = :filterReportId) " +
            "GROUP BY s, r " +
            "ORDER BY r.reportDate " + sortString + ", s.availabilityDate " + sortString + ", s.id " + sortString, ProjectSourceWrapper.class);
    query.setParameter("project", project);
    query.setParameter("filterReportId", filterReport == null ? null : filterReport.getId());
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
  
  public static ProjectSourceWrapper createNew(EntityManager em, Project project) {
    ProjectSource source = new ProjectSource();
    source.setProject(project);
    source.setReport(ProjectReportWrapper.getDefaultProjectReport(em, project));
    return new ProjectSourceWrapper(source, BigDecimal.ZERO);
  }
  
}
