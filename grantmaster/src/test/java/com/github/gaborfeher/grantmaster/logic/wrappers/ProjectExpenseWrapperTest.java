package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.framework.base.RowEditState;
import com.github.gaborfeher.grantmaster.framework.utils.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.framework.utils.Utils;
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
import static com.github.gaborfeher.grantmaster.logic.wrappers.TestUtils.assertBigDecimalEquals;

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
      // The below is the order in which the sources will be filled, because
      // report date takes precedence at sorting.
      TestUtils.createProjectSource(
          em, PROJECT1, LocalDate.of(2015, 2, 1), PROJECT1_REPORT1, "100", "1000");
      TestUtils.createProjectSource(
          em, PROJECT1, LocalDate.of(2015, 4, 1), PROJECT1_REPORT1, "200", "1000");
      TestUtils.createProjectSource(
          em, PROJECT1, LocalDate.of(2015, 3, 1), PROJECT1_REPORT2, "300", "1000");
      return true;
    }));
  }
  
  @After
  public void tearDown() {
    DatabaseSingleton.INSTANCE.close();
  }
  
  @Test
  public void testCreateExpense() {
    final ObjectHolder<ProjectExpenseWrapper> newExpense = new ObjectHolder<>();
    assertTrue(DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      newExpense.set(ProjectExpenseWrapper.createNew(em, PROJECT1));
      return true;
    })); 
    newExpense.get().setState(RowEditState.EDITING_NEW);
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
    assertEquals(RowEditState.SAVED, newExpense.get().getState());
    
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
      assertBigDecimalEquals("100000.5", expense.getOriginalAmount());
      assertBigDecimalEquals("100000.5", expenseWrapper.getAccountingCurrencyAmount());
      return true;
    });
  }

  /**
   * Make sure that the sorting used for recalculating
   * project expense-source allocations
   * is correct.
   */
  @Test
  public void testSortingForAllocation() {
    final ObjectHolder<Long> expenseId1 = new ObjectHolder<>();
    final ObjectHolder<Long> expenseId2 = new ObjectHolder<>();
    final ObjectHolder<Long> expenseId3 = new ObjectHolder<>();
    final ObjectHolder<Long> expenseId4 = new ObjectHolder<>();
    // Setup.
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      expenseId1.set(
          TestUtils.createProjectExpense(
              em,
              PROJECT1,
              SOME_GRANT,
              LocalDate.of(2014, 2, 1),
              PROJECT1_REPORT2,
              "200421",
              HUF,
              "200000.1"
          ).getId());  
      expenseId2.set(
          TestUtils.createProjectExpense(
              em,
              PROJECT1,
              SOME_GRANT,
              LocalDate.of(2014, 4, 2),
              PROJECT1_REPORT1,
              "200422",
              HUF,
              "200000.2"
          ).getId());
      expenseId3.set(
          TestUtils.createProjectExpense(
              em,
              PROJECT1,
              SOME_GRANT,
              LocalDate.of(2014, 1, 1),
              PROJECT1_REPORT2,
              "200423",
              HUF,
              "200000.3"
          ).getId());
      expenseId4.set(
          TestUtils.createProjectExpense(
              em,
              PROJECT1,
              SOME_GRANT,
              LocalDate.of(2014, 4, 1),
              PROJECT1_REPORT1,
              "200424",
              HUF,
              "200000.4"
          ).getId());
      return true;
    }));
    // Check order.
    assertTrue(DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      List<ProjectExpenseWrapper> expenses =
          ProjectExpenseWrapper.getProjectExpenseListForAllocation(em, PROJECT1, null);
      assertEquals(4, expenses.size());
      assertEquals(expenseId4.get(), expenses.get(0).getId());
      assertEquals(expenseId2.get(), expenses.get(1).getId());
      assertEquals(expenseId3.get(), expenses.get(2).getId());
      assertEquals(expenseId1.get(), expenses.get(3).getId());
      return true;
    }));
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
      
      assertBigDecimalEquals("100000", expense1.getAccountingCurrencyAmount());
      assertBigDecimalEquals("200", expense1.getExchangeRate());
      assertBigDecimalEquals("500", expense1.getGrantCurrencyAmount());
      assertBigDecimalEquals("200000", expense2.getAccountingCurrencyAmount());
      BigDecimal value133_33 = new BigDecimal("200000", Utils.MC).divide(new BigDecimal("1500", Utils.MC), Utils.MC);
      assertBigDecimalEquals(value133_33, expense2.getExchangeRate());
      assertBigDecimalEquals("1500", expense2.getGrantCurrencyAmount());
      return true;
    }));
  }
  
  @Test
  public void testShiftExpenseBackward() {
    final ObjectHolder<Long> expenseId1 = new ObjectHolder<>();
    final ObjectHolder<Long> expenseId2 = new ObjectHolder<>();
    final ObjectHolder<Long> expenseId3 = new ObjectHolder<>();
    // Setup.
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      expenseId1.set(
          TestUtils.createProjectExpense(
              em,
              PROJECT1,
              SOME_GRANT,
              LocalDate.of(2014, 2, 1),
              PROJECT1_REPORT1,
              "200042",
              HUF,
              "200000.1"
          ).getId());  
      expenseId2.set(
          TestUtils.createProjectExpense(
              em,
              PROJECT1,
              SOME_GRANT,
              LocalDate.of(2014, 2, 2),
              PROJECT1_REPORT1,
              "200043",
              HUF,
              "200000.2"
          ).getId());
      expenseId3.set(
          TestUtils.createProjectExpense(
              em,
              PROJECT1,
              SOME_GRANT,
              LocalDate.of(2014, 1, 1),
              PROJECT1_REPORT2,
              "2000044",
              HUF,
              "200000.3"
          ).getId());
      return true;
    }));
    // Sanity checks.
    assertTrue(DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      List<ProjectExpenseWrapper> expenses = ProjectExpenseWrapper.getProjectExpenseList(em, PROJECT1);
      assertEquals(3, expenses.size());
      ProjectExpenseWrapper expense1 = TestUtils.findExpenseById(em, PROJECT1, expenseId1.get());
      ProjectExpenseWrapper expense2 = TestUtils.findExpenseById(em, PROJECT1, expenseId2.get());
      ProjectExpenseWrapper expense3 = TestUtils.findExpenseById(em, PROJECT1, expenseId3.get());
      assertBigDecimalEquals("200000.1", expense1.getAccountingCurrencyAmount());
      assertBigDecimalEquals("200000.2", expense2.getAccountingCurrencyAmount());
      assertBigDecimalEquals("200000.3", expense3.getAccountingCurrencyAmount());
      return true;
    }));
    // Commit deletion
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      TestUtils.findExpenseById(em, PROJECT1, expenseId2.get()).delete(em);
      return true;
    }));
    // Check results.
    assertTrue(DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      List<ProjectExpenseWrapper> expenses = ProjectExpenseWrapper.getProjectExpenseList(em, PROJECT1);
      assertEquals(2, expenses.size());
      ProjectExpenseWrapper expense1 = TestUtils.findExpenseById(em, PROJECT1, expenseId1.get());
      ProjectExpenseWrapper expense2 = TestUtils.findExpenseById(em, PROJECT1, expenseId2.get());
      ProjectExpenseWrapper expense3 = TestUtils.findExpenseById(em, PROJECT1, expenseId3.get());
      assertBigDecimalEquals("200000.1", expense1.getAccountingCurrencyAmount());
      assertNull(expense2);
      assertBigDecimalEquals("200000.3", expense3.getAccountingCurrencyAmount());
      return true;
    }));
  }
  
  @Test
  public void testCreateOvershootExpense() {
    final ObjectHolder<ProjectExpenseWrapper> newExpense = new ObjectHolder<>();
    assertTrue(DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      newExpense.set(ProjectExpenseWrapper.createNew(em, PROJECT1));
      return true;
    })); 
    newExpense.get().setState(RowEditState.EDITING_NEW);
    newExpense.get().setProperty(
        "paymentDate", LocalDate.of(2015, 3, 4), LocalDate.class);
    newExpense.get().setProperty(
        "budgetCategory", SOME_EXPENSE, BudgetCategory.class);
    newExpense.get().setProperty(
        "originalAmount", new BigDecimal("1.5", Utils.MC),BigDecimal.class);
    newExpense.get().setProperty(
        "accountingCurrencyAmount", new BigDecimal("700000", Utils.MC), BigDecimal.class);
    // 700000 is more than the combined value of all the sources.
    newExpense.get().setProperty(
        "report", PROJECT1_REPORT1, ProjectReport.class);

    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      newExpense.get().save(em);
      return true;
    }));
    assertEquals(RowEditState.SAVED, newExpense.get().getState());

    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      List<ProjectExpenseWrapper> expenses = ProjectExpenseWrapper.getProjectExpenseList(em, PROJECT1);
      assertEquals(1, expenses.size());
      ProjectExpenseWrapper expenseWrapper = expenses.get(0);
      assertBigDecimalEquals("700000", expenseWrapper.getAccountingCurrencyAmount());
      return true;
    });
  }
}
