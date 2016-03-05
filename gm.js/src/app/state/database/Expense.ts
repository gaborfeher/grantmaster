///<reference path='../core/BigNumber.ts'/>
///<reference path='../core/Changes.ts'/>
///<reference path='../core/IRecord.ts'/>

import {BigNumber} from '../core/BigNumber';
import {Changes} from '../core/Changes';
import {Immutable, IRecord} from '../core/IRecord';

export interface Expense extends IRecord<Expense> {
  date: string;
  localAmount: BigNumber;
  foreignAmount: BigNumber;
  exchangeRate: BigNumber;
  category: string;

  multiPart: boolean;
  overshoot: boolean;

  resetComputed(): Expense;
}
export var Expense = Immutable.Record({
  date: undefined,
  localAmount: undefined,
  foreignAmount: undefined,
  exchangeRate: undefined,
  accountNo: undefined,
  partner: undefined,
  category: undefined,

  multiPart: false,
  overshoot: false
});
Expense.prototype.resetComputed = function(): Expense {
  let that: Expense = this;
  return that.merge({
    foreignAmount: new BigNumber(0),
    exchangeRate: new BigNumber(0),
    multiPart: false,
    overshoot: false
  });
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

