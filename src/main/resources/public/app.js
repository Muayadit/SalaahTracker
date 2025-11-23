document.addEventListener("DOMContentLoaded", () => {

    // --- ELEMENTS ---
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

    // Monthly Elements
    const viewSummaryButton = document.getElementById("view-summary-button");
    const summaryContainer = document.getElementById("summary-container");
    const backToPrayersButton = document.getElementById("back-to-prayers-button");
    const summaryForm = document.getElementById("summary-form");
    const summaryMonth = document.getElementById("summary-month");
    const summaryYear = document.getElementById("summary-year");
    const summaryResults = document.getElementById("summary-results");

    // Weekly Elements
    const viewWeeklyButton = document.getElementById("view-weekly-button");
    const weeklyContainer = document.getElementById("weekly-container");
    const backFromWeeklyButton = document.getElementById("back-from-weekly-button");
    const weeklyResults = document.getElementById("weekly-results");
    
    // NEW WEEKLY NAV ELEMENTS
    const prevWeekBtn = document.getElementById("prev-week-btn");
    const nextWeekBtn = document.getElementById("next-week-btn");
    const currentWeekLabel = document.getElementById("current-week-label");

    // STATE VARIABLE FOR WEEKLY VIEW
    let currentWeeklyStartDate = new Date();


    // --- SHARED FUNCTIONS ---

    function populateYearDropdown() {
        summaryYear.innerHTML = ""; 
        const currentYear = new Date().getFullYear();
        for (let i = currentYear; i >= currentYear - 5; i--) {
            const option = document.createElement("option");
            option.value = i;
            option.textContent = i;
            summaryYear.appendChild(option);
        }
    }

    function loadPrayers() {
        prayerList.innerHTML = "";
        fetch("/api/prayers/today")
            .then(response => {
                if (!response.ok) throw new Error("Could not get prayer list.");
                return response.json();
            })
            .then(prayers => {
                prayers.forEach(prayer => {
                    const prayerElement = document.createElement("div");
                    prayerElement.classList.add("prayer-item");

                    const prayerName = document.createElement("span");
                    prayerName.textContent = prayer.prayerName;
                    
                    const checkbox = document.createElement("input");
                    checkbox.type = "checkbox";
                    checkbox.checked = prayer.completed;
                    checkbox.dataset.prayerId = prayer.id;

                    if (prayer.completed) {
                        checkbox.disabled = true;
                    }

                    checkbox.addEventListener('change', () => {
                        if (checkbox.checked) {
                            const prayerId = checkbox.dataset.prayerId;
                            checkbox.disabled = true;
                            fetch("/api/prayers/complete/" + prayerId, { method: "PUT" })
                            .then(response => {
                                if (!response.ok) throw new Error("Update failed");
                                return response.json();
                            })
                            .then(data => console.log("Success:", data.message))
                            .catch(err => {
                                console.error(err);
                                checkbox.disabled = false;
                                checkbox.checked = false;
                            });
                        }
                    });

                    prayerElement.appendChild(checkbox);
                    prayerElement.appendChild(prayerName);
                    prayerList.appendChild(prayerElement);
                });
            })
            .catch(error => {
                console.error(error);
                prayerList.textContent = "Could not load prayers.";
            });
    }

    function displaySummaryData(prayers, container) {
        if (prayers.length === 0) {
            container.innerHTML = "<p>No data found for this period.</p>";
            return;
        }

        const prayersByDay = {};
        prayers.forEach(p => {
            const date = p.prayerDate;
            if (!prayersByDay[date]) {
                prayersByDay[date] = { completed: 0, total: 0 };
            }
            prayersByDay[date].total++;
            if (p.completed) {
                prayersByDay[date].completed++;
            }
        });

        container.innerHTML = ""; 
        const sortedDays = Object.keys(prayersByDay).sort();

        sortedDays.forEach(day => {
            const data = prayersByDay[day];
            const dayElement = document.createElement("div");
            dayElement.classList.add("summary-day-item");
            
            let status = "Partial";
            if (data.completed === data.total) status = "All Complete";
            else if (data.completed === 0) status = "None";

            const [y, m, d] = day.split('-').map(Number);
            const dateObj = new Date(y, m - 1, d);
            const dayName = dateObj.toLocaleDateString('en-US', { weekday: 'long' });

            dayElement.innerHTML = `
                <strong>${day} (${dayName})</strong> 
                --- [${status}] --- 
                (${data.completed}/${data.total}) prayers
            `;
            container.appendChild(dayElement);
        });
    }

    function fetchMonthlySummary(year, month) {
        summaryResults.innerHTML = "<p>Loading summary...</p>";
        fetch(`/api/summary/monthly?year=${year}&month=${month}`)
            .then(response => response.json())
            .then(prayers => displaySummaryData(prayers, summaryResults))
            .catch(error => {
                summaryResults.innerHTML = `<p class="error-message">Error loading summary.</p>`;
            });
    }

    // --- UPDATED WEEKLY LOGIC ---
    
    function formatDateForApi(date) {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    }

    function formatDateForLabel(date) {
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    }

    function updateWeeklyView() {
        // 1. Calculate Start and End dates
        const startDate = new Date(currentWeeklyStartDate);
        const endDate = new Date(currentWeeklyStartDate);
        endDate.setDate(startDate.getDate() + 6); // Add 6 days to get Saturday (Sunday-Saturday range)

        // 2. Update Label
        currentWeekLabel.textContent = `${formatDateForLabel(startDate)} - ${formatDateForLabel(endDate)}`;

        // 3. Fetch Data
        const apiDateParam = formatDateForApi(startDate);
        weeklyResults.innerHTML = "<p>Loading...</p>";
        
        fetch(`/api/summary/weekly?start=${apiDateParam}`)
            .then(response => response.json())
            .then(prayers => displaySummaryData(prayers, weeklyResults))
            .catch(error => {
                weeklyResults.innerHTML = `<p class="error-message">Error loading data.</p>`;
            });
    }

    // Helper to find the SUNDAY of the current week
    function setToCurrentWeekSunday() {
        const d = new Date();
        const day = d.getDay(); // 0 is Sunday, 1 is Monday, etc.
        // If today is Sunday (0), diff is 0. If Monday (1), diff is -1.
        const diff = d.getDate() - day; 
        currentWeeklyStartDate = new Date(d.setDate(diff));
    }


    // --- EVENT LISTENERS ---

    viewSummaryButton.addEventListener("click", () => {
        appContainer.classList.add("hidden");
        weeklyContainer.classList.add("hidden");
        summaryContainer.classList.remove("hidden");
        
        populateYearDropdown();
        const now = new Date();
        summaryYear.value = now.getFullYear();
        summaryMonth.value = now.getMonth() + 1; 
        fetchMonthlySummary(now.getFullYear(), now.getMonth() + 1);
    });

    // WEEKLY VIEW BUTTON - Initialize to current week (Sunday Start)
    viewWeeklyButton.addEventListener("click", () => {
        appContainer.classList.add("hidden");
        summaryContainer.classList.add("hidden");
        weeklyContainer.classList.remove("hidden");

        setToCurrentWeekSunday(); // Reset to this week's Sunday
        updateWeeklyView(); // Fetch and Display
    });

    // PREV BUTTON - Go back 7 days
    prevWeekBtn.addEventListener("click", () => {
        currentWeeklyStartDate.setDate(currentWeeklyStartDate.getDate() - 7);
        updateWeeklyView();
    });

    // NEXT BUTTON - Go forward 7 days
    nextWeekBtn.addEventListener("click", () => {
        currentWeeklyStartDate.setDate(currentWeeklyStartDate.getDate() + 7);
        updateWeeklyView();
    });

    summaryForm.addEventListener("submit", (event) => {
        event.preventDefault(); 
        fetchMonthlySummary(summaryYear.value, summaryMonth.value);
    });

    backToPrayersButton.addEventListener("click", () => {
        summaryContainer.classList.add("hidden");
        appContainer.classList.remove("hidden");
        loadPrayers();
    });

    backFromWeeklyButton.addEventListener("click", () => {
        weeklyContainer.classList.add("hidden");
        appContainer.classList.remove("hidden");
        loadPrayers();
    });

    loginForm.addEventListener("submit", (event) => {
        event.preventDefault();
        const formData = new URLSearchParams();
        formData.append("username", loginUsername.value);
        formData.append("password", loginPassword.value);

        fetch("/api/login", { method: "POST", body: formData })
        .then(response => {
            if (response.ok) return response.json();
            return response.json().then(err => { throw new Error(err.message); });
        })
        .then(data => {
            authContainer.classList.add("hidden");
            appContainer.classList.remove("hidden");
            welcomeMessage.textContent = `Welcome, ${data.username}!`;
            loadPrayers(); 
        })
        .catch(error => {
            loginMessage.textContent = error.message;
        });
    });

    registerForm.addEventListener("submit", (event) => {
        event.preventDefault();
        const formData = new URLSearchParams();
        formData.append("username", registerUsername.value);
        formData.append("password", registerPassword.value);

        fetch("/api/register", { method: "POST", body: formData })
        .then(response => response.json().then(data => {
            if (!response.ok) throw new Error(data.message);
            return data;
        }))
        .then(data => {
            registerMessage.classList.remove("error-message");
            registerMessage.classList.add("success-message");
            registerMessage.textContent = data.message;
            registerForm.reset();
        })
        .catch(error => {
            registerMessage.classList.remove("success-message");
            registerMessage.classList.add("error-message");
            registerMessage.textContent = error.message;
        });
    });

    logoutButton.addEventListener("click", () => {
        fetch("/api/logout", { method: "POST" })
        .then(() => window.location.reload())
        .catch(err => console.error(err));
    });

});