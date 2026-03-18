import { useState } from 'react';
import { usersApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { FormField, Input } from '../components/ui/FormField';
import Button from '../components/ui/Button';
import type { UserResponse } from '../types';

type Tab = 'signin' | 'signup';

// Sign In Form
function SignInForm({ onSuccess }: { onSuccess: (user: UserResponse) => void }) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async () => {
        setError('');
        if (!email.trim()) return setError('Email is required.');
        if (!password) return setError('Password is required.');

        setLoading(true);
        try {
            const user = await usersApi.login({ email: email.trim(), password });
            onSuccess(user);
        } catch (e: any) {
            setError(e.message ?? 'Invalid email or password.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <FormField label='Email address'>
                <Input
                type='email'
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder='user@example.com'
                autoFocus
                />
            </FormField>
            <FormField label='Password'>
                <Input
                type='password'
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
                placeholder='••••••••'
                />
            </FormField>

            {error && (
                <p className='text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2 mb-4'>
                    {error}
                </p>
            )}

            <Button onClick={handleSubmit} loading={loading} className='w-full justify-center mt-1'>
                Sign In
            </Button>
        </div>
    );
}

// Sign Up Form
function SignUpForm({ onSuccess }: {onSuccess: (user: UserResponse) => void }) {
    const [form, setForm] = useState({
        firstName: '',
        lastName: '',
        email: '',
        dateOfBirth: '',
        password: '',
        confirmPassword: '',
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const f = (field: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) => 
        setForm((prev) => ({ ...prev, [field]: e.target.value }));

    const handleSubmit = async () => {
        setError('');
        if (!form.firstName.trim()) return setError('First name is required.');
        if (!form.lastName.trim()) return setError('Last name is required.');
        if (!form.email.trim()) return setError('Email is required.');
        if (!form.dateOfBirth) return setError('Date of birth is required.');
        if (!form.password) return setError('Password is required.');
        if (form.password.length < 6) return setError('Password must be at least 6 characters,');
        if (form.password !== form.confirmPassword) return setError('Passwords do not match.');

        setLoading(true);
        try {
            const user = await usersApi.create({
                firstName: form.firstName.trim(),
                lastName: form.lastName.trim(),
                email: form.email.trim(),
                dateOfBirth: form.dateOfBirth,
                password: form.password,
            });
            onSuccess(user);
        } catch (e: any) {
            setError(e.message ?? 'Somehting went wrong, Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
    <div>
      <div className="grid grid-cols-2 gap-3">
        <FormField label="First name">
          <Input value={form.firstName} onChange={f('firstName')} placeholder="First Name" autoFocus />
        </FormField>
        <FormField label="Last name">
          <Input value={form.lastName} onChange={f('lastName')} placeholder="Last Name" />
        </FormField>
      </div>
      <FormField label="Email address">
        <Input type="email" value={form.email} onChange={f('email')} placeholder="user@example.com" />
      </FormField>
      <FormField label="Date of birth">
        <Input type="date" value={form.dateOfBirth} onChange={f('dateOfBirth')} />
      </FormField>
      <FormField label="Password" hint="Must be at least 6 characters.">
        <Input type="password" value={form.password} onChange={f('password')} placeholder="••••••••" />
      </FormField>
      <FormField label="Confirm password">
        <Input
          type="password"
          value={form.confirmPassword}
          onChange={f('confirmPassword')}
          onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
          placeholder="••••••••"
        />
      </FormField>
 
      {error && (
        <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2 mb-4">
          {error}
        </p>
      )}
 
      <Button onClick={handleSubmit} loading={loading} className="w-full justify-center mt-1">
        Create account
      </Button>
    </div>
  );
}

// Auth Page
export default function AuthPage() {
  const { signIn } = useAuth();
  const [tab, setTab] = useState<Tab>('signin');
 
  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
 
        {/* Logo */}
        <div className="flex flex-col items-center mb-8">
          <div className="w-12 h-12 rounded-2xl bg-emerald-600 flex items-center justify-center text-white font-bold text-xl mb-4">
            F
          </div>
          <h1 className="text-2xl font-bold text-slate-800 tracking-tight">Finance App</h1>
          <p className="text-sm text-slate-400 mt-1">Personal Finance</p>
        </div>
 
        {/* Card */}
        <div className="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
 
          {/* Tab switch */}
          <div className="flex border-b border-slate-100">
            {(['signin', 'signup'] as Tab[]).map((t) => (
              <button
                key={t}
                onClick={() => setTab(t)}
                className={`flex-1 py-3.5 text-sm font-semibold transition-colors ${
                  tab === t
                    ? 'text-emerald-600 border-b-2 border-emerald-600 bg-white'
                    : 'text-slate-400 hover:text-slate-600 bg-slate-50'
                }`}
              >
                {t === 'signin' ? 'Sign in' : 'Create account'}
              </button>
            ))}
          </div>
 
          {/* Form */}
          <div className="px-6 py-6">
            {tab === 'signin' ? (
              <>
                <p className="text-sm text-slate-500 mb-5">
                  Welcome back. Sign in to your account.
                </p>
                <SignInForm onSuccess={signIn} />
              </>
            ) : (
              <>
                <p className="text-sm text-slate-500 mb-5">
                  Create an account to start tracking your finances.
                </p>
                <SignUpForm onSuccess={signIn} />
              </>
            )}
          </div>
        </div>
 
        {/* Footer */}
        <p className="text-center text-xs text-slate-400 mt-6">
          {tab === 'signin' ? "Don't have an account? " : 'Already have an account? '}
          <button
            onClick={() => setTab(tab === 'signin' ? 'signup' : 'signin')}
            className="text-emerald-600 font-semibold hover:underline"
          >
            {tab === 'signin' ? 'Create one' : 'Sign in'}
          </button>
        </p>
      </div>
    </div>
  );
}