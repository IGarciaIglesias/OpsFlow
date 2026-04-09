import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

function getRoleFromToken(): string | null {
  const token = sessionStorage.getItem('token');
  if (!token) return null;

  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role?.replace('ROLE_', '') ?? null;
  } catch {
    return null;
  }
}

export const roleGuard: CanActivateFn = (route) => {

  const router = inject(Router);
  const allowedRoles: string[] = route.data?.['roles'];

  const userRole = getRoleFromToken();

  if (!userRole || !allowedRoles?.includes(userRole)) {
    router.navigate(['/requests']);
    return false;
  }

  return true;
};
