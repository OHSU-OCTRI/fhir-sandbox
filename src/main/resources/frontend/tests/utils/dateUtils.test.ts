import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest';
import { formatDate, getAge } from '../../utils/dateUtils';

describe('formatDate', () => {
  test('default date format', () => {
    expect(formatDate('2026-05-26')).toBe('26 May 2026');
  });

  test('custom date formats', () => {
    expect(formatDate('2026-05-26', 'YYYY-MM-DD')).toBe('2026-05-26');
    expect(formatDate('2026-05-26', 'MM/DD/YYYY')).toBe('05/26/2026');
  });
});

describe('getAge', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    // Use noon local time to avoid UTC-midnight parsing shifting to the previous day.
    vi.setSystemTime(new Date('2026-05-26T12:00:00'));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  test('calculates age in years, months, and days', () => {
    // Born exactly 30 years ago today -> 30y 0m 0d
    expect(getAge('1996-05-26')).toBe('30y 0m 0d');
  });

  test('calculates age with non-zero months and days', () => {
    // Born 1990-01-15 -> 36 years, 4 months, 11 days
    expect(getAge('1990-01-15')).toBe('36y 4m 11d');
  });

  test('calculates age for a newborn (same day)', () => {
    expect(getAge('2026-05-26')).toBe('0y 0m 0d');
  });

  test('calculates age for an infant born earlier in the year', () => {
    // Born 2026-03-01 -> 0 years, 2 months, 25 days
    expect(getAge('2026-03-01')).toBe('0y 2m 25d');
  });
});
