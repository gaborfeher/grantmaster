import {List, is} from 'immutable';

import {Component, Input, ChangeDetectionStrategy, SimpleChange} from '@angular/core';
import {BudgetCategorySelector} from 'app/components/BudgetCategorySelector';
import {CurrencySelector} from 'app/components/CurrencySelector';
import {Spreadsheet} from 'app/components/Spreadsheet';
import {Database} from 'app/state/database/Database';
import {Project} from 'app/state/database/Project';
import {ProjectCategory} from 'app/state/database/ProjectCategory';
import {TagNode} from 'app/state/database/TagNode';
import {TableColumn} from 'app/state/ui/TableColumn';

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

  expenseColumns: List<TableColumn>;
  incomeColumns: List<TableColumn>;
  categoryColumns: List<TableColumn>;

  getProjectCategoryList(): List<{key: string, value: string}> {
    return this.project.categories.map(
      (value: ProjectCategory) =>
        ({key: value.tagName, value: value.tagName})).toList();
  }


  ngOnChanges(chg: SimpleChange) {
    // Rebuild column lists because they depend on this component's
    // inputs.
    this.makeIncomeColumns();
    this.makeExpenseColumns();
    this.makeCategoryColumns();
  }

  makeIncomeColumns() {
    this.incomeColumns = List([
      new TableColumn({
        key: 'date',
        value: 'Date',
        kind: 'date',
        constraints: List(['not_null']),
      }),
      new TableColumn({
        key: 'foreignAmount',
        value: 'Foreign amount',
        kind: 'number',
        constraints: List(['not_null', 'positive'])
      }),
      new TableColumn({
        key: 'exchangeRate',
        value: 'Exchange rate',
        kind: 'number',
        constraints: List(['not_null', 'positive'])
      }),
      new TableColumn({
        key: 'localAmount',
        value: 'Amount (' + this.database.localCurrency + ')',
        kind: 'number',
        editable: false,
        editableAtCreation: false,
      }),
      new TableColumn({
        key: 'spentForeignAmount',
        value: 'Spent (' + this.project.foreignCurrency + ')',
        kind: 'number',
        editable: false,
        editableAtCreation: false,
      }),
      new TableColumn({
        key: 'spentLocalAmount',
        value: 'Spent (' + this.database.localCurrency + ')',
        kind: 'number',
        editable : false,
        editableAtCreation: false,
      })
    ]);
  }

  makeCategoryColumns() {
    this.categoryColumns = List([
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
        constraints: List(['positive'])
      }),
      new TableColumn({
        key: 'limitPercentageForeign',
        value: 'Limit% (' + this.project.foreignCurrency + ')',
        kind: 'number',
        constraints: List(['min:0', 'max:100'])
      }),
      new TableColumn({
        key: 'spentForeign',
        value: 'Spent (' + this.project.foreignCurrency + ')',
        kind: 'number',
        editable: false,
        editableAtCreation: false,
      }),
      new TableColumn({
        key: 'spentLocal',
        value: 'Spent (' + this.database.localCurrency + ')',
        kind: 'number',
        editable: false,
        editableAtCreation: false,
      }),
    ]);
  }

  makeExpenseColumns() {
    this.expenseColumns = List([
      new TableColumn({
        key: 'date',
        value: 'Date',
        kind: 'date',
        constraints: List(['not_null']),
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
        constraints: List(['not_null', 'positive'])
      }),
      new TableColumn({
        key: 'foreignAmount',
        value: 'Amount (' + this.project.foreignCurrency + ')',
        kind: 'number',
        editable: false,
        editableAtCreation: false,
      }),
      new TableColumn({
        key: 'exchangeRate',
        value: 'Exchange rate',
        kind: 'number',
        editable: false,
        editableAtCreation: false,
      })
    ]);
  }

}
