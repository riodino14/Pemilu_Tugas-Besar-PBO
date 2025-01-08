document.addEventListener("DOMContentLoaded", () => {
    const registrationForm = document.getElementById("registration-form");
    const backToLoginBtn = document.getElementById("back-to-login-btn");
  
    // Fungsi untuk menangani registrasi
    registrationForm.addEventListener("submit", async (e) => {
      e.preventDefault();
  
      const nik = document.getElementById("nik").value.trim();
      const nama = document.getElementById("nama").value.trim();
      const email = document.getElementById("email").value.trim();
      // const password = document.getElementById("password").value.trim();
  
      // if (!nik || !nama || !password) {
      //   alert("Harap lengkapi semua kolom.");
      //   return;
      // }
      if (!nik || !nama || !email) {
          alert("Harap lengkapi semua kolom.");
          return;
        }
    
      try {
        // const response = await fetch("http://localhost:8080/api/pemilu/register", {
        //   method: "POST",
        //   headers: { "Content-Type": "application/json" },
        //   body: JSON.stringify({ nik: nik, nama: nama, password: password }),
        // });
        const response = await fetch("http://localhost:8080/api/pemilu/register", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ nik: nik, nama: nama,email: email}),
        });
        const result = await response.json();
  
        if (response.ok) {
          alert("Registrasi berhasil. Silakan login.");
          window.location.href = "index.html"; // Arahkan ke halaman login setelah registrasi
        } else {
          alert(result.message || "Terjadi kesalahan saat registrasi.");
        }
      } catch (error) {
        console.error("Error registrasi:", error.message);
        alert("Terjadi kesalahan. Silakan coba lagi.");
      }
    });
  
    // Fungsi untuk tombol "Kembali ke Halaman Login"
    backToLoginBtn.addEventListener("click", () => {
      window.location.href = "index.html"; // Arahkan ke halaman login
    });
  });
  