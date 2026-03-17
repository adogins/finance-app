import { useState, useEffect, useCallback } from 'react';
import { allocationsApi } from '../api/client';
import { useApp } from '../context/AppContext';
import { formatCurrency, formatPercent } from '../utils/format';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import Badge from '../components/ui/Badge';
import Modal from '../components/ui/Modal';
import DataTable, { MonoValue, ActionButtons, type Column } from '../components/ui/DataTable';
import { FormField, Input, Select } from '../components/ui/FormField';
import type { IncomeAllocationResponse, IncomeAllocationRequest, AllocationType } from '../types';

const blank = (): IncomeAllocationRequest => ({
    category: '', allocationType: 'PERCENT', allocationValue: 0, priority: 1,
});

export default function Allocations() {
    const { userId, addToast } = useApp();
    const [items, setItems] = useState<IncomeAllocationResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [modalOpen, setModalOpen] = useState(false);
    const [editing, setEditing] = useState<IncomeAllocationResponse | null>(null);
    const [form, setForm] = useState<IncomeAllocationRequest>(blank());
    const [saving, setSaving] = useState(false);

    const load = useCallback( async () => {
        setLoading(true);
        try { setItems(await allocationsApi.getAll(userId)); }
        catch (e: any) { addToast(e.message, 'error'); }
        finally { setLoading(false); }
    }, [userId, addToast]);

    useEffect(() => { load(); }, [load]);

    const openAdd = () => { setEditing(null); setForm(blank()); setModalOpen(true); };
    const openEdit = (item: IncomeAllocationResponse) => {
        setEditing(item);
        setForm({ category: item.category, allocationType: item.allocationType, allocationValue: item.allocationValue, priority: item.priority });
        setModalOpen(true);
    };

    const handleSave = async () => {
        if (!form.category.trim()) return addToast('Category is required', 'error');
        if (form.allocationValue <= 0) return addToast('Allocation value must me greater than 0', 'error');
        if (form.priority < 1) return addToast('Priority must be at least 1', 'error');

        setSaving(true);
        try {
            if (editing) await allocationsApi.update(userId, editing.id, form);
            else await allocationsApi.create(userId, form);
            addToast(editing ? 'Allocation updated' : 'Allocation added');
            setModalOpen(false);
            load();
        } catch (e: any) { addToast(e.message, 'error'); }
        finally { setSaving(false); }
    };

    const handleDelete = async (id: number) => {
        if (!confirm('Delete this allocation?')) return;
        try { await allocationsApi.delete(userId, id); addToast('Deleted'); load(); }
        catch (e: any) { addToast(e.message, 'error'); }
    };

    const sorted = [...items].sort((a, b) => a.priority - b.priority);
    const totalPercent = sorted.filter((i) => i.allocationType === 'PERCENT').reduce((s, i) => s + i.allocationValue, 0);
    const isBalanced = Math.abs(totalPercent - 100) < 0.01;

    const columns: Column<IncomeAllocationResponse>[] = [
        {
            key: 'priority', header: '#', width: '50px',
            render: (r) => <span className='font-mon text-slate-500 font-semibold'>{r.priority}</span>,
        },
        {
            key: 'category', header: 'Category',
            render: (r) => <span className='text-slate-700 font-medium'>{r.category}</span>,
        },
        {
            key: 'type', header: 'Type', width: '100px',
            render: (r) => <Badge variant={r.allocationType === 'PERCENT' ? 'blue' : 'green'}>{r.allocationType}</Badge>,
        },
        {
            key: 'value', header: 'Allocation', align: 'right',
            render: (r) => (
                <MonoValue 
                    value={r.allocationType === 'PERCENT' ? formatPercent(r.allocationValue) : formatCurrency(r.allocationValue)}
                    className='text-slate-700'
                />
            ),
        },
        {
            key: 'actions', header: '', width: '120px',
            render: (r) => <ActionButtons onEdit={() => openEdit(r)} onDelete={() => handleDelete(r.id)} />,
        },
    ];

    return (
    <div className='p-6 flex flex-col gap-6'>
      <PageHeader
        title='Income Allocations'
        subtitle='Define how your income is distributed each period'
        action={<Button onClick={openAdd}>+ Add Allocation</Button>}
      />
 
      {/* Percent balance indicator */}
      {totalPercent > 0 && (
        <div className={`flex items-center gap-3 px-4 py-3 rounded-xl border text-sm font-medium ${
          isBalanced
            ? 'bg-emerald-50 border-emerald-200 text-emerald-700'
            : 'bg-amber-50 border-amber-200 text-amber-700'
        }`}>
          <span>{isBalanced ? '✓' : '⚠'}</span>
          <span>
            {isBalanced
              ? 'Percentage allocations total exactly 100% — perfect!'
              : `Percentage allocations total ${formatPercent(totalPercent)} — ${
                  totalPercent < 100
                    ? `${formatPercent(100 - totalPercent)} unallocated`
                    : `${formatPercent(totalPercent - 100)} over-allocated`
                }`}
          </span>
        </div>
      )}
 
      <Card noPad>
        <DataTable
          columns={columns}
          rows={sorted}
          rowKey={(r) => r.id}
          loading={loading}
          emptyIcon='⊞'
          emptyMessage='No allocations yet. Add rules to split your income automatically.'
        />
      </Card>
 
      {modalOpen && (
        <Modal
          title={editing ? 'Edit Allocation' : 'Add Allocation'}
          onClose={() => setModalOpen(false)}
          onConfirm={handleSave}
          confirmLabel={editing ? 'Update' : 'Add'}
          confirmLoading={saving}
        >
          <FormField label='Category' hint='e.g. Savings, Rent, Investments, Emergency Fund'>
            <Input
              value={form.category}
              onChange={(e) => setForm({ ...form, category: e.target.value })}
              placeholder='Category name'
            />
          </FormField>
          <FormField label='Allocation Type'>
            <Select
              value={form.allocationType}
              onChange={(e) => setForm({ ...form, allocationType: e.target.value as AllocationType })}
            >
              <option value='PERCENT'>Percent (%) of income</option>
              <option value='FIXED'>Fixed dollar amount ($)</option>
            </Select>
          </FormField>
          <FormField
            label={form.allocationType === 'PERCENT' ? 'Percentage (e.g. 20 for 20%)' : 'Dollar Amount'}
            hint={form.allocationType === 'PERCENT' ? 'Enter a number between 0 and 100' : 'Fixed amount per period'}
          >
            <Input
              type='number'
              min='0'
              step={form.allocationType === 'PERCENT' ? '0.1' : '0.01'}
              value={form.allocationValue || ''}
              onChange={(e) => setForm({ ...form, allocationValue: parseFloat(e.target.value) || 0 })}
              placeholder={form.allocationType === 'PERCENT' ? '20' : '500.00'}
            />
          </FormField>
          <FormField label='Priority' hint='Lower number = processed first (1 is highest priority)'>
            <Input
              type='number'
              min='1'
              step='1'
              value={form.priority || ''}
              onChange={(e) => setForm({ ...form, priority: parseInt(e.target.value) || 1 })}
              placeholder="1"
            />
          </FormField>
        </Modal>
      )}
    </div>
  );
}