import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CommitsService {
  private apiUrl = 'http://localhost:8080/api/commits/import';

  constructor(private _HttpClient: HttpClient) {}

  analyzeRepo(repoUrl: string, startDate: string, endDate: string): Observable<any> {
    const body = {
      repoUrl: repoUrl,
      startDate: startDate,
      endDate: endDate
    };
    return this._HttpClient.post(this.apiUrl, body);
  }
}
