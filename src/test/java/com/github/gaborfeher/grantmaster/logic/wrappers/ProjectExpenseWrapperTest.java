package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
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

  public ProjectExpenseWrapperTest() {
  }
  
  @Before
  public void setUp() {
    assertTrue(DatabaseSingleton.INSTANCE.connectToMemoryFileForTesting());
    assertTrue(DatabaseSingleton.INSTANCE.transaction(new TransactionRunner() {
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
        PROJECT1 = TestUtils.createProject(em, "project1", USD, HUF, SOME_GRANT);
        TestUtils.createProjectSource(
            em, PROJECT1, LocalDate.of(2015, 2, 1), "100", "1000");
        TestUtils.createProjectSource(
            em, PROJECT1, LocalDate.of(2015, 4, 1), "200", "1000");
        TestUtils.createProjectSource(
            em, PROJECT1, LocalDate.of(2015, 6, 1), "300", "1000");        
        return true;
      }
    }));
  }
  
  @After
  public void tearDown() {
    DatabaseSingleton.INSTANCE.cleanup();
  }
  
  @Test
  public void testCreateExpense() {
    final ProjectExpenseWrapper newWrapper =
        ProjectExpenseWrapper.createNew(PROJECT1);
    newWrapper.setState(EntityWrapper.State.EDITING_NEW);
    newWrapper.setProperty(
        "paymentDate", LocalDate.of(2015, 3, 4), LocalDate.class);
    newWrapper.setProperty(
        "budgetCategory", SOME_EXPENSE, BudgetCategory.class);
    newWrapper.setProperty(
        "originalAmount", new BigDecimal("100000.5", Utils.MC),BigDecimal.class);
    newWrapper.setProperty(
        "accountingCurrencyAmount", new BigDecimal("100000.5", Utils.MC), BigDecimal.class);
    
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      newWrapper.save(em);
      return true;
    }));
    assertEquals(EntityWrapper.State.SAVED, newWrapper.getState());
    
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      List<ProjectExpenseWrapper> expenses = ProjectExpenseWrapper.getProjectExpenseList(em, PROJECT1);
      assertEquals(1, expenses.size());
      ProjectExpense expense = (ProjectExpense) expenses.get(0).getEntity();
      
      assertEquals(LocalDate.of(2015, 3, 4), expense.getPaymentDate());
      assertNull(expense.getAccountNo());
      assertNull(expense.getPartnerName());
      assertNull(expense.getComment1());
      assertNull(expense.getComment2());
      assertEquals(HUF, expense.getOriginalCurrency());  // should be default
      assertEquals(0, new BigDecimal("100000.5", Utils.MC).compareTo(expense.getOriginalAmount()));
      assertEquals(0, new BigDecimal("100000.5", Utils.MC).compareTo(expense.getAccountingCurrencyAmount()));
      return true;
    });
  }

  @Test
  public void testShiftExpenseForward() {
    final ObjectHolder<Long> expenseId1 = new ObjectHolder<>();
    final ObjectHolder<Long> expenseId2 = new ObjectHolder<>();
    
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      expenseId1.set(
          (Long) TestUtils.createProjectExpense(
              em,
              PROJECT1,
              SOME_GRANT,
              LocalDate.of(2014, 2, 2),
              "100000",
              HUF,
              "100000"
          ).getEntity().getId());  
      return true;
    }));
    
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      expenseId2.set(
          (Long) TestUtils.createProjectExpense(
              em,
              PROJECT1,
              SOME_GRANT,
              LocalDate.of(2014, 2, 1),
              "200000",
              HUF,
              "200000"
          ).getEntity().getId());
      return true;
    }));
    
    assertTrue(DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      ProjectExpense expense1 = TestUtils.findExpenseById(em, PROJECT1, expenseId1.get());
      ProjectExpense expense2 = TestUtils.findExpenseById(em, PROJECT1, expenseId2.get());
      assertEquals(0, new BigDecimal("100000", Utils.MC).compareTo(expense1.getAccountingCurrencyAmount()));
      System.out.println(expense1.getAccountingCurrencyAmount() + " " + expense1.getExchangeRate() + " " + expense1.getGrantCurrencyAmount());
      System.out.println(expense2.getAccountingCurrencyAmount() + " " + expense2.getExchangeRate() + " " + expense2.getGrantCurrencyAmount());
      assertEquals(0, new BigDecimal("200", Utils.MC).compareTo(expense1.getExchangeRate()));
      assertEquals(0, new BigDecimal("500", Utils.MC).compareTo(expense1.getGrantCurrencyAmount()));
      
      assertEquals(0, new BigDecimal("200000", Utils.MC).compareTo(expense2.getAccountingCurrencyAmount()));
      BigDecimal value133_33 = new BigDecimal("200000", Utils.MC).divide(new BigDecimal("1500", Utils.MC), Utils.MC);
      assertEquals(0, value133_33.compareTo(expense2.getExchangeRate()));
      assertEquals(0, new BigDecimal("1500", Utils.MC).compareTo(expense2.getGrantCurrencyAmount()));
      return true;
    }));
    
    
  }
}
