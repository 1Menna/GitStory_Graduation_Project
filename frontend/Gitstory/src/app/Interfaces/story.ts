import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class Story {
    storyId: number | undefined;
    title: string | undefined;
    commits: string[] | undefined;
}
