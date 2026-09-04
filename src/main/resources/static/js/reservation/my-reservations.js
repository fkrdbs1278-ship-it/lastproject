(() => {
    const page = document.getElementById("myReservationPage");
    if (!page) return;

    const memberNoValue = page.dataset.memberNo;
    const isLoggedIn = page.dataset.loggedIn === "true";
    const memberNo = memberNoValue ? Number(memberNoValue) : null;

    if (!isLoggedIn || !memberNo) {
        return;
    }

    const list = document.getElementById("reservationList");
    const refreshButton = document.getElementById("refreshReservations");
    const messageBox = document.getElementById("messageBox");

    refreshButton.addEventListener("click", loadReservations);

    loadReservations();

    async function loadReservations() {
        list.innerHTML =
            `<div class="loading-box">예약을 불러오는 중입니다.</div>`;

        try {
            const response = await fetch(
                "/api/reservations/me"
            );

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

            const canCancel =
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

                <div>
                    소요시간 ${reservation.durationMinutes}분
                </div>

                ${reservation.requestMemo
                    ? `<div>요청사항: ${escapeHtml(reservation.requestMemo)}</div>`
                    : ""}

                ${canCancel
                    ? `<div>
                        <button type="button"
                                class="secondary-button cancel-button"
                                data-reservation-no="${reservation.reservationNo}">
                            예약 취소
                        </button>
                       </div>`
                    : ""}
            `;

            list.appendChild(item);
        });

        list.querySelectorAll(".cancel-button")
            .forEach(button => {
                button.addEventListener("click", async () => {
                    const reservationNo =
                        Number(button.dataset.reservationNo);

                    const reason = prompt("취소 사유를 입력해주세요.");

                    if (reason === null) return;

                    if (!reason.trim()) {
                        showMessage("취소 사유를 입력해주세요.", true);
                        return;
                    }

                    await cancelReservation(
                        reservationNo,
                        reason.trim()
                    );
                });
            });
    }

    async function cancelReservation(reservationNo, reason) {
        try {
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
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    function statusText(status) {
        return {
            REQUESTED: "예약 신청",
            CONFIRMED: "예약 확정",
            COMPLETED: "시술 완료",
            CANCELED: "예약 취소",
            NO_SHOW: "노쇼"
        }[status] || status;
    }

    function formatDateTime(value) {
        if (!value) return "";
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
        div.textContent = value || "";
        return div.innerHTML;
    }
})();
