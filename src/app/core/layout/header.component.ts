import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';
import { getUserRole } from '../utils/auth.utils';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})
export class HeaderComponent {

  role = getUserRole();

  constructor(private authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
}