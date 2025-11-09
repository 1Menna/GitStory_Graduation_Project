import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { map } from 'rxjs/operators';

import { StoryContent } from '../Interfaces/story-content';
import { IStory } from '../Interfaces/IStory';

export interface Story {
  storyId: number;
  title: string;
  commits: string[];
}

@Injectable({
  providedIn: 'root'
})
export class CommitsService {
  private storiesUpdated = new Subject<void>(); // notify when new stories are ready

  constructor(private http: HttpClient) {}

   getStories(): Observable<IStory[]> {
    return this.http.get<any>('http://localhost:8080/api/commits/schema').pipe(
      map(res => {
        const stories: IStory[] = [];
        for (const [title, content] of Object.entries(res)) {
          // Type assertion لأن Object.entries يرجع [string, unknown][]
          const storyContent = content as StoryContent;

          // Flatten all commits
          const commits: string[] = [];
          Object.values(storyContent).forEach((arr: string[]) => commits.push(...arr));

          stories.push({
            title,
            commitsByType: storyContent,
            commits
          });
        }
        return stories;
      })
    );
  }

  analyzeRepo(repoUrl: string, startDate: string, endDate: string): Observable<any> {
    const body = { repoUrl, startDate, endDate };
    return this.http.post('http://localhost:8080/api/commits/import', body);
  }

  // method to expose the Subject as Observable
  onStoriesUpdated(): Observable<void> {
    return this.storiesUpdated.asObservable();
  }

  // call this after successful analysis
  notifyStoriesUpdated() {
    this.storiesUpdated.next();
  }
}
