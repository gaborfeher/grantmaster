import {Record} from 'immutable';

class CurrencyRecord extends Record({
  name: ''
}) {}

export class Currency extends CurrencyRecord {
  name: string;

  validate(): String[] {
    let errors = [];
    if (!this.name) {
      errors.push('empty currency name');
    }
    return errors;
  }
}

