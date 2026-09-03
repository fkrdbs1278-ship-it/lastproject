document.addEventListener(
    "DOMContentLoaded",
    function () {


        // =====================================================
        // Element
        // =====================================================

        const form =
            document.getElementById(
                "customerCreateForm"
            );


        const customerNameInput =
            document.getElementById(
                "customerName"
            );


        const phoneInput =
            document.getElementById(
                "phone"
            );


        const submitButton =
            document.getElementById(
                "submitButton"
            );


        if (!form) {

            return;
        }



        // =====================================================
        // 1. 전화번호 입력 자동 하이픈
        // =====================================================
        //
        // 입력:
        //
        // 01012345678
        //
        // ↓
        //
        // 화면:
        //
        // 010-1234-5678
        //
        // =====================================================

        if (phoneInput) {


            phoneInput.addEventListener(
                "input",
                function () {


                    const digits =
                        extractPhoneDigits(
                            phoneInput.value
                        );


                    phoneInput.value =
                        formatPhoneWhileTyping(
                            digits
                        );


                    updatePhoneValidationState(
                        phoneInput
                    );

                }
            );



            // =================================================
            // 입력란을 벗어났을 때 최종 포맷
            // =================================================

            phoneInput.addEventListener(
                "blur",
                function () {


                    const digits =
                        extractPhoneDigits(
                            phoneInput.value
                        );


                    if (!digits) {

                        clearValidationState(
                            phoneInput
                        );

                        return;
                    }


                    const formattedPhone =
                        formatPhone(
                            digits
                        );


                    if (formattedPhone) {

                        phoneInput.value =
                            formattedPhone;

                    }


                    updatePhoneValidationState(
                        phoneInput
                    );

                }
            );

        }



        // =====================================================
        // 2. 고객명 입력 검증 표시
        // =====================================================

        if (customerNameInput) {


            customerNameInput.addEventListener(
                "input",
                function () {


                    updateNameValidationState(
                        customerNameInput
                    );

                }
            );

        }



        // =====================================================
        // 3. 등록 전 최종 검증
        // =====================================================

        form.addEventListener(
            "submit",
            function (event) {


                // -------------------------------------------------
                // 고객명 검증
                // -------------------------------------------------

                const customerName =
                    customerNameInput
                        ? customerNameInput.value.trim()
                        : "";


                if (!customerName) {

                    event.preventDefault();


                    setInvalid(
                        customerNameInput
                    );


                    alert(
                        "고객명을 입력해 주세요."
                    );


                    if (customerNameInput) {

                        customerNameInput.focus();

                    }


                    return;
                }



                if (
                    customerName.length > 50
                ) {

                    event.preventDefault();


                    setInvalid(
                        customerNameInput
                    );


                    alert(
                        "고객명은 50자 이하로 입력해 주세요."
                    );


                    if (customerNameInput) {

                        customerNameInput.focus();

                    }


                    return;
                }



                // -------------------------------------------------
                // 전화번호 검증
                // -------------------------------------------------

                const phoneDigits =
                    extractPhoneDigits(
                        phoneInput
                            ? phoneInput.value
                            : ""
                    );


                if (!phoneDigits) {

                    event.preventDefault();


                    setInvalid(
                        phoneInput
                    );


                    alert(
                        "전화번호를 입력해 주세요."
                    );


                    if (phoneInput) {

                        phoneInput.focus();

                    }


                    return;
                }



                const formattedPhone =
                    formatPhone(
                        phoneDigits
                    );


                if (!formattedPhone) {

                    event.preventDefault();


                    setInvalid(
                        phoneInput
                    );


                    alert(
                        "올바른 전화번호를 입력해 주세요."
                    );


                    if (phoneInput) {

                        phoneInput.focus();

                    }


                    return;
                }



                // -------------------------------------------------
                // 서버로 전송할 전화번호 표준화
                // -------------------------------------------------
                //
                // 01012345678
                //
                // ↓
                //
                // 010-1234-5678
                //
                // -------------------------------------------------

                if (phoneInput) {

                    phoneInput.value =
                        formattedPhone;

                }



                // -------------------------------------------------
                // 정상 상태 표시
                // -------------------------------------------------

                setValid(
                    customerNameInput
                );


                setValid(
                    phoneInput
                );



                // -------------------------------------------------
                // 중복 제출 방지
                // -------------------------------------------------

                if (submitButton) {


                    submitButton.disabled =
                        true;


                    submitButton.textContent =
                        "등록 중...";

                }

            }
        );

    }
);



