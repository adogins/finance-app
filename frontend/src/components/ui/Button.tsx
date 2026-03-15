import type { ButtonHTMLAttributes, ReactNode } from 'react';
 
type Variant = 'primary' | 'secondary' | 'danger' | 'ghost';
type Size = 'sm' | 'md' | 'lg';
 
interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: Size;
  children: ReactNode;
  loading?: boolean;
}
 
const variants: Record<Variant, string> = {
  primary:   'bg-emerald-600 hover:bg-emerald-700 text-white border-transparent shadow-sm',
  secondary: 'bg-white hover:bg-slate-50 text-slate-600 border-slate-200',
  danger:    'bg-red-50 hover:bg-red-100 text-red-600 border-red-200',
  ghost:     'bg-transparent hover:bg-slate-100 text-slate-600 border-transparent',
};
 
const sizes: Record<Size, string> = {
  sm: 'px-3 py-1.5 text-xs',
  md: 'px-4 py-2 text-sm',
  lg: 'px-5 py-2.5 text-sm',
};
 
export default function Button({
  variant = 'primary',
  size = 'md',
  children,
  loading = false,
  disabled,
  className = '',
  ...props
}: ButtonProps) {
  return (
    <button
      {...props}
      disabled={disabled || loading}
      className={`
        inline-flex items-center justify-center gap-2 font-semibold rounded-lg border
        transition-all duration-150 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed
        ${variants[variant]} ${sizes[size]} ${className}
      `}
    >
      {loading && (
        <span className="w-3.5 h-3.5 border-2 border-current border-t-transparent rounded-full animate-spin" />
      )}
      {children}
    </button>
  );
}