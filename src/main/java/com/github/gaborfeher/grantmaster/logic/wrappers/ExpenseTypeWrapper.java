/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import java.util.List;
import javax.persistence.EntityManager;

/**
 *
 * @author gabor
 */
public class ExpenseTypeWrapper extends EntityWrapper {
  ExpenseType expenseType;

  public ExpenseTypeWrapper(ExpenseType expenseType) {
    this.expenseType = expenseType;
  }
  
  public int getId() {
    return expenseType.getId();
  }
  
  public String getName() {
    return expenseType.getName();
  }
  
  public void setName(String name) {
    expenseType.setName(name);
  }

  public ExpenseType.Direction getDirection() {
    return expenseType.getDirection();
  }
  
  public void setDirection(ExpenseType.Direction direction) {
    expenseType.setDirection(direction);
  }
  
  public String getGroupName() {
    return expenseType.getGroupName();
  }
  
  public void setGroupName(String groupName) {
    expenseType.setGroupName(groupName);
  }
  
  @Override
  protected Object getEntity() {
    return expenseType;
  }
  
  @Override
  public List<EntityWrapper> getPossibleValues() {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    List list = getExpenseTypes();
    return list;
  }
  
  public static List<ExpenseTypeWrapper> getExpenseTypes() {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    return em.createQuery("SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ExpenseTypeWrapper(e) FROM ExpenseType e", ExpenseTypeWrapper.class).getResultList();
  }
  
}