/**
 * =========================================================
 * 전화번호 숫자만 추출
 * =========================================================
 *
 * 010-1234-5678
 * 010 1234 5678
 * 01012345678
 *
 * ↓
 *
 * 01012345678
 */
function extractPhoneDigits(
    phone
) {


    if (!phone) {

        return "";
    }


    return phone
        .replace(
            /[^0-9]/g,
            ""
        )
        .substring(
            0,
            11
        );
}



/**
 * =========================================================
 * 입력 중 전화번호 포맷
 * =========================================================
 *
 * 예:
 *
 * 0101
 *
 * ↓
 *
 * 010-1
 *
 *
 * 01012345
 *
 * ↓
 *
 * 010-1234-5
 *
 *
 * 01012345678
 *
 * ↓
 *
 * 010-1234-5678
 */
function formatPhoneWhileTyping(
    digits
) {


    if (!digits) {

        return "";
    }



    // =====================================================
    // 서울 지역번호 02
    // =====================================================

    if (
        digits.startsWith(
            "02"
        )
    ) {


        if (
            digits.length <= 2
        ) {

            return digits;
        }


        if (
            digits.length <= 5
        ) {

            return digits.substring(
                    0,
                    2
                )
                + "-"
                + digits.substring(
                    2
                );
        }


        if (
            digits.length <= 9
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
                6,
                10
            );
    }



    // =====================================================
    // 일반 전화 / 휴대폰
    // =====================================================

    if (
        digits.length <= 3
    ) {

        return digits;
    }


    if (
        digits.length <= 7
    ) {

        return digits.substring(
                0,
                3
            )
            + "-"
            + digits.substring(
                3
            );
    }


    if (
        digits.length <= 10
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
            7,
            11
        );
}



/**
 * =========================================================
 * 최종 전화번호 포맷
 * =========================================================
 *
 * 올바른 번호:
 *
 * 포맷된 전화번호 반환
 *
 * 잘못된 번호:
 *
 * null 반환
 */
function formatPhone(
    digits
) {


    if (!digits) {

        return null;
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


        return null;
    }



    // =====================================================
    // 11자리 전화번호
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
    // 10자리 일반 전화번호
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



    return null;
}



/**
 * =========================================================
 * 고객명 검증 표시
 * =========================================================
 */
function updateNameValidationState(
    input
) {


    if (!input) {

        return;
    }


    const value =
        input.value.trim();


    if (!value) {

        clearValidationState(
            input
        );

        return;
    }


    if (
        value.length <= 50
    ) {

        setValid(
            input
        );

    } else {

        setInvalid(
            input
        );

    }
}



/**
 * =========================================================
 * 전화번호 검증 표시
 * =========================================================
 */
function updatePhoneValidationState(
    input
) {


    if (!input) {

        return;
    }


    const digits =
        extractPhoneDigits(
            input.value
        );


    if (!digits) {

        clearValidationState(
            input
        );

        return;
    }


    if (
        formatPhone(
            digits
        )
    ) {

        setValid(
            input
        );

    } else {


        // 입력 중에는 바로 오류색을 표시하지 않음
        if (
            digits.length >= 9
        ) {

            setInvalid(
                input
            );

        } else {

            clearValidationState(
                input
            );

        }

    }
}



/**
 * =========================================================
 * 정상 입력 표시
 * =========================================================
 */
function setValid(
    input
) {


    if (!input) {

        return;
    }


    const formGroup =
        input.closest(
            ".form-group"
        );


    if (!formGroup) {

        return;
    }


    formGroup.classList.remove(
        "has-error"
    );


    formGroup.classList.add(
        "valid"
    );
}



/**
 * =========================================================
 * 오류 입력 표시
 * =========================================================
 */
function setInvalid(
    input
) {


    if (!input) {

        return;
    }


    const formGroup =
        input.closest(
            ".form-group"
        );


    if (!formGroup) {

        return;
    }


    formGroup.classList.remove(
        "valid"
    );


    formGroup.classList.add(
        "has-error"
    );
}



/**
 * =========================================================
 * 검증 표시 초기화
 * =========================================================
 */
function clearValidationState(
    input
) {


    if (!input) {

        return;
    }


    const formGroup =
        input.closest(
            ".form-group"
        );


    if (!formGroup) {

        return;
    }


    formGroup.classList.remove(
        "valid"
    );


    formGroup.classList.remove(
        "has-error"
    );
}