package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;

public class ProjectBudgetCategoryWrapper extends BudgetCategoryWrapperBase<ProjectBudgetLimit> {
  private BigDecimal spentGrantCurrency;
  private BigDecimal spentAccountingCurrency;
  private BigDecimal remainingAccountingCurrency;
  private BigDecimal remainingGrantCurrency;
  private BigDecimal budgetAccountingCurrency;
  
  private static ProjectBudgetLimit createEmptyLimit(BudgetCategory budgetCategory) {
    ProjectBudgetLimit limit = new ProjectBudgetLimit();
    limit.setBudgetCategory(budgetCategory);
    return limit;
  }
  
  public ProjectBudgetCategoryWrapper(BudgetCategory budgetCategory, BigDecimal spentAccountingCurrency, BigDecimal spentGrantCurrency) {
    super(createEmptyLimit(budgetCategory), null);
    this.spentGrantCurrency = spentGrantCurrency;
    this.spentAccountingCurrency = spentAccountingCurrency;
    this.remainingAccountingCurrency = null;
    this.budgetAccountingCurrency = null;
  }
  
  public ProjectBudgetCategoryWrapper(String fakeName) {
    super(null, fakeName);
    this.spentGrantCurrency = BigDecimal.ZERO;
    this.spentAccountingCurrency = BigDecimal.ZERO;
    this.remainingAccountingCurrency = null;
    this.budgetAccountingCurrency = null;
  }

  @Override
  public boolean canEdit() {
    BudgetCategory budgetCategory = entity.getBudgetCategory();
    return !getIsSummary() &&
        (budgetCategory == null ||
         budgetCategory.getDirection() == BudgetCategory.Direction.PAYMENT);
  }
  
  @Override
  public ProjectBudgetCategoryWrapper createFakeCopy(String fakeTitle) {
    ProjectBudgetCategoryWrapper copy = new ProjectBudgetCategoryWrapper(fakeTitle);
    ProjectBudgetLimit l = new ProjectBudgetLimit();
    l.setProject(entity.getProject());
    copy.setLimit(BigDecimal.ZERO, l);
    copy.addSummaryValues(this, BigDecimal.ONE);
    copy.setIsSummary(true);
    copy.setState(null);
    return copy;
  }
  
  public void setProject(Project project) {
    entity.setProject(project);
  }
  
  public void setLimit(BigDecimal total, ProjectBudgetLimit limit) {
    this.entity = limit;
    if (limit == null) {
      return;
    }

    if (limit.getBudgetPercentage() != null && total != null) {
      limit.setBudgetGrantCurrency(total.multiply(limit.getBudgetPercentage()).divide(new BigDecimal("100"), Utils.MC));
    }
    if (limit.getBudgetGrantCurrency() != null) {
      remainingGrantCurrency = limit.getBudgetGrantCurrency();
      if (spentGrantCurrency != null) {
        remainingGrantCurrency = remainingGrantCurrency.subtract(spentGrantCurrency, Utils.MC);
      }
    }
  }

  public void setBudgetCategory(BudgetCategory budgetCategory) {
    entity.setBudgetCategory(budgetCategory);
  }
  
  public BudgetCategory getBudgetCategory() {
    return entity.getBudgetCategory();
  }

  @Override
  public void addSummaryValues(BudgetCategoryWrapperBase other0, BigDecimal multiplier) {
    ProjectBudgetCategoryWrapper other = (ProjectBudgetCategoryWrapper) other0;
    spentGrantCurrency = Utils.addMult(spentGrantCurrency, other.spentGrantCurrency, multiplier);
    spentAccountingCurrency = Utils.addMult(spentAccountingCurrency, other.spentAccountingCurrency, multiplier);
  }
  
