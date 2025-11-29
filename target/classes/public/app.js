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

    const viewSummaryButton = document.getElementById("view-summary-button");
    const summaryContainer = document.getElementById("summary-container");
    const backToPrayersButton = document.getElementById("back-to-prayers-button");
    const prevMonthBtn = document.getElementById("prev-month-btn");
    const nextMonthBtn = document.getElementById("next-month-btn");
    const currentMonthLabel = document.getElementById("current-month-label");
    const calendarGrid = document.getElementById("calendar-grid");
    const calendarHeader = document.getElementById("calendar-header");
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
    
    // Settings
    const viewSettingsButton = document.getElementById("view-settings-button");
    const settingsContainer = document.getElementById("settings-container");
    const backFromSettingsButton = document.getElementById("back-from-settings-button");
    const telegramForm = document.getElementById("telegram-form");
    const telegramChatId = document.getElementById("telegram-chat-id");
    const telegramMessage = document.getElementById("telegram-message");
    const testTelegramBtn = document.getElementById("test-telegram-btn");

    const langToggle = document.getElementById("lang-toggle");

    // STATE VARIABLES
    let currentWeeklyStartDate = new Date();
    let currentMonthlyDate = new Date(); 
    let currentLang = localStorage.getItem("app_lang") || "en";

    // --- TRANSLATIONS ---
    const translations = {
        en: {
            app_title: "Salaah Tracker",
            login_title: "Login",
            login_btn: "Login",
            register_title: "Register",
            register_btn: "Register",
            logout_btn: "Logout",
            view_monthly_btn: "View Monthly Calendar",
            view_weekly_btn: "View Weekly Summary",
            notif_settings_btn: "🔔 Notification Settings",
            prayers_today_title: "Your Prayers for Today",
            back_btn: "← Back to Today",
            monthly_title: "Monthly Calendar",
            weekly_title: "Weekly Summary",
            prev_btn: "← Prev",
            next_btn: "Next →",
            notif_settings_title: "Notifications",
            step_1: "1. Open Telegram and search for <strong>@userinfobot</strong>.",
            step_2: "2. Click Start. It will give you a number (ID).",
            step_3: "3. Paste that number below:",
            connect_btn: "Connect Telegram",
            send_test_btn: "Send Test Message",
            welcome: "Welcome",
            loading: "Loading...",
            no_data: "No data found.",
            week_days: ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
            prayers: { "Fajr": "Fajr", "Dhuhr": "Dhuhr", "Asr": "Asr", "Maghrib": "Maghrib", "Isha": "Isha" }
        },
        ar: {
            app_title: "متابع الصلوات",
            login_title: "تسجيل الدخول",
            login_btn: "دخول",
            register_title: "تسجيل جديد",
            register_btn: "تسجيل",
            logout_btn: "تسجيل خروج",
            view_monthly_btn: "التقويم الشهري",
            view_weekly_btn: "الملخص الأسبوعي",
            notif_settings_btn: "🔔 إعدادات التنبيهات",
            prayers_today_title: "صلواتك اليوم",
            back_btn: "العودة لليوم →",
            monthly_title: "التقويم الشهري",
            weekly_title: "الملخص الأسبوعي",
            prev_btn: "السابق →",
            next_btn: "← التالي",
            notif_settings_title: "التنبيهات",
            step_1: "1. افتح تيليجرام وابحث عن <strong>@userinfobot</strong>.",
            step_2: "2. اضغط Start. سيعطيك رقم (هوية).",
            step_3: "3. الصق الرقم بالأسفل:",
            connect_btn: "ربط تيليجرام",
            send_test_btn: "إرسال رسالة تجريبية",
            welcome: "مرحباً",
            loading: "جاري التحميل...",
            no_data: "لا توجد بيانات.",
            week_days: ["أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت"],
            prayers: { "Fajr": "الفجر", "Dhuhr": "الظهر", "Asr": "العصر", "Maghrib": "المغرب", "Isha": "العشاء" }
        }
    };

    // --- LANGUAGE FUNCTIONS ---

    function setLanguage(lang) {
        currentLang = lang;
        localStorage.setItem("app_lang", lang);
        
        // 1. Update Direction
        document.documentElement.setAttribute("dir", lang === 'ar' ? 'rtl' : 'ltr');
        
        // 2. Update Button Text
        langToggle.textContent = lang === 'ar' ? 'English' : 'عربي';

        // 3. Update all static text with data-i18n attribute
        document.querySelectorAll("[data-i18n]").forEach(el => {
            const key = el.getAttribute("data-i18n");
            if (translations[lang][key]) {
                el.innerHTML = translations[lang][key];
            }
        });

        // 4. Update Placeholders
        document.getElementById("login-username").placeholder = lang === 'ar' ? "اسم المستخدم" : "Username";
        document.getElementById("login-password").placeholder = lang === 'ar' ? "كلمة المرور" : "Password";
        document.getElementById("register-username").placeholder = lang === 'ar' ? "اسم المستخدم" : "Username";
        document.getElementById("register-password").placeholder = lang === 'ar' ? "كلمة المرور" : "Password";
        document.getElementById("telegram-chat-id").placeholder = lang === 'ar' ? "مثال: 123456789" : "e.g. 123456789";

        // 5. Update Calendar Header (Sun, Mon...)
        calendarHeader.innerHTML = "";
        translations[lang].week_days.forEach(day => {
            const div = document.createElement("div");
            div.textContent = day;
            calendarHeader.appendChild(div);
        });

        // 6. Refresh Current Views if active
        if (!appContainer.classList.contains("hidden")) loadPrayers();
        if (!summaryContainer.classList.contains("hidden")) updateMonthlyView();
        if (!weeklyContainer.classList.contains("hidden")) updateWeeklyView();
    }

    langToggle.addEventListener("click", () => {
        setLanguage(currentLang === 'en' ? 'ar' : 'en');
    });

    // Initialize Language
    setLanguage(currentLang);


    // --- SHARED FUNCTIONS ---

    function loadPrayers() {
        prayerList.innerHTML = "";
        fetch("/api/prayers/today")
            .then(response => response.json())
            .then(prayers => {
                prayers.forEach(prayer => {
                    const prayerElement = document.createElement("div");
                    prayerElement.classList.add("prayer-item");
                    
                    // TRANSLATE PRAYER NAME
                    const tName = translations[currentLang].prayers[prayer.prayerName] || prayer.prayerName;
                    
                    const prayerName = document.createElement("span");
                    prayerName.textContent = tName;
                    const checkbox = document.createElement("input");
                    checkbox.type = "checkbox";
                    checkbox.checked = prayer.completed;
                    checkbox.dataset.prayerId = prayer.id;

                    if (prayer.completed) checkbox.disabled = true;

                    checkbox.addEventListener('change', () => {
                        if (checkbox.checked) {
                            const prayerId = checkbox.dataset.prayerId;
                            checkbox.disabled = true;
                            fetch("/api/prayers/complete/" + prayerId, { method: "PUT" })
                            .then(response => response.json())
                            .then(data => console.log("Success"))
                            .catch(err => {
                                checkbox.disabled = false;
                                checkbox.checked = false;
                            });
                        }
                    });
                    prayerElement.appendChild(checkbox);
                    prayerElement.appendChild(prayerName);
                    prayerList.appendChild(prayerElement);
                });
            });
    }

    function formatDateForApi(date) {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    }

    function formatDateForLabel(date) {
        const locale = currentLang === 'ar' ? 'ar-SA' : 'en-US';
        return date.toLocaleDateString(locale, { month: 'short', day: 'numeric' });
    }

    // --- WEEKLY LIST LOGIC ---

    function updateWeeklyView() {
        const startDate = new Date(currentWeeklyStartDate);
        const endDate = new Date(currentWeeklyStartDate);
        endDate.setDate(startDate.getDate() + 6); 

        currentWeekLabel.textContent = `${formatDateForLabel(startDate)} - ${formatDateForLabel(endDate)}`;
        const apiDateParam = formatDateForApi(startDate);
        
        weeklyList.innerHTML = `<p>${translations[currentLang].loading}</p>`;

        fetch(`/api/summary/weekly?start=${apiDateParam}`)
            .then(response => response.json())
            .then(prayers => renderWeeklyList(prayers))
            .catch(error => {
                weeklyList.innerHTML = `<p class="error-message">Error</p>`;
            });
    }

    function renderWeeklyList(prayers) {
        weeklyList.innerHTML = "";
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
                prayersCol.innerHTML = `<span style='color:#777; font-style:italic;'>${translations[currentLang].no_data}</span>`;
            } else {
                prayerNames.forEach(name => {
                    const pLog = daysPrayers.find(p => p.prayerName === name);
                    const isDone = pLog && pLog.completed;
                    
                    const tPrayerName = translations[currentLang].prayers[name];

                    const miniStatus = document.createElement("div");
                    miniStatus.classList.add("mini-prayer-status");
                    if (isDone) miniStatus.classList.add("done");
                    else miniStatus.classList.add("missed");

                    miniStatus.innerHTML = `${tPrayerName} ${isDone ? '✅' : '❌'}`;
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

    // --- MONTHLY CALENDAR LOGIC ---

    function updateMonthlyView() {
        const locale = currentLang === 'ar' ? 'ar-SA' : 'en-US';
        currentMonthLabel.textContent = currentMonthlyDate.toLocaleDateString(locale, { month: 'long', year: 'numeric' });
        calendarGrid.innerHTML = `<p>${translations[currentLang].loading}</p>`;
        dayDetails.classList.add("hidden");

        const year = currentMonthlyDate.getFullYear();
        const month = currentMonthlyDate.getMonth() + 1;

        fetch(`/api/summary/monthly?year=${year}&month=${month}`)
            .then(response => response.json())
            .then(prayers => renderCalendar(year, month, prayers));
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
        selectedDateTitle.textContent = dateKey;
        selectedDayPrayers.innerHTML = "";

        if (prayers.length === 0) {
            selectedDayPrayers.innerHTML = `<p>${translations[currentLang].no_data}</p>`;
            return;
        }

        prayers.forEach(p => {
            const pDiv = document.createElement("div");
            const icon = p.completed ? "✅" : "❌";
            const tName = translations[currentLang].prayers[p.prayerName] || p.prayerName;
            pDiv.textContent = `${tName}: ${icon}`;
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
            welcomeMessage.textContent = `${translations[currentLang].welcome}, ${data.username}!`;
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
            telegramMessage.style.color = data.status === "success" ? "green" : "red";
            telegramMessage.textContent = data.message;
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