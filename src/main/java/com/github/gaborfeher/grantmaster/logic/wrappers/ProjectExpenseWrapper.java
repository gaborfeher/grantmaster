package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseSourceAllocation;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.RefreshMessage;
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
import org.eclipse.persistence.exceptions.DatabaseException;

public class ProjectExpenseWrapper extends EntityWrapper {
  ProjectExpense expense;
  double accountingCurrencyAmount;
  
  DoubleProperty grantCurrencyAmount;
  DoubleProperty exchangeRate;
  
  double editedAccountingCurrencyAmount;
  
  public ProjectExpenseWrapper(ProjectExpense expense, double accountingCurrencyAmount, double grantCurrencyAmount) {
    this.expense = expense;
    this.accountingCurrencyAmount = accountingCurrencyAmount;
    this.grantCurrencyAmount = new SimpleDoubleProperty(grantCurrencyAmount);
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
  
  public ExpenseType getExpenseType() {
    return expense.getExpenseType();
  }
  
  public void setExpenseType(ExpenseType expenseType) {
    expense.setExpenseType(expenseType);
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
    
 //   System.out.println("nice try: " + amount);
  }

  public DoubleProperty grantCurrencyAmountProperty() {
    return grantCurrencyAmount;
  }
  
  public DoubleProperty exchangeRateProperty() {
    return exchangeRate;
  }

  @Override
  protected Object getEntity() {
    return expense;
  }
  
  private void recalculateAllocations() {
    double amount = accountingCurrencyAmount;

    List<ProjectSourceWrapper> list = ProjectSourceWrapper.getProjectSources(expense.getProject());  // TODO
    double grantCurrencyAmountDouble = 0.0;
    expense.setSourceAllocations(new ArrayList<ExpenseSourceAllocation>());
    for (ProjectSourceWrapper source : list) {
      if (amount == 0.0) {
        break;
      }
      if (source.getRemainingAccountingCurrencyAmount() > 0) {
        double take = Math.min(amount, source.getRemainingAccountingCurrencyAmount());
        amount -= take;
        ExpenseSourceAllocation allocation = new ExpenseSourceAllocation();
        allocation.setExpense(expense);
        allocation.setSource(source.getSource());
        allocation.setAccountingCurrencyAmount(take);
        grantCurrencyAmountDouble += allocation.getAccountingCurrencyAmount() / source.getExchangeRate();
        expense.getSourceAllocations().add(allocation);
      }
    }
    grantCurrencyAmount.set(grantCurrencyAmountDouble);
    
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
      return;
    }
    
    RefreshControlSingleton.getInstance().broadcastRefresh(
        new RefreshMessage(expense.getProject()));
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
      return;
    }
    
    RefreshControlSingleton.getInstance().broadcastRefresh(
        new RefreshMessage(expense.getProject()));
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
