document.addEventListener("DOMContentLoaded", () => {

    // --- TRANSLATION DICTIONARY ---
    const translations = {
        en: {
            app_title: "My Salaah Journal",
            login: "Login",
            register: "Register",
            username: "Username",
            password: "Password",
            welcome: "Welcome",
            view_monthly: "View Monthly Calendar",
            view_weekly: "View Weekly Summary",
            notif_settings: "🔔 Notification Settings",
            logout: "Logout",
            prayers_today: "Your Prayers for Today",
            back_today: "← Back to Today",
            monthly_calendar: "Monthly Calendar",
            prev: "← Prev",
            next: "Next →",
            weekly_summary: "Weekly Summary",
            notifications: "Notifications",
            step1: "1. Open Telegram and search for <strong>@userinfobot</strong>.",
            step2: "2. Click Start. It will give you a number (ID).",
            step3: "3. Paste that number below:",
            connect_telegram: "Connect Telegram",
            send_test: "Send Test Message",
            loading: "Loading...",
            no_data: "No data found.",
            details: "Details",
            sun: "Sun", mon: "Mon", tue: "Tue", wed: "Wed", thu: "Thu", fri: "Fri", sat: "Sat",
            prayers: { Fajr: "Fajr", Dhuhr: "Dhuhr", Asr: "Asr", Maghrib: "Maghrib", Isha: "Isha" }
        },
        ar: {
            app_title: "يوميات صلاتي",
            login: "تسجيل الدخول",
            register: "تسجيل جديد",
            username: "اسم المستخدم",
            password: "كلمة المرور",
            welcome: "أهلاً بك",
            view_monthly: "التقويم الشهري",
            view_weekly: "الملخص الأسبوعي",
            notif_settings: "🔔 إعدادات التنبيهات",
            logout: "تسجيل خروج",
            prayers_today: "صلوات اليوم",
            back_today: "عودة لليوم →",
            monthly_calendar: "التقويم الشهري",
            prev: "السابق →",
            next: "← التالي",
            weekly_summary: "الملخص الأسبوعي",
            notifications: "التنبيهات",
            step1: "1. افتح تيليجرام وابحث عن <strong>@userinfobot</strong>.",
            step2: "2. اضغط Start. سيظهر لك رقم (ID).",
            step3: "3. الصق الرقم هنا:",
            connect_telegram: "ربط تيليجرام",
            send_test: "إرسال رسالة تجريبية",
            loading: "جاري التحميل...",
            no_data: "لا توجد بيانات.",
            details: "التفاصيل",
            sun: "أحد", mon: "إثنين", tue: "ثلاثاء", wed: "أربعاء", thu: "خميس", fri: "جمعة", sat: "سبت",
            prayers: { Fajr: "الفجر", Dhuhr: "الظهر", Asr: "العصر", Maghrib: "المغرب", Isha: "العشاء" }
        }
    };

    let currentLang = localStorage.getItem('appLang') || 'en'; // Default to English or saved preference

    // --- ELEMENTS ---
    const langToggle = document.getElementById("lang-toggle");
    // ... (Keep all your existing element selectors) ...
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
    const prevMonthBtn = document.getElementById("prev-month-btn");
    const nextMonthBtn = document.getElementById("next-month-btn");
    const currentMonthLabel = document.getElementById("current-month-label");
    const calendarGrid = document.getElementById("calendar-grid");
    const dayDetails = document.getElementById("day-details");
    const selectedDateTitle = document.getElementById("selected-date-title");
    const selectedDayPrayers = document.getElementById("selected-day-prayers");
    const viewWeeklyButton = document.getElementById("view-weekly-button");
    const weeklyContainer = document.getElementById("weekly-container");
    const backFromWeeklyButton = document.getElementById("back-from-weekly-button");
    const weeklyList = document.getElementById("weekly-list");
    const prevWeekBtn = document.getElementById("prev-week-btn");
    const nextWeekBtn = document.getElementById("next-week-btn");
    const currentWeekLabel = document.getElementById("current-week-label");
    const weeklyDayDetails = document.getElementById("weekly-day-details");
    const weeklySelectedDateTitle = document.getElementById("weekly-selected-date-title");
    const weeklySelectedDayPrayers = document.getElementById("weekly-selected-day-prayers");
    const viewSettingsButton = document.getElementById("view-settings-button");
    const settingsContainer = document.getElementById("settings-container");
    const backFromSettingsButton = document.getElementById("back-from-settings-button");
    const telegramForm = document.getElementById("telegram-form");
    const telegramChatId = document.getElementById("telegram-chat-id");
    const telegramMessage = document.getElementById("telegram-message");
    const testTelegramBtn = document.getElementById("test-telegram-btn");

    let currentWeeklyStartDate = new Date();
    let currentMonthlyDate = new Date(); 

    // --- LANGUAGE FUNCTION ---
    function setLanguage(lang) {
        currentLang = lang;
        localStorage.setItem('appLang', lang); // Save preference
        
        // Update Body Class for RTL
        if (lang === 'ar') {
            document.body.classList.add('rtl');
            langToggle.textContent = "English";
        } else {
            document.body.classList.remove('rtl');
            langToggle.textContent = "العربية";
        }

        // Update all elements with data-i18n attribute
        document.querySelectorAll('[data-i18n]').forEach(el => {
            const key = el.getAttribute('data-i18n');
            if (translations[lang][key]) {
                el.innerHTML = translations[lang][key];
            }
        });

        // Update placeholders
        document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
            const key = el.getAttribute('data-i18n-placeholder');
            if (translations[lang][key]) {
                el.placeholder = translations[lang][key];
            }
        });

        // Refresh views if they are visible
        if (!appContainer.classList.contains('hidden')) loadPrayers();
        if (!summaryContainer.classList.contains('hidden')) updateMonthlyView();
        if (!weeklyContainer.classList.contains('hidden')) updateWeeklyView();
    }

    // Initialize Language
    setLanguage(currentLang);

    langToggle.addEventListener("click", () => {
        setLanguage(currentLang === 'en' ? 'ar' : 'en');
    });


    // --- SHARED FUNCTIONS ---

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
                    
                    // Translate prayer name
                    const t = translations[currentLang];
                    const displayName = t.prayers[prayer.prayerName] || prayer.prayerName;

                    const prayerName = document.createElement("span");
                    prayerName.textContent = displayName;
                    
                    const checkbox = document.createElement("input");
                    checkbox.type = "checkbox";
                    checkbox.checked = prayer.completed;
                    checkbox.dataset.prayerId = prayer.id;

                    // DISABLED LOGIC REMOVED FOR UNDO SUPPORT

                    checkbox.addEventListener('change', () => {
                        const prayerId = checkbox.dataset.prayerId;
                        const isChecked = checkbox.checked; // Capture true or false
                        
                        // Send PUT request with the new status
                        fetch(`/api/prayers/${prayerId}?completed=${isChecked}`, { method: "PUT" })
                        .then(response => {
                            if (!response.ok) throw new Error("Update failed");
                            return response.json();
                        })
                        .then(data => console.log("Success:", data.message))
                        .catch(err => {
                            console.error(err);
                            checkbox.checked = !isChecked; // Revert checkbox if fail
                        });
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

    function formatDateForApi(date) {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    }

    function formatDateForLabel(date) {
        // Use 'ar-SA' if arabic, else 'en-US'
        const locale = currentLang === 'ar' ? 'ar-SA' : 'en-US';
        return date.toLocaleDateString(locale, { month: 'short', day: 'numeric' });
    }

    function updateWeeklyView() {
        const startDate = new Date(currentWeeklyStartDate);
        const endDate = new Date(currentWeeklyStartDate);
        endDate.setDate(startDate.getDate() + 6); 

        currentWeekLabel.textContent = `${formatDateForLabel(startDate)} - ${formatDateForLabel(endDate)}`;
        const apiDateParam = formatDateForApi(startDate);
        
        const t = translations[currentLang];
        weeklyList.innerHTML = `<p>${t.loading}</p>`;

        fetch(`/api/summary/weekly?start=${apiDateParam}`)
            .then(response => response.json())
            .then(prayers => renderWeeklyList(prayers))
            .catch(error => {
                weeklyList.innerHTML = `<p class="error-message">Error loading data.</p>`;
            });
    }

    function renderWeeklyList(prayers) {
        weeklyList.innerHTML = "";
        const t = translations[currentLang];
        const prayersByDay = {};
        prayers.forEach(p => {
            if (!prayersByDay[p.prayerDate]) prayersByDay[p.prayerDate] = [];
            prayersByDay[p.prayerDate].push(p);
        });

        const prayerNames = ["Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"];

        for (let i = 0; i < 7; i++) {
            const cellDate = new Date(currentWeeklyStartDate);
            cellDate.setDate(cellDate.getDate() + i);
            
            const cellDateString = formatDateForApi(cellDate);
            const daysPrayers = prayersByDay[cellDateString] || [];

            const row = document.createElement("div");
            row.classList.add("weekly-day-row");

            const total = daysPrayers.length;
            const completed = daysPrayers.filter(p => p.completed).length;
            
            let statusClass = "status-empty";
            if (total > 0) {
                if (completed === total) statusClass = "status-full";
                else if (completed === 0) statusClass = "status-none";
                else statusClass = "status-partial";
            }
            row.classList.add(statusClass);

            const dateCol = document.createElement("div");
            dateCol.classList.add("weekly-date-col");
            
            const locale = currentLang === 'ar' ? 'ar-SA' : 'en-US';
            const dayName = cellDate.toLocaleDateString(locale, { weekday: 'long' });
            const dateShort = cellDate.toLocaleDateString(locale, { month: 'short', day: 'numeric' });
            
            dateCol.innerHTML = `${dayName} <span class="sub-date">${dateShort}</span>`;

            const prayersCol = document.createElement("div");
            prayersCol.classList.add("weekly-prayers-col");

            if (daysPrayers.length === 0) {
                prayersCol.innerHTML = `<span style='color:#777; font-style:italic;'>${t.no_data}</span>`;
            } else {
                prayerNames.forEach(name => {
                    const pLog = daysPrayers.find(p => p.prayerName === name);
                    const isDone = pLog && pLog.completed;
                    
                    const displayName = t.prayers[name] || name;

                    const miniStatus = document.createElement("div");
                    miniStatus.classList.add("mini-prayer-status");
                    if (isDone) miniStatus.classList.add("done");
                    else miniStatus.classList.add("missed");

                    miniStatus.innerHTML = `${displayName} ${isDone ? '✅' : '❌'}`;
                    prayersCol.appendChild(miniStatus);
                });
            }

            row.appendChild(dateCol);
            row.appendChild(prayersCol);
            weeklyList.appendChild(row);
        }
    }

    function setToCurrentWeekSunday() {
        const d = new Date();
        const day = d.getDay(); 
        const diff = d.getDate() - day; 
        currentWeeklyStartDate = new Date(d.setDate(diff));
    }

    function updateMonthlyView() {
        const locale = currentLang === 'ar' ? 'ar-SA' : 'en-US';
        currentMonthLabel.textContent = currentMonthlyDate.toLocaleDateString(locale, { month: 'long', year: 'numeric' });
        
        const t = translations[currentLang];
        calendarGrid.innerHTML = `<p>${t.loading}</p>`;
        dayDetails.classList.add("hidden");

        const year = currentMonthlyDate.getFullYear();
        const month = currentMonthlyDate.getMonth() + 1;

        fetch(`/api/summary/monthly?year=${year}&month=${month}`)
            .then(response => response.json())
            .then(prayers => renderCalendar(year, month, prayers))
            .catch(error => {
                calendarGrid.innerHTML = `<p class="error-message">Error loading calendar.</p>`;
            });
    }

    function renderCalendar(year, month, prayers) {
        calendarGrid.innerHTML = "";
        const prayersByDay = {};
        prayers.forEach(p => {
            if (!prayersByDay[p.prayerDate]) prayersByDay[p.prayerDate] = [];
            prayersByDay[p.prayerDate].push(p);
        });

        const firstDayOfMonth = new Date(year, month - 1, 1).getDay(); 
        const daysInMonth = new Date(year, month, 0).getDate();

        for (let i = 0; i < firstDayOfMonth; i++) {
            const emptyCell = document.createElement("div");
            emptyCell.classList.add("calendar-day", "empty");
            calendarGrid.appendChild(emptyCell);
        }

        for (let day = 1; day <= daysInMonth; day++) {
            const cell = document.createElement("div");
            cell.classList.add("calendar-day");

            const dateKey = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            const daysPrayers = prayersByDay[dateKey] || [];

            const total = daysPrayers.length;
            const completed = daysPrayers.filter(p => p.completed).length;
            
            let statusClass = "status-empty";
            if (total > 0) {
                if (completed === total) statusClass = "status-full";
                else if (completed === 0) statusClass = "status-none";
                else statusClass = "status-partial";
            }

            cell.classList.add(statusClass);
            // Translate digits for Arabic? Optional, usually standard numbers are fine.
            cell.innerHTML = `
                <div class="day-number">${day}</div>
                <div class="day-status">${completed}/${total}</div>
            `;

            cell.addEventListener("click", () => {
                showDayDetails(dateKey, daysPrayers);
            });

            calendarGrid.appendChild(cell);
        }
    }

    function showDayDetails(dateKey, prayers) {
        dayDetails.classList.remove("hidden");
        const t = translations[currentLang];
        selectedDateTitle.textContent = `${t.details}: ${dateKey}`;
        selectedDayPrayers.innerHTML = "";

        if (prayers.length === 0) {
            selectedDayPrayers.innerHTML = `<p>${t.no_data}</p>`;
            return;
        }

        prayers.forEach(p => {
            const pDiv = document.createElement("div");
            const icon = p.completed ? "✅" : "❌";
            const displayName = t.prayers[p.prayerName] || p.prayerName;
            
            pDiv.textContent = `${displayName}: ${icon}`;
            pDiv.style.color = p.completed ? "green" : "red";
            selectedDayPrayers.appendChild(pDiv);
        });
    }


    // --- EVENT LISTENERS ---

    viewSummaryButton.addEventListener("click", () => {
        appContainer.classList.add("hidden");
        weeklyContainer.classList.add("hidden");
        settingsContainer.classList.add("hidden"); 
        summaryContainer.classList.remove("hidden");
        currentMonthlyDate = new Date();
        currentMonthlyDate.setDate(1); 
        updateMonthlyView();
    });

    viewWeeklyButton.addEventListener("click", () => {
        appContainer.classList.add("hidden");
        summaryContainer.classList.add("hidden");
        settingsContainer.classList.add("hidden"); 
        weeklyContainer.classList.remove("hidden");
        setToCurrentWeekSunday();
        updateWeeklyView();
    });

    viewSettingsButton.addEventListener("click", () => {
        appContainer.classList.add("hidden");
        summaryContainer.classList.add("hidden");
        weeklyContainer.classList.add("hidden");
        settingsContainer.classList.remove("hidden");
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
    backFromSettingsButton.addEventListener("click", () => {
        settingsContainer.classList.add("hidden");
        appContainer.classList.remove("hidden");
        loadPrayers();
    });

    prevWeekBtn.addEventListener("click", () => {
        currentWeeklyStartDate.setDate(currentWeeklyStartDate.getDate() - 7);
        updateWeeklyView();
    });
    nextWeekBtn.addEventListener("click", () => {
        currentWeeklyStartDate.setDate(currentWeeklyStartDate.getDate() + 7);
        updateWeeklyView();
    });
    prevMonthBtn.addEventListener("click", () => {
        currentMonthlyDate.setMonth(currentMonthlyDate.getMonth() - 1);
        updateMonthlyView();
    });
    nextMonthBtn.addEventListener("click", () => {
        currentMonthlyDate.setMonth(currentMonthlyDate.getMonth() + 1);
        updateMonthlyView();
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
            // Translate the welcome message
            const t = translations[currentLang];
            welcomeMessage.textContent = `${t.welcome}, ${data.username}!`;
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

    telegramForm.addEventListener("submit", (event) => {
        event.preventDefault();
        const chatId = telegramChatId.value;
        const formData = new URLSearchParams();
        formData.append("chatId", chatId);

        fetch("/api/telegram/link", { method: "POST", body: formData })
        .then(response => response.json())
        .then(data => {
            if (data.status === "success") {
                telegramMessage.style.color = "green";
                telegramMessage.textContent = data.message;
            } else {
                telegramMessage.style.color = "red";
                telegramMessage.textContent = data.message;
            }
        });
    });

    testTelegramBtn.addEventListener("click", () => {
        telegramMessage.textContent = "Sending...";
        fetch("/api/telegram/test", { method: "POST" })
        .then(response => response.json())
        .then(data => {
            telegramMessage.style.color = data.status === "success" ? "green" : "red";
            telegramMessage.textContent = data.message;
        });
    });

});