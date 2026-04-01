import { useState, useEffect, useCallback } from "react";
import { snapshotsApi, ratiosApi, balanceSheetApi } from "../api/client";
import { useApp } from "../context/AppContext";
import { formatCurrency, formatPercent } from "../utils/format";
import PageHeader from "../components/ui/PageHeader";
import StatCard from "../components/ui/StatCard";
import Card from "../components/ui/Card";
import Button from "../components/ui/Button";
import Spinner from "../components/ui/Spinner";
import NetWorthChart from "../components/charts/NetWorthChart";
import RatioCard from "../components/charts/RatioCard";
import type {
  SnapshotResponse,
  RatiosResponse,
  MonthlyBalanceSheet,
} from "../types";

export default function Dashboard() {
  const { userId, addToast } = useApp();
  const [snapshots, setSnapshots] = useState<SnapshotResponse[]>([]);
  const [ratios, setRatios] = useState<RatiosResponse | null>(null);
  const [monthly, setMonthly] = useState<MonthlyBalanceSheet | null>(null);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [snaps, rats, mon] = await Promise.all([
        snapshotsApi.getAll(userId),
        ratiosApi.get(userId),
        balanceSheetApi.getMonthly(userId),
      ]);
      setSnapshots(snaps);
      setRatios(rats);
      setMonthly(mon);
    } catch (e: any) {
      addToast(e.message, "error");
    } finally {
      setLoading(false);
    }
  }, [userId, addToast]);

  useEffect(() => {
    load();
  }, [load]);

  const generateSnapshot = async () => {
    setGenerating(true);
    try {
      await snapshotsApi.generate(userId);
      addToast("Snapshot generated!");
      load();
    } catch (e: any) {
      addToast(e.message, "error");
    } finally {
      setGenerating(false);
    }
  };

  const latest = snapshots[snapshots.length - 1];
  const prev = snapshots[snapshots.length - 2];
  const nwDelta =
    latest && prev && Number(prev.netWorth) !== 0
      ? ((Number(latest.netWorth) - Number(prev.netWorth)) /
          Math.abs(Number(prev.netWorth))) *
        100
      : null;

  if (loading)
    return (
      <div className="p-6">
        <Spinner />
      </div>
    );

  return (
    <div className="p-6 flex flex-col gap-6">
      <PageHeader
        title="Dashboard"
        subtitle="Your complete financial picture at a glance"
        action={
          <Button onClick={generateSnapshot} loading={generating}>
            + Snapshot
          </Button>
        }
      />

      {/* KPI cards */}
      <div className="grid grid-cols-3 gap-4">
        <StatCard
          label="Net Worth"
          value={latest ? formatCurrency(latest.netWorth) : "—"}
          sub={latest ? "Latest snapshot" : "No snapshots yet"}
          delta={nwDelta}
          accent={latest && Number(latest.netWorth) >= 0 ? "green" : "red"}
          icon="◈"
        />
        <StatCard
          label="Monthly Income"
          value={monthly ? formatCurrency(monthly.totalIncome) : "—"}
          sub="This month"
          accent="blue"
          icon="↑"
        />
        <StatCard
          label="Monthly Expenses"
          value={monthly ? formatCurrency(monthly.totalExpenses) : "—"}
          sub={
            monthly
              ? `Savings rate: ${formatPercent(monthly.savingsRate)}`
              : undefined
          }
          accent="red"
          icon="↓"
        />
      </div>

      <div className="grid grid-cols-3 gap-4">
        <StatCard
          label="Total Assets"
          value={latest ? formatCurrency(latest.totalAssets) : "—"}
          accent="green"
          icon="↗"
        />
        <StatCard
          label="Total Liabilities"
          value={latest ? formatCurrency(latest.totalLiabilities) : "—"}
          accent="red"
          icon="↘"
        />
        <StatCard
          label="Net Cash Flow"
          value={monthly ? formatCurrency(monthly.netCashFlow) : "—"}
          sub="This month"
          accent={monthly && monthly.netCashFlow >= 0 ? "green" : "red"}
          icon="⇄"
        />
      </div>

      {/* Net Worth Chart */}
      {snapshots.length === 0 ? (
        <Card className="flex flex-col items-center justify-center py-16 gap-4">
          <div className="text-4xl">$</div>
          <div className="text-center">
            <p className="text-slate-600 font-medium mb-1">No snapshots yet</p>
            <p className="text-sm text-slate-500 mb-4">
              Generate your first snapshot to start tracking net worth
            </p>
            <Button onClick={generateSnapshot} loading={generating}>
              Generate First Snapshot
            </Button>
          </div>
        </Card>
      ) : (
        <NetWorthChart snapshots={snapshots} />
      )}

      {/* Ratio cards */}
      {ratios && (
        <div>
          <h2 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-4">
            Financial Health Ratios
          </h2>
          <div className="grid grid-cols-3 gap-4">
            {Object.values(ratios).map((r) => (
              <RatioCard key={r.name} ratio={r} />
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
