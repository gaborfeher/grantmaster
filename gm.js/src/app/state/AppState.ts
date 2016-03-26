///<reference path='../../../node_modules/immutable/dist/immutable.d.ts'/>
///<reference path='./database/Database.ts'/>
///<reference path='./database/ProjectCategory.ts'/>
///<reference path='./database/Expense.ts'/>
///<reference path='./database/Income.ts'/>
///<reference path='./ui/GenericTable.ts'/>
///<reference path='./ui/TagTreeTable.ts'/>
///<reference path='./ui/TableColumn.ts'/>
///<reference path='./core/Changes.ts'/>


import {BigNumber} from './core/BigNumber';
import {Changes} from './core/Changes';
import {Currency} from './database/Currency';
import {Database} from './database/Database';
import {Expense} from './database/Expense';
import {Income} from './database/Income';
import {Immutable, IRecord} from './core/IRecord';
import {Project} from './database/Project';
import {ProjectCategory} from './database/ProjectCategory';
import {TableColumn} from './ui/TableColumn';
import {TagNode} from './database/TagNode';
import {GenericTable} from './ui/GenericTable';
import {TagTreeTable} from './ui/TagTreeTable';

export interface AppState extends IRecord<AppState> {
  database: Database;
  budgetCategoryTable: TagTreeTable;
  currencyTable: GenericTable<Currency>;
  projectUIState: Immutable.Record.Class;

  mainMenuSelectedItemId: number;

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

  // TODO: move this into own class
  projectUIState: new (Immutable.Record({
    expenseTable: new GenericTable({
      myPath: ['projectUIState', 'expenseTable'],
      newItemTemplate: new Expense()
    }),
    incomeTable: new GenericTable({
      myPath: ['projectUIState', 'incomeTable'],
      newItemTemplate: new Income()
    }),
    categoryTable: new GenericTable({
      myPath: ['projectUIState', 'categoryTable'],
      newItemTemplate: new ProjectCategory()
    }),
  })),

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
AppState.prototype.resetNewItems = function(): AppState {
  let that: AppState = this;
  return that
    .updateIn(['currencyTable'], table => table.resetNewItem())
    .updateIn(['projectUIState', 'incomeTable'], table => table.resetNewItem())
    .updateIn(['projectUIState', 'categoryTable'], table => table.resetNewItem())
    .updateIn(['projectUIState', 'expenseTable'], table => table.resetNewItem());
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
    return that.updateBudgetCategories();
  } else {
    return that;
  }
}
