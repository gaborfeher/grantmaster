package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.core.Utils;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import javafx.scene.control.Alert;

class BigDecimalStringConverter extends MultiStringConverter<BigDecimal> {
  final DecimalFormat displayFormatter;
  final DecimalFormat editFormatter;
  
  public BigDecimalStringConverter() {
    displayFormatter = new DecimalFormat("#,##0.00");
    editFormatter = new DecimalFormat("#0.00");
    editFormatter.setParseBigDecimal(true);
  }

  @Override
  public String toString(BigDecimal t) {
    if (t == null) {
      return "";
    }
    return displayFormatter.format(t);
  }

  @Override
  public BigDecimal fromString(String string) {
    if (string == null) {
      return null;
    }
    ParsePosition pos = new ParsePosition(0);
    BigDecimal value = (BigDecimal) editFormatter.parse(string, pos);
    if (pos.getIndex() == string.length()) {
      return value;
    } else {
      return null;
    }
  }
  
  @Override
  public String toEditableString(BigDecimal t) {
    if (t == null) {
      return null;
    }
    return editFormatter.format(t);
  }

  @Override
  public String getParseError() {
    return Utils.getString("Edit.UnknownNumberFormat");
  }
}
