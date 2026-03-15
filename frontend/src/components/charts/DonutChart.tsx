import { ResponsiveContainer, PieChart, Pie, Cell, Tooltip } from 'recharts';
import { formatCurrency, formatPercent } from '../../utils/format';
import Card from '../ui/Card';

const COLORS = ['#2563eb','#059669','#f59e0b','#7c3aed','#0891b2','#db2777','#65a30d','#ea580c'];

interface Slice { name: string; value: number; }
interface Props { data: Slice[]; title: string; }

export default function DonutChart({ data, title }: Props) {
  const total = data.reduce((s, d) => s + d.value, 0);

  return (
    <Card>
      <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-4">{title}</h3>
      <ResponsiveContainer width="100%" height={180}>
        <PieChart>
          <Pie data={data} dataKey="value" nameKey="name" cx="50%" cy="50%"
            outerRadius={75} innerRadius={46} paddingAngle={2} strokeWidth={0}>
            {data.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
          </Pie>
          <Tooltip
            contentStyle={{ background: '#fff', border: '1px solid #e2e8f0', borderRadius: 8, fontSize: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }}
            formatter={(val) => [formatCurrency(Number(val)), '']}
          />
        </PieChart>
      </ResponsiveContainer>
      <div className="flex flex-col gap-1.5 mt-2">
        {data.slice(0, 6).map((d, i) => (
          <div key={d.name} className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="w-2 h-2 rounded-sm shrink-0" style={{ background: COLORS[i % COLORS.length] }} />
              <span className="text-xs text-slate-500 truncate max-w-[120px]">{d.name}</span>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono font-semibold text-slate-700">{formatCurrency(d.value)}</span>
              <span className="text-xs text-slate-400">{formatPercent(total > 0 ? (d.value / total) * 100 : 0, 0)}</span>
            </div>
          </div>
        ))}
      </div>
    </Card>
  );
}