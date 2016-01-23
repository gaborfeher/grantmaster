///<reference path='../state/ui/GenericTable.ts'/>

import {Component, Input, View, ChangeDetectionStrategy} from 'angular2/core';
import {NgClass, NgFor, NgIf, NgModel} from 'angular2/common';
import {bootstrap} from 'angular2/platform/browser';
import {CellEntry} from './CellEntry';
import {GenericTable} from '../state/ui/GenericTable';
import {StateService} from './StateService';

@Component({
  selector: 'Spreadsheet',
  properties: [
    'list',
    'table',
    'path'
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
@View({
  templateUrl: 'app/components/Spreadsheet.html',
  styleUrls: ['app/components/Spreadsheet.css'],
  directives: [CellEntry, NgFor, NgIf, NgModel],
})
export class Spreadsheet {
  @Input() path: Array<any>;
  @Input() table: GenericTable<any>;

  stateService: StateService;

  constructor(stateService: StateService) {
    this.stateService = stateService;
  }

  getValue(val: any): any {
    if (val === undefined) {
      return '';
    } else {
      return val;
    }
  }

  addNewItem() {
    this.stateService.addNewItem(this.table, this.path);
  }
}
