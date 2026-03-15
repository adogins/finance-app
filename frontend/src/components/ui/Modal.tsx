import { useEffect, type ReactNode } from 'react';
import Button from './Button';
 
interface ModalProps {
  title: string;
  onClose: () => void;
  onConfirm?: () => void;
  confirmLabel?: string;
  confirmLoading?: boolean;
  children: ReactNode;
  size?: 'sm' | 'md' | 'lg';
}
 
const sizes = { sm: 'max-w-sm', md: 'max-w-md', lg: 'max-w-lg' };
 
export default function Modal({
  title, onClose, onConfirm, confirmLabel = 'Save',
  confirmLoading = false, children, size = 'md',
}: ModalProps) {
  useEffect(() => {
    const handler = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [onClose]);
 
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/30 backdrop-blur-sm"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div className={`w-full ${sizes[size]} bg-white border border-slate-200 rounded-2xl shadow-xl`}>
        <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100">
          <h2 className="text-sm font-semibold text-slate-800">{title}</h2>
          <button
            onClick={onClose}
            className="w-7 h-7 rounded-lg bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-400 hover:text-slate-600 transition-colors"
          >
            ×
          </button>
        </div>
        <div className="px-5 py-4">{children}</div>
        {onConfirm && (
          <div className="flex items-center justify-end gap-2 px-5 py-3 border-t border-slate-100">
            <Button variant="secondary" size="sm" onClick={onClose}>Cancel</Button>
            <Button size="sm" onClick={onConfirm} loading={confirmLoading}>{confirmLabel}</Button>
          </div>
        )}
      </div>
    </div>
  );
}