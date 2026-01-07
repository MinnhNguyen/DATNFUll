<template>
    <div class="cart-container">
        <!-- Breadcrumb -->
        <div class="breadcrumb-container">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a href="#" @click.prevent="goShopping">Trang chủ</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Giỏ hàng</li>
                </ol>
            </nav>
        </div>

        <div class="cart-content">
            <div class="cart-header">
                <h1>Giỏ hàng của bạn</h1>
                <p class="cart-count" v-if="cartItems.length > 0">
                    Có {{cartItems.filter((_item, index) => canBeSelected(index)).length}} sản phẩm trong giỏ hàng
                </p>
            </div>

            <!-- Empty Cart State -->
            <div v-if="cartItems.length === 0" class="empty-cart">
                <shopping-cart-outlined class="empty-cart-icon" />
                <h2>Giỏ hàng trống</h2>
                <p>Bạn chưa có sản phẩm nào trong giỏ hàng</p>
                <a-button type="primary" size="large" @click="goShopping">
                    Tiếp tục mua sắm
                </a-button>
            </div>

            <!-- Cart Items -->
            <div v-else class="cart-items">
                <div class="cart-items-list">
                    <div class="cart-toolbar">
                        <a-checkbox :checked="isAllSelected" @change="e => selectAll(e.target.checked)">
                            <span class="select-all-text">Chọn tất cả ({{cartItems.filter((_item, index) =>
                                canBeSelected(index)).length}} sản phẩm)</span>
                        </a-checkbox>
                        <a-button type="text" danger @click="removeSelectedItems"
                            :disabled="selectedItems.length === 0">
                            <delete-outlined />
                            Xóa đã chọn
                        </a-button>
                    </div>

                    <div class="cart-item" v-for="(item, index) in cartItems" :key="index"
                        :class="{ 'unavailable-item': !canBeSelected(index) }">
                        <a-checkbox :checked="selectedItems.includes(index)" :disabled="!canBeSelected(index)" @change="e => {
                            // Chỉ cho phép chọn các sản phẩm khả dụng
                            if (!canBeSelected(index)) {
                                e.preventDefault();
                                toast.warning(`Sản phẩm '${item.name}' không khả dụng để thanh toán`, {
                                    position: 'top-center',
                                    autoClose: 3000
                                });
                                return;
                            }

                            if (e.target.checked) {
                                selectedItems.push(index);
                            } else {
                                selectedItems = selectedItems.filter(i => i !== index);
                            }
                        }">
                        </a-checkbox>

                        <div class="cart-item-content">
                            <div class="col-product">
                                <div class="product-image">
                                    <img :src="getProductImage(item)" style="height: 5rem; width: 5rem;"
                                        :alt="item.name">
                                </div>
                                <div class="product-info">
                                    <h3 class="product-name">{{ item.name }}</h3>
                                    <div class="product-variant" v-if="item.color || (item.size && item.size.trim())">
                                        <span v-if="item.color">Màu: {{ item.color }}</span>
                                        <span v-if="item.size && item.size.trim()">Size: {{ item.size }}</span>
                                    </div>
                                    <div class="product-price">
                                        <span class="current-price">{{ formatCurrency(item.price) }}</span>
                                        <span class="original-price"
                                            v-if="item.originalPrice && item.originalPrice > item.price">
                                            {{ formatCurrency(item.originalPrice) }}
                                        </span>
                                    </div>
                                </div>
                            </div>

                            <div class="col-quantity" v-if="canBeSelected(index)">
                                <div class="quantity-controls">
                                    <a-button class="quantity-btn" @click="handleQuantityDecrease(index)"
                                        :class="{ 'remove-quantity-btn': item.quantity <= 1 }">
                                        <minus-outlined />
                                    </a-button>
                                    <span class="quantity-value">{{ item.quantity }}</span>
                                    <a-button class="quantity-btn" @click="checkMaxAndIncrease(index)"
                                        :class="{ 'max-quantity-warning': item.quantity >= item.maxQuantity }">
                                        <plus-outlined />
                                    </a-button>
                                </div>
                                <!-- Hiển thị số lượng tồn kho nếu dưới 10 -->
                                <div v-if="item.maxQuantity < 10" class="stock-warning">
                                    <a-tag color="orange">Còn {{ item.maxQuantity }} sản phẩm</a-tag>
                                </div>
                                <!-- Hiển thị thông báo khi đạt giới hạn -->
                                <div v-if="item.quantity >= item.maxQuantity" class="max-quantity-message">
                                    <a-tag color="red">Đã đạt giới hạn tồn kho</a-tag>
                                </div>
                            </div>

                            <!-- Thông báo khi sản phẩm không khả dụng -->
                            <div class="unavailable-notice" v-if="!canBeSelected(index)">
                                <a-tag color="default">Sản phẩm không khả dụng</a-tag>
                            </div>

                            <div class="col-total" v-if="canBeSelected(index)">
                                <span class="total">{{ formatCurrency(item.price * item.quantity) }}</span>
                            </div>

                            <div class="col-action">
                                <a-button type="text" danger class="remove-btn" @click="removeItem(index)">
                                    <delete-outlined />
                                </a-button>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="cart-summary">
                    <div class="summary-header">
                        <h2>Tóm tắt đơn hàng</h2>
                    </div>

                    <div class="summary-body">
                        <div class="summary-row">
                            <span class="summary-label">Số lượng đã chọn:</span>
                            <span class="summary-value">{{ totalSelectedQuantity }} sản phẩm</span>
                        </div>

                        <div class="summary-row">
                            <span class="summary-label">Tổng tiền:</span>
                            <span class="summary-value total-price">{{ formatCurrency(totalSelectedPrice) }}</span>
                        </div>

                        <div class="summary-note">
                            <p>* Phí vận chuyển sẽ được tính ở trang thanh toán</p>
                        </div>

                        <div class="summary-actions">
                            <a-button type="primary" block size="large" @click="proceedToCheckout"
                                :disabled="selectedItems.length === 0" class="checkout-button">
                                Thanh toán
                            </a-button>
                            <a-button block @click="goShopping" class="mt-3">
                                Tiếp tục mua sắm
                            </a-button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ShoppingCartOutlined, DeleteOutlined, MinusOutlined, PlusOutlined } from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import { useGbStore } from '@/stores/gbStore';
import { useRoute } from 'vue-router';
import { banHangOnlineService } from '@/services/banHangOnlineService';
import { toast } from 'vue3-toastify';

const route = useRoute();
const router = useRouter();
const store = useGbStore();
const cartItems = ref([]);
const selectedItems = ref([]);
const idKhachHang = ref(null);
const gioHang = ref([]);

