import { useState } from "react";
import { api, type Category, type OrganismInput } from "../api/client";

interface Props {
  categories: Category[];
  onCreated: () => void;
}

const empty: OrganismInput = {
  commonName: "",
  scientificName: "",
  habitat: "",
  description: "",
  categoryId: 0,
};

export function OrganismForm({ categories, onCreated }: Props) {
  const [form, setForm] = useState<OrganismInput>(empty);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const update = (field: keyof OrganismInput, value: string) =>
    setForm((f) => ({ ...f, [field]: field === "categoryId" ? Number(value) : value }));

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSaving(true);
    try {
      await api.createOrganism({
        ...form,
        categoryId: Number(form.categoryId),
      });
      setForm(empty);
      onCreated();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create organism");
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={submit} className="card">
      <h2>Add organism</h2>
      {error && <p className="error">{error}</p>}
      <label>
        Common name*
        <input value={form.commonName} onChange={(e) => update("commonName", e.target.value)} required />
      </label>
      <label>
        Scientific name
        <input value={form.scientificName} onChange={(e) => update("scientificName", e.target.value)} />
      </label>
      <label>
        Habitat
        <input value={form.habitat} onChange={(e) => update("habitat", e.target.value)} />
      </label>
      <label>
        Description
        <input value={form.description} onChange={(e) => update("description", e.target.value)} />
      </label>
      <label>
        Category*
        <select value={form.categoryId} onChange={(e) => update("categoryId", e.target.value)} required>
          <option value={0} disabled>
            Select a category
          </option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </label>
      <button type="submit" disabled={saving || !form.categoryId}>
        {saving ? "Saving..." : "Create"}
      </button>
    </form>
  );
}
