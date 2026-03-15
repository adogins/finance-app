import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AppProvider } from './context/AppContext';
import Sidebar from './components/layout/Sidebar';
import TopBar from './components/layout/TopBar';
import ToastContainer from './components/ui/Toast';
import Dashboard from './pages/Dashboard';
import Income from './pages/Income';
import Expenses from './pages/Expenses';
import NetWorth from './pages/NetWorth';
import Retirement from './pages/Retirement';
import BalanceSheet from './pages/BalanceSheet';
import Allocations from './pages/Allocations';

function Layout() {
  return (
    <div className='flex h-screen overflow-hidden bg-slate-50'>
      <Sidebar />
      <div className='flex flex-col felx-1 overflow-hidden min-w-0'>
        <TopBar />
        <main className='flex-1 overflow-y-auto bg-slate-50'>
          <Routes>
            <Route path='/' element={<Dashboard />} />
            <Route path='/income' element={<Income />} />
            <Route path='/expenses' element={<Expenses />} />
            <Route path='/net-worth' element={<NetWorth />} />
            <Route path='/retirement' element={<Retirement />} />
            <Route path='/balance-sheet' element={<BalanceSheet />} />
            <Route path='/allocations' element={<Allocations />} />
          </Routes>
        </main>
      </div>
      <ToastContainer />
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppProvider>
        <Layout />
      </AppProvider>
    </BrowserRouter>
  );
}