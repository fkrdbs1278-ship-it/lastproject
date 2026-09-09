document.addEventListener("DOMContentLoaded", () => {

    /* 요소 */

    const phone =
        document.querySelector("#phone");

    const birthDate =
        document.querySelector("#birthDate");


    /* 전화번호 자동 하이픈 */

    function formatPhoneNumber(value) {

        /*
         * 숫자가 아닌 문자 제거
         */
        const numbers =
            value
                .replace(/\D/g, "")
                .slice(0, 11);


        /*
         * 010
         */
        if (numbers.length <= 3) {

            return numbers;
        }


        /*
         * 010-123
         */
        if (numbers.length <= 6) {

            return (
                numbers.slice(0, 3)
                + "-"
                + numbers.slice(3)
            );
        }


        /*
         * 10자리 번호
         *
         * 0111234567
         * →
         * 011-123-4567
         */
        if (numbers.length <= 10) {

            return (
                numbers.slice(0, 3)
                + "-"
                + numbers.slice(3, 6)
                + "-"
                + numbers.slice(6)
            );
        }


        /*
         * 11자리 번호
         *
         * 01012345678
         * →
         * 010-1234-5678
         */
        return (
            numbers.slice(0, 3)
            + "-"
            + numbers.slice(3, 7)
            + "-"
            + numbers.slice(7, 11)
        );
    }


    /* 생년월일

       최소 : 1900-01-01
       최대 : 어제 */

    function formatDateForInput(date) {

        const year =
            date.getFullYear();

        const month =
            String(
                date.getMonth() + 1
            ).padStart(2, "0");

        const day =
            String(
                date.getDate()
            ).padStart(2, "0");


        return `${year}-${month}-${day}`;
    }


    if (birthDate) {

        /*
         * 최소 날짜
         */
        birthDate.min =
            "1900-01-01";


        /*
         * 오늘
         */
        const today =
            new Date();


        /*
         * 어제
         */
        const yesterday =
            new Date(
                today.getFullYear(),
                today.getMonth(),
                today.getDate() - 1
            );


        /*
         * 어제까지만 선택 가능
         */
        birthDate.max =
            formatDateForInput(
                yesterday
            );
    }


    /* 휴대전화 변경 인증 */

    const sendButton =
        document.querySelector(
            "#updatePhoneSendButton"
        );

    const changeMessage =
        document.querySelector(
            "#updatePhoneChangeMessage"
        );

    const codeGroup =
        document.querySelector(
            "#updatePhoneCodeGroup"
        );

    const codeInput =
        document.querySelector(
            "#updatePhoneCode"
        );

    const verifyButton =
        document.querySelector(
            "#updatePhoneVerifyButton"
        );

    const timerElement =
        document.querySelector(
            "#updatePhoneTimer"
        );

    const verifyMessage =
        document.querySelector(
            "#updatePhoneVerifyMessage"
        );


    /*
     * 인증 UI가 없는 페이지에서도
     * 기존 전화번호/생년월일 JS는 정상 작동하도록
     * 여기서 전체 return을 하지 않는다.
     */
    if (
        phone &&
        sendButton &&
        changeMessage &&
        codeGroup &&
        codeInput &&
        verifyButton &&
        timerElement &&
        verifyMessage
    ) {

        /* CSRF */

        const csrfToken =
            document
                .querySelector(
                    'meta[name="_csrf"]'
                )
                ?.getAttribute(
                    "content"
                );


        const csrfHeader =
            document
                .querySelector(
                    'meta[name="_csrf_header"]'
                )
                ?.getAttribute(
                    "content"
                );


        /* 전화번호 공통 처리 */

        function normalizePhone(value) {

            return (value || "")
                .replace(
                    /\D/g,
                    ""
                );
        }


        function isValidPhone(value) {

            return /^01[016789]\d{7,8}$/
                .test(
                    normalizePhone(value)
                );
        }


        /*
         * 회원정보 수정 화면을 처음 열었을 때의
         * 원래 전화번호
         */
        const originalPhone =
            normalizePhone(
                phone.value
            );


        /* 상태 */

        let requestedPhone =
            null;

        let verifiedPhone =
            null;

        let verificationTimer =
            null;

        let resendTimer =
            null;

        let remainingSeconds =
            0;


        /* Fetch Header */

        function createHeaders() {

            const headers = {
                "Content-Type":
                    "application/json"
            };


            if (
                csrfToken &&
                csrfHeader
            ) {

                headers[csrfHeader] =
                    csrfToken;
            }


            return headers;
        }


        /* 인증번호 Timer */

        function stopVerificationTimer() {

            if (
                verificationTimer !== null
            ) {

                clearInterval(
                    verificationTimer
                );

                verificationTimer =
                    null;
            }
        }


        function updateTimer() {

            const minutes =
                Math.floor(
                    remainingSeconds / 60
                );


            const seconds =
                remainingSeconds % 60;


            timerElement.textContent =
                "남은 시간 "
                + String(minutes)
                    .padStart(2, "0")
                + ":"
                + String(seconds)
                    .padStart(2, "0");
        }


        function startVerificationTimer() {

            stopVerificationTimer();


            remainingSeconds =
                180;


            updateTimer();


            verificationTimer =
                setInterval(
                    () => {

                        remainingSeconds--;


                        updateTimer();


                        if (
                            remainingSeconds <= 0
                        ) {

                            stopVerificationTimer();


                            timerElement.textContent =
                                "인증시간이 만료되었습니다.";


                            verifyButton.disabled =
                                true;


                            verifyMessage.textContent =
                                "인증번호를 다시 요청해주세요.";

                            verifyMessage.className =
                                "phone-verification-message error";
                        }

                    },
                    1000
                );
        }


        /* 재전송 Timer */

        function stopResendTimer() {

            if (
                resendTimer !== null
            ) {

                clearInterval(
                    resendTimer
                );

                resendTimer =
                    null;
            }
        }


        function startResendCooldown() {

            stopResendTimer();


            let seconds =
                60;


            sendButton.disabled =
                true;


            sendButton.textContent =
                `재전송 ${seconds}초`;


            resendTimer =
                setInterval(
                    () => {

                        seconds--;


                        if (
                            seconds <= 0
                        ) {

                            stopResendTimer();


                            sendButton.disabled =
                                false;


                            sendButton.textContent =
                                "인증번호 다시 받기";


                            return;
                        }


                        sendButton.textContent =
                            `재전송 ${seconds}초`;

                    },
                    1000
                );
        }


        /* 인증 UI 초기화 */

        function resetVerificationState() {

            requestedPhone =
                null;

            verifiedPhone =
                null;


            stopVerificationTimer();


            codeGroup.hidden =
                true;

            codeGroup.style.display =
                "none";


            codeInput.value =
                "";

            codeInput.disabled =
                false;


            verifyButton.disabled =
                false;


            timerElement.textContent =
                "";


            verifyMessage.textContent =
                "";

            verifyMessage.className =
                "phone-verification-message";
        }


        /* 전화번호 변경 여부 표시 */

        function updatePhoneChangeState() {

            const currentPhone =
                normalizePhone(
                    phone.value
                );


            /*
             * 기존 번호와 동일
             *
             * SMS 인증 필요 없음
             */
            if (
                currentPhone === originalPhone
            ) {

                resetVerificationState();


                stopResendTimer();


                sendButton.hidden =
                    true;

                sendButton.disabled =
                    false;

                sendButton.textContent =
                    "인증번호 받기";


                changeMessage.textContent =
                    "";

                changeMessage.className =
                    "phone-verification-message";


                return;
            }


            /*
             * 번호가 변경됨
             */
            sendButton.hidden =
                false;


            /*
             * 아직 현재 번호 인증이 안 된 경우
             */
            if (
                verifiedPhone !== currentPhone
            ) {

                changeMessage.textContent =
                    "전화번호가 변경되었습니다. 새 번호 인증을 완료해주세요.";

                changeMessage.className =
                    "phone-verification-message error";

            } else {

                changeMessage.textContent =
                    "";
            }
        }


        /* 전화번호 입력 */

        phone.addEventListener(
            "input",
            () => {

                /*
                 * 기존 자동 하이픈 기능
                 */
                phone.value =
                    formatPhoneNumber(
                        phone.value
                    );


                const currentPhone =
                    normalizePhone(
                        phone.value
                    );


                /*
                 * 인증번호를 발송했던 번호가 있는데
                 * 현재 번호가 달라졌다면
                 *
                 * 이전 인증은 화면상 무효 처리
                 */
                if (
                    requestedPhone !== null &&
                    currentPhone !== requestedPhone
                ) {

                    resetVerificationState();
                }


                /*
                 * 인증 완료 후 다시 번호를 변경한 경우
                 */
                if (
                    verifiedPhone !== null &&
                    currentPhone !== verifiedPhone
                ) {

                    resetVerificationState();
                }


                updatePhoneChangeState();
            }
        );


        /* 인증번호 숫자만 입력 */

        codeInput.addEventListener(
            "input",
            () => {

                codeInput.value =
                    codeInput.value
                        .replace(
                            /\D/g,
                            ""
                        )
                        .slice(
                            0,
                            6
                        );
            }
        );


        /* 인증번호 발송 */

        sendButton.addEventListener(
            "click",
            async () => {

                const currentPhone =
                    phone.value;


                /*
                 * 휴대전화번호 형식 확인
                 */
                if (
                    !isValidPhone(
                        currentPhone
                    )
                ) {

                    changeMessage.textContent =
                        "올바른 휴대전화번호를 입력해주세요.";

                    changeMessage.className =
                        "phone-verification-message error";


                    phone.focus();


                    return;
                }


                /*
                 * 기존 번호라면
                 * 인증할 필요 없음
                 */
                if (
                    normalizePhone(
                        currentPhone
                    )
                    === originalPhone
                ) {

                    return;
                }


                sendButton.disabled =
                    true;


                changeMessage.textContent =
                    "인증번호를 발송하고 있습니다...";

                changeMessage.className =
                    "phone-verification-message";


                try {

                    const response =
                        await fetch(
                            "/member/phone-verification/send",
                            {

                                method:
                                    "POST",

                                headers:
                                    createHeaders(),

                                body:
                                    JSON.stringify(
                                        {

                                            phone:
                                            currentPhone,

                                            purpose:
                                                "UPDATE_PHONE"

                                        }
                                    )

                            }
                        );


                    const result =
                        await response.json();


                    /*
                     * 발송 실패
                     */
                    if (
                        !response.ok ||
                        !result.success
                    ) {

                        changeMessage.textContent =
                            result.message
                            || "인증번호 발송에 실패했습니다.";

                        changeMessage.className =
                            "phone-verification-message error";


                        sendButton.disabled =
                            false;


                        return;
                    }


                    /* 발송 성공 */

                    requestedPhone =
                        normalizePhone(
                            currentPhone
                        );


                    verifiedPhone =
                        null;


                    changeMessage.textContent =
                        "인증번호를 발송했습니다.";

                    changeMessage.className =
                        "phone-verification-message success";


                    codeGroup.hidden =
                        false;

                    codeGroup.style.display =
                        "";


                    codeInput.value =
                        "";

                    codeInput.disabled =
                        false;


                    verifyButton.disabled =
                        false;


                    verifyMessage.textContent =
                        "";


                    codeInput.focus();


                    startVerificationTimer();

                    startResendCooldown();


                } catch (error) {

                    console.error(
                        "전화번호 변경 인증번호 발송 오류:",
                        error
                    );


                    changeMessage.textContent =
                        "문자 발송 중 오류가 발생했습니다.";

                    changeMessage.className =
                        "phone-verification-message error";


                    sendButton.disabled =
                        false;
                }

            }
        );


        /* 인증번호 확인 */

        verifyButton.addEventListener(
            "click",
            async () => {

                const currentPhone =
                    normalizePhone(
                        phone.value
                    );


                const code =
                    codeInput.value
                        .trim();


                /*
                 * 발송했던 번호와 현재 번호 비교
                 */
                if (
                    requestedPhone === null ||
                    requestedPhone !== currentPhone
                ) {

                    verifyMessage.textContent =
                        "전화번호가 변경되었습니다. 인증번호를 다시 받아주세요.";

                    verifyMessage.className =
                        "phone-verification-message error";


                    return;
                }


                /*
                 * 인증번호 형식
                 */
                if (
                    !/^\d{6}$/
                        .test(code)
                ) {

                    verifyMessage.textContent =
                        "6자리 인증번호를 입력해주세요.";

                    verifyMessage.className =
                        "phone-verification-message error";


                    codeInput.focus();


                    return;
                }


                verifyButton.disabled =
                    true;


                verifyMessage.textContent =
                    "인증번호를 확인하고 있습니다...";

                verifyMessage.className =
                    "phone-verification-message";


                try {

                    const response =
                        await fetch(
                            "/member/phone-verification/verify",
                            {

                                method:
                                    "POST",

                                headers:
                                    createHeaders(),

                                body:
                                    JSON.stringify(
                                        {

                                            phone:
                                            phone.value,

                                            code:
                                            code,

                                            purpose:
                                                "UPDATE_PHONE"

                                        }
                                    )

                            }
                        );


                    const result =
                        await response.json();


                    /*
                     * 인증 실패
                     */
                    if (
                        !response.ok ||
                        !result.success
                    ) {

                        verifyMessage.textContent =
                            result.message
                            || "인증번호가 일치하지 않습니다.";

                        verifyMessage.className =
                            "phone-verification-message error";


                        verifyButton.disabled =
                            false;


                        return;
                    }


                    /* 인증 성공 */

                    verifiedPhone =
                        currentPhone;


                    stopVerificationTimer();


                    timerElement.textContent =
                        "";


                    verifyMessage.textContent =
                        "새 휴대전화번호 인증이 완료되었습니다. ✓";

                    verifyMessage.className =
                        "phone-verification-message success";


                    changeMessage.textContent =
                        "";


                    codeInput.disabled =
                        true;


                    verifyButton.disabled =
                        true;


                } catch (error) {

                    console.error(
                        "전화번호 변경 인증 확인 오류:",
                        error
                    );


                    verifyMessage.textContent =
                        "인증번호 확인 중 오류가 발생했습니다.";

                    verifyMessage.className =
                        "phone-verification-message error";


                    verifyButton.disabled =
                        false;
                }

            }
        );


        /* 처음 화면 상태 */

        updatePhoneChangeState();

    } else if (phone) {

        /*
         * SMS 인증 UI가 아직 없는 경우에도
         * 기존 자동 하이픈 기능은 유지
         */
        phone.addEventListener(
            "input",
            () => {

                phone.value =
                    formatPhoneNumber(
                        phone.value
                    );
            }
        );
    }

});