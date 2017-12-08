import {Injectable} from '@angular/core';
import {List} from 'immutable';

import {AppState} from 'app/state/AppState';
import {BigNumber} from 'app/state/core/BigNumber';
import {Changes} from 'app/state/core/Changes';
import {Database} from 'app/state/database/Database';
import {Expense} from 'app/state/database/Expense';
import {Income} from 'app/state/database/Income';
import {Project} from 'app/state/database/Project';
import {ProjectCategory} from 'app/state/database/ProjectCategory';
import {TagNode} from 'app/state/database/TagNode';
import {JSONParser} from 'app/state/database/JSONParser';
import {GenericTable} from 'app/state/ui/GenericTable';

@Injectable()
export class StateService {
  state: AppState;
  observers: Array<any>;
  jsonParser: JSONParser;

  loadDatabase(jsonData: any) {
    let newState = new AppState()
      .set('database', this.jsonParser.parseDatabase(jsonData))
      .onChange('', {budgetCategoryTreeChange: true});
    this.updateState(newState);
  }

  constructor(jsonParser: JSONParser) {
    this.jsonParser = jsonParser;
    this.state = new AppState({database: new Database()});
  }

  updateState(state: AppState) {
    this.state = state;
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
    return this.state.getIn(this.flattenPath(path));
  }

  addNewItem(table: GenericTable<any>, targetPath: Array<any>) {
    console.log('adding ', table.newItem.toJS());
    let validationErrors = table.newItem.validate();
    if (validationErrors.length > 0) {
      console.log('validation error ', validationErrors);
      window.alert('validation error ' + validationErrors);
      return;
    }

    let newItemPath = table.myPath.concat(['newItem']);
    targetPath = this.flattenPath(targetPath);

    let itemType = targetPath[2];

    let state = this.state
      .setIn(
        newItemPath,
        table.newItemTemplate);
    this.updateByPath<any>(  // any should be Immutable.List
      targetPath,
      list => list.push(table.newItem),
      state);
  }

  removeItem(targetPath: Array<any>, index: number) {
    targetPath = this.flattenPath(targetPath);
    this.updateByPath<any>(  // any should be Immutable.List
      targetPath,
      list => list.remove(index));
  }

  addProject(project: Project) {
    this.updateState(
      this.state.updateIn(['database', 'projects'], projects => projects.push(project)));
  }

  updateByPath<T>(path: Array<string>, updater: (object: T) => T, state?: AppState) {
    function recursiveUpdate(object, path: Array<string>, updater, changes: Changes) {
      let pathHead = path[0];
      let pathTail = path.slice(1);
      if (pathTail.length === 0) {
        let updatedProperty = updater(object.get(pathHead));
        object = object.set(pathHead, updatedProperty);
        if ('onChange' in object) {
          object = object.onChange(pathHead, changes);
        }
        return object;
      } else {
        let subObject = object.get(pathHead);
        subObject = recursiveUpdate(subObject, pathTail, updater, changes);
        object = object.set(pathHead, subObject);
        if ('onChange' in object) {
          object = object.onChange(pathHead, changes);
        }
        return object;
      }
    }

    state = state || this.state;
    let changes: Changes = new Changes();
    state = recursiveUpdate(state, path, updater, changes);
    this.updateState(state);
  }

  setByPath(path: Array<any>, value: any) {
    this.updateByPath(this.flattenPath(path), (_1) => value);
  }

  setProjectName(projectPath: Array<any>, name: string) {
    this.updateState(
      this.state.setIn(
        this.flattenPath([projectPath, 'name']),
        name));
  }

  setTagName(tagPath: Array<any>, newName: string) {
    let namePath = this.flattenPath([tagPath, 'name']);
    let oldName = this.state.getIn(namePath);
    // TODO: make this triggered by the name change?
    let state = this.state.updateIn(
      ['database'],
      db => db.renameTag(oldName, newName));
    this.updateByPath(namePath, _ => newName, state);
  }

  addSubTag(parentTagPath: Array<any>) {
    this.updateByPath<List<TagNode>>(
      this.flattenPath([parentTagPath, 'subTags']),
      list => list.push(new TagNode({name: 'new node'})));
  }

}
