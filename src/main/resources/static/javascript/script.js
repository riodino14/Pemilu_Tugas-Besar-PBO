document.addEventListener("DOMContentLoaded", () => {
  // ========== Konfigurasi URL ==========
  const ADMIN_BASE_URL = "http://localhost:8080/api/admin"; 
  // endpoint CRUD calon
  const CALON_CRUD_URL = `${ADMIN_BASE_URL}/calon`;

  const CALON_REKAP_URL = "http://localhost:8080/api/calon"; 
  // endpoint rekap => /api/calon/rekapitulasi

  const IMAGE_BASE_URL = "http://localhost:8080/uploads"; 
  // tempat foto disimpan

  // ========== Elemen HTML ==========
  // Bagian daftar calon
  const candidateTableBody = document.querySelector("#candidate-table tbody");
  const filterJenis = document.getElementById("filterJenis");
  const filterDaerahPemilihan = document.getElementById("filterDaerahPemilihan");

  // Tombol rekap
  const loadRekapitulasiButton = document.getElementById("loadRekapitulasi");
  const filterJenisRekap = document.getElementById("filterJenisRekap");
  const filterDaerahRekap = document.getElementById("filterDaerahRekap"); // Tambahkan ini
  const rekapitulasiContainer = document.getElementById("rekapitulasi-container");

  // Form tambah calon (jika ada)
  const addCandidateForm = document.getElementById("add-candidate-form");

  // Tombol logout
  const logoutBtn = document.getElementById("logout-btn");

  // ========== Fungsi Utilitas Fetch ==========
  const fetchData = async (url, options = {}) => {
    try {
      const response = await fetch(url, options);
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `HTTP Error ${response.status}`);
      }
      return response.json();
    } catch (error) {
      console.error("Fetch error:", error);
      throw error;
    }
  };

  // ========== 1) Load Candidates (Daftar Calon) ==========
  const loadCandidates = async () => {
    if (!candidateTableBody) return; // Pastikan elemen ada
    try {
      const candidates = await fetchData(CALON_CRUD_URL); // GET /api/admin/calon
      candidateTableBody.innerHTML = "";

      // Filter by jenis (client-side)
      const selectedJenis = filterJenis.value; // "ALL" => semua
      let filtered = candidates;
      if (selectedJenis !== "ALL") {
        filtered = filtered.filter(c => c.jenis === selectedJenis);
      }

      // Filter by daerah (partial match)
      const typedDapil = filterDaerahPemilihan.value.trim().toLowerCase();
      if (typedDapil) {
        filtered = filtered.filter(c =>
          c.daerahPemilihan &&
          c.daerahPemilihan.toLowerCase().includes(typedDapil)
        );
      }

      if (filtered.length === 0) {
        candidateTableBody.innerHTML = `
          <tr>
            <td colspan="7" class="text-center">Tidak ada calon legislatif.</td>
          </tr>
        `;
        return;
      }

      // Tampilkan data
      filtered.forEach(candidate => {
        const fotoUrl = candidate.foto
          ? `${IMAGE_BASE_URL}/${candidate.foto}`
          : "default.jpg";

        const row = `
          <tr>
            <td><img src="${fotoUrl}" alt="${candidate.nama}" style="width: 100%; height: auto; max-width: 85px;"/></td>
            <td>${candidate.nama}</td>
            <td>${candidate.partai}</td>
            <td>${candidate.daerahPemilihan}</td>
            <td>${candidate.jenis}</td>
            <td>${candidate.visiMisi}</td>
            <td>
              <button
                onclick="window.location.href='edit-candidate.html?id=${candidate.id}'"
                class="btn btn-sm btn-primary"
              >
                Edit
              </button>
              <button
                onclick="deleteCandidate(${candidate.id})"
                class="btn btn-sm btn-danger"
              >
                Delete
              </button>
            </td>
          </tr>
        `;
        candidateTableBody.insertAdjacentHTML("beforeend", row);
      });
    } catch (err) {
      alert("Gagal memuat daftar calon: " + err.message);
    }
  };

  // ========== Delete Candidate ==========
  window.deleteCandidate = async (id) => {
    if (!confirm("Apakah Anda yakin ingin menghapus calon ini?")) return;
    try {
      await fetchData(`${CALON_CRUD_URL}/${id}`, { method: "DELETE" });
      alert("Calon berhasil dihapus.");
      loadCandidates();
    } catch (err) {
      alert("Gagal menghapus calon: " + err.message);
    }
  };

  // ========== 2) Rekapitulasi ==========
  const loadRekapitulasi = async () => {
    if (!rekapitulasiContainer) return; // Pastikan elemen ada
    try {
      // Ambil filter jenis dan daerah pemilihan
      const jenis = filterJenisRekap.value; // "" => semua
      const daerah = filterDaerahRekap.value.trim(); // "" => semua

      let url = `${CALON_REKAP_URL}/rekapitulasi`; // /api/calon/rekapitulasi

      // Tambahkan query parameters jika ada filter
      const params = new URLSearchParams();
      if (jenis) {
        params.append("jenis", jenis);
      }
      if (daerah) {
        params.append("daerahPemilihan", daerah);
      }
      if ([...params].length > 0) {
        url += `?${params.toString()}`;
      }

      const candidates = await fetchData(url);
      displayRekapitulasi(candidates);
    } catch (err) {
      console.error("Error loadRekapitulasi:", err);
      alert("Terjadi kesalahan saat memuat rekapitulasi: " + err.message);
    }
  };

  // Tampilkan rekap dalam bentuk tabel
  const displayRekapitulasi = (candidates) => {
    if (!rekapitulasiContainer) return; // Pastikan elemen ada
    rekapitulasiContainer.innerHTML = "";

    if (!candidates || candidates.length === 0) {
      rekapitulasiContainer.innerHTML = "<p>Tidak ada data rekapitulasi.</p>";
      return;
    }

    // Hitung total suara
    const totalVotes = candidates.reduce((sum, c) => sum + c.totalSuara, 0);

    // Buat tabel
    let tableHTML = `
      <table class="table table-bordered">
        <thead class="table-light">
          <tr>
            <th>Nama</th>
            <th>Jenis Caleg</th>
            <th>Partai</th>
            <th>Daerah Pemilihan</th>
            <th>Visi dan Misi</th>
            <th>Total Suara</th>
            <th>Persentase</th>
          </tr>
        </thead>
        <tbody>
    `;

    candidates.forEach(candidate => {
      const percentage = totalVotes === 0
        ? "0.00"
        : ((candidate.totalSuara / totalVotes) * 100).toFixed(2);

      const fotoUrl = candidate.foto
        ? `${IMAGE_BASE_URL}/${candidate.foto}`
        : "default.jpg";

      tableHTML += `
        <tr>
          <td>
            <img src="${fotoUrl}" alt="${candidate.nama}" style="width:50px; height:50px; object-fit:cover; margin-right:10px;">
            <strong>${candidate.nama}</strong>
          </td>
          <td>${candidate.jenis}</td>
          <td>${candidate.partai}</td>
          <td>${candidate.daerahPemilihan}</td>
          <td>${candidate.visiMisi}</td>
          <td>${candidate.totalSuara}</td>
          <td>${percentage}%</td>
        </tr>
      `;
    });

    tableHTML += `
        </tbody>
      </table>
    `;

    rekapitulasiContainer.innerHTML = tableHTML;
  };

  // ========== 3) Tambah Calon (Jika ada form) ==========
  if (addCandidateForm) {
    addCandidateForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const formData = new FormData(addCandidateForm);
      try {
        const response = await fetch(`${CALON_CRUD_URL}`, {
          method: "POST",
          body: formData,
        });

        const result = await response.json();
        if (result.status === "success") {
          alert(result.message);
          window.location.href = "admin.html"; // Redirect setelah sukses
        } else {
          alert(`Gagal menambah calon: ${result.message}`);
        }
      } catch (err) {
        alert("Gagal menambah calon: " + err.message);
      }
    });
  }

  // ========== 4) Logout ==========
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      sessionStorage.clear();
      alert("Logout berhasil.");
      window.location.href = "index.html";
    });
  }

  // ========== 5) Event Listener / Inisialisasi ==========
  
  // Daftar Calon
  if (filterJenis) {
    filterJenis.addEventListener("change", loadCandidates);
  }
  if (filterDaerahPemilihan) {
    filterDaerahPemilihan.addEventListener("input", loadCandidates);
  }

  // Rekapitulasi
  if (filterJenisRekap) {
    filterJenisRekap.addEventListener("change", loadRekapitulasi);
  }
  if (filterDaerahRekap) { // Tambahkan ini
    filterDaerahRekap.addEventListener("input", loadRekapitulasi);
  }

  // Tombol rekap
  if (loadRekapitulasiButton) {
    loadRekapitulasiButton.addEventListener("click", loadRekapitulasi);
  }

  // Load awal daftar calon
  if (candidateTableBody) {
    loadCandidates();
  }
});
