///<reference path='../data/Model.ts'/>

import {ChangeDetectionStrategy, Component, Input, View} from 'angular2/core';
import {NgFor, NgIf} from 'angular2/common';
import {TagNode} from '../data/Model';
import {DataService} from './DataService';

@Component({
  selector: 'TagName',
  properties: [
    'path',
    'node',
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
@View({
  templateUrl: './app/components/TagName.html',
  styleUrls: ['./app/components/TagName.css'],
  directives: [NgFor, NgIf]
})
export class TagName {
  @Input() node: TagNode;
  @Input() path: Array<string>;

  editing: boolean;
  editedName: string;

  dataService: DataService;

  constructor(dataService: DataService) {
    this.editing = false;
    this.dataService = dataService;
  }

  startEdit() {
    this.editing = true;
    this.editedName = this.node.name;
  }

  commitEdit() {
    this.editing = false;
    this.dataService.setTagName(this.path, this.editedName);
  }

  add() {
    this.dataService.addSubTag(this.path);
  }

}

