import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// YYYYMMDD 형태를 YYYY.MM.DD 형태로 변환
export function formatDateToDot(date?: string): string {
  if (!date) {
    return '';
  }
  const trimmedDate = String(date).trim();
  const found = trimmedDate.match(/^(\d{4})(\d{2})(\d{2})$/);
  if (!found) {
    return date;
  }
  const [, year, month, day] = found;
  return `${year}.${month}.${day}`;
}

// YYYYMMDD 형태를 YYYY-MM-DD HH:MM:SS 형태로 변환
export function formatDateToHyphen(date: string): string {
  if (!date) {
    return '';
  }
  return date.replace(
    /^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})$/,
    '$1-$2-$3 $4:$5:$6'
  );
}

