import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';

/**
 * Angular application ka entry point - yahan se sab start hota hai.
 * AppModule bootstrap hota hai, jo saari components aur services register karta hai.
 * Error handling bhi yahan add kar sakte hain production logging ke liye.
 */
platformBrowserDynamic()
  .bootstrapModule(AppModule)
  .catch((err) => {
    // Bootstrap fail ho gayi toh console pe dikhao - debugging ke liye
    console.error('Application bootstrap failed:', err);
  });
