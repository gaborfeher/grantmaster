/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import javafx.util.StringConverter;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 *
 * @author gabor
 */
class ExpenseTypeStringConverter extends StringConverter<ExpenseType> {
  EntityManager em;
  
  public ExpenseTypeStringConverter() {
  }

  ExpenseTypeStringConverter(EntityManager entityManager) {
    em = entityManager;
  }

  @Override
  public String toString(ExpenseType t) {
    if (t == null) {
      return "";
    }
    return t.getName();
  }

  @Override
  public ExpenseType fromString(String string) {
    throw new RuntimeException("not used");
    //TypedQuery<ExpenseType> query = em.createQuery("SELECT t FROM ExpenseType t WHERE t.name = :name", ExpenseType.class);
    //query.setParameter("name", string);
    //return query.getSingleResult();
  }
  
}
