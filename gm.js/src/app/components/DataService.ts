///<reference path='../data/Changes.ts'/>
///<reference path='../data/Database.ts'/>
///<reference path='../data/Expense.ts'/>
///<reference path='../data/Income.ts'/>
///<reference path='../data/Project.ts'/>
///<reference path='../data/ProjectCategory.ts'/>
///<reference path='../data/TagNode.ts'/>
import {Changes} from '../data/Changes';
import {Database} from '../data/Database';
import {Expense} from '../data/Expense';
import {Income} from '../data/Income';
import {Project} from '../data/Project';
import {ProjectCategory} from '../data/ProjectCategory';
import {TagNode} from '../data/TagNode';

import {Injectable} from 'angular2/core';
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
      .addProject(new Project({
        name: 'aaa',
        categories: Immutable.List([
          new ProjectCategory({tagName: 'cat1'}),
          new ProjectCategory({tagName: 'cat2'})]),
        incomes: Immutable.List([
          new Income({
            date: '2015-01-01',
            foreignAmount: new BigNumber(1000),
            exchangeRate: new BigNumber(305)})]),
        expenses: Immutable.List([
          new Expense({
            date: '2015-02-02',
            localAmount: new BigNumber(1480),
            category: 'cat1'}),
          new Expense({
            date: '2015-02-05',
            localAmount: new BigNumber(990),
            category: 'cat2'})])
        }).recomputeIncomes())
      .addProject(new Project({name: 'bbb'}))
      .recomputeBudgetCategories();
    let monsterMode = false;
    if (monsterMode) {
      console.log('monster mode');
      for (var i = 1; i < 1000; ++i) {
        console.log('adding test ', i);
        this.database = this.database
          .updateIn(
            ['projects', 0],
            project => project.set(
              'expenses',
              project.expenses.push(new Expense({
                date: '2015-01-' + (i).toString(),
                localAmount: new BigNumber(i + 1),
                category: 'cat' + (i % 2 + 1).toString()
            }))).recomputeExpenses())
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

    let database = this.database
      .setIn(
        newItemPath,
        this.database.getIn(newItemTemplatePath));
    this.updateByPath<any>(  // any should be Immutable.List
      targetPath,
      list => list.push(newItem),
      database);
  }

  addProject(project: Project) {
    this.updateDatabase(
      this.database.set('projects', this.database.projects.push(project)));
  }

  updateByPath<T>(path: Array<string>, updater: (object: T) => T, database?: Database) {
    function recursiveUpdate(object, path: Array<string>, updater, changes: Changes) {
      let pathHead = path[0];
      let pathTail = path.slice(1);
      if (pathTail.length === 0) {
        let updatedProperty = updater(object.get(pathHead));
        object = object.set(pathHead, updatedProperty);
        if ('onPropertyChange' in object) {
          object = object.onPropertyChange(pathHead, changes);
          if ('onChange' in object) {
            object = object.onChange(changes);
          }
        }
        return object;
      } else {
        let subObject = object.get(pathHead);
        subObject = recursiveUpdate(subObject, pathTail, updater, changes);
        object = object.set(pathHead, subObject);
        if ('onChange' in object) {
          object = object.onChange(changes);
        }
        return object;
      }
    }

    database = database || this.database;
    let changes: Changes = new Changes();
    database = recursiveUpdate(database, path, updater, changes);
    this.updateDatabase(database);
  }

  setByPath(path: Array<any>, value: any) {
    this.updateByPath(this.flattenPath(path), (_1) => value);
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
