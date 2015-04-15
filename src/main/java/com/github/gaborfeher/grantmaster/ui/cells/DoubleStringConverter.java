/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui.cells;

import javafx.util.StringConverter;

/**
 *
 * @author gabor
 */
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
