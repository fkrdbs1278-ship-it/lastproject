document.addEventListener(
    "DOMContentLoaded",
    function () {


        // =====================================================
        // 전화번호 화면 표시 포맷
        // =====================================================
        //
        // DB 신규 데이터:
        //
        // 010-1234-5678
        //
        //
        // 기존 QA 데이터:
        //
        // 01012345678
        //
        //
        // 둘 다 화면에서는:
        //
        // 010-1234-5678
        //
        // 형태로 표시합니다.
        //
        // =====================================================

        const phoneElements =
            document.querySelectorAll(
                ".phone-value"
            );


        phoneElements.forEach(
            function (element) {


                const rawPhone =
                    element.dataset.phone;


                if (!rawPhone) {
                    return;
                }


                const digits =
                    rawPhone.replace(
                        /[^0-9]/g,
                        ""
                    );


                const formattedPhone =
                    formatPhone(
                        digits
                    );


                element.textContent =
                    formattedPhone;

            }
        );



        // =====================================================
        // 누적 결제액 천 단위 콤마
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
                    element.dataset.price;


                if (
                    rawPrice === undefined
                    || rawPrice === null
                    || rawPrice === ""
                ) {

                    return;
                }


                const price =
                    Number(
                        rawPrice
                    );


                if (
                    Number.isNaN(
                        price
                    )
                ) {

                    return;
                }


                element.textContent =
                    new Intl.NumberFormat(
                        "ko-KR"
                    )
                        .format(
                            price
                        );

            }
        );



        // =====================================================
        // 비활성 페이징 버튼 클릭 방지
        // =====================================================

        const disabledPageButtons =
            document.querySelectorAll(
                ".page-button.disabled"
            );


        disabledPageButtons.forEach(
            function (button) {


                button.addEventListener(
                    "click",
                    function (event) {

                        event.preventDefault();

                    }
                );

            }
        );

    }
);



/**
 * =========================================================
 * 전화번호 자동 포맷
 * =========================================================
 *
 * 숫자만 전달받아 화면 표시용 전화번호로 변경합니다.
 *
 *
 * 예:
 *
 * 01012345678
 *
 * →
 *
 * 010-1234-5678
 *
 *
 * 0212345678
 *
 * →
 *
 * 02-1234-5678
 *
 *
 * 0311234567
 *
 * →
 *
 * 031-123-4567
 */
function formatPhone(
    digits
) {


    // =====================================================
    // 값 없음
    // =====================================================

    if (!digits) {

        return "-";
    }



    // =====================================================
    // 서울 지역번호 02
    // =====================================================

    if (
        digits.startsWith(
            "02"
        )
    ) {


        // -------------------------------------------------
        // 02-123-4567
        // -------------------------------------------------

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



        // -------------------------------------------------
        // 02-1234-5678
        // -------------------------------------------------

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
    // 휴대폰 / 일반 11자리
    // =====================================================
    //
    // 01012345678
    //
    // →
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
    // 일반 전화번호 10자리
    // =====================================================
    //
    // 0311234567
    //
    // →
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
    // 예상하지 못한 형식
    // =====================================================
    //
    // 기존 잘못된 테스트 데이터가 있어도
    // 화면이 깨지지 않도록 원본 숫자를 반환합니다.
    //
    // =====================================================

    return digits;
}