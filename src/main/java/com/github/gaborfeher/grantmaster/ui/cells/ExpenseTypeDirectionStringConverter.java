/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import javafx.util.StringConverter;

/**
 *
 * @author gabor
 */
class ExpenseTypeDirectionStringConverter extends StringConverter<ExpenseType.Direction> {

  public ExpenseTypeDirectionStringConverter() {
  }

  @Override
  public String toString(ExpenseType.Direction t) {
    if (t == null) {
      return "";
    }
    return t.toString();
  }

  @Override
  public ExpenseType.Direction fromString(String string) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
