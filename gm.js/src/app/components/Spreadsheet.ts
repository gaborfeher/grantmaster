///<reference path='../state/ui/GenericTable.ts'/>

import {Component, Input, ChangeDetectionStrategy} from '@angular/core';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import {NgModel} from '@angular/forms';
import {CellEntry} from './CellEntry';
import {GenericTable} from '../state/ui/GenericTable';
import {StateService} from './StateService';

@Component({
  selector: 'Spreadsheet',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: 'app/components/Spreadsheet.html',
  styleUrls: ['app/components/Spreadsheet.css'],
})
export class Spreadsheet {
  @Input() path: Array<any>;
  @Input() table: GenericTable<any>;
  @Input() rowStyleClasses: any;
  @Input() list: any;
  @Input() columns: any;

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
