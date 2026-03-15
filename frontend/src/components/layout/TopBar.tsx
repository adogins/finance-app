import { useState } from 'react';
import { useApp } from '../../context/AppContext';
import Button from '../ui/Button';
import { Input } from '../ui/FormField';

export default function TopBar() {
    const { user, userId, setUserId } = useApp();
    const [editing, setEditing] = useState(false);
    const [val, setVal] = useState(String(userId));

    const apply = () => {
        const n = parseInt(val, 10);
        if (!isNaN(n) && n > 0) {
            setUserId(n);
            setEditing(false);
        }
    };

    return (
    <header className="h-14 bg-white border-b border-slate-200 flex items-center px-6 gap-4 shrink-0 sticky top-0 z-10">
      {/* User info */}
      <div className="flex items-center gap-3 flex-1">
        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center text-white text-xs font-bold shrink-0 border-2 border-slate-100">
          {user?.firstName?.[0] ?? '?'}
        </div>
        {user ? (
          <div>
            <div className="text-sm font-semibold text-slate-800 leading-tight">{user.fullName}</div>
            <div className="text-xs text-slate-400">{user.email} · Age {user.age}</div>
          </div>
        ) : (
          <span className="text-sm text-slate-400">No user loaded — enter a User ID</span>
        )}
      </div>
 
      {/* User ID switcher */}
      {editing ? (
        <div className="flex items-center gap-2">
          <Input
            value={val}
            onChange={(e) => setVal(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && apply()}
            className="w-24 py-1.5 text-xs"
            placeholder="User ID"
            autoFocus
          />
          <Button size="sm" onClick={apply}>Load</Button>
          <Button size="sm" variant="secondary" onClick={() => setEditing(false)}>Cancel</Button>
        </div>
      ) : (
        <button
          onClick={() => { setVal(String(userId)); setEditing(true); }}
          className="flex items-center gap-1.5 bg-slate-50 hover:bg-slate-100 border border-slate-200 rounded-lg px-3 py-1.5 text-xs text-slate-500 transition-colors"
        >
          <span className="text-slate-400">User ID:</span>
          <strong className="text-slate-700">{userId}</strong>
          <span className="text-slate-300 text-[10px]">✎</span>
        </button>
      )}
    </header>
  );
}