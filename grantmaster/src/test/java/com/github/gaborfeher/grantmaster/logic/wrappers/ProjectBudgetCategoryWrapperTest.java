package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.framework.utils.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.After;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static com.github.gaborfeher.grantmaster.logic.wrappers.TestUtils.assertBigDecimalEquals;

public class ProjectBudgetCategoryWrapperTest extends TestBase {
  Currency HUF;
  Currency USD;
  Currency EUR;
  BudgetCategory SOME_GRANT;
  BudgetCategory PAYMENT_CAT1;
  BudgetCategory PAYMENT_CAT2;
  BudgetCategory PAYMENT_CAT3;
  Project PROJECT1;
  ProjectReport PROJECT1_REPORT1;
  ProjectReport PROJECT1_REPORT2;

  public ProjectBudgetCategoryWrapperTest() {
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
      PAYMENT_CAT1 = new BudgetCategory(
          BudgetCategory.Direction.PAYMENT, "payment1", "Some kind of payment1");
      PAYMENT_CAT2 = new BudgetCategory(
          BudgetCategory.Direction.PAYMENT, "payment2", "Some kind of payment2");
      PAYMENT_CAT3 = new BudgetCategory(
          BudgetCategory.Direction.PAYMENT, "payment3", "Some kind of payment3");
      em.persist(PAYMENT_CAT1);
      em.persist(PAYMENT_CAT2);
      em.persist(PAYMENT_CAT3);
      PROJECT1 = TestUtils.createProject(
          em, "project1", USD, HUF, SOME_GRANT, Project.ExpenseMode.NORMAL_AUTO_BY_SOURCE);
      PROJECT1_REPORT1 = TestUtils.createProjectReport(
          em, PROJECT1, LocalDate.of(2015, 4, 1));
      PROJECT1_REPORT2 = TestUtils.createProjectReport(
          em, PROJECT1, LocalDate.of(2015, 8, 1));
      // Sources and expenses.
      TestUtils.createProjectSource(
          em, PROJECT1, LocalDate.of(2015, 2, 1), PROJECT1_REPORT1, "100", "10000");
      TestUtils.createProjectExpense(em, PROJECT1, PAYMENT_CAT1, LocalDate.of(2015, 12, 1), PROJECT1_REPORT1, "42", HUF, "2500.1");
      TestUtils.createProjectExpense(em, PROJECT1, PAYMENT_CAT1, LocalDate.of(2015, 12, 7), PROJECT1_REPORT1, "42", HUF, "2500.1");
      TestUtils.createProjectExpense(em, PROJECT1, PAYMENT_CAT1, LocalDate.of(2015, 12, 3), PROJECT1_REPORT2, "42", HUF, "2500.1");
      TestUtils.createProjectExpense(em, PROJECT1, PAYMENT_CAT2, LocalDate.of(2015, 12, 2), PROJECT1_REPORT2, "42", HUF, "2501.1");
      TestUtils.createProjectExpense(em, PROJECT1, PAYMENT_CAT3, LocalDate.of(2015, 12, 4), PROJECT1_REPORT2, "42", HUF, "2502.1");
      return true;
    }));
  }

  @After
  public void tearDown() {
    DatabaseSingleton.INSTANCE.close();
  }

  @Test
  public void testProjectExpenseSummariesNoFilter() {
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      List<ProjectBudgetCategoryWrapper> list = ProjectBudgetCategoryWrapper.getProjectBudgetLimits(em, PROJECT1, null);
      assertBigDecimalEquals(
          "7500.3",
          TestUtils.findByBudgetCategory(list, PAYMENT_CAT1).getProperty("spentAccountingCurrency"));
      assertBigDecimalEquals(
          "75.003",
          TestUtils.findByBudgetCategory(list, PAYMENT_CAT1).getProperty("spentGrantCurrency"));
      assertBigDecimalEquals(
          "2501.1",
          TestUtils.findByBudgetCategory(list, PAYMENT_CAT2).getProperty("spentAccountingCurrency"));
      assertBigDecimalEquals(
          "2502.1",
          TestUtils.findByBudgetCategory(list, PAYMENT_CAT3).getProperty("spentAccountingCurrency"));
      return true;
    });
  }

  @Test
  public void testProjectExpenseSummariesWithFilter() {
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      List<ProjectBudgetCategoryWrapper> list = ProjectBudgetCategoryWrapper.getProjectBudgetLimits(em, PROJECT1, PROJECT1_REPORT1);
      assertBigDecimalEquals(
          "5000.2",
          TestUtils.findByBudgetCategory(list, PAYMENT_CAT1).getProperty("spentAccountingCurrency"));
      assertBigDecimalEquals(
          "50.002",
          TestUtils.findByBudgetCategory(list, PAYMENT_CAT1).getProperty("spentGrantCurrency"));
      assertNull(TestUtils.findByBudgetCategory(list, PAYMENT_CAT2));
      assertNull(TestUtils.findByBudgetCategory(list, PAYMENT_CAT3));
      return true;
    });
  }
}
