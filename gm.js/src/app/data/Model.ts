///<reference path='../../../node_modules/immutable/dist/immutable.d.ts'/>

var Immutable = require('../../../node_modules/immutable/dist/immutable.js');
var BigNumber = require('../../../node_modules/bignumber.js/bignumber.js');

BigNumber.config({
  DECIMAL_PLACES: 30,
  ROUNDING_MODE: BigNumber.ROUND_HALF_EVEN,
  FORMAT: {
    groupSeparator: '',
    decimalSeparator: '.'
  }
});

export interface BigNumber {
  minus(other: BigNumber): BigNumber;
  plus(other: BigNumber): BigNumber;
  dividedBy(other: BigNumber): BigNumber;
  times(other: BigNumber): BigNumber;
  lessThan(other: BigNumber): boolean;
}
function bigMin(a: BigNumber, b: BigNumber): BigNumber {
  return a.lessThan(b) ? a : b;
}

export interface IRecord<T> extends Immutable.Record.Class {
  // Scavenged from:
  // https://github.com/facebook/immutable-js/blob/master/type-definitions/Immutable.d.ts

  constructor(vals: any);

  getIn(searchKeyPath: Array<any>, notSetValue?: any): any;
  getIn(searchKeyPath: Immutable.Iterable<any, any>, notSetValue?: any): any;

  setIn(keyPath: Array<any>, value: any): T;
  setIn(keyPath: Immutable.Iterable<any, any>, value: any): T;
  set(key: string, value: any): T;

  updateIn(
    keyPath: Array<any>,
    updater: (value: any) => any
    ): T;

  merge(mergeValues: any): T;

  toObject(): any;
  toJS(): any;
  toJSON(): any;
}

export interface Income extends IRecord<Income> {
  date: string;
  foreignAmount: BigNumber;
  exchangeRate: BigNumber;
  localAmount: BigNumber;
  spentForeignAmount: BigNumber;
  spentLocalAmount: BigNumber;

  spendInLocalCurrency(localAmount: BigNumber): Income;
  refresh(): Income;
  resetComputed(): Income;
}

export interface TagNode extends IRecord<TagNode> {
  name: string;
  subTags: Immutable.List<TagNode>;

  summaries: Immutable.OrderedMap<string, BigNumber>;
}
export var TagNode = Immutable.Record({
  name: undefined,
  subTags: Immutable.List(),
  summaries: Immutable.OrderedMap()
});

export interface ProjectCategory extends IRecord<ProjectCategory> {
  tagName: string;
  limitForeign: BigNumber;
  limitPercentageForeign: BigNumber;

  spentForeign: BigNumber;
  spentLocal: BigNumber;

  reset(): ProjectCategory;
  addSpentAmounts(local: BigNumber, foreign: BigNumber);
}
export var ProjectCategory = Immutable.Record({
  tagName: '',
  limitForeign: undefined,
  limitPercentageForeign: undefined,
  spentForeign: new BigNumber(0.0),
  spentLocal: new BigNumber(0.0)
});
ProjectCategory.prototype.reset = function(): ProjectCategory {
  var that: ProjectCategory = this;
  return that.merge({
    spentLocal: new BigNumber(0.0),
    spentForeign: new BigNumber(0.0)
  });
};
ProjectCategory.prototype.addSpentAmounts = function(local: BigNumber, foreign: BigNumber) {
  var that: ProjectCategory = this;
  return that.merge({
    spentLocal: that.spentLocal.plus(local),
    spentForeign: that.spentForeign.plus(foreign)
  });
}


export var Income = Immutable.Record({
  date: undefined,
  foreignAmount: undefined,
  exchangeRate: undefined,
  localAmount: undefined,
  spentForeignAmount: undefined,
  spentLocalAmount: undefined
});
Income.prototype.refresh = function(): Income {
  let that: Income = this;
  return that.merge({
    localAmount: that.foreignAmount.times(that.exchangeRate)
  });
};
Income.prototype.resetComputed = function(): Income {
  let that: Income = this;
  return that.merge({
    spentForeignAmount: new BigNumber(0),
    spentLocalAmount: new BigNumber(0)
  });
};
Income.prototype.spendInLocalCurrency = function(localAmount: BigNumber): Income {
  let that: Income = this;
  return that.merge({
    spentLocalAmount: that.spentLocalAmount.plus(localAmount),
    spentForeignAmount: that.spentForeignAmount.plus(localAmount.dividedBy(that.exchangeRate))
  });
};
function compareIncomes(a: Income, b: Income): number {
  if (a.date == b.date) {
    return 0;
  } else if (a.date > b.date) {
    return 1;
  } else {
    return -1;
  }
}