// ✅ HELPER FUNCTION - Chuẩn hóa kiểm tra đăng nhập
const getAuthenticatedUser = () => {
    // Check customer login (from login() in gbStore.js line 1750-1754)
    const customerDataStr = sessionStorage.getItem('khachHang') || localStorage.getItem('khachHang');
    if (customerDataStr) {
        try {
            const customerData = JSON.parse(customerDataStr);
            return {
                type: 'customer',
                id: customerData.idKhachHang,
                data: customerData
            };
        } catch (e) {
            console.error('Error parsing khachHang:', e);
        }
    }

    // Check admin/staff login (from loginNV() in gbStore.js line 1819-1821)
    const adminDataStr = sessionStorage.getItem('userDetails') || localStorage.getItem('userDetails');
    if (adminDataStr) {
        try {
            const adminData = JSON.parse(adminDataStr);
            return {
                type: 'admin',
                id: adminData.idKhachHang,
                data: adminData
            };
        } catch (e) {
            console.error('Error parsing userDetails:', e);
        }
    }

    return null; // Not logged in
};

// ✅ FIXED: Tải giỏ hàng từ localStorage
const loadCartFromLocalStorage = async () => {
    try {
        const auth = getAuthenticatedUser();

        // ❌ Chỉ load localStorage khi KHÔNG đăng nhập
        if (!auth) {
            const savedCart = localStorage.getItem('gb-sport-cart');
            if (savedCart) {
                cartItems.value = JSON.parse(savedCart);
                console.log('✅ [GUEST] Đã tải giỏ hàng từ localStorage:', cartItems.value.length, 'sản phẩm');

                // Cập nhật số lượng tồn kho cho tất cả sản phẩm
                await updateAllMaxQuantities();
            } else {
                cartItems.value = [];
                console.log('🔍 [GUEST] Không có sản phẩm trong giỏ hàng');
            }
        } else {
            console.log(`🔒 [${auth.type.toUpperCase()}] Đã đăng nhập, bỏ qua localStorage`);
        }
    } catch (error) {
        console.error('❌ Lỗi khi tải giỏ hàng:', error);
        cartItems.value = [];
    }
};

// Lưu giỏ hàng vào localStorage và thông báo cập nhật
const saveCartToLocalStorage = () => {
    localStorage.setItem('gb-sport-cart', JSON.stringify(cartItems.value));

    // Phát ra sự kiện custom để cập nhật các component khác
    window.dispatchEvent(new CustomEvent('cart-updated'));
};

// Tính tổng số tiền của các sản phẩm đã chọn
const totalSelectedPrice = computed(() => {
    let total = 0;
    for (const index of selectedItems.value) {
        if (index < cartItems.value.length) {
            const item = cartItems.value[index];
            total += item.price * item.quantity;
        }
    }
    return total;
});

// Kiểm tra xem sản phẩm có thể được chọn hay không
const canBeSelected = (index) => {
    const item = cartItems.value[index];
    return item.maxQuantity > 0 && (item.trang_thai === undefined || item.trang_thai === 'Hoạt động');
};

// Tính tổng số lượng sản phẩm đã chọn
const totalSelectedQuantity = computed(() => {
    let total = 0;
    for (const index of selectedItems.value) {
        if (index < cartItems.value.length) {
            total += cartItems.value[index].quantity;
        }
    }
    return total;
});

// Định dạng tiền tệ
const formatCurrency = (value) => {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0
    }).format(value);
};

// Xử lý chọn tất cả sản phẩm
const selectAll = (checked) => {
    if (checked) {
        // Chỉ chọn các sản phẩm khả dụng
        selectedItems.value = cartItems.value
            .map((item, index) => ({ item, index }))
            .filter(({ item, index }) => canBeSelected(index))
            .map(({ index }) => index);
    } else {
        selectedItems.value = [];
    }
};

// Kiểm tra xem đã chọn tất cả chưa
const isAllSelected = computed(() => {
    const availableItems = cartItems.value.filter((item, index) => canBeSelected(index));
    if (availableItems.length === 0) return false;

    const availableIndexes = cartItems.value
        .map((item, index) => ({ item, index }))
        .filter(({ item, index }) => canBeSelected(index))
        .map(({ index }) => index);

    return availableIndexes.every(index => selectedItems.value.includes(index));
});

// Tăng z-index cho Modal confirm
const showConfirmModal = (item, index) => {
    // Tạo style cho modal
    const modalStyle = document.createElement('style');
    modalStyle.type = 'text/css';
    modalStyle.id = 'cart-modal-style';
    modalStyle.innerHTML = `
        .ant-modal-root, .ant-modal-mask, .ant-modal-wrap {
            z-index: 9999 !important;
        }
    `;

    // Thêm style vào head nếu chưa có
    if (!document.getElementById('cart-modal-style')) {
        document.head.appendChild(modalStyle);
    }

    // Hiển thị modal xác nhận
    Modal.confirm({
        title: 'Xác nhận xóa sản phẩm',
        content: `Bạn có chắc chắn muốn xóa sản phẩm "${item.name}" khỏi giỏ hàng?`,
        okText: 'Xóa',
        cancelText: 'Hủy',
        okButtonProps: {
            danger: true,
        },
        onOk: () => {
            removeItem(index);
        },
        // Đảm bảo z-index cao
        zIndex: 9999
    });
};

// Xử lý sự kiện giảm số lượng
const handleQuantityDecrease = async (index) => {
    const item = cartItems.value[index];

    try {
        // Luôn kiểm tra số lượng tồn kho mới nhất
        await store.getMaxSoLuongSP(item.id);
        const maxAvailable = store.maxSoLuongSP || 0;

        // Cập nhật lại maxQuantity trong item
        item.maxQuantity = maxAvailable;

        // Kiểm tra nếu số lượng hiện tại vượt quá số lượng tồn kho mới nhất
        if (item.quantity > maxAvailable) {
            // Tính số lượng cần giảm
            const adjustQty = item.quantity - maxAvailable;

            // ✅ FIXED: Dùng helper
            const auth = getAuthenticatedUser();
            if (auth) {
                // ✅ Đã đăng nhập
                try {
                    await store.xoaSoLuongSPGH(
                        auth.id,
                        item.id,
                        adjustQty
                    );

                    const oldQuantity = item.quantity;
                    item.quantity = maxAvailable;

                    toast.warning(`Số lượng sản phẩm "${item.name}" đã được điều chỉnh từ ${oldQuantity} xuống ${maxAvailable} do hàng tồn kho đã thay đổi`, {
                        position: "top-center",
                        autoClose: 4000
                    });
                } catch (error) {
                    console.error('❌ Lỗi khi điều chỉnh số lượng:', error);
                    toast.error('Có lỗi xảy ra khi điều chỉnh số lượng sản phẩm');
                }
            } else {
                // ❌ Chưa đăng nhập
                const oldQuantity = item.quantity;
                item.quantity = maxAvailable;
                saveCartToLocalStorage();

                toast.warning(`Số lượng sản phẩm "${item.name}" đã được điều chỉnh từ ${oldQuantity} xuống ${maxAvailable} do hàng tồn kho đã thay đổi`, {
                    position: "top-center",
                    autoClose: 4000
                });
            }
            return;
        }

        // Nếu số lượng > 1, thực hiện giảm số lượng bình thường
        if (item.quantity > 1) {
            // ✅ FIXED: Dùng helper
            const auth = getAuthenticatedUser();
            if (auth) {
                try {
                    await store.xoaSoLuongSPGH(
                        auth.id,
                        item.id,
                        1 // Giảm 1
                    );

                    item.quantity -= 1;

                    // ✅ THÊM: Dispatch event
                    window.dispatchEvent(new CustomEvent('cart-updated', {
                        detail: {
                            action: 'quantity_decreased',
                            userType: auth.type,
                            totalItems: cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
                        }
                    }));

                    toast.success(`Đã giảm số lượng sản phẩm "${item.name}" xuống ${item.quantity}`);
                } catch (error) {
                    console.error('❌ Lỗi khi giảm số lượng:', error);
                    toast.error('Có lỗi xảy ra khi giảm số lượng sản phẩm');
                }
            } else {
                // ❌ Chưa đăng nhập
                item.quantity -= 1;
                saveCartToLocalStorage();
                toast.success(`Đã giảm số lượng sản phẩm "${item.name}" xuống ${item.quantity}`);
            }
            return;
        }

        // Nếu số lượng = 1, hiển thị xác nhận xóa với z-index cao
        showConfirmModal(item, index);
    } catch (error) {
        console.error('Lỗi khi kiểm tra số lượng tồn kho:', error);
        toast.error('Có lỗi xảy ra khi kiểm tra số lượng tồn kho');
    }
};

