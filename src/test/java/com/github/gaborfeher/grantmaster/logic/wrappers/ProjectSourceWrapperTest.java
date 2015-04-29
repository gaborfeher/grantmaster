package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class ProjectSourceWrapperTest {
  Currency HUF;
  Currency USD;
  Currency EUR;
  BudgetCategory SOME_GRANT;
  Project PROJECT1;
  ProjectReport PROJECT1_REPORT1;
  ProjectReport PROJECT1_REPORT2;

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
      PROJECT1 = TestUtils.createProject(em, "project1", USD, HUF, SOME_GRANT);
      PROJECT1_REPORT1 = TestUtils.createProjectReport(
          em, PROJECT1, LocalDate.of(2015, 4, 1));
      PROJECT1_REPORT2 = TestUtils.createProjectReport(
          em, PROJECT1, LocalDate.of(2015, 8, 1));
      return true;
    }));
  }
  
  @After
  public void tearDown() {
    DatabaseSingleton.INSTANCE.cleanup();
  }
  
    @Test
  public void testSortingForAllocation() {
    final ObjectHolder<Long> sourceId1 = new ObjectHolder<>();
    final ObjectHolder<Long> sourceId2 = new ObjectHolder<>();
    final ObjectHolder<Long> sourceId3 = new ObjectHolder<>();
    final ObjectHolder<Long> sourceId4 = new ObjectHolder<>();
    // Setup.
    assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      sourceId1.set(
          TestUtils.createProjectSource(
              em,
              PROJECT1,
              LocalDate.of(2014, 2, 1),
              PROJECT1_REPORT2,
              "21.1",
              "300000.1"
          ).getId());  
      sourceId2.set(
          TestUtils.createProjectSource(
              em,
              PROJECT1,
              LocalDate.of(2014, 4, 2),
              PROJECT1_REPORT1,
              "21.2",
              "300000.2"
          ).getId());
      sourceId3.set(
          TestUtils.createProjectSource(
              em,
              PROJECT1,
              LocalDate.of(2014, 1, 1),
              PROJECT1_REPORT2,
              "21.3",
              "300000.3"
          ).getId());
      sourceId4.set(
          TestUtils.createProjectSource(
              em,
              PROJECT1,
              LocalDate.of(2014, 4, 1),
              PROJECT1_REPORT1,
              "21.4",
              "300000.4"
          ).getId());
      return true;
    }));
    // Check order.
    assertTrue(DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      List<ProjectSourceWrapper> sources =
          ProjectSourceWrapper.getProjectSourceListForAllocation(em, PROJECT1);
      assertEquals(4, sources.size());
      assertEquals(sourceId4.get(), sources.get(0).getId());
      assertEquals(sourceId2.get(), sources.get(1).getId());
      assertEquals(sourceId3.get(), sources.get(2).getId());
      assertEquals(sourceId1.get(), sources.get(3).getId());
      return true;
    }));
  }

}
