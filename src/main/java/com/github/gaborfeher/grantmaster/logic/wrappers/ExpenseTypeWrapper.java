package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

/**
 *
 * @author gabor
 */
public class ExpenseTypeWrapper extends EntityWrapper {
  protected ExpenseType expenseType;
  protected final HashMap<String, Double> summaryValues;
  private final String fakeName;

  public ExpenseTypeWrapper(ExpenseType expenseType) {
    this.expenseType = expenseType;
    this.summaryValues = new HashMap<>();
    this.fakeName = null;
  }
  
  protected ExpenseTypeWrapper(String fakeName) {
    this.expenseType = null;
    this.summaryValues = new HashMap<>();
    this.fakeName = fakeName;
  }
  
  public ExpenseTypeWrapper createFakeCopy(String fakeName) {
    ExpenseTypeWrapper copy = new ExpenseTypeWrapper(fakeName);
    copy.addSummaryValues(this);
    return copy;
  }
  
  public int getId() {
    return expenseType.getId();
  }
  
  public String getName() {
    if (fakeName != null) {
      return fakeName;
    }
    return expenseType.getName();
  }
  
  public void setName(String name) {
    expenseType.setName(name);
  }

  public ExpenseType.Direction getDirection() {
    if (expenseType == null) {
      return null;
    }
    return expenseType.getDirection();
  }
  
  public void setDirection(ExpenseType.Direction direction) {
    expenseType.setDirection(direction);
  }
  
  public String getGroupName() {
    if (expenseType == null) {
      return null;
    }
    return expenseType.getGroupName();
  }
  
  public void setGroupName(String groupName) {
    expenseType.setGroupName(groupName);
  }
  
  public Object getExpenseType() {
    if (fakeName != null) {
      return fakeName;
    }
    return expenseType;
  }
  
  public void addSummaryValues(ExpenseTypeWrapper expenseTypeWrapper) {
    System.out.println("ExpenseTypeWrapper.addSummaryValues");
    
    for (Map.Entry<String, Double> summaryEntry : expenseTypeWrapper.summaryValues.entrySet()) {
      String key = summaryEntry.getKey();
      Double value = summaryEntry.getValue();
      summaryValues.put(key, value + summaryValues.getOrDefault(key, 0.0));
    }
  }
  
  @Override
  public boolean isFake() {
    return fakeName != null;
  }
  
  @Override
  protected Object getEntity() {
    return expenseType;
  }
  
  @Override
  public boolean isSummary() {
    return fakeName != null;
  }
  
  public static List<ExpenseTypeWrapper> getExpenseTypeWrappers(ExpenseType.Direction direction) {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    return em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ExpenseTypeWrapper(e) " +
            "FROM ExpenseType e " +
            "WHERE :direction IS NULL OR e.direction = :direction " +
            "ORDER BY e.direction, e.groupName NULLS LAST, e.name",
        ExpenseTypeWrapper.class).
            setParameter("direction", direction).
            getResultList();
  }
  
  public static List<ExpenseType> getExpenseTypes(ExpenseType.Direction direction) {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    return em.createQuery(
        "SELECT e " +
            "FROM ExpenseType e " +
            "WHERE e.direction = :direction " +
            "ORDER BY e.groupName NULLS LAST, e.name",
        ExpenseType.class).
            setParameter("direction", direction).
            getResultList();
  }
  
  private static void createBudgetSummaryList(
      List<ExpenseTypeWrapper> rawLines,
      String summaryTitle,
      List<ExpenseTypeWrapper> summary) {
    ExpenseTypeWrapper totalSum = null;
    ExpenseTypeWrapper groupSum = null;
    String currentGroupName = null;
    ExpenseTypeWrapper previous = null;
    for (ExpenseTypeWrapper current : rawLines) {
      if (previous != null) {
        if (currentGroupName != null && !currentGroupName.equals(current.getGroupName())) {
          summary.add(groupSum);
          currentGroupName = null;
          groupSum = null;
        }
      }
      
      if (current.getGroupName() != null) {
        if (currentGroupName == null || groupSum == null) {
          currentGroupName = current.getGroupName();
          groupSum = current.createFakeCopy(current.getGroupName() + " összesen");
        } else {
          groupSum.addSummaryValues(current);
        }
      }
      
      summary.add(current);
      if (totalSum != null) {
        totalSum.addSummaryValues(current);
      } else {
        totalSum = current.createFakeCopy(summaryTitle);
      }
      
      previous = current;
    }
    if (groupSum != null) {
      summary.add(groupSum);
    }
    summary.add(totalSum);

  }

  public static void createBudgetSummaryList(List<ExpenseTypeWrapper> paymentTypes, List<ExpenseTypeWrapper> incomeTypes, List<ExpenseTypeWrapper> output) {
    output.clear();
    createBudgetSummaryList(paymentTypes, "Kiadások összesen", output);
    createBudgetSummaryList(incomeTypes, "Bevételek összesen", output);
  }


  public void addSummaryValue(String header, Double value) {
    summaryValues.put(header, value);
  }

  public Double getSummaryValue(String columnName) {
    return summaryValues.get(columnName);
  }
  
}
