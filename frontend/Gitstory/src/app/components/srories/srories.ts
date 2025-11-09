import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IStory } from '../../Interfaces/IStory';
import { CommitsService } from './../../services/commits-service';

@Component({
  selector: 'app-stories',
  imports: [CommonModule],
  templateUrl: './srories.html',
  styleUrls: ['./srories.css']
})
export class Stories implements OnInit {
  stories: IStory[] = [];
  loading = false;
  error = '';

  constructor(private commitsService: CommitsService) {}

  ngOnInit(): void {
    this.fetchStories();
    this.commitsService.onStoriesUpdated().subscribe(() => {
      this.fetchStories();
    });
  }

  fetchStories() {
    this.loading = true;
    this.commitsService.getStories().subscribe({
      next: (data) => {
        this.stories = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load stories';
        this.loading = false;
      }
    });
  }

  storyTypes(story: IStory): string[] {
    return Object.keys(story.commitsByType);
  }
}
