import { Routes } from '@angular/router';
import { AppShell } from './layout/app-shell/app-shell';

export const routes: Routes = [
  {
    path: '',
    component: AppShell,
    children: [
      {
        path: '',
        loadComponent: () => import('./pages/foundation/foundation').then((m) => m.Foundation),
        title: 'Retail Management',
      },
      {
        path: '**',
        loadComponent: () => import('./pages/not-found/not-found').then((m) => m.NotFound),
        title: 'Page not found',
      },
    ],
  },
];
