document.addEventListener("DOMContentLoaded", () => {
    const BASE_URL = "http://localhost:8080/api/pemilu"; // URL backend
    const loginForm = document.getElementById("login-form");
    const passwordField = document.getElementById("password-field");
    const registerLink = document.getElementById("register-link"); // Tautan registrasi pemilih
    const roleRadios = document.querySelectorAll('input[name="role"]');
  
    // Fungsi untuk memperbarui tampilan berdasarkan role yang dipilih
    const updateRoleUI = (role) => {
      if (role === "pemilih") {
        passwordField.style.display = "none";
        document.getElementById("password").removeAttribute("required");
        registerLink.style.display = "block";
      } else if (role === "admin") {
        passwordField.style.display = "block";
        document.getElementById("password").setAttribute("required", "required");
        registerLink.style.display = "none";
      }
    };
  
    roleRadios.forEach((radio) => {
      radio.addEventListener("change", (e) => updateRoleUI(e.target.value));
    });
  
    const selectedRole = document.querySelector('input[name="role"]:checked');
    if (selectedRole) updateRoleUI(selectedRole.value);
  
    loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();
  
      const selectedRole = document.querySelector('input[name="role"]:checked').value;
      const identifier = document.getElementById("identifier").value.trim();
      const password = document.getElementById("password").value.trim();
  
      if (!selectedRole || !identifier || (selectedRole === "admin" && !password)) {
        alert("Harap isi semua field yang diperlukan.");
        return;
      }
  
      const loginData = { role: selectedRole, identifier: identifier };
      if (selectedRole === "admin") loginData.password = password;
  
      try {
        const response = await fetch(`${BASE_URL}/login`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(loginData),
        });
  
        if (response.ok) {
          const data = await response.json();
          sessionStorage.setItem("identifier", identifier);
          sessionStorage.setItem("role", selectedRole);
          window.location.href = selectedRole === "admin" ? "admin.html" : "pemilih.html";
        } else {
          const errorResponse = await response.json();
          alert("Login gagal: " + errorResponse.message);
        }
      } catch (error) {
        console.error("Login error:", error);
        alert("Terjadi kesalahan saat login. Silakan coba lagi.");
      }
    });
  });
  