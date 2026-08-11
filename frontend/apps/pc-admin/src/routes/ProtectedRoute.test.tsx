import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { ProtectedRoute } from './ProtectedRoute';

describe('ProtectedRoute', () => {
  it('redirects unauthenticated users to login', () => {
    render(
      <MemoryRouter initialEntries={['/platform/users']}>
        <Routes>
          <Route path="/login" element={<div>login-marker</div>} />
          <Route element={<ProtectedRoute />}>
            <Route path="/platform/users" element={<div>users-marker</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('login-marker')).toBeInTheDocument();
  });
});
