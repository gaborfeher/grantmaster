///<reference path='../data/Model.ts'/>

import {Component, Input, View} from 'angular2/core';
import {NgIf, NgModel} from 'angular2/common';
import {Project} from '../data/Model';
import {DataService} from './DataService';

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
  dataService: DataService;

  constructor(dataService: DataService) {
    this.editing = false;
    this.dataService = dataService;
  }

  startEdit() {
    this.editing = true;
    this.editedProjectName = this.project.name;
  }

  commitEdit() {
    this.editing = false;
    this.dataService.setProjectName(this.path, this.editedProjectName);
  }

}

