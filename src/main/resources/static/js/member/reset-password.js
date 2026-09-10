document.addEventListener(
    "DOMContentLoaded",
    () => {

        /* Element */

        const memberIdInput =
            document.querySelector("#resetMemberId");

        const phoneInput =
            document.querySelector("#resetPhone");

        const sendButton =
            document.querySelector("#resetSendButton");

        const codeGroup =
            document.querySelector("#resetCodeGroup");

        const codeInput =
            document.querySelector("#resetCode");

        const verifyButton =
            document.querySelector("#resetVerifyButton");

        const timerElement =
            document.querySelector("#resetTimer");

        const sendMessage =
            document.querySelector("#resetSendMessage");

        const verifyMessage =
            document.querySelector("#resetVerifyMessage");

        const newPasswordArea =
            document.querySelector("#newPasswordArea");

        const passwordInput =
            document.querySelector("#newPassword");

        const passwordCheckInput =
            document.querySelector("#newPasswordCheck");

        const passwordToggle =
            document.querySelector("#newPasswordToggle");

        const passwordCheckToggle =
            document.querySelector("#newPasswordCheckToggle");

        const matchMessage =
            document.querySelector("#passwordMatchMessage");

        const resetButton =
            document.querySelector("#resetPasswordButton");

        const resultMessage =
            document.querySelector("#resetResultMessage");


        /* Password Rule Elements */

        const ruleLength =
            document.querySelector("#resetRuleLength");

        const ruleUppercase =
            document.querySelector("#resetRuleUppercase");

        const ruleLowercase =
            document.querySelector("#resetRuleLowercase");

        const ruleNumber =
            document.querySelector("#resetRuleNumber");

        const ruleSpecial =
            document.querySelector("#resetRuleSpecial");

        const ruleNoSpace =
            document.querySelector("#resetRuleNoSpace");


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

        let verificationTimer = null;

        let resendTimer = null;

        let requestedPhone = null;

        let verifiedPhone = null;

        let remainingSeconds = 0;


        /* =========================================
           Common
        ========================================= */

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
                    normalizePhone(
                        phoneInput.value
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
                 * 인증번호 받은 이후
                 * 전화번호 변경 시 인증 취소
                 */
                if (
                    requestedPhone !== null &&
                    currentPhone !== requestedPhone
                ) {

                    requestedPhone = null;

                    verifiedPhone = null;


                    stopVerificationTimer();


                    codeGroup.hidden =
                        true;


                    newPasswordArea.hidden =
                        true;


                    resetButton.disabled =
                        true;


                    sendMessage.textContent =
                        "전화번호가 변경되었습니다. 다시 인증해주세요.";

                    sendMessage.className =
                        "verification-message error";
                }

            }
        );

        memberIdInput.addEventListener(
            "input",
            () => {

                /*
                 * 회원정보 확인 후에는
                 * input이 disabled라 변경할 수 없으므로
                 * 주로 인증 진행 중 아이디 변경을 처리한다.
                 */

                verifiedPhone =
                    null;


                newPasswordArea.hidden =
                    true;


                resetButton.disabled =
                    true;


                resultMessage.textContent =
                    "";
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
                                "verification-message error";
                        }

                    },
                    1000
                );
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

                const memberId =
                    memberIdInput.value.trim();

                const phone =
                    phoneInput.value;


                if (!memberId) {

                    alert(
                        "아이디를 입력해주세요."
                    );


                    memberIdInput.focus();


                    return;
                }


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
                                                "RESET_PASSWORD"

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


                    newPasswordArea.hidden =
                        true;


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
                        "비밀번호 재설정 인증번호 발송 오류:",
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
                                                "RESET_PASSWORD"

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


                    /* =========================================
                        휴대전화 인증 성공 후
                        아이디 + 전화번호 회원정보 확인
                    ========================================= */

                    verifyMessage.textContent =
                        "회원정보를 확인하고 있습니다...";

                    verifyMessage.className =
                        "verification-message";


                    const memberId =
                        memberIdInput.value.trim();


                    const memberResponse =
                        await fetch(
                            "/member/reset-password/validate-member",
                            {

                                method:
                                    "POST",

                                headers:
                                    createHeaders(),

                                body:
                                    JSON.stringify(
                                        {

                                            memberId:
                                            memberId,

                                            phone:
                                            phone

                                        }
                                    )

                            }
                        );


                    const memberResult =
                        await memberResponse.json();


                    /* 회원정보 불일치 */

                    if (
                        !memberResponse.ok ||
                        !memberResult.success
                    ) {

                        verifiedPhone =
                            null;


                        newPasswordArea.hidden =
                            true;


                        verifyMessage.textContent =
                            memberResult.message
                            || "입력한 회원정보를 확인해주세요.";


                        verifyMessage.className =
                            "verification-message error";


                        /*
                         * 아이디를 고칠 수 있도록
                         * 입력창은 그대로 활성화
                         */

                        memberIdInput.disabled =
                            false;


                        phoneInput.disabled =
                            false;


                        verifyButton.disabled =
                            false;


                        return;
                    }


                    /* =========================================
                       회원정보까지 정상 확인
                    ========================================= */

                    verifiedPhone =
                        currentPhone;


                    memberIdInput.disabled =
                        true;


                    phoneInput.disabled =
                        true;


                    stopVerificationTimer();


                    timerElement.textContent =
                        "";


                    verifyMessage.textContent =
                        "회원정보 확인이 완료되었습니다. ✓";


                    verifyMessage.className =
                        "verification-message success";


                    codeInput.disabled =
                        true;


                    verifyButton.disabled =
                        true;


                    /*
                     * 회원정보 확인까지 성공한 경우에만
                     * 새 비밀번호 영역 표시
                     */

                    newPasswordArea.hidden =
                        false;


                    passwordInput.focus();


                    updatePasswordValidation();


                } catch (error) {

                    console.error(
                        "비밀번호 재설정 인증 확인 오류:",
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


        /* =========================================
           비밀번호 보기 / 숨기기
        ========================================= */

        function bindPasswordToggle(
            button,
            input
        ) {

            button.addEventListener(
                "click",
                () => {

                    const isPassword =
                        input.type ===
                        "password";


                    input.type =
                        isPassword
                            ? "text"
                            : "password";


                    button.textContent =
                        isPassword
                            ? "숨기기"
                            : "보기";
                }
            );
        }


        bindPasswordToggle(
            passwordToggle,
            passwordInput
        );


        bindPasswordToggle(
            passwordCheckToggle,
            passwordCheckInput
        );


        /* =========================================
           비밀번호 Validation
        ========================================= */

        function checkPasswordRules(
            value
        ) {

            return {

                length:
                    value.length >= 8 &&
                    value.length <= 20,

                uppercase:
                    /[A-Z]/.test(value),

                lowercase:
                    /[a-z]/.test(value),

                number:
                    /\d/.test(value),

                special:
                    /[!@#$%^&*]/.test(value),

                noSpace:
                    !/\s/.test(value)

            };
        }


        function setRuleState(
            element,
            valid
        ) {

            if (valid) {

                element.classList.add(
                    "valid"
                );

            } else {

                element.classList.remove(
                    "valid"
                );
            }
        }


        function updatePasswordValidation() {

            const password =
                passwordInput.value;

            const check =
                passwordCheckInput.value;


            const rules =
                checkPasswordRules(
                    password
                );


            setRuleState(
                ruleLength,
                rules.length
            );

            setRuleState(
                ruleUppercase,
                rules.uppercase
            );

            setRuleState(
                ruleLowercase,
                rules.lowercase
            );

            setRuleState(
                ruleNumber,
                rules.number
            );

            setRuleState(
                ruleSpecial,
                rules.special
            );

            setRuleState(
                ruleNoSpace,
                rules.noSpace
            );


            const passwordValid =
                Object
                    .values(rules)
                    .every(Boolean);


            const matches =
                password.length > 0 &&
                check.length > 0 &&
                password === check;


            if (
                check.length === 0
            ) {

                matchMessage.textContent =
                    "";

                matchMessage.className =
                    "password-match-message";

            } else if (matches) {

                matchMessage.textContent =
                    "비밀번호가 일치합니다. ✓";

                matchMessage.className =
                    "password-match-message success";

            } else {

                matchMessage.textContent =
                    "비밀번호가 일치하지 않습니다.";

                matchMessage.className =
                    "password-match-message error";
            }


            resetButton.disabled =
                !(
                    verifiedPhone !== null &&
                    passwordValid &&
                    matches
                );
        }


        passwordInput.addEventListener(
            "input",
            updatePasswordValidation
        );


        passwordCheckInput.addEventListener(
            "input",
            updatePasswordValidation
        );


        /* =========================================
           비밀번호 변경
        ========================================= */

        resetButton.addEventListener(
            "click",
            async () => {

                const memberId =
                    memberIdInput.value.trim();

                const phone =
                    phoneInput.value;

                const currentPhone =
                    normalizePhone(phone);

                const newPassword =
                    passwordInput.value;

                const newPasswordCheck =
                    passwordCheckInput.value;


                if (
                    verifiedPhone === null ||
                    currentPhone !== verifiedPhone
                ) {

                    resultMessage.textContent =
                        "휴대전화 인증을 완료해주세요.";

                    resultMessage.className =
                        "verification-message error result-message";


                    return;
                }


                resetButton.disabled =
                    true;


                resetButton.textContent =
                    "변경 중...";


                resultMessage.textContent =
                    "";


                try {

                    const response =
                        await fetch(
                            "/member/reset-password",
                            {

                                method:
                                    "POST",

                                headers:
                                    createHeaders(),

                                body:
                                    JSON.stringify(
                                        {

                                            memberId:
                                            memberId,

                                            phone:
                                            phone,

                                            newPassword:
                                            newPassword,

                                            newPasswordCheck:
                                            newPasswordCheck

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

                        resultMessage.textContent =
                            result.message
                            || "비밀번호 변경에 실패했습니다.";

                        resultMessage.className =
                            "verification-message error result-message";


                        resetButton.disabled =
                            false;


                        resetButton.textContent =
                            "비밀번호 변경";


                        return;
                    }


                    /* =================================
                       성공
                    ================================= */

                    resultMessage.textContent =
                        "비밀번호가 변경되었습니다. 로그인 화면으로 이동합니다.";

                    resultMessage.className =
                        "verification-message success result-message";


                    verifiedPhone =
                        null;


                    setTimeout(
                        () => {

                            window.location.href =
                                "/member/login";

                        },
                        1500
                    );


                } catch (error) {

                    console.error(
                        "비밀번호 변경 오류:",
                        error
                    );


                    resultMessage.textContent =
                        "비밀번호 변경 중 오류가 발생했습니다.";

                    resultMessage.className =
                        "verification-message error result-message";


                    resetButton.disabled =
                        false;


                    resetButton.textContent =
                        "비밀번호 변경";
                }

            }
        );

    }
);