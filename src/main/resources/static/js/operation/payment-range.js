document.addEventListener(
    "DOMContentLoaded",
    function () {

        // ========================================
        // 날짜 조회 영역
        // ========================================

        const form =
            document.querySelector(
                ".payment-period-form"
            )
            ||
            document.querySelector(
                ".payment-chart-controls form"
            );


        if (form) {

            const startDateInput =
                form.querySelector(
                    'input[name="startDate"]'
                )
                ||
                form.querySelector(
                    'input[type="date"]:first-of-type'
                );


            const endDateInput =
                form.querySelector(
                    'input[name="endDate"]'
                )
                ||
                form.querySelectorAll(
                    'input[type="date"]'
                )[1];


            let unitInput =
                form.querySelector(
                    'input[name="unit"]'
                );


            /*
             * unit hidden input이 없는 경우
             * 자동 생성합니다.
             */
            if (!unitInput) {

                unitInput =
                    document.createElement(
                        "input"
                    );

                unitInput.type =
                    "hidden";

                unitInput.name =
                    "unit";

                unitInput.value =
                    getCurrentUnit();

                form.appendChild(
                    unitInput
                );
            }


            /*
             * 시작 날짜 변경 기준
             *
             * 예:
             * 시작일 9/10 선택
             * → 종료일 9/16 자동 설정
             */
            if (startDateInput) {

                startDateInput.addEventListener(
                    "change",
                    function () {

                        if (
                            getCurrentUnit()
                            !== "DAY"
                        ) {
                            return;
                        }


                        if (
                            !startDateInput.value
                        ) {
                            return;
                        }


                        endDateInput.value =
                            addDays(
                                startDateInput.value,
                                6
                            );
                    }
                );
            }


            /*
             * 종료 날짜 변경 기준
             *
             * 예:
             * 종료일 9/19 선택
             * → 시작일 9/13 자동 설정
             */
            if (endDateInput) {

                endDateInput.addEventListener(
                    "change",
                    function () {

                        if (
                            getCurrentUnit()
                            !== "DAY"
                        ) {
                            return;
                        }


                        if (
                            !endDateInput.value
                        ) {
                            return;
                        }


                        startDateInput.value =
                            addDays(
                                endDateInput.value,
                                -6
                            );
                    }
                );
            }


            /*
             * 조회 버튼을 눌렀을 때도
             * 혹시 날짜가 어긋나 있으면
             * DAY 기준 7일로 한 번 더 맞춥니다.
             */
            form.addEventListener(
                "submit",
                function () {

                    unitInput.value =
                        getCurrentUnit();


                    if (
                        getCurrentUnit()
                        !== "DAY"
                    ) {
                        return;
                    }


                    if (
                        !startDateInput.value
                        || !endDateInput.value
                    ) {
                        return;
                    }


                    const start =
                        parseDate(
                            startDateInput.value
                        );

                    const end =
                        parseDate(
                            endDateInput.value
                        );


                    const dayDifference =
                        Math.round(
                            (
                                end.getTime()
                                - start.getTime()
                            )
                            /
                            (
                                1000
                                * 60
                                * 60
                                * 24
                            )
                        );


                    /*
                     * 시작일부터 종료일까지
                     * 포함해서 7일이어야 하므로
                     * 날짜 차이는 6일입니다.
                     */
                    if (dayDifference !== 6) {

                        startDateInput.value =
                            addDays(
                                endDateInput.value,
                                -6
                            );
                    }
                }
            );
        }


        // ========================================
        // 일별 / 월별 / 연도별 버튼
        // ========================================

        const periodButtons =
            document.querySelectorAll(
                ".period-tab"
            );


        periodButtons.forEach(
            function (button) {

                button.addEventListener(
                    "click",
                    function () {

                        /*
                         * 기존 코드에서
                         * 버튼 동작을 하고 있을 수 있으므로
                         * 여기서는 active 상태만 참고합니다.
                         */

                        setTimeout(
                            function () {

                                const form =
                                    document.querySelector(
                                        ".payment-period-form"
                                    )
                                    ||
                                    document.querySelector(
                                        ".payment-chart-controls form"
                                    );


                                if (!form) {
                                    return;
                                }


                                const unitInput =
                                    form.querySelector(
                                        'input[name="unit"]'
                                    );


                                if (unitInput) {

                                    unitInput.value =
                                        getCurrentUnit();
                                }


                                /*
                                 * 일별로 다시 선택했을 때
                                 * 현재 종료일을 기준으로
                                 * 최근 7일 맞추기
                                 */
                                if (
                                    getCurrentUnit()
                                    === "DAY"
                                ) {

                                    const startInput =
                                        form.querySelector(
                                            'input[name="startDate"]'
                                        );


                                    const endInput =
                                        form.querySelector(
                                            'input[name="endDate"]'
                                        );


                                    if (
                                        startInput
                                        && endInput
                                        && endInput.value
                                    ) {

                                        startInput.value =
                                            addDays(
                                                endInput.value,
                                                -6
                                            );
                                    }
                                }

                            },
                            0
                        );
                    }
                );
            }
        );


        // ========================================
        // 그래프 날짜와 동그라미 위치 맞추기
        // ========================================

        /*
         * 기존 payment.js가 그래프를 그린 뒤
         * 실행될 수 있도록 한 프레임 뒤에 맞춥니다.
         */
        requestAnimationFrame(
            function () {

                alignChartLabels();

                requestAnimationFrame(
                    alignChartLabels
                );
            }
        );


        /*
         * 그래프가 JS로 다시 그려지는 경우에도
         * 날짜 위치를 다시 맞춥니다.
         */
        const chartArea =
            document.querySelector(
                ".payment-area-chart"
            );


        if (chartArea) {

            const observer =
                new MutationObserver(
                    function () {

                        requestAnimationFrame(
                            alignChartLabels
                        );
                    }
                );


            observer.observe(
                chartArea,
                {
                    childList: true,
                    subtree: true
                }
            );
        }
    }
);


