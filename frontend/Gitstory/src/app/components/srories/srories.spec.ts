import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Srories } from './srories';

describe('Srories', () => {
  let component: Srories;
  let fixture: ComponentFixture<Srories>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Srories]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Srories);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
