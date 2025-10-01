import { CommonModule } from '@angular/common';
import { Story } from '../../Interfaces/story';
import { CommitsService } from './../../services/commits-service';
import { Component, OnInit } from '@angular/core';
 

@Component({
  selector: 'app-srories',
  imports: [CommonModule],
  templateUrl: './srories.html',
  styleUrl: './srories.css'
})
 
export class Stories implements OnInit {
  stories: Story[] = [];
  loading = false;
  error = '';

  constructor(private commitsService: CommitsService) {}

  ngOnInit(): void {
  this.fetchStories(); // initial load

  // subscribe to updates
  this.commitsService.onStoriesUpdated().subscribe(() => {
    this.fetchStories(); // refresh stories when a repo is analyzed
  });
}


  fetchStories() {
    this.loading = true;
    this.commitsService.getStories().subscribe({
      next: (data) => {
        this.stories = data;
        this.loading = false;
        console.log(data);
      },
      error: (err) => {
        this.error = 'Failed to load stories';
        console.error("abcdefgh");
        this.loading = false;
      }
    });
  }
}

