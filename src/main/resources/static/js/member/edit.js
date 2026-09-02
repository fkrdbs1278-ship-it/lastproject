document.addEventListener("DOMContentLoaded", () => {

    /* =========================================================
       요소
    ========================================================= */

    const phone =
        document.querySelector("#phone");

    const birthDate =
        document.querySelector("#birthDate");


    /* =========================================================
       전화번호 자동 하이픈
    ========================================================= */

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

});