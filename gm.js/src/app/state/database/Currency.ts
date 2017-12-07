import {Immutable, IRecord} from 'app/state/core/IRecord';

export interface Currency extends IRecord<Currency> {
  name: string;

  validate(): String[];
}
export var Currency = Immutable.Record({
  name: ''
});
Currency.prototype.validate = function(): String[] {
  let errors = [];
  if (!this.name) {
    errors.push('empty currency name');
  }
  return errors;
}
