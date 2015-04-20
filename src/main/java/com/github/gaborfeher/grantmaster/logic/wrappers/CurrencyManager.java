package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import javax.persistence.EntityManager;

public class CurrencyManager {

  public static void createDefaultCurrencies(EntityManager em) {
    Currency c;
    c = new Currency(); c.setCode("HUF"); em.persist(c);
    c = new Currency(); c.setCode("USD"); em.persist(c);
    c = new Currency(); c.setCode("EUR"); em.persist(c);
    c = new Currency(); c.setCode("GBP"); em.persist(c);
  }
  
}
