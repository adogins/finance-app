import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import type { UserResponse, Toast, ToastType } from '../types';

interface AppContextValue {
  userId: number;
  user: UserResponse;
  toasts: Toast[];
  addToast: (message: string, type?: ToastType) => void;
  removeToast: (id: number) => void;
}

const AppContext = createContext<AppContextValue | null>(null);

let toastId = 0;

export function AppProvider({ user, children }: { user: UserResponse; children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const addToast = useCallback((message: string, type: ToastType = 'success') => {
    const id = ++toastId;
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 4000);
  }, []);

  const removeToast = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return (
    <AppContext.Provider value={{ userId: user.id, user, toasts, addToast, removeToast }}>
      {children}
    </AppContext.Provider>
  );
}

export function useApp() {
  const ctx = useContext(AppContext);
  if (!ctx) throw new Error('useApp must be used within AppProvider');
  return ctx;
}