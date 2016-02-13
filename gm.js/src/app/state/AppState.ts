///<reference path='../../../node_modules/immutable/dist/immutable.d.ts'/>
///<reference path='./database/Database.ts'/>
///<reference path='./database/ProjectCategory.ts'/>
///<reference path='./database/Expense.ts'/>
///<reference path='./database/Income.ts'/>
///<reference path='./ui/GenericTable.ts'/>
///<reference path='./ui/TagTreeTable.ts'/>
///<reference path='./ui/TableColumn.ts'/>
///<reference path='./core/Changes.ts'/>

var Immutable = require('../../../node_modules/immutable/dist/immutable.js');

import {BigNumber} from './core/BigNumber';
import {Changes} from './core/Changes';
import {Currency} from './database/Currency';
import {Database} from './database/Database';
import {Expense} from './database/Expense';
import {Income} from './database/Income';
import {IRecord} from './core/IRecord';
import {Project} from './database/Project';
import {ProjectCategory} from './database/ProjectCategory';
import {TableColumn} from './ui/TableColumn';
import {TagNode} from './database/TagNode';
import {GenericTable} from './ui/GenericTable';
import {TagTreeTable} from './ui/TagTreeTable';

export interface AppState extends IRecord<AppState> {
  database: Database;
  budgetCategoryTable: TagTreeTable;

  expenseTable: GenericTable<Expense>;
  incomeTable: GenericTable<Income>;
  categoryTable: GenericTable<ProjectCategory>;
  currencyTable: GenericTable<Currency>;

  mainMenuSelectedItemId: number;

