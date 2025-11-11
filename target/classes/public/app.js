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

    const registerForm = document.getElementById("register-form");
    const registerUsername = document.getElementById("register-username");
    const registerPassword = document.getElementById("register-password");
    const registerMessage = document.getElementById("register-message");

    const prayerList = document.getElementById("prayer-list");

    const logoutButton = document.getElementById("logout-button");

    const viewSummaryButton = document.getElementById("view-summary-button");
    const summaryContainer = document.getElementById("summary-container");
    const backToPrayersButton = document.getElementById("back-to-prayers-button");
    const summaryForm = document.getElementById("summary-form");
    const summaryMonth = document.getElementById("summary-month");
    const summaryYear = document.getElementById("summary-year");
    const summaryResults = document.getElementById("summary-results");


    // --- FUNCTION TO LOAD PRAYERS ---
function loadPrayers() {
    // Clear any old prayers
    prayerList.innerHTML = "";

    // 1. Fetch data from our new Java endpoint
    fetch("/api/prayers/today")
        .then(response => {
            if (!response.ok) {
                throw new Error("Could not get prayer list. Are you logged in?");
            }
            return response.json();
        })
        .then(prayers => {
            // 'prayers' is now a JSON array from our server
            // [ { "id": 1, "prayerName": "Fajr", "completed": false }, ... ]

            // 2. Loop through each prayer and create HTML for it
            prayers.forEach(prayer => {
                // Create a new div for the prayer
                const prayerElement = document.createElement("div");
                prayerElement.classList.add("prayer-item");

                // Create the text
                const prayerName = document.createElement("span");
                prayerName.textContent = prayer.prayerName;
                
                // Create the checkbox
                const checkbox = document.createElement("input");
                checkbox.type = "checkbox";
                checkbox.checked = prayer.completed;

                // --- THIS IS THE NEW CODE ---
                
                // A. Store the prayer's ID directly on the checkbox
                //    so we know which one to update
                checkbox.dataset.prayerId = prayer.id;

                // B. Disable the checkbox if it's already completed
                if (prayer.completed) {
                    checkbox.disabled = true;
                }

                // C. Add a 'change' event listener (fires on click)
                checkbox.addEventListener('change', () => {
                    // Only run this if we are CHECKING the box
                    if (checkbox.checked) {
                        const prayerId = checkbox.dataset.prayerId;

                        // Disable the box immediately so you can't click it twice
                        checkbox.disabled = true;

                        // Send the update to our new Java endpoint!
                        fetch("/api/prayers/complete/" + prayerId, {
                            method: "PUT"
                        })
                        .then(response => {
                            if (!response.ok) {
                                // If the server sends an error, throw it
                                throw new Error("Server-side update failed");
                            }
                            return response.json();
                        })
                        .then(data => {
                            // The server said success!
                            console.log("Prayer update success:", data.message);
                            // The checkbox is already checked and disabled,
                            // so we don't need to do anything else.
                        })
                        .catch(error => {
                            // If the update failed, log it and re-enable the box
                            console.error("Prayer update failed:", error);
                            checkbox.disabled = false;
                            checkbox.checked = false; // Uncheck it
                        });
                    }
                });
                // --- END OF NEW CODE ---

                // Add the checkbox and text to the prayer div
                prayerElement.appendChild(checkbox);
                prayerElement.appendChild(prayerName);

                // Add the new prayer div to the main list
                prayerList.appendChild(prayerElement);
            });
        })
        .catch(error => {
            console.error("Error loading prayers:", error);
            prayerList.textContent = "Could not load prayers.";
        });
}

