///<reference path='../../../../node_modules/immutable/dist/immutable.d.ts'/>
///<reference path='./TableColumn.ts'/>

import {Immutable, IRecord} from '../core/IRecord';
import {TableColumn} from './TableColumn';

export interface GenericTable<T> extends IRecord<GenericTable<T>> {
  newItem: T,
  newItemTemplate: T,
  columns: Array<TableColumn>,
  myPath: Array<string>

  resetNewItem(): GenericTable<T>;
}
export var GenericTable = Immutable.Record({
  newItem: undefined,
  newItemTemplate: undefined,
  columns: [],
  myPath: []
});
GenericTable.prototype.resetNewItem = function() {
  return this.set('newItem', this.newItemTemplate);
}
