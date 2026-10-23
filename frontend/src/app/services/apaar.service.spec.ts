import '@angular/compiler';
import { describe, it, expect, beforeEach } from 'vitest';
import { ApaarService } from './apaar.service';
import { of } from 'rxjs';

describe('ApaarService', () => {
  let service: ApaarService;
  let mockHttp: any;

  beforeEach(() => {
    mockHttp = {
      get: (url: string) => of({ success: true, url }),
      post: (url: string, body: any) => of({ success: true, url, body })
    };
    service = new ApaarService(mockHttp);
  });

  it('should initialize with no authenticated user', () => {
    service.logout();
    expect(service.currentUser).toBeNull();
    expect(service.isLoggedIn).toBe(false);
  });

  it('should format verifyApaar URL with 12-digit ID', () => {
    let capturedUrl = '';
    mockHttp.get = (url: string) => {
      capturedUrl = url;
      return of({ success: true });
    };

    service.verifyApaar('123456789012', true).subscribe();
    expect(capturedUrl).toContain('/verify/123456789012?consent=true');
  });

  it('should set authorization header when token exists', () => {
    sessionStorage.setItem('apaar_token', 'sample_jwt_token_2026');
    const headers = (service as any).getHeaders();
    expect(headers.get('Authorization')).toBe('Bearer sample_jwt_token_2026');
    sessionStorage.removeItem('apaar_token');
  });

  it('should clear session upon logout', () => {
    sessionStorage.setItem('apaar_token', 'test_token');
    sessionStorage.setItem('apaar_user', JSON.stringify({ id: 1, email: 'test@aicte.ac.in' }));
    
    service.logout();
    expect(service.token).toBeNull();
    expect(service.currentUser).toBeNull();
    expect(service.isLoggedIn).toBe(false);
  });
});
