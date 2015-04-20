package com.github.gaborfeher.grantmaster.ui.cells;

import java.sql.Date;

/**
 *
 * @author gabor
 */
class DateStringConverter extends MultiStringConverter<Date> {
  
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
