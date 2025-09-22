import { TestBed } from '@angular/core/testing';

import { Regulation } from './regulation';

describe('Regulation', () => {
  let service: Regulation;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Regulation);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
