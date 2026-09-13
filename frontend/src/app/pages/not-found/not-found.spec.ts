import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';

import { routes } from '../../app.routes';
import { NotFound } from './not-found';

describe('NotFound', () => {
  let component: NotFound;
  let fixture: ComponentFixture<NotFound>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NotFound],
      providers: [provideRouter(routes)],
    }).compileComponents();

    fixture = TestBed.createComponent(NotFound);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('renders a recovery link to the home route', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(fixture.nativeElement.querySelector('h1')?.textContent).toContain('Page not found');

    const nativeElement = fixture.nativeElement as HTMLElement;
    const homeLink = nativeElement.querySelector<HTMLAnchorElement>('a[routerLink="/"]');
    expect(homeLink?.textContent).toContain('Go back home');
    expect(homeLink?.getAttribute('href')).toBe('/');
  });
});
