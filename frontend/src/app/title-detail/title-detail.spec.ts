import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TitleDetail } from './title-detail';

describe('TitleDetail', () => {
  let component: TitleDetail;
  let fixture: ComponentFixture<TitleDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TitleDetail]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TitleDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
