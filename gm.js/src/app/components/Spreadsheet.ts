import {List} from 'immutable';
import {Component, Input, ChangeDetectionStrategy} from '@angular/core';
import {CellEntry} from 'app/components/CellEntry';
import {ListItem} from 'app/state/core/ListItem';
import {GenericTable} from 'app/state/ui/GenericTable';
import {TableColumn} from 'app/state/ui/TableColumn';
import {StateService} from 'app/components/StateService';

@Component({
  selector: 'Spreadsheet',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: 'app/components/Spreadsheet.html',
  styleUrls: ['app/components/Spreadsheet.css'],
})
export class Spreadsheet<T extends ListItem> {
  @Input() path: Array<any>;
  @Input() table: GenericTable<T>;
  @Input() rowStyleClasses: { [id:string]: string };
  @Input() list: Array<T>;
  @Input() columns: List<TableColumn>;

  stateService: StateService;

  constructor(stateService: StateService) {
    this.stateService = stateService;
  }

  getCssClassForItem(item: T): { [id: string]: boolean } {
    let resultClasses = {};
    for (let cssClass in this.rowStyleClasses) {
      if (this.rowStyleClasses.hasOwnProperty(cssClass)) {
        resultClasses[this.rowStyleClasses[cssClass]] = item[cssClass];
      }
    }
    return resultClasses;
  }

  trackById(index: number, item: T): number {
    return item.id;
  }

  trackByKey(index: number, column: TableColumn): string {
    return column.key;
  }

}
