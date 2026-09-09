document.addEventListener("DOMContentLoaded", function () {

    // =====================================================
    // 메인 화면
    // =====================================================

    const heroTitleInput =
        document.getElementById("heroTitleInput");

    const heroDescriptionInput =
        document.getElementById("heroDescriptionInput");

    const heroImageInput =
        document.getElementById("heroImageInput");

    const previewHeroTitle =
        document.getElementById("previewHeroTitle");

    const previewHeroDescription =
        document.getElementById("previewHeroDescription");

    const previewHeroImage =
        document.getElementById("previewHeroImage");


    // =====================================================
    // 서비스 안내 영역
    // =====================================================

    const serviceVisibleInput =
        document.getElementById("serviceVisibleInput");

    const serviceTitleInput =
        document.getElementById("serviceTitleInput");

    const previewServiceSection =
        document.getElementById("previewServiceSection");

    const previewServiceTitle =
        document.getElementById("previewServiceTitle");


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
    // 버튼
    // =====================================================

    const resetButton =
        document.getElementById("resetButton");

    const saveButton =
        document.getElementById("saveButton");


    // =====================================================
    // 기본값
    // =====================================================

    const defaultValues = {

        heroTitle:
            "나에게 어울리는 스타일을\n찾아보세요.",

        heroDescription:
            "원하는 시술과 헤어스타일을 확인하고\n편리하게 예약 서비스를 이용해보세요.",

        serviceTitle:
            "서비스 안내",

        styleTitle:
            "헤어스타일 둘러보기",

        styleDescription:
            "다양한 스타일을 확인하고\n원하는 헤어스타일을 찾아보세요.",

        heroImage:
            "/images/hero/hero1.jpg"
    };


    // =====================================================
    // 토스트 메시지 생성
    // =====================================================

    const toast =
        document.createElement("div");

    toast.className =
        "siteadmin-toast";

    document.body.appendChild(toast);


    let toastTimer;


    // 토스트 메시지 표시
    function showToast(message) {

        clearTimeout(toastTimer);

        toast.textContent = message;

        toast.classList.add("show");


        toastTimer = setTimeout(function () {

            toast.classList.remove("show");

        }, 2000);
    }


    // =====================================================
    // 메인 제목 실시간 미리보기
    // =====================================================

    heroTitleInput.addEventListener("input", function () {

        previewHeroTitle.textContent =
            heroTitleInput.value;

    });


    // =====================================================
    // 메인 설명 실시간 미리보기
    // =====================================================

    heroDescriptionInput.addEventListener("input", function () {

        previewHeroDescription.textContent =
            heroDescriptionInput.value;

    });


    // =====================================================
    // 메인 이미지 실시간 미리보기
    // =====================================================

    heroImageInput.addEventListener("change", function () {

        const file =
            heroImageInput.files[0];


        if (!file) {
            return;
        }


        const reader =
            new FileReader();


        reader.onload = function (event) {

            previewHeroImage.src =
                event.target.result;

        };


        reader.readAsDataURL(file);

    });


    // =====================================================
    // 서비스 안내 노출 / 숨김
    // =====================================================

    serviceVisibleInput.addEventListener("change", function () {

        previewServiceSection.style.display =
            serviceVisibleInput.checked
                ? ""
                : "none";

    });


    // =====================================================
    // 서비스 안내 제목 실시간 변경
    // =====================================================

    serviceTitleInput.addEventListener("input", function () {

        previewServiceTitle.textContent =
            serviceTitleInput.value;

    });


    // =====================================================
    // 헤어스타일 영역 노출 / 숨김
    // =====================================================

    styleVisibleInput.addEventListener("change", function () {

        previewStyleSection.style.display =
            styleVisibleInput.checked
                ? ""
                : "none";

    });


    // =====================================================
    // 헤어스타일 제목 실시간 변경
    // =====================================================

    styleTitleInput.addEventListener("input", function () {

        previewStyleTitle.textContent =
            styleTitleInput.value;

    });


    // =====================================================
    // 헤어스타일 설명 실시간 변경
    // =====================================================

    styleDescriptionInput.addEventListener("input", function () {

        previewStyleDescription.textContent =
            styleDescriptionInput.value;

    });


    // =====================================================
    // 초기화
    // =====================================================

    resetButton.addEventListener("click", function () {

        // 입력값 초기화
        heroTitleInput.value =
            defaultValues.heroTitle;

        heroDescriptionInput.value =
            defaultValues.heroDescription;

        serviceTitleInput.value =
            defaultValues.serviceTitle;

        styleTitleInput.value =
            defaultValues.styleTitle;

        styleDescriptionInput.value =
            defaultValues.styleDescription;


        // 노출 체크 초기화
        serviceVisibleInput.checked = true;
        styleVisibleInput.checked = true;


        // 미리보기 초기화
        previewHeroTitle.textContent =
            defaultValues.heroTitle;

        previewHeroDescription.textContent =
            defaultValues.heroDescription;

        previewServiceTitle.textContent =
            defaultValues.serviceTitle;

        previewStyleTitle.textContent =
            defaultValues.styleTitle;

        previewStyleDescription.textContent =
            defaultValues.styleDescription;


        previewServiceSection.style.display = "";
        previewStyleSection.style.display = "";


        // 이미지 초기화
        previewHeroImage.src =
            defaultValues.heroImage;

        heroImageInput.value = "";


        // 토스트 표시
        showToast(
            "미리보기 설정을 초기화했습니다."
        );

    });


    // =====================================================
    // 변경사항 저장
    // 현재는 DB 저장 전 단계
    // =====================================================

    saveButton.addEventListener("click", function () {

        showToast(
            "변경사항이 미리보기에 적용되었습니다."
        );

    });

});