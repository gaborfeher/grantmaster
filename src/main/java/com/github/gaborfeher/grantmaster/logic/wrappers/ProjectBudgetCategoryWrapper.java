package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.EntityBase;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;

public class ProjectBudgetCategoryWrapper extends BudgetCategoryWrapper {
  private ProjectBudgetLimit limit;
  private Project project;
  
  public ProjectBudgetCategoryWrapper(BudgetCategory budgetCategory, BigDecimal spentAccountingCurrency, BigDecimal spentGrantCurrency) {
    super(budgetCategory);
    setComputedValue("spentGrantCurrency", spentGrantCurrency);
    setComputedValue("spentAccountingCurrency", spentAccountingCurrency);
    setComputedValue("remainingAccountingCurrency", null);
    setComputedValue("budgetAccountingCurrency", null);
  }
  
  public ProjectBudgetCategoryWrapper(String fakeName) {
    super(fakeName);
    computedValues.put("budgetCategory", fakeName);
    setComputedValue("spentGrantCurrency", BigDecimal.ZERO);
    setComputedValue("spentAccountingCurrency", BigDecimal.ZERO);
    setComputedValue("remainingAccountingCurrency", null);
    setComputedValue("budgetAccountingCurrency", null);

  }
  
  @Override
  public BudgetCategoryWrapper createFakeCopy(String fakeTitle) {
    BudgetCategoryWrapper copy = new ProjectBudgetCategoryWrapper(fakeTitle);
    copy.addSummaryValues(this, BigDecimal.ONE);
    copy.setIsSummary(true);
    copy.setState(null);
    return copy;
  }
  
  public void setProject(Project project) {
    this.project = project;
  }
  
  public void setLimit(BigDecimal total, ProjectBudgetLimit limit) {
    this.limit = limit;
    if (limit == null) {
      return;
    }
    
    if (budgetCategory != null && !Objects.equals(budgetCategory.getId(), limit.getBudgetCategory().getId())) {
      System.out.println("bad expense type");
    }
    if (!Objects.equals(project.getId(), limit.getProject().getId())) {
      System.out.println("bad project for limit");
    }
    
    if (limit.getBudgetPercentage() != null) {
      limit.setBudgetGrantCurrency(total.multiply(limit.getBudgetPercentage()).divide(new BigDecimal("100"), Utils.MC));
    }
    if (limit.getBudgetGrantCurrency() != null) {
      BigDecimal remainingGrantCurrency = limit.getBudgetGrantCurrency();
      BigDecimal spentGrantCurrency = getComputedValue("spentGrantCurrency");
      if (spentGrantCurrency != null) {
        remainingGrantCurrency = remainingGrantCurrency.subtract(spentGrantCurrency, Utils.MC);
      }
      setComputedValue("remainingGrantCurrency", remainingGrantCurrency);
    }
  }

  public void setBudgetCategory(BudgetCategory budgetCategory) {
    limit.setBudgetCategory(budgetCategory);
    this.budgetCategory = budgetCategory;
  }

  @Override
  public void addSummaryValues(BudgetCategoryWrapper other, BigDecimal multiplier) {
    addSummaryValue(other, "spentGrantCurrency", multiplier);
    addSummaryValue(other, "spentAccountingCurrency", multiplier);
  }
  
  public void addBudgetAmounts(BigDecimal accountingCurrencyAmount, BigDecimal grantCurrencyAmount) {
    if (limit == null) {
      limit = new ProjectBudgetLimit();
    }
    if (limit.getBudgetGrantCurrency() == null) {
      limit.setBudgetGrantCurrency(grantCurrencyAmount);
    } else {
      limit.setBudgetGrantCurrency(limit.getBudgetGrantCurrency().add(grantCurrencyAmount, Utils.MC));
    }
    BigDecimal budgetAccountingCurrency = (BigDecimal) computedValues.get("budgetAccountingCurrency");
    if (budgetAccountingCurrency == null) {
      budgetAccountingCurrency = BigDecimal.ZERO;
    }
    budgetAccountingCurrency = budgetAccountingCurrency.add(accountingCurrencyAmount);
    setComputedValue("budgetAccountingCurrency", budgetAccountingCurrency);
    setComputedValue("remainingGrantCurrency", limit.getBudgetGrantCurrency().subtract(getComputedValue("spentGrantCurrency"), Utils.MC));
    setComputedValue("remainingAccountingCurrency", budgetAccountingCurrency.subtract(getComputedValue("spentAccountingCurrency"), Utils.MC));
  }
  
