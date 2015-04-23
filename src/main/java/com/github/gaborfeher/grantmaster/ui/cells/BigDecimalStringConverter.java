package com.github.gaborfeher.grantmaster.ui.cells;

import java.math.BigDecimal;
import java.text.DecimalFormat;

class BigDecimalStringConverter extends MultiStringConverter<BigDecimal> {
  final DecimalFormat formatter;
  
  public BigDecimalStringConverter() {
    formatter = new DecimalFormat("#,##0.00");
  }

  @Override
  public String toString(BigDecimal t) {
    if (t == null) {
      return "";
    }
    return formatter.format(t);
  }

  @Override
  public BigDecimal fromString(String string) {
    if (string == null) {
      return null;
    }
    return new BigDecimal(string);
  }
  
  @Override
  public String toEditableString(BigDecimal t) {
    if (t == null) {
      return null;
    }
    return String.format("%2.2f", t);
  }
  
}
