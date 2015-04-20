package com.github.gaborfeher.grantmaster.ui.cells;

import java.text.DecimalFormat;

class DoubleStringConverter extends MultiStringConverter<Double> {
  final DecimalFormat formatter;
  
  public DoubleStringConverter() {
    formatter = new DecimalFormat("#,##0.00");
  }

  @Override
  public String toString(Double t) {
    if (t == null) {
      return "";
    }
    return formatter.format(t);
  }

  @Override
  public Double fromString(String string) {
    if (string == null) {
      return null;
    }
    return Double.parseDouble(string);
  }
  
  @Override
  public String toEditableString(Double t) {
    if (t == null) {
      return null;
    }
    return String.format("%2.2f", t);
  }
  
}
