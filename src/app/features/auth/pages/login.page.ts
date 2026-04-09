import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Component({
    standalone: true,
    imports: [CommonModule, FormsModule],
    styleUrls: ['./login.page.css'],
    template: `
        <h1>Login</h1>

        <form (ngSubmit)="login()">
        <input [(ngModel)]="username" name="username" placeholder="Username" />
        <input [(ngModel)]="password" name="password" type="password" placeholder="Password" />
        <button type="submit">Login</button>
        </form>
    `,
})

export class LoginPage {

    username = '';
    password = '';

    constructor(
    private authService: AuthService,
    private router: Router
    ) {}

    login() {
    this.authService.login({
        username: this.username,
        password: this.password,
    }).subscribe({
        next: res => {
        this.authService.saveToken(res.token);
        this.router.navigate(['/requests']); 
        },
        error: err => console.error(err),
    });
    }
}