/**
 * 현재 선택된 조회 단위
 */
function getCurrentUnit() {

    const activeButton =
        document.querySelector(
            ".period-tab.active"
        );


    if (!activeButton) {

        const unitInput =
            document.querySelector(
                'input[name="unit"]'
            );

        return unitInput
            ? unitInput.value
            : "DAY";
    }


    /*
     * data-unit이 있으면 우선 사용
     */
    if (
        activeButton.dataset.unit
    ) {

        return activeButton
            .dataset.unit
            .toUpperCase();
    }


    /*
     * data-unit이 없는 기존 HTML도 대응
     */
    const text =
        activeButton
            .textContent
            .trim();


    if (text === "월별") {
        return "MONTH";
    }


    if (text === "연도별") {
        return "YEAR";
    }


    return "DAY";
}


/**
 * yyyy-MM-dd 문자열에 날짜 더하기
 */
function addDays(
    dateString,
    days
) {

    const date =
        parseDate(
            dateString
        );


    date.setDate(
        date.getDate()
        + days
    );


    return formatDate(
        date
    );
}


/**
 * yyyy-MM-dd → Date
 */
function parseDate(
    dateString
) {

    const parts =
        dateString
            .split("-")
            .map(Number);


    return new Date(
        parts[0],
        parts[1] - 1,
        parts[2]
    );
}


/**
 * Date → yyyy-MM-dd
 */
function formatDate(
    date
) {

    const year =
        date.getFullYear();


    const month =
        String(
            date.getMonth() + 1
        )
            .padStart(
                2,
                "0"
            );


    const day =
        String(
            date.getDate()
        )
            .padStart(
                2,
                "0"
            );


    return (
        year
        + "-"
        + month
        + "-"
        + day
    );
}


/**
 * 그래프 동그라미와 날짜 X축 위치를
 * 같은 좌표로 맞춥니다.
 */
function alignChartLabels() {

    const svg =
        document.querySelector(
            ".payment-area-chart svg"
        );


    const xAxis =
        document.querySelector(
            ".chart-x-axis"
        );


    if (
        !svg
        || !xAxis
    ) {
        return;
    }


    const circles =
        Array.from(
            svg.querySelectorAll(
                ".payment-chart-points circle"
            )
        );


    const labels =
        Array.from(
            xAxis.children
        );


    if (
        circles.length === 0
        || labels.length === 0
    ) {
        return;
    }


    /*
     * 일별 7일 기준에서는
     * 점 7개 / 날짜 7개가 됩니다.
     */
    if (
        circles.length
        !== labels.length
    ) {
        return;
    }


    const viewBox =
        svg.viewBox.baseVal;


    if (
        !viewBox
        || viewBox.width === 0
    ) {
        return;
    }


    /*
     * 기존 grid/flex 위치 계산을 끄고
     * SVG의 실제 동그라미 좌표를 기준으로
     * 날짜를 배치합니다.
     */
    xAxis.style.position =
        "relative";

    xAxis.style.display =
        "block";

    xAxis.style.height =
        "28px";


    labels.forEach(
        function (
            label,
            index
        ) {

            const circle =
                circles[index];


            const circleX =
                Number(
                    circle.getAttribute(
                        "cx"
                    )
                );


            const leftPercent =
                (
                    circleX
                    / viewBox.width
                )
                * 100;


            label.style.position =
                "absolute";

            label.style.left =
                leftPercent + "%";

            label.style.top =
                "0";

            label.style.transform =
                "translateX(-50%)";

            label.style.whiteSpace =
                "nowrap";

            label.style.textAlign =
                "center";
        }
    );
}