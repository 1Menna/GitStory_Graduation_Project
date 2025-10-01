import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';

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

  getStories(): Observable<Story[]> {
    return this.http.get<Story[]>('http://127.0.0.1:8001/stories');
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
