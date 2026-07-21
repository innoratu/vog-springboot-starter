import { useState } from "react";
import { api, type Category } from "../api/client";

interface Props {
  categories: Category[];
  onChanged: () => void;
}

export function CategoryManager({ categories, onChanged }: Props) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState<string | null>(null);

  async function add(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await api.createCategory({ name, description });
      setName("");
      setDescription("");
      onChanged();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to add category");
    }
  }

  async function remove(id: number) {
    setError(null);
    try {
      await api.deleteCategory(id);
      onChanged();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete category");
    }
  }

  return (
    <div className="card">
      <h2>Categories</h2>
      {error && <p className="error">{error}</p>}
      <ul className="chips">
        {categories.map((c) => (
          <li key={c.id} className="badge">
            {c.name}
            <button className="link" onClick={() => remove(c.id)} title="delete category">
              ×
            </button>
          </li>
        ))}
      </ul>
      <form onSubmit={add} className="inline-form">
        <input placeholder="New category name" value={name} onChange={(e) => setName(e.target.value)} required />
        <input placeholder="Description" value={description} onChange={(e) => setDescription(e.target.value)} />
        <button type="submit">Add</button>
      </form>
    </div>
  );
}
