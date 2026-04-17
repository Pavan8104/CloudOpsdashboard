import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

// Angular Material imports - sab Material components yahan import karo
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule } from '@angular/material/dialog';
import { MatBadgeModule } from '@angular/material/badge';

// ng2-charts - Chart.js Angular wrapper
import { NgChartsModule } from 'ng2-charts';

// App modules aur components
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

// Feature components
import { LoginComponent } from './features/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ServiceHealthComponent } from './features/service-health/service-health.component';
import { IncidentsComponent } from './features/incidents/incidents.component';
import { ResourceUsageComponent } from './features/resource-usage/resource-usage.component';

// Shared components
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';

// Core services - interceptors yahan register karte hain
import { JwtInterceptor } from './core/interceptors/jwt.interceptor';

/**
 * Root AppModule - poori Angular application yahan registered hai.
 *
 * Material modules sab import kiye hain - bundle size ke liye tree-shaking pe depend karo.
 * JwtInterceptor HTTP_INTERCEPTORS ke saath provide kiya hai - multi: true important hai.
 * NgChartsModule Chart.js ke saath register hota hai - charts kaam karne ke liye.
 */
@NgModule({
  declarations: [
    // Root component
    AppComponent,

    // Feature components - har page ka component yahan declare hota hai
    LoginComponent,
    DashboardComponent,
    ServiceHealthComponent,
    IncidentsComponent,
    ResourceUsageComponent,

    // Shared layout components
    NavbarComponent,
    SidebarComponent
  ],
  imports: [
    // Angular core
    BrowserModule,
    BrowserAnimationsModule,  // Material animations ke liye zaroori
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,

    // Routing
    AppRoutingModule,

    // Angular Material - sab module ek jagah
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatSidenavModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatChipsModule,
    MatTabsModule,
    MatMenuModule,
    MatSelectModule,
    MatTooltipModule,
    MatDividerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatBadgeModule,

    // Charts
    NgChartsModule
  ],
  providers: [
    // JWT interceptor - har HTTP request mein token attach hoga
    {
      provide: HTTP_INTERCEPTORS,
      useClass: JwtInterceptor,
      multi: true  // Multiple interceptors support ke liye multi: true zaroori hai
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
