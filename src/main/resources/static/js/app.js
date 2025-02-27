webix.ui({
    id: "root",
    rows: [
        { view: "toolbar", elements: [{ view: "label", label: "Coffee Machine Control" }] },
        {
            cols: [
                {
                    view: "sidebar",
                    id: "sidebar",
                    data: [{ id: "device_list", icon: "wxi-folder", value: "Device List" }],
                    on: {
                        onItemClick: function(id) {
                            if (id === "device_list") loadDeviceList();
                        }
                    }
                },
                {
                    view: "resizer"
                },
                {
                    id: "main",

                    rows: [
                        { id: "main_template",template: "Выберите пункт в меню" }
                    ]
                }
            ]
        }
    ]
});

function loadDeviceList() {
    webix.ajax().get("/jetinno_api/listMachines").then(function(data) {
        const machines = data.json();
        const main = $$("main");

        main.getChildViews().forEach(view => main.removeView(view));

        $$("main").addView({
            id: "device_table",
            view: "datatable",
            autoConfig: true,
            css:"webix_header_boreder webix_data_border",

            columns: [
                { id: "vmcNumber", header: "ID", width: 80 },
                { id: "address", header: "Address", fillspace: true, template: obj => obj.address || "Unknown" },
                { id: "active", header: "IsActive", width: 100 },
                {
                    id: "image",
                    header: "Image",
                    template: obj => obj.image ? `<img src='${obj.image}' width='50' height='50'>` : "No Image",
                    width: 150
                }
            ],
            data: machines,
            on: {
                onItemClick: function(id) {
                    const machine = this.getItem(id);
                    loadMachineInfo(machine);
                }
            }
        });
        $$("device_table").attachEvent("onItemClick", function(id) {
            console.log("Cliked on row")
        });
    });
}

function loadMachineInfo(machine) {
    const main = $$("main");

    main.getChildViews().forEach(view => main.removeView(view));


    $$("main").addView({
        rows: [
            { cols :
                [
                    { view: "button", value: "Ошибки", click: () => loadFaults(machine.vmcNumber) },
                    { view: "button", value: "Заказы", click: () => loadOrders(machine.vmcNumber) },
                    { view: "button", value: "Остатки", click: () => loadSupply(machine.vmcNumber) }
                ]
            },
            {
                view: "datatable",
                id: "machineTable",
                columns: [
                    { id: "info", header: "Информация", fillspace: true }
//                    { template: `Информация о машине ID: ${machine.vmcNumber}`, type: "header" },
//                    { template: `Версия ПО: ${machine.softwareVersion || "N/A"}` },
//                    { template: `Версия IO: ${machine.ioVersion || "N/A"}` },
//                    { template: `Адрес: ${machine.address || "Не указан"}` },
//                    { template: `Статус: ${machine.active ? "Активен" : "Не активен"}` }
                ],
                data: [

                    { info: `Информация о машине ID: ${machine.vmcNumber}` },
                    { info: `Версия ПО: ${machine.softwareVersion || "N/A"}` },
                    { info: `Версия IO: ${machine.ioVersion || "N/A"}` },
                    { info: `Адрес: ${machine.address || "Не указан"}` },
                    { info: `Статус: ${machine.active ? "Активен" : "Не активен"}` }

                ]
            },
            {
                view: "button",
                value: "Создать заказ",
                click: function() { openOrderWindow(machine.vmcNumber); }
            }
        ]
    });
}

function openOrderWindow(deviceId) {
    webix.ajax().get("/jetinno_api/getNameCoffeeOrders").then(function(data) {
        const products = data.json().map(item => ({
            id: item.productId,
            value: item.productName,
            price: item.productLastPrice,
        }));

        webix.ui({
            view: "window",
            id: "orderWindow",
            width: 400,
            position: "center",
            modal: true,
            head: {
                view: "toolbar",
                cols: [
                    { view: "label", label: "Создать заказ" },
                    { view: "icon", icon: "wxi-close", click: function() { $$("orderWindow").close(); } }
                ]
            },
            body: {
                rows: [
                    { view: "combo", id: "productSelect", label: "Выберите напиток", options: products,
                        on: {
                            onChange: function(newValue) {
                                const selectedProduct = products.find(item => String(item.id) === newValue);
                                $$("priceInput").setValue(selectedProduct ? selectedProduct.price : "");
                            }
                        }
                    },
                    { view: "text", id: "priceInput", label: "Цена", disabled: true },
                    {
                        view: "button",
                        value: "Отправить заказ",
                        click: function() {
                            const productId = $$("productSelect").getValue();
                            sendOrder(deviceId, productId);
                        }
                    }
                ]
            }
        }).show();
    });
}


