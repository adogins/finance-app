import { useState, useEffect, useCallback } from "react";
import { assetsApi, liabilitiesApi } from "../api/client";
import { useApp } from "../context/AppContext";
import { formatCurrency } from "../utils/format";
import PageHeader from "../components/ui/PageHeader";
import StatCard from "../components/ui/StatCard";
import Card from "../components/ui/Card";
import Button from "../components/ui/Button";
import Badge, {
  assetTypeBadge,
  liabilityTypeBadge,
} from "../components/ui/Badge";
import Modal from "../components/ui/Modal";
import DataTable, {
  MonoValue,
  ActionButtons,
  type Column,
} from "../components/ui/DataTable";
import { FormField, Input, Select } from "../components/ui/FormField";
import type {
  AssetResponse,
  AssetRequest,
  AssetType,
  LiabilityResponse,
  LiabilityRequest,
  LiabilityType,
} from "../types";

const ASSET_TYPES: AssetType[] = [
  "Savings",
  "Investment",
  "Property",
  "Retirement",
  "Emergency Fund",
  "Other",
];
const LIABILITY_TYPES: LiabilityType[] = [
  "Mortgage",
  "Auto",
  "Student",
  "Credit Card",
  "Personal",
  "Other",
];

const blankAsset = (): AssetRequest => ({
  name: "",
  type: "Savings",
  balance: 0,
});
const blankLiability = (): LiabilityRequest => ({
  name: "",
  type: "Mortgage",
  balance: 0,
  interestRate: null,
  monthlyPayment: null,
});

