document.addEventListener(
    "DOMContentLoaded",
    function () {


        // =================================================
        // Toast
        // =================================================

        const toast =
            document.querySelector(
                "#privacyToast"
            );


        let toastTimer;


        function showToast(message) {

            if (!toast) {
                return;
            }


            clearTimeout(
                toastTimer
            );


            toast.textContent =
                message;


            toast.classList.add(
                "show"
            );


            toastTimer =
                setTimeout(
                    function () {

                        toast.classList.remove(
                            "show"
                        );

                    },
                    2000
                );
        }



        // =================================================
        // 개인정보 범위 상세 접기 / 보기
        // =================================================

        const toggleButtons =
            document.querySelectorAll(
                ".scope-toggle-button"
            );


        toggleButtons.forEach(
            function (button) {

                button.addEventListener(
                    "click",
                    function () {


                        const scopeCard =
                            button.closest(
                                ".scope-card"
                            );


                        if (!scopeCard) {
                            return;
                        }


                        const detail =
                            scopeCard.querySelector(
                                ".scope-detail"
                            );


                        if (!detail) {
                            return;
                        }


                        const collapsed =
                            detail.classList.toggle(
                                "collapsed"
                            );


                        button.textContent =
                            collapsed
                                ? "보기"
                                : "접기";


                        button.setAttribute(
                            "aria-expanded",
                            String(!collapsed)
                        );
                    }
                );
            }
        );



        // =================================================
        // 회원 기능 통합 안내
        // =================================================

        const integrationInfoButton =
            document.querySelector(
                "#integrationInfoButton"
            );


        if (integrationInfoButton) {

            integrationInfoButton.addEventListener(
                "click",
                function () {

                    showToast(
                        "실제 약관 및 개인정보 동의 여부는 1part 회원 기능 통합 후 연결합니다."
                    );
                }
            );
        }



        // =================================================
        // 예약 메뉴 안내
        // =================================================

        const futureMenuButtons =
            document.querySelectorAll(
                ".future-menu-button"
            );


        futureMenuButtons.forEach(
            function (button) {

                button.addEventListener(
                    "click",
                    function () {


                        const menuName =
                            button.dataset.menuName
                            || "해당 기능";


                        showToast(
                            menuName
                            + "은(는) 담당 파트 통합 후 연결할 예정입니다."
                        );
                    }
                );
            }
        );

    }
);