// Thêm hàm mới để kiểm tra số lượng tối đa trước khi tăng
const checkMaxAndIncrease = async (index) => {
    const item = cartItems.value[index];

    try {
        // Luôn kiểm tra số lượng tồn kho mới nhất
        await store.getMaxSoLuongSP(item.id);
        const maxAvailable = store.maxSoLuongSP || 0;

        // Cập nhật lại maxQuantity trong item
        item.maxQuantity = maxAvailable;

        // Nếu số lượng hiện tại >= số lượng tối đa, hiển thị thông báo
        if (item.quantity >= maxAvailable) {
            toast.error(`Không thể thêm nữa! Sản phẩm "${item.name}" chỉ có tối đa ${maxAvailable} trong kho.`, {
                position: "top-center",
                autoClose: 3000,
            });
            return;
        }

        // Nếu có thể tăng, gọi hàm tăng số lượng
        handleQuantityIncrease(index);
    } catch (error) {
        console.error('Lỗi khi kiểm tra số lượng tồn kho:', error);
        toast.error('Có lỗi xảy ra khi kiểm tra số lượng tồn kho');
    }
};

// Xử lý tăng số lượng
const handleQuantityIncrease = async (index) => {
    const item = cartItems.value[index];

    try {
        // Luôn kiểm tra số lượng tồn kho mới nhất trước khi tăng
        await store.getMaxSoLuongSP(item.id);
        const maxAvailable = store.maxSoLuongSP || 0;

        // Cập nhật lại maxQuantity trong item
        item.maxQuantity = maxAvailable;

        // Kiểm tra nếu số lượng hiện tại vượt quá số lượng tồn kho mới nhất
        if (item.quantity > maxAvailable) {
            // Tính số lượng cần giảm
            const adjustQty = item.quantity - maxAvailable;

            const userDetailsStr = sessionStorage.getItem('userDetails');
            // ✅ FIXED: Dùng helper
            const auth = getAuthenticatedUser();
            if (auth) {
                try {
                    await store.xoaSoLuongSPGH(
                        auth.id,
                        item.id,
                        adjustQty
                    );

                    const oldQuantity = item.quantity;
                    item.quantity = maxAvailable;

                    toast.warning(`Số lượng sản phẩm "${item.name}" đã được điều chỉnh từ ${oldQuantity} xuống ${maxAvailable} do hàng tồn kho đã thay đổi`, {
                        position: "top-center",
                        autoClose: 4000
                    });
                } catch (error) {
                    console.error('❌ Lỗi khi điều chỉnh số lượng:', error);
                    toast.error('Có lỗi xảy ra khi điều chỉnh số lượng sản phẩm');
                }
            } else {
                const oldQuantity = item.quantity;
                item.quantity = maxAvailable;
                saveCartToLocalStorage();

                toast.warning(`Số lượng sản phẩm "${item.name}" đã được điều chỉnh từ ${oldQuantity} xuống ${maxAvailable} do hàng tồn kho đã thay đổi`, {
                    position: "top-center",
                    autoClose: 4000
                });
            }
            return;
        }

        // Kiểm tra nếu đã đạt giới hạn
        if (item.quantity >= maxAvailable) {
            toast.error(`Không thể thêm nữa! Sản phẩm "${item.name}" chỉ có tối đa ${maxAvailable} trong kho.`, {
                position: "top-center",
                autoClose: 3000,
                hideProgressBar: false,
                closeOnClick: true,
                pauseOnHover: true,
                draggable: true,
            });
            return;
        }

        // Nếu chưa đạt giới hạn thì tăng số lượng
        // ✅ FIXED: Dùng helper
        const auth = getAuthenticatedUser();
        if (auth) {
            try {
                await store.getGioHangByIdKH(
                    auth.id,
                    item.id,
                    1 // Thêm 1
                );

                item.quantity += 1;

                // ✅ THÊM: Dispatch event
                window.dispatchEvent(new CustomEvent('cart-updated', {
                    detail: {
                        action: 'quantity_increased',
                        userType: auth.type,
                        totalItems: cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
                    }
                }));

                toast.success(`Đã tăng số lượng sản phẩm "${item.name}" lên ${item.quantity}`);
            } catch (error) {
                console.error('❌ Lỗi khi tăng số lượng:', error);
                toast.error('Có lỗi xảy ra khi tăng số lượng sản phẩm');
            }
        } else {
            item.quantity += 1;
            saveCartToLocalStorage();
            toast.success(`Đã tăng số lượng sản phẩm "${item.name}" lên ${item.quantity}`);
        }
    } catch (error) {
        console.error('Lỗi khi kiểm tra số lượng tồn kho:', error);
        toast.error('Có lỗi xảy ra khi kiểm tra số lượng tồn kho');
    }
};

// Xóa sản phẩm khỏi giỏ hàng
const removeItem = async (index) => {
    try {
        // ✅ FIXED: Dùng helper function để check authentication
        const auth = getAuthenticatedUser();

        if (auth) {
            // ✅ Đã đăng nhập - Xóa từ database
            const item = cartItems.value[index];
            console.log(`✅ [${auth.type.toUpperCase()}] Xóa sản phẩm từ database:`, { userId: auth.id, itemId: item.id });

            // Gọi API xóa với toàn bộ số lượng của sản phẩm
            await store.xoaSoLuongSPGH(
                auth.id,
                item.id,
                item.quantity
            );

            // Sau khi xóa thành công, cập nhật UI
            cartItems.value.splice(index, 1);
            selectedItems.value = selectedItems.value
                .filter(i => i !== index)
                .map(i => i > index ? i - 1 : i);

            // ✅ THÊM: Dispatch event để sync với các component khác
            window.dispatchEvent(new CustomEvent('cart-updated', {
                detail: {
                    action: 'item_removed',
                    userType: auth.type,
                    itemId: item.id,
                    totalItems: cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
                }
            }));

            message.success('Đã xóa sản phẩm khỏi giỏ hàng');
        } else {
            // ❌ Chưa đăng nhập - Xóa từ localStorage
            console.log('🔄 [GUEST] Xóa sản phẩm từ localStorage');
            cartItems.value.splice(index, 1);
            selectedItems.value = selectedItems.value
                .filter(i => i !== index)
                .map(i => i > index ? i - 1 : i);
            saveCartToLocalStorage();
            message.success('Đã xóa sản phẩm khỏi giỏ hàng');
        }
    } catch (error) {
        console.error('❌ Lỗi khi xóa sản phẩm:', error);
        message.error('Có lỗi xảy ra khi xóa sản phẩm');
    }
};

