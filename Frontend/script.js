// =========================================================
// CẤU HÌNH CHUNG
// =========================================================
const BASE_URL = 'http://localhost:8080/transportation-booking';

const getToken = () => localStorage.getItem('accessToken');
const getAuthHeaders = () => ({
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${getToken()}`
});

function logout() {
    localStorage.clear();
    window.location.href = 'login.html';
}

const formatMoney = (money) => new Intl.NumberFormat('vi-VN').format(money) + ' đ';
const formatDate = (dateString) => new Date(dateString).toLocaleString('vi-VN');

let allTrips = [];
let currentTripId = null;
let selectedSeatsArr = [];
let tempBookingData = {}; 

// =========================================================
// 1. LOGIC AUTH (ĐĂNG NHẬP / ĐĂNG KÝ)
// =========================================================
if (document.getElementById('loginPage')) {
    document.getElementById('loginForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        try {
            const res = await fetch(`${BASE_URL}/auth/token`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username: document.getElementById('username').value,
                    password: document.getElementById('password').value
                })
            });
            const data = await res.json();
            if (!res.ok) return alert(data.message || "Đăng nhập thất bại");

            localStorage.setItem('accessToken', data.result.token);
            const userRes = await fetch(`${BASE_URL}/users/my-info`, {
                headers: { 'Authorization': `Bearer ${data.result.token}` }
            });
            const userData = await userRes.json();
            localStorage.setItem('currentUser', JSON.stringify(userData.result));
            window.location.href = (userData.result.role === 'ADMIN') ? 'admin.html' : 'index.html';
        } catch (err) { alert("Lỗi kết nối"); }
    });
}

if (document.getElementById('registerPage')) {
    document.getElementById('registerForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        try {
            const res = await fetch(`${BASE_URL}/users`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username: document.getElementById('username').value,
                    password: document.getElementById('password').value,
                    firstName: document.getElementById('firstName').value,
                    lastName: document.getElementById('lastName').value,
                    dob: document.getElementById('dob').value,
                    phone: document.getElementById('phone') ? document.getElementById('phone').value : ""
                })
            });
            if (res.ok) { alert("Đăng ký thành công!"); window.location.href = 'login.html'; }
            else { alert("Đăng ký thất bại"); }
        } catch (err) { alert("Lỗi kết nối"); }
    });
}

// =========================================================
// 2. LOGIC TRANG CHỦ USER (ĐẶT VÉ)
// =========================================================
if (document.getElementById('userPage')) {
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    const authSec = document.getElementById('authSection');

    // Header logic
    if (currentUser) {
        authSec.innerHTML = `
            <span>Xin chào, ${currentUser.firstName}</span>
            <span class="btn-auth" onclick="openMyTickets()" style="margin: 0 10px; background: rgba(255,255,255,0.2); cursor: pointer;">Vé của tôi</span>
            <span class="btn-logout" onclick="logout()" style="cursor: pointer;">Thoát</span>
        `;
    } else {
        authSec.innerHTML = `<a href="login.html" class="btn-auth">Đăng nhập</a>`;
    }

    // Load chuyến xe
    async function loadUserTrips() {
        try {
            const res = await fetch(`${BASE_URL}/trips`, { headers: getAuthHeaders() });
            if (res.status === 401) return document.getElementById('tripGrid').innerHTML = '<p align="center">Vui lòng đăng nhập</p>';
            const data = await res.json();
            allTrips = data.result;
            renderUserGrid(allTrips);
        } catch (err) { console.error(err); }
    }

    function renderUserGrid(trips) {
        const grid = document.getElementById('tripGrid');
        grid.innerHTML = '';
        if(!trips || trips.length === 0) return grid.innerHTML = '<p>Chưa có chuyến xe nào.</p>';

        trips.forEach(t => {
            const available = t.availableSeats;
            const isFull = available <= 0;
            grid.innerHTML += `
                <div class="trip-card">
                    <div class="trip-header">${t.busOperator || 'Nhà xe'}</div>
                    <div class="trip-body">
                        <div class="route">${t.departure} <i class="fa-solid fa-arrow-right"></i> ${t.destination}</div>
                        <p class="info-row"><i class="fa-regular fa-clock"></i> ${formatDate(t.departureTime)}</p>
                        <div class="info-row">
                            <span class="price">${formatMoney(t.price)}</span>
                            <span style="color:${isFull?'red':'green'}">${isFull?'Hết chỗ':'Còn '+available+' chỗ'}</span>
                        </div>
                        <button class="btn-book" ${isFull?'disabled':''} onclick="openModal(${t.id})">
                            ${isFull?'HẾT VÉ':'CHỌN GHẾ'}
                        </button>
                    </div>
                </div>
            `;
        });
    }

    // --- Mở Modal chọn ghế ---
    window.openModal = function(id) {
        if (!currentUser) return window.location.href = 'login.html';
        currentTripId = id;
        selectedSeatsArr = [];
        
        const trip = allTrips.find(t => t.id === id);
        document.getElementById('routeTitle').innerText = `${trip.departure} - ${trip.destination}`;
        document.getElementById('bookingModal').style.display = 'flex';
        
        document.getElementById('cusName').value = currentUser.firstName + " " + currentUser.lastName;
        document.getElementById('cusPhone').value = currentUser.phone || "";

        const seatGrid = document.getElementById('seatGrid');
        seatGrid.innerHTML = '<div class="driver-seat">Tài xế</div>';
        const total = trip.totalSeats || 40;
        const booked = trip.bookedSeats || [];

        for(let i=1; i<=total; i++) {
            const seatName = 'A' + (i<10?'0'+i:i);
            const isSold = booked.includes(seatName);
            const div = document.createElement('div');
            div.className = `seat-item ${isSold?'sold':''}`;
            div.innerText = seatName;
            if(!isSold) {
                div.onclick = () => {
                    if(div.classList.contains('selected')) {
                        div.classList.remove('selected');
                        selectedSeatsArr = selectedSeatsArr.filter(s=>s!==seatName);
                    } else {
                        if(selectedSeatsArr.length>=5) return alert("Tối đa 5 vé");
                        div.classList.add('selected');
                        selectedSeatsArr.push(seatName);
                    }
                    updateSummary();
                }
            } else { div.style.cursor = 'not-allowed'; }
            seatGrid.appendChild(div);
        }
        updateSummary();
    };

    function updateSummary() {
        const trip = allTrips.find(t => t.id === currentTripId);
        const count = selectedSeatsArr.length;
        document.getElementById('selectedSeatsSpan').innerText = count>0 ? selectedSeatsArr.join(', ') : '...';
        document.getElementById('totalPriceSpan').innerText = formatMoney(count * trip.price);
    }

    // --- Chuyển sang xác nhận ---
    window.confirmBook = function() {
        if(selectedSeatsArr.length === 0) return alert("Vui lòng chọn ghế!");
        const nameInput = document.getElementById('cusName').value;
        const phoneInput = document.getElementById('cusPhone').value;
        if(!nameInput || !phoneInput) return alert("Vui lòng nhập tên và SĐT!");

        const trip = allTrips.find(t => t.id === currentTripId);
        const totalMoney = selectedSeatsArr.length * trip.price;
        const paymentRadio = document.querySelector('input[name="payment"]:checked');
        let payName = paymentRadio && paymentRadio.value === 'momo' ? 'Ví MoMo' : (paymentRadio && paymentRadio.value === 'zalo' ? 'ZaloPay' : 'Tiền mặt');

        tempBookingData = {
            tripId: currentTripId, seats: selectedSeatsArr, name: nameInput, phone: phoneInput,
            paymentName: payName, total: totalMoney, tripInfo: trip
        };

        document.getElementById('cName').innerText = nameInput;
        document.getElementById('cPhone').innerText = phoneInput;
        document.getElementById('cRoute').innerText = `${trip.departure} -> ${trip.destination}`;
        document.getElementById('cTime').innerText = formatDate(trip.departureTime);
        document.getElementById('cSeats').innerText = selectedSeatsArr.join(', ');
        document.getElementById('cPayment').innerText = payName;
        document.getElementById('cTotal').innerText = formatMoney(totalMoney);

        document.getElementById('bookingModal').style.display = 'none';
        document.getElementById('confirmModal').style.display = 'flex';
    };

    window.backToBooking = function() {
        document.getElementById('confirmModal').style.display = 'none';
        document.getElementById('bookingModal').style.display = 'flex';
    };

    // --- Xử lý thanh toán ---
    window.processFinalPayment = async function() {
        const { tripId, seats, name, phone, paymentName, total, tripInfo } = tempBookingData;
        let successCount = 0;

        try {
            for (const seat of seats) {
                const res = await fetch(`${BASE_URL}/bookings/${tripId}`, {
                    method: 'POST',
                    headers: getAuthHeaders(),
                    body: JSON.stringify({ seatNumber: seat, passengerName: name, passengerPhone: phone })
                });
                if(res.ok) successCount++;
            }

            if(successCount > 0) {
                document.getElementById('rName').innerText = name;
                document.getElementById('rPhone').innerText = phone;
                document.getElementById('rCompany').innerText = tripInfo.busOperator || "Nhà xe";
                document.getElementById('rRoute').innerText = `${tripInfo.departure} -> ${tripInfo.destination}`;
                document.getElementById('rTime').innerText = formatDate(tripInfo.departureTime);
                document.getElementById('rSeats').innerText = seats.join(', ');
                document.getElementById('rTotal').innerText = formatMoney(total);
                document.getElementById('rPayment').innerText = paymentName;

                document.getElementById('confirmModal').style.display = 'none';
                document.getElementById('successModal').style.display = 'flex';
                // Đừng gọi loadUserTrips() ở đây nữa, để khi bấm Hoàn tất mới gọi cho mượt
            } else {
                alert("Đặt vé thất bại. Có thể ghế đã bị người khác đặt.");
                window.location.reload();
            }
        } catch(err) { console.error(err); alert("Lỗi kết nối Server"); }
    };

    // --- [MỚI] HÀM ĐÓNG MODAL THÀNH CÔNG (NÚT HOÀN TẤT) ---
    window.closeSuccessModal = function() {
        // 1. Ẩn modal success
        document.getElementById('successModal').style.display = 'none';
        
        // 2. Reset các biến tạm
        selectedSeatsArr = [];
        tempBookingData = {};
        currentTripId = null;

        // 3. Tải lại trang chủ để cập nhật ghế vừa mua thành màu đỏ
        loadUserTrips();
    };

    // --- Logic Vé của tôi / Hủy vé ---
    let ticketIdToCancel = null;

    window.openMyTickets = async function() {
        const container = document.getElementById('myTicketList');
        container.innerHTML = '<p align="center">Đang tải...</p>';
        document.getElementById('myTicketsModal').style.display = 'flex';

        try {
            const res = await fetch(`${BASE_URL}/bookings/my-bookings`, { headers: getAuthHeaders() });
            const data = await res.json();
            const bookings = data.result || [];
            
            container.innerHTML = '';
            if(bookings.length === 0) container.innerHTML = '<p align="center">Chưa có vé nào.</p>';
            
            bookings.reverse().forEach(b => {
                const t = b.trip;
                container.innerHTML += `
                    <div class="ticket-item" style="position: relative;">
                        <div class="t-left">
                            <h4>${t.busOperator || 'Nhà xe'}</h4>
                            <p><strong>Chuyến:</strong> ${t.departure} ➝ ${t.destination}</p>
                            <p><strong>Giờ:</strong> ${formatDate(t.departureTime)}</p>
                            <p><strong>Ghế:</strong> <span style="color:blue; font-weight:bold">${b.seatNumber}</span></p>
                        </div>
                        <div class="t-right" style="display: flex; flex-direction: column; align-items: flex-end; gap: 5px;">
                            <span class="t-price">${formatMoney(t.price)}</span>
                            <span class="t-status">Thành công</span>
                            <button onclick="openCancelTicketModal(${b.id})" 
                                    style="padding: 4px 8px; font-size: 11px; background: #fee2e2; color: #ef4444; border: 1px solid #ef4444; border-radius: 4px; cursor: pointer; margin-top: 5px;">
                                <i class="fa-solid fa-trash"></i> Hủy vé
                            </button>
                        </div>
                    </div>
                `;
            });
        } catch(err) { container.innerHTML = '<p align="center" style="color:red">Lỗi tải dữ liệu</p>'; }
    };

    window.openCancelTicketModal = function(id) {
        ticketIdToCancel = id;
        document.getElementById('cancelTicketModal').style.display = 'flex';
    };

    window.processCancelTicket = async function() {
        if(!ticketIdToCancel) return;
        try {
            const res = await fetch(`${BASE_URL}/bookings/${ticketIdToCancel}`, {
                method: 'DELETE',
                headers: getAuthHeaders()
            });
            document.getElementById('cancelTicketModal').style.display = 'none';

            if(res.ok) {
                alert("Đã hủy vé thành công!");
                openMyTickets();
                loadUserTrips();
            } else {
                alert("Lỗi: Không thể hủy vé này.");
            }
        } catch(err) { console.error(err); alert("Lỗi kết nối Server"); }
    }
    
    // Gọi hàm load khi trang user sẵn sàng
    loadUserTrips();
}

// =========================================================
// 3. LOGIC ADMIN (ĐÃ CÓ CHỨC NĂNG CHẶN XÓA & THÔNG BÁO)
// =========================================================
if (document.getElementById('adminPage')) {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    if (!user || user.role !== 'ADMIN') { 
        alert("Bạn không có quyền Admin"); 
        window.location.href = 'login.html'; 
    }

    let adminTripsList = [];

    async function loadTripsFromServer() {
        try {
            const res = await fetch(`${BASE_URL}/trips`, { headers: getAuthHeaders() });
            const data = await res.json();
            adminTripsList = data.result || [];
            renderAdminTable(adminTripsList);
        } catch (error) { console.error(error); alert("Lỗi tải danh sách chuyến xe!"); }
    }

    function renderAdminTable(trips) {
        const tbody = document.getElementById('tableBody');
        tbody.innerHTML = '';
        if(!trips || trips.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center">Chưa có chuyến xe nào</td></tr>';
            return;
        }

        trips.forEach(t => {
            const bookedCount = t.totalSeats - t.availableSeats;
            const hasPassenger = bookedCount > 0;

            tbody.innerHTML += `
                <tr>
                    <td>#${t.id}</td>
                    <td><strong>${t.busOperator || 'N/A'}</strong></td>
                    <td>${t.departure} -> ${t.destination}</td>
                    <td>${formatDate(t.departureTime)}</td>
                    <td class="price">${formatMoney(t.price)}</td>
                    <td>
                        ${t.availableSeats}/${t.totalSeats} 
                        ${hasPassenger ? `<br><small style="color:red">(Đã đặt: ${bookedCount})</small>` : '<br><small style="color:green">(Trống)</small>'}
                    </td>
                    <td style="text-align: center;">
                        <i class="fa-solid fa-xmark" 
                           style="color: ${hasPassenger ? '#ccc' : 'red'}; cursor: ${hasPassenger ? 'not-allowed' : 'pointer'}; font-size: 18px;" 
                           onclick="deleteTrip(${t.id})" 
                           title="${hasPassenger ? 'Không thể xóa vì có khách' : 'Xóa chuyến này'}"></i>
                    </td>
                </tr>
            `;
        });
    }

    window.deleteTrip = async function(id) {
        const tripToDelete = adminTripsList.find(t => t.id === id);
        if (tripToDelete && tripToDelete.availableSeats < tripToDelete.totalSeats) {
            alert(`CẢNH BÁO: Chuyến xe #${id} đã có người đặt vé!\nBạn KHÔNG THỂ xóa chuyến xe đang hoạt động.`);
            return; 
        }

        if(!confirm(`Bạn có chắc chắn muốn xóa chuyến xe #${id} không?`)) return;

        try {
            const res = await fetch(`${BASE_URL}/trips/${id}`, { method: 'DELETE', headers: getAuthHeaders() });
            if(res.ok) { 
                alert("Đã xóa chuyến xe thành công!"); 
                loadTripsFromServer(); 
            } else { alert("Lỗi từ Server: Không thể xóa chuyến xe này."); }
        } catch (err) { console.error(err); alert("Lỗi kết nối Server khi xóa."); }
    };

    document.getElementById('addTripForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        const newTripData = {
            busOperator: document.getElementById('company').value,
            departure: document.getElementById('from').value,
            destination: document.getElementById('to').value,
            departureTime: document.getElementById('time').value,
            price: Number(document.getElementById('price').value),
            totalSeats: Number(document.getElementById('seats').value)
        };

        try {
            const res = await fetch(`${BASE_URL}/trips`, { 
                method: 'POST', 
                headers: getAuthHeaders(), 
                body: JSON.stringify(newTripData) 
            });

            if (res.ok) { 
                alert("Thêm chuyến xe mới thành công!"); 
                e.target.reset(); 
                loadTripsFromServer(); 
            } else { alert("Lỗi: Không thể thêm chuyến xe. Vui lòng kiểm tra lại thông tin."); }
        } catch (error) { console.error(error); alert("Lỗi kết nối Server khi thêm mới."); }
    });

    loadTripsFromServer();
}