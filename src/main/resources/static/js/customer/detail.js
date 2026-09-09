document.addEventListener(
    "DOMContentLoaded",
    function () {


        // =====================================================
        // 1. 전화번호 화면 표시 자동 포맷
        // =====================================================
        //
        // 신규 데이터
        // 010-1234-5678
        //
        // 기존 QA 데이터
        // 01012345678
        //
        // 모두 화면에서는
        // 010-1234-5678
        //
        // =====================================================

        const phoneElements =
            document.querySelectorAll(
                ".phone-value"
            );


        phoneElements.forEach(
            function (element) {


                const rawPhone =
                    element.dataset.phone
                    || element.textContent;


                if (!rawPhone) {
                    return;
                }


                const digits =
                    rawPhone.replace(
                        /[^0-9]/g,
                        ""
                    );


                element.textContent =
                    formatPhone(
                        digits
                    );

            }
        );



        // =====================================================
        // 2. 금액 천 단위 콤마
        // =====================================================
        //
        // 1250000
        //
        // ↓
        //
        // 1,250,000
        //
        // =====================================================

        const moneyElements =
            document.querySelectorAll(
                ".money-value"
            );


        moneyElements.forEach(
            function (element) {


                const rawPrice =
                    element.dataset.price
                    || element.textContent;


                if (
                    rawPrice === undefined
                    || rawPrice === null
                    || rawPrice === ""
                ) {

                    return;
                }


                const numberValue =
                    Number(
                        String(rawPrice)
                            .replace(
                                /,/g,
                                ""
                            )
                            .trim()
                    );


                if (
                    Number.isNaN(
                        numberValue
                    )
                ) {

                    return;
                }


                element.textContent =
                    new Intl.NumberFormat(
                        "ko-KR"
                    )
                        .format(
                            numberValue
                        );

            }
        );



        // =====================================================
        // 3. 수동 등급 변경 확인
        // =====================================================

        const manualGradeForm =
            document.querySelector(
                'form[action*="/grade/manual"]'
            );


        if (manualGradeForm) {


            manualGradeForm.addEventListener(
                "submit",
                function (event) {


                    const gradeSelect =
                        manualGradeForm.querySelector(
                            'select[name="gradeCode"]'
                        );


                    let gradeName =
                        "선택한 등급";


                    if (
                        gradeSelect
                        && gradeSelect.selectedOptions.length > 0
                    ) {

                        gradeName =
                            gradeSelect
                                .selectedOptions[0]
                                .textContent
                                .trim();
                    }


                    const confirmed =
                        window.confirm(
                            "고객 등급을 "
                            + gradeName
                            + "으로 수동 변경하시겠습니까?\n\n"
                            + "수동 등급으로 변경하면 자동 등급 계산에서 제외됩니다."
                        );


                    if (!confirmed) {

                        event.preventDefault();

                        return;
                    }


                    disableSubmitButtons(
                        manualGradeForm
                    );

                }
            );
        }



        // =====================================================
        // 4. 자동 등급 관리 전환 확인
        // =====================================================

        const automaticGradeForm =
            document.querySelector(
                'form[action*="/grade/automatic"]'
            );


        if (automaticGradeForm) {


            automaticGradeForm.addEventListener(
                "submit",
                function (event) {


                    const confirmed =
                        window.confirm(
                            "자동 등급 관리로 전환하시겠습니까?\n\n"
                            + "현재 방문 횟수와 누적 결제액을 기준으로 "
                            + "등급이 다시 계산됩니다."
                        );


                    if (!confirmed) {

                        event.preventDefault();

                        return;
                    }


                    disableSubmitButtons(
                        automaticGradeForm
                    );

                }
            );
        }



        // =====================================================
        // 5. 자동 등급 재계산 확인
        // =====================================================

        const recalculateGradeForm =
            document.querySelector(
                'form[action*="/grade/recalculate"]'
            );


        if (recalculateGradeForm) {


            recalculateGradeForm.addEventListener(
                "submit",
                function (event) {


                    const confirmed =
                        window.confirm(
                            "현재 방문 횟수와 누적 결제액을 기준으로 "
                            + "고객 등급을 다시 계산하시겠습니까?"
                        );


                    if (!confirmed) {

                        event.preventDefault();

                        return;
                    }


                    disableSubmitButtons(
                        recalculateGradeForm
                    );

                }
            );
        }



        // =====================================================
        // 6. URL #anchor 이동 보정
        // =====================================================
        //
        // 고객 목록에서:
        //
        // /admin/customers/22#memo-section
        //
        // /admin/customers/22#grade-section
        //
        // 으로 들어오면 해당 영역으로 이동합니다.
        //
        // =====================================================

        moveToHashSection();

    }
);



/**
 * =========================================================
 * 전화번호 화면 포맷
 * =========================================================
 */
function formatPhone(
    digits
) {


    if (!digits) {

        return "-";
    }



    // =====================================================
    // 서울 지역번호
    // =====================================================

    if (
        digits.startsWith(
            "02"
        )
    ) {


        // 02-123-4567
        if (
            digits.length === 9
        ) {

            return digits.substring(
                    0,
                    2
                )
                + "-"
                + digits.substring(
                    2,
                    5
                )
                + "-"
                + digits.substring(
                    5
                );
        }



        // 02-1234-5678
        if (
            digits.length === 10
        ) {

            return digits.substring(
                    0,
                    2
                )
                + "-"
                + digits.substring(
                    2,
                    6
                )
                + "-"
                + digits.substring(
                    6
                );
        }
    }



    // =====================================================
    // 11자리
    // =====================================================
    //
    // 01012345678
    //
    // ↓
    //
    // 010-1234-5678
    //
    // =====================================================

    if (
        digits.length === 11
    ) {

        return digits.substring(
                0,
                3
            )
            + "-"
            + digits.substring(
                3,
                7
            )
            + "-"
            + digits.substring(
                7
            );
    }



    // =====================================================
    // 일반 10자리
    // =====================================================
    //
    // 0311234567
    //
    // ↓
    //
    // 031-123-4567
    //
    // =====================================================

    if (
        digits.length === 10
    ) {

        return digits.substring(
                0,
                3
            )
            + "-"
            + digits.substring(
                3,
                6
            )
            + "-"
            + digits.substring(
                6
            );
    }



    // =====================================================
    // 예상하지 못한 기존 데이터
    // =====================================================

    return digits;
}



/**
 * =========================================================
 * 중복 제출 방지
 * =========================================================
 *
 * 버튼을 한 번 누른 뒤 서버 응답을 기다리는 동안
 * 여러 번 제출되는 것을 방지합니다.
 */
function disableSubmitButtons(
    form
) {


    const buttons =
        form.querySelectorAll(
            'button[type="submit"]'
        );


    buttons.forEach(
        function (button) {


            button.disabled =
                true;


            button.dataset.originalText =
                button.textContent;


            button.textContent =
                "처리 중...";

        }
    );
}



/**
 * =========================================================
 * Anchor 이동
 * =========================================================
 */
function moveToHashSection() {


    const hash =
        window.location.hash;


    if (!hash) {

        return;
    }


    const target =
        document.querySelector(
            hash
        );


    if (!target) {

        return;
    }


    /*
     * 페이지 스타일 및 sticky 메뉴 렌더링 이후
     * 자연스럽게 이동하도록 약간 지연합니다.
     */
    window.setTimeout(
        function () {


            target.scrollIntoView(
                {
                    behavior: "smooth",
                    block: "start"
                }
            );

        },
        100
    );
}