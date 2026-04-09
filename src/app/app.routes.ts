import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const appRoutes: Routes = [

  {
    path: 'login',
    loadChildren: () =>
      import('./features/auth/auth.routes')
        .then(m => m.AUTH_ROUTES),
  },

  {
    path: 'requests',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/requests/requests.routes')
        .then(m => m.REQUEST_ROUTES),
  },

  { path: '', redirectTo: 'requests', pathMatch: 'full' },
  { path: '**', redirectTo: 'requests' }
];