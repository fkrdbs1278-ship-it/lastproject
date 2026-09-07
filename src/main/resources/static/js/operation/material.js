// 상품명에서 첫 번째 내용량을 읽고 DB의 소수점 두 자리 범위를 확인
function parseMaterialCapacity(name) {
    const text = name.normalize("NFKC");
    const pattern = /(?:^|[^\d.,+\-])(\d+(?:,\d{3})*(?:\.\d+)?)\s*(밀리리터|킬로그램|리터|그램|ml|kg|g|l)(?![a-z])/i;
    const match = text.match(pattern);
    if (!match) return null;

    const unit = match[2].toLowerCase();
    const [whole, fraction = ""] = match[1].replaceAll(",", "").split(".");
    const multiplier = ["kg", "l", "킬로그램", "리터"].includes(unit) ? 1000n : 1n;
    const denominator = 10n ** BigInt(fraction.length);
    const hundredths = BigInt(whole + fraction) * multiplier * 100n;

    // 반올림으로 내용량이 달라지지 않도록 정확히 표현 가능한 값만 사용
    if (hundredths % denominator !== 0n) return null;
    const scaled = hundredths / denominator;
    if (scaled < 1n || scaled > 999999999999n) return null;

    const remainder = String(scaled % 100n).padStart(2, "0").replace(/0+$/, "");
    const quantity = String(scaled / 100n) + (remainder ? "." + remainder : "");
    const usageUnit = ["ml", "l", "밀리리터", "리터"].includes(unit) ? "ML" : "G";
    return { quantity, usageUnit };
}

// 자재 등록·수정 화면의 자동 입력과 안내 처리
document.addEventListener("DOMContentLoaded", () => {
    const name = document.getElementById("materialName");
    const quantity = document.getElementById("contentQuantity");
    const unit = document.getElementById("usageUnitCode");
    const status = document.getElementById("capacityStatus");
    if (!name || !quantity || !unit || !status) return;

    let lastName = name.value;
    let dirty = false;

    // 이름을 바꿀 때만 다시 분석하고 기존 저장값은 처음 열 때 유지
    function updateCapacity() {
        if (name.value === lastName) return;
        lastName = name.value;
        dirty = false;
        const result = parseMaterialCapacity(name.value);

        if (!result) {
            // 이전 상품의 용량이 새 상품에 잘못 저장되지 않도록 초기화
            quantity.value = "";
            unit.value = "";
            status.textContent = "용량을 확인할 수 없습니다. 내용량과 사용 단위를 직접 입력해 주세요.";
            return;
        }

        quantity.value = result.quantity;
        unit.value = result.usageUnit;
        const label = result.usageUnit === "ML" ? "ml" : "g";
        status.textContent = `첫 번째 용량 ${result.quantity}${label}를 입력했습니다. 주제품 기준인지 확인해 주세요. 묶음 수량은 계산하지 않습니다.`;
    }

    // 입력 중에는 안내만 표시하고 Tab 또는 다른 칸 이동 시 자동 반영
    name.addEventListener("input", () => {
        dirty = name.value !== lastName;
        if (dirty) status.textContent = "이름 입력 후 Tab을 누르거나 다른 입력란으로 이동해 주세요.";
    });
    name.addEventListener("change", updateCapacity);
    name.addEventListener("blur", updateCapacity);

    // Enter로 바로 저장하는 경우에도 브라우저 필수 입력 검증 전에 반영
    name.addEventListener("keydown", (event) => {
        if (event.key === "Enter" && !event.isComposing) updateCapacity();
    });

    // 사용자가 확인한 수동 입력은 그대로 저장
    function showManualMessage() {
        status.textContent = "직접 입력한 내용량과 사용 단위로 저장합니다.";
    }
    quantity.addEventListener("input", showManualMessage);
    unit.addEventListener("change", showManualMessage);

    // 스크립트로 이름만 변경된 경우도 저장 직전에 확인
    name.form.addEventListener("submit", (event) => {
        if (dirty || name.value !== lastName) updateCapacity();
        if (!name.form.checkValidity()) {
            event.preventDefault();
            name.form.reportValidity();
        }
    });
});
