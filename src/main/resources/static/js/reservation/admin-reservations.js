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

    let currentPage = 0;
    const pageSize = 20;
    let totalPages = 1;

    form.addEventListener("submit", e => {
        e.preventDefault();
        currentPage = 0;
        loadReservations();
    });

    resetButton.addEventListener("click", () => {
        form.reset();
        currentPage = 0;
        loadReservations();
    });

    prevButton.addEventListener("click", () => {
        if (currentPage > 0) {
            currentPage--;
            loadReservations();
        }
    });

    nextButton.addEventListener("click", () => {
        if (currentPage + 1 < totalPages) {
            currentPage++;
            loadReservations();
        }
    });

    document.getElementById("closeDetail")
        .addEventListener("click", () =>
            detailOverlay.classList.add("hidden")
        );

    detailOverlay.addEventListener("click", e => {
        if (e.target === detailOverlay) {
            detailOverlay.classList.add("hidden");
        }
    });

    loadReservations();

    async function loadReservations() {
        tbody.innerHTML =
            `<tr><td colspan="7" class="loading-box">예약을 불러오는 중입니다.</td></tr>`;

        try {
            const response = await fetch(
                `/api/admin/reservations?${buildParams()}`
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
                <td><span class="status-badge status-${r.status}">${statusText(r.status)}</span></td>
                <td>${memo}</td>
                <td>
                    <div class="action-group" data-no="${r.reservationNo}">
                        <button type="button" class="action-button detail-button">상세</button>
                    </div>
                </td>
            `;

            tbody.appendChild(tr);

            const actions = tr.querySelector(".action-group");
            actions.querySelector(".detail-button")
                .addEventListener("click", () =>
                    openDetail(r.reservationNo)
                );

            if (r.status === "REQUESTED") {
                addAction(actions, "확정", () => changeStatus(r.reservationNo, "confirm"));
                addAction(actions, "취소", () => cancelAdmin(r.reservationNo));
            }

            if (r.status === "CONFIRMED") {
                addAction(actions, "완료", () => changeStatus(r.reservationNo, "complete"));
                addAction(actions, "노쇼", () => noShow(r.reservationNo));
                addAction(actions, "취소", () => cancelAdmin(r.reservationNo));
            }
        });
    }

    async function openDetail(reservationNo) {
        try {
            const response = await fetch(
                `/api/admin/reservations/${reservationNo}`
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

            const styleBlock =
                body.hairStyleTitle
                    ? `
                        <div class="detail-block">
                            <strong>선택 예시 스타일</strong><br>
                            ${escapeHtml(body.hairStyleTitle)}
                            ${body.hairStyleImageUrl
                                ? `<div><img class="hair-style-image" src="${escapeAttribute(body.hairStyleImageUrl)}" alt=""></div>`
                                : ""}
                        </div>
                      `
                    : `
                        <div class="detail-block">
                            <strong>선택 예시 스타일</strong><br>선택 안 함
                        </div>
                      `;

            const images =
                (body.images || []).length
                    ? body.images.map(img =>
                        `<img src="${escapeAttribute(img.fileUrl)}"
                              alt="${escapeAttribute(img.originalFileName || "참고 이미지")}">`
                      ).join("")
                    : `<div class="empty-box">첨부된 참고 이미지가 없습니다.</div>`;

            detailContent.innerHTML = `
                <div class="detail-grid">
                    <div class="detail-block"><strong>예약번호</strong><br>${r.reservationNo}</div>
                    <div class="detail-block"><strong>고객</strong><br>${customer}</div>
                    <div class="detail-block"><strong>시술</strong><br>${escapeHtml(r.serviceName)}</div>
                    <div class="detail-block"><strong>예약시간</strong><br>${formatDateTime(r.startAt)}</div>
                    <div class="detail-block"><strong>상태</strong><br>${statusText(r.status)}</div>
                    <div class="detail-block"><strong>요청사항</strong><br>${escapeHtml(r.requestMemo || "없음")}</div>
                    ${styleBlock}
                </div>

                <h3>고객 참고 이미지</h3>
                <div class="detail-images">${images}</div>
            `;

            detailOverlay.classList.remove("hidden");
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    async function changeStatus(reservationNo, action) {
        await postAction(
            `/api/admin/reservations/${reservationNo}/${action}`
        );
    }

    async function cancelAdmin(reservationNo) {
        const reason = prompt("관리자 취소 사유를 입력해주세요.");
        if (reason === null) return;
        if (!reason.trim()) return showMessage("취소 사유를 입력해주세요.", true);

        await postAction(
            `/api/admin/reservations/${reservationNo}/cancel?` +
            new URLSearchParams({ reason: reason.trim() })
        );
    }

    async function noShow(reservationNo) {
        const reason = prompt("노쇼 사유를 입력해주세요.", "예약시간 미방문");
        if (reason === null) return;

        const adminMemo = prompt("관리자 메모를 입력해주세요.", "");
        if (adminMemo === null) return;

        await postAction(
            `/api/admin/reservations/${reservationNo}/no-show?` +
            new URLSearchParams({
                reason: reason.trim(),
                adminMemo: adminMemo.trim()
            })
        );
    }

    async function postAction(url) {
        try {
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
        } catch (error) {
            showMessage(error.message, true);
        }
    }

    function addAction(container, label, handler) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "action-button";
        button.textContent = label;
        button.addEventListener("click", handler);
        container.appendChild(button);
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
                `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,"0")}-${String(d.getDate()).padStart(2,"0")}`;
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

    function escapeAttribute(value) {
        return escapeHtml(value).replace(/"/g, "&quot;");
    }
})();
