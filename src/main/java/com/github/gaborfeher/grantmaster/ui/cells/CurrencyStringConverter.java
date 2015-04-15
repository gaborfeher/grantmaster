/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import javafx.util.StringConverter;

/**
 *
 * @author gabor
 */
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
    throw new RuntimeException("This method is not used.");
    //TypedQuery<Currency> query = DatabaseConnectionSingleton.getInstance().em().createQuery("SELECT c FROM Currency c WHERE c.code = :name", Currency.class);
    //query.setParameter("name", string);
    //return query.getSingleResult();
  }

}
