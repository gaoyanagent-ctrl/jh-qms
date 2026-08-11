// @vitest-environment jsdom
import { render, screen, cleanup } from '@testing-library/react';
import { Form, Input } from 'antd';
import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import '@testing-library/jest-dom/vitest';
import { FieldPermissionWrapper } from './FieldPermissionWrapper';

describe('FieldPermissionWrapper', () => {
  afterEach(() => {
    cleanup();
  });

  beforeAll(() => {
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: vi.fn().mockImplementation((query) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn()
      }))
    });
  });

  it('renders input in VISIBLE_EDITABLE mode', () => {
    render(
      <Form initialValues={{ username: 'john_doe' }}>
        <FieldPermissionWrapper permission="VISIBLE_EDITABLE" label="Username" name="username">
          <Input data-testid="user-input" />
        </FieldPermissionWrapper>
      </Form>
    );

    expect(screen.getByTestId('user-input')).toBeInTheDocument();
    expect(screen.getByTestId('user-input')).toHaveValue('john_doe');
  });

  it('renders read-only text in VISIBLE_READONLY mode', () => {
    render(
      <Form initialValues={{ username: 'john_doe' }}>
        <FieldPermissionWrapper permission="VISIBLE_READONLY" label="Username" name="username">
          <Input data-testid="user-input" />
        </FieldPermissionWrapper>
      </Form>
    );

    expect(screen.queryByTestId('user-input')).not.toBeInTheDocument();
    expect(screen.getByText('john_doe')).toBeInTheDocument();
  });

  it('renders nothing in HIDDEN mode', () => {
    render(
      <Form initialValues={{ username: 'john_doe' }}>
        <FieldPermissionWrapper permission="HIDDEN" label="Username" name="username">
          <Input data-testid="user-input" />
        </FieldPermissionWrapper>
      </Form>
    );

    expect(screen.queryByTestId('user-input')).not.toBeInTheDocument();
    expect(screen.queryByText('john_doe')).not.toBeInTheDocument();
  });

  it('renders masked value in MASKED mode', () => {
    render(
      <Form initialValues={{ username: 'john_doe' }}>
        <FieldPermissionWrapper
          permission="MASKED"
          label="Username"
          name="username"
          maskedValue="jo******"
        >
          <Input data-testid="user-input" />
        </FieldPermissionWrapper>
      </Form>
    );

    expect(screen.queryByTestId('user-input')).not.toBeInTheDocument();
    expect(screen.queryByText('john_doe')).not.toBeInTheDocument();
    expect(screen.getByText('jo******')).toBeInTheDocument();
  });
});

// Mock PageForm wrapper if needed, here we just use Form
const PageForm = Form;
