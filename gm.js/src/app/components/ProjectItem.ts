///<reference path='../state/database/Project.ts'/>

import {Component, Input} from '@angular/core';
import {Project} from '../state/database/Project';
import {StateService} from './StateService';

@Component({
  selector: 'ProjectItem',
  templateUrl: './app/components/ProjectItem.html',
  styleUrls: ['./app/components/ProjectItem.css'],
})
export class ProjectItemComponent {
  @Input() selected: boolean;
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

