import {BigNumber} from 'bignumber.js';

import { NgModule, Injectable } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import {ChangeDetectionStrategy, Component} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';

import {CurrencySelector} from 'app/components/CurrencySelector';
import {FormattedAmount} from 'app/components/FormattedAmount';
import {ProjectItemComponent} from 'app/components/ProjectItem';
import {TagList} from 'app/components/TagList';
import {StateService} from 'app/components/StateService';
import {ExampleData1, ExampleData2} from 'app/components/ExampleData';
import {ProjectViewer} from 'app/components/ProjectViewer';
import {Spreadsheet} from 'app/components/Spreadsheet';
import {BudgetCategorySelector} from 'app/components/BudgetCategorySelector';
import {CellEntry} from 'app/components/CellEntry';
import {TagName} from 'app/components/TagName';

import {AppState} from 'app/state/AppState';
import {Project} from 'app/state/database/Project';
import {JSONParser} from 'app/state/database/JSONParser';

declare var System;
let fs = System._nodeRequire('fs');
let dialog = System._nodeRequire('electron').remote.dialog;


@Component({
  selector: 'App',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './app/components/App.html',
  styleUrls: ['./app/components/App.css'],
})
export class AppComponent {
  newProjectName: string;

  incomeColumns: Array<Object>;
  expenseColumns: Array<Object>;
  categoryColumns: Array<Object>;

  stateService: StateService;

  constructor(stateService: StateService) {
    this.stateService = stateService;
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

  loadExample1() {
    this.stateService.loadDatabase(ExampleData1());
  }

  loadExample2() {
    this.stateService.loadDatabase(ExampleData2());
  }

  saveFile() {
    console.log('saveFile');
    var fname = dialog.showSaveDialog();
    console.log('fname= ', fname);
    fs.writeFile(fname, JSON.stringify(this.stateService.state.database.toJSON()));
  }

}

@NgModule({
  providers: [
    JSONParser,
    StateService,
  ],
  declarations: [
    AppComponent,
    CellEntry,
    CurrencySelector,
    BudgetCategorySelector,
    FormattedAmount,
    ProjectItemComponent,
    ProjectViewer,
    Spreadsheet,
    TagList,
    TagName,
  ],
  bootstrap: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
  ],
})
export class App {}

BigNumber.config({
  DECIMAL_PLACES: 30,
  ROUNDING_MODE: BigNumber.ROUND_HALF_EVEN,
  FORMAT: {
    groupSeparator: '',
    decimalSeparator: '.'
  }
});
platformBrowserDynamic().bootstrapModule(App);

