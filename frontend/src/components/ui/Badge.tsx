import type { ReactNode } from 'react';

type BadgeVariant = 'default' | 'green' | 'red' | 'yellow' | 'gray' | 'blue' | 'purple' | 'orange' | 'cyan';

interface BadgeProps {
    children: ReactNode;
    variant?: BadgeVariant;
}

const variants: Record<BadgeVariant, string> = {
    default: 'bg-slate-100 text-slate-600',
    green: 'bg-emerald-50 text-emerald-700 border border-emerald-200',
    red: 'bg-red-50 text-red-700 border border-red-200',
    yellow: 'bg-amber=50 text-amber-700 border border-amber-slate-200',
    gray: 'bg-slate-100 text-slate-500 border border-slate-200',
    blue: 'bg-blue--50 text-blue-700 border border-blue-200',
    purple: 'bg-purple-50 text-purple-700 border border-purple-200',
    orange: 'bg-orange-50 text-orange-700 border border-orange-200',
    cyan: 'bg-cyan-50 text-cyan-700 border border-cyan-200',
};

export default function Badge({ children, variant = 'default' }: BadgeProps) {
    return (
        <span className={`inline-flex items-center px-2 py-0.5 rounded-md text-xs font-semibold ${variants[variant]}`}>{children}</span>
    );
}

export const assetTypeBadge: Record<string, BadgeVariant> = {
    Savings: 'green', Investment: 'blue', Property: 'purple', Retirement: 'yellow', Other: 'gray',
};

export const liabilityTypeBadge: Record<string, BadgeVariant> = {
    Mortgage: 'red', Auto: 'orange', Student: 'purple', 'Credit Card': 'yellow', Personal: 'cyan', Other: 'gray',
};

export const expenseCategoryBadge: Record<string, BadgeVariant> = {
    Housing: 'blue', Food: 'green', Transport: 'orange', Healthcare: 'purple', Entertainment: 'cyan', Utilities: 'yellow', Other: 'gray',
};

export const ratioStatusBadge: Record<string, BadgeVariant> = {
    GOOD: 'green', WARNING: 'yellow', CRITICAL: 'red', NO_DATA: 'gray',
};