  updateProjectTableColumns(): AppState;
  updateBudgetCategories(): AppState;
  resetNewItems(): AppState;
  onChange(): AppState;
  getSelectedProjectId(): number;
  getSelectedProject(): Project;
}
export var AppState = Immutable.Record({
  database: new Database(),
  budgetCategoryTable: new TagTreeTable(),

  mainMenuSelectedItemId: -3,

  expenseTable: new GenericTable({
    myPath: ['expenseTable'],
    newItemTemplate: new Expense()
  }),
  incomeTable: new GenericTable({
    myPath: ['incomeTable'],
    newItemTemplate: new Income()
  }),
  categoryTable: new GenericTable({
    myPath: ['categoryTable'],
    newItemTemplate: new ProjectCategory()
  }),
  currencyTable: new GenericTable({
    myPath: ['currencyTable'],
    newItemTemplate: new Currency(),
    columns: Immutable.List([
      new TableColumn({
        key: 'name',
        value: 'Name',
        kind: 'string'
      })
    ])
  })
});
AppState.prototype.getSelectedProjectId = function(): number {
  let that: AppState = this;
  if (that.mainMenuSelectedItemId >= 0) {
    return that.mainMenuSelectedItemId;
  } else {
    return undefined;
  }
}
AppState.prototype.getSelectedProject = function(): Project {
  let that: AppState = this;
  if (that.getSelectedProjectId() !== undefined) {
    return that.database.projects.get(that.getSelectedProjectId());
  } else {
    return undefined;
  }
}
AppState.prototype.updateProjectTableColumns = function(): AppState {
  let that: AppState = this;
  let project = that.getSelectedProject();
  if (project === undefined) {
    return that;
  }

  function getExpenseCategoryList() {
    function toNames(prefix: string) {
      return function(node: TagNode) {
        let x = {key: node.name, value: prefix + node.name};
        return Immutable.List([x]).concat(
          node.subTags.flatMap(node => toNames(prefix + '  ')(node)));
      }
    }
    // the plan is that expenses will always be at index 0, and incomes at index 1
    let expenseCategories = that.database.budgetCategories.subTags.get(0);
    return toNames('')(expenseCategories);
  }
  let categoryColumns = Immutable.List([
    new TableColumn({
      key: 'tagName',
      value: 'Name',
      kind: 'dropdown',
      items: getExpenseCategoryList(),
      editable: false,
      editableAtCreation: true
    }),
    new TableColumn({
      key: 'limitForeign',
      value: 'Limit (' + project.foreignCurrency + ')',
      kind: 'number',
      constraints: ['positive']
    }),
    new TableColumn({
      key: 'limitPercentageForeign',
      value: 'Limit% (' + project.foreignCurrency + ')',
      kind: 'number',
      constraints: ['min:0', 'max:100']
    }),
    new TableColumn({
      key: 'spentForeign',
      value: 'Spent (' + project.foreignCurrency + ')',
      kind: 'number',
      editable: false
    }),
    new TableColumn({
      key: 'spentLocal',
      value: 'Spent (' + that.database.localCurrency + ')',
      kind: 'number',
      editable: false
    }),
  ]);

  function getProjectCategoryList():
    Immutable.List<{key: string, value: string}> {
    return project.categories.map(
      (value: ProjectCategory) =>
        ({key: value.tagName, value: value.tagName})).toList();
  }
  let expenseColumns = Immutable.List();
  if (that.mainMenuSelectedItemId >= 0) {
    expenseColumns = Immutable.List([
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
        items: getProjectCategoryList()
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
        value: 'Amount (' + project.foreignCurrency + ')',
        kind: 'number',
        constraints: ['positive']
      }),
      new TableColumn({
        key: 'foreignAmount',
        value: 'Amount (' + project.foreignCurrency + ')',
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

  let incomeColumns = Immutable.List([
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
      value: 'Amount (' + that.database.localCurrency + ')',
      kind: 'number',
      editable: false
    }),
    new TableColumn({
      key: 'spentForeignAmount',
      value: 'Spent (' + project.foreignCurrency + ')',
      kind: 'number',
      editable: false
    }),
    new TableColumn({
      key: 'spentLocalAmount',
      value: 'Spent (' + that.database.localCurrency + ')',
      kind: 'number',
      editable : false
    })
  ]);

  return that
    .setIn(['incomeTable', 'columns'], incomeColumns)
    .setIn(['categoryTable', 'columns'], categoryColumns)
    .setIn(['expenseTable', 'columns'], expenseColumns);
}
AppState.prototype.resetNewItems = function(): AppState {
  let that: AppState = this;
  return that
    .updateIn(['currencyTable'], table => table.resetNewItem())
    .updateIn(['incomeTable'], table => table.resetNewItem())
    .updateIn(['categoryTable'], table => table.resetNewItem())
    .updateIn(['expenseTable'], table => table.resetNewItem());
}


function mapBudgetCategories(node: TagNode, map: Object): TagNode {
  node = node.set('subTags', node.subTags.map(subTag => mapBudgetCategories(subTag, map)));
  let items = {};
  if (node.name in map) {
    let localItems = map[node.name];
    for (var key in localItems) {
      if (localItems.hasOwnProperty(key)) {
        let year = key.split(':')[1];
        items[year] = localItems[key];
      }
    }
  }

  node.subTags.forEach(
    subTag => {
      subTag.summaries.forEach(
        (summaryItem, year) => {
          items[year] = items[year] || new BigNumber(0);
          items[year] = items[year].plus(summaryItem);
          return true;
        }
      );
      return true;
    });


  let orderedItems = Immutable.OrderedMap();
  for (var year in items) {
    if (items.hasOwnProperty(year)) {
      orderedItems = orderedItems.set(year, items[year]);
    }
  }
  node = node.set('summaries', orderedItems);
  return node;
}
AppState.prototype.updateBudgetCategories = function() {
  let that: AppState = this;

  var map = {};  // map[category name][year] = sum
  that.database.projects.forEach(
    project => {
      project.expenses.forEach(
        expense => {
          let year = expense.date.split('-')[0];
          let amount = expense.localAmount;
          let category = expense.category;
          let key = category + ':' + year;
          map[category] = map[category] || {};
          let baseAmount = map[category][key] || new BigNumber(0);
          map[category][key] = baseAmount.plus(amount);
          return true;
        });
      return true;
    });

  let budgetCategoriesWithSums =
    mapBudgetCategories(that.database.budgetCategories, map);
  return that.set(
    'budgetCategoryTable',
    that.budgetCategoryTable.refresh(
      budgetCategoriesWithSums,
      ['database', 'budgetCategories']))
}
AppState.prototype.onChange = function(property: string, changes: Changes): AppState {
  let that: AppState = this;
  if (property === 'mainMenuSelectedItemId') {
    that = that.resetNewItems();
  }
  if (changes.projectProperty === 'expenses'
      || changes.budgetCategoryTreeChange) {
    return that
      .updateBudgetCategories()
      .updateProjectTableColumns();
  } else if (property === 'mainMenuSelectedItemId' ||
    changes.projectProperty === 'categories' ||
    changes.projectProperty === 'foreignCurrency') {

    return that.updateProjectTableColumns();
  } else {
    return that;
  }
}
