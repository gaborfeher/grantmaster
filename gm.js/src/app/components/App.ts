///<reference path='../state/database/Project.ts'/>

import { NgModule, Injectable } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';


import {ChangeDetectionStrategy, Component} from '@angular/core';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import {NgModel} from '@angular/forms';
import {FormsModule} from '@angular/forms';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';

import {CurrencySelector} from './CurrencySelector';
import {ProjectItemComponent} from './ProjectItem';
import {TagList} from './TagList';
import {StateService} from './StateService';
import {ExampleData1, ExampleData2} from './ExampleData';
import {ProjectViewer} from './ProjectViewer';
import {Spreadsheet} from './Spreadsheet';
import {BudgetCategorySelector} from './BudgetCategorySelector';
import {CellEntry} from './CellEntry';
import {TagName} from './TagName';

import {AppState} from '../state/AppState';
import {Project} from '../state/database/Project';
import {JSONParser} from '../state/database/JSONParser';

var w: any = window;
var fs = w.require('fs');
var remote = w.require('remote');
var dialog = remote.require('dialog');


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
    BudgetCategorySelector,
    CurrencySelector,
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


platformBrowserDynamic().bootstrapModule(App);

