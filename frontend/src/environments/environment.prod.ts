/**
 * Production environment config - ng build --configuration production pe yahi use hoti hai.
 * API URL yahan actual backend URL hoga - GCP Cloud Run ya Load Balancer ka address.
 * Debug mode off - console logs production mein nahi dikhne chahiye.
 */
export const environment = {
  production: true,
  apiUrl: 'https://api.cloudops.internal/api',  // Production backend URL - GCP pe change karo
  appName: 'CloudOps Dashboard',
  version: '1.0.0',
  debug: false,
  tokenKey: 'cloudops_token',
  userKey: 'cloudops_user',
};
