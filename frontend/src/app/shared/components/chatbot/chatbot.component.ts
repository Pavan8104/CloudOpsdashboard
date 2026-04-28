import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked, NgZone } from '@angular/core';
import { FormControl, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Subject } from 'rxjs';
import { takeUntil, timeout } from 'rxjs/operators';
import { AuthService } from '../../../core/auth/auth.service';
import { environment } from '../../../../environments/environment';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  suggestions?: string[];
}

@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule
  ]
})
export class ChatbotComponent implements OnInit, OnDestroy, AfterViewChecked {

  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  isOpen = false;
  isLoading = false;
  isListening = false;
  hasSpeechSupport = false;
  isChatbotEnabled = environment.chatbotEnabled;
  sessionId = this.generateSessionId();
  messages: ChatMessage[] = [];
  inputControl = new FormControl('', [Validators.required, Validators.maxLength(1000)]);

  private destroy$ = new Subject<void>();
  private shouldScrollToBottom = false;
  private recognition: any;

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private ngZone: NgZone
  ) {
    try {
      this.initSpeechRecognition();
    } catch (e) {
      console.warn('Speech recognition initialization skipped:', e);
    }
  }

  private initSpeechRecognition(): void {
    if (typeof window === 'undefined' || !environment.speechRecognitionEnabled) return;
    
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (SpeechRecognition) {
      this.hasSpeechSupport = true;
      this.recognition = new SpeechRecognition();
      this.recognition.continuous = false;
      this.recognition.interimResults = true;
      this.recognition.lang = 'en-US';

      this.recognition.onstart = () => {
        this.ngZone.run(() => {
          this.isListening = true;
        });
      };

      this.recognition.onresult = (event: any) => {
        const transcript = Array.from(event.results)
          .map((result: any) => result[0])
          .map((result: any) => result.transcript)
          .join('');
        
        this.ngZone.run(() => {
          this.inputControl.setValue(transcript);
        });
      };

      this.recognition.onerror = (event: any) => {
        console.error('Speech recognition error', event.error);
        this.ngZone.run(() => {
          this.isListening = false;
        });
      };

      this.recognition.onend = () => {
        this.ngZone.run(() => {
          this.isListening = false;
        });
      };
    }
  }

  toggleVoice(): void {
    if (!this.hasSpeechSupport) {
      alert('Voice recognition is not supported in your browser.');
      return;
    }
    if (this.isListening) {
      this.recognition.stop();
    } else {
      this.inputControl.setValue('');
      this.recognition.start();
    }
  }

  ngOnInit(): void {
    this.messages.push({
      role: 'assistant',
      content: '👋 Hi! I\'m the **CloudOps Assistant**.\n\nI can help you understand incidents, service health, resource utilization, user roles, and how to navigate this dashboard.\n\nWhat would you like to know?',
      timestamp: new Date(),
      suggestions: [
        'How does the dashboard work?',
        'What do severity levels mean?',
        'How do I create an incident?'
      ]
    });
  }

  ngOnDestroy(): void {
    if (this.recognition) {
      this.recognition.stop();
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  toggleChat(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.shouldScrollToBottom = true;
    }
  }

  sendMessage(): void {
    if (this.isListening) {
      this.recognition.stop();
    }
    
    const text = this.inputControl.value?.trim();
    if (!text || this.isLoading) return;

    this.messages.push({
      role: 'user',
      content: text,
      timestamp: new Date()
    });

    this.inputControl.reset();
    this.isLoading = true;
    this.shouldScrollToBottom = true;

    const token = this.authService.getToken();
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

    this.http.post<any>(`${environment.apiUrl}/chatbot/message`, {
      message: text,
      sessionId: this.sessionId
    }, { headers }).pipe(
      takeUntil(this.destroy$),
      timeout(15000)
    ).subscribe({
      next: (response) => {
        this.messages.push({
          role: 'assistant',
          content: response.message,
          timestamp: new Date(),
          suggestions: response.suggestions
        });
        this.isLoading = false;
        this.shouldScrollToBottom = true;
      },
      error: () => {
        this.messages.push({
          role: 'assistant',
          content: 'I\'m having trouble connecting to the server. Please try again in a moment.',
          timestamp: new Date()
        });
        this.isLoading = false;
        this.shouldScrollToBottom = true;
      }
    });
  }

  useSuggestion(suggestion: string): void {
    this.inputControl.setValue(suggestion);
    this.sendMessage();
  }

  clearChat(): void {
    this.messages = [];
    this.sessionId = this.generateSessionId();
    this.ngOnInit();
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  formatMessage(content: string): string {
    return content
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\n/g, '<br>')
      .replace(/•/g, '&bull;');
  }

  private scrollToBottom(): void {
    try {
      const el = this.messagesContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch {}
  }

  private generateSessionId(): string {
    return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
}
