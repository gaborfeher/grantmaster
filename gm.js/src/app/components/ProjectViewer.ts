import {Component, Input, ChangeDetectionStrategy} from '@angular/core';
import {BudgetCategorySelector} from './BudgetCategorySelector';
import {CurrencySelector} from './CurrencySelector';
import {Spreadsheet} from './Spreadsheet';
import {Database} from '../state/database/Database';
import {Project} from '../state/database/Project';
import {ProjectCategory} from '../state/database/ProjectCategory';
import {TagNode} from '../state/database/TagNode';
import {TableColumn} from '../state/ui/TableColumn';

@Component({
  selector: 'ProjectViewer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: 'app/components/ProjectViewer.html',
  styleUrls: ['app/components/ProjectViewer.css'],
})
export class ProjectViewer {
  @Input() project: Project;
  @Input() database: Database;
  @Input() path: Array<string>;
  @Input() projectUIState: any; // TODO
  @Input() currencyList: any; // TODO

  expenseColumns: any;
  incomeColumns: any;
  categoryColumns: any;

  getProjectCategoryList():
    Immutable.List<{key: string, value: string}> {
    return this.project.categories.map(
      (value: ProjectCategory) =>
        ({key: value.tagName, value: value.tagName})).toList();
  }

  ngOnChanges(chg) {
    this.incomeColumns = Immutable.List([
      new TableColumn({
        key: 'date',
        value: 'Date',
        kind: 'date',
        constraints: ['not_null'],
      }),
      new TableColumn({
        key: 'foreignAmount',
        value: 'Foreign amount',
        kind: 'number',
        constraints: ['not_null', 'positive']
      }),
      new TableColumn({
        key: 'exchangeRate',
        value: 'Exchange rate',
        kind: 'number',
        constraints: ['not_null', 'positive']
      }),
      new TableColumn({
        key: 'localAmount',
        value: 'Amount (' + this.database.localCurrency + ')',
        kind: 'number',
        editable: false
      }),
      new TableColumn({
        key: 'spentForeignAmount',
        value: 'Spent (' + this.project.foreignCurrency + ')',
        kind: 'number',
        editable: false
      }),
      new TableColumn({
        key: 'spentLocalAmount',
        value: 'Spent (' + this.database.localCurrency + ')',
        kind: 'number',
        editable : false
      })
    ]);

    this.categoryColumns = Immutable.List([
      new TableColumn({
        key: 'tagName',
        value: 'Name',
        kind: 'dropdown',
        items: this.database.getExpenseBudgetCategories().getSubTreeAsUIList(),
        editable: false,
        editableAtCreation: true
      }),
      new TableColumn({
        key: 'limitForeign',
        value: 'Limit (' + this.project.foreignCurrency + ')',
        kind: 'number',
        constraints: ['positive']
      }),
      new TableColumn({
        key: 'limitPercentageForeign',
        value: 'Limit% (' + this.project.foreignCurrency + ')',
        kind: 'number',
        constraints: ['min:0', 'max:100']
      }),
      new TableColumn({
        key: 'spentForeign',
        value: 'Spent (' + this.project.foreignCurrency + ')',
        kind: 'number',
        editable: false
      }),
      new TableColumn({
        key: 'spentLocal',
        value: 'Spent (' + this.database.localCurrency + ')',
        kind: 'number',
        editable: false
      }),
    ]);

    this.expenseColumns = Immutable.List([
      new TableColumn({
        key: 'date',
        value: 'Date',
        kind: 'date',
        constraints: ['not_null'],
      }),
      new TableColumn({
        key: 'category',
        value: 'Category',
        kind: 'dropdown',
        items: this.getProjectCategoryList()
      }),
      new TableColumn({
        key: 'accountNo',
        value: 'Account Number',
        kind: 'string'
      }),
      new TableColumn({
        key: 'partner',
        value: 'Partner',
        kind: 'string'
      }),
      new TableColumn({
        key: 'localAmount',
        value: 'Amount (' + this.database.localCurrency + ')',
        kind: 'number',
        constraints: ['positive']
      }),
      new TableColumn({
        key: 'foreignAmount',
        value: 'Amount (' + this.project.foreignCurrency + ')',
        kind: 'number',
        editable: false
      }),
      new TableColumn({
        key: 'exchangeRate',
        value: 'Exchange rate',
        kind: 'number',
        editable: false
      })
    ]);
  }

}
