(() => {
    const form = document.getElementById("searchForm");
    if (!form) return;

    const tbody = document.getElementById("reservationTableBody");
    const pageInfo = document.getElementById("pageInfo");
    const prevButton = document.getElementById("prevPage");
    const nextButton = document.getElementById("nextPage");
    const resetButton = document.getElementById("resetSearch");
    const messageBox = document.getElementById("messageBox");

    let currentPage = 0;
    const pageSize = 20;
    let totalPages = 1;

    form.addEventListener("submit", event => {
        event.preventDefault();
        currentPage = 0;
        loadReservations();
    });

    resetButton.addEventListener("click", () => {
        form.reset();
        currentPage = 0;
        loadReservations();
    });

    prevButton.addEventListener("click", () => {
        if (currentPage <= 0) return;
        currentPage -= 1;
        loadReservations();
    });

    nextButton.addEventListener("click", () => {
        if (currentPage + 1 >= totalPages) return;
        currentPage += 1;
        loadReservations();
    });

    loadReservations();

    async function loadReservations() {
        tbody.innerHTML =
            `<tr><td colspan="7" class="loading-box">예약을 불러오는 중입니다.</td></tr>`;

        try {
            const params = buildSearchParams();

            const response = await fetch(
                `/api/admin/reservations?${params.toString()}`
            );

            const body = await readJson(response);

            if (!response.ok) {
                throw new Error(body.message || "예약 검색에 실패했습니다.");
            }

            totalPages = Math.max(body.totalPages || 0, 1);
            currentPage = body.page || 0;

            renderTable(body.content || []);

            pageInfo.textContent =
                `${currentPage + 1} / ${totalPages}`;

            prevButton.disabled = currentPage <= 0;
            nextButton.disabled =
                currentPage + 1 >= totalPages;
        } catch (error) {
            tbody.innerHTML =
                `<tr><td colspan="7" class="empty-box">${escapeHtml(error.message)}</td></tr>`;
        }
    }

    function buildSearchParams() {
        const params = new URLSearchParams();

        putIfValue(params, "status", valueOf("status"));
        putIfValue(params, "customerType", valueOf("customerType"));
        putIfValue(params, "reservationSource", valueOf("reservationSource"));
        putIfValue(params, "guestName", valueOf("guestName"));
        putIfValue(params, "guestPhone", valueOf("guestPhone"));

        const startFrom = valueOf("startFrom");
        const startTo = valueOf("startTo");

        if (startFrom) {
            params.set("startFrom", `${startFrom}T00:00:00`);
        }

        if (startTo) {
            const next = new Date(`${startTo}T00:00:00`);
            next.setDate(next.getDate() + 1);

            const yyyy = next.getFullYear();
            const mm = String(next.getMonth() + 1).padStart(2, "0");
            const dd = String(next.getDate()).padStart(2, "0");

            params.set(
                "startTo",
                `${yyyy}-${mm}-${dd}T00:00:00`
            );
        }

        params.set("page", String(currentPage));
        params.set("size", String(pageSize));

        return params;
    }

    function renderTable(reservations) {
        tbody.innerHTML = "";

        if (!reservations.length) {
            tbody.innerHTML =
                `<tr><td colspan="7" class="empty-box">검색된 예약이 없습니다.</td></tr>`;
            return;
        }

        reservations.forEach(reservation => {
            const tr = document.createElement("tr");

            const customer =
                reservation.customerType === "MEMBER"
                    ? `회원 #${reservation.memberNo}`
                    : `${escapeHtml(reservation.guestName || "")}<br>${escapeHtml(reservation.guestPhone || "")}`;

            tr.innerHTML = `
                <td>${reservation.reservationNo}</td>
                <td>${customer}</td>
                <td>${escapeHtml(reservation.serviceName)}</td>
                <td>${formatDateTime(reservation.startAt)}</td>
                <td>
                    <span class="status-badge status-${reservation.status}">
                        ${statusText(reservation.status)}
                    </span>
                </td>
                <td>${reservation.reservationSource}</td>
                <td>
                    <div class="action-group"
                         data-reservation-no="${reservation.reservationNo}"
                         data-status="${reservation.status}">
                    </div>
                </td>
            `;

            tbody.appendChild(tr);

            renderActions(
                tr.querySelector(".action-group"),
                reservation
            );
        });
    }

    function renderActions(container, reservation) {
        if (reservation.status === "REQUESTED") {
            addAction(container, "확정", "confirm");
            addAction(container, "취소", "cancel");
        }

        if (reservation.status === "CONFIRMED") {
            addAction(container, "완료", "complete");
            addAction(container, "노쇼", "no-show");
            addAction(container, "취소", "cancel");
        }
    }

    function addAction(container, label, action) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "action-button";
        button.textContent = label;

        button.addEventListener("click", () => {
            handleAction(
                Number(container.dataset.reservationNo),
                action
            );
        });

        container.appendChild(button);
    }

    async function handleAction(reservationNo, action) {
        try {
            let url =
                `/api/admin/reservations/${reservationNo}/${action}`;

            if (action === "cancel") {
                const reason = prompt("관리자 취소 사유를 입력해주세요.");
                if (reason === null) return;

                if (!reason.trim()) {
                    showMessage("취소 사유를 입력해주세요.", true);
                    return;
                }

                url += `?${new URLSearchParams({
                    reason: reason.trim()
                }).toString()}`;
            }

            if (action === "no-show") {
                const reason = prompt(
                    "노쇼 사유를 입력해주세요.",
                    "예약시간 미방문"
                );

                if (reason === null) return;

                const adminMemo = prompt(
                    "관리자 메모를 입력해주세요.",
                    ""
                );

                if (adminMemo === null) return;

                url += `?${new URLSearchParams({
                    reason: reason.trim(),
                    adminMemo: adminMemo.trim()
                }).toString()}`;
            }

            const response = await fetch(url, {
                method: "POST",
                headers: csrfHeaders()
            });

            const body = await readJson(response);

            if (!response.ok) {
                throw new Error(
                    body.message || "예약 상태 변경에 실패했습니다."
                );
            }

            showMessage("처리가 완료되었습니다.", false);
            await loadReservations();
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    function statusText(status) {
        return {
            REQUESTED: "신청",
            CONFIRMED: "확정",
            COMPLETED: "완료",
            CANCELED: "취소",
            NO_SHOW: "노쇼"
        }[status] || status;
    }

    function formatDateTime(value) {
        if (!value) return "";
        return String(value).replace("T", " ").slice(0, 16);
    }

    function valueOf(id) {
        return document.getElementById(id).value.trim();
    }

    function putIfValue(params, key, value) {
        if (value) params.set(key, value);
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
