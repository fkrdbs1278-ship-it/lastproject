// 관리자 전체 예약 현황 화면 기능
document.addEventListener("DOMContentLoaded", () => {

    // =====================================================
    // 화면 요소
    // =====================================================

    const reservationItems =
        Array.from(document.querySelectorAll(".reservation-item"));

    const detailPanel =
        document.getElementById("reservationDetailPanel");

    const detailOverlay =
        document.getElementById("reservationDetailOverlay");

    const detailCloseButton =
        document.getElementById("detailCloseButton");


    // 상세 정보
    const detailCustomerName =
        document.getElementById("detailCustomerName");

    const detailCustomerPhone =
        document.getElementById("detailCustomerPhone");

    const detailDate =
        document.getElementById("detailDate");

    const detailTime =
        document.getElementById("detailTime");

    const detailService =
        document.getElementById("detailService");

    const detailDuration =
        document.getElementById("detailDuration");

    const detailStatus =
        document.getElementById("detailStatus");

    const detailRequest =
        document.getElementById("detailRequest");

    const customerAvatar =
        document.querySelector(".customer-avatar");


    // 검색 / 상태 필터
    const reservationSearch =
        document.getElementById("reservationSearch");

    const reservationStatusFilter =
        document.getElementById("reservationStatusFilter");


    // 주간 이동
    const previousWeekButton =
        document.getElementById("previousWeekButton");

    const todayButton =
        document.getElementById("todayButton");

    const nextWeekButton =
        document.getElementById("nextWeekButton");

    const calendarDateRange =
        document.getElementById("calendarDateRange");

    const dayHeadings =
        Array.from(document.querySelectorAll(".day-heading"));


    // 월 / 주 / 목록 버튼
    const viewButtons =
        Array.from(document.querySelectorAll(".calendar-view-button"));


    // =====================================================
    // 날짜 관련 기본값
    // =====================================================

    const today = new Date();

    today.setHours(0, 0, 0, 0);

    // 현재 보고 있는 주의 월요일
    let currentWeekStart = getMonday(today);


    // =====================================================
    // 예약 상세 패널
    // =====================================================

    reservationItems.forEach((reservationItem) => {

        reservationItem.addEventListener("click", () => {

            openReservationDetail(reservationItem);

        });

    });


    // 예약 상세 열기
    function openReservationDetail(reservationItem) {

        const customer =
            reservationItem.dataset.customer || "-";

        const phone =
            reservationItem.dataset.phone || "-";

        const date =
            reservationItem.dataset.date || "-";

        const time =
            reservationItem.dataset.time || "-";

        const service =
            reservationItem.dataset.service || "-";

        const duration =
            reservationItem.dataset.duration || "-";

        const status =
            reservationItem.dataset.status || "-";

        const request =
            reservationItem.dataset.request || "요청사항이 없습니다.";


        detailCustomerName.textContent = customer;
        detailCustomerPhone.textContent = phone;

        detailDate.textContent = date;
        detailTime.textContent = time;

        detailService.textContent = service;
        detailDuration.textContent = duration;

        detailStatus.textContent = status;
        detailRequest.textContent = request;


        // 고객 이름 첫 글자를 프로필 영역에 표시
        customerAvatar.textContent =
            customer !== "-"
                ? customer.substring(0, 1)
                : "-";


        detailPanel.classList.add("open");
        detailOverlay.classList.add("open");

        detailPanel.setAttribute(
            "aria-hidden",
            "false"
        );

    }


    // 예약 상세 닫기
    function closeReservationDetail() {

        detailPanel.classList.remove("open");
        detailOverlay.classList.remove("open");

        detailPanel.setAttribute(
            "aria-hidden",
            "true"
        );

    }


    detailCloseButton.addEventListener(
        "click",
        closeReservationDetail
    );


    detailOverlay.addEventListener(
        "click",
        closeReservationDetail
    );


    // ESC 키로 상세 패널 닫기
    document.addEventListener("keydown", (event) => {

        if (event.key === "Escape") {

            closeReservationDetail();

        }

    });


    // =====================================================
    // 고객명 검색
    // =====================================================

    reservationSearch.addEventListener(
        "input",
        applyReservationFilter
    );


    // =====================================================
    // 예약 상태 필터
    // =====================================================

    reservationStatusFilter.addEventListener(
        "change",
        applyReservationFilter
    );


    // 검색 + 상태 + 현재 주를 함께 적용
    function applyReservationFilter() {

        const keyword =
            reservationSearch.value
                .trim()
                .toLowerCase();

        const selectedStatus =
            reservationStatusFilter.value;


        reservationItems.forEach((reservationItem) => {

            const customer =
                (
                    reservationItem.dataset.customer || ""
                ).toLowerCase();

            const status =
                reservationItem.dataset.status || "";

            const reservationDate =
                parseReservationDate(
                    reservationItem.dataset.date
                );


            // 고객명 검색
            const matchesKeyword =
                keyword === ""
                || customer.includes(keyword);


            // 예약 상태
            const matchesStatus =
                selectedStatus === "ALL"
                || status === getStatusLabel(selectedStatus);


            // 현재 선택된 주인지 확인
            const matchesWeek =
                isDateInCurrentWeek(reservationDate);


            if (
                matchesKeyword
                && matchesStatus
                && matchesWeek
            ) {

                reservationItem.style.display = "flex";

            } else {

                reservationItem.style.display = "none";

            }

        });

    }


    // DB 상태 코드 → 화면 한글 상태
    function getStatusLabel(statusCode) {

        const statusMap = {

            REQUESTED: "예약 요청",
            CONFIRMED: "예약 확정",
            COMPLETED: "시술 완료",
            CANCELED: "예약 취소",
            NO_SHOW: "노쇼"

        };

        return statusMap[statusCode] || "";

    }


    // =====================================================
    // 이전 주
    // =====================================================

    previousWeekButton.addEventListener("click", () => {

        currentWeekStart =
            addDays(currentWeekStart, -7);

        updateWeekCalendar();

    });


    // =====================================================
    // 다음 주
    // =====================================================

    nextWeekButton.addEventListener("click", () => {

        currentWeekStart =
            addDays(currentWeekStart, 7);

        updateWeekCalendar();

    });


    // =====================================================
    // 오늘
    // =====================================================

    todayButton.addEventListener("click", () => {

        currentWeekStart =
            getMonday(today);

        updateWeekCalendar();

    });


    // =====================================================
    // 주간 화면 갱신
    // =====================================================

    function updateWeekCalendar() {

        const weekEnd =
            addDays(currentWeekStart, 6);


        // 상단 날짜 범위
        calendarDateRange.textContent =
            formatWeekRange(
                currentWeekStart,
                weekEnd
            );


        // 요일 날짜 변경
        dayHeadings.forEach(
            (dayHeading, index) => {

                const headingDate =
                    addDays(
                        currentWeekStart,
                        index
                    );

                const dateNumber =
                    dayHeading.querySelector("strong");

                const countText =
                    dayHeading.querySelector("small");


                dateNumber.textContent =
                    headingDate.getDate();


                // 오늘 표시
                dayHeading.classList.toggle(
                    "today",
                    isSameDate(
                        headingDate,
                        today
                    )
                );


                // 일요일은 휴무
                if (index === 6) {

                    countText.textContent =
                        "휴무";

                    return;

                }


                // 해당 날짜 예약 개수
                const reservationCount =
                    reservationItems.filter(
                        (reservationItem) => {

                            const reservationDate =
                                parseReservationDate(
                                    reservationItem.dataset.date
                                );

                            return isSameDate(
                                reservationDate,
                                headingDate
                            );

                        }
                    ).length;


                countText.textContent =
                    `${reservationCount}건`;

            }
        );


        // 현재 주에 맞는 예약만 표시
        applyReservationFilter();

    }


    // =====================================================
    // 날짜 계산
    // =====================================================

    // 해당 날짜가 속한 주의 월요일
    function getMonday(date) {

        const result =
            new Date(date);

        const day =
            result.getDay();

        // 일요일은 6일 전,
        // 월요일은 그대로
        const difference =
            day === 0
                ? -6
                : 1 - day;

        result.setDate(
            result.getDate() + difference
        );

        result.setHours(
            0,
            0,
            0,
            0
        );

        return result;

    }


    // 날짜에 일수 더하기
    function addDays(date, days) {

        const result =
            new Date(date);

        result.setDate(
            result.getDate() + days
        );

        return result;

    }


    // 2026.09.08 형식을 Date로 변환
    function parseReservationDate(dateText) {

        if (!dateText) {
            return null;
        }

        const parts =
            dateText.split(".");

        if (parts.length !== 3) {
            return null;
        }

        const year =
            Number(parts[0]);

        const month =
            Number(parts[1]) - 1;

        const day =
            Number(parts[2]);

        const result =
            new Date(
                year,
                month,
                day
            );

        result.setHours(
            0,
            0,
            0,
            0
        );

        return result;

    }


    // 같은 날짜인지 확인
    function isSameDate(firstDate, secondDate) {

        if (
            !firstDate
            || !secondDate
        ) {

            return false;

        }

        return (
            firstDate.getFullYear()
            === secondDate.getFullYear()

            && firstDate.getMonth()
            === secondDate.getMonth()

            && firstDate.getDate()
            === secondDate.getDate()
        );

    }


    // 예약이 현재 보고 있는 주인지 확인
    function isDateInCurrentWeek(date) {

        if (!date) {
            return false;
        }

        const weekEnd =
            addDays(
                currentWeekStart,
                6
            );

        return (
            date >= currentWeekStart
            && date <= weekEnd
        );

    }


    // 주간 날짜 범위 표시
    function formatWeekRange(
        startDate,
        endDate
    ) {

        const startYear =
            startDate.getFullYear();

        const startMonth =
            startDate.getMonth() + 1;

        const startDay =
            startDate.getDate();


        const endYear =
            endDate.getFullYear();

        const endMonth =
            endDate.getMonth() + 1;

        const endDay =
            endDate.getDate();


        // 같은 년도 + 같은 월
        if (
            startYear === endYear
            && startMonth === endMonth
        ) {

            return (
                `${startYear}년 `
                + `${startMonth}월 `
                + `${startDay}일 ~ `
                + `${endMonth}월 `
                + `${endDay}일`
            );

        }


        // 같은 년도지만 월이 바뀌는 경우
        if (startYear === endYear) {

            return (
                `${startYear}년 `
                + `${startMonth}월 `
                + `${startDay}일 ~ `
                + `${endMonth}월 `
                + `${endDay}일`
            );

        }


        // 년도까지 바뀌는 경우
        return (
            `${startYear}년 `
            + `${startMonth}월 `
            + `${startDay}일 ~ `
            + `${endYear}년 `
            + `${endMonth}월 `
            + `${endDay}일`
        );

    }


    // =====================================================
    // 월 / 주 / 목록
    // =====================================================

    /*
        현재 HTML에는 주간 달력만 만들어져 있다.

        월간 보기와 목록 보기는
        다음 단계에서 각각 화면 영역을 추가한 뒤 연결한다.

        지금은 주간 보기만 사용할 수 있도록 한다.
    */

    viewButtons.forEach((button) => {

        const view =
            button.dataset.view;

        if (view !== "week") {

            button.disabled = true;

            button.title =
                "월간/목록 보기는 다음 단계에서 연결합니다.";

        }

    });


    // =====================================================
    // 최초 화면 표시
    // =====================================================

    updateWeekCalendar();

});