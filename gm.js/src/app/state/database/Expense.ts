///<reference path='../core/BigNumber.ts'/>
///<reference path='../core/Changes.ts'/>
///<reference path='../core/IRecord.ts'/>

import {BigNumber} from '../core/BigNumber';
import {Changes} from '../core/Changes';
import {IRecord} from '../core/IRecord';

var Immutable = require('../../../../node_modules/immutable/dist/immutable.js');

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
Expense.prototype.onChange = function(property: string, changes: Changes): Expense {
  if (property === 'date' || property === 'localAmount') {
    changes.significantExpenseChange = true;
  } else if (property === 'category') {
    changes.budgetCategoryChange = true;
  }
  return this;
}
export function compareExpenses(a: Expense, b: Expense): number {
  if (a.date == b.date) {
    return 0;
  } else if (a.date > b.date) {
    return 1;
  } else {
    return -1;
  }
}

