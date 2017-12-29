import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {TagNode} from 'app/state/database/TagNode';
import {StateService} from 'app/components/StateService';

@Component({
  selector: 'TagName',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './app/components/TagName.html',
  styleUrls: ['./app/components/TagName.css'],
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

  saveEdit() {
    this.editing = false;
    this.stateService.setTagName(this.path, this.editedName);
  }

  cancelEdit() {
    this.editing = false;
  }

  add() {
    this.stateService.addSubTag(this.path);
  }

  remove() {
    this.stateService.removeTag(this.path);
  }

}

