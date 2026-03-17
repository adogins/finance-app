import { useState, useEffect, useCallback } from 'react';
import { incomeApi } from '../api/client';
import { useApp } from '../context/AppContext';
import { formatCurrency, formatDate, todayISO } from '../utils/format';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import Modal from '../components/ui/Modal';
import DataTable, { MonoValue, ActionButtons, type Column } from '../components/ui/DataTable';
import { FormField, Input } from '../components/ui/FormField';
import DonutChart from '../components/charts/DonutChart';
import type { IncomeResponse, IncomeRequest } from '../types';

const blank = (): IncomeRequest => ({ amount: 0, source: '', receivedAt: todayISO() });

export default function Income() {
    const { userId, addToast } = useApp();
    const [items, setItems] = useState<IncomeResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [modalOpen, setModalOpen] = useState(false);
    const [editing, setEditing] = useState<IncomeResponse | null>(null);
    const [form, setForm] = useState<IncomeRequest>(blank());
    const [saving, setSaving] = useState(false);

    const load = useCallback(async () => {
        setLoading(true);
        try { setItems(await incomeApi.getAll(userId)); }
        catch (e: any) { addToast(e.message, 'error'); }
        finally { setLoading(false); }
    }, [userId, addToast]);

    useEffect(() => {load(); }, [load]);

    const openAdd = () => { setEditing(null); setForm(blank()); setModalOpen(true); };
    const openEdit = (item: IncomeResponse) => {
        setEditing(item);
        setForm({ amount: item.amount, source: item.source, receivedAt: item.receivedAt });
        setModalOpen(true);
    };

    const handleSave = async () => {
        if (!form.source.trim()) return addToast('Source is required', 'error');
        if (!form.amount || form.amount <= 0) return addToast('Amount must be greater than 0', 'error');
        if (!form.receivedAt) return addToast('Date is required', 'error');
        setSaving(true);
        try {
            if (editing) await incomeApi.update(userId, editing.id, form);
            else await incomeApi.create(userId, form);
            addToast(editing ? 'Income updated' : 'Income added');
            setModalOpen(false);
            load();
        } catch (e: any) { addToast(e.message, 'error'); }
        finally { setSaving(false); }
    };

    const handleDelete  = async (id: number) => {
        if (!confirm('Delete this income entry?')) return;
        try { await incomeApi.delete(userId, id); addToast('Deleted'); load(); }
        catch (e: any) { addToast(e.message, 'error'); }
    };

    const total = items.reduce((s, i) => s + Number(i.amount), 0);

    const bySource = Object.values(
        items.reduce<Record<string, { name: string; value: number }>>((acc, i) => {
        if (!acc[i.source]) acc[i.source] = { name: i.source, value: 0 };
        acc[i.source].value += Number(i.amount);
        return acc;
        }, {})
    );


    const columns: Column<IncomeResponse>[] = [
    {
      key: 'date', header: 'Date', width: '130px',
      render: (r) => <span className='text-slate-400 text-sm'>{formatDate(r.receivedAt)}</span>,
    },
    {
      key: 'source', header: 'Source',
      render: (r) => <span className='text-slate-700 font-medium'>{r.source}</span>,
    },
    {
      key: 'amount', header: 'Amount', align: 'right',
      render: (r) => <MonoValue value={formatCurrency(r.amount)} className='text-emerald-600' />,
    },
    {
      key: 'actions', header: '', width: '120px',
      render: (r) => <ActionButtons onEdit={() => openEdit(r)} onDelete={() => handleDelete(r.id)} />,
    },
  ];

  return (
    <div className='p-6 flex flex-col gap-6'>
      <PageHeader
        title='Income'
        subtitle={`${items.length} entries · ${formatCurrency(total)} total`}
        action={<Button onClick={openAdd}>+ Add Income</Button>}
      />
 
      <div className={`grid gap-5 ${bySource.length > 0 ? 'grid-cols-[1fr_300px]' : 'grid-cols-1'}`}>
        <Card noPad>
          <DataTable
            columns={columns}
            rows={items}
            rowKey={(r) => r.id}
            loading={loading}
            emptyIcon='💵'
            emptyMessage='No income recorded yet. Add your first entry.'
            footer={
              <div className='flex justify-between items-center'>
                <span className='text-xs font-semibold text-slate-400'>Total Income</span>
                <MonoValue value={formatCurrency(total)} className='text-emerald-600 text-sm' />
              </div>
            }
          />
        </Card>
 
        {bySource.length > 0 && <DonutChart data={bySource} title='By Source' />}
      </div>
 
      {modalOpen && (
        <Modal
          title={editing ? 'Edit Income' : 'Add Income'}
          onClose={() => setModalOpen(false)}
          onConfirm={handleSave}
          confirmLabel={editing ? 'Update' : 'Add'}
          confirmLoading={saving}
        >
          <FormField label='Source'>
            <Input
              value={form.source}
              onChange={(e) => setForm({ ...form, source: e.target.value })}
              placeholder='Salary, Freelance, Dividends…'
            />
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
          <FormField label='Date Received'>
            <Input
              type='date'
              value={form.receivedAt}
              onChange={(e) => setForm({ ...form, receivedAt: e.target.value })}
            />
          </FormField>
        </Modal>
      )}
    </div>
  );
}