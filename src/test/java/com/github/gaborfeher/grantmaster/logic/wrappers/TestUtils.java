package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectNote;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;
import javax.persistence.EntityManager;

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
      String exchangeRate,
      String accountingCurrencyAmount) {
    ProjectSource projectSource = new ProjectSource();
    projectSource.setProject(project);
    projectSource.setAvailabilityDate(LocalDate.of(2015, 2, 1));
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
      String originalAmount,
      Currency originalCurrency,
      String accountingCurrencyAmount) {
    ProjectExpenseWrapper newWrapper = ProjectExpenseWrapper.createNew(project);
    newWrapper.setState(EntityWrapper.State.EDITING_NEW);
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
    em.persist(projectNote);
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
  
  static ProjectWrapper findProjectByName(
      EntityManager em, String name) {
    for (ProjectWrapper project : ProjectWrapper.getProjects(em)) {
      if (name.equals(project.getProject().getName())) {
        return project;
      }
    }
    return null;
  }

  static ProjectExpense findExpenseById(
      EntityManager em, Project project, Long id) {
    for (ProjectExpenseWrapper expense : ProjectExpenseWrapper.getProjectExpenseList(em, project)) {
      if (Objects.equals(expense.getEntity().getId(), id)) {
        return (ProjectExpense) expense.getEntity();
      }
    }
    return null;
  }
}
