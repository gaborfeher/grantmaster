///<reference path='../data/Database.ts'/>
///<reference path='../data/Expense.ts'/>
///<reference path='../data/Income.ts'/>
///<reference path='../data/Project.ts'/>
///<reference path='../data/ProjectCategory.ts'/>
///<reference path='../data/TagNode.ts'/>
import {AppState} from '../data/AppState';
import {Expense} from '../data/Expense';
import {Income} from '../data/Income';
import {Project} from '../data/Project';
import {ProjectCategory} from '../data/ProjectCategory';
import {TagNode} from '../data/TagNode';

import {ChangeDetectionStrategy, Component, View} from 'angular2/core';
import {NgClass, NgFor, NgIf, NgModel} from 'angular2/common';
import {bootstrap} from 'angular2/platform/browser';
import {JSONParser} from '../data/JSONParser';
import {ProjectItemComponent} from './ProjectItem';
import {Spreadsheet} from './Spreadsheet';
import {TagList} from './TagList';
import {DataService} from './DataService';

var BigNumber = require('../../../node_modules/bignumber.js/bignumber.js');

var w: any = window;
var fs = w.require('fs');
var remote = w.require('remote');
var dialog = remote.require('dialog');

@Component({
  selector: 'App',
  changeDetection: ChangeDetectionStrategy.OnPush
})
@View({
  templateUrl: './app/components/App.html',
  styleUrls: ['./app/components/App.css'],
  directives: [NgClass, NgFor, NgIf, NgModel, Spreadsheet, ProjectItemComponent, TagList],
})
class App {
  newExpenseTemplate: Expense;
  newIncomeTemplate: Income;
  newCategoryTemplate: ProjectCategory;
  newProjectName: string;

  incomeColumns: Array<Object>;
  expenseColumns: Array<Object>;
  categoryColumns: Array<Object>;

  dataService: DataService;

  constructor(dataService: DataService) {
    var that = this;

    this.dataService = dataService;

    this.incomeColumns = [
      {
        key: 'date',
        value: 'Date',
        kind: 'date',
        constraints: ['not_null'],
      },
      {
        key: 'foreignAmount',
        value: 'Foreign amount',
        kind: 'number',
        constraints: ['not_null', 'positive']
      },
      {
        key: 'exchangeRate',
        value: 'Exchange rate',
        kind: 'number',
        constraints: ['not_null', 'positive']
      },
      {key: 'localAmount', value: 'Local amount', kind: 'number', editable: false},
      {key: 'spentForeignAmount', value: 'Spent (F)', kind: 'number', editable: false},
      {key: 'spentLocalAmount', value: 'Spent (L)', kind: 'number', editable : false}];
  }

  addProject() {
    this.dataService.addProject(
      new Project({name: this.newProjectName}));
    this.newProjectName = '';
  }

  selectMenuItem(id: number) {
    this.dataService.setByPath(['mainMenuSelectedItemId'], id);
  }

  isSelectedMenuItem(id: number): boolean {
    return this.dataService.state.mainMenuSelectedItemId === id;
  }

  state(): AppState {
    return this.dataService.state;
  }

  loadFile() {
    var fname = dialog.showOpenDialog();
    console.log(fname[0]);
    var data = fs.readFileSync(fname[0]);
    this.dataService.loadDatabase(JSON.parse(data));
  }

  saveFile() {
    console.log('saveFile');
    var fname = dialog.showSaveDialog();
    console.log('fname= ', fname);
    fs.writeFile(fname, JSON.stringify(this.dataService.state.database.toJSON()));
  }

}
bootstrap(App, [DataService, JSONParser]);

