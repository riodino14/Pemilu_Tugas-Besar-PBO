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

  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = loginForm.querySelector("input[placeholder='Email']").value.trim();
    const password = loginForm.querySelector("input[placeholder='Password']").value.trim();

    if (!email || !password) {
      alert("Harap lengkapi semua kolom.");
      return;
    }

    try {
      const response = await fetch("http://localhost:8080/api/auth/login/admin", { // Perbaiki URL
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email, password: password }),
      });

      if (response.ok) {
        alert("Login berhasil!");
        sessionStorage.setItem("adminEmail", email); // Simpan email admin ke sessionStorage
        window.location.href = "admin.html"; // Redirect jika login berhasil
      } else {
        const error = await response.json();
        alert(error.error || "Login gagal.");
      }
    } catch (error) {
      console.error("Error login:", error.message);
      alert("Terjadi kesalahan. Silakan coba lagi.");
    }
  });

   // Fungsi untuk hashing password
  async function hashPassword(password) {
    const encoder = new TextEncoder();
    const data = encoder.encode(password);
    const hash = await crypto.subtle.digest('SHA-256', data);
    return Array.from(new Uint8Array(hash))
        .map((byte) => byte.toString(16).padStart(2, '0'))
        .join('');
  }

document.addEventListener("DOMContentLoaded", () => {
        
  const loginForm = document.querySelector(".sign-in form");

  loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();

      const email = loginForm.querySelector("input[placeholder='Email']").value.trim();
      const password = loginForm.querySelector("input[placeholder='Password']").value.trim();

      if (!email || !password) {
          alert("Harap lengkapi semua kolom.");
          return;
      }

      // Hash password
      const hashedPassword = await hashPassword(password);

      try {
          const response = await fetch("http://localhost:8080/api/auth/login/admin", {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({ email: email, password: hashedPassword }),
          });

          if (response.ok) {
              alert("Login berhasil!");
              window.location.href = "admin.html";
          } else {
              const error = await response.json();
              alert(error.message || "Login gagal.");
          }
      } catch (error) {
          console.error("Error login:", error.message);
          alert("Terjadi kesalahan. Silakan coba lagi.");
        }
    });
  });
});
