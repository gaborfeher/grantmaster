///<reference path='../state/database/Project.ts'/>

import {Component, Input, View} from 'angular2/core';
import {NgIf, NgModel} from 'angular2/common';
import {Project} from '../state/database/Project';
import {StateService} from './StateService';

@Component({
  selector: 'ProjectItem',
  properties: [
    'project',
    'selected',
    'path'
  ]
})
@View({
  templateUrl: './app/components/ProjectItem.html',
  styleUrls: ['./app/components/ProjectItem.css'],
  directives: [NgIf, NgModel],
})
export class ProjectItemComponent {
  @Input() project: Project;
  @Input() path: Array<string>;
  editing: boolean;
  editedProjectName: string;
  stateService: StateService;

  constructor(stateService: StateService) {
    this.editing = false;
    this.stateService = stateService;
  }

  startEdit() {
    this.editing = true;
    this.editedProjectName = this.project.name;
  }

  commitEdit() {
    this.editing = false;
    this.stateService.setProjectName(this.path, this.editedProjectName);
  }

}