// Điều hướng đến trang sản phẩm
const goShopping = () => {
    router.push('/home');
};

// Xử lý thanh toán
const proceedToCheckout = async () => {
    if (selectedItems.value.length === 0) {
        message.warning('Vui lòng chọn ít nhất một sản phẩm để thanh toán');
        return;
    }

    // Kiểm tra số lượng tồn kho trước khi thanh toán
    try {
        let hasStockIssue = false;
        let problematicProducts = [];

        // Cập nhật số lượng tồn kho cho tất cả sản phẩm đã chọn
        for (const index of selectedItems.value) {
            const item = cartItems.value[index];
            await store.getMaxSoLuongSP(item.id);
            const maxAvailable = store.maxSoLuongSP || 0;

            // Cập nhật lại maxQuantity
            item.maxQuantity = maxAvailable;

            // Kiểm tra nếu số lượng hiện tại vượt quá số lượng tối đa
            if (item.quantity > maxAvailable) {
                hasStockIssue = true;
                problematicProducts.push({
                    name: item.name,
                    currentQty: item.quantity,
                    maxQty: maxAvailable,
                    index: index,
                    id: item.id,
                    adjustQty: item.quantity - maxAvailable // Số lượng cần điều chỉnh
                });
            }
        }

        // Nếu có vấn đề về số lượng tồn kho, hiển thị thông báo và không cho phép thanh toán
        if (hasStockIssue) {
            let errorMessage = 'Không thể thanh toán do vượt quá số lượng tồn kho:';
            problematicProducts.forEach(product => {
                errorMessage += `\n- ${product.name}: chỉ còn ${product.maxQty} sản phẩm (đang chọn ${product.currentQty})`;
            });

            // Thêm thông báo về điều chỉnh tự động
            errorMessage += '\n\nNhấn "Đã hiểu" để tự động điều chỉnh số lượng sản phẩm.';

            // Hiển thị thông báo lỗi với callback onOk để xử lý khi nhấn "Đã hiểu"
            Modal.error({
                title: 'Lỗi số lượng tồn kho',
                content: errorMessage,
                okText: 'Đã hiểu',
                zIndex: 9999,
                onOk: async () => {
                    // Xử lý điều chỉnh số lượng
                    const userDetailsStr = sessionStorage.getItem('userDetails');

                    // Biến theo dõi xem đã xử lý xong chưa
                    let processingComplete = false;

                    if (userDetailsStr) {
                        // Khách hàng đã đăng nhập - cập nhật qua API
                        try {
                            const userDetails = JSON.parse(userDetailsStr);

                            // Xử lý từng sản phẩm vượt quá số lượng
                            for (const product of problematicProducts) {
                                // Cập nhật số lượng sản phẩm vào số lượng tối đa cho phép
                                await store.xoaSoLuongSPGH(
                                    userDetails.idKhachHang,
                                    product.id,
                                    product.adjustQty // Số lượng cần giảm
                                );

                                // Cập nhật lại số lượng trong hiển thị
                                cartItems.value[product.index].quantity = product.maxQty;
                            }

                            processingComplete = true;
                            message.success('Đã tự động điều chỉnh số lượng sản phẩm');
                        } catch (error) {
                            console.error('Lỗi khi cập nhật số lượng sản phẩm:', error);
                            message.error('Có lỗi xảy ra khi điều chỉnh số lượng sản phẩm');
                            return;
                        }
                    } else {
                        // Khách hàng chưa đăng nhập - cập nhật vào localStorage
                        try {
                            // Cập nhật số lượng trong cartItems
                            for (const product of problematicProducts) {
                                cartItems.value[product.index].quantity = product.maxQty;
                            }

                            // Lưu lại vào localStorage
                            saveCartToLocalStorage();

                            processingComplete = true;
                            message.success('Đã tự động điều chỉnh số lượng sản phẩm');
                        } catch (error) {
                            console.error('Lỗi khi cập nhật số lượng sản phẩm trong localStorage:', error);
                            message.error('Có lỗi xảy ra khi điều chỉnh số lượng sản phẩm');
                            return;
                        }
                    }

                    // Nếu xử lý thành công, tiếp tục thanh toán
                    if (processingComplete) {
                        // Lấy các sản phẩm đã chọn với số lượng đã được điều chỉnh
                        const selectedProducts = selectedItems.value.map(index => {
                            const item = cartItems.value[index];
                            return {
                                ...item,
                                price: Number(item.price) || 0,
                                quantity: Number(item.quantity) || 1,
                                originalPrice: Number(item.originalPrice) || 0,
                                maxQuantity: Number(item.maxQuantity) || 1,
                                id_chi_tiet_san_pham: item.id,
                                ten_san_pham: item.name,
                                hinh_anh: item.image,
                                ten_mau_sac: item.color,
                                gia_tri: item.size,
                                gia: Number(item.price) || 0,
                                so_luong: Number(item.quantity) || 1,
                                gia_goc: Number(item.originalPrice) || 0
                            };
                        });

                        // Lưu vào store để sử dụng ở trang thanh toán
                        store.setCheckoutItems(selectedProducts);

                        // Chuyển hướng đến trang thanh toán
                        // setTimeout(() => {
                        //     router.push('/thanhtoan-banhang');
                        // }, 500); // Chờ một chút để hiển thị thông báo thành công
                    }
                }
            });
            return;
        }
    } catch (error) {
        console.error('Lỗi khi kiểm tra số lượng tồn kho:', error);
        message.error('Có lỗi xảy ra khi kiểm tra số lượng tồn kho');
        return;
    }

    // Lấy các sản phẩm đã chọn
    const selectedProducts = selectedItems.value.map(index => {
        const item = cartItems.value[index];
        // Đảm bảo các giá trị số là đúng định dạng
        return {
            ...item,
            price: Number(item.price) || 0,
            quantity: Number(item.quantity) || 1,
            originalPrice: Number(item.originalPrice) || 0,
            maxQuantity: Number(item.maxQuantity) || 1,
            // Đổi tên field để khớp với cấu trúc dữ liệu của trang thanh toán
            id_chi_tiet_san_pham: item.id,
            ten_san_pham: item.name,
            hinh_anh: item.image,
            ten_mau_sac: item.color,
            gia_tri: item.size,
            gia: Number(item.price) || 0,
            so_luong: Number(item.quantity) || 1,
            gia_goc: Number(item.originalPrice) || 0
        };
    });

    console.log('Sản phẩm đã chọn để thanh toán:', selectedProducts);

    // Lưu vào store để sử dụng ở trang thanh toán
    store.setCheckoutItems(selectedProducts);

    // Chuyển hướng đến trang thanh toán
    router.push('/thanhtoan-banhang');
};