function sendOrder(deviceId, productId) {
    webix.ajax().headers({
      "Content-Type": "application/json"
    }).post("/jetinno_api/setorder?deviceid=" + encodeURIComponent(String(deviceId)), { deviceid: deviceId, order: { productId } })
        .then(() => webix.message("Заказ отправлен!"))
        .fail(() => webix.alert("Ошибка при отправке заказа"));
}

function loadFaults(deviceId) {
    webix.ajax().headers({
        "Content-Type": "application/json"
    }).get("/jetinno_api/faulty", { deviceid: String(deviceId) }).then(function(response) {
        const main = $$("main");
        main.getChildViews().forEach(view => main.removeView(view));
        const data = response.json();
        const faults = data.products || [];

        $$("main").addView({

            rows: [
                {
                    cols: [
                        { view: "button", value: "Ошибки", click: () => loadFaults(machine.vmcNumber) },
                        { view: "button", value: "Заказы", click: () => loadOrders(machine.vmcNumber) },
                        { view: "button", value: "Остатки", click: () => loadSupply(machine.vmcNumber) }
                    ]
                },
                {
                    view: "datatable",
                    id: "faultsTable",
                    resizeColumn: true,
                    scrollX: false,
                    columns: [
                        { id: "FaultyCode", header: "Code" },
                        { id: "FaultyInfo", header: "Info" },
                        { id: "OccuredTime", header: "Occured" },
                        { id: "ClearTime", header: "Cleared" },
                        { id: "FaultDuration", header: "Duration" }
                    ],
                    data: faults
                }
            ]
        });
    });
}

function loadOrders(deviceId) {
    webix.ajax().headers({
        "Content-Type": "application/json"
    }).get("/jetinno_api/order", { deviceid: String(deviceId) }).then(function(response) {
        const main = $$("main");
        main.getChildViews().forEach(view => main.removeView(view));
        const data = response.json();
        const orders = data.products || [];
        $$("main").addView({
            rows: [
                {
                    cols: [
                        { view: "button", value: "Ошибки", click: () => loadFaults(machine.vmcNumber) },
                        { view: "button", value: "Заказы", click: () => loadOrders(machine.vmcNumber) },
                        { view: "button", value: "Остатки", click: () => loadSupply(machine.vmcNumber) }
                    ]
                },
                {
                    view: "datatable",
                    id: "ordersTable",
                    resizeColumn: true,
                    scrollX: false,
                    columns: [
                        { id: "ProductID", header: "Product ID" },
                        { id: "Price", header: "Price" },
                        { id: "ProductName", header: "Product Name" },
                        { id: "BuyTime", header: "Buy Time" }
                    ],
                    data: orders
                }
            ]
        });
    });
}

function loadSupply(deviceId) {
    webix.ajax().post("/jetinno_api/supply?deviceid=" + encodeURIComponent(String(deviceId))).then(function(response) {
        const main = $$("main");
        main.getChildViews().forEach(view => main.removeView(view));
        const data = response.json();
        const supplys = data.products || [];
        $$("main").addView({
            rows: [
                { cols: [
                    { view: "button", value: "Ошибки", click: () => loadFaults(machine.vmcNumber) },
                    { view: "button", value: "Заказы", click: () => loadOrders(machine.vmcNumber) },
                    { view: "button", value: "Остатки", click: () => loadSupply(machine.vmcNumber) }
                ]
                }, {

                    view: "datatable",
                    id: "supplyTable",
                    resizeColumn: true,
                    scrollX: false,
                    columns: [
                        { id: "SupplyName", header: "Name" },
                        { id: "Surplus", header: "Surplus" },
                        { id: "UploadTime", header: "Upload Time" }
                    ],
                    data: supplys
                }
            ]
        });
    });
}
