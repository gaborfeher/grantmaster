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

import com.github.gaborfeher.grantmaster.framework.base.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseSourceAllocation;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectExpenseWrapper extends EntityWrapper<ProjectExpense> {
  private static final Logger logger = LoggerFactory.getLogger(ProjectExpenseWrapper.class);

  @NotNull(message="%ValidationErrorExpenseAmount")
  @DecimalMin(value="0.01", message="%ValidationErrorExpenseAmount")
  private BigDecimal accountingCurrencyAmount;

  private final BigDecimal accountingCurrencyAmountNotEdited;

  private BigDecimal grantCurrencyAmount;

  private BigDecimal exchangeRate;

  /*
  public ProjectExpenseWrapper(ProjectExpense expense, BigDecimal accountingCurrencyAmount, BigDecimal grantCurrencyAmount) {
    super(expense);
    this.accountingCurrencyAmount = accountingCurrencyAmount;
    this.accountingCurrencyAmountNotEdited = accountingCurrencyAmount;
    this.grantCurrencyAmount = grantCurrencyAmount;
    this.exchangeRate = grantCurrencyAmount.compareTo(BigDecimal.ZERO) <= 0 ? null : accountingCurrencyAmount.divide(grantCurrencyAmount, Utils.MC);
  }
  */

   public ProjectExpenseWrapper(ProjectExpense expense) {
    super(expense);

    this.exchangeRate = BigDecimal.ZERO;
    this.accountingCurrencyAmount = BigDecimal.ZERO;
    this.grantCurrencyAmount = BigDecimal.ZERO;

    if (expense.getExchangeRateOverride() != null) {
      this.exchangeRate = expense.getExchangeRateOverride();
      this.accountingCurrencyAmount = expense.getAccountingCurrencyAmountOverride();
      this.grantCurrencyAmount = expense.getAccountingCurrencyAmountOverride().divide(exchangeRate, Utils.MC);
    } else if (expense.getSourceAllocations() != null) {
      this.accountingCurrencyAmount = BigDecimal.ZERO;
      this.grantCurrencyAmount = BigDecimal.ZERO;
      for (ExpenseSourceAllocation allocation : expense.getSourceAllocations()) {
        this.accountingCurrencyAmount = this.accountingCurrencyAmount.add(
            allocation.getAccountingCurrencyAmount(), Utils.MC);
        this.grantCurrencyAmount = this.grantCurrencyAmount.add(
            allocation.getAccountingCurrencyAmount().divide(allocation.getSource().getExchangeRate(), Utils.MC), Utils.MC);
      }
      this.exchangeRate = grantCurrencyAmount.compareTo(BigDecimal.ZERO) <= 0 ? null : accountingCurrencyAmount.divide(grantCurrencyAmount, Utils.MC);
    }
    this.accountingCurrencyAmountNotEdited = accountingCurrencyAmount;
  }

  @AssertTrue(message="%ValidationErrorExpenseConsistency")
  private boolean isValid() {
    ProjectExpense expense = getEntity();
    if (expense.getProject() == null ||
        expense.getProject().getAccountCurrency() == null ||
        expense.getOriginalCurrency() == null ||
        expense.getOriginalAmount() == null ||
        accountingCurrencyAmount == null) {
      return false;
    }
    Project project = expense.getProject();
    if (!expense.getOriginalCurrency().equals(project.getAccountCurrency())) {
      // Nothing to check if they are not equal.
      return true;
    }
    return 0 == expense.getOriginalAmount().compareTo(accountingCurrencyAmount);
  }

  @AssertTrue(message="%ValidationErrorLockedReport")
  private boolean isEditingAllowed() {
    return ProjectReport.Status.OPEN.equals(getEntity().getReport().getStatus());
  }

  @Override
  protected boolean checkIsLocked() {
    if (getEntity().getReport().getStatus() != ProjectReport.Status.CLOSED) {
      return false;
    }
    validate();  // trigger error message
    return true;
  }

  /**
   * Adds an expense source allocation to this expense. In other words,
   * spent money is added. Not that grantCurrencyAmount is updated but
   * accountingCurrencyAmount is not, because it is assumed that
   * recalculateAllocations is taking care of it.
   */
  private void addAllocation(
      ProjectSource source,
      BigDecimal accountingCurrencyAmountToTake) {
    grantCurrencyAmount = grantCurrencyAmount.add(
        accountingCurrencyAmountToTake.divide(source.getExchangeRate(), Utils.MC), Utils.MC);
    ExpenseSourceAllocation allocation = new ExpenseSourceAllocation();
    allocation.setExpense(entity);
    allocation.setSource(source);
    allocation.setAccountingCurrencyAmount(accountingCurrencyAmountToTake);
    entity.getSourceAllocations().add(allocation);
  }

  /**
   * Recalculate the sourceAllocations array. So that this expense uses the
   * earliest free sources for its cost. Preconditions: the
   * accountingCurrencyAmount member variable should store the desired value
   * of this expense. sourceAllocations should be empty, and any previously
   * used allocations should be deleted (and flushed) from the database.
   */
  private void recalculateAllocations(EntityManager em) {
    BigDecimal remainingAccountingCurrencyAmount = accountingCurrencyAmount;
    grantCurrencyAmount = BigDecimal.ZERO;
    List<ProjectSourceWrapper> sources =
        ProjectSourceWrapper.getProjectSourceListForAllocation(em, entity.getProject());
    entity.setSourceAllocations(new ArrayList<>());

    for (int i = 0; i < sources.size(); ++i) {
      ProjectSource source = sources.get(i).getEntity();
      if (remainingAccountingCurrencyAmount.compareTo(BigDecimal.ZERO) <= 0) {
        break;  // Done
      }
      // Minimum of needed amount and available amount from this source:
      BigDecimal take = remainingAccountingCurrencyAmount.min(source.getRemainingAccountingCurrencyAmount());
      if (i == sources.size() - 1) {
        // Allow of going below zero balance for the last source.
        take = remainingAccountingCurrencyAmount;
      }
      if (take.compareTo(BigDecimal.ZERO) > 0) {
        remainingAccountingCurrencyAmount = remainingAccountingCurrencyAmount.subtract(take, Utils.MC);
        addAllocation(source, take);
      }
    }
    exchangeRate = accountingCurrencyAmount.divide(grantCurrencyAmount, Utils.MC);
    for (ExpenseSourceAllocation allocation : entity.getSourceAllocations()) {
      em.persist(allocation);
    }
  }

  public static void updateExpenseAllocations(EntityManager em, Project project, LocalDate startingFrom) {
    // Get list of expenses to update.
    List<ProjectExpenseWrapper> expensesToUpdate =
        getProjectExpenseListForAllocation(em, project, startingFrom);
    // Delete allocations and flush this to database. (Accounting currency amounts
    // are still kept in the database.)
    for (ProjectExpenseWrapper e : expensesToUpdate) {
      em.createQuery("DELETE FROM ExpenseSourceAllocation a WHERE a.expense = :expense").
          setParameter("expense", e.getEntity()).
          executeUpdate();
    }
    em.flush();
    // Recompute expenses.
    for (ProjectExpenseWrapper e : expensesToUpdate) {
      e.recalculateAllocations(em);
    }
  }

  private boolean propagateAccountingCurrencyAmountChange(
      EntityManager em, BigDecimal editedAccountingCurrencyAmount) {
    if (entity.getExchangeRateOverride() == null) {
      // Exchange rate is computed here. (Classic way of expenses.)
      // Set the allocation size to be right for this entity and flush.
      // This is just an initial fake setting which will be removed while normalizing.

      ExpenseSourceAllocation allocation;
      if (entity.getSourceAllocations().size() > 0) {
        allocation = entity.getSourceAllocations().get(0);
        allocation.setAccountingCurrencyAmount(editedAccountingCurrencyAmount);
        while (entity.getSourceAllocations().size() > 1) {
          entity.getSourceAllocations().remove(1);
        }
      } else {
        ProjectSource source0 = em.createQuery("SELECT s FROM ProjectSource s WHERE s.project = :project", ProjectSource.class).
            setParameter("project", entity.getProject()).
            setMaxResults(1).
            getSingleResult();
        if (source0 == null) {
          return false;
        }
        allocation = new ExpenseSourceAllocation();
        em.persist(allocation);
        allocation.setExpense(entity);
        allocation.setAccountingCurrencyAmount(editedAccountingCurrencyAmount);
        allocation.setSource(source0);
        entity.getSourceAllocations().add(allocation);
      }
      em.flush();
      updateExpenseAllocations(em, entity.getProject(), entity.getPaymentDate());
    } else {
      entity.setAccountingCurrencyAmountOverride(editedAccountingCurrencyAmount);
      grantCurrencyAmount = editedAccountingCurrencyAmount.divide(exchangeRate, Utils.MC);
    }
    return true;
  }

  @Override
  protected boolean saveInternal(EntityManager em) {
    if (ProjectReport.Status.CLOSED.equals(entity.getReport().getStatus())) {
      return false;
    }
    if (!super.saveInternal(em)) {
      return false;
    }
    BigDecimal editedAccountingCurrencyAmount = null;
    if (getAccountingCurrencyAmount().compareTo(accountingCurrencyAmountNotEdited) != 0) {
      editedAccountingCurrencyAmount = getAccountingCurrencyAmount();
    }
    if (editedAccountingCurrencyAmount != null) {
      if (!propagateAccountingCurrencyAmountChange(em, editedAccountingCurrencyAmount)) {
        return false;
      }
    }

    if (entity.getExchangeRateOverride() == null) {
      if (entity.getSourceAllocations() == null || entity.getSourceAllocations().isEmpty()) {
        logger.error("saving expense failed with empty alloc list");
        return false;
      }
    } else {
      if (entity.getAccountingCurrencyAmountOverride() == null) {
        logger.error("saving expense failed with missing accounting currency amount override");
        return false;
      }
    }
    return true;
  }

  @Override
  public Object getProperty(String name) {
    if ("accountingCurrencyAmount".equals(name)) {
      return getAccountingCurrencyAmount();
    } else if ("grantCurrencyAmount".equals(name)) {
      return getGrantCurrencyAmount();
    } else if ("exchangeRate".equals(name)) {
      return getExchangeRate();
    }
    return super.getProperty(name);
  }

  @Override
  public boolean setProperty(String name, Object value, Class<?> paramType) {
    // If the below condition holds, then the two amount values are tied
    // together: updating one should update the other.
    boolean amountsAreInterlocked = entity.getOriginalCurrency() != null &&
          entity.getOriginalCurrency().equals(entity.getProject().getAccountCurrency());
    if ("accountingCurrencyAmount".equals(name)) {
      BigDecimal backup = accountingCurrencyAmount;
      accountingCurrencyAmount = (BigDecimal) value;
      if (amountsAreInterlocked) {
        if (!super.setProperty("originalAmount", value, paramType)) {
          accountingCurrencyAmount = backup;
          return false;
        }
      }
      return true;
    } else if ("originalAmount".equals(name)) {
      if (super.setProperty(name, value, paramType)) {
        if (amountsAreInterlocked) {
          accountingCurrencyAmount = (BigDecimal) value;
        }
        return true;
      } else {
        return false;
      }
    } else if ("exchangeRate".equals(name)) {
      setExchangeRate((BigDecimal) value);
      return true;
    } else {
      return super.setProperty(name, value, paramType);
    }
  }

  public static ProjectExpenseWrapper createNew(EntityManager em, Project project) {
    ProjectExpense expense = new ProjectExpense();
    expense.setProject(project);
    expense.setOriginalCurrency(project.getAccountCurrency());
    expense.setReport(ProjectReportWrapper.getDefaultProjectReport(em, project));
    ProjectExpenseWrapper wrapper = new ProjectExpenseWrapper(expense);
    return wrapper;
  }

  @Override
  public boolean delete(EntityManager em) {
    if (ProjectReport.Status.CLOSED.equals(entity.getReport().getStatus())) {
      return false;
    }
    LocalDate startDate = entity.getPaymentDate();
    ProjectExpense mergedExpense = em.find(ProjectExpense.class, entity.getId());
    em.remove(mergedExpense);
    em.flush();
    updateExpenseAllocations(em, mergedExpense.getProject(), startDate);
    requestTableRefresh();
    return true;
  }

  public static List<ProjectExpenseWrapper> getProjectExpenseList(EntityManager em, Project project) {
    return getProjectExpenseListQuery(em, project, true, "").getResultList();
  }

  /**
   * @return List of expenses in the order they should be taken when allocating
   * sources.( Note that filtering is only based on report date, therefore
   * earlier expenses in a report may be have their allocations recalculated
   * unnecessarily.)
   */
  public static List<ProjectExpenseWrapper> getProjectExpenseListForAllocation(
      EntityManager em,
      Project project,
      LocalDate earliestReportDate) {
    if (earliestReportDate != null) {
      return getProjectExpenseListQuery(em, project, false, " AND e.report.reportDate >= :date").
          setParameter("date", earliestReportDate).
          getResultList();
    } else {
      return getProjectExpenseListQuery(em, project, false, "").getResultList();
    }
  }

  private static TypedQuery<ProjectExpenseWrapper> getProjectExpenseListQuery(EntityManager em, Project project, boolean descending, String extraWhere) {
    String sortString = descending ? " DESC" : "";
    String queryString =
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper(e) " +
        "FROM ProjectExpense e LEFT OUTER JOIN ProjectReport r ON e.report = r " + //LEFT OUTER JOIN e.sourceAllocations AS a " +
        "WHERE e.project = :project " + extraWhere + " " +
        "GROUP BY e, r " +
        "ORDER BY r.reportDate " + sortString + ", e.paymentDate " + sortString + ", e.id " + sortString;
    return em.createQuery(queryString, ProjectExpenseWrapper.class).
        setParameter("project", project);
  }

  public static List<ProjectExpenseWrapper> getExpenseList(
      EntityManager em,
      Project project,
      LocalDate startDate,
      LocalDate endDate,
      BudgetCategory budgetCategory,
      String budgetCategoryGroup,
      String accountNo,
      String partnerName,
      String comment1,
      String comment2) {
    if ("".equals(budgetCategoryGroup)) {
      budgetCategoryGroup = null;
    }
    if ("".equals(accountNo)) {
      accountNo = null;
    }
    if ("".equals(partnerName)) {
      partnerName = null;
    }
    if ("".equals(comment1)) {
      comment1 = null;
    }
    if ("".equals(comment2)) {
      comment2 = null;
    }

    TypedQuery<ProjectExpenseWrapper> query = em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper(e) " +
            "FROM ProjectExpense e " +
            "WHERE (e.project.id = :project OR :project IS NULL) " +
            " AND (e.paymentDate >= :startDate OR :startDate IS NULL) " +
            " AND (e.paymentDate <= :endDate OR :endDate IS NULL) " +
            " AND (e.budgetCategory.id = :budgetCategory OR :budgetCategory IS NULL) " +
            " AND (e.budgetCategory.groupName = :budgetCategoryGroup OR :budgetCategoryGroup IS NULL) " +
            " AND (LOCATE(LOWER(:accountNo), LOWER(e.accountNo)) > 0 OR :accountNo IS NULL) " +
            " AND (LOCATE(LOWER(:partnerName), LOWER(e.partnerName)) > 0 OR :partnerName IS NULL) " +
            " AND (LOCATE(LOWER(:comment1), LOWER(e.comment1)) > 0 OR :comment1 IS NULL) " +
            " AND (LOCATE(LOWER(:comment2), LOWER(e.comment2)) > 0  OR :comment2 IS NULL) " +
            "GROUP BY e " +
            "ORDER BY e.paymentDate, e.id",
        ProjectExpenseWrapper.class);
     query.setParameter("project", project == null ? null : project.getId());
     query.setParameter("startDate", startDate);
     query.setParameter("endDate", endDate);
     query.setParameter("budgetCategory", budgetCategory == null ? null : budgetCategory.getId());
     query.setParameter("budgetCategoryGroup", budgetCategoryGroup);
     query.setParameter("partnerName", partnerName);
     query.setParameter("accountNo", accountNo);
     query.setParameter("comment1", comment1);
     query.setParameter("comment2", comment2);

     return query.getResultList();
  }

  public static void removeProjectExpenses(EntityManager em, Project project) {
    em.createQuery("DELETE FROM ExpenseSourceAllocation a WHERE a IN (SELECT a FROM ExpenseSourceAllocation a, ProjectExpense e WHERE a.expense = e AND e.project = :project)").
        setParameter("project", project).
        executeUpdate();
    em.createQuery("DELETE FROM ProjectExpense p WHERE p.project = :project").
        setParameter("project", project).
        executeUpdate();
  }

  public BigDecimal getAccountingCurrencyAmount() {
    return accountingCurrencyAmount;
  }

  public BigDecimal getGrantCurrencyAmount() {
    return grantCurrencyAmount;
  }

  public BigDecimal getExchangeRate() {
    return exchangeRate;
  }

  public void setExchangeRate(BigDecimal exchangeRate) {
    this.exchangeRate = exchangeRate;
    entity.setExchangeRateOverride(exchangeRate);
    if (entity.getSourceAllocations() != null) {
      entity.getSourceAllocations().clear();
    }
  }
}
