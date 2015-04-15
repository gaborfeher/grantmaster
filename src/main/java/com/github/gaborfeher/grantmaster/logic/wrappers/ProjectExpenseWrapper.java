package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseSourceAllocation;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.RefreshMessage;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

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
//    System.out.printf("exchangeRate= %.2f / %.2f\n", grantCurrencyAmount, accountingCurrencyAmount);
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
  
  private double updateExpenseAllocations() {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    em.createQuery("DELETE FROM ExpenseSourceAllocation a WHERE a.expense = :expense").
        setParameter("expense", expense).
        executeUpdate();
    
    List<ProjectSourceWrapper> list = ProjectSourceWrapper.getProjectSources(expense.getProject());  // TODO
    double grantCurrencyAmount = 0.0;
    double amount = editedAccountingCurrencyAmount;
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
        grantCurrencyAmount += allocation.getAccountingCurrencyAmount() / source.getExchangeRate();
        expense.getSourceAllocations().add(allocation);
      }
    }
    
    for (ExpenseSourceAllocation allocation : expense.getSourceAllocations()) {
      em.persist(allocation);
    }
    return grantCurrencyAmount;
  }
  
  @Override
  public void persist() {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    em.getTransaction().begin();
    double newGrantCurrencyAmount = grantCurrencyAmount.get();
    if (editedAccountingCurrencyAmount != accountingCurrencyAmount) {
      newGrantCurrencyAmount = updateExpenseAllocations();
    }
    if (expense.getSourceAllocations() == null || expense.getSourceAllocations().isEmpty()) {
      em.getTransaction().rollback();
      System.out.println("persist failed, missing allocation");
      return;
    }
    em.persist(expense);
    em.getTransaction().commit();
    
    accountingCurrencyAmount = editedAccountingCurrencyAmount;
    grantCurrencyAmount.set(newGrantCurrencyAmount);
    exchangeRate.set(accountingCurrencyAmount / newGrantCurrencyAmount);
    
    RefreshControlSingleton.getInstance().broadcastRefresh(
        new RefreshMessage(expense.getProject()));
  }
  
  public static List<ProjectExpenseWrapper> getProjectExpenseList(Project project) {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    TypedQuery<ProjectExpenseWrapper> query = em.createQuery("SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper(e, SUM(a.accountingCurrencyAmount), SUM(a.accountingCurrencyAmount / a.source.exchangeRate)) FROM ProjectExpense e LEFT OUTER JOIN ExpenseSourceAllocation a ON a.expense = e WHERE e.project = :project GROUP BY e ORDER by e.paymentDate", ProjectExpenseWrapper.class);
    query.setParameter("project", project);
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
