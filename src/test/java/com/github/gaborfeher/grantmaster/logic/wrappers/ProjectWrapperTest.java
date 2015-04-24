package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author gabor
 */
public class ProjectWrapperTest {
  Currency HUF;
  Currency USD;
  Currency EUR;
  BudgetCategory SOME_GRANT;
  
  public ProjectWrapperTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() {
    assertTrue(DatabaseConnectionSingleton.getInstance().connectToMemoryFileForTesting());
    assertTrue(DatabaseConnectionSingleton.getInstance().runInTransaction(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        HUF = new Currency(); HUF.setCode("HUF"); em.persist(HUF);
        USD = new Currency(); USD.setCode("USD"); em.persist(USD);
        EUR = new Currency(); EUR.setCode("EUR"); em.persist(EUR);
        SOME_GRANT = new BudgetCategory(
            BudgetCategory.Direction.INCOME, "stuff", "Some kind of project grant");
        em.persist(SOME_GRANT);
        return true;
      }
    }));
  }
  
  @After
  public void tearDown() {
    DatabaseConnectionSingleton.getInstance().cleanup();
  }

  @Test
  public void testCreateProject() {
    final ProjectWrapper newWrapper = ProjectWrapper.createNew();
    newWrapper.setState(EntityWrapper.State.EDITING_NEW);
    newWrapper.setProperty("name", "testProject");
    newWrapper.setProperty("grantCurrency", USD);
    newWrapper.setProperty("accountCurrency", HUF);
    newWrapper.setProperty("incomeType", SOME_GRANT);
    
    assertTrue(DatabaseConnectionSingleton.getInstance().runInTransaction(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        newWrapper.save(em);
        return true;
      }
    }));
    assertEquals(EntityWrapper.State.SAVED, newWrapper.getState());
    
    DatabaseConnectionSingleton.getInstance().runWithEntityManager(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        Project project = em.find(Project.class, newWrapper.getId());
        assertEquals("testProject", project.getName());
        assertEquals("USD", project.getGrantCurrency().getCode());
        assertEquals("HUF", project.getAccountCurrency().getCode());
        assertEquals(SOME_GRANT.getId(), project.getIncomeType().getId());
        return true;
      }
    });
  }

 
}
