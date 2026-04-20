import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { LoginRequest, LoginResponse } from '../models/login.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.css'],
})
export class LoginPage {
  username = '';
  password = '';
  loading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  login(): void {
    this.errorMessage = '';

    const payload: LoginRequest = {
      username: this.username.trim(),
      password: this.password,
    };

    if (!payload.username || !payload.password) {
      this.errorMessage = 'Introduce usuario y contraseña';
      return;
    }

    this.loading = true;

    this.authService.login(payload).subscribe({
      next: (response: LoginResponse) => {
        this.authService.saveToken(response.token);
        this.router.navigate(['/requests']);
      },
      error: () => {
        this.errorMessage = 'Credenciales incorrectas';
        this.loading = false;
      },
    });
  }
}