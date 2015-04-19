package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;

/**
 *
 * @author gabor
 */
public class ExpenseTypeWrapper extends EntityWrapper {
  ExpenseType expenseType;
  protected final HashMap<String, Double> summaryValues;

  public ExpenseTypeWrapper(ExpenseType expenseType) {
    this.expenseType = expenseType;
    this.summaryValues = new HashMap<>();
  }
  
  public int getId() {
    return expenseType.getId();
  }
  
  public String getName() {
    return expenseType.getName();
  }
  
  public void setName(String name) {
    expenseType.setName(name);
  }

  public ExpenseType.Direction getDirection() {
    return expenseType.getDirection();
  }
  
  public void setDirection(ExpenseType.Direction direction) {
    expenseType.setDirection(direction);
  }
  
  public String getGroupName() {
    return expenseType.getGroupName();
  }
  
  public void setGroupName(String groupName) {
    expenseType.setGroupName(groupName);
  }
  
  @Override
  protected Object getEntity() {
    return expenseType;
  }
  
  @Override
  public List<EntityWrapper> getPossibleValues() {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    List list = getExpenseTypes();
    return list;
  }
  
  public static List<ExpenseTypeWrapper> getExpenseTypes() {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    return em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ExpenseTypeWrapper(e) " +
            "FROM ExpenseType e " +
            "ORDER BY e.direction, e.groupName NULLS LAST, e.name",
        ExpenseTypeWrapper.class).
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


  public void addSummaryValue(String header, Double value) {
    summaryValues.put(header, value);
  }

  public Double getSummaryValue(String columnName) {
    return summaryValues.get(columnName);
  }
  
}
