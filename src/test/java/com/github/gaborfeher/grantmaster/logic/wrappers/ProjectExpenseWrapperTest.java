package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProjectExpenseWrapperTest {
  Currency HUF;
  Currency USD;
  Currency EUR;
  BudgetCategory SOME_GRANT;
  BudgetCategory SOME_EXPENSE;
  Project PROJECT1;
  ProjectSource PROJECT1_SOURCE1;

  public ProjectExpenseWrapperTest() {
  }
  
  @Before
  public void setUp() {
    assertTrue(DatabaseConnectionSingleton.getInstance().connectToMemoryFileForTesting());
    assertTrue(DatabaseConnectionSingleton.getInstance().runInTransaction(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        HUF = new Currency(); HUF.setCode("HUF"); em.persist(HUF);
        USD = new Currency(); USD.setCode("USD"); em.persist(USD);
        EUR = new Currency(); EUR.setCode("EUR"); em.persist(EUR);
        SOME_GRANT = new BudgetCategory(
            BudgetCategory.Direction.INCOME, "i.stuff", "Some kind of project grant");
        em.persist(SOME_GRANT);
        SOME_EXPENSE = new BudgetCategory(
            BudgetCategory.Direction.PAYMENT, "p.stuff", "Some kind of payment");
        em.persist(SOME_EXPENSE);
        PROJECT1 = new Project();
        PROJECT1.setName("project1");
        PROJECT1.setAccountCurrency(HUF);
        PROJECT1.setGrantCurrency(USD);
        PROJECT1.setIncomeType(SOME_GRANT);
        em.persist(PROJECT1);
        PROJECT1_SOURCE1 = new ProjectSource();
        PROJECT1_SOURCE1.setProject(PROJECT1);
        PROJECT1_SOURCE1.setAvailabilityDate(LocalDate.of(2015, 2, 1));
        PROJECT1_SOURCE1.setExchangeRate(new BigDecimal(230, Utils.MC));
        PROJECT1_SOURCE1.setGrantCurrencyAmount(new BigDecimal(10000, Utils.MC));
        em.persist(PROJECT1_SOURCE1);
        return true;
      }
    }));
  }
  
  @After
  public void tearDown() {
  }
  
  @Test
  public void testCreateExpense() {
    final ProjectExpenseWrapper newWrapper = ProjectExpenseWrapper.createNew(PROJECT1);
    newWrapper.setState(EntityWrapper.State.EDITING_NEW);
    newWrapper.setProperty("paymentDate", LocalDate.of(2015, 3, 4));
    newWrapper.setProperty("budgetCategory", SOME_EXPENSE);
    newWrapper.setProperty("originalAmount", new BigDecimal("10000.5", Utils.MC));
    newWrapper.setProperty("accountingCurrencyAmount", new BigDecimal("10000.5", Utils.MC));
    
    assertTrue(DatabaseConnectionSingleton.getInstance().runInTransaction(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        newWrapper.save(em);
        return true;
      }
    }));
    assertEquals(EntityWrapper.State.SAVED, newWrapper.getState());
    
    DatabaseConnectionSingleton.getInstance().runWithEntityManager(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        List<ProjectExpenseWrapper> expenses = ProjectExpenseWrapper.getProjectExpenseList(em, PROJECT1);
        assertEquals(1, expenses.size());
        ProjectExpenseWrapper added = expenses.get(0);
        ProjectExpense expense = (ProjectExpense) added.getEntity();
        
        assertEquals(LocalDate.of(2015, 3, 4), expense.getPaymentDate());
        assertNull(expense.getAccountNo());
        assertNull(expense.getPartnerName());
        assertNull(expense.getComment1());
        assertNull(expense.getComment2());
        assertEquals(HUF, expense.getOriginalCurrency());  // should be default
        assertEquals(0, new BigDecimal("10000.5", Utils.MC).compareTo(expense.getOriginalAmount()));
        assertEquals(0, new BigDecimal("10000.5", Utils.MC).compareTo((BigDecimal)added.getProperty("accountingCurrencyAmount")));
        return true;
      }
    });
  }

}