  @Override
  public EntityBase getEntity() {
    return limit;
  }
  
  @Override
  public boolean save(EntityManager em) {
    System.out.println("ProjectBudgetCategoryWrapper.save");
    if (limit == null) {
      System.out.println("  set limit");
      setState(State.EDITING_NEW);
      limit = new ProjectBudgetLimit();
      limit.setBudgetCategory(budgetCategory);
      limit.setProject(project);
    }
    System.out.println("   " + limit + " " + getEntity());
    return super.save(em);
  }
  
  public static List<ProjectBudgetCategoryWrapper> getProjectBudgetLimits(
      EntityManager em,
      Project project,
      LocalDate filterStartDate,
      LocalDate filterEndDate) {
    BigDecimal total =
        Utils.getSingleResultWithDefault(BigDecimal.ZERO,
            em.createQuery("SELECT SUM(s.grantCurrencyAmount) FROM ProjectSource s WHERE s.project = :project GROUP BY s.project", BigDecimal.class).
                setParameter("project", project));

        
    List<ProjectBudgetCategoryWrapper> list = em.createQuery("SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetCategoryWrapper(c, SUM(a.accountingCurrencyAmount), SUM(a.accountingCurrencyAmount / s.exchangeRate)) " +
        "FROM BudgetCategory c LEFT OUTER JOIN ProjectExpense e ON e.budgetCategory = c AND e.project = :project LEFT OUTER JOIN ExpenseSourceAllocation a ON a.expense = e LEFT OUTER JOIN ProjectSource s ON a.source = s AND s.project = :project " +
            "WHERE c.direction = :direction " +
            " AND (:filterStartDate IS NULL OR e.paymentDate >= :filterStartDate) " +
            " AND (:filterEndDate IS NULL OR e.paymentDate <= :filterEndDate) " +
            "GROUP BY c " +
            "ORDER BY c.groupName NULLS LAST, c.name",
        ProjectBudgetCategoryWrapper.class).
            setParameter("project", project).
            setParameter("direction", BudgetCategory.Direction.PAYMENT).
            setParameter("filterStartDate", filterStartDate).
            setParameter("filterEndDate", filterEndDate).
            getResultList();
    
    Iterator<ProjectBudgetCategoryWrapper> iterator = list.iterator();
    while (iterator.hasNext()) {
      ProjectBudgetCategoryWrapper limitWrapper = iterator.next();
      
      limitWrapper.setProject(project);
      ProjectBudgetLimit limit = 
          Utils.getSingleResultWithDefault(
              null,
              em.createQuery(
                  "SELECT l FROM ProjectBudgetLimit l WHERE l.project = :project AND l.budgetCategory = :budgetCategory",
                  ProjectBudgetLimit.class).
                  setParameter("project", project).
                  setParameter("budgetCategory", limitWrapper.getBudgetCategory()));


      limitWrapper.setLimit(total, limit);
      if (limitWrapper.getEntity() == null && limitWrapper.computedValues.get("spentGrantCurrency") == null) {
        iterator.remove();
      }
    }

    return list;
  }
  
  static void removeProjectBudgetLimits(EntityManager em, Project project) {
    em.createQuery("DELETE FROM ProjectBudgetLimit l WHERE l.project = :project").
        setParameter("project", project).
        executeUpdate();
  }

  @Override
  protected void setEntity(EntityBase entity) {
    this.limit = (ProjectBudgetLimit) entity;
  }


}
