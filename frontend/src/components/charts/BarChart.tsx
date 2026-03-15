import {
  ResponsiveContainer, BarChart, Bar, XAxis, YAxis,
  CartesianGrid, Tooltip, Legend,
} from 'recharts';
import { formatCurrencyCompact } from '../../utils/format';
import Card from '../ui/Card';

interface BarData { label: string; income: number; expenses: number; }
interface Props { data: BarData[]; title?: string; height?: number; }

export default function IncomeExpenseBar({ data, title = 'Income vs Expenses', height = 240 }: Props) {
  const chartData = data.map((d) => ({ name: d.label, Income: d.income, Expenses: d.expenses }));

  return (
    <Card>
      <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-5">{title}</h3>
      <ResponsiveContainer width="100%" height={height}>
        <BarChart data={chartData} barGap={4} margin={{ top: 4, right: 4, left: 0, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
          <XAxis dataKey="name" tick={{ fill: '#94a3b8', fontSize: 11 }} axisLine={false} tickLine={false} />
          <YAxis tick={{ fill: '#94a3b8', fontSize: 11 }} axisLine={false} tickLine={false} tickFormatter={formatCurrencyCompact} width={65} />
          <Tooltip
            contentStyle={{ background: '#fff', border: '1px solid #e2e8f0', borderRadius: 8, fontSize: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }}
            formatter={(val) => [formatCurrencyCompact(Number(val)), '']}
          />
          <Legend iconType="square" iconSize={8} wrapperStyle={{ fontSize: 12, color: '#94a3b8', paddingTop: 10 }} />
          <Bar dataKey="Income"   fill="#059669" radius={[4, 4, 0, 0]} maxBarSize={32} />
          <Bar dataKey="Expenses" fill="#ef4444" radius={[4, 4, 0, 0]} maxBarSize={32} />
        </BarChart>
      </ResponsiveContainer>
    </Card>
  );
}