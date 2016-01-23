///<reference path='../state/database/Project.ts'/>
import {AppState} from '../state/AppState';
import {Project} from '../state/database/Project';

import {ChangeDetectionStrategy, Component, View} from 'angular2/core';
import {NgClass, NgFor, NgIf, NgModel} from 'angular2/common';
import {bootstrap} from 'angular2/platform/browser';
import {JSONParser} from '../state/database/JSONParser';
import {ProjectItemComponent} from './ProjectItem';
import {Spreadsheet} from './Spreadsheet';
import {TagList} from './TagList';
import {StateService} from './StateService';

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
  newProjectName: string;

  incomeColumns: Array<Object>;
  expenseColumns: Array<Object>;
  categoryColumns: Array<Object>;

  stateService: StateService;

  constructor(stateService: StateService) {
    var that = this;

    this.stateService = stateService;

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
    this.stateService.addProject(
      new Project({name: this.newProjectName}));
    this.newProjectName = '';
  }

  selectMenuItem(id: number) {
    this.stateService.setByPath(['mainMenuSelectedItemId'], id);
  }

  isSelectedMenuItem(id: number): boolean {
    return this.stateService.state.mainMenuSelectedItemId === id;
  }

  state(): AppState {
    return this.stateService.state;
  }

  loadFile() {
    var fname = dialog.showOpenDialog();
    console.log(fname[0]);
    var data = fs.readFileSync(fname[0]);
    this.stateService.loadDatabase(JSON.parse(data));
  }

  saveFile() {
    console.log('saveFile');
    var fname = dialog.showSaveDialog();
    console.log('fname= ', fname);
    fs.writeFile(fname, JSON.stringify(this.stateService.state.database.toJSON()));
  }

}
bootstrap(App, [StateService, JSONParser]);

