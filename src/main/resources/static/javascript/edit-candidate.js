// edit-candidate.js
document.addEventListener("DOMContentLoaded", async () => {
  const ADMIN_BASE_URL = "http://localhost:8080/api/admin";
  const CALON_BASE_URL = `${ADMIN_BASE_URL}/calon`;

  const urlParams = new URLSearchParams(window.location.search);
  const candidateId = urlParams.get("id");
  const editCandidateForm = document.getElementById("edit-candidate-form");

  // **Load Candidate Data**
  const loadCandidateData = async (id) => {
    try {
      if (!id) {
        console.error("ID calon tidak ditemukan.");
        alert("ID calon tidak valid.");
        window.location.href = "admin.html"; // Redirect ke halaman admin jika ID tidak valid
        return;
      }

      const response = await fetch(`${CALON_BASE_URL}/${id}`);
      if (!response.ok) {
        throw new Error(`Server Error: ${response.status} - ${response.statusText}`);
      }

      const candidate = await response.json();

      // Populate form fields
      document.getElementById("nama").value = candidate.nama || "";
      document.getElementById("partai").value = candidate.partai || "";
      document.getElementById("daerahPemilihan").value = candidate.daerahPemilihan || "";
      document.getElementById("jenis").value = candidate.jenis || "";
      document.getElementById("visiMisi").value = candidate.visiMisi || "";

      // Menampilkan foto saat ini
      const currentFoto = document.getElementById("current-foto");
      if (currentFoto) { // Pastikan elemen ada
        if (candidate.foto) {
          currentFoto.src = `http://localhost:8080/uploads/${candidate.foto}`;
          currentFoto.alt = candidate.nama;
        } else {
          currentFoto.src = 'default.jpg';
          currentFoto.alt = 'Foto Tidak Tersedia';
        }
      } else {
        console.error("Elemen 'current-foto' tidak ditemukan di DOM.");
      }
    } catch (error) {
      console.error("Gagal memuat data calon:", error.message);
      alert("Gagal memuat data calon. Detail error: " + error.message);
    }
  };

  // **Handle Form Submission**
  editCandidateForm?.addEventListener("submit", async (e) => {
    e.preventDefault();

    const formData = new FormData(editCandidateForm);
    const nama = document.getElementById("nama").value.trim();
    const partai = document.getElementById("partai").value.trim();
    const daerahPemilihan = document.getElementById("daerahPemilihan").value.trim();
    const visiMisi = document.getElementById("visiMisi").value.trim();
    const jenis = document.getElementById("jenis").value;
    const foto = document.getElementById("foto").files[0];

    // Validasi input (opsional)
    if (!nama || !partai || !daerahPemilihan || !visiMisi || !jenis) {
      alert("Harap isi semua kolom yang wajib.");
      return;
    }

    try {
      // Mengirim data form ke server
      const response = await fetch(`${CALON_BASE_URL}/${candidateId}`, {
        method: "PUT",
        body: formData,
      });

      if (response.ok) {
        alert("Data calon berhasil diperbarui!");
        window.location.href = "admin.html"; // Redirect ke halaman admin
      } else {
        // Menangani error yang berasal dari server
        const errorMessage = await response.json();
        alert("Gagal memperbarui data calon: " + errorMessage.message);
      }
    } catch (error) {
      console.error("Gagal memperbarui data calon:", error.message);
      alert("Terjadi kesalahan. Silakan coba lagi.");
    }
  });

  // Load candidate data on page load
  if (candidateId) {
    await loadCandidateData(candidateId);
  } else {
    alert("ID calon tidak ditemukan!");
    window.location.href = "admin.html";
  }
});
