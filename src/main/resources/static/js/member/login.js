document.addEventListener("DOMContentLoaded", () => {

    /* =========================================================
       요소
    ========================================================= */

    const loginForm =
        document.querySelector("#loginForm");

    const loginPassword =
        document.querySelector("#loginPassword");

    const passwordToggle =
        document.querySelector("#passwordToggle");

    const capsLockMessage =
        document.querySelector("#capsLockMessage");

    const loginButton =
        document.querySelector("#loginButton");


    /* =========================================================
       비밀번호 보기 / 숨기기
    ========================================================= */

    if (
        loginPassword &&
        passwordToggle
    ) {

        passwordToggle.addEventListener(
            "click",
            () => {

                const isHidden =
                    loginPassword.type === "password";


                if (isHidden) {

                    loginPassword.type =
                        "text";

                    passwordToggle.textContent =
                        "숨기기";

                    passwordToggle.setAttribute(
                        "aria-label",
                        "비밀번호 숨기기"
                    );

                } else {

                    loginPassword.type =
                        "password";

                    passwordToggle.textContent =
                        "보기";

                    passwordToggle.setAttribute(
                        "aria-label",
                        "비밀번호 표시"
                    );
                }


                loginPassword.focus();
            }
        );
    }


    /* =========================================================
       Caps Lock 안내
    ========================================================= */

    function checkCapsLock(event) {

        if (!capsLockMessage) {
            return;
        }


        const capsLockOn =
            event.getModifierState
            && event.getModifierState(
                "CapsLock"
            );


        if (capsLockOn) {

            capsLockMessage.textContent =
                "Caps Lock이 켜져 있습니다.";

            capsLockMessage.classList.add(
                "show"
            );

        } else {

            capsLockMessage.textContent =
                "";

            capsLockMessage.classList.remove(
                "show"
            );
        }
    }


    if (loginPassword) {

        loginPassword.addEventListener(
            "keydown",
            checkCapsLock
        );

        loginPassword.addEventListener(
            "keyup",
            checkCapsLock
        );


        loginPassword.addEventListener(
            "blur",
            () => {

                if (!capsLockMessage) {
                    return;
                }


                capsLockMessage.textContent =
                    "";

                capsLockMessage.classList.remove(
                    "show"
                );
            }
        );
    }


    /* =========================================================
       로그인 중복 제출 방지
    ========================================================= */

    if (
        loginForm &&
        loginButton
    ) {

        loginForm.addEventListener(
            "submit",
            (event) => {

                /*
                 * 이미 제출 중
                 */
                if (
                    loginForm.dataset.submitting
                    === "true"
                ) {

                    event.preventDefault();

                    return;
                }


                loginForm.dataset.submitting =
                    "true";


                loginButton.disabled =
                    true;

                loginButton.textContent =
                    "로그인 중...";
            }
        );
    }


    /* =========================================================
       브라우저 뒤로가기 시 버튼 복구
    ========================================================= */

    window.addEventListener(
        "pageshow",
        () => {

            if (
                !loginForm ||
                !loginButton
            ) {
                return;
            }


            loginForm.dataset.submitting =
                "false";

            loginButton.disabled =
                false;

            loginButton.textContent =
                "로그인";
        }
    );

});