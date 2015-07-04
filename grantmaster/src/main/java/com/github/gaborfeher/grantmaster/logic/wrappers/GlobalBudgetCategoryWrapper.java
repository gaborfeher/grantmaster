/**
 * This file is a part of GrantMaster.
 * Copyright (C) 2015  Gábor Fehér <feherga@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;

/**
 * Wraps a BudgetCategory entity and stores yearly spending summaries of
 * the expenses OR incomes in that category. The currency of the summaries
 * is the accounting currency of the expense. Each year-accounting currency
 * pair will get a table column, i.e. an entry in computedValues.
 */
public class GlobalBudgetCategoryWrapper
    extends BudgetCategoryWrapperBase<BudgetCategory> {
  // Column Name to summary value, e.g. "2015 (HUF)" -> 1,000,000
  protected final Map<String, BigDecimal> computedValues;
  
  public GlobalBudgetCategoryWrapper(BudgetCategory budgetCategory) {
    super(budgetCategory, null);
    computedValues = new HashMap<>();
  }
  
  protected GlobalBudgetCategoryWrapper(String fakeName) {
    super(null, fakeName);
    computedValues = new HashMap<>();
  }

  public BigDecimal getComputedValue(String key) {
    BigDecimal result = computedValues.get(key);
    if (result == null) {
      result = BigDecimal.ZERO;
    }
    return result;
  }
  
  public void setComputedValue(String key, BigDecimal value) {
    computedValues.put(key, value);
  }
  
  public GlobalBudgetCategoryWrapper createFakeCopy(String fakeName) {
    GlobalBudgetCategoryWrapper copy = new GlobalBudgetCategoryWrapper(fakeName);
    copy.addSummaryValues(this, BigDecimal.ONE);
    copy.setIsSummary(true);
    copy.setState(null);
    return copy;
  }
  
  @Override
  public Object getProperty(String name) {
    if (computedValues.containsKey(name)) {
      return computedValues.get(name);
    } else if (getEntity() != null || "name".equals(name)) {
      return super.getProperty(name);
    }
    return null;
  }

  @Override
  public boolean canEdit() {
    return getBudgetCategory() != null;
  }
      
  protected void addSummaryValue(BudgetCategoryWrapperBase other0, String key, BigDecimal multiplier) {
    GlobalBudgetCategoryWrapper other = (GlobalBudgetCategoryWrapper) other0;
    BigDecimal value = multiplier.multiply(other.getComputedValue(key), Utils.MC);
    setComputedValue(key, value.add(getComputedValue(key), Utils.MC));
  }
  
  @Override
  public void addSummaryValues(BudgetCategoryWrapperBase other0, BigDecimal multiplier) {
    GlobalBudgetCategoryWrapper other = (GlobalBudgetCategoryWrapper) other0;
    for (Map.Entry<String, BigDecimal> summaryEntry : other.computedValues.entrySet()) {
      String key = summaryEntry.getKey();
      Object entryValue = summaryEntry.getValue();
      if (entryValue != null && entryValue instanceof BigDecimal) {
        addSummaryValue(other, key, multiplier);
      }
    }
  }

  @Override
  public BudgetCategory getBudgetCategory() {
    return entity;
  }

  public static List<GlobalBudgetCategoryWrapper> getBudgetCategoryWrappers(EntityManager em, BudgetCategory.Direction direction) {
    return em.createQuery("SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.GlobalBudgetCategoryWrapper(c) " +
            "FROM BudgetCategory c " +
            "WHERE :direction IS NULL OR c.direction = :direction " +
            "ORDER BY c.direction, c.groupName NULLS LAST, c.name",
        GlobalBudgetCategoryWrapper.class).
            setParameter("direction", direction).
            getResultList();
  }
  
  public static List<BudgetCategory> getBudgetCategories(EntityManager em, BudgetCategory.Direction direction) {
    return em.createQuery("SELECT c " +
            "FROM BudgetCategory c " +
            "WHERE c.direction = :direction " +
            "ORDER BY c.groupName NULLS LAST, c.name",
        BudgetCategory.class).
            setParameter("direction", direction).
            getResultList();
  }
  
  private static void getYearlyBudgetCategorySummaryMap(
      EntityManager em,
      String query,
      Map<Long, GlobalBudgetCategoryWrapper> map,
      Set<String> columnNames) {
    List<Object[]> summaryList = em.createQuery(query, Object[].class).getResultList();
    for (Object[] line : summaryList) {
      GlobalBudgetCategoryWrapper budgetCategoryWrapper = map.get(((BudgetCategory)line[0]).getId());
      int year = (Integer)line[2];
      String header = String.format("%d (%s)", year, (String)line[1]);
      columnNames.add(header);
      budgetCategoryWrapper.setComputedValue(header, (BigDecimal)line[3]);
    }
  }
  
  /**
   * Retrieves all the budget categories. Each one is populated with yearly
   * summaries of its corresponding expense or income.
   */
  public static void getYearlyBudgetCategorySummaries(
      EntityManager em,
      List<GlobalBudgetCategoryWrapper> paymentCategories,
      List<GlobalBudgetCategoryWrapper> incomeCategories,
      Set<String> columnNames) {
    paymentCategories.clear();
    paymentCategories.addAll(getBudgetCategoryWrappers(
        em, BudgetCategory.Direction.PAYMENT));
    incomeCategories.clear();
    incomeCategories.addAll(getBudgetCategoryWrappers(
        em, BudgetCategory.Direction.INCOME));
    
    Map<Long, GlobalBudgetCategoryWrapper> budgetCategoryMap = new HashMap<>();
    for (GlobalBudgetCategoryWrapper budgetCategoryWrapper : paymentCategories) {
      budgetCategoryMap.put(budgetCategoryWrapper.getId(), budgetCategoryWrapper);
    }
    for (GlobalBudgetCategoryWrapper budgetCategoryWrapper : incomeCategories) {
      budgetCategoryMap.put(budgetCategoryWrapper.getId(), budgetCategoryWrapper);
    }
    
    // Collect expense summaries.
    getYearlyBudgetCategorySummaryMap(
        em,
        "SELECT e.budgetCategory, e.project.accountCurrency.code AS currency, FUNCTION('YEAR', e.paymentDate) AS year, SUM(a.accountingCurrencyAmount) " +
        "FROM ProjectExpense e, ExpenseSourceAllocation a " +
        "WHERE a.expense = e " +
        "GROUP BY e.budgetCategory, year, currency",
        budgetCategoryMap,
        columnNames);

    // Collect income summaries.
    getYearlyBudgetCategorySummaryMap(
        em,
        "SELECT s.project.incomeType AS incomeType, s.project.accountCurrency.code AS currency, FUNCTION('YEAR', s.availabilityDate) AS year, SUM(s.grantCurrencyAmount * s.exchangeRate) " +
        "FROM ProjectSource s " +
        "GROUP BY incomeType, year, currency",
        budgetCategoryMap,
        columnNames);
  }

  public static void createDefaultBudgetCategories(EntityManager em) {
    String groupName = "Személyi jellegű ráfordítások";
    BudgetCategory.Direction direction = BudgetCategory.Direction.PAYMENT;
    for (String name :
        new String[]{
            "Alkalmazottak bruttó bérköltsége",
            "Alkalmazottak járulékköltsége",
            "Bruttó megbízási díjak",
            "Megbízási díjak járulékköltsége", 
            "Béren kívüli juttatások összege",
            "Béren kívüli juttatások adóterhe",
            "Reprezentációs költség",
            "Személyi jellegű egyéb kifizetések"}) {
      em.persist(new BudgetCategory(direction, groupName, name));
    }
    
    groupName = "Iroda működtetésének költségei (bérek nélkül)";
    direction = BudgetCategory.Direction.PAYMENT;
    for (String name :
        new String[]{
            "Irodaszerek (egy éven belül elhasználódó anyagi eszközök)",
            "Bérleti díjak",
            "Telefonköltségek és járulékai",
            "Internet költsége",
            "Postaköltség",
            "Könyvviteli szolgáltatás",
            "Hirdetési díjak",
            "Karbantartás költsége",
            "Egyéb kiadások, ráfordítások"}) {
      em.persist(new BudgetCategory(direction, groupName, name));
    }
    
    groupName = "Egyéb szolgáltatások költsége";
    direction = BudgetCategory.Direction.PAYMENT;
    for (String name :
        new String[]{
            "Biztosítási díjak",
            "Bankköltségek",
            "Jogi szolgáltatási díjak",
            "Utazási, kiküldetési költségek",
            "Fordítási díjak",
            "Minden egyéb szolgáltatás díja"}) {
      em.persist(new BudgetCategory(direction, groupName, name));
    }
    
    groupName = "Tárgyi eszköz beszerzés";
    direction = BudgetCategory.Direction.PAYMENT;
    for (String name :
        new String[]{"100eFt egyedi érték feletti eszközök beszerzése"}) {
      em.persist(new BudgetCategory(direction, groupName, name));
    }
    
    groupName = null;
    direction = BudgetCategory.Direction.INCOME;
    for (String name : new String[]{
            "Közhasznú tevékenység bevétele",
            "Tagdíjak",
            "SZJA 1%",
            "Magánszemélyek támogatásai",
            "Jogi személyek támogatásai",
            "Pályázati bevételek",
            "Kamatbevételek",
            "Egyéb bevételek"}) {
      em.persist(new BudgetCategory(direction, groupName, name));
    }
  }
}
