import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import { usersApi } from '../api/client';
import type { UserResponse, Toast, ToastType } from '../types';

interface AppContextValue {
    userId: number;
    setUserId: (id: number) => void;
    user: UserResponse | null;
    userLoading: boolean;
    toasts: Toast[];
    addToast: (message: string, type?: ToastType) => void;
    removeToast: (id: number) => void;
}

const AppContext = createContext<AppContextValue | null>(null);

let toastId = 0;

export function AppProvider({ children }: { children: ReactNode }) {
    const [userId, setUserId] = useState(1);
    const [user, setUser] = useState<UserResponse | null>(null);
    const [userLoading, setUserLoading] = useState(false);
    const [toasts, setToasts] = useState<Toast[]>([]);

    useEffect(() => {
        setUserLoading(true);
        setUser(null);
        usersApi
            .getById(userId)
            .then(setUser)
            .catch(() => setUser(null))
            .finally(() => setUserLoading(false));
    }, [userId]);

    const addToast = useCallback((message: string, type: ToastType = 'success') => {
        const id  = ++toastId;
        setToasts((prev) => [...prev, { id, message, type }]);
        setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 4000);
    }, []);

    const removeToast = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
    }, []);

    return (
        <AppContext.Provider value={{ userId, setUserId, user, userLoading, toasts, addToast, removeToast}}>
            {children}
        </AppContext.Provider>
    );
}

export function useApp() {
    const ctx = useContext(AppContext);
    if (!ctx) throw new Error('useApp must be used within AppProvider');
    return ctx;
}