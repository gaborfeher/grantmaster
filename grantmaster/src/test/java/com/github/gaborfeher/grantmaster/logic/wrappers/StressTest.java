/*
 * This file is a part of GrantMaster.
 * Copyright (C) 2015 Gabor Feher <feherga@gmail.com>
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

import com.github.gaborfeher.grantmaster.framework.utils.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import java.time.LocalDate;
import javax.persistence.EntityManager;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class StressTest extends TestBase {
  Currency HUF;
  Currency USD;
  Currency EUR;
  BudgetCategory SOME_GRANT;
  BudgetCategory SOME_EXPENSE;
  Project PROJECT1;
  ProjectReport PROJECT1_REPORT;

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
      em.persist(SOME_EXPENSE);
      PROJECT1 = TestUtils.createProject(
          em, "project1", USD, HUF, SOME_GRANT, Project.ExpenseMode.NORMAL_AUTO_BY_SOURCE);
      PROJECT1_REPORT = TestUtils.createProjectReport(
          em, PROJECT1, LocalDate.of(2015, 4, 1));
      TestUtils.createProjectSource(em, PROJECT1, LocalDate.of(2000, 1,1), PROJECT1_REPORT, "100", "1000000");
      return true;
    }));
  }

  @After
  public void tearDown() {
    DatabaseSingleton.INSTANCE.close();
  }

  @Test
  public void testAddManyExpenses() {
    final int expensesToAdd = 500;
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < expensesToAdd; ++i) {
      final int j = i;
      assertTrue(DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
        int amount = j * 100 + 5;
        TestUtils.createProjectExpense(
            em,
            PROJECT1,
            SOME_EXPENSE,
            LocalDate.ofEpochDay(j),
            PROJECT1_REPORT,
            Integer.toString(amount),
            HUF,
            Integer.toString(amount));
        return true;
      }));
    }
    long finishTime = System.currentTimeMillis();
    long elapsedTimeSecs = (finishTime - startTime) / 1000;
    System.out.printf(
        "Adding %d expenses (in order of increasing date) took %d seconds.\n",
        expensesToAdd,
        elapsedTimeSecs);
  }

}
