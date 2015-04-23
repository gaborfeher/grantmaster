package com.github.gaborfeher.grantmaster.ui.cells;

import java.time.LocalDate;

/**
 *
 * @author gabor
 */
class DateStringConverter extends MultiStringConverter<LocalDate> {
  
  public DateStringConverter() {
  }

  @Override
  public String toString(LocalDate date) {
    if (date == null) {
      return "";
    }
    return date.toString();
  }

  @Override
  public LocalDate fromString(String string) {
    return LocalDate.parse(string);
  }
  
}
