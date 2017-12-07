///<reference path='../state/database/TagNode.ts'/>
///<reference path='../state/ui/TagTreeTable.ts'/>

import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {NgForOf} from '@angular/common';
import {TagTreeTable} from '../state/ui/TagTreeTable';
import {TagName} from './TagName';
import {CellEntry} from './CellEntry';

@Component({
  selector: 'TagList',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './app/components/TagList.html',
  styleUrls: ['./app/components/TagList.css'],
  // directives: [NgFor, CellEntry, TagName],
})
export class TagList {
  @Input() table: TagTreeTable;
}

