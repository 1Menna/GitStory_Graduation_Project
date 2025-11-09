import { Injectable } from '@angular/core';
import { StoryContent } from './story-content';


export interface IStory {
  storyId?: number;          // اختياري
  title: string;
  commitsByType: StoryContent;
  commits: string[];         // جميع الـ commits مسطحة
}
