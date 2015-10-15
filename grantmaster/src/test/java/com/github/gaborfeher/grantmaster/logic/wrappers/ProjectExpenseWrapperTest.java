package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.framework.base.RowEditState;
import com.github.gaborfeher.grantmaster.framework.utils.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.CurrencyPair;
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
import java.time.Month;

public class ProjectExpenseWrapperTest extends TestBase {
  Currency HUF;
  Currency USD;
  Currency EUR;
  BudgetCategory SOME_GRANT;
  BudgetCategory SOME_EXPENSE;
  Project PROJECT1;
  ProjectReport PROJECT1_REPORT1;
  ProjectReport PROJECT1_REPORT2;
  Project PROJECT2_OVERRIDE;
  ProjectReport PROJECT2_OVERRIDE_REPORT1;

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
      PROJECT1 = TestUtils.createProject(
          em, "project1", USD, HUF, SOME_GRANT, Project.ExpenseMode.NORMAL_AUTO_BY_SOURCE);
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
      PROJECT2_OVERRIDE = TestUtils.createProject(
          em, "project2", USD, HUF, SOME_GRANT, Project.ExpenseMode.OVERRIDE_AUTO_BY_RATE_TABLE);
      PROJECT2_OVERRIDE_REPORT1 = TestUtils.createProjectReport(
          em, PROJECT2_OVERRIDE, LocalDate.of(2015, 7, 1));

      CurrencyPair currencyPair = TestUtils.createCurrencyPair(em, HUF, USD);
      TestUtils.createExchangeRate(em, currencyPair, LocalDate.of(2015, 8, 20), "1000");
      TestUtils.createExchangeRate(em, currencyPair, LocalDate.of(2015, 8, 21), "2000");
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

    assertTrue(DatabaseSingleton.INSTANCE.transaction(newExpense.get()::save));
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

