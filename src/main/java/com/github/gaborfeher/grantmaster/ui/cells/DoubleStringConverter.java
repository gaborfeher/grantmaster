package com.github.gaborfeher.grantmaster.ui.cells;

import java.text.DecimalFormat;
import javafx.util.StringConverter;

class DoubleStringConverter extends StringConverter<Double> {
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
    string = string.replace(",", "");
    // TODO: replaceFirst(" [A-Z]+$", "");
    return Double.parseDouble(string);
  }
  
}
