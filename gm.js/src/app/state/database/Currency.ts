import {Record} from 'immutable';
import {ListItem} from 'app/state/core/ListItem';

class CurrencyRecord extends Record({
  name: '',
  id: -1,
}) {}

export class Currency extends CurrencyRecord implements ListItem {
  name: string;
  id: number;

  validate(): String[] {
    let errors = [];
    if (!this.name) {
      errors.push('empty currency name');
    }
    return errors;
  }
}