  @Test
  public void testCreateMultiSourceExpenseAfterReportTime() {
    // This covers a bug in which expenses before the report time were never updated.
    final ObjectHolder<ProjectExpenseWrapper> newExpense = new ObjectHolder<>();
    assertTrue(DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      newExpense.set(ProjectExpenseWrapper.createNew(em, PROJECT1));
      return true;
    }));
    newExpense.get().setState(RowEditState.EDITING_NEW);
    newExpense.get().setProperty(
        "paymentDate", LocalDate.of(2015, 5, 1), LocalDate.class);  // After REPORT1's time.
    newExpense.get().setProperty(
        "budgetCategory", SOME_EXPENSE, BudgetCategory.class);
    newExpense.get().setProperty(
        "originalAmount", new BigDecimal("300000", Utils.MC),BigDecimal.class);
    newExpense.get().setProperty(
        "accountingCurrencyAmount", new BigDecimal("300000", Utils.MC), BigDecimal.class);
    newExpense.get().setProperty(
        "report", PROJECT1_REPORT1, ProjectReport.class);

    assertTrue(DatabaseSingleton.INSTANCE.transaction(newExpense.get()::save));
    assertEquals(RowEditState.SAVED, newExpense.get().getState());

    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      List<ProjectExpenseWrapper> expenses = ProjectExpenseWrapper.getProjectExpenseList(em, PROJECT1);
      assertEquals(1, expenses.size());
      ProjectExpenseWrapper expenseWrapper = expenses.get(0);
      assertBigDecimalEquals("300000", expenseWrapper.getAccountingCurrencyAmount());
      assertBigDecimalEquals("150", expenseWrapper.getExchangeRate());
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
          ProjectExpenseWrapper.getProjectExpenseListForAllocation(em, PROJECT1, null, null);
      assertEquals(4, expenses.size());
      assertEquals(expenseId4.get(), expenses.get(0).getId());
      assertEquals(expenseId2.get(), expenses.get(1).getId());
      assertEquals(expenseId3.get(), expenses.get(2).getId());
      assertEquals(expenseId1.get(), expenses.get(3).getId());
      return true;
    }));
  }

  /**
   * Test the thresholding used to decide which expenses are to be reallocated
   * when something has changed.
   */
  @Test
  public void testGetProjectExpenseListForAllocation() {
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
              LocalDate.of(2016, 2, 1),
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
              LocalDate.of(2016, 4, 2),
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
              LocalDate.of(2016, 1, 1),
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
              LocalDate.of(2016, 4, 1),
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
          ProjectExpenseWrapper.getProjectExpenseListForAllocation(
              em,
              PROJECT1,
              LocalDate.of(2015, 3, 30),  // just before PROJECT1_REPORT1
              null);
      assertEquals(4, expenses.size());
      assertEquals(expenseId4.get(), expenses.get(0).getId());
      assertEquals(expenseId2.get(), expenses.get(1).getId());
      assertEquals(expenseId3.get(), expenses.get(2).getId());
      assertEquals(expenseId1.get(), expenses.get(3).getId());

      expenses = ProjectExpenseWrapper.getProjectExpenseListForAllocation(
          em,
          PROJECT1,
          LocalDate.of(2015, 7, 29),  // just before PROJECT1_REPORT2
          null);
      assertEquals(2, expenses.size());
      assertEquals(expenseId3.get(), expenses.get(0).getId());
      assertEquals(expenseId1.get(), expenses.get(1).getId());

      expenses = ProjectExpenseWrapper.getProjectExpenseListForAllocation(
          em,
          PROJECT1,
          LocalDate.of(2015, 4, 1),  // PROJECT1_REPORT1
          LocalDate.of(2016, 4, 2)); // expenseId2
      assertEquals(3, expenses.size());
      assertEquals(expenseId2.get(), expenses.get(0).getId());
      assertEquals(expenseId3.get(), expenses.get(1).getId());
      assertEquals(expenseId1.get(), expenses.get(2).getId());

      expenses = ProjectExpenseWrapper.getProjectExpenseListForAllocation(
          em,
          PROJECT1,
          LocalDate.of(2015, 8, 1),  // PROJECT1_REPORT2
          LocalDate.of(2016, 1, 1)); // expenseId3
      assertEquals(2, expenses.size());
      assertEquals(expenseId3.get(), expenses.get(0).getId());
      assertEquals(expenseId1.get(), expenses.get(1).getId());

      expenses = ProjectExpenseWrapper.getProjectExpenseListForAllocation(
          em,
          PROJECT1,
          LocalDate.of(2015, 8, 1),  // PROJECT1_REPORT2
          LocalDate.of(2016, 2, 2)); // one day after expenseId4
      assertEquals(0, expenses.size());
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

    assertTrue(DatabaseSingleton.INSTANCE.transaction(newExpense.get()::save));
    assertEquals(RowEditState.SAVED, newExpense.get().getState());

    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      List<ProjectExpenseWrapper> expenses = ProjectExpenseWrapper.getProjectExpenseList(em, PROJECT1);
      assertEquals(1, expenses.size());
      ProjectExpenseWrapper expenseWrapper = expenses.get(0);
      assertBigDecimalEquals("700000", expenseWrapper.getAccountingCurrencyAmount());
      return true;
    });
  }

  @Test
  public void testSetOriginalAmountWhenTied() {
    final ObjectHolder<ProjectExpenseWrapper> expense = new ObjectHolder<>();
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      expense.set(TestUtils.createProjectExpense(em, PROJECT1, SOME_GRANT, LocalDate.of(2015, 7, 8), PROJECT1_REPORT1, "15.0", HUF, "15.0"));
      return true;
    }));
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      expense.get().setProperty("originalAmount", new BigDecimal("16", Utils.MC), BigDecimal.class);
      return true;
    }));
    assertBigDecimalEquals("16", expense.get().getProperty("originalAmount"));
    assertBigDecimalEquals("16", expense.get().getProperty("accountingCurrencyAmount"));
  }

  @Test
  public void testSetOriginalAmountWhenNotTied() {
    final ObjectHolder<ProjectExpenseWrapper> expense = new ObjectHolder<>();
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      expense.set(TestUtils.createProjectExpense(em, PROJECT1, SOME_GRANT, LocalDate.of(2015, 7, 8), PROJECT1_REPORT1, "15.0", EUR, "4500.0"));
      return true;
    }));
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      expense.get().setProperty("originalAmount", new BigDecimal("16", Utils.MC), BigDecimal.class);
      return true;
    }));
    assertBigDecimalEquals("16", expense.get().getProperty("originalAmount"));
    assertBigDecimalEquals("4500", expense.get().getProperty("accountingCurrencyAmount"));
  }


  @Test
  public void testSetAccountingCurrencyAmountWhenTied() {
    final ObjectHolder<ProjectExpenseWrapper> expense = new ObjectHolder<>();
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      expense.set(TestUtils.createProjectExpense(em, PROJECT1, SOME_GRANT, LocalDate.of(2015, 7, 8), PROJECT1_REPORT1, "15.0", HUF, "15.0"));
      return true;
    }));
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      expense.get().setProperty("accountingCurrencyAmount", new BigDecimal("16", Utils.MC), BigDecimal.class);
      return true;
    }));
    assertBigDecimalEquals("16", expense.get().getProperty("originalAmount"));
    assertBigDecimalEquals("16", expense.get().getProperty("accountingCurrencyAmount"));
  }

  @Test
  public void testSetAccountingCurrencyAmountWhenNotTied() {
    final ObjectHolder<ProjectExpenseWrapper> expense = new ObjectHolder<>();
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      expense.set(TestUtils.createProjectExpense(em, PROJECT1, SOME_GRANT, LocalDate.of(2015, 7, 8), PROJECT1_REPORT1, "15.0", EUR, "4500.0"));
      return true;
    }));
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      expense.get().setProperty("accountingCurrencyAmount", new BigDecimal("4600", Utils.MC), BigDecimal.class);
      return true;
    }));
    assertBigDecimalEquals("15", expense.get().getProperty("originalAmount"));
    assertBigDecimalEquals("4600", expense.get().getProperty("accountingCurrencyAmount"));
  }

  @Test
  public void testCreateExpenseInOverriddenModeManual() {
    final ObjectHolder<ProjectExpenseWrapper> newExpense = new ObjectHolder<>();
    assertTrue(DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      newExpense.set(ProjectExpenseWrapper.createNew(em, PROJECT2_OVERRIDE));
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
    newExpense.get().setProperty("exchangeRate", new BigDecimal("1000"), BigDecimal.class);
    newExpense.get().setProperty(
        "report", PROJECT2_OVERRIDE_REPORT1, ProjectReport.class);

    assertTrue(DatabaseSingleton.INSTANCE.transaction(newExpense.get()::save));
    assertEquals(RowEditState.SAVED, newExpense.get().getState());

    assertBigDecimalEquals("1000", newExpense.get().getExchangeRate());
    assertBigDecimalEquals("100000.5", newExpense.get().getAccountingCurrencyAmount());
    assertBigDecimalEquals("100.0005", newExpense.get().getGrantCurrencyAmount());
  }

  @Test
  public void testCreateExpenseInOverrideModeAuto() {
    final ObjectHolder<ProjectExpenseWrapper> expense0820 = new ObjectHolder<>();
    final ObjectHolder<ProjectExpenseWrapper> expense0821 = new ObjectHolder<>();
    assertTrue(DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      expense0820.set(ProjectExpenseWrapper.createNew(em, PROJECT2_OVERRIDE));
      expense0821.set(ProjectExpenseWrapper.createNew(em, PROJECT2_OVERRIDE));
      return true;
    }));
    expense0820.get().setState(RowEditState.EDITING_NEW);
    expense0820.get().setProperty(
        "paymentDate", LocalDate.of(2015, 8, 20), LocalDate.class);
    expense0820.get().setProperty(
        "budgetCategory", SOME_EXPENSE, BudgetCategory.class);
    expense0820.get().setProperty(
        "originalAmount", new BigDecimal("100000", Utils.MC),BigDecimal.class);
    expense0820.get().setProperty(
        "accountingCurrencyAmount", new BigDecimal("100000", Utils.MC), BigDecimal.class);
    expense0820.get().setProperty(
        "report", PROJECT2_OVERRIDE_REPORT1, ProjectReport.class);

    expense0821.get().setState(RowEditState.EDITING_NEW);
    expense0821.get().setProperty(
        "paymentDate", LocalDate.of(2015, 8, 21), LocalDate.class);
    expense0821.get().setProperty(
        "budgetCategory", SOME_EXPENSE, BudgetCategory.class);
    expense0821.get().setProperty(
        "originalAmount", new BigDecimal("100000", Utils.MC),BigDecimal.class);
    expense0821.get().setProperty(
        "accountingCurrencyAmount", new BigDecimal("100000", Utils.MC), BigDecimal.class);
    expense0821.get().setProperty(
        "report", PROJECT2_OVERRIDE_REPORT1, ProjectReport.class);

    assertTrue(DatabaseSingleton.INSTANCE.transaction(expense0820.get()::save));
    assertTrue(DatabaseSingleton.INSTANCE.transaction(expense0821.get()::save));
    assertEquals(RowEditState.SAVED, expense0820.get().getState());
    assertEquals(RowEditState.SAVED, expense0821.get().getState());

    // 1000 is the exchange rate for 2015-08-20
    assertBigDecimalEquals("1000", expense0820.get().getExchangeRate());
    // 2000 is the exchange rate for 2015-08-21
    assertBigDecimalEquals("2000", expense0821.get().getExchangeRate());
    assertBigDecimalEquals("100000", expense0820.get().getAccountingCurrencyAmount());
    assertBigDecimalEquals("100000", expense0821.get().getAccountingCurrencyAmount());
    assertBigDecimalEquals("100", expense0820.get().getGrantCurrencyAmount());
    assertBigDecimalEquals("50", expense0821.get().getGrantCurrencyAmount());
  }

}
