(() => {
    const page = document.getElementById("reservationPage");
    if (!page) return;

    const memberNoValue = page.dataset.memberNo;
    const isLoggedIn = page.dataset.loggedIn === "true";
    const memberNo = memberNoValue ? Number(memberNoValue) : null;

    const selectedHairStyleNo =
        page.dataset.hairStyleNo
            ? Number(page.dataset.hairStyleNo)
            : null;

    const selectedHairStyleTitle =
        page.dataset.hairStyleTitle || "";

    const linkedServiceMenuNos =
        (page.dataset.linkedServiceMenuNos || "")
            .split(",")
            .map(value => Number(value))
            .filter(Number.isFinite);

    const preferredServiceMenuNo =
        page.dataset.preferredServiceMenuNo
            ? Number(page.dataset.preferredServiceMenuNo)
            : null;

    const hasHairStyleContext =
        selectedHairStyleNo != null
        && linkedServiceMenuNos.length > 0;

    const dateInput = document.getElementById("reservationDate");
    const timeSection = document.getElementById("timeSection");
    const timeSlots = document.getElementById("timeSlots");
    const availabilityNotice =
        document.getElementById("availabilityNotice");

    const categoryRadios =
        document.querySelectorAll(".service-category-radio");

    const serviceMenuCards =
        document.querySelectorAll(".service-detail-card");

    const serviceMenuEmpty =
        document.getElementById("serviceMenuEmpty");

    const serviceCategoryHelp =
        document.getElementById("serviceCategoryHelp");

    const submitButton =
        document.getElementById("submitReservation");

    const summary =
        document.getElementById("reservationSummary");

    const memoInput =
        document.getElementById("requestMemo");

    const memoCount =
        document.getElementById("memoCount");

    const guestNameInput =
        document.getElementById("guestName");

    const guestPhoneInput =
        document.getElementById("guestPhone");

    const imageInput =
        document.getElementById("reservationImages");

    const imagePreview =
        document.getElementById("imagePreview");

    const messageBox =
        document.getElementById("messageBox");

    let selectedCategory = "";
    let selectedCategoryName = "";
    let selectedMenuNo = null;
    let selectedMenuName = "";
    let selectedDate = "";
    let selectedStartTime = "";
    let selectedFiles = [];

    const categoryNames = {
        CUT: "커트",
        PERM: "펌",
        COLOR: "컬러",
        CLINIC: "클리닉",
        ETC: "기타"
    };

    const today = new Date();

    dateInput.min = [
        today.getFullYear(),
        String(today.getMonth() + 1)
            .padStart(2, "0"),
        String(today.getDate())
            .padStart(2, "0")
    ].join("-");

    categoryRadios.forEach(radio => {
        radio.addEventListener(
            "change",
            event => {
                selectedCategory =
                    event.target.value;

                selectedCategoryName =
                    categoryNames[selectedCategory]
                    || selectedCategory;

                clearSelectedMenu();
                renderServiceMenus(
                    selectedCategory
                );

                updateSummary();
            }
        );
    });

    document.getElementById(
        "clearServiceCategory"
    )
        ?.addEventListener(
            "click",
            () => {
                if (hasHairStyleContext) {
                    location.href =
                        "/reservation";
                    return;
                }

                categoryRadios
                    .forEach(radio =>
                        radio.checked = false
                    );

                selectedCategory = "";
                selectedCategoryName = "";

                clearSelectedMenu();

                serviceMenuCards
                    .forEach(card =>
                        card.classList.add("hidden")
                    );

                serviceMenuEmpty.textContent =
                    "먼저 시술 카테고리를 선택해주세요.";

                serviceMenuEmpty
                    .classList.remove("hidden");

                clearSelectedTime();
                timeSection.classList.add("hidden");

                updateSummary();
            }
        );

    document.querySelectorAll(
        ".menu-radio"
    )
        .forEach(radio => {
            radio.addEventListener(
                "change",
                async event => {
                    selectedMenuNo =
                        Number(
                            event.target.value
                        );

                    selectedMenuName =
                        event.target
                            .closest(
                                ".menu-card"
                            )
                            .querySelector(
                                "strong"
                            )
                            .textContent
                            .trim();

                    clearSelectedTime();

                    if (selectedDate) {
                        await loadAvailableTimes();
                    }

                    updateSummary();
                }
            );
        });

    document.getElementById(
        "clearServiceMenu"
    )
        ?.addEventListener(
            "click",
            () => {
                clearSelectedMenu();
                clearSelectedTime();
                timeSection.classList.add("hidden");
                updateSummary();
            }
        );

    dateInput.addEventListener(
        "change",
        async () => {
            selectedDate =
                dateInput.value;

            clearSelectedTime();

            if (selectedMenuNo
                    && selectedDate) {
                await loadAvailableTimes();
            } else {
                timeSection
                    .classList.add("hidden");
            }

            updateSummary();
        }
    );

    memoInput.addEventListener(
        "input",
        () => {
            memoCount.textContent =
                String(
                    memoInput.value.length
                );
        }
    );

    if (guestPhoneInput) {
        guestPhoneInput
            .addEventListener(
                "input",
                () => {
                    guestPhoneInput.value =
                        guestPhoneInput.value
                            .replace(
                                /[^\d-]/g,
                                ""
                            );
                }
            );
    }

    imageInput.addEventListener(
        "change",
        () => {
            const files =
                Array.from(
                    imageInput.files || []
                );

            if (files.length > 3) {
                return resetFiles(
                    "참고 이미지는 최대 3장까지 선택할 수 있습니다."
                );
            }

            for (const file of files) {
                if (file.size
                        > 10 * 1024 * 1024) {
                    return resetFiles(
                        `${file.name}: 10MB를 초과합니다.`
                    );
                }

                if (![
                    "image/jpeg",
                    "image/png",
                    "image/webp"
                ].includes(file.type)) {
                    return resetFiles(
                        `${file.name}: 지원하지 않는 이미지 형식입니다.`
                    );
                }
            }

            selectedFiles = files;
            renderPreview();
        }
    );

    submitButton.addEventListener(
        "click",
        submitReservation
    );


    initializeHairStyleReservationContext();

    function initializeHairStyleReservationContext() {
        if (!hasHairStyleContext) {
            return;
        }

        if (serviceCategoryHelp) {
            serviceCategoryHelp.textContent =
                `"${selectedHairStyleTitle}" 스타일과 연결된 시술 카테고리만 선택할 수 있습니다.`;
        }

        const linkedCategories =
            new Set();

        serviceMenuCards
            .forEach(card => {
                const menuNo =
                    Number(
                        card.dataset.serviceMenuNo
                    );

                if (linkedServiceMenuNos
                        .includes(menuNo)) {
                    linkedCategories.add(
                        card.dataset.category
                    );
                }
            });

        categoryRadios
            .forEach(radio => {
                const enabled =
                    linkedCategories
                        .has(radio.value);

                radio.disabled = !enabled;

                radio.closest(
                    ".service-category-card"
                )
                    ?.classList.toggle(
                        "disabled",
                        !enabled
                    );
            });

        let targetMenuNo =
            preferredServiceMenuNo;

        if (targetMenuNo == null
                && linkedServiceMenuNos.length === 1) {
            targetMenuNo =
                linkedServiceMenuNos[0];
        }

        let targetCard = null;

        if (targetMenuNo != null) {
            targetCard =
                Array.from(
                    serviceMenuCards
                )
                    .find(
                        card =>
                            Number(
                                card.dataset.serviceMenuNo
                            )
                            === targetMenuNo
                    )
                    || null;
        }

        if (!targetCard) {
            targetCard =
                Array.from(
                    serviceMenuCards
                )
                    .find(
                        card =>
                            linkedServiceMenuNos
                                .includes(
                                    Number(
                                        card.dataset.serviceMenuNo
                                    )
                                )
                    )
                    || null;
        }

        if (!targetCard) {
            return;
        }

        const targetCategory =
            targetCard.dataset.category;

        const categoryRadio =
            Array.from(categoryRadios)
                .find(
                    radio =>
                        radio.value
                        === targetCategory
                );

        if (categoryRadio) {
            categoryRadio.checked = true;
            selectedCategory =
                targetCategory;
            selectedCategoryName =
                categoryNames[targetCategory]
                || targetCategory;
        }

        renderServiceMenus(
            targetCategory
        );

        if (targetMenuNo != null) {
            const menuRadio =
                targetCard.querySelector(
                    ".menu-radio"
                );

            if (menuRadio) {
                menuRadio.checked = true;
                selectedMenuNo =
                    Number(
                        menuRadio.value
                    );

                selectedMenuName =
                    targetCard
                        .querySelector(
                            "strong"
                        )
                        ?.textContent
                        ?.trim()
                    || "";
            }
        }

        updateSummary();
    }

    function renderServiceMenus(
            category
    ) {
        let visibleCount = 0;

        serviceMenuCards
            .forEach(card => {
                const menuNo =
                    Number(
                        card.dataset.serviceMenuNo
                    );

                const sameCategory =
                    card.dataset.category
                    === category;

                const linkedToStyle =
                    !hasHairStyleContext
                    || linkedServiceMenuNos
                        .includes(menuNo);

                const matches =
                    sameCategory
                    && linkedToStyle;

                card.classList
                    .toggle(
                        "hidden",
                        !matches
                    );

                if (matches) {
                    visibleCount++;
                }
            });

        if (visibleCount === 0) {
            serviceMenuEmpty.textContent =
                hasHairStyleContext
                    ? `${categoryNames[category] || category} 카테고리에는 선택한 헤어스타일과 연결된 시술 메뉴가 없습니다.`
                    : `${categoryNames[category] || category} 카테고리에 등록된 상세 시술 메뉴가 없습니다.`;

            serviceMenuEmpty
                .classList.remove(
                    "hidden"
                );
        } else {
            serviceMenuEmpty
                .classList.add(
                    "hidden"
                );
        }
    }

    function clearSelectedMenu() {
        document.querySelectorAll(
            ".menu-radio"
        )
            .forEach(radio =>
                radio.checked = false
            );

        selectedMenuNo = null;
        selectedMenuName = "";
    }

    async function loadAvailableTimes() {
        timeSlots.innerHTML =
            `<div class="loading-box">예약 가능 시간을 조회 중입니다.</div>`;

        timeSection
            .classList.remove(
                "hidden"
            );

        availabilityNotice
            ?.classList.add(
                "hidden"
            );

        if (availabilityNotice) {
            availabilityNotice.innerHTML = "";
        }

        const params =
            new URLSearchParams({
                date: selectedDate,
                serviceMenuNo:
                    String(
                        selectedMenuNo
                    )
            });

        try {
            const [
                timeResponse,
                noticeResponse
            ] =
                await Promise.all([
                    fetch(
                        `/api/reservations/available-times?${params}`
                    ),
                    fetch(
                        `/api/reservations/availability-notices?`
                        + new URLSearchParams({
                            date: selectedDate
                        })
                    )
                ]);

            const timeBody =
                await readJson(
                    timeResponse
                );

            const noticeBody =
                await readJson(
                    noticeResponse
                );

            if (!timeResponse.ok) {
                timeSlots.innerHTML =
                    `<div class="empty-box">${
                        escapeHtml(
                            timeBody.message
                            || "조회 실패"
                        )
                    }</div>`;
                return;
            }

            if (noticeResponse.ok) {
                renderAvailabilityNotice(
                    availabilityNotice,
                    noticeBody
                );
            }

            timeSlots.innerHTML = "";

            if (!Array.isArray(timeBody)
                    || timeBody.length === 0) {

                const customerMessage =
                    bestUnavailableMessage(
                        noticeBody
                    );

                timeSlots.innerHTML =
                    `<div class="empty-box">${
                        escapeHtml(
                            customerMessage
                            || "예약 가능한 시간이 없습니다."
                        )
                    }</div>`;

                return;
            }

            timeBody.forEach(time => {
                const button =
                    document.createElement(
                        "button"
                    );

                button.type = "button";
                button.className =
                    "time-button";

                button.textContent =
                    String(
                        time.startTime
                    ).slice(0, 5);

                button.addEventListener(
                    "click",
                    () => {
                        document.querySelectorAll(
                            ".time-button"
                        )
                            .forEach(item =>
                                item.classList
                                    .remove(
                                        "selected"
                                    )
                            );

                        button.classList
                            .add("selected");

                        selectedStartTime =
                            String(
                                time.startTime
                            ).slice(0, 5);

                        updateSummary();
                    }
                );

                timeSlots.appendChild(
                    button
                );
            });
        } catch (error) {
            timeSlots.innerHTML =
                `<div class="empty-box">예약 정보를 불러오지 못했습니다.</div>`;
        }
    }

    function renderAvailabilityNotice(
            container,
            notice
    ) {
        if (!container || !notice) {
            return;
        }

        const lines = [];

        const hasAllDayNotice =
            (notice.notices || [])
                .some(
                    item =>
                        item.allDay
                );

        if (notice.openDay === false
                && notice.dayMessage
                && !hasAllDayNotice) {

            lines.push({
                type: "closed",
                text:
                    notice.dayMessage
            });
        }

        (notice.notices || [])
            .forEach(item => {
                let text =
                    item.message
                    || item.title
                    || "";

                if (!item.allDay
                        && item.startTime
                        && item.endTime) {

                    text +=
                        ` (${String(
                            item.startTime
                        ).slice(0, 5)}`
                        + ` ~ ${String(
                            item.endTime
                        ).slice(0, 5)})`;
                }

                lines.push({
                    type:
                        item.noticeType
                        === "PERSONAL"
                            ? "personal"
                            : "holiday",
                    text
                });
            });

        if (!lines.length) {
            container
                .classList.add(
                    "hidden"
                );

            container.innerHTML = "";
            return;
        }

        container.innerHTML =
            lines
                .map(line => `
                    <div class="availability-notice-item availability-${line.type}">
                        ${escapeHtml(line.text)}
                    </div>
                `)
                .join("");

        container
            .classList.remove(
                "hidden"
            );
    }

    function bestUnavailableMessage(
            notice
    ) {
        if (!notice) {
            return null;
        }

        const allDay =
            (notice.notices || [])
                .find(
                    item =>
                        item.allDay
                );

        if (allDay) {
            return allDay.message
                || allDay.title;
        }

        if (notice.openDay === false) {
            return notice.dayMessage
                || "정기 휴무일입니다.";
        }

        return null;
    }

    async function submitReservation() {
        if (!selectedCategory
                || !selectedMenuNo
                || !selectedDate
                || !selectedStartTime) {

            return showMessage(
                "시술 카테고리, 상세 메뉴, 날짜, 시간을 선택해주세요.",
                true
            );
        }

        if (!isLoggedIn) {
            const name =
                guestNameInput.value
                    .trim();

            const phone =
                guestPhoneInput.value
                    .trim();

            if (!/^[\p{L}][\p{L}\p{M} .'-]{0,48}[\p{L}\p{M}]$/u
                    .test(name)) {

                return showMessage(
                    "예약자 이름 형식을 확인해주세요.",
                    true
                );
            }

            if (!/^010-?\d{4}-?\d{4}$/
                    .test(phone)) {

                return showMessage(
                    "휴대전화 번호 형식을 확인해주세요.",
                    true
                );
            }
        }

        submitButton.disabled = true;
        submitButton.textContent =
            "예약 처리 중...";

        try {
            const requestBody = {
                guestName:
                    isLoggedIn
                        ? null
                        : guestNameInput.value
                            .trim(),

                guestPhone:
                    isLoggedIn
                        ? null
                        : guestPhoneInput.value
                            .trim(),

                serviceMenuNo:
                    selectedMenuNo,

                /*
                 * 일반 /reservation 진입은 hairStyleNo = null.
                 * /hairstyles/{no}에서 진입한 경우에는 선택한 hairStyleNo를 유지해
                 * RESERVATION.HAIR_STYLE_NO에 저장한다.
                 */
                hairStyleNo:
                    selectedHairStyleNo,

                startAt:
                    `${selectedDate}T${selectedStartTime}:00`,

                requestMemo:
                    memoInput.value
                        .trim()
                    || null,

                reservationSource:
                    "ONLINE"
            };

            const response =
                await fetch(
                    isLoggedIn
                        ? "/api/reservations/me"
                        : "/api/reservations",
                    {
                        method: "POST",
                        headers: {
                            "Content-Type":
                                "application/json",
                            ...csrfHeaders()
                        },
                        body:
                            JSON.stringify(
                                requestBody
                            )
                    }
                );

            const body =
                await readJson(
                    response
                );

            if (!response.ok) {
                throw new Error(
                    body.message
                    || "예약 신청에 실패했습니다."
                );
            }

            for (const file
                    of selectedFiles) {

                await uploadImage(
                    body.reservationNo,
                    file
                );
            }

            if (isLoggedIn) {
                showMessage(
                    `예약이 완료되었습니다. 예약번호 ${body.reservationNo}`,
                    false
                );

                setTimeout(
                    () =>
                        location.href =
                            "/my-reservations",
                    1000
                );
            } else {
                alert(
                    `예약이 완료되었습니다.\n예약번호: ${body.reservationNo}\n\n`
                    + "비회원 예약 조회 시 예약번호와 휴대전화 번호가 필요합니다."
                );

                location.href =
                    "/guest-reservation";
            }
        } catch (error) {
            showMessage(
                error.message,
                true
            );

            submitButton.disabled = false;
            submitButton.textContent =
                "예약 신청";
        }
    }

    async function uploadImage(
            reservationNo,
            file
    ) {
        const data =
            new FormData();

        data.append(
            "file",
            file
        );

        let uploadUrl;

        if (isLoggedIn) {
            uploadUrl =
                `/api/reservations/me/${reservationNo}/images`;
        } else {
            data.append(
                "guestPhone",
                guestPhoneInput.value
                    .trim()
            );

            uploadUrl =
                `/api/reservations/guest/${reservationNo}/images`;
        }

        const response =
            await fetch(
                uploadUrl,
                {
                    method: "POST",
                    headers:
                        csrfHeaders(),
                    body: data
                }
            );

        if (!response.ok) {
            const body =
                await readJson(
                    response
                );

            throw new Error(
                body.message
                || "예약은 생성됐지만 이미지 업로드에 실패했습니다."
            );
        }
    }

    function updateSummary() {
        if (!selectedCategory
                || !selectedMenuNo
                || !selectedDate
                || !selectedStartTime) {

            summary.textContent =
                "시술 카테고리, 상세 메뉴, 날짜, 시간을 선택해주세요.";

            submitButton.disabled = true;
            return;
        }

        summary.textContent =
            `${selectedCategoryName} · `
            + `${selectedMenuName} · `
            + `${selectedDate} `
            + `${selectedStartTime}`;

        submitButton.disabled = false;
    }

    function clearSelectedTime() {
        selectedStartTime = "";
        timeSlots.innerHTML = "";
        submitButton.disabled = true;
    }

    function resetFiles(message) {
        showMessage(
            message,
            true
        );

        imageInput.value = "";
        selectedFiles = [];

        renderPreview();
    }

    function renderPreview() {
        imagePreview.innerHTML = "";

        selectedFiles
            .forEach(file => {
                const item =
                    document.createElement(
                        "div"
                    );

                item.className =
                    "preview-item";

                const img =
                    document.createElement(
                        "img"
                    );

                const reader =
                    new FileReader();

                reader.onload =
                    event =>
                        img.src =
                            event.target.result;

                reader.readAsDataURL(
                    file
                );

                item.appendChild(img);
                imagePreview.appendChild(
                    item
                );
            });
    }

    function csrfHeaders() {
        const token =
            document.querySelector(
                'meta[name="_csrf"]'
            )?.content;

        const header =
            document.querySelector(
                'meta[name="_csrf_header"]'
            )?.content;

        return token && header
            ? { [header]: token }
            : {};
    }

    async function readJson(response) {
        const text =
            await response.text();

        if (!text) {
            return {};
        }

        try {
            return JSON.parse(text);
        } catch {
            return {
                message: text
            };
        }
    }

    function showMessage(
            message,
            error
    ) {
        messageBox.textContent =
            message;

        messageBox.classList
            .toggle(
                "error",
                Boolean(error)
            );

        messageBox.classList
            .remove("hidden");

        setTimeout(
            () =>
                messageBox.classList
                    .add("hidden"),
            3500
        );
    }

    function escapeHtml(value) {
        const div =
            document.createElement(
                "div"
            );

        div.textContent =
            value || "";

        return div.innerHTML;
    }
})();
