import {Record, List} from 'immutable';
import {TableColumn} from 'app/state/ui/TableColumn';

class GenericTableRecord extends Record({
  newItem: undefined,
  newItemTemplate: undefined,
  columns: List<TableColumn>([]),
  myPath: []
}) {}

export class GenericTable<T> extends GenericTableRecord {
  newItem: T;
  newItemTemplate: T;
  columns: List<TableColumn>;
  myPath: Array<string>;

  constructor(params?: any) {
    params ? super(params) : super();
  }

  resetNewItem(): GenericTable<T> {
    return this.set('newItem', this.newItemTemplate);
  }
}
