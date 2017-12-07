import {List, Record} from 'immutable';

class TableColumnRecord extends Record({
  key: '',
  value: '',
  kind: '',
  items: List(),
  constraints: List<string>(),
  editable: true,
  editableAtCreation: true
}) {}

export class TableColumn extends TableColumnRecord {
  key: string;
  value: string;
  kind: string;
  items: List<string>;
  constraints: List<string>;
  editable: boolean;
  editableAtCreation: boolean;

  constructor(params?: any) {
    params ? super(params) : super();
  }

  isEditable(creationMode: boolean): boolean {
    return creationMode ? this.editableAtCreation : this.editable;
  }
}
