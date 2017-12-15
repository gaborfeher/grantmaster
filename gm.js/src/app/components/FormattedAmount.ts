import {Component, Input} from '@angular/core';

import {BigNumber} from 'bignumber.js';
import {Utils} from 'app/utils/Utils';

@Component({
  selector: 'FormattedAmount',
  templateUrl: './app/components/FormattedAmount.html',
  styleUrls: ['./app/components/FormattedAmount.css'],
})
export class FormattedAmount {
  @Input() amount: BigNumber;
  @Input() currency: string;

  formatted(): string {
    let s: string = Utils.bigFormat(this.amount);
    if (this.amount.greaterThan(0.0)) {
      s = '+' + s;
    }
    return s;
  }

  cssClass(): string {
    if (this.amount.greaterThan(0.0)) {
      return 'positive';
    } else if (this.amount.lessThan(0.0)) {
      return 'negative';
    } else {
      return 'zero';
    }
  }

}

