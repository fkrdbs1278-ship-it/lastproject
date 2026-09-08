document.addEventListener(
    "DOMContentLoaded",
    function () {


        // =================================================
        // Toast
        // =================================================

        const toast =
            document.querySelector(
                "#activityToast"
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
        // 예약 / 리뷰 연동 정보
        // =================================================

        const infoButtons =
            document.querySelectorAll(
                ".info-button"
            );


        infoButtons.forEach(
            function (button) {

                button.addEventListener(
                    "click",
                    function () {


                        const message =
                            button.dataset.message;


                        if (!message) {

                            return;
                        }


                        showToast(
                            message
                        );
                    }
                );
            }
        );



        // =================================================
        // 아직 연결되지 않은 메뉴
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