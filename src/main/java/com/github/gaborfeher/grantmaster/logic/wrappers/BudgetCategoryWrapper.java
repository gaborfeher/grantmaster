package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.EntityManager;

public class BudgetCategoryWrapper extends EntityWrapper {
  protected BudgetCategory budgetCategory;
  protected final HashMap<String, Double> summaryValues;
  private final String fakeName;

  public BudgetCategoryWrapper(BudgetCategory budgetCategory) {
    this.budgetCategory = budgetCategory;
    this.summaryValues = new HashMap<>();
    this.fakeName = null;
  }
  
  protected BudgetCategoryWrapper(String fakeName) {
    this.budgetCategory = null;
    this.summaryValues = new HashMap<>();
    this.fakeName = fakeName;
  }
  
  public BudgetCategoryWrapper createFakeCopy(String fakeName) {
    BudgetCategoryWrapper copy = new BudgetCategoryWrapper(fakeName);
    copy.addSummaryValues(this);
    return copy;
  }
  
  public int getId() {
    return budgetCategory.getId();
  }
  
  public String getName() {
    if (fakeName != null) {
      return fakeName;
    }
    return budgetCategory.getName();
  }
  
  public void setName(String name) {
    budgetCategory.setName(name);
  }

  public BudgetCategory.Direction getDirection() {
    if (budgetCategory == null) {
      return null;
    }
    return budgetCategory.getDirection();
  }
  
  public void setDirection(BudgetCategory.Direction direction) {
    budgetCategory.setDirection(direction);
  }
  
  public String getGroupName() {
    if (budgetCategory == null) {
      return null;
    }
    return budgetCategory.getGroupName();
  }
  
  public void setGroupName(String groupName) {
    budgetCategory.setGroupName(groupName);
  }
  
  public Object getBudgetCategory() {
    if (fakeName != null) {
      return fakeName;
    }
    return budgetCategory;
  }
  
  public void addSummaryValues(BudgetCategoryWrapper other) {
    for (Map.Entry<String, Double> summaryEntry : other.summaryValues.entrySet()) {
      String key = summaryEntry.getKey();
      Double value = summaryEntry.getValue();
      summaryValues.put(key, value + summaryValues.getOrDefault(key, 0.0));
    }
  }
  
  @Override
  public boolean isFake() {
    return fakeName != null;
  }
  
  @Override
  protected Object getEntity() {
    return budgetCategory;
  }
  
  @Override
  public boolean isSummary() {
    return fakeName != null;
  }

  public void addSummaryValue(String header, Double value) {
    summaryValues.put(header, value);
  }

  public Double getSummaryValue(String columnName) {
    return summaryValues.get(columnName);
  }

  public static List<BudgetCategoryWrapper> getBudgetCategoryWrappers(BudgetCategory.Direction direction) {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    return em.createQuery("SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.BudgetCategoryWrapper(c) " +
            "FROM BudgetCategory c " +
            "WHERE :direction IS NULL OR c.direction = :direction " +
            "ORDER BY c.direction, c.groupName NULLS LAST, c.name",
        BudgetCategoryWrapper.class).
            setParameter("direction", direction).
            getResultList();
  }
  
  public static List<BudgetCategory> getBudgetCategories(BudgetCategory.Direction direction) {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    return em.createQuery("SELECT c " +
            "FROM BudgetCategory c " +
            "WHERE c.direction = :direction " +
            "ORDER BY c.groupName NULLS LAST, c.name",
        BudgetCategory.class).
            setParameter("direction", direction).
            getResultList();
  }
  
  private static void getYearlyBudgetCategorySummaryMap(
      String query,
      Map<Integer, BudgetCategoryWrapper> map,
      Set<String> columnNames) {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    List<Object[]> summaryList = em.createQuery(query).getResultList();
    for (Object[] line : summaryList) {
      BudgetCategoryWrapper budgetCategoryWrapper = map.get(((BudgetCategory)line[0]).getId());
      int year = (Integer)line[2];
      String header = String.format("%d (%s)", year, (String)line[1]);
      columnNames.add(header);
      budgetCategoryWrapper.addSummaryValue(header, (Double)line[3]);
    }
  }
  
  /**
   * Retrieves all the budget categories. Each one is populated with yearly
   * summaries of its corresponding expense or income.
   * @param paymentCategories
   * @param incomeCategories 
   */
  public static void getYearlyBudgetCategorySummaries(
      List<BudgetCategoryWrapper> paymentCategories,
      List<BudgetCategoryWrapper> incomeCategories,
      Set<String> columnNames) {
    paymentCategories.clear();
    paymentCategories.addAll(getBudgetCategoryWrappers(
        BudgetCategory.Direction.PAYMENT));
    incomeCategories.clear();
    incomeCategories.addAll(getBudgetCategoryWrappers(
        BudgetCategory.Direction.INCOME));
    
    Map<Integer, BudgetCategoryWrapper> budgetCategoryMap = new HashMap<>();
    for (BudgetCategoryWrapper budgetCategoryWrapper : paymentCategories) {
      budgetCategoryMap.put(budgetCategoryWrapper.getId(), budgetCategoryWrapper);
    }
    for (BudgetCategoryWrapper budgetCategoryWrapper : incomeCategories) {
      budgetCategoryMap.put(budgetCategoryWrapper.getId(), budgetCategoryWrapper);
    }
    
    // Collect expense summaries.
    getYearlyBudgetCategorySummaryMap(
        "SELECT e.budgetCategory, e.project.accountCurrency.code AS currency, FUNCTION('YEAR', e.paymentDate) AS year, SUM(a.accountingCurrencyAmount) " +
        "FROM ProjectExpense e, ExpenseSourceAllocation a " +
        "WHERE a.expense = e " +
        "GROUP BY e.budgetCategory, year, currency",
        budgetCategoryMap,
        columnNames);

    // Collect income summaries.
    getYearlyBudgetCategorySummaryMap(
        "SELECT s.project.incomeType AS incomeType, s.project.accountCurrency.code AS currency, FUNCTION('YEAR', s.availabilityDate) AS year, SUM(s.amount * s.exchangeRate) " +
        "FROM ProjectSource s " +
        "GROUP BY incomeType, year, currency",
        budgetCategoryMap,
        columnNames);
  }
  
  private static void createBudgetSummaryList(
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
          groupSum = current.createFakeCopy(current.getGroupName() + " összesen");
        } else {
          groupSum.addSummaryValues(current);
        }
      }
      
      summary.add(current);
      if (totalSum != null) {
        totalSum.addSummaryValues(current);
      } else {
        totalSum = current.createFakeCopy(summaryTitle);
      }
      
      previous = current;
    }
    if (groupSum != null) {
      summary.add(groupSum);
    }
    summary.add(totalSum);

  }

  /**
   * Processes the lists of income and expense budget categories, and inserts
   * summary lines after groups of categories.
   * @param paymentCategories
   * @param incomeCategories
   * @param output 
   */
  public static void createBudgetSummaryList(
      List<BudgetCategoryWrapper> paymentCategories,
      List<BudgetCategoryWrapper> incomeCategories,
      List<BudgetCategoryWrapper> output) {
    output.clear();
    createBudgetSummaryList(paymentCategories, "Kiadások összesen", output);
    createBudgetSummaryList(incomeCategories, "Bevételek összesen", output);
  }
  
  public static void createDefaultBudgetCategories() {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    
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
