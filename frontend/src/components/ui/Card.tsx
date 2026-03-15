import type { ReactNode } from 'react';

interface CardProps {
    children: ReactNode;
    className?: string;
    noPad?: boolean;
}

export default function Card({ children, className = '', noPad = false }: CardProps) {
    return (
        <div className={`bg-white border border-slate-200 rounded-xl ${noPad ? '' : 'p-5'} ${className}`}>{children}</div>
    );
}