export const formatCurrency = (amount: number | null | undefined): string => {
    if (amount == null) return '-';
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    }).format(amount);
};

export const formatCurrencyCompact = (amount: number | null | undefined): string => {
    if (amount == null) return '-';
    const abs = Math.abs(amount);
    const sign = amount < 0 ? '-' : '';
    if (abs >= 1_000_000) return `${sign}$${(abs / 1_000_000).toFixed(1)}M`;
    if (abs >= 1_000) return `${sign}$${(abs / 1_000).toFixed(0)}K`;
    return formatCurrency(amount);
};

export const formatPercent = (value: number | null | undefined, decimals = 1):
string => {
    if (value == null) return '-';
    return `${value.toFixed(decimals)}%`;
};

export const formatDate = (isoString: string | null | undefined): string => {
    if (!isoString) return '-';
    return new Date(isoString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
    });
};

export const formatDateShort = (isoString: string | null | undefined): string => {
  if (!isoString) return '—';
  return new Date(isoString).toLocaleDateString('en-US', {
    month: 'short',
    year: '2-digit',
  });
};

export const toISODate = (date: Date): string => {
    return date.toISOString().split('T')[0];
};

export const todayISO = (): string => toISODate(new Date());