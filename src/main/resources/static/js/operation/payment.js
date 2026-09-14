document.addEventListener("DOMContentLoaded", function () {

    const dataContainer =
        document.getElementById("paymentTrendData");

    const svg =
        document.getElementById("paymentSalesChart");

    const linePath =
        document.getElementById("paymentChartLine");

    const areaPath =
        document.getElementById("paymentChartArea");

    const pointsGroup =
        document.getElementById("paymentChartPoints");

    const xAxis =
        document.getElementById("paymentChartXAxis");


    if (
        !dataContainer
        || !svg
        || !linePath
        || !areaPath
        || !pointsGroup
        || !xAxis
    ) {
        return;
    }


    const rows =
        Array.from(
            dataContainer.querySelectorAll("span")
        );


    const data =
        rows.map(function (row) {

            return {
                label:
                    row.dataset.label || "",

                amount:
                    Number(
                        row.dataset.amount || 0
                    )
            };

        });


    if (data.length === 0) {
        return;
    }


    const chartLeft = 18;
    const chartRight = 702;

    const chartTop = 28;
    const chartBottom = 268;


    const chartWidth =
        chartRight - chartLeft;

    const chartHeight =
        chartBottom - chartTop;


    const maxAmount =
        Math.max(
            ...data.map(
                item => item.amount
            ),
            0
        );


    const chartMax =
        makeNiceMaximum(maxAmount);


    updateYAxis(chartMax);


    const points =
        data.map(function (item, index) {

            let x;

            if (data.length === 1) {

                x =
                    chartLeft
                    + chartWidth / 2;

            } else {

                x =
                    chartLeft
                    + (
                        chartWidth
                        * index
                        / (data.length - 1)
                    );
            }


            const ratio =
                chartMax === 0
                    ? 0
                    : item.amount / chartMax;


            const y =
                chartBottom
                - (
                    ratio
                    * chartHeight
                );


            return {
                x,
                y,
                label: item.label,
                amount: item.amount
            };

        });


    drawLine(points);

    drawArea(points);

    drawPoints(points);

    drawXAxis(data);


    function drawLine(points) {

        if (points.length === 0) {
            return;
        }


        let path =
            `M ${points[0].x} ${points[0].y}`;


        for (
            let i = 1;
            i < points.length;
            i++
        ) {

            path +=
                ` L ${points[i].x} ${points[i].y}`;
        }


        linePath.setAttribute(
            "d",
            path
        );
    }


    function drawArea(points) {

        if (points.length === 0) {
            return;
        }


        let path =
            `M ${points[0].x} ${points[0].y}`;


        for (
            let i = 1;
            i < points.length;
            i++
        ) {

            path +=
                ` L ${points[i].x} ${points[i].y}`;
        }


        path +=
            ` L ${points[points.length - 1].x} ${chartBottom}`;

        path +=
            ` L ${points[0].x} ${chartBottom}`;

        path += " Z";


        areaPath.setAttribute(
            "d",
            path
        );
    }


    function drawPoints(points) {

        pointsGroup.innerHTML = "";


        // 조회 기간이 너무 길면 점을 모두 표시하지 않음
        if (points.length > 40) {
            return;
        }


        points.forEach(function (point) {

            const circle =
                document.createElementNS(
                    "http://www.w3.org/2000/svg",
                    "circle"
                );


            circle.setAttribute(
                "cx",
                point.x
            );

            circle.setAttribute(
                "cy",
                point.y
            );

            circle.setAttribute(
                "r",
                5
            );


            const title =
                document.createElementNS(
                    "http://www.w3.org/2000/svg",
                    "title"
                );


            title.textContent =
                `${point.label} : `
                + `${point.amount.toLocaleString("ko-KR")}원`;


            circle.appendChild(title);

            pointsGroup.appendChild(circle);

        });
    }


    function drawXAxis(data) {

        xAxis.innerHTML = "";


        xAxis.style.gridTemplateColumns =
            `repeat(${data.length}, minmax(0, 1fr))`;


        let showEvery = 1;


        if (data.length > 20) {
            showEvery = 5;

        } else if (data.length > 12) {
            showEvery = 3;

        } else if (data.length > 8) {
            showEvery = 2;
        }


        data.forEach(function (item, index) {

            const span =
                document.createElement("span");


            const showLabel =
                index === 0
                || index === data.length - 1
                || index % showEvery === 0;


            span.textContent =
                showLabel
                    ? item.label
                    : "";


            xAxis.appendChild(span);

        });
    }


    function updateYAxis(max) {

        const labels =
            document.querySelectorAll(
                ".chart-y-axis span"
            );


        const values = [
            max,
            max * 0.75,
            max * 0.5,
            max * 0.25,
            0
        ];


        labels.forEach(function (
            label,
            index
        ) {

            label.textContent =
                formatAxisAmount(
                    values[index]
                );

        });
    }


    function makeNiceMaximum(value) {

        if (value <= 0) {
            return 10000;
        }


        const magnitude =
            Math.pow(
                10,
                Math.floor(
                    Math.log10(value)
                )
            );


        return (
            Math.ceil(
                value / magnitude
            )
            * magnitude
        );
    }


    function formatAxisAmount(value) {

        const number =
            Math.round(value);


        if (number >= 100000000) {

            return (
                    number / 100000000
                ).toFixed(1)
                    .replace(".0", "")
                + "억";
        }


        if (number >= 10000) {

            return (
                    number / 10000
                ).toFixed(1)
                    .replace(".0", "")
                + "만";
        }


        return number.toLocaleString(
            "ko-KR"
        );
    }

});