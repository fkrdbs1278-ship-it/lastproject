document.addEventListener(
    "DOMContentLoaded",
    function () {


        // =================================================
        // 금액 천 단위 콤마
        // =================================================

        const moneyValues =
            document.querySelectorAll(
                ".money-value"
            );


        moneyValues.forEach(
            function (element) {


                const rawValue =
                    element.dataset.price;


                if (
                    rawValue === undefined
                    || rawValue === null
                    || rawValue === ""
                ) {

                    return;
                }


                const numberValue =
                    Number(
                        rawValue
                    );


                if (
                    Number.isNaN(
                        numberValue
                    )
                ) {

                    return;
                }


                element.textContent =
                    new Intl.NumberFormat(
                        "ko-KR"
                    )
                        .format(
                            numberValue
                        );
            }
        );



        // =================================================
        // 실제 DB 변경 전 확인
        // =================================================

        const dbChangeForms =
            document.querySelectorAll(
                ".db-change-form"
            );


        dbChangeForms.forEach(
            function (form) {


                form.addEventListener(
                    "submit",
                    function (event) {


                        const message =
                            form.dataset.confirm;


                        if (!message) {

                            return;
                        }


                        const confirmed =
                            window.confirm(
                                message
                            );


                        if (!confirmed) {

                            event.preventDefault();
                        }

                    }
                );

            }
        );

    }
);