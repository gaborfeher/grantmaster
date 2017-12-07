import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {TagTreeTable} from 'app/state/ui/TagTreeTable';

@Component({
  selector: 'TagList',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './app/components/TagList.html',
  styleUrls: ['./app/components/TagList.css'],
})
export class TagList {
  @Input() table: TagTreeTable;
}

