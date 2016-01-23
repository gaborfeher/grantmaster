///<reference path='../state/database/TagNode.ts'/>

import {ChangeDetectionStrategy, Component, Input, View} from 'angular2/core';
import {NgFor, NgIf} from 'angular2/common';
import {TagNode} from '../state/database/TagNode';
import {StateService} from './StateService';

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

  stateService: StateService;

  constructor(stateService: StateService) {
    this.editing = false;
    this.stateService = stateService;
  }

  startEdit() {
    this.editing = true;
    this.editedName = this.node.name;
  }

  commitEdit() {
    this.editing = false;
    this.stateService.setTagName(this.path, this.editedName);
  }

  add() {
    this.stateService.addSubTag(this.path);
  }

}

