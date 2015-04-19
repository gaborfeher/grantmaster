package com.github.gaborfeher.grantmaster.ui.cells;

import java.text.DecimalFormat;
import javafx.util.StringConverter;

class DoubleStringConverter extends StringConverter<Double> {
  final DecimalFormat formatter = new DecimalFormat("#,###.00");
  
  public DoubleStringConverter() {
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
    string = string.replace(",", "");
    // TODO: replaceFirst(" [A-Z]+$", "");
    return Double.parseDouble(string);
  }
  
}
