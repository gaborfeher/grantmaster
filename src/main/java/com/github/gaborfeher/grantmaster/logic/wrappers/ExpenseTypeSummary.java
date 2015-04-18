package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import java.util.Map;

public class ExpenseTypeSummary extends ExpenseTypeWrapper {
  final String title;

  public ExpenseTypeSummary(String title) {
    super(null);
    this.title = title;
  }
  
  @Override
  public String getName() {
    return title;
  }
  
  public void addSummaryValues(ExpenseTypeWrapper expenseTypeWrapper) {
    for (Map.Entry<String, Double> summaryEntry : expenseTypeWrapper.summaryValues.entrySet()) {
      String key = summaryEntry.getKey();
      Double value = summaryEntry.getValue();
      summaryValues.put(key, value + summaryValues.getOrDefault(key, 0.0));
    }
  }

  @Override
  public Double getSummaryValue(String columnName) {
    return summaryValues.get(columnName);
  }
  
  @Override
  public ExpenseType.Direction getDirection() {
    return null;
  }
  
  @Override
  public String getGroupName() {
    return null;
  }
  
  @Override
  public int getId() {
    return -1;
  }
  
  @Override
  protected Object getEntity() {
    return null;
  }
  
  @Override
  public boolean isFake() {
    return true;
  }
  
}
