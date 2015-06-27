package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.framework.base.RowEditState;
import com.github.gaborfeher.grantmaster.framework.utils.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProjectWrapperTest {
  Currency HUF;
  Currency USD;
  Currency EUR;
  BudgetCategory SOME_GRANT;
  BudgetCategory SOME_EXPENSE;
  
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
    assertTrue(DatabaseSingleton.INSTANCE.connectToMemoryFileForTesting());
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      HUF = TestUtils.createCurrency(em, "HUF");
      USD = TestUtils.createCurrency(em, "USD");
      EUR = TestUtils.createCurrency(em, "EUR");
      SOME_GRANT = new BudgetCategory(
          BudgetCategory.Direction.INCOME, "i.stuff", "Some kind of project grant");
      em.persist(SOME_GRANT);
      SOME_EXPENSE = new BudgetCategory(
          BudgetCategory.Direction.PAYMENT, "p.stuff", "Some kind of expense");
      return true;
    }));
  }
  
  @After
  public void tearDown() {
    DatabaseSingleton.INSTANCE.close();
  }

  @Test
  public void testCreateProject() {
    final ProjectWrapper newWrapper = ProjectWrapper.createNew();
    newWrapper.setState(RowEditState.EDITING_NEW);
    newWrapper.setProperty("name", "testProject", String.class);
    newWrapper.setProperty("grantCurrency", USD, Currency.class);
    newWrapper.setProperty("accountCurrency", HUF, Currency.class);
    newWrapper.setProperty("incomeType", SOME_GRANT, BudgetCategory.class);
    
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) ->
        newWrapper.save(em)));
    assertEquals(RowEditState.SAVED, newWrapper.getState());
    
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      Project project = em.find(Project.class, newWrapper.getId());
      assertEquals("testProject", project.getName());
      assertEquals("USD", project.getGrantCurrency().getCode());
      assertEquals("HUF", project.getAccountCurrency().getCode());
      assertEquals(SOME_GRANT.getId(), project.getIncomeType().getId());
      return true;
    });
  }
  
  @Test
  public void testDeleteProject() {
    // Create projects for testing.
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      Project project1 = TestUtils.createProject(em, "P1", USD, HUF, SOME_GRANT);
      ProjectReport report1 = TestUtils.createProjectReport(em, project1, LocalDate.of(2015, 5, 18));
      TestUtils.createProjectSource(em, project1, LocalDate.of(2014, 1, 1), report1, "100", "1000");
      TestUtils.createProjectExpense(em, project1, SOME_EXPENSE, LocalDate.of(2014, 6, 4), report1, "1000", HUF, "1000");
      TestUtils.createProjectNote(em, project1, new Timestamp(1234), "hello, world");
      TestUtils.createProjectBudgetLimit(em, project1, SOME_EXPENSE, null, "1000");
      TestUtils.createProject(em, "P2", USD, HUF, SOME_GRANT);
      return true;
    }));
    // Query projects into a list, to simulate real use case, when the wrappers
    // have detached objects.
    final List<ProjectWrapper> projects = new ArrayList<>();    
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      projects.addAll(ProjectWrapper.getProjects(em));
      return true;
    }));
    // Projects in list get detached now.
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      ProjectWrapper projectToDelete = TestUtils.findProjectByName(em, "P1");
      projectToDelete.delete(em);
      return true;
    }));
    // Verify effect.
    assertTrue(DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      List<ProjectWrapper> list = ProjectWrapper.getProjects(em);
      assertEquals(1, list.size());
      assertEquals("P2", list.get(0).getEntity().getName());
      return true;
    }));
    
    
  }

 
}
