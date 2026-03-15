import Badge, { ratioStatusBadge } from '../ui/Badge';
import Card from '../ui/Card';
import type { RatioResult } from '../../types';
 
const statusColor = {
  GOOD:    'bg-emerald-500',
  WARNING: 'bg-amber-400',
  CRITICAL:'bg-red-500',
  NO_DATA: 'bg-slate-300',
};
 
const valueColor = {
  GOOD:    'text-emerald-600',
  WARNING: 'text-amber-600',
  CRITICAL:'text-red-600',
  NO_DATA: 'text-slate-400',
};
 
interface Props { ratio: RatioResult; }
 
export default function RatioCard({ ratio }: Props) {
  const hasData = ratio.status !== 'NO_DATA';
  const max = Math.max((ratio.benchmarkMax ?? ratio.benchmarkMin ?? 100) * 1.7, 0.01);
  const pct = hasData ? Math.min(100, (ratio.value / max) * 100) : 0;
 
  return (
    <Card className="flex flex-col gap-3">
      <div className="flex items-start justify-between gap-2">
        <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider leading-tight">
          {ratio.name}
        </span>
        <Badge variant={ratioStatusBadge[ratio.status]}>{ratio.status}</Badge>
      </div>
      <div className={`text-3xl font-bold tabular-nums tracking-tight ${valueColor[ratio.status]}`}>
        {hasData ? `${ratio.value.toFixed(1)}${ratio.unit}` : '—'}
      </div>
      <div className="h-1.5 bg-slate-100 rounded-full overflow-hidden">
        <div
          className={`h-full rounded-full transition-all duration-700 ${statusColor[ratio.status]}`}
          style={{ width: `${pct}%` }}
        />
      </div>
      {ratio.benchmarkMin != null && (
        <p className="text-xs text-slate-400">
          Target: {ratio.benchmarkMin}{ratio.benchmarkMax != null ? `–${ratio.benchmarkMax}` : '+'}{ratio.unit}
        </p>
      )}
      {hasData && (
        <p className="text-xs text-slate-400 leading-relaxed line-clamp-2">{ratio.recommendation}</p>
      )}
    </Card>
  );
}