import { useState, useEffect, useCallback } from 'react';
import { retirementApi } from '../api/client';
import { useApp } from '../context/AppContext';
import { formatCurrency } from '../utils/format';
import PageHeader from '../components/ui/PageHeader';
import StatCard from '../components/ui/StatCard';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import Modal from '../components/ui/Modal';
import Spinner from '../components/ui/Spinner';
import DataTable, { MonoValue, ActionButtons, type Column } from '../components/ui/DataTable';
import { FormField, Input } from '../components/ui/FormField';
import {
  ResponsiveContainer, AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip,
} from 'recharts';
import { formatCurrencyCompact } from '../utils/format';
import type { RetirementAccountResponse, RetirementAccountRequest, ProjectionSummary } from '../types';

const blank = (): RetirementAccountRequest => ({
  name: '', provider: null, balance: 0, monthlyContribution: 0, employerMatch: null, expectedReturnRate: null,
});

export default function Retirement() {
  const { userId, addToast } = useApp();
  const [accounts, setAccounts] = useState<RetirementAccountResponse[]>([]);
  const [projection, setProjection] = useState<ProjectionSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [projectionLoading, setProjectionLoading] = useState(false);
  const [retirementAge, setRetirementAge] = useState(65);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<RetirementAccountResponse | null>(null);
  const [form, setForm] = useState<RetirementAccountRequest>(blank());
  const [saving, setSaving] = useState(false);

  const loadAccounts = useCallback(async () => {
    setLoading(true);
    try { setAccounts(await retirementApi.getAll(userId)); }
    catch (e: any) { addToast(e.message, 'error'); }
    finally { setLoading(false); }
  }, [userId, addToast]);

  const loadProjection = useCallback(async () => {
    setProjectionLoading(true);
    try { setProjection(await retirementApi.getProjection(userId, retirementAge)); }
    catch (e: any) { addToast(e.message, 'error'); }
    finally { setProjectionLoading(false); }
  }, [userId, retirementAge, addToast]);

  useEffect(() => { loadAccounts(); }, [loadAccounts]);
  useEffect(() => { loadProjection(); }, [loadProjection]);

  const openAdd = () => { setEditing(null); setForm(blank()); setModalOpen(true); };
  const openEdit = (acc: RetirementAccountResponse) => {
    setEditing(acc);
    setForm({ name: acc.name, provider: acc.provider, balance: acc.balance, monthlyContribution: acc.monthlyContribution, employerMatch: acc.employerMatch, expectedReturnRate: acc.expectedReturnRate });
    setModalOpen(true);
  };

  const handleSave = async () => {
    if (!form.name.trim()) return addToast('Name is required', 'error');
    if (form.balance < 0) return addToast('Balance cannot be negative', 'error');
    setSaving(true);
    try {
      if (editing) await retirementApi.update(userId, editing.id, form);
      else await retirementApi.create(userId, form);
      addToast(editing ? 'Account updated' : 'Account added');
      setModalOpen(false);
      loadAccounts();
      loadProjection();
    } catch (e: any) { addToast(e.message, 'error'); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this retirement account?')) return;
    try { await retirementApi.delete(userId, id); addToast('Deleted'); loadAccounts(); loadProjection(); }
    catch (e: any) { addToast(e.message, 'error'); }
  };

  // breakdown data for chart (every Nth point for readbility)
  const allBreakdown = projection?.accounts.flatMap((a) => a.yearlyBreakdown) ?? [];
  const combined = allBreakdown.reduce<Record<number, number>>((acc, yv) => {
    acc[yv.year] = (acc[yv.year] ?? 0) + yv.balance;
    return acc;
  }, {});

  const step = Math.max(1, Math.ceil(Object.keys(combined).length / 25));
  const chartData = Object.entries(combined)
    .filter((_, i) => i % step === 0 || i === Object.keys(combined).length - 1)
    .map(([year, balance]) => ({ year: `Yr ${year}`, balance }));
 
  const columns: Column<RetirementAccountResponse>[] = [
    {
      key: 'name', header: 'Account',
      render: (r) => (
        <div>
          <div className="text-slate-700 font-medium">{r.name}</div>
          {r.provider && <div className="text-xs text-slate-500 mt-0.5">{r.provider}</div>}
        </div>
      ),
    },
    { key: 'balance', header: 'Balance', align: 'right', render: (r) => <MonoValue value={formatCurrency(r.balance)} className="text-blue-600" /> },
    {
      key: 'contrib', header: 'Monthly', align: 'right',
      render: (r) => (
        <div className="text-right">
          <div className="font-mono font-semibold text-slate-700 text-sm">{formatCurrency(r.totalMonthlyContribution)}</div>
          {r.employerMatch != null && r.employerMatch > 0 && (
            <div className="text-xs text-emerald-600">+{formatCurrency(r.employerMatch)} match</div>
          )}
        </div>
      ),
    },
    { key: 'rate', header: 'Return', align: 'right', render: (r) => <span className="text-slate-400 text-sm">{r.expectedReturnRate ?? 7.0}%</span> },
    { key: 'actions', header: '', width: '120px', render: (r) => <ActionButtons onEdit={() => openEdit(r)} onDelete={() => handleDelete(r.id)} /> },
  ];

  return (
    <div className="p-6 flex flex-col gap-6">
      <PageHeader
        title="Retirement"
        subtitle="Project your savings and plan for financial independence"
        action={<Button onClick={openAdd}>+ Add Account</Button>}
      />
 
      {/* Projection summary */}
      {projection && projection.yearsToRetirement > 0 && (
        <div className="grid grid-cols-4 gap-4">
          <StatCard label="Projected Balance"  value={formatCurrency(projection.totalProjectedBalance)} accent="blue" icon="◎" />
          <StatCard label="Monthly Income"     value={formatCurrency(projection.estimatedMonthlyIncome)} icon="💰" />
          <StatCard label="Years to Retire"    value={`${projection.yearsToRetirement} yrs`} icon="⏱" />
          <StatCard label="Retirement Age"     value={`Age ${projection.retirementAge}`} icon="🎯" />
        </div>
      )}
 
      {projection && projection.yearsToRetirement === 0 && (
        <Card className="text-center py-6">
          <p className="text-slate-600 font-semibold mb-1">You've reached retirement age!</p>
          <p className="text-slate-400 text-sm">Current balance: <span className="text-blue-600 font-mono font-bold">{formatCurrency(accounts.reduce((s, a) => s + Number(a.balance), 0))}</span></p>
        </Card>
      )}
 
      {/* Projection chart with age slider */}
      <Card>
        <div className="flex items-center justify-between mb-5">
          <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Projected Growth</h3>
          <div className="flex items-center gap-3">
            <span className="text-xs text-slate-500">Retire at age:</span>
            <input
              type="range" min={50} max={80} value={retirementAge}
              onChange={(e) => setRetirementAge(Number(e.target.value))}
              className="w-28 accent-emerald-500"
            />
            <span className="text-sm font-bold text-blue-600 font-mono w-6">{retirementAge}</span>
          </div>
        </div>
        {projectionLoading ? (
          <Spinner />
        ) : chartData.length > 0 ? (
          <ResponsiveContainer width="100%" height={220}>
            <AreaChart data={chartData} margin={{ top: 4, right: 4, left: 0, bottom: 0 }}>
              <defs>
                <linearGradient id="gRetire" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#3b82f6" stopOpacity={0.3} />
                  <stop offset="100%" stopColor="#3b82f6" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} />
              <XAxis dataKey="year" tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fill: '#64748b', fontSize: 11 }} axisLine={false} tickLine={false} tickFormatter={formatCurrencyCompact} width={70} />
              <Tooltip
                contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8, fontSize: 12 }}
                formatter={(val) => [formatCurrency(Number(val)), 'Balance']}
              />
              <Area type="monotone" dataKey="balance" name="Balance" stroke="#3b82f6" fill="url(#gRetire)" strokeWidth={2.5} dot={false} />
            </AreaChart>
          </ResponsiveContainer>
        ) : (
          <div className="text-center py-10 text-slate-500 text-sm">Add a retirement account to see projections.</div>
        )}
      </Card>
 
      {/* Accounts */}
      <Card noPad>
        <div className="px-5 py-4 border-b border-slate-200">
          <h2 className="text-sm font-semibold text-slate-700">Accounts</h2>
        </div>
        <DataTable
          columns={columns} rows={accounts} rowKey={(r) => r.id} loading={loading}
          emptyIcon="$" emptyMessage="No retirement accounts yet."
          footer={
            <div className="flex justify-between items-center">
              <span className="text-xs font-semibold text-slate-400">Total Balance</span>
              <MonoValue value={formatCurrency(accounts.reduce((s, a) => s + Number(a.balance), 0))} className="text-blue-600 text-sm" />
            </div>
          }
        />
      </Card>
 
      {modalOpen && (
        <Modal title={editing ? 'Edit Account' : 'Add Retirement Account'} onClose={() => setModalOpen(false)} onConfirm={handleSave} confirmLabel={editing ? 'Update' : 'Add'} confirmLoading={saving} size="lg">
          <FormField label="Account Name"><Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="401(k) — Fidelity" /></FormField>
          <FormField label="Provider (optional)"><Input value={form.provider ?? ''} onChange={(e) => setForm({ ...form, provider: e.target.value || null })} placeholder="Fidelity, Vanguard…" /></FormField>
          <FormField label="Current Balance"><Input type="number" min="0" step="0.01" value={form.balance || ''} onChange={(e) => setForm({ ...form, balance: parseFloat(e.target.value) || 0 })} placeholder="0.00" /></FormField>
          <FormField label="Your Monthly Contribution"><Input type="number" min="0" step="0.01" value={form.monthlyContribution || ''} onChange={(e) => setForm({ ...form, monthlyContribution: parseFloat(e.target.value) || 0 })} placeholder="0.00" /></FormField>
          <FormField label="Employer Match (optional)"><Input type="number" min="0" step="0.01" value={form.employerMatch ?? ''} onChange={(e) => setForm({ ...form, employerMatch: e.target.value ? parseFloat(e.target.value) : null })} placeholder="0.00" /></FormField>
          <FormField label="Expected Annual Return %" hint="Defaults to 7% if left blank"><Input type="number" min="0" max="30" step="0.1" value={form.expectedReturnRate ?? ''} onChange={(e) => setForm({ ...form, expectedReturnRate: e.target.value ? parseFloat(e.target.value) : null })} placeholder="7.0" /></FormField>
        </Modal>
      )}
    </div>
  );
}