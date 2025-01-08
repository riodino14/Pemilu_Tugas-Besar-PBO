document.addEventListener("DOMContentLoaded", () => {
  const BASE_URL = "http://localhost:8080/api/pemilu";  // URL backend Spring Boot

  const addCandidateForm = document.getElementById("add-candidate-form");
  const candidateTableBody = document.querySelector("#candidate-table tbody");
  const filterJenis = document.getElementById("filterJenis");

  // **Handle Login Form**
  const loginForm = document.getElementById("login-form");
  loginForm?.addEventListener("submit", async (e) => {
    e.preventDefault();

    const selectedRole = document.querySelector('input[name="role"]:checked').value;
    const identifier = document.getElementById("identifier").value.trim();

    if (!selectedRole || !identifier) {
      alert("Harap pilih peran dan masukkan ID/NIK.");
      return;
    }

    try {
      const response = await fetch(`${BASE_URL}/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ role: selectedRole, identifier: identifier }),
      });
      if (response.ok) {
        alert("Login berhasil!");
        const data = await response.json();
        sessionStorage.setItem("identifier", identifier); // Simpan NIK
        console.log("NIK yang disimpan ke sessionStorage:", identifier);

        sessionStorage.setItem("role", selectedRole);
      
        if (selectedRole === "pemilih") {
          window.location.href = "pemilih.html";
        } else if (selectedRole === "admin") {
          window.location.href = "admin.html";
        }
      } else {
        const errorMessage = await response.text();
        alert("Login gagal: " + errorMessage);
      }
    } catch (error) {
      console.error("Login error:", error.message);
      alert("Terjadi kesalahan saat login. Silakan coba lagi.");
    }
  });

  // **Load Candidates with Filter and Photo**
  const loadCandidates = async (filter = "ALL") => {
    try {
      const response = await fetch(`${BASE_URL}/calon`);
      if (!response.ok) {
        throw new Error(`Server Error: ${response.status}`);
      }

      const candidates = await response.json();
      candidateTableBody.innerHTML = "";

      const filteredCandidates = filter === "ALL"
        ? candidates
        : candidates.filter(c => c.jenis === filter);

      if (filteredCandidates.length === 0) {
        candidateTableBody.innerHTML = "<tr><td colspan='7'>Tidak ada calon legislatif.</td></tr>";
        return;
      }

      filteredCandidates.forEach((candidate) => {
        const row = document.createElement("tr");
        row.innerHTML = `
          <td><img src="${BASE_URL}/uploads/${candidate.foto}" alt="${candidate.nama}" width="50"></td>
          <td>${candidate.nama}</td>
          <td>${candidate.partai}</td>
          <td>${candidate.daerahPemilihan}</td>
          <td>${candidate.jenis}</td>
          <td>${candidate.visiMisi}</td>
          <td>
            <button onclick="window.location.href='edit-candidate.html?id=${candidate.id}'">Edit</button>
            <button onclick="deleteCandidate('${candidate.id}')">Delete</button>
          </td>
        `;
        candidateTableBody.appendChild(row);
      });
    } catch (error) {
      console.error("Gagal memuat daftar kandidat:", error.message);
      alert("Gagal memuat daftar kandidat.");
    }
  };

  // **Add Candidate (With File Upload)**
  addCandidateForm?.addEventListener("submit", async (e) => {
    e.preventDefault();

    const formData = new FormData(addCandidateForm);
    const id = addCandidateForm.dataset.id;  // Ambil ID calon yang diedit (jika ada)

    try {
      const endpoint = id ? `${BASE_URL}/calon/${id}` : `${BASE_URL}/calon/upload`;
      const method = id ? "PUT" : "POST";

      const response = await fetch(endpoint, {
        method: method,
        body: formData,
      });

      if (response.ok) {
        alert(id ? "Calon berhasil diperbarui!" : "Calon berhasil ditambahkan!");
        addCandidateForm.reset();
        delete addCandidateForm.dataset.id;  // Hapus ID setelah edit
        await loadCandidates(filterJenis.value);
      } else {
        const errorMessage = await response.text();
        alert("Gagal memperbarui/tambah calon: " + errorMessage);
      }
    } catch (error) {
      console.error("Gagal memperbarui/tambah calon:", error.message);
      alert("Terjadi kesalahan. Silakan coba lagi.");
    }
  });

  // **Handle Filter Change**
  filterJenis?.addEventListener("change", () => {
    loadCandidates(filterJenis.value);
  });

  // **Delete Candidate**
  window.deleteCandidate = async (id) => {
    if (confirm("Apakah Anda yakin ingin menghapus calon ini?")) {
      try {
        const response = await fetch(`${BASE_URL}/calon/${id}`, {
          method: "DELETE",
        });

        if (response.ok) {
          alert("Calon berhasil dihapus.");
          await loadCandidates(filterJenis.value);
        } else {
          alert("Gagal menghapus calon.");
        }
      } catch (error) {
        console.error("Gagal menghapus calon:", error.message);
      }
    }
  };

  // **Initial Load**
  loadCandidates();
  // **Logout Button**
  document.getElementById("logout-btn").addEventListener("click", () => {
    // Hapus data sesi
    sessionStorage.clear();
  
    // Redirect ke halaman login
    alert("Logout berhasil.");
    window.location.href = "index.html";
  });
  const rekapitulasiSection = document.getElementById("rekapitulasi-section");
  const filterDaerah = document.getElementById("filterDaerah");
  const loadRekapitulasiButton = document.getElementById("loadRekapitulasi");
  const rekapitulasiContainer = document.getElementById("rekapitulasi-container");

  // Fungsi untuk memuat rekapitulasi berdasarkan daerah
  const loadRekapitulasiByDaerah = async () => {
    const daerah = filterDaerah.value.trim();

    if (!daerah) {
      alert("Harap masukkan daerah pemilihan.");
      return;
    }

    try {
      const response = await fetch(`${BASE_URL}/rekapitulasi-daerah/${encodeURIComponent(daerah)}`);
      if (!response.ok) {
        if (response.status === 404) {
          alert("Tidak ada rekapitulasi untuk daerah pemilihan tersebut.");
          rekapitulasiContainer.innerHTML = "<p>Tidak ada data untuk daerah ini.</p>";
          return;
        } else {
          throw new Error(`Gagal memuat rekapitulasi: ${response.status}`);
        }
      }

      const candidates = await response.json();
      displayRekapitulasi(candidates);
    } catch (error) {
      console.error("Error:", error.message);
      alert("Terjadi kesalahan saat memuat rekapitulasi. Silakan coba lagi.");
    }
  };

  // Fungsi untuk menampilkan hasil rekapitulasi
  const displayRekapitulasi = (candidates) => {
    rekapitulasiContainer.innerHTML = ""; // Bersihkan container sebelumnya

    if (candidates.length === 0) {
      rekapitulasiContainer.innerHTML = "<p>Tidak ada data rekapitulasi.</p>";
      return;
    }

    const totalVotes = candidates.reduce((sum, candidate) => sum + candidate.totalSuara, 0);

    candidates.forEach((candidate) => {
      const percentage = ((candidate.totalSuara / totalVotes) * 100).toFixed(2);
      const candidateInfo = `
        <div style="border: 1px solid #ccc; padding: 10px; margin-bottom: 10px;">
          <img src="${BASE_URL}/uploads/${candidate.foto}" alt="${candidate.nama}" style="width: 60px; height: 60px; object-fit: cover;">
          <strong>${candidate.nama}</strong> (${candidate.partai})<br>
          Daerah: ${candidate.daerahPemilihan}<br>
          Visi: ${candidate.visiMisi.split('\n')[0]}<br>
          Total Suara: ${candidate.totalSuara} (${percentage}%)
        </div>
      `;
      rekapitulasiContainer.innerHTML += candidateInfo;
    });
  };

  // Event Listener untuk tombol "Tampilkan Rekapitulasi"
  loadRekapitulasiButton.addEventListener("click", loadRekapitulasiByDaerah);
});
