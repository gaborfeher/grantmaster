package com.github.gaborfeher.grantmaster.ui.cells;

import javafx.util.StringConverter;

class DoubleStringConverter extends StringConverter<Double> {
  
  public DoubleStringConverter() {
  }

  @Override
  public String toString(Double t) {
    if (t == null) {
      return "";
    }
    return String.format("%.2f", t);
  }

  @Override
  public Double fromString(String string) {
    return Double.parseDouble(string);
  }
  
}
