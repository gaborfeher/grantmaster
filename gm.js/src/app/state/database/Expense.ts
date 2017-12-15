import {Record} from 'immutable';
import {BigNumber} from 'bignumber.js';

import {Changes} from 'app/state/core/Changes';
import {ListItem} from 'app/state/core/ListItem';
import {Utils} from 'app/utils/Utils';

class ExpenseRecord extends Record({
  id: -1,
  date: undefined,
  localAmount: undefined,
  foreignAmount: undefined,
  exchangeRate: undefined,
  accountNo: undefined,
  partner: undefined,
  category: undefined,

  multiPart: false,
  overshoot: false
}) {}

export class Expense extends ExpenseRecord implements ListItem {
  id: number;
  date: string;
  localAmount: BigNumber;
  foreignAmount: BigNumber;
  exchangeRate: BigNumber;
  category: string;

  multiPart: boolean;
  overshoot: boolean;

  resetComputed(): Expense {
    let that: Expense = this;
    return that.merge({
      foreignAmount: new BigNumber(0),
      exchangeRate: new BigNumber(0),
      multiPart: false,
      overshoot: false
    });
  }

  validate(): String[] {
    let errors = [];
    if (!this.date || !Utils.validateDate(this.date)) {
      errors.push('invalid date');
    }
    if (!this.localAmount || this.localAmount.lessThanOrEqualTo(0.0)) {
      errors.push('non-positive local amount');
    }
    return errors;
  }
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

