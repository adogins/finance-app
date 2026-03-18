import { createContext, useContext, useState, useCallback, type ReactNode } from "react";
import type { UserResponse } from "../types";

interface AuthContextValue {
    currentUser: UserResponse | null;
    signIn: (user: UserResponse) => void;
    signOut: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

const STORAGE_KEY = 'finace_app_user';

function loadUser(): UserResponse | null {
    try {
        const raw = localStorage.getItem(STORAGE_KEY);
        return raw ? (JSON.parse(raw) as UserResponse) : null;
    } catch {
        return null;
    }
}

export function AuthProvider({ children }: { children: ReactNode }) {
    const [currentUser, setCurrentUser] = useState<UserResponse | null>(loadUser);

    const signIn = useCallback((user: UserResponse) => {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
        setCurrentUser(user);
    }, []);

    const signOut = useCallback(() => {
        localStorage.removeItem(STORAGE_KEY);
        setCurrentUser(null);
    }, []);

    return (
        <AuthContext.Provider value={{ currentUser, signIn, signOut }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth must be used within AuthProvider');
    return ctx;
}