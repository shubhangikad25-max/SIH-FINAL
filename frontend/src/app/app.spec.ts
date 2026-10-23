import '@angular/compiler';
import { describe, it, expect, beforeEach } from 'vitest';
import { App } from './app';

describe('App Component Logic', () => {
  let app: App;
  let mockApaarService: any;

  beforeEach(() => {
    mockApaarService = {
      user$: { subscribe: (cb: any) => cb(null) },
      logout: () => {},
      sahayakQuery: (text: string) => ({
        subscribe: (handlers: any) => handlers.next({ success: true, reply: 'AICTE Sahayak answer' })
      })
    };
    app = new App(mockApaarService);
  });

  it('should initialize with closed bot widget and default greeting', () => {
    expect(app.botOpen).toBe(false);
    expect(app.botMessages.length).toBeGreaterThan(0);
    expect(app.botMessages[0].sender).toBe('bot');
  });

  it('should toggle botOpen flag when toggleBot() is called', () => {
    expect(app.botOpen).toBe(false);
    app.toggleBot();
    expect(app.botOpen).toBe(true);
    app.toggleBot();
    expect(app.botOpen).toBe(false);
  });

  it('should format Indian Standard Time string in updateTime()', () => {
    app.updateTime();
    expect(app.currentDateStr).toBeTruthy();
    expect(app.currentDateStr.length).toBeGreaterThan(10);
  });

  it('should dispatch sahayakQuery through ApaarService in sendBotMessage()', () => {
    app.botInput = 'What is APAAR ID?';
    app.sendBotMessage();
    expect(app.botMessages.some(m => m.text.includes('What is APAAR ID?'))).toBe(true);
    expect(app.botMessages.some(m => m.text.includes('AICTE Sahayak answer'))).toBe(true);
  });
});
