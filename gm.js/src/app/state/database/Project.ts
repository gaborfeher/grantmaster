///<reference path='../../../../node_modules/immutable/dist/immutable.d.ts'/>
///<reference path='../core/BigNumber.ts'/>
///<reference path='../core/Changes.ts'/>
///<reference path='../core/IRecord.ts'/>
///<reference path='./Expense.ts'/>
///<reference path='./Income.ts'/>
///<reference path='./ProjectCategory.ts'/>

import {BigNumber, bigMin} from '../core/BigNumber';
import {Changes} from '../core/Changes';
import {IRecord} from '../core/IRecord';
import {Expense, compareExpenses} from './Expense';
import {Income, compareIncomes} from './Income';
import {ProjectCategory} from './ProjectCategory';

var Immutable = require('../../../../node_modules/immutable/dist/immutable.js');

export interface Project extends IRecord<Project> {
  name: string;

  expenses: Immutable.List<Expense>;
  incomes: Immutable.List<Income>;
  categories: Immutable.List<ProjectCategory>;

  recomputeExpenses(): Project;
  recomputeBudgetCategories(): Project;

  recomputeIncomes(): Project;

  addLastExpenseInternal(expense: Expense): Project;
  fulfillExpense(expense: Expense, fulfilledAmount: BigNumber): Project;
}
export var Project = Immutable.Record({
  name: '',
  incomes: Immutable.List(),
  expenses: Immutable.List(),
  categories: Immutable.List(),

  newExpense: new Expense({}),
  newExpenseTemplate: new Expense({}),
  newIncome: new Income({}),
  newIncomeTemplate: new Income({}),
  newCategory: new ProjectCategory({}),
  newCategoryTemplate: new ProjectCategory({})
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
  that.expenses.forEach(
    expense => {
      if (expense.category in map) {
        // TODO: make category compulsory, remove this check
        map[expense.category] =
          map[expense.category].addSpentAmounts(expense.localAmount, expense.foreignAmount);
      }
      return true;
    }
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
  return that.fulfillExpense(expense, new BigNumber(0));
};
Project.prototype.fulfillExpense = function(expense: Expense, fulfilledAmount: BigNumber): Project {
  let that: Project = this;
  if (fulfilledAmount >= expense.localAmount) {
    let exchangeRate = expense.localAmount.dividedBy(expense.foreignAmount);
    return that.merge({
      expenses: that.expenses.push(
        expense.set('exchangeRate', exchangeRate)
      )
    });
  } else {
    let pos = findFirstNonEmptyIncome(that.incomes);
    let income = that.incomes.get(pos);
    let neededValue = expense.localAmount.minus(fulfilledAmount);
    let valueToTake = neededValue;
    if (pos < that.incomes.size - 1) {
      // If this is not the last possible income, then cap fulfillment amount
      // to this income.
      let availableValue = income.localAmount.minus(income.spentLocalAmount);
      valueToTake = bigMin(neededValue, availableValue);
    }
    return that.merge({
      incomes: that.incomes.set(pos, income.spendInLocalCurrency(valueToTake))
    }).fulfillExpense(
      expense.merge({
        foreignAmount: expense.foreignAmount.plus(valueToTake.dividedBy(income.exchangeRate))
      }), fulfilledAmount.plus(valueToTake));
  }
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
  if (property === 'expenses') {
    changes.significantExpenseChange = true;
  } else if (property === 'incomes') {
    changes.significantIncomeChange = true;
  } else if (property === 'categories') {
    changes.projectCategoryListChange = true;
  }

  if (changes.significantExpenseChange) {
    return that.recomputeExpenses();
  } else if (changes.budgetCategoryChange) {
    return that.recomputeBudgetCategories();
  } else if (changes.significantIncomeChange) {
    return that.recomputeIncomes();
  } else {
    return that;
  }
}
