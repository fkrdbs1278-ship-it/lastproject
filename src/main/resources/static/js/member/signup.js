document.addEventListener("DOMContentLoaded", () => {

    /* 요소 */

    const signupForm =
        document.querySelector("#signupForm");

    const signupButton =
        document.querySelector("#signupButton");

    const nameInput =
        document.querySelector("#name");

    const nicknameInput =
        document.querySelector("#nickname");

    const nameMessage =
        document.querySelector("#nameMessage");

    const nicknameMessage =
        document.querySelector("#nicknameMessage");


    /* 아이디 */

    const memberIdInput =
        document.querySelector("#memberId");

    const memberIdCheckButton =
        document.querySelector("#memberIdCheckButton");

    const memberIdMessage =
        document.querySelector("#memberIdMessage");

    const memberIdServerError =
        document.querySelector("#memberIdServerError");


    /*
     * 중복 확인 완료 여부
     *
     * true  = 현재 입력된 아이디 중복 확인 완료
     * false = 다시 중복 확인 필요
     */
    let memberIdChecked = false;


    /*
     * 마지막으로 중복 확인에 성공한 아이디
     */
    let checkedMemberId = "";


    const password = document.querySelector("#password");
    const passwordCheck = document.querySelector("#passwordCheck");

    const passwordToggle =
        document.querySelector("#passwordToggle");

    const passwordCheckToggle =
        document.querySelector("#passwordCheckToggle");

    const passwordMatchMessage =
        document.querySelector("#passwordMatchMessage");

    const phone =
        document.querySelector("#phone");

    const birthDate =
        document.querySelector("#birthDate");



    /* =========================================================
        아이디 검사
        ========================================================= */

    /*
     * 아이디 허용 규칙
     *
     * 영문 소문자
     * 숫자
     * _
     * -
     *
     * 4 ~ 20자
     */
    const memberIdPattern =
        /^[a-z0-9_-]{4,20}$/;


    /*
     * 아이디 메시지 초기화
     */
    function clearMemberIdMessage() {

        if (!memberIdMessage) {
            return;
        }

        memberIdMessage.textContent = "";

        memberIdMessage.className =
            "member-id-message";
    }


    /*
     * 아이디 실시간 형식 검사
     */
    function validateMemberId() {

        if (!memberIdInput || !memberIdMessage) {
            return false;
        }


        const memberId =
            memberIdInput.value;


        /*
         * 입력값이 바뀌면
         * 기존 중복 확인 결과는 무효
         */
        memberIdChecked = false;
        checkedMemberId = "";


        /*
         * 서버에서 내려온 기존 Validation 오류 제거
         */
        if (memberIdServerError) {

            memberIdServerError.textContent = "";

            memberIdServerError.style.display =
                "none";
        }


        /*
         * 아무것도 입력하지 않은 경우
         */
        if (memberId === "") {

            clearMemberIdMessage();

            return false;
        }

        /*
         * 공백 포함 검사
         */
        if (/\s/.test(memberId)) {

            memberIdMessage.textContent =
                "아이디에는 공백을 사용할 수 없습니다.";

            memberIdMessage.className =
                "member-id-message error";

            return false;
        }



        /*
         * 한글 포함
         */
        if (
            /[ㄱ-ㅎㅏ-ㅣ가-힣]/.test(memberId)
        ) {

            memberIdMessage.textContent =
                "아이디는 영어로 입력해주세요.";

            memberIdMessage.className =
                "member-id-message error";

            return false;
        }


        /*
         * 대문자 포함
         */
        if (/[A-Z]/.test(memberId)) {

            memberIdMessage.textContent =
                "아이디는 영문 소문자로 입력해주세요.";

            memberIdMessage.className =
                "member-id-message error";

            return false;
        }


        /*
         * 허용되지 않는 문자 또는 길이
         */
        if (!memberIdPattern.test(memberId)) {

            memberIdMessage.textContent =
                "영문 소문자, 숫자, _, -를 사용하여 4~20자로 입력해주세요.";

            memberIdMessage.className =
                "member-id-message error";

            return false;
        }


        /*
         * 형식은 정상
         * 아직 중복 확인 전
         */
        memberIdMessage.textContent =
            "사용 가능한 형식입니다. 중복 확인을 해주세요.";

        memberIdMessage.className =
            "member-id-message";

        return true;
    }


    /* 비밀번호 조건 표시 요소 */

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


    /* 비밀번호 조건 검사 */

    function validatePassword() {

        if (!password) {
            return;
        }

        const value = password.value;


        const conditions = {

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


    /* 비밀번호 확인 */

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
    /* 비밀번호 보기 / 숨기기 */

    function setupPasswordToggle(
        input,
        toggleButton
    ) {

        if (!input || !toggleButton) {
            return;
        }


        toggleButton.addEventListener(
            "click",
            () => {

                const isHidden =
                    input.type === "password";


                if (isHidden) {

                    input.type = "text";

                    toggleButton.textContent =
                        "숨기기";

                    toggleButton.setAttribute(
                        "aria-label",
                        "비밀번호 숨기기"
                    );

                } else {

                    input.type = "password";

                    toggleButton.textContent =
                        "보기";

                    toggleButton.setAttribute(
                        "aria-label",
                        "비밀번호 표시"
                    );
                }


                input.focus();
            }
        );
    }


    setupPasswordToggle(
        password,
        passwordToggle
    );

    setupPasswordToggle(
        passwordCheck,
        passwordCheckToggle
    );


    /* 전화번호 자동 하이픈 */

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
        서버 Validation 오류 제거
        ========================================================= */

    /*
     * 서버에서 출력한 빨간 오류 메시지를 숨긴다.
     */
    function hideServerError(
        errorElement
    ) {

        if (!errorElement) {
            return;
        }


        errorElement.textContent =
            "";

        errorElement.style.display =
            "none";
    }


    /*
     * 해당 입력칸이 속한 form-group의
     * 서버 Validation 오류를 제거한다.
     */
    function clearServerValidationError(
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


        const errors =
            formGroup.querySelectorAll(
                ".field-error"
            );


        errors.forEach(
            (error) => {

                hideServerError(
                    error
                );
            }
        );
    }


    /* =========================================================
    이름 / 닉네임 최대 글자 수 제한
    ========================================================= */

    function limitTextLength(
        input,
        maxLength
    ) {

        if (!input) {
            return;
        }


        const characters =
            Array.from(
                input.value
            );


        if (
            characters.length >
            maxLength
        ) {

            input.value =
                characters
                    .slice(
                        0,
                        maxLength
                    )
                    .join("");
        }
    }


    function validateTextLength(
        input,
        messageElement,
        label
    ) {

        if (!input || !messageElement) {
            return;
        }


        const value =
            input.value.trim();


        /*
         * 빈 값은 서버의 필수값 Validation에 맡긴다.
         */
        if (value.length === 0) {

            messageElement.textContent =
                "";

            messageElement.className =
                "field-message";

            return;
        }


        if (
            value.length < 2 ||
            value.length > 20
        ) {

            messageElement.textContent =
                `${label}은(는) 2자 이상 20자 이하로 입력해주세요.`;

            messageElement.className =
                "field-message error";

            return;
        }


        messageElement.textContent =
            "";

        messageElement.className =
            "field-message";
    }

    /* 이름 실시간 검사 */

    function validateName() {

        if (!nameInput || !nameMessage) {
            return;
        }


        const value =
            nameInput.value;


        /* 빈 값 */

        if (value.length === 0) {

            nameMessage.textContent = "";

            nameMessage.className =
                "field-message";

            return;
        }


        /* 길이 검사 */

        if (
            value.length < 2 ||
            value.length > 20
        ) {

            nameMessage.textContent =
                "이름은 2자 이상 20자 이하로 입력해주세요.";

            nameMessage.className =
                "field-message error";

            return;
        }


        /*
         * 이름 형식 검사
         *
         * 가능:
         * 홍길동
         * Lee
         * Lee Jae Heon
         *
         * 불가능:
         * Lee  Jae
         *  Lee
         * Lee
         * 숫자/특수문자
         */
        if (
            !/^[가-힣a-zA-Z]+(?: [가-힣a-zA-Z]+)*$/
                .test(value)
        ) {

            nameMessage.textContent =
                "이름은 한글, 영문과 단어 사이 한 칸의 공백만 사용할 수 있습니다.";

            nameMessage.className =
                "field-message error";

            return;
        }


        /* 정상 */

        nameMessage.textContent = "";

        nameMessage.className =
            "field-message";
    }




    /*닉네임 전용 검사함수*/
    function validateNickname() {

        if (!nicknameInput || !nicknameMessage) {
            return;
        }


        const value =
            nicknameInput.value;


        if (value.length === 0) {

            nicknameMessage.textContent = "";

            nicknameMessage.className =
                "field-message";

            return;
        }


        /* 공백 검사 */

        if (/\s/.test(value)) {

            nicknameMessage.textContent =
                "닉네임에는 공백을 사용할 수 없습니다.";

            nicknameMessage.className =
                "field-message error";

            return;
        }


        /* 길이 검사 */

        if (
            value.length < 2 ||
            value.length > 20
        ) {

            nicknameMessage.textContent =
                "닉네임은 2자 이상 20자 이하로 입력해주세요.";

            nicknameMessage.className =
                "field-message error";

            return;
        }


        /* 허용 문자 검사 */

        if (
            !/^[가-힣a-zA-Z0-9]+$/.test(
                value
            )
        ) {

            nicknameMessage.textContent =
                "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.";

            nicknameMessage.className =
                "field-message error";

            return;
        }


        nicknameMessage.textContent = "";

        nicknameMessage.className =
            "field-message";
    }


    /* Event */

    /* =========================================================
    약관 / 개인정보 내용보기
    ========================================================= */

    const agreementDetailButtons =
        document.querySelectorAll(
            ".agreement-detail-button"
        );


    agreementDetailButtons.forEach(
        (button) => {

            button.addEventListener(
                "click",
                () => {

                    const dialogId =
                        button.dataset.dialog;


                    const dialog =
                        document.getElementById(
                            dialogId
                        );


                    if (!dialog) {
                        return;
                    }


                    dialog.showModal();
                }
            );
        }
    );


    /* 닫기 / 확인 버튼 */

    document
        .querySelectorAll(
            ".agreement-dialog"
        )
        .forEach(
            (dialog) => {

                const closeButton =
                    dialog.querySelector(
                        ".agreement-dialog-close"
                    );


                const confirmButton =
                    dialog.querySelector(
                        ".agreement-dialog-confirm"
                    );


                if (closeButton) {

                    closeButton.addEventListener(
                        "click",
                        () => {

                            dialog.close();
                        }
                    );
                }


                if (confirmButton) {

                    confirmButton.addEventListener(
                        "click",
                        () => {

                            dialog.close();
                        }
                    );
                }


                /*
                 * 모달 바깥 영역 클릭 시 닫기
                 */
                dialog.addEventListener(
                    "click",
                    (event) => {

                        if (
                            event.target === dialog
                        ) {

                            dialog.close();
                        }
                    }
                );
            }
        );




    if (nameInput) {

        nameInput.addEventListener(
            "input",
            (event) => {

                /*
                 * 한글 조합 중에는
                 * 20자 강제 자르기만 하지 않는다.
                 */
                if (!event.isComposing) {

                    limitTextLength(
                        nameInput,
                        20
                    );
                }


                /*
                 * 길이 검사는 조합 중에도 실행
                 */
                validateName();
            }
        );


        nameInput.addEventListener(
            "compositionend",
            () => {

                limitTextLength(
                    nameInput,
                    20
                );


                validateName();
            }
        );
    }


    if (nicknameInput) {

        nicknameInput.addEventListener(
            "input",
            (event) => {

                /*
                 * 한글 조합 중에는
                 * 20자 강제 자르기만 하지 않는다.
                 */
                if (!event.isComposing) {

                    limitTextLength(
                        nicknameInput,
                        20
                    );
                }


                /*
                 * 길이 검사는 조합 중에도 실행
                 */
                validateNickname();
            }
        );


        nicknameInput.addEventListener(
            "compositionend",
            () => {

                limitTextLength(
                    nicknameInput,
                    20
                );


                validateNickname();
            }
        );
    }





    /* =========================================================
    입력값 수정 시 기존 서버 오류 제거
    ========================================================= */

    if (signupForm) {

        const validationFields =
            signupForm.querySelectorAll(
                ".form-group input, "
                + ".form-group select"
            );


        validationFields.forEach(
            (field) => {

                /*
                 * 문자 입력
                 */
                field.addEventListener(
                    "input",
                    () => {

                        clearServerValidationError(
                            field
                        );
                    }
                );


                /*
                 * 날짜 / 라디오 / select 등
                 */
                field.addEventListener(
                    "change",
                    () => {

                        clearServerValidationError(
                            field
                        );
                    }
                );
            }
        );
    }


    /* =========================================================
     약관 동의 서버 오류 제거
    ========================================================= */

    if (signupForm) {

        const agreementInputs =
            signupForm.querySelectorAll(
                ".agreement-item input"
            );


        agreementInputs.forEach(
            (checkbox) => {

                checkbox.addEventListener(
                    "change",
                    () => {

                        /*
                         * 체크되지 않은 상태라면
                         * 오류를 지우지 않는다.
                         */
                        if (!checkbox.checked) {
                            return;
                        }


                        const agreementItem =
                            checkbox.closest(
                                ".agreement-item"
                            );


                        if (!agreementItem) {
                            return;
                        }


                        /*
                         * label 바로 다음의
                         * field-error 찾기
                         */
                        const errorElement =
                            agreementItem
                                .nextElementSibling;


                        if (
                            errorElement &&
                            errorElement
                                .classList
                                .contains(
                                    "field-error"
                                )
                        ) {

                            hideServerError(
                                errorElement
                            );
                        }
                    }
                );
            }
        );
    }

    if (memberIdInput) {

        memberIdInput.addEventListener(
            "input",
            validateMemberId
        );
    }

    /* 아이디 중복 확인 */

    if (
        memberIdInput &&
        memberIdCheckButton &&
        memberIdMessage
    ) {

        memberIdCheckButton.addEventListener(
            "click",
            async () => {

                /*
                 * 먼저 형식 검사
                 */
                if (!validateMemberId()) {

                    memberIdInput.focus();

                    return;
                }


                const memberId =
                    memberIdInput.value;


                /*
                 * CSRF
                 */
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


                const headers = {

                    "Content-Type":
                        "application/x-www-form-urlencoded;charset=UTF-8"
                };


                if (
                    csrfToken &&
                    csrfHeader
                ) {

                    headers[csrfHeader] =
                        csrfToken;
                }


                /*
                 * 검사 중 버튼 비활성화
                 */
                memberIdCheckButton.disabled =
                    true;

                memberIdCheckButton.textContent =
                    "확인 중...";


                memberIdMessage.textContent =
                    "아이디 중복 여부를 확인하고 있습니다.";

                memberIdMessage.className =
                    "member-id-message";


                try {

                    const response =
                        await fetch(
                            "/member/id-check",
                            {

                                method:
                                    "POST",

                                headers:
                                headers,

                                body:
                                    new URLSearchParams(
                                        {
                                            memberId:
                                            memberId
                                        }
                                    )
                            }
                        );


                    if (!response.ok) {

                        throw new Error(
                            "아이디 중복 확인 요청 실패"
                        );
                    }


                    const data =
                        await response.json();


                    /*
                     * 중복 확인 요청 중
                     * 사용자가 아이디를 변경한 경우
                     */
                    if (
                        memberIdInput.value
                        !== memberId
                    ) {

                        memberIdChecked =
                            false;

                        checkedMemberId =
                            "";

                        memberIdMessage.textContent =
                            "아이디가 변경되었습니다. 다시 중복 확인해주세요.";

                        memberIdMessage.className =
                            "member-id-message error";

                        return;
                    }


                    /*
                     * 서버 응답이
                     *
                     * isDuplicate
                     * 또는
                     * duplicate
                     *
                     * 둘 중 어느 이름이어도 처리 가능
                     */
                    const isDuplicate =
                        data.isDuplicate
                        ?? data.duplicate
                        ?? false;


                    if (isDuplicate) {

                        memberIdChecked =
                            false;

                        checkedMemberId =
                            "";

                        memberIdMessage.textContent =
                            "이미 사용 중인 아이디입니다.";

                        memberIdMessage.className =
                            "member-id-message error";

                    } else {

                        memberIdChecked =
                            true;

                        checkedMemberId =
                            memberId;

                        memberIdMessage.textContent =
                            "사용 가능한 아이디입니다. ✓";

                        memberIdMessage.className =
                            "member-id-message success";
                    }


                } catch (error) {

                    console.error(
                        "아이디 중복 확인 오류:",
                        error
                    );


                    memberIdChecked =
                        false;

                    checkedMemberId =
                        "";

                    memberIdMessage.textContent =
                        "아이디 중복 확인 중 오류가 발생했습니다.";

                    memberIdMessage.className =
                        "member-id-message error";

                } finally {

                    memberIdCheckButton.disabled =
                        false;

                    memberIdCheckButton.textContent =
                        "중복 확인";
                }
            }
        );
    }

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

    /* 회원가입 중복 제출 방지 */

    if (signupForm && signupButton) {

        signupForm.addEventListener(
            "submit",
            (event) => {

                /*
                 * 현재 입력되어 있는 아이디
                 */
                const currentMemberId =
                    memberIdInput
                        ?.value
                    ?? "";


                /*
                 * 아이디 형식이 잘못됐거나
                 * 중복 확인을 하지 않았거나
                 * 중복 확인 후 아이디가 변경된 경우
                 */
                if (
                    !memberIdPattern.test(
                        currentMemberId
                    ) ||
                    !memberIdChecked ||
                    checkedMemberId !== currentMemberId
                ) {

                    event.preventDefault();


                    if (memberIdMessage) {

                        /*
                         * 아이디 형식 자체가 잘못된 경우
                         */
                        if (
                            !memberIdPattern.test(
                                currentMemberId
                            )
                        ) {

                            memberIdMessage.textContent =
                                "올바른 아이디를 입력해주세요.";

                        } else {

                            /*
                             * 형식은 맞지만
                             * 중복 확인이 안 된 경우
                             */
                            memberIdMessage.textContent =
                                "아이디 중복 확인을 해주세요.";
                        }


                        memberIdMessage.className =
                            "member-id-message error";
                    }


                    if (memberIdInput) {

                        memberIdInput.focus();
                    }


                    return;
                }


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
                signupButton.disabled =
                    true;


                signupButton.textContent =
                    "가입 처리 중...";
            }
        );
    }

    /* 브라우저 뒤로가기 시 버튼 상태 복구 */

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


/* 휴대전화 인증 */

document.addEventListener(
    "DOMContentLoaded",
    () => {

        /* 요소 찾기 */

        const phoneInput =
            document.querySelector("#phone");

        const sendButton =
            document.querySelector("#phoneSendButton");

        const codeGroup =
            document.querySelector("#phoneCodeGroup");

        const codeInput =
            document.querySelector("#phoneVerificationCode");

        const verifyButton =
            document.querySelector("#phoneVerifyButton");

        const sendMessage =
            document.querySelector("#phoneSendMessage");

        const verifyMessage =
            document.querySelector("#phoneVerifyMessage");

        const timerElement =
            document.querySelector("#phoneTimer");


        /*
         * 필요한 요소가 없으면
         * 휴대전화 인증 JS 실행 중지
         */
        if (
            !phoneInput ||
            !sendButton ||
            !codeGroup ||
            !codeInput ||
            !verifyButton
        ) {
            return;
        }


        /* CSRF Token */

        const csrfToken =
            document
                .querySelector('meta[name="_csrf"]')
                ?.getAttribute("content");


        const csrfHeader =
            document
                .querySelector('meta[name="_csrf_header"]')
                ?.getAttribute("content");


        /* 상태 */

        let verificationTimer = null;

        let resendTimer = null;

        let remainingSeconds = 0;

        /*
         * 마지막으로 인증번호를 받은 전화번호
         */
        let requestedPhone = null;

        /*
         * 최종 인증 완료된 전화번호
         */
        let verifiedPhone = null;


        /* 전화번호 숫자만 추출

           010-1234-5678
                 ↓
           01012345678 */

        function normalizePhone(value) {

            if (!value) {
                return "";
            }

            return value.replace(
                /\D/g,
                ""
            );
        }


        /* 전화번호 형식 검사 */

        function isValidPhone(value) {

            const phone =
                normalizePhone(value);


            return /^01[016789]\d{7,8}$/
                .test(phone);
        }


        /* Fetch Header 생성 */

        function createHeaders() {

            const headers = {
                "Content-Type":
                    "application/json"
            };


            /*
             * Spring Security CSRF
             */
            if (
                csrfToken &&
                csrfHeader
            ) {

                headers[csrfHeader] =
                    csrfToken;
            }


            return headers;
        }


        /* 인증번호 3분 Timer */

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


                            if (timerElement) {

                                timerElement.textContent =
                                    "인증시간이 만료되었습니다.";
                            }


                            verifyButton.disabled =
                                true;


                            if (verifyMessage) {

                                verifyMessage.textContent =
                                    "인증번호를 다시 요청해주세요.";

                                verifyMessage.className =
                                    "phone-verification-message error";
                            }
                        }

                    },
                    1000
                );
        }


        function updateVerificationTimer() {

            if (!timerElement) {
                return;
            }


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


        /* 인증번호 재발송 60초 제한 */

        function startResendCooldown() {

            /*
             * 기존 재전송 Timer가 있으면 정리
             */
            if (
                resendTimer !== null
            ) {

                clearInterval(
                    resendTimer
                );

                resendTimer = null;
            }


            let resendSeconds =
                60;


            sendButton.disabled =
                true;


            sendButton.textContent =
                `재전송 ${resendSeconds}초`;


            resendTimer =
                setInterval(
                    () => {

                        resendSeconds--;


                        if (
                            resendSeconds <= 0
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
                            `재전송 ${resendSeconds}초`;

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


                /*
                 * 전화번호 형식 확인
                 */
                if (
                    !isValidPhone(phone)
                ) {

                    if (sendMessage) {

                        sendMessage.textContent =
                            "올바른 휴대전화번호를 입력해주세요.";

                        sendMessage.className =
                            "phone-verification-message error";
                    }


                    phoneInput.focus();


                    return;
                }


                sendButton.disabled =
                    true;


                if (sendMessage) {

                    sendMessage.textContent =
                        "인증번호를 발송하고 있습니다...";

                    sendMessage.className =
                        "phone-verification-message";
                }


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
                                                "SIGNUP"

                                        }
                                    )

                            }
                        );


                    const result =
                        await response.json();


                    /*
                     * 서버에서 실패 응답
                     */
                    if (
                        !response.ok ||
                        !result.success
                    ) {

                        if (sendMessage) {

                            sendMessage.textContent =
                                result.message
                                || "인증번호 발송에 실패했습니다.";

                            sendMessage.className =
                                "phone-verification-message error";
                        }


                        sendButton.disabled =
                            false;


                        return;
                    }


                    /* 발송 성공 */

                    requestedPhone =
                        normalizePhone(phone);


                    verifiedPhone =
                        null;


                    if (sendMessage) {

                        sendMessage.textContent =
                            "인증번호를 발송했습니다.";

                        sendMessage.className =
                            "phone-verification-message success";
                    }


                    /*
                     * 인증번호 입력 영역 표시
                     */
                    codeGroup.hidden =
                        false;


                    codeInput.disabled =
                        false;


                    codeInput.value =
                        "";


                    verifyButton.disabled =
                        false;


                    if (verifyMessage) {

                        verifyMessage.textContent =
                            "";

                        verifyMessage.className =
                            "phone-verification-message";
                    }


                    codeInput.focus();


                    /*
                     * 인증번호 3분
                     */
                    startVerificationTimer();


                    /*
                     * 재전송 60초
                     */
                    startResendCooldown();


                } catch (error) {

                    console.error(
                        "휴대전화 인증번호 발송 오류:",
                        error
                    );


                    if (sendMessage) {

                        sendMessage.textContent =
                            "문자 발송 중 오류가 발생했습니다.";

                        sendMessage.className =
                            "phone-verification-message error";
                    }


                    sendButton.disabled =
                        false;
                }

            }
        );


        /* 인증번호 입력은 숫자만 */

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


        /* 인증번호 확인 */

        verifyButton.addEventListener(
            "click",
            async () => {

                const phone =
                    phoneInput.value;


                const currentPhone =
                    normalizePhone(phone);


                const code =
                    codeInput.value
                        .trim();


                /*
                 * 전화번호를 인증번호 발송 후
                 * 변경했는지 확인
                 */
                if (
                    requestedPhone === null ||
                    requestedPhone !== currentPhone
                ) {

                    if (verifyMessage) {

                        verifyMessage.textContent =
                            "전화번호가 변경되었습니다. 인증번호를 다시 받아주세요.";

                        verifyMessage.className =
                            "phone-verification-message error";
                    }


                    return;
                }


                /*
                 * 인증번호 형식
                 */
                if (
                    !/^\d{6}$/.test(code)
                ) {

                    if (verifyMessage) {

                        verifyMessage.textContent =
                            "6자리 인증번호를 입력해주세요.";

                        verifyMessage.className =
                            "phone-verification-message error";
                    }


                    codeInput.focus();


                    return;
                }


                verifyButton.disabled =
                    true;


                if (verifyMessage) {

                    verifyMessage.textContent =
                        "인증번호를 확인하고 있습니다...";

                    verifyMessage.className =
                        "phone-verification-message";
                }


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
                                                "SIGNUP"

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

                        if (verifyMessage) {

                            verifyMessage.textContent =
                                result.message
                                || "인증번호가 일치하지 않습니다.";

                            verifyMessage.className =
                                "phone-verification-message error";
                        }


                        verifyButton.disabled =
                            false;


                        return;
                    }


                    /* 인증 성공 */

                    verifiedPhone =
                        currentPhone;


                    stopVerificationTimer();


                    if (timerElement) {

                        timerElement.textContent =
                            "";
                    }


                    if (verifyMessage) {

                        verifyMessage.textContent =
                            "휴대전화 인증이 완료되었습니다. ✓";

                        verifyMessage.className =
                            "phone-verification-message success";
                    }


                    codeInput.disabled =
                        true;


                    verifyButton.disabled =
                        true;


                } catch (error) {

                    console.error(
                        "휴대전화 인증 확인 오류:",
                        error
                    );


                    if (verifyMessage) {

                        verifyMessage.textContent =
                            "인증번호 확인 중 오류가 발생했습니다.";

                        verifyMessage.className =
                            "phone-verification-message error";
                    }


                    verifyButton.disabled =
                        false;
                }

            }
        );


        /* 전화번호가 변경되면 인증 상태 취소 */

        phoneInput.addEventListener(
            "input",
            () => {

                const currentPhone =
                    normalizePhone(
                        phoneInput.value
                    );


                /*
                 * 인증번호를 받은 뒤
                 * 전화번호가 바뀐 경우
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


                    if (timerElement) {

                        timerElement.textContent =
                            "";
                    }


                    if (verifyMessage) {

                        verifyMessage.textContent =
                            "";
                    }


                    if (sendMessage) {

                        sendMessage.textContent =
                            "전화번호가 변경되었습니다. 다시 인증해주세요.";

                        sendMessage.className =
                            "phone-verification-message error";
                    }
                }

            }
        );

    }
);