package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import javafx.util.StringConverter;

class BudgetCategoryStringConverter extends StringConverter<Object> {
  public BudgetCategoryStringConverter() {
  }

  @Override
  public String toString(Object t) {
    if (t == null) {
      return "";
    }
    return t.toString();
  }

  @Override
  public BudgetCategory fromString(String string) {
    throw new RuntimeException("not used");
  }
  
}