// Xóa các sản phẩm đã chọn
const removeSelectedItems = async () => {
    if (selectedItems.value.length === 0) {
        message.warning('Vui lòng chọn ít nhất một sản phẩm để xóa');
        return;
    }

    try {
        // ✅ FIXED: Dùng helper function để check authentication
        const auth = getAuthenticatedUser();

        if (auth) {
            // ✅ Đã đăng nhập - Xóa từ database
            console.log(`✅ [${auth.type.toUpperCase()}] Xóa ${selectedItems.value.length} sản phẩm từ database`);

            // Sắp xếp theo thứ tự giảm dần để xóa từ cuối lên
            const sortedIndexes = [...selectedItems.value].sort((a, b) => b - a);

            // Xóa từng sản phẩm đã chọn
            for (const index of sortedIndexes) {
                const item = cartItems.value[index];
                await store.xoaSoLuongSPGH(
                    auth.id,
                    item.id,
                    item.quantity
                );
                cartItems.value.splice(index, 1);
            }

            selectedItems.value = [];

            // ✅ THÊM: Dispatch event
            window.dispatchEvent(new CustomEvent('cart-updated', {
                detail: {
                    action: 'selected_items_removed',
                    userType: auth.type,
                    totalItems: cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
                }
            }));

            message.success('Đã xóa các sản phẩm đã chọn khỏi giỏ hàng');
        } else {
            // ❌ Chưa đăng nhập - Xóa từ localStorage
            console.log('🔄 [GUEST] Xóa sản phẩm từ localStorage');
            const sortedIndexes = [...selectedItems.value].sort((a, b) => b - a);
            for (const index of sortedIndexes) {
                cartItems.value.splice(index, 1);
            }
            selectedItems.value = [];
            saveCartToLocalStorage();
            message.success('Đã xóa các sản phẩm đã chọn khỏi giỏ hàng');
        }
    } catch (error) {
        console.error('❌ Lỗi khi xóa các sản phẩm đã chọn:', error);
        message.error('Có lỗi xảy ra khi xóa sản phẩm');
    }
};

// Lấy hình ảnh sản phẩm từ danh sách sản phẩm dựa vào ID chi tiết sản phẩm
const getProductImage = (item) => {
    try {
        // Kiểm tra trước nếu item đã có đường dẫn ảnh hợp lệ
        if (item.hinh_anh) {
            return item.hinh_anh;
        }

        // Nếu item.image là URL string
        if (typeof item.image === 'string') {
            return item.image;
        }

        // Nếu item.image là object có url
        if (item.image && typeof item.image === 'object' && item.image.url) {
            return item.image.url;
        }

        // Tìm kiếm trong danh sách sản phẩm từ store
        if (store.getAllSanPham && store.getAllSanPham.length > 0) {
            // Tìm theo ID chi tiết sản phẩm
            const productDetail = store.getAllSanPham.find(p => {
                // Tìm trong chi tiết sản phẩm
                if (p.chiTietSanPham && Array.isArray(p.chiTietSanPham)) {
                    return p.chiTietSanPham.some(ct =>
                        ct.id === item.id ||
                        ct.id === item.id_chi_tiet_san_pham
                    );
                }
                return false;
            });

            if (productDetail) {
                // Tìm chi tiết sản phẩm cụ thể
                const ctsp = productDetail.chiTietSanPham.find(ct =>
                    ct.id === item.id ||
                    ct.id === item.id_chi_tiet_san_pham
                );

                if (ctsp && ctsp.hinh_anh) {
                    return ctsp.hinh_anh;
                }

                // Nếu không có ảnh trong CTSP, dùng ảnh của sản phẩm chính
                if (productDetail.hinh_anh) {
                    return productDetail.hinh_anh;
                }

                // Kiểm tra mảng hình ảnh
                if (productDetail.hinh_anhs && productDetail.hinh_anhs.length > 0) {
                    if (typeof productDetail.hinh_anhs[0] === 'object' && productDetail.hinh_anhs[0].url) {
                        return productDetail.hinh_anhs[0].url;
                    }
                    return productDetail.hinh_anhs[0];
                }
            }

            // Tìm theo tên sản phẩm nếu không tìm thấy theo ID
            if (item.name) {
                const productByName = store.getAllSanPham.find(p =>
                    p.ten_san_pham &&
                    p.ten_san_pham.toLowerCase() === item.name.toLowerCase()
                );

                if (productByName) {
                    if (productByName.hinh_anh) {
                        return productByName.hinh_anh;
                    }
                    if (productByName.hinh_anhs && productByName.hinh_anhs.length > 0) {
                        if (typeof productByName.hinh_anhs[0] === 'object' && productByName.hinh_anhs[0].url) {
                            return productByName.hinh_anhs[0].url;
                        }
                        return productByName.hinh_anhs[0];
                    }
                }
            }
        }

        // Nếu không tìm thấy ảnh, trả về ảnh mặc định
        return 'https://via.placeholder.com/80x80';
    } catch (error) {
        console.error('Lỗi khi lấy hình ảnh sản phẩm:', error);
        return 'https://via.placeholder.com/80x80';
    }
};

// Thêm hàm để tải danh sách sản phẩm nếu chưa có
const loadProductsIfNeeded = async () => {
    try {
        // Kiểm tra xem store đã có danh sách sản phẩm chưa
        if (!store.getAllSanPham || store.getAllSanPham.length === 0) {
            console.log('DEBUG - Tải danh sách sản phẩm cho giỏ hàng');
            await store.getAllSP();
            console.log('DEBUG - Đã tải xong danh sách sản phẩm, số lượng:',
                store.getAllSanPham ? store.getAllSanPham.length : 0);

            // In ra một vài sản phẩm đầu tiên để kiểm tra cấu trúc
            if (store.getAllSanPham && store.getAllSanPham.length > 0) {
                console.log('DEBUG - Cấu trúc sản phẩm mẫu:',
                    JSON.stringify(store.getAllSanPham[0], null, 2));
            }
        } else {
            console.log('DEBUG - Đã có sẵn danh sách sản phẩm, số lượng:', store.getAllSanPham.length);
        }
    } catch (error) {
        console.error('DEBUG - Lỗi khi tải danh sách sản phẩm:', error);
    }
};

// Debug cấu trúc giỏ hàng
const debugCartStructure = () => {
    try {
        console.log('DEBUG - Cấu trúc giỏ hàng:');
        console.log('DEBUG - Số lượng sản phẩm trong giỏ:', cartItems.value.length);

        if (cartItems.value.length > 0) {
            console.log('DEBUG - Sản phẩm đầu tiên trong giỏ:',
                JSON.stringify(cartItems.value[0], null, 2));
        }
    } catch (e) {
        console.error('Lỗi khi parse userDetails từ sessionStorage:', e);
        return null;
    }
};

