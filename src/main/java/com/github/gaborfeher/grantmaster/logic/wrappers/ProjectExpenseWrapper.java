package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.logic.entities.ExpenseSourceAllocation;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

public class ProjectExpenseWrapper extends EntityWrapper<ProjectExpense> {
  @NotNull(message="%ValidationErrorExpenseAmount")
  @DecimalMin(value="0.01", message="%ValidationErrorExpenseAmount")
  private BigDecimal accountingCurrencyAmount;
  
  private BigDecimal accountingCurrencyAmountNotEdited;
  
  private BigDecimal grantCurrencyAmount;
  
  private BigDecimal exchangeRate;
  
  public ProjectExpenseWrapper(ProjectExpense expense, BigDecimal accountingCurrencyAmount, BigDecimal grantCurrencyAmount) {
    super(expense);
    this.accountingCurrencyAmount = accountingCurrencyAmount;
    this.accountingCurrencyAmountNotEdited = accountingCurrencyAmount;
    this.grantCurrencyAmount = grantCurrencyAmount;
    this.exchangeRate = grantCurrencyAmount.compareTo(BigDecimal.ZERO) <= 0 ? null : accountingCurrencyAmount.divide(grantCurrencyAmount, Utils.MC);
  }
  
  private void recalculateAllocations(EntityManager em) {
    BigDecimal remainingAccountingCurrencyAmount = getAccountingCurrencyAmount();
    grantCurrencyAmount = BigDecimal.ZERO;
    ProjectExpense expense = (ProjectExpense) entity;
    List<ProjectSourceWrapper> sources = ProjectSourceWrapper.getProjectSources(em, expense.getProject(), null);
    expense.setSourceAllocations(new ArrayList<ExpenseSourceAllocation>());
    
    for (int i = 0; i < sources.size(); ++i) {
      ProjectSource source = sources.get(i).getEntity();
      if (remainingAccountingCurrencyAmount.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }
      if (source.getRemainingAccountingCurrencyAmount().compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal take = remainingAccountingCurrencyAmount.min(source.getRemainingAccountingCurrencyAmount());
        if (i == sources.size() - 1) {
          take = remainingAccountingCurrencyAmount;  // Allow of going below zero balance for the last source.
        }
        remainingAccountingCurrencyAmount = remainingAccountingCurrencyAmount.subtract(take, Utils.MC);
        grantCurrencyAmount = grantCurrencyAmount.add(take.divide(source.getExchangeRate(), Utils.MC), Utils.MC);
        ExpenseSourceAllocation allocation = new ExpenseSourceAllocation();
        allocation.setExpense(expense);
        allocation.setSource(source);
        allocation.setAccountingCurrencyAmount(take);
        expense.getSourceAllocations().add(allocation);
      }
    }
    exchangeRate = accountingCurrencyAmount.divide(grantCurrencyAmount, Utils.MC);
    for (ExpenseSourceAllocation allocation : expense.getSourceAllocations()) {
      em.persist(allocation);
    }
  }
  
  public static void updateExpenseAllocations(EntityManager em, Project project, LocalDate startingFrom) {
    // Get list of expenses to update.
    List<ProjectExpenseWrapper> expensesToUpdate;
    if (startingFrom != null) {
      expensesToUpdate = getProjectExpenseListQuery(em, project, false, " AND e.report.reportDate >= :date").
          setParameter("date", startingFrom).
          getResultList();
    } else {
      expensesToUpdate = getProjectExpenseListQuery(em, project, false, "").getResultList();
    }
    
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
  
  @Override
  public boolean save(EntityManager em) {
    super.save(em);
    BigDecimal editedAccountingCurrencyAmount = null;
    if (getAccountingCurrencyAmount().compareTo(accountingCurrencyAmountNotEdited) != 0) {
      editedAccountingCurrencyAmount = getAccountingCurrencyAmount();
    }
    System.out.println("ProjectExpense.save: 2");

    if (editedAccountingCurrencyAmount != null) {
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
    }

    if (entity.getSourceAllocations() == null || entity.getSourceAllocations().isEmpty()) {
      System.out.println("save failed with empty alloc list");
      return false;
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
    if ("accountingCurrencyAmount".equals(name)) {
      accountingCurrencyAmount = (BigDecimal) value;
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
    ProjectExpenseWrapper wrapper = new ProjectExpenseWrapper(expense, BigDecimal.ZERO, BigDecimal.ZERO);
    return wrapper;
  }
  
  @Override
  public void delete(EntityManager em) {
    LocalDate startDate = entity.getPaymentDate();
    ProjectExpense mergedExpense = em.find(ProjectExpense.class, entity.getId());
    em.remove(mergedExpense);
    em.flush();
    updateExpenseAllocations(em, mergedExpense.getProject(), startDate);
    getParent().onRefresh();
  }
  
  public static List<ProjectExpenseWrapper> getProjectExpenseList(EntityManager em, Project project) {
    return getProjectExpenseListQuery(em, project, true, "").getResultList();
  }
  
  public static TypedQuery<ProjectExpenseWrapper> getProjectExpenseListQuery(EntityManager em, Project project, boolean descending, String extraWhere) {
    String sortString = descending ? " DESC" : "";
    String queryString =
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper(e, SUM(a.accountingCurrencyAmount), SUM(a.accountingCurrencyAmount / a.source.exchangeRate)) " +
        "FROM ProjectExpense e LEFT OUTER JOIN ExpenseSourceAllocation a ON a.expense = e LEFT OUTER JOIN ProjectReport r ON e.report = r " +
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
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper(e, SUM(a.accountingCurrencyAmount), SUM(a.accountingCurrencyAmount / a.source.exchangeRate)) " +
            "FROM ProjectExpense e LEFT OUTER JOIN ExpenseSourceAllocation a ON a.expense = e " +
            "WHERE (e.project.id = :project OR :project IS NULL) " +
            " AND (e.paymentDate >= :startDate OR :startDate IS NULL) " +
            " AND (e.paymentDate <= :endDate OR :endDate IS NULL) " +
            " AND (e.budgetCategory.id = :budgetCategory OR :budgetCategory IS NULL) " +
            " AND (e.budgetCategory.groupName = :budgetCategoryGroup OR :budgetCategoryGroup IS NULL) " +
            " AND (e.accountNo = :accountNo OR :accountNo IS NULL) " +
            " AND (e.partnerName = :partnerName OR :partnerName IS NULL) " +
            " AND (e.comment1 = :comment1 OR :comment1 IS NULL) " +
            " AND (e.comment2 = :comment2 OR :comment2 IS NULL) " +
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
}
