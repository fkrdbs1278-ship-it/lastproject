document.addEventListener("DOMContentLoaded", () => {
    const categorySelect =
        document.querySelector("#categoryCode");

    const materialSelect =
        document.querySelector("#materialNo");

    const unitPriceInput =
        document.querySelector("#unitPrice");

    const orderQuantityInput =
        document.querySelector("#orderQuantity");

    const itemAmountElement =
        document.querySelector("#itemAmount");

    const itemAmountFormulaElement =
        document.querySelector("#itemAmountFormula");

    // 발주 품목 추가 화면이 아니면 실행하지 않습니다.
    if (
        !categorySelect ||
        !materialSelect ||
        !unitPriceInput ||
        !orderQuantityInput ||
        !itemAmountElement ||
        !itemAmountFormulaElement
    ) {
        return;
    }

    // 서버에서 전달받은 실제 자재 항목을 보관합니다.
    const materialOptions =
        Array.from(materialSelect.options)
            .filter((option) => option.value !== "")
            .map((option) => option.cloneNode(true));

    // 발주 수량과 개당 구매가로 합계를 계산합니다.
    function updateItemAmount() {
        const orderQuantity =
            Number(orderQuantityInput.value);

        const unitPrice =
            Number(unitPriceInput.value);

        if (
            !Number.isFinite(orderQuantity) ||
            !Number.isFinite(unitPrice) ||
            orderQuantity <= 0 ||
            unitPrice < 0
        ) {
            itemAmountElement.textContent = "0원";

            itemAmountFormulaElement.textContent =
                "자재와 수량을 선택해 주세요.";

            return;
        }

        const itemAmount =
            orderQuantity * unitPrice;

        itemAmountFormulaElement.textContent =
            `${orderQuantityInput.value} × ` +
            `${unitPrice.toLocaleString("ko-KR")}원`;

        itemAmountElement.textContent =
            `${itemAmount.toLocaleString("ko-KR")}원`;
    }

    // 선택한 분류에 해당하는 자재만 표시합니다.
    function renderMaterials(categoryCode) {
        materialSelect.innerHTML = "";

        const placeholder =
            document.createElement("option");

        placeholder.value = "";

        placeholder.textContent =
            categoryCode
                ? "자재를 선택해 주세요."
                : "먼저 분류를 선택해 주세요.";

        materialSelect.appendChild(placeholder);

        // 분류가 없으면 자재 선택창을 비활성화합니다.
        materialSelect.disabled = !categoryCode;

        // 선택한 분류에 해당하는 자재만 추가합니다.
        materialOptions
            .filter((option) => {
                const materialCategory =
                    option.dataset.category
                        ?.trim()
                        .toUpperCase();

                const selectedCategory =
                    categoryCode
                        ?.trim()
                        .toUpperCase();

                return materialCategory === selectedCategory;
            })
            .forEach((option) => {
                materialSelect.appendChild(
                    option.cloneNode(true)
                );
            });

        // 분류가 변경되면 구매가와 합계를 초기화합니다.
        unitPriceInput.value = "";

        updateItemAmount();
    }

    // 분류를 선택하면 관련 자재만 표시합니다.
    categorySelect.addEventListener("change", () => {
        renderMaterials(categorySelect.value);
    });

    // 자재를 선택하면 등록된 개당 구매가를 자동 입력합니다.
    materialSelect.addEventListener("change", () => {
        const selectedOption =
            materialSelect.selectedOptions[0];

        unitPriceInput.value =
            selectedOption?.dataset.price ?? "";

        updateItemAmount();
    });

    // 발주 수량이 변경되면 합계를 다시 계산합니다.
    orderQuantityInput.addEventListener(
        "input",
        updateItemAmount
    );

    // 처음에는 자재 선택창을 비활성화합니다.
    categorySelect.value = "";

    renderMaterials("");

    updateItemAmount();
});