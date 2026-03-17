import { useState, useEffect, useCallback } from 'react';
import { balanceSheetApi } from '../api/client';
import { useApp } from '../context/AppContext';
import { formatCurrency, formatPercent } from '../utils/format';
import PageHeader from '../components/ui/PageHeader';
import StatCard from '../components/ui/StatCard';
import Card from '../components/ui/Card';
import Spinner from '../components/ui/Spinner';
import IncomeExpenseBar from '../components/charts/BarChart';
import type { MonthlyBalanceSheet, YearlyBalanceSheet } from '../types';

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
const NOW = new Date();
const CURRENT_YEAR = NOW.getFullYear();
const CURRENT_MONTH = NOW.getMonth() + 1;
const YEARS = Array.from({ length: 6 }, (_, i)=> CURRENT_YEAR - 2 + i);

export default function BalanceSheet() {
    const { userId, addToast } = useApp();
    const [tab, setTab] = useState<'monthly' | 'yearly'>('monthly');
    const [year, setYear] = useState(CURRENT_YEAR);
    const [month, setMonth] = useState(CURRENT_MONTH);
    const [monthly, setMonthly] = useState<MonthlyBalanceSheet | null>(null);
    const [yearly, setYearly] = useState<YearlyBalanceSheet | null>(null);
    const [loading, setLoading] = useState(true);

    const load = useCallback( async () => {
        setLoading(true);
        try {
            if (tab === 'monthly') setMonthly(await balanceSheetApi.getMonthly(userId, year, month));
            else setYearly(await balanceSheetApi.getYearly(userId, year));
        } catch (e: any) {
            addToast(e.message, 'error');
        } finally {
            setLoading(false);
        }
    }, [userId, tab, year, month, addToast]);

    useEffect(() => { load(); }, [load]);

    return (
    <div className='p-6 flex flex-col gap-6'>
      <PageHeader
        title='Balance Sheet'
        subtitle='Cash flow and net worth changes over time'
        action={
          <div className='flex items-center gap-2'>
            <div className='flex bg-white border border-slate-200 rounded-lg p-0.5'>
              {(['monthly', 'yearly'] as const).map((t) => (
                <button
                  key={t}
                  onClick={() => setTab(t)}
                  className={`px-4 py-1.5 rounded-md text-xs font-semibold transition-all capitalize ${
                    tab === t ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-slate-700'
                  }`}
                >
                  {t}
                </button>
              ))}
            </div>
 
            {tab === 'monthly' && (
              <select
                value={month}
                onChange={(e) => setMonth(Number(e.target.value))}
                className='bg-white border border-slate-200 rounded-lg px-3 py-1.5 text-xs text-slate-600 focus:outline-none focus:border-blue-500'
              >
                {MONTHS.map((m, i) => <option key={i} value={i + 1}>{m}</option>)}
              </select>
            )}
            <select
              value={year}
              onChange={(e) => setYear(Number(e.target.value))}
              className='bg-white border border-slate-200 rounded-lg px-3 py-1.5 text-xs text-slate-600 focus:outline-none focus:border-blue-500'
            >
              {YEARS.map((y) => <option key={y} value={y}>{y}</option>)}
            </select>
          </div>
        }
      />
 
      {loading ? <Spinner /> : tab === 'monthly' ? (
        <MonthlyView data={monthly} />
      ) : (
        <YearlyView data={yearly} />
      )}
    </div>
  );
}
 
