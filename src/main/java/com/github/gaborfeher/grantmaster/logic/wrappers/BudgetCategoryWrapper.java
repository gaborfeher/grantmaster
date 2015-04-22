package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.EntityBase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;

public class BudgetCategoryWrapper extends EntityWrapper {
  protected BudgetCategory budgetCategory;

  public BudgetCategoryWrapper(BudgetCategory budgetCategory) {
    this.budgetCategory = budgetCategory;
  }
  
  protected BudgetCategoryWrapper(String fakeName) {
    this.budgetCategory = null;
    this.computedValues.put("name", fakeName);
  }
  
  public BudgetCategoryWrapper createFakeCopy(String fakeName) {
    BudgetCategoryWrapper copy = new BudgetCategoryWrapper(fakeName);
    copy.addSummaryValues(this, 1.0);
    copy.setIsSummary(true);
    copy.setState(null);
    return copy;
  }
  
  public int getId() {
    return budgetCategory.getId();
  }
  
  @Override
  public Object getProperty(String name) {
    if (computedValues.containsKey(name)) {
      return computedValues.get(name);
    } else if (getEntity() != null) {
      return super.getProperty(name);
    }
    return null;
  }

   public String getGroupName() {
    if (budgetCategory == null) {
      return null;
    }
    return budgetCategory.getGroupName();
  }
   
  public BudgetCategory getBudgetCategory() {
    return budgetCategory;
  }
  
  protected void addSummaryValue(BudgetCategoryWrapper other, String key, double multiplier) {
    Double value = (Double)other.computedValues.get(key) * multiplier;
    computedValues.put(key, value + (Double) computedValues.getOrDefault(key, 0.0));
  }
  
  public void addSummaryValues(BudgetCategoryWrapper other, double multiplier) {
    for (Map.Entry<String, Object> summaryEntry : other.computedValues.entrySet()) {
      String key = summaryEntry.getKey();
      Object entryValue = summaryEntry.getValue();
      if (entryValue != null && entryValue instanceof Double) {
        addSummaryValue(other, key, multiplier);
      }
    }
  }
  
  @Override
  protected EntityBase getEntity() {
    return budgetCategory;
  }

  public static List<BudgetCategoryWrapper> getBudgetCategoryWrappers(EntityManager em, BudgetCategory.Direction direction) {
    return em.createQuery(
            "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.BudgetCategoryWrapper(c) " +
            "FROM BudgetCategory c " +
            "WHERE :direction IS NULL OR c.direction = :direction " +
            "ORDER BY c.direction, c.groupName NULLS LAST, c.name",
        BudgetCategoryWrapper.class).
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
      Map<Integer, BudgetCategoryWrapper> map,
      Set<String> columnNames) {
    List<Object[]> summaryList = em.createQuery(query, Object[].class).getResultList();
    for (Object[] line : summaryList) {
      BudgetCategoryWrapper budgetCategoryWrapper = map.get(((BudgetCategory)line[0]).getId());
      int year = (Integer)line[2];
      String header = String.format("%d (%s)", year, (String)line[1]);
      columnNames.add(header);
      budgetCategoryWrapper.computedValues.put(header, (Double)line[3]);
    }
  }
  
  /**
   * Retrieves all the budget categories. Each one is populated with yearly
   * summaries of its corresponding expense or income.
   * @param paymentCategories
   * @param incomeCategories 
   */
  public static void getYearlyBudgetCategorySummaries(
      EntityManager em,
      List<BudgetCategoryWrapper> paymentCategories,
      List<BudgetCategoryWrapper> incomeCategories,
      Set<String> columnNames) {
    paymentCategories.clear();
    paymentCategories.addAll(getBudgetCategoryWrappers(
        em, BudgetCategory.Direction.PAYMENT));
    incomeCategories.clear();
    incomeCategories.addAll(getBudgetCategoryWrappers(
        em, BudgetCategory.Direction.INCOME));
    
    Map<Integer, BudgetCategoryWrapper> budgetCategoryMap = new HashMap<>();
    for (BudgetCategoryWrapper budgetCategoryWrapper : paymentCategories) {
      budgetCategoryMap.put(budgetCategoryWrapper.getId(), budgetCategoryWrapper);
    }
    for (BudgetCategoryWrapper budgetCategoryWrapper : incomeCategories) {
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
        "SELECT s.project.incomeType AS incomeType, s.project.accountCurrency.code AS currency, FUNCTION('YEAR', s.availabilityDate) AS year, SUM(s.amount * s.exchangeRate) " +
        "FROM ProjectSource s " +
        "GROUP BY incomeType, year, currency",
        budgetCategoryMap,
        columnNames);
  }
  
  public static BudgetCategoryWrapper createBudgetSummaryList(
      EntityManager em,
      List<BudgetCategoryWrapper> rawLines,
      String summaryTitle,
      List<BudgetCategoryWrapper> summary) {
    BudgetCategoryWrapper totalSum = null;
    BudgetCategoryWrapper groupSum = null;
    String currentGroupName = null;
    BudgetCategoryWrapper previous = null;
    for (BudgetCategoryWrapper current : rawLines) {
      if (previous != null) {
        if (currentGroupName != null && !currentGroupName.equals(current.getGroupName())) {
          summary.add(groupSum);
          currentGroupName = null;
          groupSum = null;
        }
      }
      
      if (current.getGroupName() != null) {
        if (currentGroupName == null || groupSum == null) {
          currentGroupName = current.getGroupName();
          groupSum = current.createFakeCopy(current.getGroupName() + " mindösszesen");
        } else {
          groupSum.addSummaryValues(current, 1.0);
        }
      }
      
      summary.add(current);
      if (totalSum != null) {
        totalSum.addSummaryValues(current, 1.0);
      } else {
        totalSum = current.createFakeCopy(summaryTitle);
      }
      
      previous = current;
    }
    if (groupSum != null) {
      summary.add(groupSum);
    }
    if (totalSum != null) {
      summary.add(totalSum);
    }
    return totalSum;
  }

  /**
   * Processes the lists of income and expense budget categories, and inserts
   * summary lines after groups of categories.
   * @param paymentCategories
   * @param incomeCategories
   * @param output 
   */
  public static void createBudgetSummaryList(
      EntityManager em,
      List<BudgetCategoryWrapper> paymentCategories,
      List<BudgetCategoryWrapper> incomeCategories,
      List<BudgetCategoryWrapper> output) {
    output.clear();
    BudgetCategoryWrapper expenseSum =
        createBudgetSummaryList(em, paymentCategories, "Költségek mindösszesen", output);
    BudgetCategoryWrapper incomeSum = 
        createBudgetSummaryList(em, incomeCategories, "Bevételek mindösszesen", output);
    BudgetCategoryWrapper finalSum = incomeSum.createFakeCopy("Különbség");
    if (expenseSum != null) {
      finalSum.addSummaryValues(expenseSum, -1.0);
    }
    output.add(finalSum);
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

  @Override
  protected void setEntity(EntityBase entity) {
    this.budgetCategory = (BudgetCategory) entity;
  }


  
}
