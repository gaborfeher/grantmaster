package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseSourceAllocation;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class ProjectExpenseWrapper extends EntityWrapper {
  private ProjectExpense expense;
  private double accountingCurrencyAmount;
  private double grantCurrencyAmount;
  DoubleProperty exchangeRate;
  
  double editedAccountingCurrencyAmount;
  
  public ProjectExpenseWrapper(ProjectExpense expense, double accountingCurrencyAmount, double grantCurrencyAmount) {
    this.expense = expense;
    this.accountingCurrencyAmount = accountingCurrencyAmount;
    this.grantCurrencyAmount = grantCurrencyAmount;
    this.exchangeRate = new SimpleDoubleProperty(accountingCurrencyAmount / grantCurrencyAmount);
    this.editedAccountingCurrencyAmount = accountingCurrencyAmount;
  }
  
  public Date getPaymentDate() {
    return expense.getPaymentDate();
  }
  
  public void setPaymentDate(Date date) {
    expense.setPaymentDate(date);
  }
  
  public String getAccountNo() {
    return expense.getAccountNo();
  }
  
  public void setAccountNo(String accountNo) {
    expense.setAccountNo(accountNo);
  }
  
  public String getPartnerName() {
    return expense.getPartnerName();
  }
  
  public void setPartnerName(String partnerName) {
    expense.setPartnerName(partnerName);
  }
  
  public BudgetCategory getBudgetCategory() {
    return expense.getBudgetCategory();
  }
  
  public void setBudgetCategory(BudgetCategory budgetCategory) {
    expense.setBudgetCategory(budgetCategory);
  }
  
  public Double getOriginalAmount() {
    return expense.getOriginalAmount();
  }
  
  public void setOriginalAmount(Double originalAmount) {
    expense.setOriginalAmount(originalAmount);
  }
  
  public Currency getOriginalCurrency() {
    return expense.getOriginalCurrency();
  }
  
  public void setOriginalCurrency(Currency currency) {
    expense.setOriginalCurrency(currency);
  }
  
  public double getAccountingCurrencyAmount() {
    return accountingCurrencyAmount;
  }
  
  public void setAccountingCurrencyAmount(Double accountingCurrencyAmount) {
    this.editedAccountingCurrencyAmount = accountingCurrencyAmount;
  }

  public double getGrantCurrencyAmount() {
    return grantCurrencyAmount;
  }
  
  public DoubleProperty exchangeRateProperty() {
    return exchangeRate;
  }

  public String getComment1() {
    return expense.getComment1();
  }
  
  public void setComment1(String comment1) {
    expense.setComment1(comment1);
  }

  public String getComment2() {
    return expense.getComment2();
  }
  
  public void setComment2(String comment2) {
    expense.setComment2(comment2);
  }
  
  public Project getProject() {
    return expense.getProject();
  }
  
  @Override
  protected Object getEntity() {
    return expense;
  }
  
  private void recalculateAllocations() {
    double amount = accountingCurrencyAmount;

    List<ProjectSourceWrapper> list = ProjectSourceWrapper.getProjectSources(expense.getProject(), null, null);  // TODO
    double grantCurrencyAmount = 0.0;
    expense.setSourceAllocations(new ArrayList<ExpenseSourceAllocation>());
    
    for (int i = 0; i < list.size(); ++i) {
      ProjectSourceWrapper source = list.get(i);
      if (amount == 0.0) {
        break;
      }
      if (source.getRemainingAccountingCurrencyAmount() > 0) {
        double take = Math.min(amount, source.getRemainingAccountingCurrencyAmount());
        if (i == list.size() - 1) {
          take = amount;  // Allow of going below zero balance for the last source.
        }
        amount -= take;
        ExpenseSourceAllocation allocation = new ExpenseSourceAllocation();
        allocation.setExpense(expense);
        allocation.setSource(source.getSource());
        allocation.setAccountingCurrencyAmount(take);
        grantCurrencyAmount += allocation.getAccountingCurrencyAmount() / source.getExchangeRate();
        expense.getSourceAllocations().add(allocation);
      }
    }
    
    for (ExpenseSourceAllocation allocation : expense.getSourceAllocations()) {
      DatabaseConnectionSingleton.getInstance().em().persist(allocation);
    }
  }
  
  public static void updateExpenseAllocations(Project project, Date startingFrom) {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    // Get list of expenses to update.
    List<ProjectExpenseWrapper> expensesToUpdate;
    if (startingFrom != null) {
        expensesToUpdate = getProjectExpenseListQuery(project, " AND e.paymentDate >= :date").
            setParameter("date", startingFrom).
            getResultList();
    } else {
      expensesToUpdate = getProjectExpenseList(project);
    }
    // Delete allocations and flush this to database. (Accounting currency amounts
    // are still kept in the database.)
    for (ProjectExpenseWrapper e : expensesToUpdate) {
      em.createQuery("DELETE FROM ExpenseSourceAllocation a WHERE a.expense = :expense").
          setParameter("expense", e.expense).
          executeUpdate();
    }
    em.flush();
    // Recompute expenses.
    for (ProjectExpenseWrapper e : expensesToUpdate) {
      e.recalculateAllocations();
    }
  }
  
  @Override
  public void persist() {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    try {
      em.getTransaction().begin();
      em.persist(expense);

      if (editedAccountingCurrencyAmount != accountingCurrencyAmount || expense.getSourceAllocations().isEmpty()) {
        // Set the allocation size to be right for this entity and flush.
        // This is just and initial fake setting which will be removed while normalizing.
        ExpenseSourceAllocation allocation;
        if (expense.getSourceAllocations().size() > 0) {
          System.out.println(" updating with fake value: " + accountingCurrencyAmount + " -> " + editedAccountingCurrencyAmount);
          allocation = expense.getSourceAllocations().get(0);
          allocation.setAccountingCurrencyAmount(allocation.getAccountingCurrencyAmount() - accountingCurrencyAmount + editedAccountingCurrencyAmount);
        } else {
          System.out.println(" adding fake value: " + accountingCurrencyAmount + " -> " + editedAccountingCurrencyAmount);
          allocation = new ExpenseSourceAllocation();
          allocation.setExpense(expense);
          allocation.setAccountingCurrencyAmount(editedAccountingCurrencyAmount);
          ProjectSource source0 = em.createQuery("SELECT s FROM ProjectSource s", ProjectSource.class).
              setMaxResults(1).
              getSingleResult();
          allocation.setSource(source0);
          expense.getSourceAllocations().add(allocation);
        }
        em.persist(allocation);
        em.flush();
        updateExpenseAllocations(expense.getProject(), expense.getPaymentDate());
      }
      
      if (expense.getSourceAllocations() == null || expense.getSourceAllocations().isEmpty()) {
        em.getTransaction().rollback();
        System.out.println("persist failed, missing allocation");
        return;
      }
      em.getTransaction().commit();
    } catch (Throwable t) {
      Logger.getLogger(ProjectExpenseWrapper.class.getName()).log(Level.SEVERE, null, t);
      em.getTransaction().rollback();
      DatabaseConnectionSingleton.getInstance().hardReset();
      return;
    }
    
    RefreshControlSingleton.getInstance().broadcastRefresh();
  }
  
  @Override
  public void delete() {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    try {
      em.getTransaction().begin();
      Date startDate = expense.getPaymentDate();
      em.remove(expense);
      em.flush();
      updateExpenseAllocations(expense.getProject(), startDate);
      
      em.getTransaction().commit();
    } catch (Throwable t) {
      Logger.getLogger(ProjectExpenseWrapper.class.getName()).log(Level.SEVERE, null, t);
      em.getTransaction().rollback();
      DatabaseConnectionSingleton.getInstance().hardReset();
      return;
    }
    
    RefreshControlSingleton.getInstance().broadcastRefresh();
  }
  
  public static List<ProjectExpenseWrapper> getProjectExpenseList(Project project) {
    return getProjectExpenseListQuery(project, "").getResultList();
  }
  
  public static TypedQuery<ProjectExpenseWrapper> getProjectExpenseListQuery(Project project, String extraWhere) {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    return em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper(e, SUM(a.accountingCurrencyAmount), SUM(a.accountingCurrencyAmount / a.source.exchangeRate)) " +
            "FROM ProjectExpense e LEFT OUTER JOIN ExpenseSourceAllocation a ON a.expense = e " +
            "WHERE e.project = :project " + extraWhere + " " +
            "GROUP BY e " +
            "ORDER BY e.paymentDate, e.id",
        ProjectExpenseWrapper.class).setParameter("project", project);
  }

  public static List<ProjectExpenseWrapper> getExpenseList(
      Project project,
      Date startDate,
      Date endDate,
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

    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
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
  
  public static void removeProjectExpenses(Project project) {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    em.createQuery("DELETE FROM ExpenseSourceAllocation a WHERE a IN (SELECT a FROM ExpenseSourceAllocation a, ProjectExpense e WHERE a.expense = e AND e.project = :project)").
        setParameter("project", project).
        executeUpdate();
    em.createQuery("DELETE FROM ProjectExpense p WHERE p.project = :project").
        setParameter("project", project).
        executeUpdate();
  }
}
