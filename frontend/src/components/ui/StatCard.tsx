import type { ReactNode } from 'react';
import Card from './Card';

interface StatCardProps {
    label: string;
    value: string;
    sub?: string;
    icon?: ReactNode;
    accent?: 'green' | 'red' | 'yellow' | 'default';
    delta?: number | null;
}

const accents = {
    green:   'text-emerald-600',
    red:     'text-red-600',
    blue:    'text-blue-600',
    yellow:  'text-amber-600',
    default: 'text-slate-800',
};

const iconBg = {
  green:   'bg-emerald-50 text-emerald-600',
  red:     'bg-red-50 text-red-600',
  blue:    'bg-blue-50 text-blue-600',
  yellow:  'bg-amber-50 text-amber-600',
  default: 'bg-slate-100 text-slate-500',
};
 
export default function StatCard({ label, value, sub, icon, accent = 'default', delta }: StatCardProps) {
  return (
    <Card>
      <div className="flex items-start justify-between mb-3">
        <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">{label}</span>
        {icon && (
          <div className={`w-8 h-8 rounded-lg flex items-center justify-center text-sm ${iconBg[accent]}`}>
            {icon}
          </div>
        )}
      </div>
      <div className={`text-2xl font-bold tracking-tight tabular-nums mb-2 ${accents[accent]}`}>
        {value}
      </div>
      <div className="flex items-center gap-2">
        {delta != null && (
          <span className={`text-xs font-semibold px-1.5 py-0.5 rounded ${delta >= 0 ? 'bg-emerald-50 text-emerald-600' : 'bg-red-50 text-red-600'}`}>
            {delta >= 0 ? '▲' : '▼'} {Math.abs(delta).toFixed(1)}%
          </span>
        )}
        {sub && <span className="text-xs text-slate-400">{sub}</span>}
      </div>
    </Card>
  );
}