export default function NetWorth() {
  const { userId, addToast } = useApp();
  const [assets, setAssets] = useState<AssetResponse[]>([]);
  const [liabilities, setLiabilities] = useState<LiabilityResponse[]>([]);
  const [loading, setLoading] = useState(true);

  // Asset modal
  const [assetModal, setAssetModal] = useState(false);
  const [editingAsset, setEditingAsset] = useState<AssetResponse | null>(null);
  const [assetForm, setAssetForm] = useState<AssetRequest>(blankAsset());
  const [savingAsset, setSavingAsset] = useState(false);

  // Liability modal
  const [liabilityModal, setLiabilityModal] = useState(false);
  const [editingLiability, setEditingLiability] =
    useState<LiabilityResponse | null>(null);
  const [liabilityForm, setLiabilityForm] = useState<LiabilityRequest>(
    blankLiability()
  );
  const [savingLiability, setSavingLiability] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [a, l] = await Promise.all([
        assetsApi.getAll(userId),
        liabilitiesApi.getAll(userId),
      ]);
      setAssets(a);
      setLiabilities(l);
    } catch (e: any) {
      addToast(e.message, "error");
    } finally {
      setLoading(false);
    }
  }, [userId, addToast]);

  useEffect(() => {
    load();
  }, [load]);

  const totalAssets = assets.reduce((s, a) => s + Number(a.balance), 0);
  const totalLiabilities = liabilities.reduce(
    (s, l) => s + Number(l.balance),
    0
  );
  const netWorth = totalAssets - totalLiabilities;

  // asset handlers
  const openAddAsset = () => {
    setEditingAsset(null);
    setAssetForm(blankAsset());
    setAssetModal(true);
  };
  const openEditAsset = (a: AssetResponse) => {
    setEditingAsset(a);
    setAssetForm({ name: a.name, type: a.type, balance: a.balance });
    setAssetModal(true);
  };

  const handleSaveAsset = async () => {
    if (!assetForm.name.trim()) return addToast("Name is required", "error");
    if (assetForm.balance < 0)
      return addToast("Balance cannot be negative", "error");
    setSavingAsset(true);
    try {
      if (editingAsset)
        await assetsApi.update(userId, editingAsset.id, assetForm);
      else await assetsApi.create(userId, assetForm);
      addToast(editingAsset ? "Asset updated" : "Asset added");
      setAssetModal(false);
      load();
    } catch (e: any) {
      addToast(e.message, "error");
    } finally {
      setSavingAsset(false);
    }
  };

  const handleDeleteAsset = async (id: number) => {
    if (!confirm("Delete this asset?")) return;
    try {
      await assetsApi.delete(userId, id);
      addToast("Deleted");
      load();
    } catch (e: any) {
      addToast(e.message, "error");
    }
  };

  // liability handlers
  const openAddLiability = () => {
    setEditingLiability(null);
    setLiabilityForm(blankLiability());
    setLiabilityModal(true);
  };
  const openEditLiability = (l: LiabilityResponse) => {
    setEditingLiability(l);
    setLiabilityForm({
      name: l.name,
      type: l.type,
      balance: l.balance,
      interestRate: l.interestRate,
      monthlyPayment: l.monthlyPayment,
    });
    setLiabilityModal(true);
  };

  const handleSaveLiability = async () => {
    if (!liabilityForm.name.trim())
      return addToast("Name is required", "error");
    if (liabilityForm.balance < 0)
      return addToast("Balance cannot be negative", "error");
    setSavingLiability(true);
    try {
      if (editingLiability)
        await liabilitiesApi.update(userId, editingLiability.id, liabilityForm);
      else await liabilitiesApi.create(userId, liabilityForm);
      addToast(editingLiability ? "Liability updated" : "Liability added");
      setLiabilityModal(false);
      load();
    } catch (e: any) {
      addToast(e.message, "error");
    } finally {
      setSavingLiability(false);
    }
  };

  const handleDeleteLiability = async (id: number) => {
    if (!confirm("Delete this liability>")) return;
    try {
      await liabilitiesApi.delete(userId, id);
      addToast("Deleted");
      load();
    } catch (e: any) {
      addToast(e.message, "error");
    }
  };

  const assetColumns: Column<AssetResponse>[] = [
    {
      key: "name",
      header: "Name",
      render: (r) => (
        <span className="text-slate-700 font-medium">{r.name}</span>
      ),
    },
    {
      key: "type",
      header: "Type",
      width: "120px",
      render: (r) => (
        <Badge
          variant={
            r.type === "Emergency Fund" ? "blue" : assetTypeBadge[r.type]
          }
        >
          {r.type}
        </Badge>
      ),
    },
    {
      key: "type",
      header: "Type",
      width: "120px",
      render: (r) => <Badge variant={assetTypeBadge[r.type]}>{r.type}</Badge>,
    },
    {
      key: "balance",
      header: "Balance",
      align: "right",
      render: (r) => (
        <MonoValue
          value={formatCurrency(r.balance)}
          className="text-emerald-600"
        />
      ),
    },
    {
      key: "actions",
      header: "",
      width: "120px",
      render: (r) => (
        <ActionButtons
          onEdit={() => openEditAsset(r)}
          onDelete={() => handleDeleteAsset(r.id)}
        />
      ),
    },
  ];

  const liabilityColumns: Column<LiabilityResponse>[] = [
    {
      key: "name",
      header: "Name",
      render: (r) => (
        <span className="text-slate-700 font-medium">{r.name}</span>
      ),
    },
    {
      key: "type",
      header: "Type",
      width: "130px",
      render: (r) => (
        <Badge variant={liabilityTypeBadge[r.type]}>{r.type}</Badge>
      ),
    },
    {
      key: "balance",
      header: "Balance",
      align: "right",
      render: (r) => (
        <MonoValue value={formatCurrency(r.balance)} className="text-red-600" />
      ),
    },
    {
      key: "rate",
      header: "Rate",
      align: "right",
      width: "80px",
      render: (r) => (
        <span className="text-slate-400 text-sm">
          {r.interestRate != null ? `${r.interestRate}%` : "—"}
        </span>
      ),
    },
    {
      key: "payment",
      header: "Monthly",
      align: "right",
      width: "110px",
      render: (r) => (
        <span className="text-slate-400 text-sm">
          {r.monthlyPayment != null ? formatCurrency(r.monthlyPayment) : "—"}
        </span>
      ),
    },
    {
      key: "actions",
      header: "",
      width: "120px",
      render: (r) => (
        <ActionButtons
          onEdit={() => openEditLiability(r)}
          onDelete={() => handleDeleteLiability(r.id)}
        />
      ),
    },
  ];

  return (
    <div className="p-6 flex flex-col gap-6">
      <PageHeader
        title="Net Worth"
        subtitle="Assets, liabilities, and overall wealth position"
      />

      <div className="grid grid-cols-3 gap-4">
        <StatCard
          label="Total Assets"
          value={formatCurrency(totalAssets)}
          accent="green"
          icon="↗"
        />
        <StatCard
          label="Total Liabilities"
          value={formatCurrency(totalLiabilities)}
          accent="red"
          icon="↘"
        />
        <StatCard
          label="Net Worth"
          value={formatCurrency(netWorth)}
          accent={netWorth >= 0 ? "green" : "red"}
          icon="◈"
        />
      </div>

      {/* Assets */}
      <Card noPad>
        <div className="flex items-center justify-between px-5 py-4 border-b border-slate-200">
          <h2 className="text-sm font-semibold text-slate-700">Assets</h2>
          <Button size="sm" onClick={openAddAsset}>
            + Add Asset
          </Button>
        </div>
        <DataTable
          columns={assetColumns}
          rows={assets}
          rowKey={(r) => r.id}
          loading={loading}
          emptyIcon="🏦"
          emptyMessage="No assets yet."
          footer={
            <div className="flex justify-between items-center">
              <span className="text-xs font-semibold text-slate-400">
                Total Assets
              </span>
              <MonoValue
                value={formatCurrency(totalAssets)}
                className="text-emerald-600 text-sm"
              />
            </div>
          }
        />
      </Card>

      {/* Liabilities */}
      <Card noPad>
        <div className="flex items-center justify-between px-5 py-4 border-b border-slate-200">
          <h2 className="text-sm font-semibold text-slate-700">Liabilities</h2>
          <Button size="sm" onClick={openAddLiability}>
            + Add Liability
          </Button>
        </div>
        <DataTable
          columns={liabilityColumns}
          rows={liabilities}
          rowKey={(r) => r.id}
          loading={loading}
          emptyIcon="📋"
          emptyMessage="No liabilities yet."
          footer={
            <div className="flex justify-between items-center">
              <span className="text-xs font-semibold text-slate-400">
                Total Liabilities
              </span>
              <MonoValue
                value={formatCurrency(totalLiabilities)}
                className="text-red-600 text-sm"
              />
            </div>
          }
        />
      </Card>

      {/* Asset Modal */}
      {assetModal && (
        <Modal
          title={editingAsset ? "Edit Asset" : "Add Asset"}
          onClose={() => setAssetModal(false)}
          onConfirm={handleSaveAsset}
          confirmLabel={editingAsset ? "Update" : "Add"}
          confirmLoading={savingAsset}
        >
          <FormField label="Name">
            <Input
              value={assetForm.name}
              onChange={(e) =>
                setAssetForm({ ...assetForm, name: e.target.value })
              }
              placeholder="Chase Savings Account"
            />
          </FormField>
          <FormField label="Type">
            <Select
              value={assetForm.type}
              onChange={(e) =>
                setAssetForm({
                  ...assetForm,
                  type: e.target.value as AssetType,
                })
              }
            >
              {ASSET_TYPES.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </Select>
          </FormField>
          <FormField label="Balance">
            <Input
              type="number"
              min="0"
              step="0.01"
              value={assetForm.balance || ""}
              onChange={(e) =>
                setAssetForm({
                  ...assetForm,
                  balance: parseFloat(e.target.value) || 0,
                })
              }
              placeholder="0.00"
            />
          </FormField>
        </Modal>
      )}

      {/* Liability Modal */}
      {liabilityModal && (
        <Modal
          title={editingLiability ? "Edit Liability" : "Add Liability"}
          onClose={() => setLiabilityModal(false)}
          onConfirm={handleSaveLiability}
          confirmLabel={editingLiability ? "Update" : "Add"}
          confirmLoading={savingLiability}
        >
          <FormField label="Name">
            <Input
              value={liabilityForm.name}
              onChange={(e) =>
                setLiabilityForm({ ...liabilityForm, name: e.target.value })
              }
              placeholder="Home Mortgage"
            />
          </FormField>
          <FormField label="Type">
            <Select
              value={liabilityForm.type}
              onChange={(e) =>
                setLiabilityForm({
                  ...liabilityForm,
                  type: e.target.value as LiabilityType,
                })
              }
            >
              {LIABILITY_TYPES.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </Select>
          </FormField>
          <FormField label="Balance">
            <Input
              type="number"
              min="0"
              step="0.01"
              value={liabilityForm.balance || ""}
              onChange={(e) =>
                setLiabilityForm({
                  ...liabilityForm,
                  balance: parseFloat(e.target.value) || 0,
                })
              }
              placeholder="0.00"
            />
          </FormField>
          <FormField label="Interest Rate % (optional)">
            <Input
              type="number"
              min="0"
              max="100"
              step="0.01"
              value={liabilityForm.interestRate ?? ""}
              onChange={(e) =>
                setLiabilityForm({
                  ...liabilityForm,
                  interestRate: e.target.value
                    ? parseFloat(e.target.value)
                    : null,
                })
              }
              placeholder="e.g. 4.5"
            />
          </FormField>
          <FormField label="Monthly Payment (optional)">
            <Input
              type="number"
              min="0"
              step="0.01"
              value={liabilityForm.monthlyPayment ?? ""}
              onChange={(e) =>
                setLiabilityForm({
                  ...liabilityForm,
                  monthlyPayment: e.target.value
                    ? parseFloat(e.target.value)
                    : null,
                })
              }
              placeholder="0.00"
            />
          </FormField>
        </Modal>
      )}
    </div>
  );
}
