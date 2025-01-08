// Fungsi untuk hashing password
async function hashPassword(password) {
  const encoder = new TextEncoder();
  const data = encoder.encode(password);
  const hash = await crypto.subtle.digest('SHA-256', data);
  return Array.from(new Uint8Array(hash))
      .map((byte) => byte.toString(16).padStart(2, '0'))
      .join('');
}

// Fungsi untuk mengatur toggle antara login dan register
const container = document.getElementById("container");
const registerBtn = document.getElementById("register");
const loginBtn = document.getElementById("login");

// Toggle ke mode registrasi
registerBtn.addEventListener("click", () => {
  container.classList.add("active");
});

// Toggle ke mode login
loginBtn.addEventListener("click", () => {
  container.classList.remove("active");
});

// Setelah DOM selesai dimuat
document.addEventListener("DOMContentLoaded", () => {
  const togglePasswordIcons = document.querySelectorAll(".toggle-password");

  togglePasswordIcons.forEach((icon) => {
    icon.addEventListener("click", (e) => {
      const input = e.target.previousElementSibling;
      if (input.type === "password") {
        input.type = "text";
        e.target.classList.remove("fa-eye-slash");
        e.target.classList.add("fa-eye");
      } else {
        input.type = "password";
        e.target.classList.remove("fa-eye");
        e.target.classList.add("fa-eye-slash");
      }
    });
  });

  const loginForm = document.querySelector(".sign-in form");
  const registerForm = document.querySelector(".sign-up form");

  // Fungsi untuk login
  loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();

      // Ambil input dari form login
      const nik = loginForm.querySelector("input[placeholder='NIK']").value.trim();
      const email = loginForm.querySelector("input[placeholder='Email']").value.trim();
      const password = loginForm.querySelector("input[placeholder='Password']").value.trim();

      // Validasi input
      if (!nik || !email || !password) {
          alert("Harap lengkapi semua kolom.");
          return;
      }

      // Hash password sebelum dikirim ke server
      const hashedPassword = await hashPassword(password);

      try {
          // Kirim data ke endpoint login user backend
          const response = await fetch("http://localhost:8080/api/auth/login/user", {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({ nik: nik, email: email, password: hashedPassword }),
          });

          // Periksa respons
          if (response.ok) {
              alert("Login berhasil!");
              sessionStorage.setItem("nik", nik); // Simpan NIK di sessionStorage
              sessionStorage.setItem("hasVoted", "false"); // Simpan status voting
              window.location.href = "pemilih.html";
          } else {
              const error = await response.json();
              alert(error.message || "Login gagal.");
          }
      } catch (error) {
          console.error("Error login:", error.message);
          alert("Terjadi kesalahan. Silakan coba lagi.");
      }
  });

  // Fungsi untuk registrasi
  registerForm.addEventListener("submit", async (e) => {
      e.preventDefault();

      // Ambil input dari form registrasi
      const nama = registerForm.querySelector("input[placeholder='Nama']").value.trim();
      const nik = registerForm.querySelector("input[placeholder='NIK']").value.trim();
      const noTelp = registerForm.querySelector("input[placeholder='Nomor Telpon']").value.trim();
      const email = registerForm.querySelector("input[placeholder='Email']").value.trim();
      const password = registerForm.querySelector("input[placeholder='Password']").value.trim();

      // Validasi input
      if (!nama || !nik || !noTelp || !email || !password) {
          alert("Harap lengkapi semua kolom.");
          return;
      }

      // Validasi panjang NIK
      if (nik.length !== 16 || isNaN(nik)) {
          alert("NIK harus terdiri dari 16 angka.");
          return;
      }

      // Validasi panjang nomor telepon
      if (noTelp.length < 11 || noTelp.length > 13 || isNaN(noTelp)) {
          alert("Nomor telepon harus terdiri dari 11 hingga 13 angka.");
          return;
      }

      // Validasi format email
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // Regex untuk memeriksa adanya '@' dan '.'
      if (!emailRegex.test(email)) {
          alert("Email tidak valid. Pastikan email memiliki format yang benar, misalnya 'example@domain.com'.");
          return;
      }

      

      // Hash password sebelum dikirim ke server
      const hashedPassword = await hashPassword(password);

      try {
          // Kirim data ke endpoint register backend
          const response = await fetch("http://localhost:8080/api/auth/register", {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({
                  nama: nama,
                  nik: nik,
                  noTelp: noTelp,
                  email: email,
                  password: hashedPassword,
              }),
          });

          // Periksa respons
          if (response.ok) {
              alert("Registrasi berhasil. Silakan login.");
              // Kembali ke mode login
              container.classList.remove("active");
          } else {
              const error = await response.json();
              alert(error.message || "Registrasi gagal.");
          }
      } catch (error) {
          console.error("Error registrasi:", error.message);
          alert("Terjadi kesalahan. Silakan coba lagi.");
      }
  });
});
