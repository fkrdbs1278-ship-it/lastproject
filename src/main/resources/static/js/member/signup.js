document.addEventListener("DOMContentLoaded", () => {

    /* =========================================================
       요소
    ========================================================= */

    const signupForm =
        document.querySelector("#signupForm");

    const signupButton =
        document.querySelector("#signupButton");


    const password = document.querySelector("#password");
    const passwordCheck = document.querySelector("#passwordCheck");

    const passwordMatchMessage =
        document.querySelector("#passwordMatchMessage");

    const phone =
        document.querySelector("#phone");

    const birthDate =
        document.querySelector("#birthDate");


    /* =========================================================
       비밀번호 조건 표시 요소
    ========================================================= */

    const passwordRules = {

        length:
            document.querySelector("#ruleLength"),

        uppercase:
            document.querySelector("#ruleUppercase"),

        lowercase:
            document.querySelector("#ruleLowercase"),

        number:
            document.querySelector("#ruleNumber"),

        special:
            document.querySelector("#ruleSpecial"),

        noSpace:
            document.querySelector("#ruleNoSpace")
    };


    /* =========================================================
       비밀번호 조건 검사
    ========================================================= */

    function validatePassword() {

        if (!password) {
            return;
        }

        const value = password.value;


        const conditions = {

            length:
                value.length >= 8 &&
                value.length <= 100,

            uppercase:
                /[A-Z]/.test(value),

            lowercase:
                /[a-z]/.test(value),

            number:
                /\d/.test(value),

            special:
                /[^A-Za-z0-9\s]/.test(value),

            noSpace:
                value.length > 0 &&
                !/\s/.test(value)
        };


        Object.entries(conditions)
            .forEach(([key, valid]) => {

                const element =
                    passwordRules[key];

                if (!element) {
                    return;
                }


                element.classList.toggle(
                    "valid",
                    valid
                );

                element.classList.toggle(
                    "invalid",
                    !valid
                );
            });


        /*
         * 비밀번호가 바뀌면
         * 비밀번호 확인 상태도 다시 검사
         */
        validatePasswordMatch();
    }


    /* =========================================================
       비밀번호 확인
    ========================================================= */

    function validatePasswordMatch() {

        if (
            !password ||
            !passwordCheck ||
            !passwordMatchMessage
        ) {
            return;
        }


        const original =
            password.value;

        const confirmation =
            passwordCheck.value;


        /*
         * 아직 비밀번호 확인을 입력하지 않은 경우
         */
        if (confirmation.length === 0) {

            passwordMatchMessage.textContent = "";

            passwordMatchMessage.classList.remove(
                "success",
                "error"
            );

            return;
        }


        /*
         * 일치
         */
        if (
            original.length > 0 &&
            original === confirmation
        ) {

            passwordMatchMessage.textContent =
                "✓ 비밀번호가 일치합니다.";

            passwordMatchMessage.classList.add(
                "success"
            );

            passwordMatchMessage.classList.remove(
                "error"
            );

        }

        /*
         * 불일치
         */
        else {

            passwordMatchMessage.textContent =
                "비밀번호가 일치하지 않습니다.";

            passwordMatchMessage.classList.add(
                "error"
            );

            passwordMatchMessage.classList.remove(
                "success"
            );
        }
    }


    /* =========================================================
       전화번호 자동 하이픈
    ========================================================= */

    function formatPhoneNumber(value) {

        /*
         * 숫자를 제외한 문자 제거
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
         * 10자리 전화번호
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
         * 11자리 전화번호
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


    if (phone) {

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


    /* =========================================================
       생년월일

       최소 : 1900-01-01
       최대 : 어제
    ========================================================= */

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
         * 오늘 날짜 생성
         */
        const today =
            new Date();


        /*
         * 오늘의 00:00 기준으로 생성
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


    /* =========================================================
       Event
    ========================================================= */

    if (password) {

        password.addEventListener(
            "input",
            validatePassword
        );
    }


    if (passwordCheck) {

        passwordCheck.addEventListener(
            "input",
            validatePasswordMatch
        );
    }


    /*
     * 서버 Validation 후 화면으로 돌아왔을 때를 위해
     * 초기 상태도 한번 검사
     */
    validatePassword();
    validatePasswordMatch();

    /* =========================================================
    회원가입 중복 제출 방지
    ========================================================= */

    if (signupForm && signupButton) {

        signupForm.addEventListener(
            "submit",
            (event) => {

                /*
                 * 이미 한번 제출된 경우
                 */
                if (
                    signupForm.dataset.submitting
                    === "true"
                ) {

                    event.preventDefault();

                    return;
                }


                /*
                 * 제출 상태 기록
                 */
                signupForm.dataset.submitting =
                    "true";


                /*
                 * 버튼 비활성화
                 */
                signupButton.disabled = true;

                signupButton.textContent =
                    "가입 처리 중...";
            }
        );
    }

    /* =========================================================
       브라우저 뒤로가기 시 버튼 상태 복구
    ========================================================= */

    window.addEventListener(
        "pageshow",
        () => {

            if (!signupForm || !signupButton) {
                return;
            }

            signupForm.dataset.submitting =
                "false";

            signupButton.disabled =
                false;

            signupButton.textContent =
                "회원가입";
        }
    );





});