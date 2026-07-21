const BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080/api";

export interface Category {
  id: number;
  name: string;
  description?: string | null;
}

export interface Organism {
  id: number;
  commonName: string;
  scientificName?: string | null;
  habitat?: string | null;
  description?: string | null;
  categoryId: number;
  categoryName: string;
}

export interface OrganismInput {
  commonName: string;
  scientificName?: string;
  habitat?: string;
  description?: string;
  categoryId: number;
}

export interface CategoryInput {
  name: string;
  description?: string;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...init,
  });
  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      if (body?.message) message = body.message;
      if (body?.details?.length) message += `: ${body.details.join(", ")}`;
    } catch {
      /* non-JSON error body */
    }
    throw new Error(message);
  }
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

export const api = {
  listCategories: () => request<Category[]>("/categories"),
  createCategory: (input: CategoryInput) =>
    request<Category>("/categories", { method: "POST", body: JSON.stringify(input) }),
  deleteCategory: (id: number) =>
    request<void>(`/categories/${id}`, { method: "DELETE" }),

  listOrganisms: (categoryId?: number, name?: string) => {
    const params = new URLSearchParams();
    if (categoryId) params.set("categoryId", String(categoryId));
    if (name) params.set("name", name);
    const qs = params.toString();
    return request<Organism[]>(`/organisms${qs ? `?${qs}` : ""}`);
  },
  createOrganism: (input: OrganismInput) =>
    request<Organism>("/organisms", { method: "POST", body: JSON.stringify(input) }),
  deleteOrganism: (id: number) =>
    request<void>(`/organisms/${id}`, { method: "DELETE" }),
};
