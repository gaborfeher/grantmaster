package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import javafx.util.StringConverter;

class BudgetCategoryDirectionStringConverter extends StringConverter<BudgetCategory.Direction> {

  public BudgetCategoryDirectionStringConverter() {
  }

  @Override
  public String toString(BudgetCategory.Direction t) {
    if (t == null) {
      return "";
    }
    return t.toString();
  }

  @Override
  public BudgetCategory.Direction fromString(String string) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
