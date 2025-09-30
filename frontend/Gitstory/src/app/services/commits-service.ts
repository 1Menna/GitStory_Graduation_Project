import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CommitsService {

  private apiUrl = 'http://localhost:8080/api/commits/import'; // replace with your API

  constructor(private _HttpClient:HttpClient){}

  analyzeRepo(repoUrl: string): Observable<any> {
    return this._HttpClient.post(this.apiUrl, { repoUrl });
  }
}
