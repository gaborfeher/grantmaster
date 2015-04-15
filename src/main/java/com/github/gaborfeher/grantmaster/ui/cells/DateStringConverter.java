/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui.cells;

import java.sql.Date;
import javafx.util.StringConverter;

/**
 *
 * @author gabor
 */
class DateStringConverter extends StringConverter<Date> {
  
  public DateStringConverter() {
  }

  @Override
  public String toString(Date date) {
    if (date == null) {
      return "";
    }
    return date.toString();
  }

  @Override
  public Date fromString(String string) {
    return Date.valueOf(string);
  }
  
}
