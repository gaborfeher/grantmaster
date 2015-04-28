package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
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
  ProjectReport PROJECT1_REPORT1;
  ProjectReport PROJECT1_REPORT2;

  public ProjectExpenseWrapperTest() {
  }
  
  @Before
  public void setUp() {
    assertTrue(DatabaseSingleton.INSTANCE.connectToMemoryFileForTesting());
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      HUF = TestUtils.createCurrency(em, "HUF");
      USD = TestUtils.createCurrency(em, "USD");
      EUR = TestUtils.createCurrency(em, "EUR");
      SOME_GRANT = new BudgetCategory(
          BudgetCategory.Direction.INCOME, "i.stuff", "Some kind of project grant");
      em.persist(SOME_GRANT);
      SOME_EXPENSE = new BudgetCategory(
          BudgetCategory.Direction.PAYMENT, "p.stuff", "Some kind of payment");
      em.persist(SOME_EXPENSE);
      PROJECT1 = TestUtils.createProject(em, "project1", USD, HUF, SOME_GRANT);
      PROJECT1_REPORT1 = TestUtils.createProjectReport(
          em, PROJECT1, LocalDate.of(2015, 4, 1));
      PROJECT1_REPORT2 = TestUtils.createProjectReport(
          em, PROJECT1, LocalDate.of(2015, 8, 1));
      TestUtils.createProjectSource(
          em, PROJECT1, LocalDate.of(2015, 2, 1), PROJECT1_REPORT1, "100", "1000");
      TestUtils.createProjectSource(
          em, PROJECT1, LocalDate.of(2015, 4, 1), PROJECT1_REPORT1, "200", "1000");
      TestUtils.createProjectSource(
          em, PROJECT1, LocalDate.of(2015, 6, 1), PROJECT1_REPORT1, "300", "1000");
      return true;
    }));
  }
  
  @After
  public void tearDown() {
    DatabaseSingleton.INSTANCE.cleanup();
  }
  
  @Test
  public void testCreateExpense() {
    final ObjectHolder<ProjectExpenseWrapper> newExpense = new ObjectHolder<>();
    assertTrue(DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      newExpense.set(ProjectExpenseWrapper.createNew(em, PROJECT1));
      return true;
    })); 
    newExpense.get().setState(EntityWrapper.State.EDITING_NEW);
    newExpense.get().setProperty(
        "paymentDate", LocalDate.of(2015, 3, 4), LocalDate.class);
    newExpense.get().setProperty(
        "budgetCategory", SOME_EXPENSE, BudgetCategory.class);
    newExpense.get().setProperty(
        "originalAmount", new BigDecimal("100000.5", Utils.MC),BigDecimal.class);
    newExpense.get().setProperty(
        "accountingCurrencyAmount", new BigDecimal("100000.5", Utils.MC), BigDecimal.class);
    newExpense.get().setProperty(
        "report", PROJECT1_REPORT1, ProjectReport.class);
    
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      newExpense.get().save(em);
      return true;
    }));
    assertEquals(EntityWrapper.State.SAVED, newExpense.get().getState());
    
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      List<ProjectExpenseWrapper> expenses = ProjectExpenseWrapper.getProjectExpenseList(em, PROJECT1);
      assertEquals(1, expenses.size());
      ProjectExpenseWrapper expenseWrapper = expenses.get(0);
      ProjectExpense expense = expenseWrapper.getEntity();
      assertEquals(LocalDate.of(2015, 3, 4), expense.getPaymentDate());
      assertNull(expense.getAccountNo());
      assertNull(expense.getPartnerName());
      assertNull(expense.getComment1());
      assertNull(expense.getComment2());
      assertEquals(HUF, expense.getOriginalCurrency());  // should be default
      assertEquals(0, new BigDecimal("100000.5", Utils.MC).compareTo(
          expense.getOriginalAmount()));
      assertEquals(0, new BigDecimal("100000.5", Utils.MC).compareTo(
          expenseWrapper.getAccountingCurrencyAmount()));
      return true;
    });
  }

  @Test
  public void testShiftExpenseForward() {
    final ObjectHolder<Long> expenseId1 = new ObjectHolder<>();
    final ObjectHolder<Long> expenseId2 = new ObjectHolder<>();
    
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      expenseId1.set(
          TestUtils.createProjectExpense(
              em,
              PROJECT1,
              SOME_GRANT,
              LocalDate.of(2014, 2, 1),
              PROJECT1_REPORT2,  // the later report
              "100000",
              HUF,
              "100000"
          ).getId());  
      return true;
    }));
    
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      expenseId2.set(
          TestUtils.createProjectExpense(
              em,
              PROJECT1,
              SOME_GRANT,
              LocalDate.of(2014, 2, 2),
              PROJECT1_REPORT1,  // the earlier report
              "200000",
              HUF,
              "200000"
          ).getId());
      return true;
    }));
    
    assertTrue(DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      ProjectExpenseWrapper expense1 = TestUtils.findExpenseById(em, PROJECT1, expenseId1.get());
      ProjectExpenseWrapper expense2 = TestUtils.findExpenseById(em, PROJECT1, expenseId2.get());
      assertEquals(0, new BigDecimal("100000", Utils.MC).compareTo(expense1.getAccountingCurrencyAmount()));
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
