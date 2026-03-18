import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { AppProvider } from './context/AppContext';
import Sidebar from './components/layout/Sidebar';
import TopBar from './components/layout/TopBar';
import ToastContainer from './components/ui/Toast';
import AuthPage from './pages/Auth';
import Dashboard from './pages/Dashboard';
import Income from './pages/Income';
import Expenses from './pages/Expenses';
import NetWorth from './pages/NetWorth';
import Retirement from './pages/Retirement';
import BalanceSheet from './pages/BalanceSheet';
import Allocations from './pages/Allocations';

function AppLayout() {
  return (
    <div className="flex h-screen overflow-hidden bg-slate-50">
      <Sidebar />
      <div className="flex flex-col flex-1 overflow-hidden min-w-0">
        <TopBar />
        <main className="flex-1 overflow-y-auto bg-slate-50">
          <Routes>
            <Route path="/"              element={<Dashboard />} />
            <Route path="/income"        element={<Income />} />
            <Route path="/expenses"      element={<Expenses />} />
            <Route path="/net-worth"     element={<NetWorth />} />
            <Route path="/retirement"    element={<Retirement />} />
            <Route path="/balance-sheet" element={<BalanceSheet />} />
            <Route path="/allocations"   element={<Allocations />} />
          </Routes>
        </main>
      </div>
      <ToastContainer />
    </div>
  );
}

function Root() {
  /*const { currentUser } = useAuth();

  if (!currentUser) {
    return <AuthPage />;
  }

  return (
    <AppProvider user={currentUser}>
      <AppLayout />
    </AppProvider>
  );
  */

  const { currentUser } = useAuth();

  // TEMP: skip auth for development
  const devUser = {
    id: 1,
    email: 'test@test.com',
    firstName: 'Test',
    lastName: 'User',
    fullName: 'Test User',
    dateOfBirth: '1990-01-01',
    age: 35,
    ageBracket: '30s',
    createdAt: '2025-01-01T00:00:00',
  };

  return (
    <AppProvider user={devUser}>
      <AppLayout />
    </AppProvider>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Root />
      </AuthProvider>
    </BrowserRouter>
  );
}