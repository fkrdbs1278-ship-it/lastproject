document.addEventListener(
    "DOMContentLoaded",
    function () {

        const form =
                document.getElementById(
                    "customerCreateForm"
                );

        const customerNameInput =
                document.getElementById(
                    "customerName"
                );

        const phoneInput =
                document.getElementById(
                    "phone"
                );

        const submitButton =
                document.getElementById(
                    "submitButton"
                );

        const customerNameError =
                document.getElementById(
                    "customerNameClientError"
                );

        const phoneError =
                document.getElementById(
                    "phoneClientError"
                );


        if (!form) {

            return;
        }


        if (phoneInput) {

            phoneInput.addEventListener(
                "input",
                function () {

                    clearFieldError(
                        phoneInput,
                        phoneError
                    );

                    phoneInput.value =
                            formatPhoneWhileTyping(
                                extractPhoneDigits(
                                    phoneInput.value
                                )
                            );
                }
            );
        }


        if (customerNameInput) {

            customerNameInput.addEventListener(
                "input",
                function () {

                    clearFieldError(
                        customerNameInput,
                        customerNameError
                    );
                }
            );
        }


        form.addEventListener(
            "submit",
            function (event) {

                clearFieldError(
                    customerNameInput,
                    customerNameError
                );

                clearFieldError(
                    phoneInput,
                    phoneError
                );


                const customerName =
                        customerNameInput
                            ? customerNameInput.value.trim()
                            : "";


                if (!customerName) {

                    event.preventDefault();

                    showFieldError(
                        customerNameInput,
                        customerNameError,
                        "고객명을 입력해 주세요."
                    );

                    return;
                }


                if (customerName.length > 50) {

                    event.preventDefault();

                    showFieldError(
                        customerNameInput,
                        customerNameError,
                        "고객명은 50자 이하로 입력해 주세요."
                    );

                    return;
                }


                const phoneDigits =
                        extractPhoneDigits(
                            phoneInput
                                ? phoneInput.value
                                : ""
                        );

                const formattedPhone =
                        formatPhone(
                            phoneDigits
                        );


                if (!formattedPhone) {

                    event.preventDefault();

                    showFieldError(
                        phoneInput,
                        phoneError,
                        "전화번호 형식을 확인해 주세요. 예: 010-1234-5678"
                    );

                    return;
                }


                phoneInput.value =
                        formattedPhone;


                if (submitButton) {

                    submitButton.disabled =
                            true;

                    submitButton.textContent =
                            "등록 중...";
                }
            }
        );
    }
);


function extractPhoneDigits(
        phone
) {

    if (!phone) {

        return "";
    }


    return phone.replace(
        /[^0-9]/g,
        ""
    );
}


function formatPhoneWhileTyping(
        digits
) {

    if (!digits) {

        return "";
    }


    const limitedDigits =
            digits.substring(
                0,
                11
            );


    if (limitedDigits.startsWith("02")) {

        if (limitedDigits.length <= 2) {

            return limitedDigits;
        }


        if (limitedDigits.length <= 5) {

            return limitedDigits.substring(0, 2)
                    + "-"
                    + limitedDigits.substring(2);
        }


        if (limitedDigits.length <= 9) {

            return limitedDigits.substring(0, 2)
                    + "-"
                    + limitedDigits.substring(2, 5)
                    + "-"
                    + limitedDigits.substring(5);
        }


        return limitedDigits.substring(0, 2)
                + "-"
                + limitedDigits.substring(2, 6)
                + "-"
                + limitedDigits.substring(6, 10);
    }


    if (limitedDigits.length <= 3) {

        return limitedDigits;
    }


    if (limitedDigits.length <= 7) {

        return limitedDigits.substring(0, 3)
                + "-"
                + limitedDigits.substring(3);
    }


    if (limitedDigits.length <= 10) {

        return limitedDigits.substring(0, 3)
                + "-"
                + limitedDigits.substring(3, 6)
                + "-"
                + limitedDigits.substring(6);
    }


    return limitedDigits.substring(0, 3)
            + "-"
            + limitedDigits.substring(3, 7)
            + "-"
            + limitedDigits.substring(7);
}


function formatPhone(
        digits
) {

    if (!digits) {

        return null;
    }


    if (digits.startsWith("02")) {

        if (digits.length === 9) {

            return digits.substring(0, 2)
                    + "-"
                    + digits.substring(2, 5)
                    + "-"
                    + digits.substring(5);
        }


        if (digits.length === 10) {

            return digits.substring(0, 2)
                    + "-"
                    + digits.substring(2, 6)
                    + "-"
                    + digits.substring(6);
        }


        return null;
    }


    if (digits.length === 11) {

        return digits.substring(0, 3)
                + "-"
                + digits.substring(3, 7)
                + "-"
                + digits.substring(7);
    }


    if (digits.length === 10) {

        return digits.substring(0, 3)
                + "-"
                + digits.substring(3, 6)
                + "-"
                + digits.substring(6);
    }


    return null;
}


function showFieldError(
        input,
        errorElement,
        message
) {

    if (input) {

        input.classList.add(
            "input-error"
        );

        input.focus();
    }


    if (errorElement) {

        errorElement.textContent =
                message;

        errorElement.hidden =
                false;
    }
}


function clearFieldError(
        input,
        errorElement
) {

    if (input) {

        input.classList.remove(
            "input-error"
        );
    }


    if (errorElement) {

        errorElement.textContent =
                "";

        errorElement.hidden =
                true;
    }
}
