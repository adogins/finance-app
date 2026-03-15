import { useApp } from '../../context/AppContext';
import type { Toast as ToastItem } from '../../types';
 
const styles = {
  success: 'bg-emerald-50 border-emerald-200 text-emerald-800',
  error:   'bg-red-50 border-red-200 text-red-800',
  info:    'bg-blue-50 border-blue-200 text-blue-800',
};
 
const icons = { success: '✓', error: '✕', info: 'ℹ' };
 
function ToastItem({ toast, onClose }: { toast: ToastItem; onClose: () => void }) {
  return (
    <div className={`flex items-center gap-3 px-4 py-3 rounded-xl border text-sm font-medium shadow-lg ${styles[toast.type]}`}>
      <span className="font-bold shrink-0">{icons[toast.type]}</span>
      <span className="flex-1">{toast.message}</span>
      <button onClick={onClose} className="shrink-0 opacity-50 hover:opacity-100 transition-opacity text-base leading-none">×</button>
    </div>
  );
}
 
export default function ToastContainer() {
  const { toasts, removeToast } = useApp();
  if (!toasts.length) return null;
  return (
    <div className="fixed bottom-5 right-5 z-[100] flex flex-col gap-2 w-80">
      {toasts.map((t) => (
        <ToastItem key={t.id} toast={t} onClose={() => removeToast(t.id)} />
      ))}
    </div>
  );
}