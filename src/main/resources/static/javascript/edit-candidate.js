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


});
