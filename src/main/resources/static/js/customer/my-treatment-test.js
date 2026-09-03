document.addEventListener(
    "DOMContentLoaded",
    function () {


        // =================================================
        // 요소 조회
        // =================================================

        const historyList =
            document.querySelector(
                ".history-list"
            );


        const sortSelect =
            document.querySelector(
                "#historySort"
            );


        const toast =
            document.querySelector(
                "#testToast"
            );



        // =================================================
        // Toast
        // =================================================

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
                    1800
                );
        }



        // =================================================
        // 아직 통합되지 않은 마이페이지 메뉴
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



        // =================================================
        // 시술이력이 없는 경우
        // =================================================

        if (!historyList) {

            return;
        }



        // =================================================
        // 금액 천 단위 콤마
        // =================================================

        const priceElements =
            document.querySelectorAll(
                ".price-value"
            );


        priceElements.forEach(
            function (priceElement) {

                const rawPrice =
                    priceElement.dataset.price;


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


                priceElement.textContent =
                    new Intl.NumberFormat(
                        "ko-KR"
                    ).format(
                        price
                    );
            }
        );



        // =================================================
        // 상세 보기 / 접기
        // =================================================

        const toggleButtons =
            document.querySelectorAll(
                ".toggle-detail-button"
            );


        toggleButtons.forEach(
            function (button) {

                button.addEventListener(
                    "click",
                    function () {


                        const historyCard =
                            button.closest(
                                ".history-card"
                            );


                        if (!historyCard) {

                            return;
                        }


                        const historyDetail =
                            historyCard.querySelector(
                                ".history-detail"
                            );


                        if (!historyDetail) {

                            return;
                        }


                        const collapsed =
                            historyDetail.classList.toggle(
                                "collapsed"
                            );


                        if (collapsed) {

                            button.textContent =
                                "상세 보기";


                            button.setAttribute(
                                "aria-expanded",
                                "false"
                            );

                        } else {

                            button.textContent =
                                "상세 접기";


                            button.setAttribute(
                                "aria-expanded",
                                "true"
                            );
                        }

                    }
                );
            }
        );



        // =================================================
        // 날짜 변환
        // =================================================

        function convertDate(
            dateString
        ) {

            if (!dateString) {

                return 0;
            }


            const date =
                new Date(
                    dateString + "T00:00:00"
                );


            const timestamp =
                date.getTime();


            if (
                Number.isNaN(
                    timestamp
                )
            ) {

                return 0;
            }


            return timestamp;
        }



        // =================================================
        // 최신순 / 오래된순 정렬
        // =================================================

        if (sortSelect) {

            sortSelect.addEventListener(
                "change",
                function () {


                    const historyCards =
                        Array.from(
                            historyList.querySelectorAll(
                                ".history-card"
                            )
                        );


                    const sortType =
                        sortSelect.value;


                    historyCards.sort(
                        function (
                            cardA,
                            cardB
                        ) {


                            const dateA =
                                convertDate(
                                    cardA.dataset.treatmentDate
                                );


                            const dateB =
                                convertDate(
                                    cardB.dataset.treatmentDate
                                );


                            // 오래된순
                            if (
                                sortType === "oldest"
                            ) {

                                return dateA - dateB;
                            }


                            // 기본 최신순
                            return dateB - dateA;
                        }
                    );


                    historyCards.forEach(
                        function (card) {

                            historyList.appendChild(
                                card
                            );
                        }
                    );


                    if (
                        sortType === "oldest"
                    ) {

                        showToast(
                            "오래된 시술부터 정렬했습니다."
                        );

                    } else {

                        showToast(
                            "최근 시술부터 정렬했습니다."
                        );
                    }

                }
            );
        }

    }
);