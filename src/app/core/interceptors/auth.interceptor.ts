import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  // 🔑 NO enviar Authorization en login
  if (req.url.includes('/auth/login')) {
    return next(req);
  }

  const token = sessionStorage.getItem('token');
  if (!token) {
    return next(req);
  }

  const authReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(authReq);
};