// ✅ FIXED: Cập nhật hàm getGioHang để gán dữ liệu vào cartItems
const getGioHang = async () => {
    try {
        const auth = getAuthenticatedUser();

        if (!auth) {
            console.log('🔄 [GUEST] Không đăng nhập, tải giỏ hàng từ localStorage');
            await loadCartFromLocalStorage();
            return;
        }

        // User logged in - load from database
        idKhachHang.value = auth.id;
        console.log(`✅ [${auth.type.toUpperCase()}] Đang tải giỏ hàng từ database:`, idKhachHang.value);

        try {
            const response = await banHangOnlineService.getGioHang(idKhachHang.value);
            gioHang.value = response;

            // 🔍 DEBUG: Xem structure của API response
            console.log('🔍 [DEBUG] API Response:', response);
            console.log('🔍 [DEBUG] First item structure:', response?.[0]);

            // Chuyển đổi dữ liệu từ API sang định dạng cartItems
            if (response && Array.isArray(response)) {
                cartItems.value = response.map(item => {
                    // 🔍 DEBUG: Xem từng item trước khi map
                    console.log('🔍 [DEBUG] Mapping item:', {
                        id_chi_tiet_san_pham: item.id_chi_tiet_san_pham,
                        id: item.id,
                        ten_san_pham: item.ten_san_pham,
                        name: item.name,
                        gia_ban: item.gia_ban,
                        price: item.price
                    });

                    // Kết hợp giá trị và đơn vị size nếu có
                    let formattedSize = '';
                    if (item.gia_tri !== undefined && item.don_vi !== undefined) {
                        formattedSize = `${item.gia_tri} ${item.don_vi}`;
                    } else if (item.gia_tri !== undefined && item.gia_tri !== '') {
                        formattedSize = item.gia_tri;
                    } else if (item.ten_kich_thuoc && item.ten_kich_thuoc.trim() !== '') {
                        formattedSize = item.ten_kich_thuoc;
                    } else if (item.gia_tri !== undefined && item.don_vi == undefined) {
                        formattedSize = item.gia_tri;
                    }

                    // ✅ FIXED: Sử dụng đúng field names từ backend
                    const cartItem = {
                        id: item.id_chi_tiet_san_pham || item.id,
                        name: item.ten_san_pham || item.name,
                        image: item.hinh_anh || item.image,
                        price: item.gia_ban || item.price || 0,
                        originalPrice: item.gia_goc || item.originalPrice || 0,
                        quantity: item.so_luong || item.quantity || 1,
                        maxQuantity: item.so_luong_ton || item.maxQuantity || 0,
                        color: item.ten_mau_sac || item.color,
                        size: formattedSize,
                        trang_thai: item.trang_thai
                    };

                    // 🔍 DEBUG: Xem item sau khi map
                    console.log('🔍 [DEBUG] Mapped cart item:', {
                        id: cartItem.id,
                        name: cartItem.name,
                        idType: typeof cartItem.id
                    });

                    return cartItem;
                });

                console.log(`✅ Đã load ${cartItems.value.length} sản phẩm từ database`);
                console.log('🔍 [DEBUG] Full cartItems:', cartItems.value);

                // Cập nhật số lượng tồn kho
                await updateAllMaxQuantities();
            } else {
                console.log('🔍 Giỏ hàng trống hoặc không có dữ liệu');
                cartItems.value = [];
            }
        } catch (apiError) {
            console.error(`❌ Lỗi khi tải giỏ hàng từ API [${auth.type}]:`, apiError);
            // Fallback to localStorage on API error
            await loadCartFromLocalStorage();
        }
    } catch (error) {
        console.error('❌ Lỗi trong getGioHang:', error);
        await loadCartFromLocalStorage();
    }
};

// Cập nhật số lượng tồn kho cho tất cả sản phẩm trong giỏ hàng và tự động điều chỉnh nếu cần
const updateAllMaxQuantities = async () => {
    try {
        let hasAdjustedQuantity = false;

        for (const item of cartItems.value) {
            // ✅ VALIDATION: Kiểm tra item.id trước khi gọi API
            if (!item.id) {
                console.warn('⚠️ [WARNING] Item has no ID, skipping:', {
                    name: item.name,
                    item: item
                });
                continue;
            }

            // 🔍 DEBUG: Log item trước khi gọi API
            console.log('🔍 [DEBUG] Processing item:', {
                id: item.id,
                name: item.name,
                idType: typeof item.id
            });

            // Gọi API để lấy số lượng tồn kho mới nhất
            await store.getMaxSoLuongSP(item.id);
            const maxAvailable = store.maxSoLuongSP || 0;

            console.log(`Cập nhật số lượng tối đa cho ${item.name}: ${maxAvailable} (hiện tại: ${item.quantity})`);

            // Cập nhật maxQuantity không xóa sản phẩm
            item.maxQuantity = maxAvailable;

            // Kiểm tra trạng thái sản phẩm
            try {
                await store.getTrangThaiCTSP(item.id);
                const isActive = store.trangThaiCTSP;

                // Cập nhật trạng thái cho sản phẩm
                item.trang_thai = isActive ? 'Hoạt động' : 'Không hoạt động';
            } catch (error) {
                console.error(`Lỗi khi kiểm tra trạng thái sản phẩm ${item.name}:`, error);
            }

            // Nếu sản phẩm hết hàng hoặc không hoạt động, bỏ qua điều chỉnh
            if (maxAvailable <= 0 || (item.trang_thai && item.trang_thai !== 'Hoạt động')) {
                console.log(`Sản phẩm ${item.name} không khả dụng, bỏ qua điều chỉnh số lượng`);
                continue;
            }

            // Chỉ điều chỉnh số lượng đối với sản phẩm có sẵn
            if (item.quantity > maxAvailable) {
                console.log(`Sản phẩm ${item.name} có số lượng ${item.quantity} vượt quá số lượng tồn kho ${maxAvailable}`);
                hasAdjustedQuantity = true;

                // Tính số lượng cần giảm
                const quantityToReduce = item.quantity - maxAvailable;

                const userDetailsStr = sessionStorage.getItem('userDetails');
                if (userDetailsStr) {
                    // Khách hàng đã đăng nhập
                    try {
                        const userDetails = JSON.parse(userDetailsStr);

                        // Gọi API để giảm số lượng
                        await store.xoaSoLuongSPGH(
                            userDetails.idKhachHang,
                            item.id,
                            quantityToReduce
                        );

                        // Cập nhật lại số lượng trong giỏ hàng
                        const oldQuantity = item.quantity;
                        item.quantity = maxAvailable;

                        // Hiển thị thông báo cho người dùng
                        toast.warning(`Số lượng sản phẩm "${item.name}" đã được điều chỉnh từ ${oldQuantity} xuống ${maxAvailable} do hàng tồn kho đã thay đổi`, {
                            position: "top-center",
                            autoClose: 4000
                        });
                    } catch (error) {
                        console.error('Lỗi khi điều chỉnh số lượng sản phẩm:', error);
                        toast.error('Có lỗi xảy ra khi điều chỉnh số lượng sản phẩm');
                    }
                } else {
                    // Khách hàng chưa đăng nhập
                    const oldQuantity = item.quantity;
                    item.quantity = maxAvailable;
                    saveCartToLocalStorage();

                    // Hiển thị thông báo cho người dùng
                    toast.warning(`Số lượng sản phẩm "${item.name}" đã được điều chỉnh từ ${oldQuantity} xuống ${maxAvailable} do hàng tồn kho đã thay đổi`, {
                        position: "top-center",
                        autoClose: 4000
                    });
                }
            }
        }

        if (hasAdjustedQuantity) {
            console.log('Đã tự động điều chỉnh số lượng cho một số sản phẩm do thay đổi tồn kho');
        } else {
            console.log('Tất cả sản phẩm đều có số lượng hợp lệ');
        }
    } catch (error) {
        console.error('Lỗi khi cập nhật số lượng tồn kho:', error);
    }
};

