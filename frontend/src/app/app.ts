import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApaarService, AuthUser } from './services/apaar.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  currentDateStr: string = '';
  botOpen: boolean = false;
  botInput: string = '';
  botLoading: boolean = false;
  botMessages: Array<{ sender: 'bot' | 'user'; text: string }> = [
    { sender: 'bot', text: 'Namaste! I am AICTE Sahayak, powered by Spring Boot 3 & Angular. How can I assist you with APAAR ID verification or approved intake audit?' }
  ];

  currentUser: AuthUser | null = null;

  constructor(public apaarService: ApaarService) {}

  ngOnInit() {
    this.updateTime();
    setInterval(() => this.updateTime(), 1000);

    this.apaarService.user$.subscribe(user => {
      this.currentUser = user;
    });
  }

  updateTime() {
    const now = new Date();
    this.currentDateStr = now.toLocaleString('en-IN', {
      weekday: 'long',
      month: '2-digit',
      day: '2-digit',
      year: 'numeric',
      hour: 'numeric',
      minute: 'numeric',
      second: 'numeric',
      hour12: true
    });
  }

  toggleBot() {
    this.botOpen = !this.botOpen;
  }

  logout() {
    this.apaarService.logout();
  }

  sendBotMessage(query?: string) {
    const text = query || this.botInput;
    if (!text.trim()) return;

    this.botMessages.push({ sender: 'user', text });
    if (!query) this.botInput = '';
    this.botLoading = true;

    // Connect to real authorized backend API endpoint
    this.apaarService.sahayakQuery(text).subscribe({
      next: (res) => {
        this.botLoading = false;
        const reply = res.reply || 'Under SIH PS 26263, APAAR Verify synchronizes student records with Spring Boot JPA and DigiLocker.';
        this.botMessages.push({ sender: 'bot', text: reply });
      },
      error: () => {
        this.botLoading = false;
        this.botMessages.push({
          sender: 'bot',
          text: 'Under NEP 2020, the 12-digit APAAR ID links with DigiLocker and the Academic Bank of Credits (ABC) to ensure verified student records.'
        });
      }
    });
  }
}
