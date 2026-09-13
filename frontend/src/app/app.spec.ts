import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { beforeEach, describe, expect, it } from 'vitest';

import { App } from './app';
import { routes } from './app.routes';
import { AppShell } from './layout/app-shell/app-shell';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter(routes)],
    }).compileComponents();
  });

  it('renders the foundation page through the application root outlet', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    await TestBed.inject(Router).navigateByUrl('/');
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('h1')?.textContent).toContain('Retail Management');
    expect(fixture.nativeElement.textContent).toContain(
      'Business functionality is not available yet',
    );
    expect(document.title).toBe('Retail Management');
  });

  it('renders the not-found page for an unknown route', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/unknown-route', AppShell);

    expect(harness.routeNativeElement?.querySelector('h1')?.textContent).toContain(
      'Page not found',
    );
    expect(document.title).toBe('Page not found');
  });

  it('navigates home when the not-found page home link is activated', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/unknown-route', AppShell);
    const homeLink = harness.routeNativeElement?.querySelector<HTMLAnchorElement>('a');

    expect(homeLink).not.toBeNull();
    homeLink!.click();
    await harness.fixture.whenStable();

    expect(TestBed.inject(Router).url).toBe('/');
    expect(harness.routeNativeElement?.querySelector('h1')?.textContent).toContain(
      'Retail Management',
    );
  });
});