viewSummaryButton.addEventListener("click", () => {
        // Hide the main app, show the summary
        appContainer.classList.add("hidden");
        summaryContainer.classList.remove("hidden");
        
        // Let's also populate the year dropdown
        populateYearDropdown();
    });

    backToPrayersButton.addEventListener("click", () => {
        // Hide the summary, show the main app
        summaryContainer.classList.add("hidden");
        appContainer.classList.remove("hidden");
        
        // Refresh the prayer list just in case
        loadPrayers();
    });

    // This helper function fills the year dropdown
    function populateYearDropdown() {
        summaryYear.innerHTML = ""; // Clear it
        const currentYear = new Date().getFullYear();
        for (let i = currentYear; i >= currentYear - 5; i--) {
            const option = document.createElement("option");
            option.value = i;
            option.textContent = i;
            summaryYear.appendChild(option);
        }
    }

    summaryForm.addEventListener("submit", (event) => {
        event.preventDefault(); // Stop page reload

        const year = summaryYear.value;
        const month = summaryMonth.value;

        // Show a loading message
        summaryResults.innerHTML = "<p>Loading summary...</p>";

        // 1. Fetch the data from our new Java endpoint
        fetch(`/api/summary/monthly?year=${year}&month=${month}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error("Could not load summary.");
                }
                return response.json();
            })
            .then(prayers => {
                // 2. Process the data (just like your console app!)
                if (prayers.length === 0) {
                    summaryResults.innerHTML = "<p>No data found for this month.</p>";
                    return;
                }

                // This object will hold our grouped data
                // e.g., { "2025-11-01": { completed: 2, total: 5 }, ... }
                const prayersByDay = {};

                prayers.forEach(p => {
                    const date = p.prayerDate;
                    
                    // If we haven't seen this day yet, create it
                    if (!prayersByDay[date]) {
                        prayersByDay[date] = { completed: 0, total: 0 };
                    }
                    
                    prayersByDay[date].total++; // Increment total
                    if (p.completed) {
                        prayersByDay[date].completed++; // Increment completed
                    }
                });

                // 3. Display the processed data
                summaryResults.innerHTML = ""; // Clear the "Loading..."
                
                // Get all the dates, sort them, and create HTML
                const sortedDays = Object.keys(prayersByDay).sort();

                sortedDays.forEach(day => {
                    const data = prayersByDay[day];
                    const dayElement = document.createElement("div");
                    dayElement.classList.add("summary-day-item");
                    
                    let status = "Partial";
                    if (data.completed === data.total) {
                        status = "All Complete";
                    } else if (data.completed === 0) {
                        status = "None";
                    }

                    dayElement.innerHTML = `
                        <strong>${day}</strong> 
                        --- [${status}] --- 
                        (${data.completed}/${data.total}) prayers
                    `;
                    summaryResults.appendChild(dayElement);
                });

            })
            .catch(error => {
                summaryResults.innerHTML = `<p class="error-message">${error.message}</p>`;
            });
    });

    

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
            loadPrayers(); 
        })
        .catch(error => {
            // 6. We are here if the fetch failed or if the server
            //    sent a failure message (like "Wrong username or password")
            console.error("Login failed:", error);
            loginMessage.textContent = error.message;
        });
    });

    registerForm.addEventListener("submit", (event) => {
        // 1. Prevent the form from reloading the page
        event.preventDefault();

        // 2. Create the data object
        const formData = new URLSearchParams();
        formData.append("username", registerUsername.value);
        formData.append("password", registerPassword.value);

        // 3. Send the data to the new '/api/register' endpoint
        fetch("/api/register", {
            method: "POST",
            body: formData
        })
        .then(response => {
            // 4. Check the response
            return response.json().then(data => {
                if (!response.ok) {
                    // If response is not ok (like 409 Conflict), throw an error
                    throw new Error(data.message);
                }
                return data; // This is the success data
            });
        })
        .then(data => {
            // 5. We are here if registration was successful!
            //    data = { status: "success", message: "Registration successful! ..." }
            console.log("Registration successful:", data);
            
            // Clear any old error messages
            registerMessage.classList.remove("error-message");
            registerMessage.classList.add("success-message");
            registerMessage.textContent = data.message;
            
            // Clear the form fields
            registerForm.reset();
            loginMessage.textContent = ""; // Clear login error too
        })
        .catch(error => {
            // 6. We are here if the registration failed
            //    (e.g., "Username is already taken")
            console.error("Registration failed:", error);
            
            // Show the error message
            registerMessage.classList.remove("success-message");
            registerMessage.classList.add("error-message");
            registerMessage.textContent = error.message;
        });
    });

    logoutButton.addEventListener("click", () => {
    // Send a request to our new logout endpoint
    fetch("/api/logout", {
        method: "POST"
    })
    .then(response => response.json())
    .then(data => {
        console.log("Logout:", data.message);

        // Reload the page. Since the session is destroyed,
        // the server will send us back to the login screen.
        window.location.reload();
    })
    .catch(error => {
        console.error("Logout failed:", error);
    });
});

});