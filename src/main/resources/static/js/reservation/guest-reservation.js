(() => {
    const lookupButton = document.getElementById("lookupButton");
    if (!lookupButton) return;

    const result = document.getElementById("lookupResult");
    const messageBox = document.getElementById("messageBox");

    lookupButton.addEventListener("click", lookup);

    async function lookup() {
        const reservationNo =
            Number(document.getElementById("lookupReservationNo").value);
        const guestPhone =
            document.getElementById("lookupGuestPhone").value.trim();

        if (!reservationNo || !/^010-?\d{4}-?\d{4}$/.test(guestPhone)) {
            return showMessage("예약번호와 휴대전화 번호를 확인해주세요.", true);
        }

        const response = await fetch("/api/reservations/guest/lookup", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                ...csrfHeaders()
            },
            body: JSON.stringify({ reservationNo, guestPhone })
        });

        const body = await readJson(response);

        if (!response.ok) {
            result.classList.add("hidden");
            return showMessage(body.message || "예약을 찾을 수 없습니다.", true);
        }

        const canCancel =
            body.status === "REQUESTED" || body.status === "CONFIRMED";

        result.innerHTML = `
            <h2>예약 #${body.reservationNo}</h2>
            <div class="detail-grid">
                <div class="detail-block"><strong>시술</strong><br>${escapeHtml(body.serviceName)}</div>
                <div class="detail-block"><strong>상태</strong><br>${statusText(body.status)}</div>
                <div class="detail-block"><strong>예약시간</strong><br>${formatDateTime(body.startAt)}</div>
                <div class="detail-block"><strong>요청사항</strong><br>${escapeHtml(body.requestMemo || "없음")}</div>
            </div>
            ${canCancel ? `<button id="guestCancelButton" class="secondary-button" type="button">예약 취소</button>` : ""}
        `;

        result.classList.remove("hidden");

        document.getElementById("guestCancelButton")
            ?.addEventListener("click", () =>
                cancelGuest(body.reservationNo, guestPhone)
            );
    }

    async function cancelGuest(reservationNo, guestPhone) {
        const reason = prompt("취소 사유를 입력해주세요.");
        if (reason === null) return;
        if (!reason.trim()) return showMessage("취소 사유를 입력해주세요.", true);

        const response = await fetch("/api/reservations/guest/cancel", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                ...csrfHeaders()
            },
            body: JSON.stringify({
                reservationNo,
                guestPhone,
                reason: reason.trim()
            })
        });

        const body = await readJson(response);

        if (!response.ok) {
            return showMessage(body.message || "취소 실패", true);
        }

        showMessage("예약이 취소되었습니다.", false);
        await lookup();
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
        return value ? String(value).replace("T", " ").slice(0, 16) : "";
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
})();
