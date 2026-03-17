import { useState, useEffect, useCallback } from 'react';
import { expensesApi } from '../api/client';
import { useApp } from '../context/AppContext';
import { formatCurrency, formatDate, todayISO } from '../utils/format';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import Badge, { expenseCategoryBadge } from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import DataTable, { MonoValue, ActionButtons, type Column } from '../components/ui/DataTable';
import { FormField, Input, Select, Textarea } from '../components/ui/FormField';
import DonutChart from '../components/charts/DonutChart';
import type { ExpenseResponse, ExpenseRequest } from '../types';

const CATEGORIES = ['Hosuing', 'Food', 'Transport', 'Healthcare', 'Entertainment', 'Utilities', 'Other'];
const CATEGORY_COLORS: Record<string, string> = {
    Housing: '#3b82f6', Food: '#10b981', Transport: '#f97316', Healthcare: '#8b5cf6', Entertainment: '#06b6d4', Utilities: '#f59e0b',
    Other: '#64748b',};

const blank = (): ExpenseRequest => ({ amount: 0, category: 'Housing', description: null, spentAt: todayISO() });

export default function Expenses() {
    const { userId, addToast } = useApp();
    const [items, setItems] = useState<ExpenseResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [filterCategory, setFilterCategory] = useState('');
    const [modalOpen, setModalOpen] = useState(false);
    const [editing, setEditing] = useState<ExpenseResponse | null>(null);
    const [form, setForm] = useState<ExpenseRequest>(blank());
    const [saving, setSaving] = useState(false);

    const load = useCallback(async () => {
        setLoading(true);
        try { setItems(await expensesApi.getAll(userId, filterCategory || undefined)); }
        catch (e: any) { addToast(e.message, 'error'); }
        finally { setLoading(false); }
    }, [userId, filterCategory, addToast]);

    useEffect(() => { load(); }, [load]);

    const openAdd = () => { setEditing(null); setForm(blank()); setModalOpen(true); };
    const openEdit = (item: ExpenseResponse) => {
        setEditing(item);
        setForm({ amount: item.amount, category: item.category, description: item.description, spentAt: item.spentAt});
        setModalOpen(true);
    };

    const handleSave = async () => {
        if (!form.amount || form.amount <= 0) return addToast('Amount must be greater than 0', 'error');
        if (!form.spentAt) return addToast('Date is required', 'error');
        setSaving(true);
        try {
            if (editing) await expensesApi.update(userId, editing.id, form);
            else await expensesApi.create(userId, form);
            addToast(editing ? 'Expense updated' : 'Expense added');
            setModalOpen(false);
            load();
        } catch (e: any) { addToast(e.message, 'error'); }
        finally { setSaving(false); }
    };

    const handleDelete = async (id: number) => {
        if (!confirm('Delete this expense?')) return;
        try { await expensesApi.delete(userId, id); addToast('Deleted'); load(); }
        catch (e: any) { addToast(e.message, 'error'); }
    };

    const total = items.reduce((s, i) => s + Number(i.amount), 0);

    const byCategory = CATEGORIES
        .map((cat) => ({
            name: cat,
            value: items.filter((i) => i.category === cat).reduce((s, i) => s + Number(i.amount), 0),
        }))
        .filter((d) => d.value > 0)
        .sort((a, b) => b.value - a.value);

    const columns: Column<ExpenseResponse>[] = [
        {
            key: 'date', header: 'Date', width: '120px',
            render: (r) => <span className='text-slate-400 text-sm'>{formatDate(r.spentAt)}</span>,
        },
        {
            key: 'category', header: 'Category', width: '140px',
            render: (r) => <Badge variant={expenseCategoryBadge[r.category] ?? 'gray'}>{r.category}</Badge>,
        },
        {
            key: 'description', header: 'Description',
            render: (r) => <span className='text-slate-400'>{r.description || '-'}</span>,
        },
        {
            key: 'amount', header: 'Amount', align: 'right',
            render: (r) => <MonoValue value={formatCurrency(r.amount)} className='text-red-600' />,
        },
        {
            key: 'actions', header: '', width: '120px',
            render: (r) => <ActionButtons onEdit={() => openEdit(r)} onDelete={() => handleDelete(r.id)} />,
        },
    ];

    return (
    <div className='p-6 flex flex-col gap-6'>
      <PageHeader
        title='Expenses'
        subtitle={`${items.length} entries · ${formatCurrency(total)} total`}
        action={
          <div className='flex items-center gap-2'>
            <Select
              value={filterCategory}
              onChange={(e) => setFilterCategory(e.target.value)}
              className='w-44 py-1.5 text-xs'
            >
              <option value=''>All Categories</option>
              {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
            </Select>
            <Button onClick={openAdd}>+ Add Expense</Button>
          </div>
        }
      />
 
      <div className={`grid gap-5 ${byCategory.length > 0 ? 'grid-cols-[1fr_300px]' : 'grid-cols-1'}`}>
        <Card noPad>
          <DataTable
            columns={columns}
            rows={items}
            rowKey={(r) => r.id}
            loading={loading}
            emptyIcon='🧾'
            emptyMessage='No expenses yet.'
            footer={
              <div className='flex justify-between items-center'>
                <span className='text-xs font-semibold text-slate-400'>Total Expenses</span>
                <MonoValue value={formatCurrency(total)} className='text-red-600 text-sm' />
              </div>
            }
          />
        </Card>
 
        {byCategory.length > 0 && (
          <div className='flex flex-col gap-4'>
            <DonutChart data={byCategory} title='By Category' />
            {/* Category breakdown bars */}
            <Card>
              <h3 className='text-xs font-semibold text-slate-400 uppercase tracking-wider mb-4'>Breakdown</h3>
              <div className='flex flex-col gap-3'>
                {byCategory.map((d) => (
                  <div key={d.name}>
                    <div className='flex justify-between text-xs mb-1'>
                      <span className='text-slate-400'>{d.name}</span>
                      <span className='font-mono font-semibold text-slate-700'>{formatCurrency(d.value)}</span>
                    </div>
                    <div className='h-1.5 bg-slate-100 rounded-full overflow-hidden'>
                      <div
                        className='h-full rounded-full'
                        style={{
                          width: `${(d.value / total) * 100}%`,
                          background: CATEGORY_COLORS[d.name] ?? '#64748b',
                        }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          </div>
        )}
      </div>
 
      {modalOpen && (
        <Modal
          title={editing ? 'Edit Expense' : 'Add Expense'}
          onClose={() => setModalOpen(false)}
          onConfirm={handleSave}
          confirmLabel={editing ? 'Update' : 'Add'}
          confirmLoading={saving}
        >
          <FormField label='Category'>
            <Select
              value={form.category}
              onChange={(e) => setForm({ ...form, category: e.target.value })}
            >
              {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
            </Select>
          </FormField>
          <FormField label='Amount'>
            <Input
              type='number'
              min='0.01'
              step='0.01'
              value={form.amount || ''}
              onChange={(e) => setForm({ ...form, amount: parseFloat(e.target.value) || 0 })}
              placeholder='0.00'
            />
          </FormField>
          <FormField label='Description (optional)'>
            <Textarea
              value={form.description ?? ''}
              onChange={(e) => setForm({ ...form, description: e.target.value || null })}
              placeholder='What was this for?'
              rows={2}
            />
          </FormField>
          <FormField label='Date'>
            <Input
              type='date'
              value={form.spentAt}
              onChange={(e) => setForm({ ...form, spentAt: e.target.value })}
            />
          </FormField>
        </Modal>
      )}
    </div>
  );
}