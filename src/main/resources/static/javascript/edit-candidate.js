document.addEventListener("DOMContentLoaded", async () => {
  const BASE_URL = "http://localhost:8080/api/pemilu"; // URL backend Spring Boot
  const urlParams = new URLSearchParams(window.location.search);
  const candidateId = urlParams.get("id");
  const editCandidateForm = document.getElementById("edit-candidate-form");

  // **Load Candidate Data**
  const loadCandidateData = async (id) => {
    try {
      const response = await fetch(`${BASE_URL}/calon/${id}`);
      if (!response.ok) {
        throw new Error(`Server Error: ${response.status}`);
      }

      const candidate = await response.json();

      // Populate form fields
      document.getElementById("nama").value = candidate.nama || "";
      document.getElementById("partai").value = candidate.partai || "";
      document.getElementById("daerahPemilihan").value = candidate.daerahPemilihan || "";
      document.getElementById("jenis").value = candidate.jenis || "";
      document.getElementById("visiMisi").value = candidate.visiMisi || "";
    } catch (error) {
      console.error("Gagal memuat data calon:", error.message);
      alert("Gagal memuat data calon.");
    }
  };

  // **Handle Form Submission**
  editCandidateForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const formData = new FormData();
    const nama = document.getElementById("nama").value.trim();
    const partai = document.getElementById("partai").value.trim();
    const daerahPemilihan = document.getElementById("daerahPemilihan").value.trim();
    const visiMisi = document.getElementById("visiMisi").value.trim();
    const jenis = document.getElementById("jenis").value;
    const foto = document.getElementById("foto").files[0];

    // Append only fields with values
    if (nama) formData.append("nama", nama);
    if (partai) formData.append("partai", partai);
    if (daerahPemilihan) formData.append("daerahPemilihan", daerahPemilihan);
    if (visiMisi) formData.append("visiMisi", visiMisi);
    if (jenis) formData.append("jenis", jenis);
    if (foto) formData.append("foto", foto);

    try {
      const response = await fetch(`${BASE_URL}/calon/${candidateId}`, {
        method: "PUT",
        body: formData,
      });

      if (response.ok) {
        alert("Data calon berhasil diperbarui!");
        window.location.href = "admin.html"; // Redirect ke halaman admin
      } else {
        const errorMessage = await response.text();
        alert("Gagal memperbarui data calon: " + errorMessage);
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
