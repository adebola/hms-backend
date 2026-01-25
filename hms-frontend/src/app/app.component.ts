import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TenantService } from './core/services/tenant.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'HMS - Health Management System';

  constructor(private tenantService: TenantService) {}

  ngOnInit(): void {
    // Load tenant branding on app initialization
    this.tenantService.loadBrandingForCurrentTenant();
  }
}
