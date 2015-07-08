package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.framework.base.RowEditState;
import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectNote;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import static org.junit.Assert.assertEquals;

public class TestUtils {
  public static Project createProject(
      EntityManager em,
      String name,
      Currency income,
      Currency expense,
      BudgetCategory incomeCategory) {
    Project project = new Project();
    project.setName(name);
    project.setAccountCurrency(expense);
    project.setGrantCurrency(income);
    project.setIncomeType(incomeCategory);
    em.persist(project);
    return project;
  }
  
  public static ProjectSource createProjectSource(
      EntityManager em,
      Project project,
      LocalDate date,
      ProjectReport report,
      String exchangeRate,
      String accountingCurrencyAmount) {
    ProjectSource projectSource = new ProjectSource();
    projectSource.setProject(project);
    projectSource.setReport(report);
    projectSource.setAvailabilityDate(date);
    projectSource.setExchangeRate(new BigDecimal(exchangeRate, Utils.MC));
    projectSource.setGrantCurrencyAmount(
        new BigDecimal(accountingCurrencyAmount, Utils.MC));
    em.persist(projectSource);
    return projectSource;
  }

  static ProjectExpenseWrapper createProjectExpense(
      EntityManager em,
      Project project,
      BudgetCategory budgetCategory,
      LocalDate date,
      ProjectReport report,
      String originalAmount,
      Currency originalCurrency,
      String accountingCurrencyAmount) {
    ProjectExpenseWrapper newWrapper = ProjectExpenseWrapper.createNew(em, project);
    newWrapper.setState(RowEditState.EDITING_NEW);
    newWrapper.setProperty("originalCurrency", originalCurrency, Currency.class);
    newWrapper.setProperty("paymentDate", date, LocalDate.class);
    newWrapper.setProperty(
        "budgetCategory",
        budgetCategory, BudgetCategory.class);
    newWrapper.setProperty(
        "originalAmount",
        new BigDecimal(originalAmount, Utils.MC), BigDecimal.class);
    newWrapper.setProperty(
        "accountingCurrencyAmount",
        new BigDecimal(accountingCurrencyAmount, Utils.MC), BigDecimal.class);
    newWrapper.setProperty("report", report, ProjectReport.class);
    newWrapper.save(em);
    return newWrapper;
  }

  static void createProjectNote(
      EntityManager em,
      Project project,
      Timestamp entryTime,
      String note) {
    ProjectNote projectNote = new ProjectNote();
    projectNote.setEntryTime(entryTime);
    projectNote.setNote(note);
    projectNote.setProject(project);
    em.persist(projectNote);
  }
  
  static ProjectReport createProjectReport(
      EntityManager em,
      Project project,
      LocalDate reportDate) {
    ProjectReport report = new ProjectReport();
    report.setProject(project);
    report.setReportDate(reportDate);
    em.persist(report);
    return report;
  }

  static void createProjectBudgetLimit(
      EntityManager em,
      Project project,
      BudgetCategory budgetCategory,
      String percentage,
      String budget) {
    ProjectBudgetLimit limit = new ProjectBudgetLimit();
    limit.setProject(project);
    limit.setBudgetCategory(budgetCategory);
    if (percentage != null) {
      limit.setBudgetPercentage(new BigDecimal(percentage, Utils.MC));
    }
    if (budget != null) {
      limit.setBudgetGrantCurrency(new BigDecimal(budget, Utils.MC));
    }
  }
  
  static Currency createCurrency(EntityManager em, String code) {
    Currency currency = new Currency();
    currency.setCode(code);
    em.persist(currency);
    return currency;
  }
  
  static ProjectWrapper findProjectByName(
      EntityManager em, String name) {
    for (ProjectWrapper project : ProjectWrapper.getProjects(em)) {
      if (name.equals(project.getEntity().getName())) {
        return project;
      }
    }
    return null;
  }

  static ProjectExpenseWrapper findExpenseById(
      EntityManager em, Project project, Long id) {
    for (ProjectExpenseWrapper expense : ProjectExpenseWrapper.getProjectExpenseList(em, project)) {
      if (Objects.equals(expense.getId(), id)) {
        return expense;
      }
    }
    return null;
  }

  static BudgetCategoryWrapperBase findByBudgetCategory(
      List list, BudgetCategory filter) {
    BudgetCategoryWrapperBase result = null;
    for (Object curObj : list) {
      BudgetCategoryWrapperBase cur = (BudgetCategoryWrapperBase)curObj;
      if (cur.getBudgetCategory().getId().equals(filter.getId())) {
        if (result != null) {
          throw new RuntimeException("Multiple matches.");
        } else {
          result = cur;
        }
      }
    }
    return result;
  }

  static void assertBigDecimalEquals(BigDecimal expected, Object tested) {
    assertEquals(0, expected.compareTo((BigDecimal)tested));
  }
  
  static void assertBigDecimalEquals(String expected, Object tested) {
    assertBigDecimalEquals(new BigDecimal(expected, Utils.MC), tested);
  }
}
