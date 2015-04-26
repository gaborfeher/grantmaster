package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.logic.entities.ExpenseSourceAllocation;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.EntityBase;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class ProjectExpenseWrapper extends EntityWrapper<ProjectExpense> {
  public ProjectExpenseWrapper(ProjectExpense expense, BigDecimal accountingCurrencyAmount, BigDecimal grantCurrencyAmount) {
    super(expense);
    expense.setAccountingCurrencyAmount(accountingCurrencyAmount);
    expense.setAccountingCurrencyAmountNotEdited(accountingCurrencyAmount);
    expense.setGrantCurrencyAmount(grantCurrencyAmount);
    expense.setExchangeRate(grantCurrencyAmount.compareTo(BigDecimal.ZERO) <= 0 ? null : accountingCurrencyAmount.divide(grantCurrencyAmount, Utils.MC));
  }
  
  private void recalculateAllocations(EntityManager em) {
    BigDecimal accountingCurrencyAmount = (BigDecimal) getProperty("accountingCurrencyAmount");
    ProjectExpense expense = (ProjectExpense) entity;
    List<ProjectSourceWrapper> sources = ProjectSourceWrapper.getProjectSources(em, expense.getProject(), null, null);  // TODO
    expense.setSourceAllocations(new ArrayList<ExpenseSourceAllocation>());
    
    for (int i = 0; i < sources.size(); ++i) {
      ProjectSource source = sources.get(i).getEntity();
      if (accountingCurrencyAmount.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }
      if (source.getRemainingAccountingCurrencyAmount().compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal take = accountingCurrencyAmount.min(source.getRemainingAccountingCurrencyAmount());
        if (i == sources.size() - 1) {
          take = accountingCurrencyAmount;  // Allow of going below zero balance for the last source.
        }
        accountingCurrencyAmount = accountingCurrencyAmount.subtract(take, Utils.MC);
        ExpenseSourceAllocation allocation = new ExpenseSourceAllocation();
        allocation.setExpense(expense);
        allocation.setSource(source);
        allocation.setAccountingCurrencyAmount(take);
  //      grantCurrencyAmount += allocation.getAccountingCurrencyAmount() / source.getExchangeRate();
        expense.getSourceAllocations().add(allocation);
      }
    }
    
    for (ExpenseSourceAllocation allocation : expense.getSourceAllocations()) {
      em.persist(allocation);
    }
  }
  
  public static void updateExpenseAllocations(EntityManager em, Project project, LocalDate startingFrom) {
    // Get list of expenses to update.
    List<ProjectExpenseWrapper> expensesToUpdate;
    if (startingFrom != null) {
      expensesToUpdate = getProjectExpenseListQuery(em, project, false, " AND e.paymentDate >= :date").
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
    BigDecimal editedAccountingCurrencyAmount = null;
    if (entity.getAccountingCurrencyAmount().compareTo(entity.getAccountingCurrencyAmountNotEdited()) != 0) {
      editedAccountingCurrencyAmount = entity.getAccountingCurrencyAmount();
    }
    super.save(em);  // expense is replaced here, transient fields are lost
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
  
  public static ProjectExpenseWrapper createNew(Project project) {
    ProjectExpense expense = new ProjectExpense();
    expense.setProject(project);
    expense.setOriginalCurrency(project.getAccountCurrency());
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
    return em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper(e, SUM(a.accountingCurrencyAmount), SUM(a.accountingCurrencyAmount / a.source.exchangeRate)) " +
            "FROM ProjectExpense e LEFT OUTER JOIN ExpenseSourceAllocation a ON a.expense = e " +
            "WHERE e.project = :project " + extraWhere + " " +
            "GROUP BY e " +
            "ORDER BY e.paymentDate " + sortString + ", e.id " + sortString,
        ProjectExpenseWrapper.class).setParameter("project", project);
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
}
