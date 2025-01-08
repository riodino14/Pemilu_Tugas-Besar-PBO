document.addEventListener("DOMContentLoaded", () => {
    const BASE_URL = "http://localhost:8080/api/pemilu"; // URL backend
    const daerahInput = document.getElementById("daerah-input");
    const loadCandidatesButton = document.getElementById("load-candidates");
    const candidateList = document.getElementById("candidate-list");
    const voteButton = document.getElementById("vote-button");
    const candidateSection = document.getElementById("candidate-section");
    const votingSection = document.getElementById("voting-section");
    const voteForms = document.getElementById("vote-forms");
    const rekapitulasiSection = document.getElementById("rekapitulasi-section");
    const rekapitulasiContainer = document.createElement("div");

    let daerahPemilih = ""; // Variabel untuk menyimpan daerah pemilih

    // Cek apakah pemilih sudah memberikan suara dari backend
    const checkVotingStatus = async () => {
        try {
            const nik = sessionStorage.getItem("identifier");
            if (!nik) {
                alert("NIK tidak ditemukan. Harap login kembali.");
                window.location.href = "index.html";
                return;
            }

            const response = await fetch(`${BASE_URL}/pemilih/${nik}`);
            if (!response.ok) {
                throw new Error("Gagal memeriksa status pemilih.");
            }

            const pemilih = await response.json();
            daerahPemilih = pemilih.daerahPemilihan; // Simpan daerah pemilih

            if (pemilih.sudahMemilih) {
                sessionStorage.setItem("hasVoted", "true");
                alert("Anda sudah memberikan suara.");
                loadCandidatesButton.disabled = true;
                voteButton.disabled = true;
                showThankYouMessage();
            }
        } catch (error) {
            console.error("Error:", error.message);
        }
    };

    /**
     * Fungsi untuk memuat kandidat berdasarkan daerah pemilihan
     */
    const loadCandidates = async () => {
        const daerahPemilihan = daerahPemilih || daerahInput.value.trim();

        if (!daerahPemilihan) {
            alert("Harap masukkan daerah pemilihan.");
            return;
        }

        try {
            const response = await fetch(`${BASE_URL}/calon-daerah/${encodeURIComponent(daerahPemilihan)}`);
            if (!response.ok) {
                if (response.status === 404) {
                    alert("Tidak ada kandidat untuk daerah pemilihan tersebut.");
                    candidateList.innerHTML = "<p>Tidak ada kandidat untuk daerah ini.</p>";
                    return;
                } else {
                    throw new Error(`Gagal memuat kandidat: ${response.status}`);
                }
            }

            const candidates = await response.json();
            candidateList.innerHTML = ""; // Bersihkan daftar sebelumnya
            voteForms.innerHTML = ""; // Bersihkan form voting sebelumnya

            // Kelompokkan kandidat berdasarkan jenis jabatan
            const groupedCandidates = candidates.reduce((acc, candidate) => {
                if (!acc[candidate.jenis]) acc[candidate.jenis] = [];
                acc[candidate.jenis].push(candidate);
                return acc;
            }, {});

            // Perbarui bagian daftar kandidat
            Object.entries(groupedCandidates).forEach(([jenis, candidates]) => {
                const header = document.createElement("h3");
                header.textContent = jenis;
                candidateList.appendChild(header);

                candidates.forEach((candidate) => {
                    const listItem = document.createElement("li");
                    listItem.innerHTML = `
                        <div style="display: flex; align-items: center; margin-bottom: 10px;">
                            <img src="${BASE_URL}/uploads/${candidate.foto}" alt="${candidate.nama}" style="width: 60px; height: 60px; margin-right: 10px; object-fit: cover;">
                            <div>
                                <strong>${candidate.nama}</strong> (${candidate.partai})<br>
                                Daerah: ${candidate.daerahPemilihan}<br>
                                Visi: ${candidate.visiMisi.split('\n')[0]}<br>
                                Misi: ${candidate.visiMisi.split('\n').slice(1).join(', ')}
                            </div>
                        </div>
                    `;
                    candidateList.appendChild(listItem);
                });

                // Tambahkan form voting
                const formContainer = document.createElement("div");
                formContainer.innerHTML = `<h4>Pilih untuk ${jenis}</h4>`;
                const select = document.createElement("select");
                select.classList.add("candidate-select");
                select.dataset.jenis = jenis;
                select.innerHTML = `<option value="">Pilih Kandidat</option>`;

                candidates.forEach((candidate) => {
                    const option = document.createElement("option");
                    option.value = candidate.id;
                    option.textContent = `${candidate.nama} (${candidate.partai})`;
                    select.appendChild(option);
                });

                formContainer.appendChild(select);
                voteForms.appendChild(formContainer);
            });

            candidateSection.style.display = "block";
            votingSection.style.display = "block";
        } catch (error) {
            console.error("Error:", error.message);
            alert("Terjadi kesalahan saat memuat kandidat. Silakan coba lagi.");
        }
    };

    /**
     * Fungsi untuk mengirimkan vote
     */
    const submitVote = async () => {
        const selectedCandidates = {}; // Untuk menyimpan kandidat berdasarkan jenis

        // Ambil semua kandidat yang dipilih
        document.querySelectorAll(".candidate-select").forEach((select) => {
            const jenis = select.dataset.jenis; // Ambil jenis kandidat (DPR, DPRD, dll)
            const candidateId = select.value;

            if (candidateId) {
                selectedCandidates[jenis] = candidateId;
            }
        });

        // Pastikan semua jenis memiliki pilihan
        if (!selectedCandidates["DPR"] || !selectedCandidates["DPRD Provinsi"] || !selectedCandidates["DPRD Kotamadya"]) {
            alert("Harap pilih satu kandidat dari setiap jenis (DPR, DPRD Provinsi, DPRD Kotamadya).");
            return;
        }

        try {
            const nik = sessionStorage.getItem("identifier");
            if (!nik) {
                alert("NIK tidak ditemukan. Harap login kembali.");
                window.location.href = "index.html";
                return;
            }

            const votes = Object.entries(selectedCandidates).map(([jenis, candidateId]) => ({
                nik: nik,
                candidateId: candidateId,
            }));

            const response = await fetch(`${BASE_URL}/vote/batch`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(votes),
            });

            if (response.ok) {
                alert("Terima kasih! Suara Anda telah tercatat.");
                sessionStorage.setItem("hasVoted", "true");
                showThankYouMessage();
            } else {
                const errorResponse = await response.json();
                alert("Gagal memberikan suara: " + errorResponse.message);
            }
        } catch (error) {
            console.error("Error:", error.message);
            alert("Terjadi kesalahan saat memberikan suara. Silakan coba lagi.");
        }
    };

    /**
     * Fungsi untuk menampilkan ucapan terima kasih dan tombol rekapitulasi
     */
    const showThankYouMessage = () => {
        votingSection.innerHTML = `
            <h2>Terima Kasih!</h2>
            <p>Suara Anda telah tercatat. Klik tombol di bawah ini untuk melihat hasil rekapitulasi suara:</p>
            <button id="lihat-rekapitulasi">Lihat Rekapitulasi</button>
        `;

        const rekapitulasiButton = document.getElementById("lihat-rekapitulasi");
        rekapitulasiButton.addEventListener("click", loadRekapitulasi);
    };

    /**
     * Fungsi untuk memuat rekapitulasi suara berdasarkan daerah pemilih
     */
    const loadRekapitulasi = async () => {
        try {
            const daerahPemilih = daerahInput.value.trim(); // Ambil input daerah pemilihan
            if (!daerahPemilih) {
                alert("Harap masukkan daerah pemilihan.");
                return;
            }
    
            const response = await fetch(`${BASE_URL}/rekapitulasi-daerah/${encodeURIComponent(daerahPemilih)}`);
            if (!response.ok) {
                throw new Error(`Gagal memuat rekapitulasi: ${response.status}`);
            }
    
            const candidates = await response.json();
            displayRekapitulasi(candidates);
        } catch (error) {
            console.error("Error:", error.message);
            alert("Terjadi kesalahan saat memuat rekapitulasi. Silakan coba lagi.");
        }
    };

    /**
     * Fungsi untuk menampilkan rekapitulasi suara
     */
    const displayRekapitulasi = (candidates) => {
        const totalVotes = candidates.reduce((sum, candidate) => sum + candidate.totalSuara, 0);
    
        rekapitulasiContainer.innerHTML = ""; // Bersihkan rekapitulasi sebelumnya
    
        candidates.forEach((candidate) => {
            const percentage = candidate.persentaseSuara.toFixed(2);
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
    
        rekapitulasiSection.style.display = "block";
        rekapitulasiSection.appendChild(rekapitulasiContainer);
    };
    
    // Event Listener
    loadCandidatesButton.addEventListener("click", loadCandidates);
    voteButton.addEventListener("click", submitVote);

    // Periksa status voting saat halaman dimuat
    checkVotingStatus();
});
