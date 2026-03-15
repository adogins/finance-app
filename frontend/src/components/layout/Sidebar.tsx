import { NavLink } from 'react-router-dom';
 
interface NavItem {
  to: string;
  label: string;
  icon: string;
}
 
interface NavGroup {
  label: string;
  items: NavItem[];
}
 
const GROUPS: NavGroup[] = [
  {
    label: 'Overview',
    items: [{ to: '/', label: 'Dashboard', icon: '⬡' }],
  },
  {
    label: 'Money',
    items: [
      { to: '/income', label: 'Income', icon: '↑' },
      { to: '/expenses', label: 'Expenses', icon: '↓' },
    ],
  },
  {
    label: 'Wealth',
    items: [
      { to: '/net-worth', label: 'Net Worth', icon: '◈' },
      { to: '/retirement', label: 'Retirement', icon: '◎' },
    ],
  },
  {
    label: 'Reports',
    items: [
      { to: '/balance-sheet', label: 'Balance Sheet', icon: '≡' },
      { to: '/allocations', label: 'Allocations', icon: '⊞' },
    ],
  },
];
 
export default function Sidebar() {
  return (
    <aside className="w-56 bg-emerald-950 border-r border-emerald-900/60 flex flex-col h-screen sticky top-0 shrink-0">
      {/* Logo */}
      <div className="px-5 py-5 border-b border-emerald-900/60">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-emerald-500 flex items-center justify-center text-white font-bold text-sm shrink-0">
            V
          </div>
          <div>
            <div className="text-sm font-bold text-emerald-50 tracking-tight">Verdn</div>
            <div className="text-[10px] text-emerald-600">Personal Finance</div>
          </div>
        </div>
      </div>
 
      {/* Nav */}
      <nav className="flex-1 px-3 py-4 overflow-y-auto">
        {GROUPS.map((group) => (
          <div key={group.label} className="mb-5">
            <div className="text-[9px] font-bold text-emerald-800 uppercase tracking-widest px-2.5 mb-1.5">
              {group.label}
            </div>
            {group.items.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/'}
                className={({ isActive }) =>
                  `flex items-center gap-2.5 px-2.5 py-2 rounded-lg text-sm mb-0.5 transition-all ${
                    isActive
                      ? 'bg-emerald-500/20 text-emerald-300 font-semibold border border-emerald-500/30'
                      : 'text-emerald-600 hover:text-emerald-300 hover:bg-emerald-900/60 border border-transparent'
                  }`
                }
              >
                <span className="text-base w-4 text-center shrink-0">{item.icon}</span>
                {item.label}
              </NavLink>
            ))}
          </div>
        ))}
      </nav>
 
      <div className="px-5 py-3 border-t border-emerald-900/60">
        <p className="text-[10px] text-emerald-900">© 2025 Verdn</p>
      </div>
    </aside>
  );
}