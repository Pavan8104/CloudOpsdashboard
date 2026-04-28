/**
 * Development environment config - local machine pe kaam karte waqt yahi file use hoti hai.
 * Production ke liye environment.prod.ts use hoti hai - angular.json mein switch hota hai.
 * API URL proxy ke through jaata hai - proxy.conf.json mein backend forward hota hai.
 */
export const environment = {
  production: false,
  apiUrl: '/api',  // Proxy se forward hoga localhost:8080/api pe
  appName: 'CloudOps Dashboard',
  version: '1.0.0-dev',
  // Debug mode on - extra logging ke liye
  debug: true,
  // Token storage key - localStorage mein save hoga
  tokenKey: 'cloudops_token',
  userKey: 'cloudops_user',
  // Feature flags
  chatbotEnabled: true,
  speechRecognitionEnabled: true
};
