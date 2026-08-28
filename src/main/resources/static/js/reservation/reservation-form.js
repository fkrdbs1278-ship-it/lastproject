(() => {
    const page = document.getElementById("reservationPage");
    if (!page) return;

    const memberNoValue = page.dataset.memberNo;
    const isLoggedIn = page.dataset.loggedIn === "true";
    const memberNo = memberNoValue ? Number(memberNoValue) : null;

    const dateInput = document.getElementById("reservationDate");
    const timeSection = document.getElementById("timeSection");
    const timeSlots = document.getElementById("timeSlots");
    const submitButton = document.getElementById("submitReservation");
    const summary = document.getElementById("reservationSummary");
    const memoInput = document.getElementById("requestMemo");
    const guestNameInput = document.getElementById("guestName");
    const guestPhoneInput = document.getElementById("guestPhone");
    const imageInput = document.getElementById("reservationImages");
    const imagePreview = document.getElementById("imagePreview");
    const messageBox = document.getElementById("messageBox");

    let selectedMenuNo = null;
    let selectedMenuName = "";
    let selectedDate = "";
    let selectedStartTime = "";
    let selectedFiles = [];

    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");
    const dd = String(today.getDate()).padStart(2, "0");
    dateInput.min = `${yyyy}-${mm}-${dd}`;

    document.querySelectorAll(".menu-radio").forEach(radio => {
        radio.addEventListener("change", async event => {
            selectedMenuNo = Number(event.target.value);
            selectedMenuName =
                event.target.closest(".menu-card")
                    .querySelector("strong").textContent.trim();

            clearSelectedTime();
            updateSummary();

            if (selectedDate) {
                await loadAvailableTimes();
            }
        });
    });

    dateInput.addEventListener("change", async () => {
        selectedDate = dateInput.value;
        clearSelectedTime();
        updateSummary();

        if (selectedMenuNo && selectedDate) {
            await loadAvailableTimes();
        } else {
            timeSection.classList.add("hidden");
        }
    });

    imageInput.addEventListener("change", () => {
        const files = Array.from(imageInput.files || []);

        if (files.length > 3) {
            showMessage("참고 이미지는 최대 3장까지 선택할 수 있습니다.", true);
            imageInput.value = "";
            selectedFiles = [];
            renderPreview();
            return;
        }

        for (const file of files) {
            if (file.size > 10 * 1024 * 1024) {
                showMessage(`${file.name}: 10MB를 초과합니다.`, true);
                imageInput.value = "";
                selectedFiles = [];
                renderPreview();
                return;
            }

            if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
                showMessage(`${file.name}: 지원하지 않는 이미지 형식입니다.`, true);
                imageInput.value = "";
                selectedFiles = [];
                renderPreview();
                return;
            }
        }

        selectedFiles = files;
        renderPreview();
    });

    submitButton.addEventListener("click", async () => {
        if (!isReady()) {
            showMessage("예약 정보를 모두 입력해주세요.", true);
            return;
        }

        if (!isLoggedIn) {
            if (!guestNameInput.value.trim() || !guestPhoneInput.value.trim()) {
                showMessage("비회원 이름과 휴대전화 번호를 입력해주세요.", true);
                return;
            }
        }

        submitButton.disabled = true;
        submitButton.textContent = "예약 처리 중...";

        try {
            const requestBody = {
                memberNo: isLoggedIn ? memberNo : null,
                guestName: isLoggedIn ? null : guestNameInput.value.trim(),
                guestPhone: isLoggedIn ? null : guestPhoneInput.value.trim(),
                serviceMenuNo: selectedMenuNo,
                startAt: `${selectedDate}T${selectedStartTime}:00`,
                requestMemo: memoInput.value.trim() || null,
                reservationSource: "ONLINE"
            };

            const response = await fetch("/api/reservations", {
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

            if (selectedFiles.length > 0) {
                for (const file of selectedFiles) {
                    await uploadImage(body.reservationNo, file);
                }
            }

            showMessage(
                `예약 신청이 완료되었습니다. 예약번호: ${body.reservationNo}`,
                false
            );

            setTimeout(() => {
                if (isLoggedIn) {
                    location.href = "/my-reservations";
                } else {
                    location.href = "/reservation";
                }
            }, 900);
        } catch (error) {
            showMessage(error.message || "예약 처리 중 오류가 발생했습니다.", true);
            submitButton.disabled = false;
            submitButton.textContent = "예약 신청";
        }
    });

    async function loadAvailableTimes() {
        timeSlots.innerHTML = `<div class="loading-box">예약 가능 시간을 조회 중입니다.</div>`;
        timeSection.classList.remove("hidden");

        try {
            const params = new URLSearchParams({
                date: selectedDate,
                serviceMenuNo: String(selectedMenuNo)
            });

            const response = await fetch(
                `/api/reservations/available-times?${params.toString()}`
            );

            const body = await readJson(response);

            if (!response.ok) {
                throw new Error(body.message || "예약 가능시간 조회에 실패했습니다.");
            }

            renderTimeSlots(body);
        } catch (error) {
            timeSlots.innerHTML =
                `<div class="empty-box">${escapeHtml(error.message)}</div>`;
        }
    }

    function renderTimeSlots(times) {
        timeSlots.innerHTML = "";

        if (!Array.isArray(times) || times.length === 0) {
            timeSlots.innerHTML =
                `<div class="empty-box">선택한 날짜에 예약 가능한 시간이 없습니다.</div>`;
            return;
        }

        times.forEach(time => {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "time-button";
            button.textContent = normalizeTime(time.startTime);
            button.dataset.startTime = normalizeTime(time.startTime);

            button.addEventListener("click", () => {
                document.querySelectorAll(".time-button")
                    .forEach(item => item.classList.remove("selected"));

                button.classList.add("selected");
                selectedStartTime = button.dataset.startTime;
                updateSummary();
            });

            timeSlots.appendChild(button);
        });
    }

    async function uploadImage(reservationNo, file) {
        const formData = new FormData();
        formData.append("file", file);

        const response = await fetch(
            `/api/reservations/${reservationNo}/images`,
            {
                method: "POST",
                headers: csrfHeaders(),
                body: formData
            }
        );

        if (!response.ok) {
            const body = await readJson(response);
            throw new Error(
                body.message ||
                `예약은 생성되었지만 ${file.name} 이미지 업로드에 실패했습니다.`
            );
        }
    }

    function renderPreview() {
        imagePreview.innerHTML = "";

        selectedFiles.forEach(file => {
            const item = document.createElement("div");
            item.className = "preview-item";

            const img = document.createElement("img");
            img.alt = file.name;

            const reader = new FileReader();
            reader.onload = event => {
                img.src = event.target.result;
            };
            reader.readAsDataURL(file);

            item.appendChild(img);
            imagePreview.appendChild(item);
        });
    }

    function clearSelectedTime() {
        selectedStartTime = "";
        timeSlots.innerHTML = "";
        submitButton.disabled = true;
    }

    function updateSummary() {
        if (!selectedMenuNo || !selectedDate || !selectedStartTime) {
            summary.textContent = "시술, 날짜, 시간을 선택해주세요.";
            submitButton.disabled = true;
            return;
        }

        summary.textContent =
            `${selectedMenuName} · ${selectedDate} ${selectedStartTime}`;

        submitButton.disabled = false;
    }

    function isReady() {
        return Boolean(
            selectedMenuNo &&
            selectedDate &&
            selectedStartTime
        );
    }

    function normalizeTime(value) {
        if (!value) return "";
        return String(value).slice(0, 5);
    }

    function csrfHeaders() {
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector('meta[name="_csrf_header"]')?.content;

        if (!token || !header) {
            return {};
        }

        return { [header]: token };
    }

    async function readJson(response) {
        const text = await response.text();
        if (!text) return {};

        try {
            return JSON.parse(text);
        } catch {
            return { message: text };
        }
    }

    function showMessage(message, error) {
        messageBox.textContent = message;
        messageBox.classList.toggle("error", Boolean(error));
        messageBox.classList.remove("hidden");

        setTimeout(() => {
            messageBox.classList.add("hidden");
        }, 3500);
    }

    function escapeHtml(value) {
        const div = document.createElement("div");
        div.textContent = value || "";
        return div.innerHTML;
    }
})();