// Thêm hàm tải giỏ hàng với kiểm tra số lượng tồn kho
const getGioHangWithStockCheck = async () => {
    await getGioHang();  // Gọi hàm lấy giỏ hàng gốc
    await updateAllMaxQuantities();  // Ngay sau khi tải giỏ hàng, kiểm tra và điều chỉnh số lượng
};

// Thêm các hàm phụ trợ vào onMounted để đảm bảo kiểm tra chặt chẽ
onMounted(async () => {
    console.log('DEBUG - Component GioHang mounted');

    // Ưu tiên tải giỏ hàng với kiểm tra số lượng tồn kho
    await getGioHangWithStockCheck();

    debugCartStructure();
    await loadProductsIfNeeded();
    await store.getAllSP();

    // Áp dụng CSS toàn cục cho các component của Ant Design
    const styleSheet = document.createElement('style');
    styleSheet.type = 'text/css';
    styleSheet.innerText = `
        .ant-checkbox-checked .ant-checkbox-inner {
            background-color: #f33b47 !important;
            border-color: #f33b47 !important;
        }
        .ant-checkbox-wrapper:hover .ant-checkbox-inner,
        .ant-checkbox:hover .ant-checkbox-inner,
        .ant-checkbox-input:focus + .ant-checkbox-inner {
            border-color: #f33b47 !important;
        }
        .ant-btn-primary {
            background-color: #f33b47 !important;
            border-color: #f33b47 !important;
        }
        .ant-btn-primary:hover:not(:disabled) {
            background-color: #ff505e !important;
            border-color: #ff505e !important;
        }
        .ant-btn-primary:disabled {
            background-color: #d9d9d9 !important;
            border-color: #d9d9d9 !important;
            color: white !important;
        }
        
        /* Tăng z-index cho modal */
        .ant-modal-root, .ant-modal-mask, .ant-modal-wrap {
            z-index: 9999 !important;
        }

        .unavailable-item {
            opacity: 0.7;
            background-color: #f8f9fa;
        }

        .unavailable-item img {
            filter: grayscale(100%);
        }
    `;

    // Thêm style vào head
    document.head.appendChild(styleSheet);

    // Thêm sự kiện focus vào cửa sổ để tự động kiểm tra lại khi người dùng quay lại trang
    window.addEventListener('focus', async () => {
        console.log('Window focused - checking product quantities');
        await updateAllMaxQuantities();
    });
});
</script>

<style scoped>
:root {
    --primary-color: #f33b47;
    --primary-light: #ff6a76;
    --primary-dark: #d62638;
    --primary-hover: #ff505e;
    --primary-bg-light: #fff1f2;
    --text-dark: #333;
    --text-muted: #666;
    --border-light: #eee;
    --bg-light: #f8f9fa;
    --white: #fff;
    --danger: #f33b47;
    --success: #4caf50;
    --warning: #ff9800;
}

.cart-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    font-family: 'Roboto', sans-serif;
    color: var(--text-dark);
    background-color: #f8f9fa;
    min-height: 90vh;
    border-radius: 12px;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.03);
}

/* Breadcrumb styles */
.breadcrumb-container {
    margin-bottom: 30px;
    background-color: var(--white);
    padding: 12px 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.breadcrumb {
    display: flex;
    list-style: none;
    padding: 0;
    margin: 0;
}

.breadcrumb-item {
    display: inline-block;
    font-size: 14px;
}

.breadcrumb-item+.breadcrumb-item::before {
    content: '/';
    padding: 0 8px;
    color: var(--text-muted);
}

.breadcrumb-item a {
    color: var(--primary-color);
    text-decoration: none;
    font-weight: 500;
    transition: all 0.2s;
}

.breadcrumb-item a:hover {
    color: var(--primary-hover);
    text-decoration: underline;
}

.breadcrumb-item.active {
    color: var(--text-muted);
}

/* Cart header */
.cart-header {
    margin-bottom: 30px;
    background-color: var(--white);
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    position: relative;
    overflow: hidden;
}

.cart-header::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 4px;
    background: linear-gradient(to right, var(--primary-color), var(--primary-light));
}

.cart-header h1 {
    font-size: 28px;
    font-weight: 600;
    color: var(--text-dark);
    margin-bottom: 10px;
}

.cart-count {
    color: var(--text-muted);
    font-size: 14px;
}

/* Empty cart state */
.empty-cart {
    text-align: center;
    padding: 60px 20px;
    background-color: var(--white);
    border-radius: 12px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
    margin-bottom: 30px;
    position: relative;
    overflow: hidden;
    border: 1px dashed #eee;
}

.empty-cart-icon {
    font-size: 80px;
    color: var(--primary-light);
    margin-bottom: 20px;
    opacity: 0.8;
}

.empty-cart h2 {
    font-size: 26px;
    color: var(--text-dark);
    margin-bottom: 10px;
}

.empty-cart p {
    color: var(--text-muted);
    margin-bottom: 25px;
    font-size: 16px;
}

.empty-cart .ant-btn-primary {
    background-color: var(--primary-color);
    border-color: var(--primary-color);
    box-shadow: 0 4px 10px rgba(243, 59, 71, 0.2);
    transition: all 0.3s;
}

.empty-cart .ant-btn-primary:hover {
    background-color: var(--primary-hover);
    border-color: var(--primary-hover);
    box-shadow: 0 6px 15px rgba(243, 59, 71, 0.3);
    transform: translateY(-2px);
}

/* Cart items */
.cart-items {
    display: grid;
    grid-template-columns: 1fr 320px;
    gap: 20px;
}

@media (max-width: 992px) {
    .cart-items {
        grid-template-columns: 1fr;
    }
}

.cart-items-list {
    background-color: var(--white);
    border-radius: 12px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
    overflow: hidden;
}

.cart-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    background: linear-gradient(to right, #f8f9fa, #fff);
    border-bottom: 1px solid var(--border-light);
}

