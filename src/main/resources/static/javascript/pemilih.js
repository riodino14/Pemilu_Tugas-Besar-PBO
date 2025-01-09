document.addEventListener("DOMContentLoaded", () => {
    const BASE_URL = "http://localhost:8080/api/pemilih";
    const IMAGE_BASE_URL = "http://localhost:8080/uploads";

    const daerahInput = document.getElementById("daerah-input");
    const loadCandidatesButton = document.getElementById("load-candidates");
    const candidateList = document.getElementById("candidate-list");
    const voteButton = document.getElementById("vote-button");
    const rekapitulasiButton = document.getElementById("rekapitulasi-button");
    const candidateSection = document.getElementById("candidate-section");
    const votingSection = document.getElementById("voting-section");
    const voteForms = document.getElementById("vote-forms");
    const rekapitulasiContainer = document.getElementById("rekapitulasi-container");

    // Bagian filter
    const filterContainer = document.getElementById("filter-container");
    const filterJenisSelect = document.getElementById("filter-jenis");
    // Kita tidak ada tombol "Terapkan Filter" lagi

    let rekapCandidates = []; // Menyimpan data rekap

    /**
     * Fungsi untuk menampilkan popup pesan
     */
    function showPopup(message) {
        const popup = document.createElement("div");
        popup.className = "popup";
        popup.innerHTML = `
          <div class="popup-content">
            <p>${message}</p>
            <button class="popup-close">OK</button>
          </div>
        `;
        document.body.appendChild(popup);
        popup.querySelector(".popup-close").addEventListener("click", () => {
          document.body.removeChild(popup);
        });
    }

    // Logout
    document.getElementById("logout-btn").addEventListener("click", () => {
        sessionStorage.clear();
        showPopup("Logout telah berhasil.");
        window.location.href = "index.html";
    });

    /**
     * Fungsi untuk memeriksa status voting pemilih
     */
    const checkVotingStatus = async () => {
        try {
            const nik = sessionStorage.getItem("nik");
            if (!nik) {
                showPopup("NIK tidak ditemukan. Harap login kembali.");
                window.location.href = "index.html";
                return;
            }

            const response = await fetch(`${BASE_URL}/pemilih/${nik}`);
            if (!response.ok) {
                throw new Error("Gagal memeriksa status pemilih.");
            }

            const pemilih = await response.json();
            if (pemilih.sudahMemilih) {
                sessionStorage.setItem("hasVoted", "true");
                showPopup("Selamat datang kembali. Anda sudah memberikan suara.");
                loadRekapitulasi(); // Memuat rekap saat sudah pernah memilih
            } else {
                showPopup("Silakan memberikan suara terlebih dahulu.");
            }
        } catch (error) {
            console.error("Error:", error.message);
            showPopup("Terjadi kesalahan saat memeriksa status pemilih.");
        }
    };

    /**
     * Fungsi untuk memuat kandidat berdasarkan daerah pemilihan
     */
    const loadCandidates = async () => {
        const daerahPemilihan = daerahInput.value.trim();
        if (!daerahPemilihan) {
            showPopup("Harap masukkan daerah pemilihan.");
            return;
        }

        try {
            const response = await fetch(`${BASE_URL}/calon-daerah/${encodeURIComponent(daerahPemilihan)}`);
            if (!response.ok) {
                if (response.status === 404) {
                    showPopup("Tidak ada kandidat untuk daerah pemilihan tersebut.");
                    candidateList.innerHTML = "<p>Tidak ada kandidat untuk daerah ini.</p>";
                    return;
                } else {
                    throw new Error(`Gagal memuat kandidat: ${response.status}`);
                }
            }

            const candidates = await response.json();
            console.log("Candidates:", candidates);

            candidateList.innerHTML = ""; 
            voteForms.innerHTML = ""; 

            // Kelompokkan kandidat berdasarkan jenis
            const groupedCandidates = candidates.reduce((acc, candidate) => {
                if (!acc[candidate.jenis]) acc[candidate.jenis] = [];
                acc[candidate.jenis].push(candidate);
                return acc;
            }, {});

            // Tampilkan daftar
            Object.entries(groupedCandidates).forEach(([jenis, candidates]) => {
                const header = document.createElement("h3");
                header.textContent = jenis;
                candidateList.appendChild(header);

                candidates.forEach((candidate) => {
                    const listItem = document.createElement("li");
                    listItem.innerHTML = `
                        <div style="display: flex; align-items: center; margin-bottom: 10px;">
                            <img src="${IMAGE_BASE_URL}/${candidate.foto}" alt="${candidate.nama}" 
                                 style="width: 60px; height: 60px; margin-right: 10px; object-fit: cover;">
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
            showPopup("Terjadi kesalahan saat memuat kandidat. Silakan coba lagi.");
        }
    };

    /**
     * Fungsi untuk mengirimkan vote
     */
    const submitVote = async () => {
        const selectedCandidates = {};
        document.querySelectorAll(".candidate-select").forEach((select) => {
            const jenis = select.dataset.jenis;
            const candidateId = select.value;
            if (candidateId) {
                selectedCandidates[jenis] = candidateId;
            }
        });

        // Pastikan semua jenis terpilih
        const requiredJenis = ["DPR", "DPRD Provinsi", "DPRD Kotamadya"];
        const missingJenis = requiredJenis.filter(jenis => !selectedCandidates[jenis]);
        if (missingJenis.length > 0) {
            showPopup(`Harap pilih satu kandidat dari setiap jenis (${missingJenis.join(', ')}).`);
            return;
        }

        try {
            const nik = sessionStorage.getItem("nik");
            if (!nik) {
                showPopup("NIK tidak ditemukan. Harap login kembali.");
                window.location.href = "loginPemilih.html";
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
                showPopup("Terima kasih! Suara Anda telah tercatat.");
                sessionStorage.setItem("hasVoted", "true");
                showThankYouMessage();
            } else {
                const errorResponse = await response.json();
                showPopup("Gagal memberikan suara: " + errorResponse.message);
            }
        } catch (error) {
            console.error("Error:", error.message);
            showPopup("Terjadi kesalahan saat memberikan suara. Silakan coba lagi.");
        }
    };

    /**
     * Fungsi menampilkan ucapan terima kasih dan tombol rekapitulasi
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
     * Fungsi memuat rekapitulasi suara
     */
    const loadRekapitulasi = async () => {
        try {
            const daerahPemilihan = daerahInput.value.trim();
            if (!daerahPemilihan) {
                showPopup("Harap masukkan daerah pemilihan.");
                return;
            }

            const response = await fetch(`${BASE_URL}/rekapitulasi-daerah/${encodeURIComponent(daerahPemilihan)}`);
            if (!response.ok) {
                throw new Error(`Gagal memuat rekapitulasi: ${response.status}`);
            }

            // Simpan data rekap di variabel global
            rekapCandidates = await response.json();

            // Tampilkan filter & data rekap pertama kali
            filterContainer.style.display = "block";
            displayRekapitulasi(rekapCandidates);
        } catch (error) {
            console.error("Error:", error.message);
            showPopup("Terjadi kesalahan saat memuat rekapitulasi. Silakan coba lagi.");
        }
    };

    /**
     * Menampilkan rekapitulasi suara dalam bentuk tabel
     */
    const displayRekapitulasi = (candidates) => {
        const totalVotes = candidates.reduce((sum, candidate) => sum + candidate.totalSuara, 0);
        rekapitulasiContainer.innerHTML = "";

        let tableHTML = `
            <table border="1" style="width:100%; border-collapse: collapse; text-align: left;">
                <thead>
                    <tr style="background-color: #f2f2f2;">
                        <th style="padding: 8px;">Nama</th>
                        <th style="padding: 8px;">Jenis Caleg</th>
                        <th style="padding: 8px;">Partai</th>
                        <th style="padding: 8px;">Daerah</th>
                        <th style="padding: 8px;">Visi dan Misi</th>
                        <th style="padding: 8px;">Total Suara</th>
                        <th style="padding: 8px;">Persentase</th>
                    </tr>
                </thead>
                <tbody>
        `;
        candidates.forEach(candidate => {
            const percentage = totalVotes > 0 
                ? ((candidate.totalSuara / totalVotes) * 100).toFixed(2)
                : "0.00";

            tableHTML += `
                <tr>
                    <td style="padding: 8px;">
                        <img src="${IMAGE_BASE_URL}/${candidate.foto}" 
                             alt="${candidate.nama}" 
                             style="width: 50px; height: 50px; object-fit: cover; vertical-align: middle; margin-right: 8px;">
                        <strong>${candidate.nama}</strong>
                    </td>
                    <td style="padding: 8px;">${candidate.jenis}</td>
                    <td style="padding: 8px;">${candidate.partai}</td>
                    <td style="padding: 8px;">${candidate.daerahPemilihan}</td>
                    <td style="padding: 8px;">${candidate.visiMisi}</td>
                    <td style="padding: 8px;">${candidate.totalSuara}</td>
                    <td style="padding: 8px;">${percentage}%</td>
                </tr>
            `;
        });
        tableHTML += `
                </tbody>
            </table>
        `;
        rekapitulasiContainer.innerHTML = tableHTML;
    };

    /**
     * Fungsi untuk menerapkan filter jenis (dipanggil setiap dropdown "change")
     */
    const applyJenisFilter = () => {
        const selectedJenis = filterJenisSelect.value;
        if (!selectedJenis) {
            // Jika dropdown = "Semua", tampilkan keseluruhan data
            displayRekapitulasi(rekapCandidates);
        } else {
            // Filter data rekapitulasi
            const filteredData = rekapCandidates.filter(c => c.jenis === selectedJenis);
            displayRekapitulasi(filteredData);
        }
    };

    // **Event Listener**
    loadCandidatesButton.addEventListener("click", loadCandidates);
    voteButton.addEventListener("click", submitVote);
    rekapitulasiButton.addEventListener("click", loadRekapitulasi);

    // == Inilah kuncinya: event "change" pada dropdown ==
    filterJenisSelect.addEventListener("change", applyJenisFilter);

    checkVotingStatus();
});
