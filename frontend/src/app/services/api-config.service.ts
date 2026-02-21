import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ApiConfigService {
  private readonly baseUrl = 'http://localhost:8089/api';
  private readonly apiDocsUrl = 'http://localhost:8089/api-docs';
  private readonly swaggerUrl = 'http://localhost:8089/swagger-ui.html';

  /**
   * Get the base API URL
   * @returns Base API URL (e.g., http://localhost:8089/api)
   */
  getBaseUrl(): string {
    return this.baseUrl;
  }

  /**
   * Get the full endpoint URL
   * @param endpoint The endpoint path (e.g., /sites, /equipment)
   * @returns Full endpoint URL
   */
  getEndpointUrl(endpoint: string): string {
    return `${this.baseUrl}${endpoint}`;
  }

  /**
   * Get API documentation URL
   * @returns API docs URL
   */
  getApiDocsUrl(): string {
    return this.apiDocsUrl;
  }

  /**
   * Get Swagger UI URL
   * @returns Swagger URL
   */
  getSwaggerUrl(): string {
    return this.swaggerUrl;
  }

  /**
   * Update base URL (useful for environment changes)
   * @param url New base URL
   */
  setBaseUrl(url: string): void {
    // Note: This would require making baseUrl non-readonly for dynamic changes
    console.log('Base URL configuration is set at application startup');
    console.log('To change it, modify this service or use environment configuration');
  }
}
