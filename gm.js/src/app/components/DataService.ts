///<reference path='../data/Model.ts'/>

import {Injectable} from 'angular2/core';
import {Database, Expense, Income, Project, ProjectCategory, TagNode} from '../data/Model';
import {JSONParser} from '../data/JSONParser';

var BigNumber = require('../../../node_modules/bignumber.js/bignumber.js');

@Injectable()
export class DataService {
  database: Database;
  observers: Array<any>;
  jsonParser: JSONParser;

  loadDatabase(jsonData: any) {
    this.updateDatabase(this.jsonParser.parseDatabase(jsonData));
  }

  constructor(jsonParser: JSONParser) {
    this.jsonParser = jsonParser;
    this.observers = [];
    this.database = new Database()
      .setIn(
        ['budgetCategories', 'subTags'],
        Immutable.List([new TagNode({name: 'cat1'}), new TagNode({name: 'cat2'})]))
      .addProject(
        (new Project({name: 'aaa'}))
          .set(
            'categories',
            Immutable.List([
              new ProjectCategory({tagName: 'cat1'}),
              new ProjectCategory({tagName: 'cat2'})]))
          .addIncome(new Income({
            date: '2015-01-01',
            foreignAmount: new BigNumber(1000),
            exchangeRate: new BigNumber(305)}))
          .addExpense(new Expense({
            date: '2015-02-02',
            localAmount: new BigNumber(1480),
            category: 'cat1'}))
          .addExpense(new Expense({
            date: '2015-02-05',
            localAmount: new BigNumber(990),
            category: 'cat2'})))
      .addProject(new Project({name: 'bbb'}))
      .recomputeBudgetCategories();
    let monsterMode = false;
    if (monsterMode) {
      for (var i = 1; i < 1000; ++i) {
        console.log('adding test ', i);
        this.database = this.database
          .updateIn(
            ['projects', 0],
            project => project.addExpense(new Expense({
              date: 'monster' + (i).toString(),
              localAmount: new BigNumber(i + 1),
              category: 'cat' + (i % 2 + 1).toString()
            })))
          .recomputeBudgetCategories();
      }
    }
  }

  updateDatabase(database: Database) {
    this.database = database;
    this.broadcast();
  }

  flattenPath(path: Array<any>): Array<string> {
    let flatPath = path;
    // first element can be array
    while (flatPath[0].constructor === Array) {
      flatPath = flatPath[0].concat(flatPath.slice(1));
    }
    return flatPath;
  }

  getIn(path: Array<any>): any {
    return this.database.getIn(this.flattenPath(path));
  }

  addNewItem(newItemPath: Array<any>, targetPath: Array<any>) {
    newItemPath = this.flattenPath(newItemPath);
    targetPath = this.flattenPath(targetPath);

    let newItemTemplatePath = [];
    for (var i = 0; i < newItemPath.length; ++i) {
      newItemTemplatePath.push(newItemPath[i]);
    }
    newItemTemplatePath[newItemTemplatePath.length - 1] =
      newItemTemplatePath[newItemTemplatePath.length - 1] + 'Template';
    let newItem = this.database.getIn(newItemPath);
    let itemType = targetPath[2];

    let database2 = this.database
      .setIn(
        newItemPath,
        this.database.getIn(newItemTemplatePath))

    let projectPath = targetPath.slice(0, 2);
    if (itemType === 'expenses') {
      database2 = database2
        .updateIn(
          projectPath,
          project => project.addExpense(newItem))
        .recomputeBudgetCategories();
    } else if (itemType === 'incomes') {
      database2 = database2.updateIn(
        projectPath,
        project => project.addIncome(newItem));
    } else if (itemType === 'categories') {
      database2 = database2.updateIn(
        targetPath.slice(0, 3),
        categories => categories.push(newItem));
    } else {
      console.error('bad itemType');
    }
    this.updateDatabase(database2);
  }

  addProject(project: Project) {
    this.updateDatabase(
      this.database.set('projects', this.database.projects.push(project)));
  }

  onGlobalChange(msg) {

    let path = this.flattenPath(msg.path);

    let projectPath = path.slice(0, 2);
    let itemType = path[2];

    let itemIndex = path[3];
    let propertyName = path[4];
    if (itemType === 'expenses') {
      if (propertyName === 'date' || propertyName === 'localAmount') {
        this.updateDatabase(this.database
          .updateIn(
            projectPath,
            project => project.updateExpenseAndPropagate(
              itemIndex,
              project.expenses.get(itemIndex).set(propertyName, msg.value)))
          .recomputeBudgetCategories());
        return;
      } else if (propertyName === 'category') {
        this.updateDatabase(this.database
          .setIn(path, msg.value)
          .updateIn(
            projectPath,
            project => project.recomputeBudgetCategories())
          .recomputeBudgetCategories());
        return;
      }
    } else if (itemType === 'incomes') {
      if (propertyName === 'exchangeRate' || propertyName === 'foreignAmount' || propertyName === 'date') {
        this.updateDatabase(this.database.updateIn(
          projectPath,
          project => project.updateIncomeAndPropagate(
            itemIndex,
            project.incomes.get(itemIndex).set(propertyName, msg.value))));
        return;
      }
    }

    this.updateDatabase(this.database.setIn(path, msg.value));
  }

  setProjectName(projectPath: Array<any>, name: string) {
    this.updateDatabase(
      this.database.setIn(
        this.flattenPath([projectPath, 'name']),
        name));
  }

  setTagName(tagPath: Array<any>, newName: string) {
    let namePath = this.flattenPath([tagPath, 'name']);
    let oldName = this.database.getIn(namePath);
    this.updateDatabase(
      this.database
        .setIn(namePath, newName)
        .renameTag(oldName, newName));
  }

  addSubTag(parentTagPath: Array<any>) {
    this.updateDatabase(
      this.database.updateIn(
        this.flattenPath([parentTagPath, 'subTags']),
        list => list.push(new TagNode({name: 'new node'}))));
  }

  subscribe(fun) {
    this.observers.push(fun);
  }

  broadcast() {
    for (var i = 0; i < this.observers.length; ++i) {
      this.observers[i]();
    }
  }

}
