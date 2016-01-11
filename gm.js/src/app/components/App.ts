///<reference path='../data/Database.ts'/>
///<reference path='../data/Expense.ts'/>
///<reference path='../data/Income.ts'/>
///<reference path='../data/Project.ts'/>
///<reference path='../data/ProjectCategory.ts'/>
///<reference path='../data/TagNode.ts'/>
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
  selectedMenuItemId: number;

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
    this.dataService.subscribe(function() { that.refreshCalculations(); });

    this.selectedMenuItemId = -2;

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

    this.refreshCalculations();
  }

  refreshCalculations() {
    var that = this;

    function getCategoryList() {
      function toNames(prefix: string) {
        return function(node: TagNode) {
          var x = {key: node.name, value: prefix + node.name};
          return Immutable.List([x]).concat(
            node.subTags.flatMap(node => toNames(prefix + '  ')(node)));
        }
      }
      return toNames('')(that.dataService.database.budgetCategories);
    }

    function getProjectCategoryList():
      Immutable.List<{key: string, value: string}> {

      let project = that.selectedProject();
      if (project === undefined) {
        return Immutable.List([]);
      }
      return project.categories.map(
        (value: ProjectCategory) =>
          ({key: value.tagName, value: value.tagName})).toList();
    }

    this.categoryColumns = [
      {
        key: 'tagName',
        value: 'Name',
        kind: 'dropdown',
        items: getCategoryList()
      },
      {
        key: 'limitForeign',
        value: 'Limit (F)',
        kind: 'number',
        constraints: ['positive']
      },
      {
        key: 'limitPercentageForeign',
        value: 'Limit% (F)',
        kind: 'number',
        constraints: ['min:0', 'max:100']
      },
      {key: 'spentForeign', value: 'Spent (F)', kind: 'number', editable: false},
      {key: 'spentLocal', value: 'Spent (L)', kind: 'number', editable: false},
    ];

    this.expenseColumns = [
      {
        key: 'date',
        value: 'Date',
        kind: 'date',
        constraints: ['not_null'],
      },
      {key: 'category', value: 'Category', kind: 'dropdown', items: getProjectCategoryList()},
      {key: 'accountNo', value: 'Account Number', kind: 'string'},
      {key: 'partner', value: 'Partner', kind: 'string'},
      {
        key: 'localAmount',
        value: 'Local amount',
        kind: 'number',
        constraints: ['positive']
      },
      {key: 'foreignAmount', value: 'Foreign amount', kind: 'number', editable: false},
      {key: 'exchangeRate', value: 'Exchange rate', kind: 'number', editable: false}];
  }

  addProject() {
    this.dataService.addProject(
      new Project({name: this.newProjectName}));
    this.newProjectName = '';
  }

  selectMenuItem(id: number) {
    this.selectedMenuItemId = id;
    this.refreshCalculations();
  }

  isSelectedMenuItem(id: number) {
    return this.selectedMenuItemId === id;
  }

  selectedProjectId(): number {
    return this.selectedMenuItemId >= 0 ? this.selectedMenuItemId : undefined;
  }

  selectedProject(): Project {
    return this.selectedMenuItemId >= 0 ? this.dataService.database.projects.get(this.selectedMenuItemId) : undefined;
  }

  onDataChange(dummy: number) {
    console.log('app changes: ', dummy);
    this.refreshCalculations();
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
    fs.writeFile(fname, JSON.stringify(this.dataService.database.toJSON()));
  }

}
bootstrap(App, [DataService, JSONParser]);

