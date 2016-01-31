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
    'path',
    'rowStyleClasses'
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
@View({
  templateUrl: 'app/components/Spreadsheet.html',
  styleUrls: ['app/components/Spreadsheet.css'],
  directives: [CellEntry, NgClass, NgFor, NgIf, NgModel],
})
export class Spreadsheet {
  @Input() path: Array<any>;
  @Input() table: GenericTable<any>;
  @Input() rowStyleClasses: any;

  stateService: StateService;

  constructor(stateService: StateService) {
    this.stateService = stateService;
  }

  getCssClassForItem(item: any): any {
    let that: Spreadsheet = this;
    let resultClasses = {};
    for (let cssClass in that.rowStyleClasses) {
      if (that.rowStyleClasses.hasOwnProperty(cssClass)) {
        resultClasses[that.rowStyleClasses[cssClass]] = item[cssClass];
      }
    }
    return resultClasses;
  }

}
