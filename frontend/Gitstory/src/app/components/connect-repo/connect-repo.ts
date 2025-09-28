import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommitsService } from '../../services/commits-service';
 // make sure path is correct

@Component({
  selector: 'app-connect-repo',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './connect-repo.html',
  styleUrls: ['./connect-repo.css']
})
export class ConnectRepo {
  repoUrl: string = '';
  errorMsg: string = '';
  loading: boolean = false;

  // Inject your CommitsService
  constructor(private _commitsService:  CommitsService) {}

  analyzeRepo() {
    this.errorMsg = '';
    if (!this.repoUrl || !this.isValidGithubUrl(this.repoUrl)) {
      this.errorMsg = 'Please enter a valid GitHub repository URL.';
      return;
    }

    this.loading = true;

    // Use mt Commits_Service to send URL to back 
    this._commitsService.analyzeRepo(this.repoUrl).subscribe({
      next: (res: any) => {
        console.log(res); // handle backend response
        alert(`Repository ${this.repoUrl} analyzed successfully!`);
        this.repoUrl = '';
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'Error analyzing repository. Please try again.';
        this.loading = false;
      }
    });
  }

  isValidGithubUrl(url: string): boolean {
    const githubRegex = /^https:\/\/github\.com\/[\w-]+\/[\w-]+(\.git)?$/;
    return githubRegex.test(url);
  }
}
