// 관리자 목록 검색창의 최근 검색어와 자동완성을 공통으로 처리합니다.
document.addEventListener("DOMContentLoaded", function () {
    const searchAreas = document.querySelectorAll(
        "[data-list-autocomplete]"
    );

    searchAreas.forEach(function (searchArea) {
        initializeListAutocomplete(searchArea);
    });
});

function initializeListAutocomplete(searchArea) {
    const searchForm = searchArea.querySelector(
        '[data-search-role="form"]'
    );
    const searchInput = searchArea.querySelector(
        '[data-search-role="input"]'
    );
    const searchPanel = searchArea.querySelector(
        '[data-search-role="panel"]'
    );
    const recentContent = searchArea.querySelector(
        '[data-search-role="recent-content"]'
    );
    const suggestionContent = searchArea.querySelector(
        '[data-search-role="suggestion-content"]'
    );
    const suggestionList = searchArea.querySelector(
        '[data-search-role="suggestion-list"]'
    );
    const suggestionEmpty = searchArea.querySelector(
        '[data-search-role="suggestion-empty"]'
    );
    const suggestionUrl = searchArea.dataset.suggestionUrl;

    if (!searchForm
        || !searchInput
        || !searchPanel
        || !recentContent
        || !suggestionContent
        || !suggestionList
        || !suggestionEmpty
        || !suggestionUrl) {
        return;
    }

    const noResultMessage =
        suggestionEmpty.textContent.trim()
        || "일치하는 검색 결과가 없습니다.";

    let suggestionCache = [];
    let cacheReady = false;
    let cachePromise = null;
    let activeSuggestionIndex = -1;
    let pendingArrowDirection = 0;
    let lastRenderedKeyword = "";
    let isComposing = false;
    let compositionRenderTimer;

    function openSearchPanel() {
        searchPanel.hidden = false;
        searchInput.setAttribute("aria-expanded", "true");
    }

    function closeSearchPanel() {
        searchPanel.hidden = true;
        searchInput.setAttribute("aria-expanded", "false");
    }

    function showRecentSearches() {
        activeSuggestionIndex = -1;
        pendingArrowDirection = 0;
        lastRenderedKeyword = "";
        recentContent.hidden = false;
        suggestionContent.hidden = true;
    }

    function showSuggestions() {
        recentContent.hidden = true;
        suggestionContent.hidden = false;
    }

    function normalizeSearchText(value) {
        return value.trim().toLocaleLowerCase("ko-KR");
    }

    function findMatchingSuggestions(keyword) {
        const normalizedKeyword =
            normalizeSearchText(keyword);

        if (normalizedKeyword === "") {
            return [];
        }

        return suggestionCache.filter(
            function (suggestion) {
                return normalizeSearchText(suggestion)
                    .includes(normalizedKeyword);
            }
        );
    }

    function setActiveSuggestion(index) {
        const suggestionButtons = Array.from(
            suggestionList.querySelectorAll(
                ".list-suggestion-button"
            )
        );

        if (suggestionButtons.length === 0) {
            activeSuggestionIndex = -1;
            return;
        }

        if (index < 0) {
            activeSuggestionIndex =
                suggestionButtons.length - 1;
        } else if (index >= suggestionButtons.length) {
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

    function renderSuggestions(suggestions) {
        activeSuggestionIndex = -1;
        suggestionList.replaceChildren();

        suggestionEmpty.textContent =
            noResultMessage;

        suggestionEmpty.hidden =
            suggestions.length > 0;

        suggestions.forEach(
            function (suggestion, index) {
                const listItem =
                    document.createElement("li");

                const button =
                    document.createElement("button");

                button.type = "button";
                button.className =
                    "list-suggestion-button";

                button.textContent = suggestion;

                button.setAttribute(
                    "aria-selected",
                    "false"
                );

                button.addEventListener(
                    "click",
                    function () {
                        searchInput.value = suggestion;
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
                suggestionList.appendChild(listItem);
            }
        );

        if (suggestions.length > 0
            && pendingArrowDirection !== 0) {
            const nextIndex =
                pendingArrowDirection > 0
                    ? 0
                    : suggestions.length - 1;

            pendingArrowDirection = 0;
            setActiveSuggestion(nextIndex);
            return;
        }

        pendingArrowDirection = 0;
    }

    function renderCurrentKeyword() {
        const keyword = searchInput.value.trim();

        if (keyword === "") {
            showRecentSearches();
            return;
        }

        lastRenderedKeyword = keyword;
        showSuggestions();

        renderSuggestions(
            findMatchingSuggestions(keyword)
        );
    }

    // 페이지 진입 시 검색 후보를 한 번만 조회
    function loadSuggestionCache() {
        if (cachePromise) {
            return cachePromise;
        }

        cachePromise = fetch(suggestionUrl)
            .then(function (response) {
                if (!response.ok) {
                    throw new Error(
                        "자동완성 목록 조회 실패"
                    );
                }

                return response.json();
            })
            .then(function (suggestions) {
                suggestionCache =
                    Array.isArray(suggestions)
                        ? suggestions
                        : [];

                cacheReady = true;

                if (!searchPanel.hidden
                    && searchInput.value.trim() !== "") {
                    renderCurrentKeyword();
                }

                return suggestionCache;
            })
            .catch(function () {
                suggestionCache = [];
                cacheReady = true;

                if (!searchPanel.hidden
                    && searchInput.value.trim() !== "") {
                    renderCurrentKeyword();
                }

                return suggestionCache;
            });

        return cachePromise;
    }

    function updateSearchPanel() {
        const keyword = searchInput.value.trim();

        openSearchPanel();

        if (keyword === "") {
            showRecentSearches();
            return;
        }

        showSuggestions();

        if (cacheReady) {
            renderCurrentKeyword();
            return;
        }

        activeSuggestionIndex = -1;
        suggestionList.replaceChildren();

        suggestionEmpty.textContent =
            "검색 준비 중입니다...";

        suggestionEmpty.hidden = false;

        loadSuggestionCache();
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

            /*
             * 한글 조합 종료 후 발생하는 input 이벤트와
             * 중복으로 목록을 그리지 않도록 확인합니다.
             */
            compositionRenderTimer =
                window.setTimeout(function () {
                    const keyword =
                        searchInput.value.trim();

                    if (keyword !== lastRenderedKeyword
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
                openSearchPanel();
                showSuggestions();

                const suggestionButtons =
                    Array.from(
                        suggestionList.querySelectorAll(
                            ".list-suggestion-button"
                        )
                    );

                if (event.isComposing
                    || isComposing
                    || !cacheReady) {
                    pendingArrowDirection =
                        isArrowDown ? 1 : -1;

                    if (!event.isComposing
                        && !isComposing) {
                        event.preventDefault();
                        loadSuggestionCache();
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
                        suggestionList.querySelectorAll(
                            ".list-suggestion-button"
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

    /*
     * 한글 입력이 화살표 키로 끝난 경우
     * 첫 번째 화살표 입력을 바로 반영합니다.
     */
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
                || !cacheReady) {
                return;
            }

            if (lastRenderedKeyword
                !== searchInput.value.trim()) {
                renderCurrentKeyword();
            }

            if (pendingArrowDirection !== 0) {
                const suggestionButtons =
                    suggestionList.querySelectorAll(
                        ".list-suggestion-button"
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
                closeSearchPanel();
            }
        }
    );

    document.addEventListener(
        "keydown",
        function (event) {
            if (event.key === "Escape"
                && !searchPanel.hidden) {
                closeSearchPanel();
                searchInput.blur();
            }
        }
    );

    searchForm.addEventListener(
        "submit",
        closeSearchPanel
    );

    // 검색창을 사용하기 전에 후보 목록 준비
    loadSuggestionCache();
}