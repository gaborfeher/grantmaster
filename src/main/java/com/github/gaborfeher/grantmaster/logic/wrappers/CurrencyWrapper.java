package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.EntityBase;
import java.util.List;
import javax.persistence.EntityManager;

public class CurrencyWrapper extends EntityWrapper {
  private Currency currency;

  public CurrencyWrapper(Currency currency) {
    this.currency = currency;
  }
  
  @Override
  public EntityBase getEntity() {
    return currency;
  }

  @Override
  protected void setEntity(EntityBase entity) {
    this.currency = (Currency) entity;
  }
  
  public static List<CurrencyWrapper> getCurrencyWrappers(EntityManager em) {
    return em.createQuery(
          "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.CurrencyWrapper(c) FROM Currency c", CurrencyWrapper.class).getResultList();
  }
  
  public static void createDefaultCurrencies(EntityManager em) {
    Currency c;
    c = new Currency(); c.setCode("HUF"); em.persist(c);
    c = new Currency(); c.setCode("USD"); em.persist(c);
    c = new Currency(); c.setCode("EUR"); em.persist(c);
    c = new Currency(); c.setCode("GBP"); em.persist(c);
  }
  
}
