import dayjs from 'dayjs';

/**
 * Converts the ISO 8601 date string to the given format. The default format is "DD MMM YYYY", e.g. "26 May 2026".
 *
 * @param date ISO 8601 date string
 * @param format output format; defaults to "DD MMM YYYY"
 * @returns formatted date string
 */
export function formatDate(date: string, format: string = 'DD MMM YYYY'): string {
  return dayjs(date).format(format);
}

/**
 * Returns a string representation of a person's age in years, months, and days as of today, given their birthdate.
 *
 * @param birthday ISO 8601 date string
 * @returns string representation of the age in "${years}y ${months}m ${days}d" format
 */
export function getAge(birthday: string): string {
  const now = dayjs();
  const birth = dayjs(birthday);
  const years = now.diff(birth, 'year');
  const afterYears = now.subtract(years, 'year');
  const months = afterYears.diff(birth, 'month');
  const afterMonths = afterYears.subtract(months, 'month');
  const days = afterMonths.diff(birth, 'day');
  return `${years}y ${months}m ${days}d`;
}
