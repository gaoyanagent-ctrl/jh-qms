import { IafThemeProvider } from '@iaf/theme';
import { render, screen } from '@testing-library/react';
import type { ReactElement } from 'react';
import { describe, expect, it } from 'vitest';
import { InventoryStatusTag, StatusTag, TaskStatusTag, resolveBusinessStatusTone } from './index';

const renderWithTheme = (node: ReactElement) => render(<IafThemeProvider>{node}</IafThemeProvider>);
const findTag = (label: string) => {
  const tag = screen.getByText(label).closest('.ant-tag');
  expect(tag).not.toBeNull();
  return tag as HTMLElement;
};

describe('business status tags', () => {
  it('maps platform and business statuses to semantic tones', () => {
    expect(resolveBusinessStatusTone('DRAFT')).toBe('draft');
    expect(resolveBusinessStatusTone('PENDING_APPROVAL')).toBe('pending');
    expect(resolveBusinessStatusTone('APPROVED')).toBe('approved');
    expect(resolveBusinessStatusTone('REJECTED')).toBe('rejected');
    expect(resolveBusinessStatusTone('IN_PROGRESS')).toBe('processing');
    expect(resolveBusinessStatusTone('CLOSED')).toBe('closed');
    expect(resolveBusinessStatusTone('ACTIVE')).toBe('approved');
    expect(resolveBusinessStatusTone('DISABLED')).toBe('rejected');
  });

  it('renders status tags from theme semantic tokens', () => {
    renderWithTheme(<StatusTag status="PENDING_APPROVAL" label="Pending" />);
    const tag = findTag('Pending');
    expect(tag.style.color).toBe('rgb(184, 132, 22)');
    expect(tag.style.background).toBe('rgb(255, 244, 214)');
  });

  it('uses inventory and urgent task semantic tokens', () => {
    renderWithTheme(
      <>
        <InventoryStatusTag status="AVAILABLE" label="Available" />
        <InventoryStatusTag status="FROZEN" label="Frozen" />
        <TaskStatusTag status="OPEN" label="Urgent" urgent />
      </>
    );

    expect(findTag('Available').style.color).toBe('rgb(4, 120, 87)');
    expect(findTag('Frozen').style.color).toBe('rgb(184, 132, 22)');
    expect(findTag('Urgent').style.color).toBe('rgb(220, 38, 38)');
  });
});