.select-all-text {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-dark);
}

.cart-item {
    display: flex;
    align-items: flex-start;
    padding: 20px;
    border-bottom: 1px solid var(--border-light);
    transition: all 0.2s;
}

.cart-item:hover {
    background-color: var(--primary-bg-light);
}

.cart-item:last-child {
    border-bottom: none;
}

.cart-item-content {
    display: flex;
    flex: 1;
    margin-left: 15px;
    align-items: center;
}

@media (max-width: 768px) {
    .cart-item-content {
        flex-direction: column;
        align-items: flex-start;
    }

    .col-quantity,
    .col-total,
    .col-action {
        margin-top: 15px;
        align-self: flex-start;
    }
}

.col-product {
    display: flex;
    align-items: center;
    flex: 1;
    margin-right: 15px;
}

.product-image {
    width: 90px;
    height: 90px;
    object-fit: cover;
    border-radius: 10px;
    margin-right: 15px;
    border: 1px solid var(--border-light);
    transition: all 0.3s;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
    overflow: hidden;
}

.cart-item:hover .product-image {
    transform: scale(1.03);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

.product-info {
    flex: 1;
}

.product-name {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 8px;
    color: var(--text-dark);
}

.product-variant {
    font-size: 13px;
    color: var(--text-muted);
    margin-bottom: 10px;
    background-color: var(--bg-light);
    padding: 4px 10px;
    border-radius: 4px;
    display: inline-block;
}

.product-variant span {
    margin-right: 10px;
}

.product-price {
    display: flex;
    align-items: center;
    gap: 8px;
}

.current-price {
    font-weight: 600;
    color: var(--primary-color);
    font-size: 16px;
}

.original-price {
    color: var(--text-muted);
    text-decoration: line-through;
    font-size: 13px;
}

.col-quantity {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    margin: 0 20px;
}

.quantity-controls {
    display: flex;
    align-items: center;
    border: 1px solid #ddd;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.quantity-btn {
    background-color: var(--bg-light);
    border: none;
    border-radius: 0;
    height: 34px;
    width: 34px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s;
}

.quantity-btn:hover:not(:disabled) {
    background-color: var(--primary-bg-light);
    color: var(--primary-color);
}

.quantity-value {
    padding: 0 12px;
    font-size: 15px;
    font-weight: 600;
    min-width: 40px;
    text-align: center;
    color: var(--text-dark);
}

.col-total {
    font-weight: 600;
    color: var(--text-dark);
    font-size: 18px;
    margin: 0 20px;
    min-width: 100px;
    text-align: right;
}

.col-action {
    display: flex;
    align-items: center;
    justify-content: flex-end;
}

.remove-btn {
    color: var(--primary-color);
    transition: all 0.2s;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
}

.remove-btn:hover {
    color: var(--primary-hover);
    background-color: var(--primary-bg-light);
}

/* Stock warning */
.stock-warning {
    margin-top: 8px;
    text-align: center;
    font-size: 12px;
}

.stock-warning .ant-tag {
    margin-right: 0;
    border-radius: 4px;
    background-color: #fff3e0;
    border-color: #ffcc80;
    color: #ef6c00;
}

/* Summary section */
.cart-summary {
    background-color: var(--white);
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
    height: fit-content;
    border: 1px solid var(--border-light);
    transition: all 0.3s;
}

.cart-summary:hover {
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

.summary-header {
    margin-bottom: 20px;
    border-bottom: 1px solid var(--border-light);
    padding-bottom: 15px;
    position: relative;
}

.summary-header::after {
    content: '';
    position: absolute;
    bottom: -1px;
    left: 0;
    width: 80px;
    height: 3px;
    background: linear-gradient(to right, var(--primary-color), var(--primary-light));
    border-radius: 3px;
}

.summary-header h2 {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-dark);
    margin: 0;
}

.summary-body {
    margin-bottom: 20px;
}

.summary-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    padding-bottom: 15px;
    border-bottom: 1px dashed var(--border-light);
}

.summary-row:last-of-type {
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid var(--border-light);
    font-size: 18px;
}

.summary-label {
    color: var(--text-muted);
    font-weight: 500;
}

.summary-value {
    font-weight: 600;
    color: var(--text-dark);
}

.summary-value.total-price {
    color: var(--primary-color);
    font-size: 22px;
    font-weight: 700;
}

.summary-note {
    margin-bottom: 20px;
    font-size: 13px;
    color: var(--text-muted);
    padding: 12px;
    background-color: var(--primary-bg-light);
    border-radius: 8px;
    border-left: 3px solid var(--primary-color);
}

.summary-note p {
    margin: 0;
}

.summary-actions {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.summary-actions .ant-btn-primary {
    background-color: var(--primary-color);
    border-color: var(--primary-color);
    height: 44px;
    font-size: 16px;
    font-weight: 500;
    box-shadow: 0 4px 10px rgba(243, 59, 71, 0.2);
    transition: all 0.3s;
}

.summary-actions .ant-btn-primary:hover:not(:disabled) {
    background-color: var(--primary-hover);
    border-color: var(--primary-hover);
    box-shadow: 0 6px 15px rgba(243, 59, 71, 0.3);
    transform: translateY(-2px);
}

.summary-actions .ant-btn {
    height: 44px;
    font-size: 15px;
    border-radius: 6px;
}

.mt-3 {
    margin-top: 12px;
}

/* Thêm class CSS cho nút tăng khi số lượng đã đạt tối đa */
.max-quantity-warning {
    background-color: var(--primary-bg-light) !important;
    border-color: var(--primary-light) !important;
    color: var(--primary-color) !important;
}

.max-quantity-warning:hover {
    background-color: #ffe8e9 !important;
    border-color: var(--primary-color) !important;
}

.max-quantity-message {
    margin-top: 5px;
    text-align: center;
    animation: pulse 1.5s infinite;
}

.max-quantity-message .ant-tag {
    margin-right: 0;
    border-radius: 4px;
    font-size: 10px;
    padding: 0 6px;
}

.remove-quantity-btn {
    color: var(--primary-color);
    background-color: var(--primary-bg-light);
    transition: all 0.3s;
}

.remove-quantity-btn:hover {
    color: white;
    background-color: var(--primary-color);
}

/* CSS cho sản phẩm không khả dụng */
.unavailable-item {
    opacity: 0.7;
    background-color: #f8f9fa;
}

.unavailable-item img {
    filter: grayscale(100%);
}

/* CSS cho thông báo sản phẩm không khả dụng */
.unavailable-notice {
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 20px;
    min-width: 180px;
    text-align: center;
}

.unavailable-notice .ant-tag {
    margin-right: 0;
    font-size: 12px;
    padding: 2px 10px;
    border-radius: 4px;
    background-color: #f5f5f5;
    border: 1px solid #d9d9d9;
    color: #999;
}

@keyframes pulse {
    0% {
        opacity: 0.6;
    }

    50% {
        opacity: 1;
    }

    100% {
        opacity: 0.6;
    }
}
</style>