import type { ReactNode } from 'react';
import Spinner from './Spinner';
 
export interface Column<T> {
  key: string;
  header: string;
  align?: 'left' | 'right' | 'center';
  width?: string;
  render: (row: T) => ReactNode;
}
 
interface DataTableProps<T> {
  columns: Column<T>[];
  rows: T[];
  rowKey: (row: T) => string | number;
  loading?: boolean;
  emptyMessage?: string;
  emptyIcon?: string;
  footer?: ReactNode;
}
 
const alignClass = { left: 'text-left', right: 'text-right', center: 'text-center' };
 
export default function DataTable<T>({
  columns, rows, rowKey, loading = false,
  emptyMessage = 'No data yet.', emptyIcon = '📭', footer,
}: DataTableProps<T>) {
  if (loading) return <Spinner />;
 
  return (
    <div className="overflow-x-auto">
      <table className="w-full border-collapse">
        <thead>
          <tr className="border-b border-slate-100 bg-slate-50">
            {columns.map((col) => (
              <th
                key={col.key}
                style={{ width: col.width }}
                className={`px-4 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider ${alignClass[col.align ?? 'left']}`}
              >
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="py-12 text-center text-slate-400">
                <div className="text-2xl mb-2">{emptyIcon}</div>
                <div className="text-sm">{emptyMessage}</div>
              </td>
            </tr>
          ) : (
            rows.map((row) => (
              <tr
                key={rowKey(row)}
                className="border-b border-slate-100 hover:bg-slate-50 transition-colors"
              >
                {columns.map((col) => (
                  <td key={col.key} className={`px-4 py-3 text-sm ${alignClass[col.align ?? 'left']}`}>
                    {col.render(row)}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
      {footer && rows.length > 0 && (
        <div className="px-4 py-3 border-t border-slate-100 bg-slate-50">
          {footer}
        </div>
      )}
    </div>
  );
}
 
export function MonoValue({ value, className = '' }: { value: string; className?: string }) {
  return <span className={`font-mono font-semibold tabular-nums ${className}`}>{value}</span>;
}
 
export function ActionButtons({ onEdit, onDelete }: { onEdit: () => void; onDelete: () => void }) {
  return (
    <div className="flex items-center justify-end gap-1.5">
      <button
        onClick={onEdit}
        className="px-2.5 py-1 text-xs font-medium text-slate-600 bg-white hover:bg-slate-50 border border-slate-200 rounded-md transition-colors"
      >
        Edit
      </button>
      <button
        onClick={onDelete}
        className="px-2.5 py-1 text-xs font-medium text-red-600 bg-red-50 hover:bg-red-100 border border-red-200 rounded-md transition-colors"
      >
        Delete
      </button>
    </div>
  );
}