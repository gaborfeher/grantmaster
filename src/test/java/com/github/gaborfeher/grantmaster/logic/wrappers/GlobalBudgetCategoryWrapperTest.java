package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static com.github.gaborfeher.grantmaster.logic.wrappers.TestUtils.assertBigDecimalEquals;


public class GlobalBudgetCategoryWrapperTest {
  Currency HUF;
  Currency USD;
  Currency EUR;
  BudgetCategory INCOME_CAT1;
  BudgetCategory INCOME_CAT2;
  BudgetCategory PAYMENT_CAT1;
  BudgetCategory PAYMENT_CAT2;
  BudgetCategory PAYMENT_CAT3;
  Project PROJECT1;
  Project PROJECT2;
  ProjectReport PROJECT1_REPORT1;
  ProjectReport PROJECT1_REPORT2;
  ProjectReport PROJECT2_REPORT1;

  public GlobalBudgetCategoryWrapperTest() {
  }
  
  @Before
  public void setUp() {
    assertTrue(DatabaseSingleton.INSTANCE.connectToMemoryFileForTesting());
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      HUF = TestUtils.createCurrency(em, "HUF");
      USD = TestUtils.createCurrency(em, "USD");
      EUR = TestUtils.createCurrency(em, "EUR");
      INCOME_CAT1 = new BudgetCategory(
          BudgetCategory.Direction.INCOME, "group1", "income1");
      em.persist(INCOME_CAT1);
      INCOME_CAT2 = new BudgetCategory(
          BudgetCategory.Direction.INCOME, "group2", "income2");
      em.persist(INCOME_CAT2);
      PAYMENT_CAT1 = new BudgetCategory(
          BudgetCategory.Direction.PAYMENT, "group3", "payment1");
      PAYMENT_CAT2 = new BudgetCategory(
          BudgetCategory.Direction.PAYMENT, "group3", "payment2");
      PAYMENT_CAT3 = new BudgetCategory(
          BudgetCategory.Direction.PAYMENT, "group4", "payment3");
      em.persist(PAYMENT_CAT1);
      em.persist(PAYMENT_CAT2);
      em.persist(PAYMENT_CAT3);
      PROJECT1 = TestUtils.createProject(em, "project1", USD, HUF, INCOME_CAT1);
      PROJECT2 = TestUtils.createProject(em, "project2", USD, HUF, INCOME_CAT2);
      PROJECT1_REPORT1 = TestUtils.createProjectReport(
          em, PROJECT1, LocalDate.of(2011, 4, 1));
      PROJECT1_REPORT2 = TestUtils.createProjectReport(
          em, PROJECT1, LocalDate.of(2011, 8, 1));
      PROJECT2_REPORT1 = TestUtils.createProjectReport(
          em, PROJECT2, LocalDate.of(2011, 4, 1));
      // Sources and expenses.
      TestUtils.createProjectSource(
          em, PROJECT1, LocalDate.of(2015, 2, 1), PROJECT1_REPORT1, "100", "10000");
      TestUtils.createProjectSource(
          em, PROJECT1, LocalDate.of(2015, 2, 2), PROJECT1_REPORT1, "100", "10000");
      TestUtils.createProjectSource(
          em, PROJECT2, LocalDate.of(2015, 2, 1), PROJECT1_REPORT1, "100.5", "10000");
      TestUtils.createProjectExpense(em, PROJECT1, PAYMENT_CAT1, LocalDate.of(2014, 12, 1), PROJECT1_REPORT1, "42", HUF, "1100000.1");
      TestUtils.createProjectExpense(em, PROJECT2, PAYMENT_CAT1, LocalDate.of(2015, 12, 7), PROJECT1_REPORT1, "42", HUF, "1010000.1");
      TestUtils.createProjectExpense(em, PROJECT1, PAYMENT_CAT1, LocalDate.of(2015, 12, 3), PROJECT1_REPORT2, "42", HUF, "1001000.1");
      TestUtils.createProjectExpense(em, PROJECT1, PAYMENT_CAT2, LocalDate.of(2015, 12, 2), PROJECT1_REPORT2, "42", HUF, "1000010.1");
      TestUtils.createProjectExpense(em, PROJECT1, PAYMENT_CAT3, LocalDate.of(2015, 12, 4), PROJECT1_REPORT2, "42", HUF, "1000001.1");
      return true;
    }));
  }
  
  @After
  public void tearDown() {
    DatabaseSingleton.INSTANCE.close();
  }
  
  @Test
  public void testGetYearlyBudgetCategorySummaries_Columns() {
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      List<GlobalBudgetCategoryWrapper> incomes = new ArrayList<>();
      List<GlobalBudgetCategoryWrapper> expenses = new ArrayList<>();
      Set<String> columnNames = new HashSet<>();
      GlobalBudgetCategoryWrapper.getYearlyBudgetCategorySummaries(
          em,
          expenses,
          incomes,
          columnNames);
      // Columns.
      assertEquals(2, columnNames.size());
      assertTrue(columnNames.contains("2014 (HUF)"));
      assertTrue(columnNames.contains("2015 (HUF)"));
      // Expenses.
      assertBigDecimalEquals(
          "2011000.2",
          TestUtils.findByBudgetCategory(expenses, PAYMENT_CAT1).getProperty("2015 (HUF)"));
      assertBigDecimalEquals(
          "1100000.1",
          TestUtils.findByBudgetCategory(expenses, PAYMENT_CAT1).getProperty("2014 (HUF)"));
      assertBigDecimalEquals(
          "1000001.1",
          TestUtils.findByBudgetCategory(expenses, PAYMENT_CAT3).getProperty("2015 (HUF)"));
      // Incomes.
      assertBigDecimalEquals(
          "2000000",
          TestUtils.findByBudgetCategory(incomes, INCOME_CAT1).getProperty("2015 (HUF)"));
      assertBigDecimalEquals(
          "1005000",
          TestUtils.findByBudgetCategory(incomes, INCOME_CAT2).getProperty("2015 (HUF)"));
      return true;
    });
  }
  
  @Test
  public void testGetYearlyBudgetCategorySummaries_Expenses() {
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      List<GlobalBudgetCategoryWrapper> incomes = new ArrayList<>();
      List<GlobalBudgetCategoryWrapper> expenses = new ArrayList<>();
      Set<String> columnNames = new HashSet<>();
      GlobalBudgetCategoryWrapper.getYearlyBudgetCategorySummaries(
          em,
          expenses,
          incomes,
          columnNames);
      System.out.println(incomes.size() + " " + expenses.size());
      return true;
    });
  }
  
}