  public void addBudgetAmounts(BigDecimal accountingCurrencyAmount, BigDecimal grantCurrencyAmount) {
    if (entity.getBudgetGrantCurrency() == null) {
      entity.setBudgetGrantCurrency(grantCurrencyAmount);
    } else {
      entity.setBudgetGrantCurrency(entity.getBudgetGrantCurrency().add(grantCurrencyAmount, Utils.MC));
    }
    if (budgetAccountingCurrency == null) {
      budgetAccountingCurrency = BigDecimal.ZERO;
    }
    budgetAccountingCurrency = budgetAccountingCurrency.add(accountingCurrencyAmount);
    remainingGrantCurrency = entity.getBudgetGrantCurrency().subtract(spentGrantCurrency, Utils.MC);
    remainingAccountingCurrency = budgetAccountingCurrency.subtract(spentAccountingCurrency, Utils.MC);
  }
  
  @Override
  public Object getProperty(String name) {
    if ("spentGrantCurrency".equals(name)) return spentGrantCurrency;
    if ("spentAccountingCurrency".equals(name)) return spentAccountingCurrency;
    if ("remainingAccountingCurrency".equals(name)) return remainingAccountingCurrency;
    if ("remainingGrantCurrency".equals(name)) return remainingGrantCurrency;
    if ("budgetAccountingCurrency".equals(name)) return budgetAccountingCurrency;
    return super.getProperty(name);
  }
  
  public static ProjectBudgetCategoryWrapper createNew(Project project) {
    ProjectBudgetLimit limit = new ProjectBudgetLimit();
    limit.setProject(project);
    ProjectBudgetCategoryWrapper wrapper = new ProjectBudgetCategoryWrapper(null, BigDecimal.ZERO, BigDecimal.ZERO);
    wrapper.setLimit(BigDecimal.ZERO, limit);
    return wrapper;
  }
  
  public static List<ProjectBudgetCategoryWrapper> getProjectBudgetLimits(
      EntityManager em,
      Project project,
      ProjectReport filterReport) {
    BigDecimal total =
        Utils.getSingleResultWithDefault(BigDecimal.ZERO,
            em.createQuery("SELECT SUM(s.grantCurrencyAmount) FROM ProjectSource s WHERE s.project = :project GROUP BY s.project", BigDecimal.class).
                setParameter("project", project));
        
    List<ProjectBudgetCategoryWrapper> list = em.createQuery("SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetCategoryWrapper(c, SUM(a.accountingCurrencyAmount), SUM(a.accountingCurrencyAmount / s.exchangeRate)) " +
        "FROM BudgetCategory c LEFT OUTER JOIN ProjectExpense e ON e.budgetCategory = c AND e.project = :project LEFT OUTER JOIN ExpenseSourceAllocation a ON a.expense = e LEFT OUTER JOIN ProjectSource s ON a.source = s AND s.project = :project " +
            "WHERE c.direction = :direction " +
            " AND (:filterReportId IS NULL OR e.report.id = :filterReportId) " +
            "GROUP BY c " +
            "ORDER BY c.groupName NULLS LAST, c.name",
        ProjectBudgetCategoryWrapper.class).
            setParameter("project", project).
            setParameter("direction", BudgetCategory.Direction.PAYMENT).
            setParameter("filterReportId", filterReport == null ? null : filterReport.getId()).
            getResultList();
    
    Iterator<ProjectBudgetCategoryWrapper> iterator = list.iterator();
    while (iterator.hasNext()) {
      ProjectBudgetCategoryWrapper limitWrapper = iterator.next();
      ProjectBudgetLimit limit = 
          Utils.getSingleResultWithDefault(
              null,
              em.createQuery(
                  "SELECT l FROM ProjectBudgetLimit l WHERE l.project = :project AND l.budgetCategory = :budgetCategory",
                  ProjectBudgetLimit.class).
                  setParameter("project", project).
                  setParameter("budgetCategory", limitWrapper.getBudgetCategory()));
      
      if (limit == null && limitWrapper.spentGrantCurrency == null) {
        iterator.remove();
      }
      if (limit != null) {
        limitWrapper.setLimit(total, limit);
      } else {
        limitWrapper.setProject(project);
      }
    }

    return list;
  }
  
  static void removeProjectBudgetLimits(EntityManager em, Project project) {
    em.createQuery("DELETE FROM ProjectBudgetLimit l WHERE l.project = :project").
        setParameter("project", project).
        executeUpdate();
  }
}
