///<reference path='../state/database/TagNode.ts'/>
///<reference path='../state/ui/TagTreeTable.ts'/>

import {ChangeDetectionStrategy, Component, Input, View} from 'angular2/core';
import {NgFor} from 'angular2/common';
import {TagTreeTable} from '../state/ui/TagTreeTable';
import {TagName} from './TagName';
import {CellEntry} from './CellEntry';

@Component({
  selector: 'TagList',
  properties: [
    'table',
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
@View({
  templateUrl: './app/components/TagList.html',
  styleUrls: ['./app/components/TagList.css'],
  directives: [NgFor, CellEntry, TagName],
})
export class TagList {
  @Input() table: TagTreeTable;
}

