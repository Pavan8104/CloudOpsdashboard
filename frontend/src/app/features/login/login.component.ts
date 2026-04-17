import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

/**
 * Login Component - CloudOps ka entry point.
 *
 * Reactive forms use kar rahe hain - template driven se zyada control milta hai.
 * Error messages specific hain - "kya galat hua" clearly dikhaao.
 * Password visibility toggle bhi hai - standard UX pattern hai.
 * Loading state manage karo - double submission prevent karne ke liye.
 */
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  hidePassword = true;  // Password field visibility toggle

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Pehle se logged in hai toh dashboard pe redirect karo
    if (this.authService.isLoggedIn) {
      this.router.navigate(['/dashboard']);
      return;
    }

    // Reactive form banao validators ke saath
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  /**
   * Form submit - validation pass hone ke baad API call karo.
   */
  onSubmit(): void {
    if (this.loginForm.invalid) {
      // Form invalid - error messages automatically dikhenge
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const credentials = this.loginForm.value;

    this.authService.login(credentials).subscribe({
      next: () => {
        // Login successful - dashboard pe jaao
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        // User friendly error message - stack trace mat dikhaao
        if (err.status === 401) {
          this.errorMessage = 'Invalid username ya password - dobara check karo';
        } else if (err.status === 0) {
          this.errorMessage = 'Server se connection nahi ho raha - backend running hai?';
        } else {
          this.errorMessage = err.error?.message || 'Login mein kuch gadbad ho gayi';
        }
      }
    });
  }

  // Form field getters - template mein clean access ke liye
  get usernameControl() { return this.loginForm.get('username'); }
  get passwordControl() { return this.loginForm.get('password'); }
}
