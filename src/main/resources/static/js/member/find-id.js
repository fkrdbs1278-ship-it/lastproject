document.addEventListener(
    "DOMContentLoaded",
    () => {

        /* Element */

        const nameInput =
            document.querySelector("#findIdName");

        const phoneInput =
            document.querySelector("#findIdPhone");

        const sendButton =
            document.querySelector("#findIdSendButton");

        const codeGroup =
            document.querySelector("#findIdCodeGroup");

        const codeInput =
            document.querySelector("#findIdCode");

        const verifyButton =
            document.querySelector("#findIdVerifyButton");

        const findButton =
            document.querySelector("#findIdButton");

        const sendMessage =
            document.querySelector("#findIdSendMessage");

        const verifyMessage =
            document.querySelector("#findIdVerifyMessage");

        const timerElement =
            document.querySelector("#findIdTimer");

        const resultArea =
            document.querySelector("#findIdResult");

        const resultIds =
            document.querySelector("#findIdResultIds");


        /* CSRF */

        const csrfToken =
            document
                .querySelector('meta[name="_csrf"]')
                ?.getAttribute("content");


        const csrfHeader =
            document
                .querySelector('meta[name="_csrf_header"]')
                ?.getAttribute("content");


        /* State */

        let verificationTimer =
            null;

        let resendTimer =
            null;

        let remainingSeconds =
            0;

        let requestedPhone =
            null;

        let verifiedPhone =
            null;


        /* Common */

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


        /* 전화번호 자동 하이픈 */

        phoneInput.addEventListener(
            "input",
            () => {

                let value =
                    phoneInput.value
                        .replace(
                            /\D/g,
                            ""
                        );


                if (
                    value.length <= 3
                ) {

                    phoneInput.value =
                        value;

                } else if (
                    value.length <= 7
                ) {

                    phoneInput.value =
                        value.slice(0, 3)
                        + "-"
                        + value.slice(3);

                } else {

                    phoneInput.value =
                        value.slice(0, 3)
                        + "-"
                        + value.slice(
                            3,
                            value.length - 4
                        )
                        + "-"
                        + value.slice(-4);
                }


                const currentPhone =
                    normalizePhone(
                        phoneInput.value
                    );


                /*
                 * 인증번호를 받은 뒤
                 * 전화번호가 변경되면
                 * 기존 인증 UI 초기화
                 */
                if (
                    requestedPhone !== null &&
                    currentPhone !== requestedPhone
                ) {

                    requestedPhone =
                        null;

                    verifiedPhone =
                        null;


                    stopVerificationTimer();


                    codeGroup.hidden =
                        true;


                    codeInput.value =
                        "";


                    codeInput.disabled =
                        false;


                    verifyButton.disabled =
                        false;


                    findButton.disabled =
                        true;


                    resultArea.hidden =
                        true;


                    if (timerElement) {

                        timerElement.textContent =
                            "";
                    }


                    if (verifyMessage) {

                        verifyMessage.textContent =
                            "";
                    }


                    sendMessage.textContent =
                        "전화번호가 변경되었습니다. 다시 인증해주세요.";

                    sendMessage.className =
                        "verification-message error";
                }

            }
        );


        /* 인증번호 숫자만 */

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


        /* 3분 Timer */

        function startVerificationTimer() {

            stopVerificationTimer();


            remainingSeconds =
                180;


            updateVerificationTimer();


            verificationTimer =
                setInterval(
                    () => {

                        remainingSeconds--;


                        updateVerificationTimer();


                        if (
                            remainingSeconds <= 0
                        ) {

                            stopVerificationTimer();


                            timerElement.textContent =
                                "인증시간이 만료되었습니다.";


                            verifyButton.disabled =
                                true;


                            findButton.disabled =
                                true;


                            verifyMessage.textContent =
                                "인증번호를 다시 요청해주세요.";

                            verifyMessage.className =
                                "verification-message error";
                        }

                    },
                    1000
                );
        }


        function updateVerificationTimer() {

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


        /* 재전송 60초 */

        function startResendCooldown() {

            if (
                resendTimer !== null
            ) {

                clearInterval(
                    resendTimer
                );
            }


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

                            clearInterval(
                                resendTimer
                            );


                            resendTimer =
                                null;


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


        /* 인증번호 발송 */

        sendButton.addEventListener(
            "click",
            async () => {

                const phone =
                    phoneInput.value;


                if (
                    !isValidPhone(phone)
                ) {

                    sendMessage.textContent =
                        "올바른 휴대전화번호를 입력해주세요.";

                    sendMessage.className =
                        "verification-message error";


                    phoneInput.focus();


                    return;
                }


                sendButton.disabled =
                    true;


                sendMessage.textContent =
                    "인증번호를 발송하고 있습니다...";

                sendMessage.className =
                    "verification-message";


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
                                            phone,

                                            purpose:
                                                "FIND_ID"

                                        }
                                    )

                            }
                        );


                    const result =
                        await response.json();


                    if (
                        !response.ok ||
                        !result.success
                    ) {

                        sendMessage.textContent =
                            result.message
                            || "인증번호 발송에 실패했습니다.";

                        sendMessage.className =
                            "verification-message error";


                        sendButton.disabled =
                            false;


                        return;
                    }


                    /* 발송 성공 */

                    requestedPhone =
                        normalizePhone(phone);


                    verifiedPhone =
                        null;


                    sendMessage.textContent =
                        "인증번호를 발송했습니다.";

                    sendMessage.className =
                        "verification-message success";


                    codeGroup.hidden =
                        false;


                    codeInput.disabled =
                        false;


                    codeInput.value =
                        "";


                    verifyButton.disabled =
                        false;


                    findButton.disabled =
                        true;


                    resultArea.hidden =
                        true;


                    verifyMessage.textContent =
                        "";


                    codeInput.focus();


                    startVerificationTimer();

                    startResendCooldown();


                } catch (error) {

                    console.error(
                        "아이디 찾기 인증번호 발송 오류:",
                        error
                    );


                    sendMessage.textContent =
                        "문자 발송 중 오류가 발생했습니다.";

                    sendMessage.className =
                        "verification-message error";


                    sendButton.disabled =
                        false;
                }

            }
        );


        /* 인증번호 확인 */

        verifyButton.addEventListener(
            "click",
            async () => {

                const phone =
                    phoneInput.value;

                const currentPhone =
                    normalizePhone(phone);

                const code =
                    codeInput.value.trim();


                if (
                    requestedPhone === null ||
                    requestedPhone !== currentPhone
                ) {

                    verifyMessage.textContent =
                        "전화번호가 변경되었습니다. 인증번호를 다시 받아주세요.";

                    verifyMessage.className =
                        "verification-message error";


                    return;
                }


                if (
                    !/^\d{6}$/
                        .test(code)
                ) {

                    verifyMessage.textContent =
                        "6자리 인증번호를 입력해주세요.";

                    verifyMessage.className =
                        "verification-message error";


                    codeInput.focus();


                    return;
                }


                verifyButton.disabled =
                    true;


                verifyMessage.textContent =
                    "인증번호를 확인하고 있습니다...";

                verifyMessage.className =
                    "verification-message";


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
                                            phone,

                                            code:
                                            code,

                                            purpose:
                                                "FIND_ID"

                                        }
                                    )

                            }
                        );


                    const result =
                        await response.json();


                    if (
                        !response.ok ||
                        !result.success
                    ) {

                        verifyMessage.textContent =
                            result.message
                            || "인증번호가 일치하지 않습니다.";

                        verifyMessage.className =
                            "verification-message error";


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
                        "휴대전화 인증이 완료되었습니다. ✓";

                    verifyMessage.className =
                        "verification-message success";


                    codeInput.disabled =
                        true;


                    verifyButton.disabled =
                        true;


                    /*
                     * 이제 아이디 찾기 가능
                     */
                    findButton.disabled =
                        false;


                } catch (error) {

                    console.error(
                        "아이디 찾기 인증 확인 오류:",
                        error
                    );


                    verifyMessage.textContent =
                        "인증번호 확인 중 오류가 발생했습니다.";

                    verifyMessage.className =
                        "verification-message error";


                    verifyButton.disabled =
                        false;
                }

            }
        );


        /* 아이디 찾기 */

        findButton.addEventListener(
            "click",
            async () => {

                const name =
                    nameInput.value.trim();

                const phone =
                    phoneInput.value;


                if (
                    name.length === 0
                ) {

                    alert(
                        "이름을 입력해주세요."
                    );


                    nameInput.focus();


                    return;
                }


                if (
                    verifiedPhone === null ||
                    verifiedPhone !==
                    normalizePhone(phone)
                ) {

                    alert(
                        "휴대전화 인증을 완료해주세요."
                    );


                    return;
                }


                findButton.disabled =
                    true;


                findButton.textContent =
                    "아이디를 찾고 있습니다...";


                try {

                    const response =
                        await fetch(
                            "/member/find-id/result",
                            {

                                method:
                                    "POST",

                                headers:
                                    createHeaders(),

                                body:
                                    JSON.stringify(
                                        {

                                            name:
                                            name,

                                            phone:
                                            phone

                                        }
                                    )

                            }
                        );


                    const result =
                        await response.json();


                    if (
                        !response.ok ||
                        !result.success
                    ) {

                        alert(
                            result.message
                            || "아이디 찾기에 실패했습니다."
                        );


                        findButton.disabled =
                            false;


                        return;
                    }


                    /* 결과 표시 */

                    resultIds.innerHTML =
                        "";


                    result.memberIds
                        .forEach(
                            memberId => {

                                const div =
                                    document.createElement(
                                        "div"
                                    );


                                div.className =
                                    "result-id";


                                /*
                                 * innerHTML이 아닌
                                 * textContent 사용
                                 */
                                div.textContent =
                                    memberId;


                                resultIds.appendChild(
                                    div
                                );
                            }
                        );


                    resultArea.hidden =
                        false;


                    /*
                     * 서버에서 FIND_ID 인증정보가
                     * consume 되었으므로
                     * 같은 인증으로 다시 조회 불가
                     */
                    verifiedPhone =
                        null;


                    findButton.disabled =
                        true;


                    findButton.textContent =
                        "아이디 찾기";


                } catch (error) {

                    console.error(
                        "아이디 찾기 오류:",
                        error
                    );


                    alert(
                        "아이디 찾기 중 오류가 발생했습니다."
                    );


                    findButton.disabled =
                        false;


                } finally {

                    if (
                        !resultArea.hidden
                    ) {

                        findButton.textContent =
                            "아이디 찾기";
                    }
                }

            }
        );

    }
);