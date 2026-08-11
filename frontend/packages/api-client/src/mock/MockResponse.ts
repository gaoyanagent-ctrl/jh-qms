import type { Result } from '@iaf/domain-types';

export interface MockResponse<T = any> {
  status: number;
  data: Result<T>;
}

export const mockSuccess = <T>(data: T, message = 'Success', code = 'OK'): MockResponse<T> => ({
  status: 200,
  data: {
    success: true,
    code,
    message,
    data
  }
});

export const mockError = (message: string, code = 'ERROR', status = 400): MockResponse<any> => ({
  status,
  data: {
    success: false,
    code,
    message,
    data: undefined as any
  }
});
