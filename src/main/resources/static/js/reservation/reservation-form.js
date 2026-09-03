(() => {
    const page = document.getElementById("reservationPage");
    if (!page) return;

    const memberNoValue = page.dataset.memberNo;
    const isLoggedIn = page.dataset.loggedIn === "true";
    const memberNo = memberNoValue ? Number(memberNoValue) : null;

    const dateInput = document.getElementById("reservationDate");
    const timeSection = document.getElementById("timeSection");
    const timeSlots = document.getElementById("timeSlots");
    const hairStyleList = document.getElementById("hairStyleList");
    const submitButton = document.getElementById("submitReservation");
    const summary = document.getElementById("reservationSummary");
    const memoInput = document.getElementById("requestMemo");
    const memoCount = document.getElementById("memoCount");
    const guestNameInput = document.getElementById("guestName");
    const guestPhoneInput = document.getElementById("guestPhone");
    const imageInput = document.getElementById("reservationImages");
    const imagePreview = document.getElementById("imagePreview");
    const messageBox = document.getElementById("messageBox");

    let selectedMenuNo = null;
    let selectedMenuName = "";
    let selectedHairStyleNo = null;
    let selectedHairStyleName = "";
    let selectedDate = "";
    let selectedStartTime = "";
    let selectedFiles = [];

    const today = new Date();
    dateInput.min = [
        today.getFullYear(),
        String(today.getMonth() + 1).padStart(2, "0"),
        String(today.getDate()).padStart(2, "0")
    ].join("-");

    document.querySelectorAll(".menu-radio").forEach(radio => {
        radio.addEventListener("change", async event => {
            selectedMenuNo = Number(event.target.value);
            selectedMenuName =
                event.target.closest(".menu-card")
                    .querySelector("strong").textContent.trim();

            selectedHairStyleNo = null;
            selectedHairStyleName = "";
            clearSelectedTime();

            await loadHairStyles();

            if (selectedDate) {
                await loadAvailableTimes();
            }

            updateSummary();
        });
    });

    document.getElementById("clearServiceMenu")
        .addEventListener("click", () => {
            document.querySelectorAll(".menu-radio")
                .forEach(radio => radio.checked = false);

            selectedMenuNo = null;
            selectedMenuName = "";
            selectedHairStyleNo = null;
            selectedHairStyleName = "";
            clearSelectedTime();
            timeSection.classList.add("hidden");
            hairStyleList.innerHTML =
                `<div class="empty-box">먼저 시술 메뉴를 선택해주세요.</div>`;
            updateSummary();
        });

    document.getElementById("clearHairStyle")
        .addEventListener("click", () => {
            document.querySelectorAll(".hair-style-radio")
                .forEach(radio => radio.checked = false);

            selectedHairStyleNo = null;
            selectedHairStyleName = "";
            updateSummary();
        });

    dateInput.addEventListener("change", async () => {
        selectedDate = dateInput.value;
        clearSelectedTime();

        if (selectedMenuNo && selectedDate) {
            await loadAvailableTimes();
        } else {
            timeSection.classList.add("hidden");
        }

        updateSummary();
    });

    memoInput.addEventListener("input", () => {
        memoCount.textContent = String(memoInput.value.length);
    });

    if (guestPhoneInput) {
        guestPhoneInput.addEventListener("input", () => {
            guestPhoneInput.value =
                guestPhoneInput.value.replace(/[^\d-]/g, "");
        });
    }

    imageInput.addEventListener("change", () => {
        const files = Array.from(imageInput.files || []);

        if (files.length > 3) {
            return resetFiles("참고 이미지는 최대 3장까지 선택할 수 있습니다.");
        }

        for (const file of files) {
            if (file.size > 10 * 1024 * 1024) {
                return resetFiles(`${file.name}: 10MB를 초과합니다.`);
            }

            if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
                return resetFiles(`${file.name}: 지원하지 않는 이미지 형식입니다.`);
            }
        }

        selectedFiles = files;
        renderPreview();
    });

    submitButton.addEventListener("click", submitReservation);

    async function loadHairStyles() {
        hairStyleList.innerHTML =
            `<div class="loading-box">예시 스타일을 불러오는 중입니다.</div>`;

        const response = await fetch(
            `/api/reservations/hair-styles?serviceMenuNo=${selectedMenuNo}`
        );
        const body = await readJson(response);

        if (!response.ok) {
            hairStyleList.innerHTML =
                `<div class="empty-box">${escapeHtml(body.message || "스타일 조회 실패")}</div>`;
            return;
        }

        if (!body.length) {
            hairStyleList.innerHTML =
                `<div class="empty-box">이 시술에 등록된 예시 스타일이 없습니다.</div>`;
            return;
        }

        hairStyleList.innerHTML = "";

        body.forEach(style => {
            const label = document.createElement("label");
            label.className = "hair-style-card";

            const image =
                style.imageUrl
                    ? `<img class="hair-style-image" src="${escapeAttribute(style.imageUrl)}" alt="">`
                    : `<div class="hair-style-image"></div>`;

            label.innerHTML = `
                <input type="radio"
                       name="hairStyle"
                       class="hair-style-radio"
                       value="${style.hairStyleNo}">
                <span class="hair-style-content">
                    ${image}
                    <span class="hair-style-text">
                        <strong>${escapeHtml(style.title)}</strong>
                        <span>${escapeHtml(style.description || "")}</span>
                    </span>
                </span>
            `;

            label.querySelector(".hair-style-radio")
                .addEventListener("change", () => {
                    selectedHairStyleNo = Number(style.hairStyleNo);
                    selectedHairStyleName = style.title;
                    updateSummary();
                });

            hairStyleList.appendChild(label);
        });
    }

    async function loadAvailableTimes() {
        timeSlots.innerHTML =
            `<div class="loading-box">예약 가능 시간을 조회 중입니다.</div>`;
        timeSection.classList.remove("hidden");

        const params = new URLSearchParams({
            date: selectedDate,
            serviceMenuNo: String(selectedMenuNo)
        });

        const response = await fetch(
            `/api/reservations/available-times?${params}`
        );
        const body = await readJson(response);

        if (!response.ok) {
            timeSlots.innerHTML =
                `<div class="empty-box">${escapeHtml(body.message || "조회 실패")}</div>`;
            return;
        }

        timeSlots.innerHTML = "";

        if (!body.length) {
            timeSlots.innerHTML =
                `<div class="empty-box">예약 가능한 시간이 없습니다.</div>`;
            return;
        }

        body.forEach(time => {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "time-button";
            button.textContent = String(time.startTime).slice(0, 5);

            button.addEventListener("click", () => {
                document.querySelectorAll(".time-button")
                    .forEach(item => item.classList.remove("selected"));

                button.classList.add("selected");
                selectedStartTime = String(time.startTime).slice(0, 5);
                updateSummary();
            });

            timeSlots.appendChild(button);
        });
    }

    async function submitReservation() {
        if (!selectedMenuNo || !selectedDate || !selectedStartTime) {
            return showMessage("시술, 날짜, 시간을 선택해주세요.", true);
        }

        if (!isLoggedIn) {
            const name = guestNameInput.value.trim();
            const phone = guestPhoneInput.value.trim();

            if (!/^[\p{L}][\p{L}\p{M} .'-]{0,48}[\p{L}\p{M}]$/u.test(name)) {
                return showMessage("예약자 이름 형식을 확인해주세요.", true);
            }

            if (!/^010-?\d{4}-?\d{4}$/.test(phone)) {
                return showMessage("휴대전화 번호 형식을 확인해주세요.", true);
            }
        }

        submitButton.disabled = true;
        submitButton.textContent = "예약 처리 중...";

        try {
            const requestBody = {
                                guestName: isLoggedIn ? null : guestNameInput.value.trim(),
                guestPhone: isLoggedIn ? null : guestPhoneInput.value.trim(),
                serviceMenuNo: selectedMenuNo,
                hairStyleNo: selectedHairStyleNo,
                startAt: `${selectedDate}T${selectedStartTime}:00`,
                requestMemo: memoInput.value.trim() || null,
                reservationSource: "ONLINE"
            };

            const response = await fetch(isLoggedIn ? "/api/reservations/me" : "/api/reservations", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...csrfHeaders()
                },
                body: JSON.stringify(requestBody)
            });

            const body = await readJson(response);

            if (!response.ok) {
                throw new Error(body.message || "예약 신청에 실패했습니다.");
            }

            for (const file of selectedFiles) {
                await uploadImage(body.reservationNo, file);
            }

            if (isLoggedIn) {
                showMessage(`예약이 완료되었습니다. 예약번호 ${body.reservationNo}`, false);
                setTimeout(() => location.href = "/my-reservations", 1000);
            } else {
                alert(
                    `예약이 완료되었습니다.\n예약번호: ${body.reservationNo}\n\n` +
                    "비회원 예약 조회 시 예약번호와 휴대전화 번호가 필요합니다."
                );
                location.href = "/guest-reservation";
            }
        } catch (error) {
            showMessage(error.message, true);
            submitButton.disabled = false;
            submitButton.textContent = "예약 신청";
        }
    }

    async function uploadImage(reservationNo, file) {
        const data = new FormData();
        data.append("file", file);

        const response = await fetch(
            `/api/reservations/${reservationNo}/images`,
            {
                method: "POST",
                headers: csrfHeaders(),
                body: data
            }
        );

        if (!response.ok) {
            const body = await readJson(response);
            throw new Error(
                body.message || "예약은 생성됐지만 이미지 업로드에 실패했습니다."
            );
        }
    }

    function updateSummary() {
        if (!selectedMenuNo || !selectedDate || !selectedStartTime) {
            summary.textContent = "시술, 날짜, 시간을 선택해주세요.";
            submitButton.disabled = true;
            return;
        }

        summary.textContent =
            `${selectedMenuName}` +
            (selectedHairStyleName ? ` · ${selectedHairStyleName}` : "") +
            ` · ${selectedDate} ${selectedStartTime}`;

        submitButton.disabled = false;
    }

    function clearSelectedTime() {
        selectedStartTime = "";
        timeSlots.innerHTML = "";
        submitButton.disabled = true;
    }

    function resetFiles(message) {
        showMessage(message, true);
        imageInput.value = "";
        selectedFiles = [];
        renderPreview();
    }

    function renderPreview() {
        imagePreview.innerHTML = "";

        selectedFiles.forEach(file => {
            const item = document.createElement("div");
            item.className = "preview-item";

            const img = document.createElement("img");
            const reader = new FileReader();
            reader.onload = event => img.src = event.target.result;
            reader.readAsDataURL(file);

            item.appendChild(img);
            imagePreview.appendChild(item);
        });
    }

    function csrfHeaders() {
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector('meta[name="_csrf_header"]')?.content;
        return token && header ? { [header]: token } : {};
    }

    async function readJson(response) {
        const text = await response.text();
        if (!text) return {};
        try { return JSON.parse(text); }
        catch { return { message: text }; }
    }

    function showMessage(message, error) {
        messageBox.textContent = message;
        messageBox.classList.toggle("error", Boolean(error));
        messageBox.classList.remove("hidden");
        setTimeout(() => messageBox.classList.add("hidden"), 3500);
    }

    function escapeHtml(value) {
        const div = document.createElement("div");
        div.textContent = value || "";
        return div.innerHTML;
    }

    function escapeAttribute(value) {
        return escapeHtml(value).replace(/"/g, "&quot;");
    }
})();
