document.addEventListener(
    "DOMContentLoaded",
    () => {

        /* 요소 */

        const heroSlider =
            document.querySelector(
                "#heroSlider"
            );

        const slides =
            document.querySelectorAll(
                ".hero-background"
            );

        const dots =
            document.querySelectorAll(
                ".hero-dot"
            );

        const prevButton =
            document.querySelector(
                "#heroPrev"
            );

        const nextButton =
            document.querySelector(
                "#heroNext"
            );


        if (
            !heroSlider ||
            slides.length === 0
        ) {

            return;
        }


        /* 상태 */

        let currentIndex = 0;

        let timer = null;


        /*
         * 4초마다 이미지 변경
         */
        const AUTO_SLIDE_TIME =
            4000;


        /* 현재 Slide 표시 */

        function showSlide(index) {

            /*
             * 마지막 → 처음
             * 처음 → 마지막
             *
             * 순환 처리
             */

            if (index < 0) {

                index =
                    slides.length - 1;

            } else if (
                index >= slides.length
            ) {

                index = 0;
            }


            /*
             * 기존 active 제거
             */

            slides.forEach(
                (slide) => {

                    slide.classList.remove(
                        "active"
                    );
                }
            );


            dots.forEach(
                (dot) => {

                    dot.classList.remove(
                        "active"
                    );
                }
            );


            /*
             * 현재 이미지 활성화
             */

            slides[index]
                .classList
                .add(
                    "active"
                );


            /*
             * 현재 Dot 활성화
             */

            if (dots[index]) {

                dots[index]
                    .classList
                    .add(
                        "active"
                    );
            }


            currentIndex =
                index;
        }


        /* 다음 */

        function nextSlide() {

            showSlide(
                currentIndex + 1
            );
        }


        /* 이전 */

        function prevSlide() {

            showSlide(
                currentIndex - 1
            );
        }


        /* 자동 슬라이드 시작 */

        function startAutoSlide() {

            stopAutoSlide();


            /*
             * 이미지가 한 장뿐이면
             * 자동 슬라이드 필요 없음
             */

            if (slides.length <= 1) {

                return;
            }


            timer =
                setInterval(
                    nextSlide,
                    AUTO_SLIDE_TIME
                );
        }


        /* 자동 슬라이드 중지 */

        function stopAutoSlide() {

            if (timer !== null) {

                clearInterval(
                    timer
                );

                timer = null;
            }
        }


        /* 다음 버튼 */

        if (nextButton) {

            nextButton.addEventListener(
                "click",
                () => {

                    nextSlide();

                    startAutoSlide();
                }
            );
        }


        /* 이전 버튼 */

        if (prevButton) {

            prevButton.addEventListener(
                "click",
                () => {

                    prevSlide();

                    startAutoSlide();
                }
            );
        }


        /* Dot 클릭 */

        dots.forEach(
            (dot) => {

                dot.addEventListener(
                    "click",
                    () => {

                        const index =
                            Number(
                                dot.dataset.slide
                            );


                        if (
                            Number.isNaN(index)
                        ) {

                            return;
                        }


                        showSlide(index);

                        startAutoSlide();
                    }
                );
            }
        );


        /* 마우스를 올리면 자동재생 정지 */

        heroSlider.addEventListener(
            "mouseenter",
            stopAutoSlide
        );


        /* 마우스가 나가면 자동재생 시작 */

        heroSlider.addEventListener(
            "mouseleave",
            startAutoSlide
        );


        /* 초기 실행 */

        showSlide(0);

        startAutoSlide();

    }
);