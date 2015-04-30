package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.core.Utils;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

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
    try {
      return LocalDate.parse(string);
    } catch (DateTimeParseException ex) {
      return null;
    }
  }

  @Override
  public String getParseError() {
    return Utils.getString("Edit.UnknownDateFormat");
  }
}
