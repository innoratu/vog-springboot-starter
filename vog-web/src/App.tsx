import { useCallback, useEffect, useState } from "react";
import { api, type Category, type Organism } from "./api/client";
import { OrganismList } from "./components/OrganismList";
import { OrganismForm } from "./components/OrganismForm";
import { CategoryManager } from "./components/CategoryManager";
import "./App.css";

export default function App() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [organisms, setOrganisms] = useState<Organism[]>([]);
  const [filterCategoryId, setFilterCategoryId] = useState<number | 0>(0);
  const [error, setError] = useState<string | null>(null);

  const loadCategories = useCallback(async () => {
    setCategories(await api.listCategories());
  }, []);

  const loadOrganisms = useCallback(async () => {
    setOrganisms(await api.listOrganisms(filterCategoryId || undefined));
  }, [filterCategoryId]);

  const refreshAll = useCallback(async () => {
    setError(null);
    try {
      await Promise.all([loadCategories(), loadOrganisms()]);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load data");
    }
  }, [loadCategories, loadOrganisms]);

  useEffect(() => {
    loadOrganisms().catch((err) =>
      setError(err instanceof Error ? err.message : "Failed to load organisms"),
    );
  }, [loadOrganisms]);

  useEffect(() => {
    refreshAll();
    // run once on mount
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <main className="container">
      <header>
        <h1>🧬 Vog — Living Things Catalog</h1>
        <p>Browse and categorize living things (mammals, fish, birds, plants, humans…).</p>
      </header>

      {error && <p className="error banner">{error}</p>}

      <CategoryManager categories={categories} onChanged={refreshAll} />
      <OrganismForm categories={categories} onCreated={refreshAll} />
      <OrganismList
        organisms={organisms}
        categories={categories}
        filterCategoryId={filterCategoryId}
        onFilterChange={setFilterCategoryId}
        onDeleted={refreshAll}
      />
    </main>
  );
}
