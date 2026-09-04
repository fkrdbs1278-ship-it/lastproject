(() => {
    const page = document.getElementById("myReservationPage");
    if (!page) return;

    const isLoggedIn = page.dataset.loggedIn === "true";
    if (!isLoggedIn) return;

    const list = document.getElementById("reservationList");
    const refreshButton = document.getElementById("refreshReservations");
    const messageBox = document.getElementById("messageBox");

    const detailOverlay = document.getElementById("memberDetailOverlay");
    const detailContent = document.getElementById("memberDetailContent");
    const closeDetailButton = document.getElementById("closeMemberDetail");

    const editOverlay = document.getElementById("memberEditOverlay");
    const editForm = document.getElementById("memberEditForm");
    const closeEditButton = document.getElementById("closeMemberEdit");
    const cancelEditButton = document.getElementById("cancelMemberEdit");
    const editReservationNo = document.getElementById("editReservationNo");
    const editServiceMenu = document.getElementById("editServiceMenu");
    const editDate = document.getElementById("editDate");
    const editTimeSlots = document.getElementById("editTimeSlots");
    const editRequestMemo = document.getElementById("editRequestMemo");

    let serviceMenus = [];
    let editingDetail = null;
    let selectedEditTime = null;

    refreshButton?.addEventListener("click", () =>
        withScrollPreserved(loadReservations)
    );

    closeDetailButton?.addEventListener("click", closeDetail);
    closeEditButton?.addEventListener("click", closeEdit);
    cancelEditButton?.addEventListener("click", closeEdit);

    detailOverlay?.addEventListener("click", e => {
        if (e.target === detailOverlay) closeDetail();
    });

    editOverlay?.addEventListener("click", e => {
        if (e.target === editOverlay) closeEdit();
    });

    editServiceMenu?.addEventListener("change", () => {
        selectedEditTime = null;
        loadEditTimes();
    });

    editDate?.addEventListener("change", () => {
        selectedEditTime = null;
        loadEditTimes();
    });

    editForm?.addEventListener("submit", async e => {
        e.preventDefault();
        await saveEdit();
    });

    loadReservations();

    async function loadReservations() {
        list.innerHTML =
            `<div class="loading-box">예약을 불러오는 중입니다.</div>`;

        try {
            const response = await fetch("/api/reservations/me");
            const body = await readJson(response);

            if (!response.ok) {
                throw new Error(body.message || "예약 조회에 실패했습니다.");
            }

            renderReservations(body);
        } catch (error) {
            list.innerHTML =
                `<div class="empty-box">${escapeHtml(error.message)}</div>`;
        }
    }

    function renderReservations(reservations) {
        list.innerHTML = "";

        if (!Array.isArray(reservations) || reservations.length === 0) {
            list.innerHTML =
                `<div class="empty-box">등록된 예약이 없습니다.</div>`;
            return;
        }

        reservations.forEach(reservation => {
            const item = document.createElement("article");
            item.className = "reservation-item";
            item.dataset.reservationNo = reservation.reservationNo;

            const modifiable =
                reservation.modifiable === true ||
                reservation.status === "REQUESTED" ||
                reservation.status === "CONFIRMED";

            const cancelable =
                reservation.cancelable === true ||
                reservation.status === "REQUESTED" ||
                reservation.status === "CONFIRMED";

            item.innerHTML = `
                <div class="reservation-item-header">
                    <div>
                        <strong>${escapeHtml(reservation.serviceName)}</strong>
                        <div>#${reservation.reservationNo}</div>
                    </div>
                    <span class="status-badge status-${reservation.status}">
                        ${statusText(reservation.status)}
                    </span>
                </div>

                <div>
                    ${formatDateTime(reservation.startAt)}
                    ~ ${formatTime(reservation.endAt)}
                </div>

                <div>소요시간 ${reservation.durationMinutes}분</div>

                ${reservation.requestMemo
                    ? `<div>요청사항: ${escapeHtml(reservation.requestMemo)}</div>`
                    : ""}

                <div class="reservation-actions">
                    <button type="button"
                            class="secondary-button detail-button">
                        상세 조회
                    </button>

                    ${modifiable
                        ? `<button type="button"
                                   class="secondary-button edit-button">
                                예약 변경
                           </button>`
                        : ""}

                    ${cancelable
                        ? `<button type="button"
                                   class="secondary-button cancel-button">
                                예약 취소
                           </button>`
                        : ""}
                </div>
            `;

            item.querySelector(".detail-button")
                ?.addEventListener("click", () =>
                    openDetail(reservation.reservationNo)
                );

            item.querySelector(".edit-button")
                ?.addEventListener("click", () =>
                    openEdit(reservation.reservationNo)
                );

            item.querySelector(".cancel-button")
                ?.addEventListener("click", async () => {
                    const reason = prompt("취소 사유를 입력해주세요.");
                    if (reason === null) return;

                    if (!reason.trim()) {
                        showMessage("취소 사유를 입력해주세요.", true);
                        return;
                    }

                    await withScrollPreserved(() =>
                        cancelReservation(
                            reservation.reservationNo,
                            reason.trim()
                        )
                    );
                });

            list.appendChild(item);
        });
    }

    async function openDetail(reservationNo) {
        try {
            const response = await fetch(
                `/api/reservations/me/${reservationNo}`
            );
            const body = await readJson(response);

            if (!response.ok) {
                throw new Error(body.message || "예약 상세 조회에 실패했습니다.");
            }

            renderDetail(body);
            detailOverlay.classList.remove("hidden");
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    function renderDetail(body) {
        const r = body.reservation || {};
        const styleBlock = body.hairStyleTitle
            ? `
                <div class="detail-block">
                    <strong>선택 예시 스타일</strong><br>
                    ${escapeHtml(body.hairStyleTitle)}
                    ${body.hairStyleImageUrl
                        ? `<div class="detail-style-image-wrap">
                               <img class="hair-style-image"
                                    src="${escapeAttribute(body.hairStyleImageUrl)}"
                                    alt="${escapeAttribute(body.hairStyleTitle)}">
                           </div>`
                        : ""}
                </div>
              `
            : `
                <div class="detail-block">
                    <strong>선택 예시 스타일</strong><br>
                    선택 안 함
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
                    <strong>예약번호</strong><br>${r.reservationNo ?? "-"}
                </div>
                <div class="detail-block">
                    <strong>시술</strong><br>${escapeHtml(r.serviceName || "-")}
                </div>
                <div class="detail-block">
                    <strong>예약시간</strong><br>
                    ${formatDateTime(r.startAt)} ~ ${formatTime(r.endAt)}
                </div>
                <div class="detail-block">
                    <strong>소요시간</strong><br>${r.durationMinutes ?? "-"}분
                </div>
                <div class="detail-block">
                    <strong>상태</strong><br>${statusText(r.status)}
                </div>
                <div class="detail-block">
                    <strong>예약 경로</strong><br>${sourceText(r.reservationSource)}
                </div>
                <div class="detail-block">
                    <strong>요청사항</strong><br>${escapeHtml(r.requestMemo || "없음")}
                </div>
                <div class="detail-block">
                    <strong>예약 생성</strong><br>${formatDateTime(r.createdAt)}
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

            <h3>내 참고 이미지</h3>
            <p class="field-help">
                이 이미지는 로그인한 예약자 본인 전용 API를 통해 조회됩니다.
            </p>
            <div class="detail-images">${images}</div>
        `;
    }

    async function openEdit(reservationNo) {
        try {
            const detailResponse = await fetch(
                `/api/reservations/me/${reservationNo}`
            );
            const detail = await readJson(detailResponse);

            if (!detailResponse.ok) {
                throw new Error(
                    detail.message || "예약 정보를 불러오지 못했습니다."
                );
            }

            if (detail.reservation?.modifiable === false) {
                throw new Error("현재 상태에서는 예약을 변경할 수 없습니다.");
            }

            await ensureServiceMenus();

            editingDetail = detail;
            selectedEditTime =
                String(detail.reservation.startAt || "").slice(11, 16);

            editReservationNo.value = reservationNo;
            editServiceMenu.innerHTML =
                serviceMenus.map(menu => `
                    <option value="${menu.serviceMenuNo}">
                        ${escapeHtml(menu.name)}
                        (${menu.durationMin}분)
                    </option>
                `).join("");

            editServiceMenu.value =
                String(detail.reservation.serviceMenuNo);

            editDate.value =
                String(detail.reservation.startAt).slice(0, 10);

            editRequestMemo.value =
                detail.reservation.requestMemo || "";

            editOverlay.classList.remove("hidden");
            await loadEditTimes(true);
        } catch (error) {
            showMessage(error.message, true);
        }
    }

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

    async function loadEditTimes(keepCurrent = false) {
        const serviceMenuNo = Number(editServiceMenu.value);
        const date = editDate.value;

        if (!serviceMenuNo || !date) {
            editTimeSlots.innerHTML =
                `<div class="empty-box">시술과 날짜를 선택해주세요.</div>`;
            return;
        }

        editTimeSlots.innerHTML =
            `<div class="loading-box">예약 가능 시간을 불러오는 중입니다.</div>`;

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

            const slots = Array.isArray(body) ? body : [];

            /*
             * 현재 예약 시간을 편집할 때는 자기 자신의 기존 예약 때문에
             * available-times에서 빠질 수 있다.
             * 동일 날짜/동일 시술인 경우 현재 시간을 선택지에 보강한다.
             */
            const currentStart =
                String(editingDetail?.reservation?.startAt || "").slice(11, 16);
            const currentDate =
                String(editingDetail?.reservation?.startAt || "").slice(0, 10);
            const currentService =
                Number(editingDetail?.reservation?.serviceMenuNo);

            if (keepCurrent &&
                currentStart &&
                currentDate === date &&
                currentService === serviceMenuNo &&
                !slots.some(slot => timeValue(slot.startTime) === currentStart)) {
                slots.unshift({
                    startTime: currentStart,
                    endTime: String(editingDetail.reservation.endAt || "").slice(11, 16)
                });
            }

            if (!slots.length) {
                editTimeSlots.innerHTML =
                    `<div class="empty-box">예약 가능한 시간이 없습니다.</div>`;
                selectedEditTime = null;
                return;
            }

            editTimeSlots.innerHTML = "";

            slots.forEach(slot => {
                const start = timeValue(slot.startTime);
                const end = timeValue(slot.endTime);

                const button = document.createElement("button");
                button.type = "button";
                button.className = "time-button";
                button.textContent = `${start} ~ ${end}`;

                if (selectedEditTime === start) {
                    button.classList.add("selected");
                }

                button.addEventListener("click", () => {
                    selectedEditTime = start;
                    editTimeSlots.querySelectorAll(".time-button")
                        .forEach(el => el.classList.remove("selected"));
                    button.classList.add("selected");
                });

                editTimeSlots.appendChild(button);
            });
        } catch (error) {
            editTimeSlots.innerHTML =
                `<div class="empty-box">${escapeHtml(error.message)}</div>`;
        }
    }

    async function saveEdit() {
        const reservationNo = Number(editReservationNo.value);
        const serviceMenuNo = Number(editServiceMenu.value);
        const date = editDate.value;

        if (!reservationNo || !serviceMenuNo || !date || !selectedEditTime) {
            return showMessage("시술, 날짜, 시간을 모두 선택해주세요.", true);
        }

        try {
            const response = await fetch(
                `/api/reservations/me/${reservationNo}`,
                {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        ...csrfHeaders()
                    },
                    body: JSON.stringify({
                        serviceMenuNo,
                        hairStyleNo:
                            editingDetail?.reservation?.hairStyleNo ?? null,
                        startAt: `${date}T${selectedEditTime}:00`,
                        requestMemo:
                            editRequestMemo.value.trim() || null
                    })
                }
            );

            const body = await readJson(response);

            if (!response.ok) {
                throw new Error(body.message || "예약 변경에 실패했습니다.");
            }

            const y = window.scrollY;
            closeEdit();
            showMessage("예약이 변경되었습니다.", false);
            await loadReservations();
            restoreScroll(y);
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    async function cancelReservation(reservationNo, reason) {
        const params = new URLSearchParams({ reason });

        const response = await fetch(
            `/api/reservations/me/${reservationNo}/cancel?${params.toString()}`,
            {
                method: "POST",
                headers: csrfHeaders()
            }
        );

        const body = await readJson(response);

        if (!response.ok) {
            throw new Error(body.message || "예약 취소에 실패했습니다.");
        }

        showMessage("예약이 취소되었습니다.", false);
        await loadReservations();
    }

    function closeDetail() {
        detailOverlay?.classList.add("hidden");
    }

    function closeEdit() {
        editOverlay?.classList.add("hidden");
        editingDetail = null;
        selectedEditTime = null;
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

    function timeValue(value) {
        if (!value) return "";
        return String(value).slice(0, 5);
    }

    function statusText(status) {
        return {
            REQUESTED: "예약 신청",
            CONFIRMED: "예약 확정",
            COMPLETED: "시술 완료",
            CANCELED: "예약 취소",
            NO_SHOW: "노쇼"
        }[status] || status || "-";
    }

    function sourceText(source) {
        return {
            ONLINE: "온라인",
            PHONE: "전화 예약"
        }[source] || source || "-";
    }

    function formatDateTime(value) {
        if (!value) return "-";
        return String(value).replace("T", " ").slice(0, 16);
    }

    function formatTime(value) {
        if (!value) return "";
        return String(value).slice(11, 16);
    }

    function csrfHeaders() {
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector('meta[name="_csrf_header"]')?.content;

        if (!token || !header) return {};
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
        div.textContent = value == null ? "" : String(value);
        return div.innerHTML;
    }

    function escapeAttribute(value) {
        return escapeHtml(value).replace(/"/g, "&quot;");
    }
})();