// Monthly View
function MonthlyView({ data }: { data: MonthlyBalanceSheet | null }) {
  if (!data) return (
    <Card className='text-center py-12 text-slate-500'>No data for this period.</Card>
  );
 
  const catData = [...(data.expensesByCategory ?? [])]
    .sort((a, b) => b.amount - a.amount)
    .filter((c) => c.amount > 0);
 
  return (
    <div className='flex flex-col gap-5'>
      <div className='grid grid-cols-4 gap-4'>
        <StatCard label='Total Income'   value={formatCurrency(data.totalIncome)}   accent='green' icon='↑' />
        <StatCard label='Total Expenses' value={formatCurrency(data.totalExpenses)} accent='red'   icon='↓' />
        <StatCard label='Net Cash Flow'  value={formatCurrency(data.netCashFlow)}   accent={data.netCashFlow >= 0 ? 'green' : 'red'} icon='⇄' />
        <StatCard label='Savings Rate'   value={formatPercent(data.savingsRate)}    accent={data.savingsRate >= 20 ? 'green' : data.savingsRate >= 10 ? 'yellow' : 'red'} icon='%' />
      </div>
 
      <div className='grid grid-cols-2 gap-5'>
        {catData.length > 0 && (
          <Card>
            <h3 className='text-xs font-semibold text-slate-400 uppercase tracking-wider mb-4'>Expenses by Category</h3>
            <div className='flex flex-col gap-3'>
              {catData.map((c) => (
                <div key={c.category}>
                  <div className='flex justify-between text-xs mb-1'>
                    <span className='text-slate-400'>{c.category}</span>
                    <span className='font-mono font-semibold text-slate-700'>{formatCurrency(c.amount)}</span>
                  </div>
                  <div className='h-1.5 bg-slate-100 rounded-full overflow-hidden'>
                    <div
                      className='h-full bg-blue-500 rounded-full'
                      style={{ width: `${(c.amount / data.totalExpenses) * 100}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </Card>
        )}
 
        <Card>
          <h3 className='text-xs font-semibold text-slate-400 uppercase tracking-wider mb-4'>Net Worth Movement</h3>
          <NWRow label='Opening Net Worth' value={data.openingNetWorth} />
          <div className='h-px bg-slate-100 my-2' />
          <div className='py-2 flex justify-between items-center'>
            <span className='text-sm text-slate-400'>Net cash flow</span>
            <span className={`font-mono font-bold text-sm ${data.netCashFlow >= 0 ? 'text-emerald-600' : 'text-red-600'}`}>
              {data.netCashFlow >= 0 ? '+' : ''}{formatCurrency(data.netCashFlow)}
            </span>
          </div>
          <div className='h-px bg-slate-100 my-2' />
          <NWRow label='Closing Net Worth' value={data.closingNetWorth} bold />
        </Card>
      </div>
    </div>
  );
}
 
// Yearly View
function YearlyView({ data }: { data: YearlyBalanceSheet | null }) {
  if (!data) return (
    <Card className='text-center py-12 text-slate-500'>No data for this year.</Card>
  );
 
  const barData = (data.months ?? []).map((m, i) => ({
    label: MONTHS[i],
    income: Number(m.totalIncome),
    expenses: Number(m.totalExpenses),
  }));
 
  return (
    <div className='flex flex-col gap-5'>
      <div className='grid grid-cols-4 gap-4'>
        <StatCard label='Annual Income'    value={formatCurrency(data.annualIncome)}      accent='green' icon='↑' />
        <StatCard label='Annual Expenses'  value={formatCurrency(data.annualExpenses)}    accent='red'   icon='↓' />
        <StatCard label='Annual Cash Flow' value={formatCurrency(data.annualNetCashFlow)} accent={data.annualNetCashFlow >= 0 ? 'green' : 'red'} icon='⇄' />
        <StatCard label='Savings Rate'     value={formatPercent(data.annualSavingsRate)}  accent={data.annualSavingsRate >= 20 ? 'green' : 'yellow'} icon='%' />
      </div>
 
      {barData.some((d) => d.income > 0 || d.expenses > 0) && (
        <IncomeExpenseBar data={barData} />
      )}
 
      {/* Monthly table */}
      <Card noPad>
        <div className='px-5 py-4 border-b border-slate-200'>
          <h2 className='text-sm font-semibold text-slate-700'>Monthly Breakdown</h2>
        </div>
        <div className='overflow-x-auto'>
          <table className='w-full border-collapse'>
            <thead>
              <tr className='border-b border-slate-200 bg-white/50'>
                {['Month','Income','Expenses','Net Cash Flow','Savings Rate'].map((h, i) => (
                  <th key={h} className={`px-4 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider ${i === 0 ? 'text-left' : 'text-right'}`}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {(data.months ?? []).map((m, i) => (
                <tr key={i} className='border-b border-slate-200/50 hover:bg-slate-100/20'>
                  <td className='px-4 py-3 text-sm font-medium text-slate-600'>{MONTHS[i]}</td>
                  <td className='px-4 py-3 text-sm text-right font-mono font-semibold text-emerald-600'>{formatCurrency(m.totalIncome)}</td>
                  <td className='px-4 py-3 text-sm text-right font-mono font-semibold text-red-600'>{formatCurrency(m.totalExpenses)}</td>
                  <td className={`px-4 py-3 text-sm text-right font-mono font-bold ${m.netCashFlow >= 0 ? 'text-emerald-600' : 'text-red-600'}`}>
                    {m.netCashFlow >= 0 ? '+' : ''}{formatCurrency(m.netCashFlow)}
                  </td>
                  <td className='px-4 py-3 text-sm text-right text-slate-400'>{formatPercent(m.savingsRate)}</td>
                </tr>
              ))}
              <tr className='bg-white/50 border-t-2 border-slate-200'>
                <td className='px-4 py-3 text-sm font-bold text-slate-700'>Full Year</td>
                <td className='px-4 py-3 text-sm text-right font-mono font-bold text-emerald-600'>{formatCurrency(data.annualIncome)}</td>
                <td className='px-4 py-3 text-sm text-right font-mono font-bold text-red-600'>{formatCurrency(data.annualExpenses)}</td>
                <td className={`px-4 py-3 text-sm text-right font-mono font-bold ${data.annualNetCashFlow >= 0 ? 'text-emerald-600' : 'text-red-600'}`}>
                  {data.annualNetCashFlow >= 0 ? '+' : ''}{formatCurrency(data.annualNetCashFlow)}
                </td>
                <td className='px-4 py-3 text-sm text-right font-semibold text-slate-600'>{formatPercent(data.annualSavingsRate)}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </Card>
 
      {/* Net worth change */}
      {(data.openingNetWorth != null || data.closingNetWorth != null) && (
        <Card>
          <h3 className='text-xs font-semibold text-slate-400 uppercase tracking-wider mb-4'>Net Worth Change</h3>
          <NWRow label='Opening Net Worth (Jan 1)' value={data.openingNetWorth} />
          <div className='h-px bg-slate-100 my-2' />
          <NWRow label='Closing Net Worth (Dec 31)' value={data.closingNetWorth} bold />
          {data.netWorthChange != null && (
            <>
              <div className='h-px bg-slate-100 my-2' />
              <div className='flex justify-between items-center py-1'>
                <span className='text-sm text-slate-400'>Annual Change</span>
                <span className={`font-mono font-bold ${data.netWorthChange >= 0 ? 'text-emerald-600' : 'text-red-600'}`}>
                  {data.netWorthChange >= 0 ? '+' : ''}{formatCurrency(data.netWorthChange)}
                </span>
              </div>
            </>
          )}
        </Card>
      )}
    </div>
  );
}
 
function NWRow({ label, value, bold }: { label: string; value: number | null; bold?: boolean }) {
  return (
    <div className={`flex justify-between items-center py-2 ${bold ? '' : ''}`}>
      <span className={`text-sm ${bold ? 'text-slate-700 font-semibold' : 'text-slate-400'}`}>{label}</span>
      <span className={`font-mono font-${bold ? 'bold' : 'semibold'} text-${bold ? 'base' : 'sm'} ${value != null ? (value >= 0 ? 'text-emerald-600' : 'text-red-600') : 'text-slate-500'}`}>
        {value != null ? formatCurrency(value) : '—'}
      </span>
    </div>
  );
}