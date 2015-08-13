/**
 * This file is a part of GrantMaster.
 * Copyright (C) 2015  Gábor Fehér <feherga@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

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

  public static void getAggregateLimits(
      EntityManager em,
      Map<BudgetCategory, Map<Currency, BigDecimal>> limits) {
    List<Project> projects = ProjectWrapper.getProjectsWithoutWrapping(em);
    for (Project project : projects) {
      List<ProjectBudgetCategoryWrapper> projectLimits = getProjectBudgetLimits(em, project, null);
      for (ProjectBudgetCategoryWrapper projectLimit : projectLimits) {
        BudgetCategory category = projectLimit.getBudgetCategory();
        Map<Currency, BigDecimal> limitsPerCurrency = limits.get(category);
        if (limitsPerCurrency == null) {
          limitsPerCurrency = new HashMap<>();
          limits.put(category, limitsPerCurrency);
        }
        ProjectBudgetLimit limit = projectLimit.getEntity();
        if (limit != null && limit.getBudgetGrantCurrency() != null) {
          Currency currency = project.getGrantCurrency();
          BigDecimal value = limit.getBudgetGrantCurrency();
          BigDecimal sumValue = limitsPerCurrency.getOrDefault(currency, BigDecimal.ZERO);
          limitsPerCurrency.put(currency, sumValue.add(value, Utils.MC));
        }
      }
    }
  }

  @Override
  protected void removeColumns(Set<String> columns) {
    throw new UnsupportedOperationException("Not supported.");
  }

  /**
   * Gets all budget categories. Populates expense summaries from expenses
   * of the given project. Only expenses specified in "normal" more are
   * considered.
   * @return true if at least one expense was populated
   */
  private static boolean getProjectBudgetCategoriesNormalMode(
      EntityManager em,
      Project project,
      ProjectReport filterReport,
      List<ProjectBudgetCategoryWrapper> categories) {
    List<ProjectBudgetCategoryWrapper> list = em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetCategoryWrapper(c, SUM(a.accountingCurrencyAmount), SUM(a.accountingCurrencyAmount / s.exchangeRate)) " +
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
    categories.addAll(list);
    for (ProjectBudgetCategoryWrapper categoryWrapper : list) {
      if (categoryWrapper.spentGrantCurrency != null) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets all budget categories. Populates expense summaries from expenses
   * of the given project. Only expenses specified in "override" more are
   * considered.
   * @return true if at least one expense was populated
   */
  private static boolean getProjectBudgetCategoriesOverrideMode(
      EntityManager em,
      Project project,
      ProjectReport filterReport,
      List<ProjectBudgetCategoryWrapper> categories) {
    List<ProjectBudgetCategoryWrapper> list = em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetCategoryWrapper(c, SUM(e.accountingCurrencyAmountOverride), SUM(e.accountingCurrencyAmountOverride / e.exchangeRateOverride)) " +
        "FROM BudgetCategory c LEFT OUTER JOIN ProjectExpense e ON e.budgetCategory = c AND e.project = :project " +
            "WHERE c.direction = :direction " +
            " AND (:filterReportId IS NULL OR e.report.id = :filterReportId) " +
            "GROUP BY c " +
            "ORDER BY c.groupName NULLS LAST, c.name",
        ProjectBudgetCategoryWrapper.class).
            setParameter("project", project).
            setParameter("direction", BudgetCategory.Direction.PAYMENT).
            setParameter("filterReportId", filterReport == null ? null : filterReport.getId()).
            getResultList();
    categories.addAll(list);
    for (ProjectBudgetCategoryWrapper categoryWrapper : list) {
      if (categoryWrapper.spentGrantCurrency != null) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets total expense sum per budget categories for a project. Only considers
   * expenses where the exchange rate was manually specified, that is
   * "override mode".
   */
  private static void getProjectBudgetCategories(
      EntityManager em,
      Project project,
      ProjectReport filterReport,
      List<ProjectBudgetCategoryWrapper> categories) {
    List<ProjectBudgetCategoryWrapper> list = em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetCategoryWrapper(c, 0.0, 0.0) " +
        "FROM BudgetCategory c LEFT OUTER JOIN ProjectExpense e ON e.budgetCategory = c AND e.project = :project " +
            "WHERE c.direction = :direction " +
            " AND (:filterReportId IS NULL OR e.report.id = :filterReportId) " +
            "GROUP BY c " +
            "ORDER BY c.groupName NULLS LAST, c.name",
        ProjectBudgetCategoryWrapper.class).
            setParameter("project", project).
            setParameter("direction", BudgetCategory.Direction.PAYMENT).
            setParameter("filterReportId", filterReport == null ? null : filterReport.getId()).
            getResultList();
    categories.addAll(list);
  }

  private static BigDecimal getTotalProjectIncome(
      EntityManager em,
      Project project) {
    TypedQuery<BigDecimal> query =
        em.createQuery("SELECT SUM(s.grantCurrencyAmount) FROM ProjectSource s WHERE s.project = :project GROUP BY s.project", BigDecimal.class).
            setParameter("project", project);
    return Utils.getSingleResultWithDefault(BigDecimal.ZERO, query);
  }

  private static ProjectBudgetLimit getLimitForProjectAndCategory(
      EntityManager em,
      Project project,
      BudgetCategory budgetCategory) {
    return Utils.getSingleResultWithDefault(
        null,
        em.createQuery(
            "SELECT l FROM ProjectBudgetLimit l WHERE l.project = :project AND l.budgetCategory = :budgetCategory",
            ProjectBudgetLimit.class).
            setParameter("project", project).
            setParameter("budgetCategory", budgetCategory));
  }

  public static List<ProjectBudgetCategoryWrapper> getProjectBudgetLimits(
      EntityManager em,
      Project project,
      ProjectReport filterReport) {
    BigDecimal totalIncome = getTotalProjectIncome(em, project);
    List<ProjectBudgetCategoryWrapper> categories = new ArrayList<>();
    // Get list of all budget categories with cumulative expenses of this project.
    if (!getProjectBudgetCategoriesNormalMode(em, project, filterReport, categories)) {
      // We don't know whether this project had "normal" or "override" mode
      // expenses. If normal did not yield any results, we try again with
      // override.
      categories.clear();
      getProjectBudgetCategoriesOverrideMode(em, project, filterReport, categories);
    }

    // Go over list of categories, populate limit values where present.
    // Drop categories which have neither limit nor expense values.
    Iterator<ProjectBudgetCategoryWrapper> iterator = categories.iterator();
    while (iterator.hasNext()) {
      ProjectBudgetCategoryWrapper categoryWrapper = iterator.next();
      ProjectBudgetLimit limit = getLimitForProjectAndCategory(em, project, categoryWrapper.getBudgetCategory());
      if (limit == null && categoryWrapper.spentGrantCurrency == null) {
        iterator.remove();
      }
      if (limit != null) {
        categoryWrapper.setLimit(totalIncome, limit);
      } else {
        categoryWrapper.setProject(project);
      }
    }

    return categories;
  }

  static void removeProjectBudgetLimits(EntityManager em, Project project) {
    em.createQuery("DELETE FROM ProjectBudgetLimit l WHERE l.project = :project").
        setParameter("project", project).
        executeUpdate();
  }
}
