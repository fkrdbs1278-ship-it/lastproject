// 자재 검색창의 최근 검색어와 자재명 자동완성을 표시합니다.
document.addEventListener("DOMContentLoaded", function () {
    const searchArea =
        document.querySelector(".material-search-area");

    const searchInput =
        document.getElementById("materialSearchInput");

    const recentSearchPanel =
        document.getElementById("recentSearchPanel");

    const recentSearchContent =
        document.getElementById("recentSearchContent");

    const searchSuggestionContent =
        document.getElementById(
            "searchSuggestionContent"
        );

    const searchSuggestionList =
        document.getElementById(
            "searchSuggestionList"
        );

    const searchSuggestionEmpty =
        document.getElementById(
            "searchSuggestionEmpty"
        );

    const searchForm =
        document.querySelector(".search-form");

    let materialNameCache = [];
    let materialCacheReady = false;
    let materialCachePromise = null;
    let activeSuggestionIndex = -1;
    let pendingArrowDirection = 0;
    let lastRenderedKeyword = "";
    let isComposing = false;
    let compositionRenderTimer;

    if (!searchArea
        || !searchInput
        || !recentSearchPanel
        || !recentSearchContent
        || !searchSuggestionContent
        || !searchSuggestionList
        || !searchSuggestionEmpty
        || !searchForm) {
        return;
    }

    function openRecentSearchPanel() {
        recentSearchPanel.hidden = false;

        searchInput.setAttribute(
            "aria-expanded",
            "true"
        );
    }

    function closeRecentSearchPanel() {
        recentSearchPanel.hidden = true;

        searchInput.setAttribute(
            "aria-expanded",
            "false"
        );
    }

    function showRecentSearches() {
        activeSuggestionIndex = -1;
        pendingArrowDirection = 0;
        lastRenderedKeyword = "";

        recentSearchContent.hidden = false;
        searchSuggestionContent.hidden = true;
    }

    function showSearchSuggestions() {
        recentSearchContent.hidden = true;
        searchSuggestionContent.hidden = false;
    }

    function normalizeSearchText(value) {
        return value.trim()
            .toLocaleLowerCase("ko-KR");
    }

    function findMatchingMaterialNames(keyword) {
        const normalizedKeyword =
            normalizeSearchText(keyword);

        if (normalizedKeyword === "") {
            return [];
        }

        return materialNameCache.filter(
            function (materialName) {
                return normalizeSearchText(
                    materialName
                ).includes(normalizedKeyword);
            }
        );
    }

    function renderSearchSuggestions(
        materialNames
    ) {
        activeSuggestionIndex = -1;
        searchSuggestionList.replaceChildren();

        searchSuggestionEmpty.textContent =
            "일치하는 자재가 없습니다.";

        searchSuggestionEmpty.hidden =
            materialNames.length > 0;

        materialNames.forEach(
            function (materialName, index) {
                const listItem =
                    document.createElement("li");

                const button =
                    document.createElement("button");

                button.type = "button";
                button.className =
                    "search-suggestion-button";

                button.textContent = materialName;

                button.setAttribute(
                    "aria-selected",
                    "false"
                );

                button.addEventListener(
                    "click",
                    function () {
                        searchInput.value =
                            materialName;

                        searchForm.requestSubmit();
                    }
                );

                button.addEventListener(
                    "mouseenter",
                    function () {
                        setActiveSuggestion(index);
                    }
                );

                listItem.appendChild(button);

                searchSuggestionList.appendChild(
                    listItem
                );
            }
        );

        if (materialNames.length > 0
            && pendingArrowDirection !== 0) {
            const nextIndex =
                pendingArrowDirection > 0
                    ? 0
                    : materialNames.length - 1;

            pendingArrowDirection = 0;
            setActiveSuggestion(nextIndex);
            return;
        }

        pendingArrowDirection = 0;
    }

    function setActiveSuggestion(index) {
        const suggestionButtons = Array.from(
            searchSuggestionList.querySelectorAll(
                ".search-suggestion-button"
            )
        );

        if (suggestionButtons.length === 0) {
            activeSuggestionIndex = -1;
            return;
        }

        if (index < 0) {
            activeSuggestionIndex =
                suggestionButtons.length - 1;
        } else if (index
            >= suggestionButtons.length) {
            activeSuggestionIndex = 0;
        } else {
            activeSuggestionIndex = index;
        }

        suggestionButtons.forEach(
            function (button, buttonIndex) {
                const isActive =
                    buttonIndex === activeSuggestionIndex;

                button.classList.toggle(
                    "active",
                    isActive
                );

                button.setAttribute(
                    "aria-selected",
                    String(isActive)
                );
            }
        );

        suggestionButtons[
            activeSuggestionIndex
            ].scrollIntoView({
            block: "nearest"
        });
    }

    function renderCurrentKeyword() {
        const keyword =
            searchInput.value.trim();

        if (keyword === "") {
            showRecentSearches();
            return;
        }

        lastRenderedKeyword = keyword;
        showSearchSuggestions();

        renderSearchSuggestions(
            findMatchingMaterialNames(keyword)
        );
    }

    function loadMaterialNameCache() {
        if (materialCachePromise) {
            return materialCachePromise;
        }

        materialCachePromise = fetch(
            "/admin/material/searchsuggestions"
        )
            .then(function (response) {
                if (!response.ok) {
                    throw new Error(
                        "자재명 목록 조회 실패"
                    );
                }

                return response.json();
            })
            .then(function (materialNames) {
                materialNameCache =
                    Array.isArray(materialNames)
                        ? materialNames
                        : [];

                materialCacheReady = true;

                if (!recentSearchPanel.hidden
                    && searchInput.value.trim() !== "") {
                    renderCurrentKeyword();
                }

                return materialNameCache;
            })
            .catch(function () {
                materialNameCache = [];
                materialCacheReady = true;

                if (!recentSearchPanel.hidden
                    && searchInput.value.trim() !== "") {
                    renderCurrentKeyword();
                }

                return materialNameCache;
            });

        return materialCachePromise;
    }

    function updateSearchPanel() {
        const keyword =
            searchInput.value.trim();

        openRecentSearchPanel();

        if (keyword === "") {
            showRecentSearches();
            return;
        }

        showSearchSuggestions();

        if (materialCacheReady) {
            renderCurrentKeyword();
            return;
        }

        activeSuggestionIndex = -1;

        searchSuggestionList.replaceChildren();

        searchSuggestionEmpty.textContent =
            "검색 준비 중입니다...";

        searchSuggestionEmpty.hidden = false;

        loadMaterialNameCache();
    }

    searchInput.addEventListener(
        "focus",
        updateSearchPanel
    );

    searchInput.addEventListener(
        "click",
        updateSearchPanel
    );

    searchInput.addEventListener(
        "input",
        updateSearchPanel
    );

    searchInput.addEventListener(
        "compositionstart",
        function () {
            isComposing = true;
        }
    );

    searchInput.addEventListener(
        "compositionend",
        function () {
            isComposing = false;

            window.clearTimeout(
                compositionRenderTimer
            );

            compositionRenderTimer =
                window.setTimeout(function () {
                    const keyword =
                        searchInput.value.trim();

                    if (keyword
                        !== lastRenderedKeyword
                        || pendingArrowDirection !== 0) {
                        updateSearchPanel();
                    }
                }, 0);
        }
    );

    searchInput.addEventListener(
        "keydown",
        function (event) {
            const isArrowDown =
                event.key === "ArrowDown";

            const isArrowUp =
                event.key === "ArrowUp";

            const isArrowKey =
                isArrowDown || isArrowUp;

            const keyword =
                searchInput.value.trim();

            if (isArrowKey && keyword !== "") {
                openRecentSearchPanel();
                showSearchSuggestions();

                const suggestionButtons =
                    Array.from(
                        searchSuggestionList
                            .querySelectorAll(
                                ".search-suggestion-button"
                            )
                    );

                if (event.isComposing
                    || isComposing
                    || !materialCacheReady) {
                    pendingArrowDirection =
                        isArrowDown ? 1 : -1;

                    if (!event.isComposing
                        && !isComposing) {
                        event.preventDefault();
                        loadMaterialNameCache();
                    }

                    return;
                }

                if (suggestionButtons.length === 0) {
                    return;
                }

                event.preventDefault();

                setActiveSuggestion(
                    activeSuggestionIndex
                    + (isArrowDown ? 1 : -1)
                );

                return;
            }

            if (!event.isComposing
                && !isComposing
                && event.key === "Enter"
                && activeSuggestionIndex >= 0) {
                const suggestionButtons =
                    Array.from(
                        searchSuggestionList
                            .querySelectorAll(
                                ".search-suggestion-button"
                            )
                    );

                if (suggestionButtons[
                    activeSuggestionIndex
                    ]) {
                    event.preventDefault();

                    suggestionButtons[
                        activeSuggestionIndex
                        ].click();
                }
            }
        }
    );

    searchInput.addEventListener(
        "keyup",
        function (event) {
            const isArrowKey =
                event.key === "ArrowDown"
                || event.key === "ArrowUp";

            if (!isArrowKey
                || pendingArrowDirection === 0
                || event.isComposing
                || isComposing
                || !materialCacheReady) {
                return;
            }

            if (lastRenderedKeyword
                !== searchInput.value.trim()) {
                renderCurrentKeyword();
            }

            if (pendingArrowDirection !== 0) {
                const suggestionButtons =
                    searchSuggestionList
                        .querySelectorAll(
                            ".search-suggestion-button"
                        );

                if (suggestionButtons.length > 0) {
                    const nextIndex =
                        pendingArrowDirection > 0
                            ? 0
                            : suggestionButtons.length - 1;

                    pendingArrowDirection = 0;
                    setActiveSuggestion(nextIndex);
                }
            }
        }
    );

    document.addEventListener(
        "click",
        function (event) {
            if (!searchArea.contains(event.target)) {
                closeRecentSearchPanel();
            }
        }
    );

    document.addEventListener(
        "keydown",
        function (event) {
            if (event.key === "Escape") {
                closeRecentSearchPanel();
                searchInput.blur();
            }
        }
    );

    searchForm.addEventListener(
        "submit",
        closeRecentSearchPanel
    );

    loadMaterialNameCache();
});