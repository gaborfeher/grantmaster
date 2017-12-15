import {Record} from 'immutable';
import {BigNumber} from 'bignumber.js';

import {Changes} from 'app/state/core/Changes';
import {ListItem} from 'app/state/core/ListItem';
import {Utils} from 'app/utils/Utils';

class IncomeRecord extends Record({
  id: -1,
  date: undefined,
  foreignAmount: undefined,
  exchangeRate: undefined,
  localAmount: undefined,
  spentForeignAmount: undefined,
  spentLocalAmount: undefined
}) {}

export class Income extends IncomeRecord implements ListItem {
  id: number;
  date: string;
  foreignAmount: BigNumber;
  exchangeRate: BigNumber;
  localAmount: BigNumber;
  spentForeignAmount: BigNumber;
  spentLocalAmount: BigNumber;

  spendInLocalCurrency(localAmount: BigNumber): Income {
    let that: Income = this;
    return that.merge({
      spentLocalAmount: that.spentLocalAmount.plus(localAmount),
      spentForeignAmount: that.spentForeignAmount.plus(localAmount.dividedBy(that.exchangeRate))
    });
  }

  refresh(): Income {
    let that: Income = this;
    return that.merge({
      localAmount: that.foreignAmount.times(that.exchangeRate)
    });
  }

  resetComputed(): Income {
    let that: Income = this;
    return that.merge({
      spentForeignAmount: new BigNumber(0),
      spentLocalAmount: new BigNumber(0)
    });
  }

  validate(): String[] {
    let that: Income = this;
    let errors = [];
    if (!that.date || !Utils.validateDate(that.date)) {
      errors.push('invalid date');
    }
    if (!that.foreignAmount || that.foreignAmount.lessThanOrEqualTo(0.0)) {
      errors.push('non-positive foreign amount');
    }
    if (!that.exchangeRate || that.exchangeRate.lessThanOrEqualTo(0.0)) {
      errors.push('non-positive exchange rate');
    }
    return errors;
  }

  remainingLocalAmount(): BigNumber {
    return this.localAmount.minus(this.spentLocalAmount);
  }

}

export function compareIncomes(a: Income, b: Income): number {
  if (a.date == b.date) {
    return 0;
  } else if (a.date > b.date) {
    return 1;
  } else {
    return -1;
  }
}

