import {List, Record} from 'immutable';
import {BigNumber} from 'bignumber.js';

import {Changes} from 'app/state/core/Changes';
import {Expense, compareExpenses} from 'app/state/database/Expense';
import {Income, compareIncomes} from 'app/state/database/Income';
import {ProjectCategory} from 'app/state/database/ProjectCategory';
import {Utils} from 'app/utils/Utils';

class ProjectRecord extends Record({
  name: '',
  incomes: List(),
  expenses: List(),
  categories: List(),
  foreignCurrency: '',
  incomeCategory: '',
  remainingLocalAmount: new BigNumber(0.0),
}) {}

export class Project extends ProjectRecord {
  name: string;
  foreignCurrency: string;
  incomeCategory: string;
  remainingLocalAmount: BigNumber;

  expenses: List<Expense>;
  incomes: List<Income>;
  categories: List<ProjectCategory>;

  onChange(property: string, changes: Changes): Project {
    let that: Project = this;
    changes.projectProperty = property;

    if (property === 'expenses') {
      return that.recomputeExpenses();
    } else if (property === 'categories') {
      // The only reason we recompute here is because limits may have changed.
      return that.recomputeBudgetCategories();
    } else if (property === 'incomes') {
      return that.recomputeIncomes();
    } else {
      return that;
    }
  }

  recomputeBudgetCategories(): Project {
    let that: Project = this;
    let map = {};
    that.categories.forEach(
      category => {
        map[category.tagName] = category.reset();
        return true;
      }
    );
    that = that.set(
      'expenses',
      that.expenses.map(
        expense => {
          let overshoot = expense.overshoot;
          if (expense.category in map) {
            // TODO: make category compulsory, remove this check
            map[expense.category] =
              map[expense.category].addSpentAmounts(expense.localAmount, expense.foreignAmount);
            if (map[expense.category].overshoot) {
              overshoot = true;
            }
          }
          return expense.set('overshoot', overshoot);
        }
      )
    );
    return that.set('categories', that.categories.map(category => map[category.tagName]));
  }

  recomputeExpenses(): Project {
    let that: Project = this;
    let expenseList = that.expenses.sort(compareExpenses);
    // Clean project.
    that = that.merge({
      expenses: List(),
      incomes: that.incomes.map(income => income.resetComputed())
    });
    // Add expenses one by one.
    for (let i = 0; i < expenseList.size; ++i) {
      that = that.addLastExpenseInternal(expenseList.get(i).resetComputed());
    }
    return that
        .recomputeRemaining()
        .recomputeBudgetCategories();
  }

  recomputeRemaining(): Project {
    let remaining: BigNumber = this.incomes
        .map(
            (i: Income) => i.remainingLocalAmount())
        .reduce(
            (a: BigNumber, b: BigNumber) => a.plus(b),
            new BigNumber(0));
    return this.set('remainingLocalAmount', remaining);
  }

  recomputeIncomes(): Project {
    return this
      .set(
        'incomes',
        this
          .incomes
          .sort(compareIncomes)
          .map(income => income.refresh()))
      .recomputeExpenses();
  }

  addLastExpenseInternal(expense: Expense): Project {
    let that: Project = this;

    let fulfilledAmount = new BigNumber(0);
    let iteration = 0;
    while (fulfilledAmount.lessThan(expense.localAmount)) {
      iteration += 1;
      let pos = findFirstNonEmptyIncome(that.incomes);
      let income = that.incomes.get(pos);
      let neededValue = expense.localAmount.minus(fulfilledAmount);
      let valueToTake = neededValue;
      let availableValue = income.localAmount.minus(income.spentLocalAmount);
      if (pos < that.incomes.size - 1) {
        // If this is not the last possible income, then cap fulfillment amount
        // to this income.
        valueToTake = Utils.bigMin(neededValue, availableValue);
      } else if (availableValue.lessThan(neededValue)) {
        expense = expense.set('overshoot', true);
      }
      that = that.merge({
        incomes: that.incomes.set(pos, income.spendInLocalCurrency(valueToTake))
      });
      expense = expense.merge({
        foreignAmount:
          expense.foreignAmount.plus(valueToTake.dividedBy(income.exchangeRate)),
        multiPart: iteration > 1
      });
      fulfilledAmount = fulfilledAmount.plus(valueToTake);
    }

    let exchangeRate = expense.localAmount.dividedBy(expense.foreignAmount);
    return that.merge({
      expenses: that.expenses.push(
        expense.set('exchangeRate', exchangeRate)
      )
    });
  }
}
function findFirstNonEmptyIncome(incomes: List<Income>): number {
  for (let i = 0; i < incomes.size; ++i) {
    let income = incomes.get(i);
    if (income.spentForeignAmount.lessThan(income.foreignAmount)) {
      return i;
    }
  }
  return incomes.size - 1;
}
