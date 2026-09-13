import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';

import { routes } from '../../app.routes';
import { AppShell } from './app-shell';

describe('AppShell', () => {
  let component: AppShell;
  let fixture: ComponentFixture<AppShell>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppShell],
      providers: [provideRouter(routes)],
    }).compileComponents();

    fixture = TestBed.createComponent(AppShell);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('provides a router outlet for child routes', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(fixture.nativeElement.querySelector('router-outlet')).not.toBeNull();
  });
});
