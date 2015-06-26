package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import javafx.util.StringConverter;

class CurrencyStringConverter extends StringConverter<Currency> {

  CurrencyStringConverter() {
  }

  @Override
  public String toString(Currency c) {
    if (c == null) {
      return "";
    }
    return c.getCode();
  }

  @Override
  public Currency fromString(String string) {
    throw new RuntimeException("not used");
  }

}
