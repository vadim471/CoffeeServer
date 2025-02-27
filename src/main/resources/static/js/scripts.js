async function fetchSupply() {
    const deviceid = parseInt(document.getElementById("deviceid").value, 10);
    console.log(deviceid, typeof deviceid);

    if (!deviceid) return alert("Введите deviceid!");

    const url = `/jetinno_api/supply?deviceid=${deviceid}`;
    console.log(url);

    const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" }
    });

    const data = await response.json();
    const tableBody = document.getElementById("supplyTableBody");
    tableBody.innerHTML = "";

    data.products.forEach(product => {
        const row = `<tr>
            <td>${product.SupplyName}</td>
            <td>${product.Surplus}</td>
            <td>${product.UploadTime}</td>
        </tr>`;
        tableBody.innerHTML += row;
    });
}

async function loadMachineIds() {
    const response = await fetch("/jetinno_api/listMachines");
    const machineList = await response.json();
    const machineIds = machineList.map(machine => machine.vmcNumber)
    const selects = [document.getElementById("deviceid"), document.getElementById("device")];

    selects.forEach(select => {
        select.innerHTML = "";
        machineIds.forEach(id => {
            const option = document.createElement("option");
            option.value = id;
            option.textContent = id;
            select.appendChild(option)
        });
    })

}

async function loadProducts() {
    const response = await fetch("/jetinno_api/getNameCoffeeOrders");
    const products = await response.json();

    const select = document.getElementById("productSelect");
    select.innerHTML = "";
    products.forEach(product => {
        select.innerHTML += `<option value="${product.productId}" data-price="${product.productLastPrice}">
            ${product.productName}
        </option>`;
    });
}

function updateOrderFields() {
    const select = document.getElementById("productSelect");
    const priceInput = document.getElementById("priceInput");
    const selectedOption = select.options[select.selectedIndex];

    priceInput.value = selectedOption.getAttribute("data-price");
}

async function sendOrder() {
    const deviceid = parseInt(document.getElementById("device").value, 10);
    const select = document.getElementById("productSelect");
    const productId = parseInt(select.value, 10);
    const price = parseInt(document.getElementById("priceInput").value, 10);


    if (!deviceid || !productId || !price) return alert("Заполните все поля!");

    const payload = {
        deviceid,
        order: { productId, price }
    };

    const url = `/jetinno_api/setorder?deviceid=${deviceid}`;

    const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });

    if (response.ok) {
        alert("Заказ успешно отправлен!");
    }
    const json = await response.json();
    alert(json);
}

async function fetchOrders() {
    const deviceid = parseInt(document.getElementById("deviceid").value, 10);
    const startDate = document.getElementById("startDate").value;
    const endDate = document.getElementById("endDate").value;

    if (!deviceid || !startDate || !endDate) return alert("Заполните все поля!");

    const url = `/jetinno_api/order?deviceid=${deviceid}&dates=${startDate}&datef=${endDate}`;

    const response = await fetch(url);
    const data = await response.json();
    const tableBody = document.getElementById("ordersTableBody");
    tableBody.innerHTML = "";

    data.products.forEach(order => {
        const row = `<tr>
            <td>${order.ProductName}</td>
            <td>${order.ProductID}</td>
            <td>${order.Price}</td>
            <td>${order.PayType}</td>
            <td>${order.Status}</td>
            <td>${order.BuyTime}</td>
        </tr>`;
        tableBody.innerHTML += row;
    });
}

async function fetchFaults() {
    const deviceid = parseInt(document.getElementById("deviceid").value, 10);
    const startDate = document.getElementById("faultStartDate").value;
    const endDate = document.getElementById("faultEndDate").value;

    let url = `/jetinno_api/faulty?deviceid=${deviceid}`;
    if (startDate && endDate) url += `&startDate=${startDate}&endDate=${endDate}`;

    const response = await fetch(url);
    const data = await response.json();
    const tableBody = document.getElementById("faultsTableBody");
    tableBody.innerHTML = "";

    data.products.forEach(error => {
        const row = `<tr>
            <td>${error.FaultyCode}</td>
            <td>${error.FaultyInfo}</td>
            <td>${error.OccuredTime}</td>
            <td>${error.ClearTime}</td>
            <td>${error.FaultDuration}</td>
        </tr>`;
        tableBody.innerHTML += row;
    });
}

document.addEventListener("DOMContentLoaded", loadProducts);
document.addEventListener("DOMContentLoaded", () => {
    loadMachineIds();
    document.getElementById("deviceid").addEventListener("focus", loadMachineIds);
    document.getElementById("device").addEventListener("focus", loadMachineIds);
});
