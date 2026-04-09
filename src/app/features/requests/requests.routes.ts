import { Routes } from '@angular/router';
import { RequestListPage } from './pages/request-list.page';
import { RequestCreatePage } from './pages/request-create.page';
import { RequestDetailPage } from './pages/request-detail.page';
import { roleGuard } from '../../core/guards/role.guard';

export const REQUEST_ROUTES: Routes = [

  {
    path: '',
    component: RequestListPage,
  },

  {
    path: 'new',
    component: RequestCreatePage,
    canActivate: [roleGuard],
    data: {
      roles: ['ADMIN', 'MANAGER', 'OPERATOR'],
    },
  },

  {
    path: ':id',
    component: RequestDetailPage,
    canActivate: [roleGuard],
    data: {
      roles: ['ADMIN', 'MANAGER'],
    },
  },
];