export interface Expense extends IRecord<Expense> {
  date: string;
  localAmount: BigNumber;
  foreignAmount: BigNumber;
  exchangeRate: BigNumber;
  category: string;

  resetComputed(): Expense;
}
export var Expense = Immutable.Record({
  date: undefined,
  localAmount: undefined,
  foreignAmount: undefined,
  exchangeRate: undefined,
  accountNo: undefined,
  partner: undefined,
  category: undefined
});
Expense.prototype.resetComputed = function(): Expense {
  let that: Expense = this;
  return that
    .set('foreignAmount', new BigNumber(0))
    .set('exchangeRate', new BigNumber(0));
}
function compareExpenses(a: Expense, b: Expense): number {
  if (a.date == b.date) {
    return 0;
  } else if (a.date > b.date) {
    return 1;
  } else {
    return -1;
  }
}


export interface Project extends IRecord<Project> {
  name: string;

  expenses: Immutable.List<Expense>;
  incomes: Immutable.List<Income>;
  categories: Immutable.List<ProjectCategory>;

  addExpense(expense: Expense): Project;
  updateExpenseAndPropagate(pos: number, expense: Expense): Project;
  recomputeExpenses(): Project;
  recomputeBudgetCategories(): Project;

  addIncome(income: Income): Project;
  updateIncomeAndPropagate(pos: number, income: Income): Project;
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
Project.prototype.updateExpenseAndPropagate = function(pos: number, expense: Expense): Project {
  let that: Project = this;
  return that.setIn(['expenses', pos], expense).recomputeExpenses();
}
Project.prototype.addExpense = function(expense: Expense): Project {
  let that: Project = this;
  return that
    .set('expenses', that.expenses.push(expense))
    .recomputeExpenses()
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
Project.prototype.addIncome = function(income: Income): Project {
  let that: Project = this;
  return that
    .merge({ incomes: that.incomes.push(income) })
    .recomputeIncomes();
}
Project.prototype.updateIncomeAndPropagate = function(pos: number, income: Income): Project {
  let that: Project = this;
  return that
    .setIn(['incomes', pos], income.refresh())
    .recomputeIncomes();
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


export interface Database extends IRecord<Database> {
  projects: Immutable.List<Project>;
  budgetCategories: TagNode;

  addProject(project: Project): Database;
  renameTag(oldName: string, newName: string): Database;
  recomputeBudgetCategories(): Database;
}
export var Database = Immutable.Record({
  projects: Immutable.List([]),
  budgetCategories: new TagNode({name: 'Budget categories'}),
  programs: Immutable.Map({}),
  currencies: Immutable.Map({}),
});
Database.prototype.addProject = function(project: Project): Database {
  var that: Database = this;
  return that.set('projects', that.projects.push(project));
};
Database.prototype.renameTag = function(oldName: string, newName: string) {
  var that: Database = this;
  return that.set(
    'projects',
    that.projects.map(
      project => project.merge({
        categories: project.categories.map(
          category => {
            if (category.tagName === oldName) {
              return category.set('tagName', newName);
            } else {
              return category;
            }
          }),
        expenses: project.expenses.map(
          expense => {
            if (expense.category === oldName) {
              return expense.set('category', newName);
            } else {
              return expense;
            }
          }
        )
      })));
}
function mapBudgetCategories(node: TagNode, map: Object): TagNode {
  node = node.set('subTags', node.subTags.map(subTag => mapBudgetCategories(subTag, map)));
  let items = {};
  if (node.name in map) {
    let localItems = map[node.name];
    for (var key in localItems) {
      if (localItems.hasOwnProperty(key)) {
        let year = key.split(':')[1];
        items[year] = localItems[key];
      }
    }
  }

  node.subTags.forEach(
    subTag => {
      subTag.summaries.forEach(
        (summaryItem, year) => {
          items[year] = items[year] || new BigNumber(0);
          items[year] = items[year].plus(summaryItem);
          return true;
        }
      );
      return true;
    });


  let orderedItems = Immutable.OrderedMap();
  for (var year in items) {
    if (items.hasOwnProperty(year)) {
      orderedItems = orderedItems.set(year, items[year]);
    }
  }
  
  node = node.set('summaries', orderedItems);
  
  return node;
}
Database.prototype.recomputeBudgetCategories = function() {
  let that: Database = this;
  var map = {};
  that.projects.forEach(
    project => {
      project.expenses.forEach(
        expense => {
          let year = expense.date.split('-')[0];
          let amount = expense.localAmount;
          let category = expense.category;
          let key = category + ':' + year;
          map[category] = map[category] || {};
          let baseAmount = map[category][key] || new BigNumber(0);
          map[category][key] = baseAmount.plus(amount);
          return true;
        });
      return true;
    });
  return that.set('budgetCategories', mapBudgetCategories(that.budgetCategories, map));
}
