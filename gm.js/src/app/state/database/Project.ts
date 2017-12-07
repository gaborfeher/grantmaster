///<reference path='../../../../node_modules/immutable/dist/immutable.d.ts'/>

import {BigNumber, bigMin} from 'app/state/core/BigNumber';
import {Changes} from 'app/state/core/Changes';
import {Immutable, IRecord} from 'app/state/core/IRecord';
import {Expense, compareExpenses} from 'app/state/database/Expense';
import {Income, compareIncomes} from 'app/state/database/Income';
import {ProjectCategory} from 'app/state/database/ProjectCategory';

export interface Project extends IRecord<Project> {
  name: string;
  foreignCurrency: string;
  incomeCategory: string;

  expenses: Immutable.List<Expense>;
  incomes: Immutable.List<Income>;
  categories: Immutable.List<ProjectCategory>;

  recomputeExpenses(): Project;
  recomputeBudgetCategories(): Project;

  recomputeIncomes(): Project;

  addLastExpenseInternal(expense: Expense): Project;
}
export var Project = Immutable.Record({
  name: '',
  incomes: Immutable.List(),
  expenses: Immutable.List(),
  categories: Immutable.List(),
  foreignCurrency: '',
  incomeCategory: '',
});
Project.prototype.recomputeBudgetCategories = function() {
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
Project.prototype.recomputeExpenses = function() {
  let that: Project = this;
  let expenseList = that.expenses.sort(compareExpenses);
  // Clean project.
  that = that.merge({
    expenses: Immutable.List(),
    incomes: that.incomes.map(income => income.resetComputed())
  });
  // Add expenses one by one.
  for (let i = 0; i < expenseList.size; ++i) {
    that = that.addLastExpenseInternal(expenseList.get(i).resetComputed());
  }
  return that.recomputeBudgetCategories();
}
Project.prototype.addLastExpenseInternal = function(expense: Expense): Project {
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
      valueToTake = bigMin(neededValue, availableValue);
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
};
function findFirstNonEmptyIncome(incomes: Immutable.List<Income>): number {
  for (let i = 0; i < incomes.size; ++i) {
    let income = incomes.get(i);
    if (income.spentForeignAmount.lessThan(income.foreignAmount)) {
      return i;
    }
  }
  return incomes.size - 1;
}
Project.prototype.recomputeIncomes = function(): Project {
  let that: Project = this;
  return that
    .set(
      'incomes',
      that
        .incomes
        .sort(compareIncomes)
        .map(income => income.refresh()))
    .recomputeExpenses();
}
Project.prototype.onChange = function(property: string, changes: Changes): Project {
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
