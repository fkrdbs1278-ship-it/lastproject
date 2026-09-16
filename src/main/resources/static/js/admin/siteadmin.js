document.addEventListener("DOMContentLoaded", function () {

    // =====================================================
    // 공통 요소
    // =====================================================

    const siteSettingForm =
        document.getElementById("siteSettingForm");

    const heroTitleInput =
        document.getElementById("heroTitleInput");

    const heroDescriptionInput =
        document.getElementById("heroDescriptionInput");

    const heroImageInput =
        document.getElementById("heroImageInput");

    const previewHero =
        document.getElementById("previewHero");

    const previewHeroTitle =
        document.getElementById("previewHeroTitle");

    const previewHeroDescription =
        document.getElementById("previewHeroDescription");

    const previewHeroImage =
        document.getElementById("previewHeroImage");

    const previewHeroSlides =
        Array.from(
            document.querySelectorAll(
                ".preview-hero-background"
            )
        );

    const previewHeroDots =
        Array.from(
            document.querySelectorAll(
                ".preview-hero-dot"
            )
        );

    const previewHeroPrev =
        document.getElementById("previewHeroPrev");

    const previewHeroNext =
        document.getElementById("previewHeroNext");


    // =====================================================
    // 헤어스타일 영역
    // =====================================================

    const styleVisibleInput =
        document.getElementById("styleVisibleInput");

    const styleTitleInput =
        document.getElementById("styleTitleInput");

    const styleDescriptionInput =
        document.getElementById("styleDescriptionInput");

    const previewStyleSection =
        document.getElementById("previewStyleSection");

    const previewStyleTitle =
        document.getElementById("previewStyleTitle");

    const previewStyleDescription =
        document.getElementById("previewStyleDescription");


    // =====================================================
    // 버튼 / 토스트
    // =====================================================

    const resetButton =
        document.getElementById("resetButton");

    const toast =
        document.getElementById("siteadminToast");

    let toastTimer;


    function showToast(message, isError = false) {

        if (!toast) {
            return;
        }

        clearTimeout(toastTimer);

        toast.textContent = message;
        toast.classList.toggle("error", isError);
        toast.classList.add("show");

        toastTimer = setTimeout(function () {
            toast.classList.remove("show");
        }, 2400);
    }


    // redirect 후 서버에서 전달한 저장 결과가 있으면 표시
    if (
        toast
        && toast.textContent.trim() !== ""
    ) {
        showToast(
            toast.textContent.trim(),
            toast.classList.contains("error")
        );
    }


    // =====================================================
    // Hero 슬라이더
    // 사용자 메인과 같은 4장 흐름
    // =====================================================

    let currentHeroSlide = 0;
    let heroSlideTimer = null;

    const heroSlideInterval = 5000;


    function showHeroSlide(index) {

        if (previewHeroSlides.length === 0) {
            return;
        }

        currentHeroSlide =
            (
                index
                + previewHeroSlides.length
            )
            % previewHeroSlides.length;


        previewHeroSlides.forEach(
            function (slide, slideIndex) {

                slide.classList.toggle(
                    "active",
                    slideIndex === currentHeroSlide
                );

            }
        );


        previewHeroDots.forEach(
            function (dot, dotIndex) {

                dot.classList.toggle(
                    "active",
                    dotIndex === currentHeroSlide
                );

            }
        );
    }


    function stopHeroSlider() {

        if (heroSlideTimer !== null) {

            clearInterval(heroSlideTimer);
            heroSlideTimer = null;
        }
    }


    function startHeroSlider() {

        stopHeroSlider();

        if (previewHeroSlides.length <= 1) {
            return;
        }

        heroSlideTimer =
            setInterval(function () {

                showHeroSlide(
                    currentHeroSlide + 1
                );

            }, heroSlideInterval);
    }


    function restartHeroSlider() {

        startHeroSlider();
    }


    if (previewHeroPrev) {

        previewHeroPrev.addEventListener(
            "click",
            function () {

                showHeroSlide(
                    currentHeroSlide - 1
                );

                restartHeroSlider();
            }
        );
    }


    if (previewHeroNext) {

        previewHeroNext.addEventListener(
            "click",
            function () {

                showHeroSlide(
                    currentHeroSlide + 1
                );

                restartHeroSlider();
            }
        );
    }


    previewHeroDots.forEach(
        function (dot) {

            dot.addEventListener(
                "click",
                function () {

                    const targetIndex =
                        Number(dot.dataset.slide);

                    showHeroSlide(targetIndex);
                    restartHeroSlider();
                }
            );
        }
    );


    if (previewHero) {

        previewHero.addEventListener(
            "mouseenter",
            stopHeroSlider
        );

        previewHero.addEventListener(
            "mouseleave",
            startHeroSlider
        );
    }


    showHeroSlide(0);
    startHeroSlider();


    // =====================================================
    // 메인 제목 / 설명 실시간 미리보기
    // =====================================================

    if (heroTitleInput && previewHeroTitle) {

        heroTitleInput.addEventListener(
            "input",
            function () {

                previewHeroTitle.textContent =
                    heroTitleInput.value;
            }
        );
    }


    if (
        heroDescriptionInput
        && previewHeroDescription
    ) {

        heroDescriptionInput.addEventListener(
            "input",
            function () {

                previewHeroDescription.textContent =
                    heroDescriptionInput.value;
            }
        );
    }


    // =====================================================
    // 메인 이미지 실시간 미리보기
    // 1번 슬라이드만 교체
    // =====================================================

    const initialHeroImage =
        previewHeroImage
            ? previewHeroImage.src
            : "/images/hero/hero1.jpg";


    if (heroImageInput && previewHeroImage) {

        heroImageInput.addEventListener(
            "change",
            function () {

                const file =
                    heroImageInput.files[0];


                if (!file) {
                    return;
                }


                if (
                    !file.type
                    || !file.type.startsWith("image/")
                ) {

                    heroImageInput.value = "";

                    showToast(
                        "이미지 파일만 선택할 수 있습니다.",
                        true
                    );

                    return;
                }


                const maxSize =
                    10 * 1024 * 1024;

                if (file.size > maxSize) {

                    heroImageInput.value = "";

                    showToast(
                        "메인 이미지는 10MB 이하만 업로드할 수 있습니다.",
                        true
                    );

                    return;
                }


                const reader =
                    new FileReader();


                reader.onload =
                    function (event) {

                        previewHeroImage.src =
                            event.target.result;

                        showHeroSlide(0);
                        restartHeroSlider();

                        showToast(
                            "1번 슬라이드 미리보기에 이미지를 적용했습니다."
                        );
                    };


                reader.readAsDataURL(file);
            }
        );
    }


    // =====================================================
    // 헤어스타일 영역
    // =====================================================

    if (
        styleVisibleInput
        && previewStyleSection
    ) {

        styleVisibleInput.addEventListener(
            "change",
            function () {

                previewStyleSection.style.display =
                    styleVisibleInput.checked
                        ? ""
                        : "none";
            }
        );
    }


    if (
        styleTitleInput
        && previewStyleTitle
    ) {

        styleTitleInput.addEventListener(
            "input",
            function () {

                previewStyleTitle.textContent =
                    styleTitleInput.value;
            }
        );
    }


    if (
        styleDescriptionInput
        && previewStyleDescription
    ) {

        styleDescriptionInput.addEventListener(
            "input",
            function () {

                previewStyleDescription.textContent =
                    styleDescriptionInput.value;
            }
        );
    }


    // =====================================================
    // 초기화
    // =====================================================

    const defaultValues = {

        heroTitle:
            "나에게 어울리는 스타일을\n찾아보세요.",

        heroDescription:
            "원하는 시술과 헤어스타일을 확인하고\n편리하게 예약 서비스를 이용해보세요.",

        styleTitle:
            "헤어스타일 둘러보기",

        styleDescription:
            "다양한 스타일을 확인하고\n원하는 헤어스타일을 찾아보세요."
    };


    if (resetButton) {

        resetButton.addEventListener(
            "click",
            function () {

                heroTitleInput.value =
                    defaultValues.heroTitle;

                heroDescriptionInput.value =
                    defaultValues.heroDescription;

                styleTitleInput.value =
                    defaultValues.styleTitle;

                styleDescriptionInput.value =
                    defaultValues.styleDescription;

                styleVisibleInput.checked = true;


                previewHeroTitle.textContent =
                    defaultValues.heroTitle;

                previewHeroDescription.textContent =
                    defaultValues.heroDescription;

                previewStyleTitle.textContent =
                    defaultValues.styleTitle;

                previewStyleDescription.textContent =
                    defaultValues.styleDescription;

                previewStyleSection.style.display = "";


                // 선택 중이던 파일은 취소하고
                // 현재 DB에 저장되어 있던 1번 이미지를 다시 표시
                heroImageInput.value = "";
                previewHeroImage.src =
                    initialHeroImage;

                showHeroSlide(0);
                restartHeroSlider();


                showToast(
                    "미리보기 설정을 초기화했습니다."
                );
            }
        );
    }


    // =====================================================
    // 제출 직전 파일 최종 검증
    // =====================================================

    if (siteSettingForm) {

        siteSettingForm.addEventListener(
            "submit",
            function (event) {

                const file =
                    heroImageInput
                        ? heroImageInput.files[0]
                        : null;


                if (
                    file
                    && (
                        !file.type
                        || !file.type.startsWith("image/")
                    )
                ) {

                    event.preventDefault();

                    showToast(
                        "이미지 파일만 업로드할 수 있습니다.",
                        true
                    );

                    return;
                }


                if (
                    file
                    && file.size > 10 * 1024 * 1024
                ) {

                    event.preventDefault();

                    showToast(
                        "메인 이미지는 10MB 이하만 업로드할 수 있습니다.",
                        true
                    );
                }
            }
        );
    }

});