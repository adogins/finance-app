import {
  ResponsiveContainer, AreaChart, Area, XAxis, YAxis,
  CartesianGrid, Tooltip, Legend,
} from 'recharts';
import { formatCurrencyCompact, formatDateShort } from '../../utils/format';
import type { SnapshotResponse } from '../../types';
import Card from '../ui/Card';

interface Props { snapshots: SnapshotResponse[]; }

export default function NetWorthChart({ snapshots }: Props) {
  const data = snapshots.map((s) => ({
    date: formatDateShort(s.snapshotDate),
    'Net Worth':  Number(s.netWorth),
    Assets:       Number(s.totalAssets),
    Liabilities:  Number(s.totalLiabilities),
  }));

  return (
    <Card>
      <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-5">
        Net Worth Over Time
      </h3>
      <ResponsiveContainer width="100%" height={240}>
        <AreaChart data={data} margin={{ top: 4, right: 4, left: 0, bottom: 0 }}>
          <defs>
            <linearGradient id="gNW" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#2563eb" stopOpacity={0.15} />
              <stop offset="100%" stopColor="#2563eb" stopOpacity={0} />
            </linearGradient>
            <linearGradient id="gAssets" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#059669" stopOpacity={0.12} />
              <stop offset="100%" stopColor="#059669" stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
          <XAxis dataKey="date" tick={{ fill: '#94a3b8', fontSize: 11 }} axisLine={false} tickLine={false} />
          <YAxis tick={{ fill: '#94a3b8', fontSize: 11 }} axisLine={false} tickLine={false} tickFormatter={formatCurrencyCompact} width={70} />
          <Tooltip
            contentStyle={{ background: '#fff', border: '1px solid #e2e8f0', borderRadius: 8, fontSize: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }}
            labelStyle={{ color: '#64748b' }}
            formatter={(val) => [formatCurrencyCompact(Number(val)), '']}
          />
          <Legend iconType="circle" iconSize={7} wrapperStyle={{ fontSize: 12, color: '#94a3b8', paddingTop: 12 }} />
          <Area type="monotone" dataKey="Assets"    stroke="#059669" fill="url(#gAssets)" strokeWidth={1.5} dot={false} />
          <Area type="monotone" dataKey="Net Worth" stroke="#2563eb" fill="url(#gNW)"     strokeWidth={2.5} dot={false} />
          <Area type="monotone" dataKey="Liabilities" stroke="#ef4444" fill="none" strokeWidth={1.5} dot={false} strokeDasharray="4 3" />
        </AreaChart>
      </ResponsiveContainer>
    </Card>
  );
}