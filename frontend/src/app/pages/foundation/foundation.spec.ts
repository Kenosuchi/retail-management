import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';

import { Foundation } from './foundation';

describe('Foundation', () => {
  let component: Foundation;
  let fixture: ComponentFixture<Foundation>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Foundation],
    }).compileComponents();

    fixture = TestBed.createComponent(Foundation);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('communicates the foundation release state', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(fixture.nativeElement.querySelector('h1')?.textContent).toContain('Retail Management');
    expect(fixture.nativeElement.querySelector('p')?.textContent).toContain(
      'Business functionality is not available yet',
    );
  });
});
