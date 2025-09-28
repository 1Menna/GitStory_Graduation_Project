import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConnectRepo } from './connect-repo';

describe('ConnectRepo', () => {
  let component: ConnectRepo;
  let fixture: ComponentFixture<ConnectRepo>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConnectRepo]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConnectRepo);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
