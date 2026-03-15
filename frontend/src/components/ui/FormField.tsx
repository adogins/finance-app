import type { InputHTMLAttributes, SelectHTMLAttributes, TextareaHTMLAttributes, ReactNode } from 'react';
 
const inputBase = `
  w-full bg-white border border-slate-200 rounded-lg px-3 py-2 text-sm text-slate-800
  placeholder:text-slate-400 focus:outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/10
  transition-colors
`;
 
export function Input({ className = '', ...props }: InputHTMLAttributes<HTMLInputElement> & { className?: string }) {
  return <input className={`${inputBase} ${className}`} {...props} />;
}
 
export function Select({
  children, className = '', ...props
}: SelectHTMLAttributes<HTMLSelectElement> & { className?: string }) {
  return (
    <select className={`${inputBase} cursor-pointer ${className}`} {...props}>
      {children}
    </select>
  );
}
 
export function Textarea({ className = '', ...props }: TextareaHTMLAttributes<HTMLTextAreaElement> & { className?: string }) {
  return <textarea className={`${inputBase} resize-none ${className}`} rows={3} {...props} />;
}
 
export function FormField({ label, children, hint }: { label: string; children: ReactNode; hint?: string }) {
  return (
    <div className="mb-4">
      <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
        {label}
      </label>
      {children}
      {hint && <p className="text-xs text-slate-400 mt-1">{hint}</p>}
    </div>
  );
}