(() => {
    const form = document.getElementById("searchForm");
    if (!form) return;

    const tbody = document.getElementById("reservationTableBody");
    const pageInfo = document.getElementById("pageInfo");
    const prevButton = document.getElementById("prevPage");
    const nextButton = document.getElementById("nextPage");
    const resetButton = document.getElementById("resetSearch");
    const messageBox = document.getElementById("messageBox");

    const detailOverlay = document.getElementById("detailOverlay");
    const detailContent = document.getElementById("detailContent");

    const toolOverlay = document.getElementById("adminToolOverlay");
    const toolTitle = document.getElementById("adminToolTitle");
    const toolContent = document.getElementById("adminToolContent");

    let currentPage = 0;
    const pageSize = 20;
    let totalPages = 1;

    let serviceMenus = [];
    let phoneSelectedTime = null;

    form.addEventListener("submit", e => {
        e.preventDefault();
        currentPage = 0;
        withScrollPreserved(loadReservations);
    });

    resetButton.addEventListener("click", () => {
        form.reset();
        currentPage = 0;
        withScrollPreserved(loadReservations);
    });

    prevButton.addEventListener("click", () => {
        if (currentPage > 0) {
            currentPage--;
            withScrollPreserved(loadReservations);
        }
    });

    nextButton.addEventListener("click", () => {
        if (currentPage + 1 < totalPages) {
            currentPage++;
            withScrollPreserved(loadReservations);
        }
    });

    document.getElementById("closeDetail")
        ?.addEventListener("click", closeDetail);

    detailOverlay?.addEventListener("click", e => {
        if (e.target === detailOverlay) closeDetail();
    });

    document.getElementById("closeAdminTool")
        ?.addEventListener("click", closeTool);

    toolOverlay?.addEventListener("click", e => {
        if (e.target === toolOverlay) closeTool();
    });

    document.getElementById("openPhoneReservation")
        ?.addEventListener("click", openPhoneReservation);

    document.getElementById("openBusinessHours")
        ?.addEventListener("click", openBusinessHours);

    document.getElementById("openHolidays")
        ?.addEventListener("click", openHolidays);

    document.getElementById("openAvailabilityBlocks")
        ?.addEventListener("click", openAvailabilityBlocks);

    loadReservations();

    async function loadReservations() {
        tbody.innerHTML =
            `<tr><td colspan="7" class="loading-box">예약을 불러오는 중입니다.</td></tr>`;

        try {
            const response = await fetch(
                `/admin/api/reservations?${buildParams()}`
            );
            const body = await readJson(response);

            if (!response.ok) {
                throw new Error(body.message || "예약 검색에 실패했습니다.");
            }

            totalPages = Math.max(body.totalPages || 0, 1);
            currentPage = body.page || 0;
            renderRows(body.content || []);

            pageInfo.textContent = `${currentPage + 1} / ${totalPages}`;
            prevButton.disabled = currentPage <= 0;
            nextButton.disabled = currentPage + 1 >= totalPages;
        } catch (error) {
            tbody.innerHTML =
                `<tr><td colspan="7">${escapeHtml(error.message)}</td></tr>`;
        }
    }

    function renderRows(rows) {
        tbody.innerHTML = "";

        if (!rows.length) {
            tbody.innerHTML =
                `<tr><td colspan="7" class="empty-box">검색된 예약이 없습니다.</td></tr>`;
            return;
        }

        rows.forEach(r => {
            const tr = document.createElement("tr");

            const customer =
                r.customerType === "MEMBER"
                    ? `회원 #${r.memberNo}`
                    : `${escapeHtml(r.guestName || "")}<br>${escapeHtml(r.guestPhone || "")}`;

            const memo =
                r.requestMemo
                    ? escapeHtml(
                        r.requestMemo.length > 24
                            ? r.requestMemo.slice(0, 24) + "…"
                            : r.requestMemo
                    )
                    : "-";

            tr.innerHTML = `
                <td>${r.reservationNo}</td>
                <td>${customer}</td>
                <td>${escapeHtml(r.serviceName)}</td>
                <td>${formatDateTime(r.startAt)}</td>
                <td>
                    <span class="status-badge status-${r.status}">
                        ${statusText(r.status)}
                    </span>
                </td>
                <td>${memo}</td>
                <td>
                    <div class="action-group" data-no="${r.reservationNo}">
                        <button type="button"
                                class="action-button detail-button">
                            상세
                        </button>
                    </div>
                </td>
            `;

            tbody.appendChild(tr);

            const actions = tr.querySelector(".action-group");

            actions.querySelector(".detail-button")
                ?.addEventListener("click", () =>
                    openDetail(r.reservationNo)
                );

            if (r.status === "REQUESTED") {
                addAction(actions, "확정", () =>
                    withScrollPreserved(() =>
                        changeStatus(r.reservationNo, "confirm")
                    )
                );
                addAction(actions, "취소", () =>
                    cancelAdmin(r.reservationNo)
                );
            }

            if (r.status === "CONFIRMED") {
                addAction(actions, "완료", () =>
                    withScrollPreserved(() =>
                        changeStatus(r.reservationNo, "complete")
                    )
                );
                addAction(actions, "노쇼", () =>
                    noShow(r.reservationNo)
                );
                addAction(actions, "취소", () =>
                    cancelAdmin(r.reservationNo)
                );
            }
        });
    }

    async function openDetail(reservationNo) {
        try {
            const response = await fetch(
                `/admin/api/reservations/${reservationNo}`
            );
            const body = await readJson(response);

            if (!response.ok) {
                throw new Error(body.message || "상세 조회에 실패했습니다.");
            }

            const r = body.reservation;

            const customer =
                r.customerType === "MEMBER"
                    ? `${escapeHtml(body.memberName || "회원")} / ${escapeHtml(body.memberPhone || "")}`
                    : `${escapeHtml(r.guestName || "")} / ${escapeHtml(r.guestPhone || "")}`;

            const styleBlock = body.hairStyleTitle
                ? `
                    <div class="detail-block">
                        <strong>선택 예시 스타일</strong><br>
                        ${escapeHtml(body.hairStyleTitle)}
                        ${body.hairStyleImageUrl
                            ? `<div class="detail-style-image-wrap">
                                   <img class="hair-style-image"
                                        src="${escapeAttribute(body.hairStyleImageUrl)}"
                                        alt="">
                               </div>`
                            : ""}
                    </div>
                  `
                : `
                    <div class="detail-block">
                        <strong>선택 예시 스타일</strong><br>선택 안 함
                    </div>
                  `;

            const images = (body.images || []).length
                ? body.images.map(img => `
                    <a href="${escapeAttribute(img.fileUrl)}"
                       target="_blank"
                       rel="noopener"
                       class="protected-image-link">
                        <img src="${escapeAttribute(img.fileUrl)}"
                             alt="${escapeAttribute(img.originalFileName || "참고 이미지")}">
                    </a>
                  `).join("")
                : `<div class="empty-box">첨부된 참고 이미지가 없습니다.</div>`;

            detailContent.innerHTML = `
                <div class="detail-grid">
                    <div class="detail-block">
                        <strong>예약번호</strong><br>${r.reservationNo}
                    </div>
                    <div class="detail-block">
                        <strong>고객</strong><br>${customer}
                    </div>
                    <div class="detail-block">
                        <strong>고객 유형</strong><br>${r.customerType === "MEMBER" ? "회원" : "비회원"}
                    </div>
                    <div class="detail-block">
                        <strong>예약 경로</strong><br>${sourceText(r.reservationSource)}
                    </div>
                    <div class="detail-block">
                        <strong>시술</strong><br>${escapeHtml(r.serviceName)}
                    </div>
                    <div class="detail-block">
                        <strong>예약시간</strong><br>
                        ${formatDateTime(r.startAt)} ~ ${formatTime(r.endAt)}
                    </div>
                    <div class="detail-block">
                        <strong>소요시간</strong><br>${r.durationMinutes}분
                    </div>
                    <div class="detail-block">
                        <strong>상태</strong><br>${statusText(r.status)}
                    </div>
                    <div class="detail-block">
                        <strong>요청사항</strong><br>${escapeHtml(r.requestMemo || "없음")}
                    </div>
                    <div class="detail-block">
                        <strong>생성일시</strong><br>${formatDateTime(r.createdAt)}
                    </div>
                    ${r.confirmedAt
                        ? `<div class="detail-block"><strong>확정일시</strong><br>${formatDateTime(r.confirmedAt)}</div>`
                        : ""}
                    ${r.completedAt
                        ? `<div class="detail-block"><strong>완료일시</strong><br>${formatDateTime(r.completedAt)}</div>`
                        : ""}
                    ${r.canceledAt
                        ? `<div class="detail-block"><strong>취소일시</strong><br>${formatDateTime(r.canceledAt)}</div>`
                        : ""}
                    ${r.cancelReason
                        ? `<div class="detail-block"><strong>취소사유</strong><br>${escapeHtml(r.cancelReason)}</div>`
                        : ""}
                    ${styleBlock}
                </div>

                <h3>고객 참고 이미지</h3>
                <p class="field-help">
                    관리자 권한이 확인된 보호 API를 통해 조회됩니다.
                </p>
                <div class="detail-images">${images}</div>
            `;

            detailOverlay.classList.remove("hidden");
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    async function changeStatus(reservationNo, action) {
        await postAction(
            `/admin/api/reservations/${reservationNo}/${action}`
        );
    }

    async function cancelAdmin(reservationNo) {
        const reason = prompt("관리자 취소 사유를 입력해주세요.");
        if (reason === null) return;
        if (!reason.trim()) {
            return showMessage("취소 사유를 입력해주세요.", true);
        }

        await withScrollPreserved(() =>
            postAction(
                `/admin/api/reservations/${reservationNo}/cancel?` +
                new URLSearchParams({ reason: reason.trim() })
            )
        );
    }

    async function noShow(reservationNo) {
        const reason = prompt("노쇼 사유를 입력해주세요.", "예약시간 미방문");
        if (reason === null) return;

        const adminMemo = prompt("관리자 메모를 입력해주세요.", "");
        if (adminMemo === null) return;

        await withScrollPreserved(() =>
            postAction(
                `/admin/api/reservations/${reservationNo}/no-show?` +
                new URLSearchParams({
                    reason: reason.trim(),
                    adminMemo: adminMemo.trim()
                })
            )
        );
    }

    async function postAction(url) {
        const response = await fetch(url, {
            method: "POST",
            headers: csrfHeaders()
        });

        const body = await readJson(response);

        if (!response.ok) {
            throw new Error(body.message || "처리에 실패했습니다.");
        }

        showMessage("처리가 완료되었습니다.", false);
        await loadReservations();
    }

    // -----------------------------------------------------------------
    // 전화 예약
    // -----------------------------------------------------------------

    async function openPhoneReservation() {
        try {
            await ensureServiceMenus();

            toolTitle.textContent = "전화 예약 등록";
            phoneSelectedTime = null;

            toolContent.innerHTML = `
                <form id="phoneReservationForm">
                    <div class="form-two-columns">
                        <label class="field">
                            <span>예약자 이름</span>
                            <input id="phoneGuestName"
                                   type="text"
                                   maxlength="50"
                                   required>
                        </label>

                        <label class="field">
                            <span>휴대전화</span>
                            <input id="phoneGuestPhone"
                                   type="tel"
                                   placeholder="01012345678"
                                   maxlength="13"
                                   required>
                        </label>

                        <label class="field">
                            <span>시술</span>
                            <select id="phoneServiceMenu" required>
                                ${serviceMenus.map(menu => `
                                    <option value="${menu.serviceMenuNo}">
                                        ${escapeHtml(menu.name)}
                                        (${menu.durationMin}분)
                                    </option>
                                `).join("")}
                            </select>
                        </label>

                        <label class="field">
                            <span>예약 날짜</span>
                            <input id="phoneReservationDate"
                                   type="date"
                                   required>
                        </label>
                    </div>

                    <div class="field">
                        <span>예약 가능 시간</span>
                        <div id="phoneTimeSlots" class="time-slots">
                            <div class="empty-box">날짜를 선택해주세요.</div>
                        </div>
                    </div>

                    <label class="field">
                        <span>요청사항</span>
                        <textarea id="phoneRequestMemo"
                                  maxlength="500"
                                  rows="4"></textarea>
                    </label>

                    <div class="modal-actions">
                        <button type="submit" class="primary-button">
                            전화 예약 등록
                        </button>
                        <button type="button"
                                id="cancelPhoneReservation"
                                class="secondary-button">
                            취소
                        </button>
                    </div>
                </form>
            `;

            toolOverlay.classList.remove("hidden");

            const dateInput =
                document.getElementById("phoneReservationDate");
            const serviceSelect =
                document.getElementById("phoneServiceMenu");

            dateInput.min = todayString();

            dateInput.addEventListener("change", () => {
                phoneSelectedTime = null;
                loadPhoneTimes();
            });

            serviceSelect.addEventListener("change", () => {
                phoneSelectedTime = null;
                loadPhoneTimes();
            });

            document.getElementById("cancelPhoneReservation")
                .addEventListener("click", closeTool);

            document.getElementById("phoneReservationForm")
                .addEventListener("submit", async e => {
                    e.preventDefault();
                    await submitPhoneReservation();
                });
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    async function loadPhoneTimes() {
        const serviceMenuNo =
            Number(document.getElementById("phoneServiceMenu")?.value);
        const date =
            document.getElementById("phoneReservationDate")?.value;
        const container =
            document.getElementById("phoneTimeSlots");

        if (!container) return;

        if (!serviceMenuNo || !date) {
            container.innerHTML =
                `<div class="empty-box">시술과 날짜를 선택해주세요.</div>`;
            return;
        }

        container.innerHTML =
            `<div class="loading-box">예약 가능 시간을 조회하는 중입니다.</div>`;

        try {
            const response = await fetch(
                `/api/reservations/available-times?` +
                new URLSearchParams({
                    date,
                    serviceMenuNo: String(serviceMenuNo)
                })
            );

            const body = await readJson(response);

            if (!response.ok) {
                throw new Error(body.message || "예약 가능 시간 조회에 실패했습니다.");
            }

            if (!Array.isArray(body) || !body.length) {
                container.innerHTML =
                    `<div class="empty-box">예약 가능한 시간이 없습니다.</div>`;
                return;
            }

            container.innerHTML = "";

            body.forEach(slot => {
                const start = timeValue(slot.startTime);
                const end = timeValue(slot.endTime);

                const button = document.createElement("button");
                button.type = "button";
                button.className = "time-button";
                button.textContent = `${start} ~ ${end}`;

                button.addEventListener("click", () => {
                    phoneSelectedTime = start;
                    container.querySelectorAll(".time-button")
                        .forEach(el => el.classList.remove("selected"));
                    button.classList.add("selected");
                });

                container.appendChild(button);
            });
        } catch (error) {
            container.innerHTML =
                `<div class="empty-box">${escapeHtml(error.message)}</div>`;
        }
    }

    async function submitPhoneReservation() {
        const guestName =
            document.getElementById("phoneGuestName").value.trim();
        const guestPhone =
            document.getElementById("phoneGuestPhone").value.trim();
        const serviceMenuNo =
            Number(document.getElementById("phoneServiceMenu").value);
        const date =
            document.getElementById("phoneReservationDate").value;
        const requestMemo =
            document.getElementById("phoneRequestMemo").value.trim();

        if (!guestName ||
            !/^010-?\d{4}-?\d{4}$/.test(guestPhone) ||
            !serviceMenuNo ||
            !date ||
            !phoneSelectedTime) {
            return showMessage(
                "이름, 010 휴대전화 번호, 시술, 날짜, 시간을 확인해주세요.",
                true
            );
        }

        const response = await fetch(
            "/admin/api/reservations/phone",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...csrfHeaders()
                },
                body: JSON.stringify({
                    guestName,
                    guestPhone,
                    serviceMenuNo,
                    hairStyleNo: null,
                    startAt: `${date}T${phoneSelectedTime}:00`,
                    requestMemo: requestMemo || null
                })
            }
        );

        const body = await readJson(response);

        if (!response.ok) {
            return showMessage(
                body.message || "전화 예약 등록에 실패했습니다.",
                true
            );
        }

        const y = window.scrollY;
        closeTool();
        showMessage(
            `전화 예약이 등록되었습니다. 예약번호 #${body.reservationNo}`,
            false
        );
        await loadReservations();
        restoreScroll(y);
    }

    // -----------------------------------------------------------------
    // 영업시간
    // -----------------------------------------------------------------

    async function openBusinessHours() {
        toolTitle.textContent = "영업시간 / 정기휴무";
        toolContent.innerHTML =
            `<div class="loading-box">영업시간을 불러오는 중입니다.</div>`;
        toolOverlay.classList.remove("hidden");

        try {
            const response =
                await fetch("/admin/api/business-hours");
            const body = await readJson(response);

            if (!response.ok) {
                throw new Error(body.message || "영업시간 조회에 실패했습니다.");
            }

            toolContent.innerHTML = `
                <div class="admin-setting-list">
                    ${(body || []).map(hour => `
                        <div class="admin-setting-row"
                             data-day="${hour.dayOfWeek}">
                            <strong>${dayName(hour.dayOfWeek)}</strong>

                            <label class="inline-check">
                                <input type="checkbox"
                                       class="business-open"
                                       ${hour.open ? "checked" : ""}>
                                영업
                            </label>

                            <input type="time"
                                   class="business-open-time"
                                   value="${escapeAttribute(hour.openTime || "")}"
                                   ${hour.open ? "" : "disabled"}>

                            <span>~</span>

                            <input type="time"
                                   class="business-close-time"
                                   value="${escapeAttribute(hour.closeTime || "")}"
                                   ${hour.open ? "" : "disabled"}>

                            <button type="button"
                                    class="secondary-button save-business-hour">
                                저장
                            </button>
                        </div>
                    `).join("")}
                </div>
            `;

            toolContent.querySelectorAll(".admin-setting-row")
                .forEach(row => {
                    const open =
                        row.querySelector(".business-open");
                    const openTime =
                        row.querySelector(".business-open-time");
                    const closeTime =
                        row.querySelector(".business-close-time");

                    open.addEventListener("change", () => {
                        openTime.disabled = !open.checked;
                        closeTime.disabled = !open.checked;
                    });

                    row.querySelector(".save-business-hour")
                        .addEventListener("click", () =>
                            saveBusinessHour(row)
                        );
                });
        } catch (error) {
            toolContent.innerHTML =
                `<div class="empty-box">${escapeHtml(error.message)}</div>`;
        }
    }

    async function saveBusinessHour(row) {
        const dayOfWeek = Number(row.dataset.day);
        const open = row.querySelector(".business-open").checked;
        const openTime = row.querySelector(".business-open-time").value;
        const closeTime = row.querySelector(".business-close-time").value;

        if (open && (!openTime || !closeTime)) {
            return showMessage("영업일은 오픈/마감 시간을 입력해주세요.", true);
        }

        const response = await fetch(
            `/admin/api/business-hours/${dayOfWeek}`,
            {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    ...csrfHeaders()
                },
                body: JSON.stringify({
                    open,
                    openTime: open ? openTime : null,
                    closeTime: open ? closeTime : null
                })
            }
        );

        const body = await readJson(response);

        if (!response.ok) {
            return showMessage(
                body.message || "영업시간 저장에 실패했습니다.",
                true
            );
        }

        showMessage(`${dayName(dayOfWeek)} 영업시간이 저장되었습니다.`, false);
    }

    // -----------------------------------------------------------------
    // 휴일
    // -----------------------------------------------------------------

    async function openHolidays() {
        toolTitle.textContent = "임시 휴일 / 휴가 관리";
        toolContent.innerHTML =
            `<div class="loading-box">휴일을 불러오는 중입니다.</div>`;
        toolOverlay.classList.remove("hidden");

        try {
            const response =
                await fetch("/admin/api/holidays");
            const body = await readJson(response);

            if (!response.ok) {
                throw new Error(body.message || "휴일 조회에 실패했습니다.");
            }

            renderHolidayTool(Array.isArray(body) ? body : []);
        } catch (error) {
            toolContent.innerHTML =
                `<div class="empty-box">${escapeHtml(error.message)}</div>`;
        }
    }

    function renderHolidayTool(items) {
        toolContent.innerHTML = `
            <form id="holidayForm" class="admin-editor-form">
                <input type="hidden" id="holidayNo">

                <div class="form-two-columns">
                    <label class="field">
                        <span>유형</span>
                        <select id="holidayType">
                            <option value="TEMPORARY">임시 휴일</option>
                            <option value="VACATION">휴가</option>
                            <option value="ETC">기타</option>
                        </select>
                    </label>

                    <label class="field">
                        <span>제목</span>
                        <input id="holidayTitle"
                               type="text"
                               maxlength="100"
                               required>
                    </label>

                    <label class="field">
                        <span>시작</span>
                        <input id="holidayStartAt"
                               type="datetime-local"
                               required>
                    </label>

                    <label class="field">
                        <span>종료</span>
                        <input id="holidayEndAt"
                               type="datetime-local"
                               required>
                    </label>
                </div>

                <label class="inline-check editor-check">
                    <input id="holidayAllDay" type="checkbox">
                    하루 전체 일정
                </label>

                <label class="field">
                    <span>메모</span>
                    <textarea id="holidayMemo"
                              maxlength="500"
                              rows="3"></textarea>
                </label>

                <div class="modal-actions">
                    <button type="submit" class="primary-button">
                        저장
                    </button>
                    <button id="resetHolidayForm"
                            type="button"
                            class="secondary-button">
                        새 일정
                    </button>
                </div>
            </form>

            <h3>등록된 휴일</h3>
            <div class="admin-record-list">
                ${items.length
                    ? items.map(holiday => holidayCard(holiday)).join("")
                    : `<div class="empty-box">등록된 휴일이 없습니다.</div>`}
            </div>
        `;

        document.getElementById("holidayForm")
            .addEventListener("submit", async e => {
                e.preventDefault();
                await saveHoliday();
            });

        document.getElementById("resetHolidayForm")
            .addEventListener("click", resetHolidayForm);

        toolContent.querySelectorAll(".edit-holiday")
            .forEach(button => {
                button.addEventListener("click", () => {
                    const item =
                        items.find(v =>
                            String(v.salonHolidayNo) === button.dataset.no
                        );
                    if (item) fillHolidayForm(item);
                });
            });

        toolContent.querySelectorAll(".delete-holiday")
            .forEach(button => {
                button.addEventListener("click", async () => {
                    if (!confirm("이 휴일을 삭제하시겠습니까?")) return;
                    await deleteHoliday(Number(button.dataset.no));
                });
            });
    }

    function holidayCard(item) {
        return `
            <article class="admin-record-card">
                <div>
                    <strong>${escapeHtml(item.title)}</strong>
                    <div>${holidayTypeText(item.holidayType)}</div>
                    <div>
                        ${formatDateTime(item.startAt)}
                        ~ ${formatDateTime(item.endAt)}
                    </div>
                    ${item.memo
                        ? `<div>${escapeHtml(item.memo)}</div>`
                        : ""}
                </div>
                <div class="action-group">
                    <button type="button"
                            class="action-button edit-holiday"
                            data-no="${item.salonHolidayNo}">
                        수정
                    </button>
                    <button type="button"
                            class="action-button delete-holiday"
                            data-no="${item.salonHolidayNo}">
                        삭제
                    </button>
                </div>
            </article>
        `;
    }

    function fillHolidayForm(item) {
        document.getElementById("holidayNo").value =
            item.salonHolidayNo;
        document.getElementById("holidayType").value =
            item.holidayType;
        document.getElementById("holidayTitle").value =
            item.title || "";
        document.getElementById("holidayStartAt").value =
            localDateTimeInput(item.startAt);
        document.getElementById("holidayEndAt").value =
            localDateTimeInput(item.endAt);
        document.getElementById("holidayAllDay").checked =
            item.allDay === true;
        document.getElementById("holidayMemo").value =
            item.memo || "";
    }

    function resetHolidayForm() {
        document.getElementById("holidayForm").reset();
        document.getElementById("holidayNo").value = "";
    }

    async function saveHoliday() {
        const no = document.getElementById("holidayNo").value;
        const payload = {
            holidayType: document.getElementById("holidayType").value,
            title: document.getElementById("holidayTitle").value.trim(),
            startAt: toApiDateTime(
                document.getElementById("holidayStartAt").value
            ),
            endAt: toApiDateTime(
                document.getElementById("holidayEndAt").value
            ),
            allDay: document.getElementById("holidayAllDay").checked,
            memo:
                document.getElementById("holidayMemo").value.trim() || null
        };

        if (!payload.title || !payload.startAt || !payload.endAt) {
            return showMessage("제목, 시작, 종료 시간을 입력해주세요.", true);
        }

        const response = await fetch(
            no
                ? `/admin/api/holidays/${no}`
                : "/admin/api/holidays",
            {
                method: no ? "PUT" : "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...csrfHeaders()
                },
                body: JSON.stringify(payload)
            }
        );

        const body = await readJson(response);

        if (!response.ok) {
            return showMessage(
                body.message || "휴일 저장에 실패했습니다.",
                true
            );
        }

        showMessage("휴일이 저장되었습니다.", false);
        await openHolidays();
    }

    async function deleteHoliday(no) {
        const response = await fetch(
            `/admin/api/holidays/${no}`,
            {
                method: "DELETE",
                headers: csrfHeaders()
            }
        );

        if (!response.ok) {
            const body = await readJson(response);
            return showMessage(
                body.message || "휴일 삭제에 실패했습니다.",
                true
            );
        }

        showMessage("휴일이 삭제되었습니다.", false);
        await openHolidays();
    }

    // -----------------------------------------------------------------
    // 개인 일정 / 예약 불가 시간
    // -----------------------------------------------------------------

    async function openAvailabilityBlocks() {
        toolTitle.textContent = "개인 일정 / 예약 불가 시간";
        toolContent.innerHTML =
            `<div class="loading-box">개인 일정을 불러오는 중입니다.</div>`;
        toolOverlay.classList.remove("hidden");

        try {
            const response =
                await fetch("/admin/api/availability-blocks");
            const body = await readJson(response);

            if (!response.ok) {
                throw new Error(
                    body.message || "개인 일정 조회에 실패했습니다."
                );
            }

            renderBlockTool(Array.isArray(body) ? body : []);
        } catch (error) {
            toolContent.innerHTML =
                `<div class="empty-box">${escapeHtml(error.message)}</div>`;
        }
    }

    function renderBlockTool(items) {
        toolContent.innerHTML = `
            <form id="blockForm" class="admin-editor-form">
                <input type="hidden" id="blockNo">

                <div class="form-two-columns">
                    <label class="field">
                        <span>제목</span>
                        <input id="blockTitle"
                               type="text"
                               maxlength="100"
                               placeholder="점심 / 외출 / 병원 / 교육"
                               required>
                    </label>

                    <label class="field">
                        <span>시작</span>
                        <input id="blockStartAt"
                               type="datetime-local"
                               required>
                    </label>

                    <label class="field">
                        <span>종료</span>
                        <input id="blockEndAt"
                               type="datetime-local"
                               required>
                    </label>
                </div>

                <label class="inline-check editor-check">
                    <input id="blockAllDay" type="checkbox">
                    하루 전체 일정
                </label>

                <label class="field">
                    <span>메모</span>
                    <textarea id="blockMemo"
                              maxlength="500"
                              rows="3"></textarea>
                </label>

                <div class="modal-actions">
                    <button type="submit" class="primary-button">
                        저장
                    </button>
                    <button id="resetBlockForm"
                            type="button"
                            class="secondary-button">
                        새 일정
                    </button>
                </div>
            </form>

            <h3>등록된 개인 일정</h3>
            <div class="admin-record-list">
                ${items.length
                    ? items.map(block => blockCard(block)).join("")
                    : `<div class="empty-box">등록된 개인 일정이 없습니다.</div>`}
            </div>
        `;

        document.getElementById("blockForm")
            .addEventListener("submit", async e => {
                e.preventDefault();
                await saveBlock();
            });

        document.getElementById("resetBlockForm")
            .addEventListener("click", resetBlockForm);

        toolContent.querySelectorAll(".edit-block")
            .forEach(button => {
                button.addEventListener("click", () => {
                    const item =
                        items.find(v =>
                            String(v.salonHolidayNo) === button.dataset.no
                        );
                    if (item) fillBlockForm(item);
                });
            });

        toolContent.querySelectorAll(".delete-block")
            .forEach(button => {
                button.addEventListener("click", async () => {
                    if (!confirm("이 개인 일정을 삭제하시겠습니까?")) return;
                    await deleteBlock(Number(button.dataset.no));
                });
            });
    }

    function blockCard(item) {
        return `
            <article class="admin-record-card">
                <div>
                    <strong>${escapeHtml(item.title)}</strong>
                    <div>
                        ${formatDateTime(item.startAt)}
                        ~ ${formatDateTime(item.endAt)}
                    </div>
                    ${item.memo
                        ? `<div>${escapeHtml(item.memo)}</div>`
                        : ""}
                </div>
                <div class="action-group">
                    <button type="button"
                            class="action-button edit-block"
                            data-no="${item.salonHolidayNo}">
                        수정
                    </button>
                    <button type="button"
                            class="action-button delete-block"
                            data-no="${item.salonHolidayNo}">
                        삭제
                    </button>
                </div>
            </article>
        `;
    }

    function fillBlockForm(item) {
        document.getElementById("blockNo").value =
            item.salonHolidayNo;
        document.getElementById("blockTitle").value =
            item.title || "";
        document.getElementById("blockStartAt").value =
            localDateTimeInput(item.startAt);
        document.getElementById("blockEndAt").value =
            localDateTimeInput(item.endAt);
        document.getElementById("blockAllDay").checked =
            item.allDay === true;
        document.getElementById("blockMemo").value =
            item.memo || "";
    }

    function resetBlockForm() {
        document.getElementById("blockForm").reset();
        document.getElementById("blockNo").value = "";
    }

    async function saveBlock() {
        const no = document.getElementById("blockNo").value;
        const payload = {
            title: document.getElementById("blockTitle").value.trim(),
            startAt: toApiDateTime(
                document.getElementById("blockStartAt").value
            ),
            endAt: toApiDateTime(
                document.getElementById("blockEndAt").value
            ),
            allDay: document.getElementById("blockAllDay").checked,
            memo:
                document.getElementById("blockMemo").value.trim() || null
        };

        if (!payload.title || !payload.startAt || !payload.endAt) {
            return showMessage("제목, 시작, 종료 시간을 입력해주세요.", true);
        }

        const response = await fetch(
            no
                ? `/admin/api/availability-blocks/${no}`
                : "/admin/api/availability-blocks",
            {
                method: no ? "PUT" : "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...csrfHeaders()
                },
                body: JSON.stringify(payload)
            }
        );

        const body = await readJson(response);

        if (!response.ok) {
            return showMessage(
                body.message || "개인 일정 저장에 실패했습니다.",
                true
            );
        }

        showMessage("개인 일정이 저장되었습니다.", false);
        await openAvailabilityBlocks();
    }

    async function deleteBlock(no) {
        const response = await fetch(
            `/admin/api/availability-blocks/${no}`,
            {
                method: "DELETE",
                headers: csrfHeaders()
            }
        );

        if (!response.ok) {
            const body = await readJson(response);
            return showMessage(
                body.message || "개인 일정 삭제에 실패했습니다.",
                true
            );
        }

        showMessage("개인 일정이 삭제되었습니다.", false);
        await openAvailabilityBlocks();
    }

    // -----------------------------------------------------------------
    // 공통
    // -----------------------------------------------------------------

    async function ensureServiceMenus() {
        if (serviceMenus.length) return;

        const response =
            await fetch("/api/reservations/service-menus");
        const body = await readJson(response);

        if (!response.ok) {
            throw new Error(body.message || "시술 메뉴 조회에 실패했습니다.");
        }

        serviceMenus = Array.isArray(body) ? body : [];
    }

    function addAction(container, label, handler) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "action-button";
        button.textContent = label;
        button.addEventListener("click", handler);
        container.appendChild(button);
    }

    function closeDetail() {
        detailOverlay?.classList.add("hidden");
    }

    function closeTool() {
        toolOverlay?.classList.add("hidden");
        toolContent.innerHTML = "";
        phoneSelectedTime = null;
    }

    async function withScrollPreserved(task) {
        const y = window.scrollY;
        try {
            await task();
        } catch (error) {
            showMessage(error.message, true);
        } finally {
            restoreScroll(y);
        }
    }

    function restoreScroll(y) {
        requestAnimationFrame(() => {
            window.scrollTo({
                top: y,
                left: 0,
                behavior: "instant"
            });
        });
    }

    function buildParams() {
        const params = new URLSearchParams();

        put(params, "status", value("status"));
        put(params, "customerType", value("customerType"));
        put(params, "guestName", value("guestName"));
        put(params, "guestPhone", value("guestPhone"));

        const startFrom = value("startFrom");
        const startTo = value("startTo");

        if (startFrom) {
            params.set("startFrom", `${startFrom}T00:00:00`);
        }

        if (startTo) {
            const d = new Date(`${startTo}T00:00:00`);
            d.setDate(d.getDate() + 1);
            const date =
                `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
            params.set("startTo", `${date}T00:00:00`);
        }

        params.set("page", String(currentPage));
        params.set("size", String(pageSize));

        return params;
    }

    function value(id) {
        return document.getElementById(id)?.value.trim() || "";
    }

    function put(params, key, val) {
        if (val) params.set(key, val);
    }

    function statusText(status) {
        return {
            REQUESTED: "신청",
            CONFIRMED: "확정",
            COMPLETED: "완료",
            CANCELED: "취소",
            NO_SHOW: "노쇼"
        }[status] || status || "-";
    }

    function sourceText(source) {
        return {
            ONLINE: "온라인",
            PHONE: "전화 예약"
        }[source] || source || "-";
    }

    function dayName(day) {
        return {
            1: "월요일",
            2: "화요일",
            3: "수요일",
            4: "목요일",
            5: "금요일",
            6: "토요일",
            7: "일요일"
        }[day] || `${day}요일`;
    }

    function holidayTypeText(type) {
        return {
            TEMPORARY: "임시 휴일",
            VACATION: "휴가",
            PERSONAL: "개인 일정",
            ETC: "기타"
        }[type] || type;
    }

    function formatDateTime(value) {
        return value
            ? String(value).replace("T", " ").slice(0, 16)
            : "-";
    }

    function formatTime(value) {
        return value ? String(value).slice(11, 16) : "";
    }

    function timeValue(value) {
        return value ? String(value).slice(0, 5) : "";
    }

    function todayString() {
        const d = new Date();
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
    }

    function localDateTimeInput(value) {
        return value ? String(value).slice(0, 16) : "";
    }

    function toApiDateTime(value) {
        if (!value) return null;
        return value.length === 16
            ? `${value}:00`
            : value;
    }

    function csrfHeaders() {
        const token =
            document.querySelector('meta[name="_csrf"]')?.content;
        const header =
            document.querySelector('meta[name="_csrf_header"]')?.content;

        return token && header
            ? { [header]: token }
            : {};
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
        div.textContent =
            value == null ? "" : String(value);
        return div.innerHTML;
    }

    function escapeAttribute(value) {
        return escapeHtml(value).replace(/"/g, "&quot;");
    }
})();
