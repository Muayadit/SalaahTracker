// This waits for the HTML page to be fully loaded before running any JS
document.addEventListener("DOMContentLoaded", () => {

    // Get all the important HTML elements
    const loginForm = document.getElementById("login-form");
    const loginUsername = document.getElementById("login-username");
    const loginPassword = document.getElementById("login-password");
    const loginMessage = document.getElementById("login-message");

    const authContainer = document.getElementById("auth-container");
    const appContainer = document.getElementById("app-container");
    const welcomeMessage = document.getElementById("welcome-message");

    // --- LOGIN ---
    // Add an event listener to the login form
    loginForm.addEventListener("submit", (event) => {
        // 1. Prevent the form from reloading the page (its default behavior)
        event.preventDefault();

        // 2. Create an object with the data from the form
        //    NOTE: The keys 'username' and 'password' *must* match
        //    what your Java server expects (ctx.formParam("username"))
        const formData = new URLSearchParams();
        formData.append("username", loginUsername.value);
        formData.append("password", loginPassword.value);

        // 3. Send the data to the Java server's '/api/login' endpoint
        fetch("/api/login", {
            method: "POST",
            body: formData
        })
        .then(response => {
            // 4. Check if the server response is OK (status 200-299)
            if (response.ok) {
                return response.json(); // Server said success (200)
            } else {
                // Server said failure (like 401 Unauthorized)
                return response.json().then(errorData => {
                    // Throw an error to be caught by the .catch() block
                    throw new Error(errorData.message);
                });
            }
        })
        .then(data => {
            // 5. We are here if the login was successful!
            //    The 'data' object is the JSON from your Java server:
            //    { status: "success", username: "muayad" }
            
            console.log("Login successful:", data);
            
            // Hide login form and show the main app
            authContainer.classList.add("hidden");
            appContainer.classList.remove("hidden");
            welcomeMessage.textContent = `Welcome, ${data.username}!`;
            
            // We'll add a function here later to load the prayers
            // loadPrayers(); 
        })
        .catch(error => {
            // 6. We are here if the fetch failed or if the server
            //    sent a failure message (like "Wrong username or password")
            console.error("Login failed:", error);
            loginMessage.textContent = error.message;
        });
    });

    // We will add logic for Register, Logout, and Prayer fetching here...

});