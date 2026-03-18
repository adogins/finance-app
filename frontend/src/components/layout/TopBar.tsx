import { useApp } from '../../context/AppContext';
import { useAuth } from '../../context/AuthContext';
import Button from '../ui/Button';

export default function TopBar() {
  const { user } = useApp();
  const { signOut } = useAuth();

  return (
    <header className="h-14 bg-emerald-700 border-b border-slate-200 flex items-center px-6 gap-4 shrink-0 sticky top-0 z-10">
      {/* User info */}
      <div className="flex items-center gap-3 flex-1">
        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-emerald-300 to-teal-500 flex items-center justify-center text-white text-xs font-bold shrink-0">
          {user.firstName[0]}{user.lastName[0]}
        </div>
        <div>
          <div className="text-sm font-semibold text-Emerald-900 leading-tight">{user.fullName}</div>
          <div className="text-xs text-slate-200">{user.email} · Age {user.age}</div>
        </div>
      </div>
 
      {/* Sign out */}
      <Button variant="secondary" size="sm" onClick={signOut} className='hover:bg-emerald-200 hover:border-emerald-300'>
        Sign out
      </Button>
    </header>
  );
}