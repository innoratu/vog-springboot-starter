import { api, type Category, type Organism } from "../api/client";

interface Props {
  organisms: Organism[];
  categories: Category[];
  filterCategoryId: number | 0;
  onFilterChange: (categoryId: number | 0) => void;
  onDeleted: () => void;
}

export function OrganismList({
  organisms,
  categories,
  filterCategoryId,
  onFilterChange,
  onDeleted,
}: Props) {
  async function remove(id: number) {
    await api.deleteOrganism(id);
    onDeleted();
  }

  return (
    <div className="card">
      <div className="list-header">
        <h2>Organisms ({organisms.length})</h2>
        <label>
          Filter:
          <select
            value={filterCategoryId}
            onChange={(e) => onFilterChange(Number(e.target.value))}
          >
            <option value={0}>All categories</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
      </div>
      {organisms.length === 0 ? (
        <p>No organisms found.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Common name</th>
              <th>Scientific name</th>
              <th>Category</th>
              <th>Habitat</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {organisms.map((o) => (
              <tr key={o.id}>
                <td>{o.commonName}</td>
                <td>
                  <em>{o.scientificName ?? "-"}</em>
                </td>
                <td>
                  <span className="badge">{o.categoryName}</span>
                </td>
                <td>{o.habitat ?? "-"}</td>
                <td>
                  <button className="link" onClick={() => remove(o.id)}>
                    delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
