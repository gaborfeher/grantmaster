import {BigNumber} from 'bignumber.js';

export namespace Utils {
  export function validateDate(date: string): boolean {
    for (var i = 0; i < date.length; ++i) {
      if (date[i] === '-' || date[i] >= '0' && date[i] <= '9') {
        // ok
      } else {
        return false;
      }
    }

    var dateParts = date.split('-');
    if (dateParts.length != 3) {
      return false;
    }
    var y = parseInt(dateParts[0]);
    var m = parseInt(dateParts[1]);
    var d = parseInt(dateParts[2]);
    if (y < 0 || y > 3000) {
      return false;
    }
    if (m < 1 || m > 12) {
      return false;
    }
    if (d < 1 || d > 31) {
      return false;
    }
    return true;
  }

  export function bigMin(a: BigNumber, b: BigNumber): BigNumber {
    return a.lessThan(b) ? a : b;
  }

  export function bigFormat(num: BigNumber): string {
    let s = num.round(3, BigNumber.ROUND_HALF_UP).toFormat();
    let dotPos = s.indexOf('.');
    if (dotPos < 0) {
      s += '.';
      dotPos = s.length - 1;
    }
    let numDecimals = s.length - dotPos - 1;
    for (let i = numDecimals; i < 3; ++i) {
      s += '0';
    }
    return s;
  }
}
