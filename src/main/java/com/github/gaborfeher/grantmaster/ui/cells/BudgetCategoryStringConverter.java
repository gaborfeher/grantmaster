/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import javafx.util.StringConverter;
import javax.persistence.EntityManager;

/**
 *
 * @author gabor
 */
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
