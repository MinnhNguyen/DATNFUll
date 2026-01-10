<template>
  <div class="checkout-container">
    <!-- Breadcrumb -->
    <div class="breadcrumb-container">
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item"><a href="#">Trang chủ</a></li>
          <li class="breadcrumb-item"><a href="#">Giỏ hàng</a></li>
          <li class="breadcrumb-item active" aria-current="page">Thanh toán</li>
        </ol>
      </nav>
    </div>

    <!-- Order Status Timeline -->
    <div class="order-timeline">
      <div class="timeline-step" :class="{ active: currentStatus >= 1, completed: currentStatus > 1 }">
        <div class="step-icon">
          <shopping-cart-outlined v-if="currentStatus < 1" />
          <check-circle-outlined v-else />
        </div>
        <div class="step-label">Giỏ hàng</div>
      </div>
      <div class="timeline-connector" :class="{ active: currentStatus >= 1 }"></div>
      <div class="timeline-step" :class="{ active: currentStatus >= 2, completed: currentStatus > 2 }">
        <div class="step-icon">
          <form-outlined v-if="currentStatus < 2" />
          <check-circle-outlined v-else />
        </div>
        <div class="step-label">Thanh toán</div>
      </div>
      <div class="timeline-connector" :class="{ active: currentStatus >= 2 }"></div>
      <div class="timeline-step" :class="{ active: currentStatus >= 3, completed: currentStatus > 3 }">
        <div class="step-icon">
          <credit-card-outlined v-if="currentStatus < 3" />
          <check-circle-outlined v-else />
        </div>
        <div class="step-label">Đặt hàng</div>
      </div>
      <div class="timeline-connector" :class="{ active: currentStatus >= 3 }"></div>
      <div class="timeline-step" :class="{ active: currentStatus >= 4, completed: currentStatus > 4 }">
        <div class="step-icon">
          <gift-outlined v-if="currentStatus < 4" />
          <check-circle-outlined v-else />
        </div>
        <div class="step-label">Hoàn tất</div>
      </div>
    </div>

    <div class="checkout-main-content">
      <!-- Left Side: Customer Information and Payment Methods -->
      <div class="checkout-info-section">
        <div class="section-box">
          <h2 class="section-title">Thông tin giao hàng</h2>
          <div class="customer-info">
            <a-form :model="customer" layout="vertical" class="shipping-form" ref="customerForm"
              :rules="validationRules">
              <div class="form-row">
                <a-form-item label="Họ tên người nhận" name="ho_ten" required class="form-item">
                  <a-input v-model:value="customer.ho_ten" placeholder="Nhập họ tên người nhận" />
                </a-form-item>
                <a-form-item label="Số điện thoại" name="so_dien_thoai" required class="form-item">
                  <a-input v-model:value="customer.so_dien_thoai" placeholder="Nhập số điện thoại" />
                </a-form-item>
              </div>
              <div class="form-row">
                <a-form-item label="Email" name="email" required class="form-item">
                  <a-input v-model:value="customer.email" placeholder="Nhập email" />
                </a-form-item>
                <a-form-item label="Tỉnh/Thành phố" name="tinh_thanh_pho" required class="form-item">
                  <a-select v-model:value="customer.tinh_thanh_pho" placeholder="Chọn Tỉnh/Thành phố"
                    @change="handleProvinceChange" :loading="loadingProvinces">
                    <a-select-option v-for="province in provinces" :key="province.code" :value="province.name">
                      {{ province.name }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </div>
              <div class="form-row">
                <a-form-item label="Quận/Huyện" name="quan_huyen" required class="form-item">
                  <a-select v-model:value="customer.quan_huyen" placeholder="Chọn Quận/Huyện"
                    @change="handleDistrictChange" :loading="loadingDistricts" :disabled="!customer.tinh_thanh_pho">
                    <a-select-option v-for="district in districts" :key="district.code" :value="district.name">
                      {{ district.name }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="Phường/Xã" name="xa_phuong" required class="form-item">
                  <a-select v-model:value="customer.xa_phuong" placeholder="Chọn Phường/Xã" :loading="loadingWards"
                    :disabled="!customer.quan_huyen">
                    <a-select-option v-for="ward in wards" :key="ward.code" :value="ward.name">
                      {{ ward.name }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </div>
              <div class="form-row">
                <a-form-item label="Địa chỉ cụ thể" name="so_nha" required class="form-item full-width">
                  <a-textarea v-model:value="customer.so_nha" placeholder="Số nhà, tên đường..." :rows="2" />
                </a-form-item>
              </div>
            </a-form>
          </div>
        </div>

        <div class="section-box">
          <h2 class="section-title">Hình thức thanh toán</h2>
          <a-radio-group v-model:value="selectedPaymentMethod" class="payment-methods">
            <div class="payment-method-item">
              <a-radio value="cod" class="payment-radio">
                <div class="payment-content">
                  <div class="payment-icon">
                    <dollar-outlined />
                  </div>
                  <div class="payment-info">
                    <p class="payment-name">Thanh toán khi nhận hàng (COD)</p>
                    <p class="payment-desc">Thanh toán khi nhận hàng</p>
                  </div>
                </div>
              </a-radio>
            </div>

            <div class="payment-method-item">
              <a-radio value="vnpay" class="payment-radio">
                <div class="payment-content">
                  <div class="payment-icon">
                    <credit-card-outlined />
                  </div>
                  <div class="payment-info">
                    <p class="payment-name">VNPAY</p>
                    <p class="payment-desc">Thanh toán qua VNPAY</p>
                  </div>
                  <div class="payment-logo">
                    <img src="https://cdn.haitrieu.com/wp-content/uploads/2022/10/Icon-VNPAY-QR.png" alt="VNPAY"
                      class="online-logo" />
                  </div>
                </div>
              </a-radio>
            </div>

            <div class="payment-method-item">
              <a-radio value="online-qr" class="payment-radio">
                <div class="payment-content">
                  <div class="payment-icon">
                    <credit-card-outlined />
                  </div>
                  <div class="payment-info">
                    <p class="payment-name">Thanh toán QR Code</p>
                    <p class="payment-desc">PayOS hoặc ZaloPay - Quét mã QR ngay</p>
                  </div>
                  <div class="payment-logo">
                    <img src="../images/icon/logoVietQR.png" style="width: 50px; height: 50px;" alt="QR Payment"
                      class="online-logo" />
                  </div>
                </div>
              </a-radio>
            </div>
          </a-radio-group>
        </div>

        <div class="section-box">
          <h2 class="section-title">Ghi chú đơn hàng</h2>
          <a-textarea v-model:value="orderNote" placeholder="Nhập ghi chú cho đơn hàng (không bắt buộc)" :rows="4" />
        </div>
      </div>

      <!-- Right Side: Order Summary -->
      <div class="checkout-summary-section">
        <div class="section-box">
          <h2 class="section-title">Thông tin đơn hàng</h2>

          <div class="order-products">
            <div class="product-item" v-for="(item, index) in orderItems" :key="index">
              <div class="product-image">
                <img :src="item.hinh_anh" :alt="item.ten_san_pham" />
                <span class="product-quantity">{{ item.so_luong }}</span>
              </div>
              <div class="product-details">
                <p class="product-name">{{ item.ten_san_pham }}</p>
                <p class="product-variant">
                  Màu sắc: {{ item.ten_mau_sac }} <br>Size: {{ item.ten_kich_thuoc }}
                </p>
              </div>
              <div class="product-price">
                {{ formatCurrency(item.gia * item.so_luong) }}
              </div>
            </div>
          </div>

          <!-- Phần voucher chỉ hiển thị khi khách hàng đã đăng nhập -->
          <div class="coupon-section" v-if="isLoggedIn">
            <a-button type="primary" @click="showVoucherModal" class="select-voucher-btn" block>
              <gift-outlined /> Chọn voucher
            </a-button>
            <div class="applied-coupons" v-if="appliedCoupons.length > 0">
              <div class="applied-coupon" v-for="(coupon, index) in appliedCoupons" :key="index">
                <div class="coupon-info">
                  <div class="coupon-badge">
                    <span class="coupon-type">{{ coupon.loai === 'percent' ? 'GIẢM %' : 'GIẢM GIÁ'
                      }}</span>
                  </div>
                  <div class="coupon-details">
                    <p class="coupon-value">{{ coupon.loai === 'percent' ? `Giảm ${coupon.gia_tri}%`
                      : formatCurrency(coupon.gia_tri) }}</p>
                    <p class="coupon-code">{{ coupon.ma }}</p>
                    <p class="coupon-desc">{{ coupon.mo_ta }}</p>
                  </div>
                </div>
                <a-button type="text" danger @click="removeCoupon(index)" class="remove-coupon-btn">
                  <close-outlined />
                </a-button>
              </div>
            </div>
          </div>

          <!-- Thông báo về voucher khi chưa đăng nhập -->
          <div class="coupon-login-notice" v-if="!isLoggedIn">
            <p class="notice-text"><a href="/login-register/login" class="nav-link text-decoration-underline">Đăng
                nhập</a> để sử dụng
              voucher giảm giá</p>
          </div>

          <div class="order-summary">
            <div class="summary-item">
              <span class="item-label">Tạm tính:</span>
              <span class="item-value">{{ formatCurrency(subtotal) }}</span>
            </div>
            <div class="summary-item" v-if="discount > 0">
              <span class="item-label">Giảm giá:</span>
              <span class="item-value discount">-{{ formatCurrency(discount) }}</span>
            </div>
            <div class="summary-item">
              <span class="item-label">Phí vận chuyển:</span>
              <span class="item-value">{{ formatCurrency(shippingFee) }}</span>
            </div>
            <div class="summary-item total">
              <span class="item-label">Tổng cộng:</span>
              <span class="item-value">{{ formatCurrency(grandTotal) }}</span>
            </div>
          </div>

          <div class="order-actions">
            <a-button type="primary" size="large" @click="placeOrder" :loading="placing" block>
              Đặt hàng ngay
            </a-button>
          </div>
        </div>
        <div class="section-box address-info">
          <h2 class="section-title">Thông tin địa chỉ</h2>

          <!-- Danh sách địa chỉ của khách hàng đã đăng nhập -->
          <div v-if="store.danhSachDiaChi && store.danhSachDiaChi.length > 0" class="address-list">
            <a-radio-group v-model:value="selectedAddressId" class="address-radio-group">
              <div v-for="(address, index) in store.danhSachDiaChi" :key="index" class="address-item">
                <!-- Sử dụng index làm value cho radio -->
                <a-radio :value="index" class="address-radio">
                  <div class="address-content">
                    <p class="address-name">{{ customer.ho_ten }}
                      <span v-if="address.dia_chi_mac_dinh === 'true'" class="default-tag">[Mặc
                        định]</span>
                    </p>
                    <p class="address-phone">{{ customer.so_dien_thoai }}</p>
                    <p class="address-email">{{ customer.email }}</p>
                    <p class="address-full">
                      {{ address.so_nha }}, {{ address.xa_phuong }}, {{ address.quan_huyen }}, {{
                        address.tinh_thanh_pho }}
                    </p>
                  </div>
                </a-radio>
              </div>
            </a-radio-group>
          </div>
          <div v-else class="empty-address-message">
            Chưa có địa chỉ nào.
          </div>
        </div>
      </div>

    </div>


    <!-- Voucher Modal -->
    <a-modal v-model:visible="voucherModalVisible" title="Chọn voucher giảm giá" :footer="null" class="voucher-modal"
      :zIndex="1050" :mask-closable="false" v-if="isLoggedIn">
      <div class="voucher-header">
        <h3 class="voucher-header-title">Chọn voucher phù hợp nhất với đơn hàng của bạn</h3>
        <p class="voucher-header-desc">Chúng tôi đã sắp xếp các voucher theo mức giảm giá tốt nhất</p>
        <div class="applied-voucher-banner" v-if="appliedCoupons.length > 0">
          <div class="banner-content">
            <check-circle-outlined class="banner-icon" />
            <span>{{ getAppliedVoucherInfo() }}</span>
          </div>
          <a-button type="text" danger class="remove-btn-small"
            @click="appliedCoupons = []; message.success('Đã bỏ áp dụng voucher');">
            <close-outlined />
          </a-button>
        </div>
      </div>
      <div class="voucher-list">
        <div v-for="(voucher, index) in displayVouchers" :key="index" class="voucher-item" :class="{
          'disabled': !isVoucherValid(voucher),
          'selected': isVoucherSelected(voucher),
          'not-selected': !isVoucherSelected(voucher) && appliedCoupons.length > 0
        }" @click="toggleVoucher(voucher)">
          <div class="voucher-left">
            <div class="voucher-badge">
              <span class="voucher-type">{{ voucher.loai === 'percent' ? 'GIẢM %' : 'GIẢM GIÁ' }}</span>
            </div>
          </div>
          <div class="voucher-content">
            <p class="voucher-value">
              {{ voucher.loai === 'percent' ? `${voucher.gia_tri}%` : formatCurrency(voucher.gia_tri) }}
              <span class="voucher-currency">đ</span>
            </p>
            <p class="voucher-code">{{ voucher.ma }}</p>
            <p class="voucher-desc">{{ voucher.mo_ta }}</p>
            <p class="voucher-condition" v-if="voucher.dieu_kien > 0">
              Đơn tối thiểu {{ formatCurrency(voucher.dieu_kien) }} đ
            </p>
            <p class="voucher-expiry">HSD: {{ formatDateVN(voucher.ngay_het_han || "2023-12-31") }}</p>
          </div>
          <div class="voucher-right">
            <a-button :type="isVoucherSelected(voucher) ? 'default' : 'primary'" @click.stop="toggleVoucher(voucher)"
              :class="{ 'apply-btn': !isVoucherSelected(voucher), 'remove-btn': isVoucherSelected(voucher) }">
              {{ isVoucherSelected(voucher) ? 'Bỏ chọn' : 'Áp dụng' }}
            </a-button>
          </div>
          <div v-if="index === 0" class="best-saving-tag">TIẾT KIỆM NHẤT</div>
        </div>
      </div>
      <div class="voucher-empty" v-if="availableVouchers.length === 0">
        <img src="https://deo.shopeemobile.com/shopee/shopee-pcmall-live-sg/cart/9bdd8040b334d31946f49e36beaf32db.png"
          alt="Không có voucher" class="empty-img">
        <p>Không có voucher phù hợp với đơn hàng của bạn</p>
      </div>
      <div class="voucher-actions">
        <a-button type="primary" @click="voucherModalVisible = false">Xác nhận</a-button>
      </div>
    </a-modal>

    <!-- ✅ Phase 5: ZaloPay Payment Modal -->
    <a-modal v-model:visible="zaloPayModalVisible" title="Thanh toán ZaloPay" :footer="null" width="480px"
      :closable="false" :mask-closable="false" centered :zIndex="1000">

      <div class="zalopay-modal">
        <div class="qr-section" v-if="zaloPayQRUrl">
          <div class="qr-header">
            <h3>Quét mã QR bằng ZaloPay</h3>
            <p>Mở ứng dụng ZaloPay và quét mã QR bên dưới để thanh toán</p>
          </div>

          <div class="qr-container">
            <img :src="zaloPayQRUrl" alt="ZaloPay QR Code" class="qr-code" />
          </div>

          <div class="payment-info">
            <div class="info-row">
              <span class="label">Số tiền:</span>
              <span class="value">{{ formatCurrency(grandTotal) }}</span>
            </div>
            <div class="info-row">
              <span class="label">Mã đơn:</span>
              <span class="value">{{ createdOrderCode }}</span>
            </div>
          </div>

          <div class="payment-status" v-if="paymentInProgress">
            <a-spin size="large" />
            <p>Đang chờ thanh toán...</p>
            <p class="status-note">Vui lòng không đóng cửa sổ này</p>
          </div>
        </div>

        <div class="modal-actions">
          <a-button @click="cancelZaloPay" :disabled="paymentInProgress" size="large" block>
            Hủy thanh toán
          </a-button>
        </div>
      </div>
    </a-modal>

    <!-- Payment Method Modal -->
    <payment-method-modal v-model:visible="paymentModalVisible" :invoice-id="createdInvoiceId" :amount="grandTotal"
      @payment-success="handlePaymentSuccess" @payment-cancelled="handlePaymentCancelled" />

    <!-- ✅ Success Modal - Premium Design -->
    <a-modal v-model:visible="showSuccessModal" :footer="null" :closable="false" :maskClosable="false"
      class="success-modal" centered :width="420">
      <div class="success-modal-content">
        <!-- Success Icon with Animation -->
        <div class="success-icon-wrapper">
          <check-circle-outlined class="success-icon" />
        </div>

        <!-- Title -->
        <h2 class="success-title">Đặt hàng thành công</h2>

        <!-- Message -->
        <p class="success-message">
          Đơn hàng của bạn đã được tạo thành công với mã đơn hàng <strong>{{ successOrderCode }}</strong>.
          Chúng tôi sẽ liên hệ với bạn trong thời gian sớm nhất để xác nhận đơn hàng.
        </p>

        <!-- Action Button -->
        <a-button type="primary" size="large" block class="success-btn" @click="handleSuccessModalClose">
          Theo dõi đơn hàng
        </a-button>
      </div>
    </a-modal>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { message, Form, Modal } from 'ant-design-vue';
import axios from 'axios';
import { thanhToanService } from '@/services/thanhToan';
import PaymentMethodModal from './PaymentMethodModal.vue';
import {
  ShoppingCartOutlined,
  CheckCircleOutlined,
  FormOutlined,
  CreditCardOutlined,
  GiftOutlined,
  PlusOutlined,
  DollarOutlined,
  BankOutlined,
  CloseOutlined
} from '@ant-design/icons-vue';
import { useGbStore } from '@/stores/gbStore';
import { banHangOnlineService } from '@/services/banHangOnlineService';
import { banHangService } from '@/services/banHangService';
// ✅ Phase 1: ZaloPay Integration - Add QRCode library
import QRCode from 'qrcode';
// ✅ Phase 7: Add payment polling service for auto status checking
import { paymentPollingService } from '@/services/paymentPollingService.js';


const router = useRouter();
const route = useRoute();
const store = useGbStore();
// Timeline status
const currentStatus = ref(2); // 1: Cart, 2: Checkout, 3: Order, 4: Complete

// Customer information
const customer = ref({
  id: 1,
  ho_ten: '',
  so_dien_thoai: '',
  email: '',
  tinh_thanh_pho: '',
  quan_huyen: '',
  xa_phuong: '',
  so_nha: '',
  dia_chi_mac_dinh: 0  // Dùng 0 thay vì false
});

// Customer addresses
const customerAddresses = ref([]);
const selectedAddressId = ref(1);

// Payment methods
const selectedPaymentMethod = ref('');
const paymentModalVisible = ref(false);

// ✅ Phase 2: ZaloPay State Management
const zaloPayModalVisible = ref(false);
const zaloPayQRUrl = ref('');
const paymentInProgress = ref(false);
const createdInvoiceId = ref(null);
const createdOrderCode = ref(null); // Store order code
const showSuccessModal = ref(false); // Success modal visibility
const successOrderCode = ref(''); // Order code for success modal
const isConfirming = ref(false); // ✅ Flag để tránh duplicate confirm
// const selectedOnlineMethod = ref('vnpay');

// Order note
const orderNote = ref('');

// Order items
const orderItems = ref([]);

// Coupon code
const couponCode = ref('');
const applyingCoupon = ref(false);
const appliedCoupons = ref([]);

// Address modal
const addressModalVisible = ref(false);
const isEditingAddress = ref(false);
const addressForm = reactive({
  id: null,
  ten_nguoi_nhan: '',
  so_dien_thoai: '',
  email: '',
  tinh_thanh_pho: '',
  quan_huyen: '',
  xa_phuong: '',
  so_nha: '',
  dia_chi_mac_dinh: false
});

// Location data for address form
const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);
const loadingProvinces = ref(false);
const loadingDistricts = ref(false);
const loadingWards = ref(false);

// Order placement
const placing = ref(false);

// Form validation
const customerForm = ref(null);

// Validation rules
const validationRules = {
  ho_ten: [
    { required: true, message: 'Vui lòng nhập họ tên người nhận', trigger: 'blur' },
    { min: 2, message: 'Họ tên phải có ít nhất 2 ký tự', trigger: 'blur' }
  ],
  so_dien_thoai: [
    { required: true, message: 'Vui lòng nhập số điện thoại', trigger: 'blur' },
    { pattern: /^(0[3|5|7|8|9])+([0-9]{8})$/, message: 'Số điện thoại không hợp lệ', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Vui lòng nhập email', trigger: 'blur' },
    { type: 'email', message: 'Email không hợp lệ', trigger: 'blur' }
  ],
  tinh_thanh_pho: [
    { required: true, message: 'Vui lòng chọn Tỉnh/Thành phố', trigger: 'change' }
  ],
  quan_huyen: [
    { required: true, message: 'Vui lòng chọn Quận/Huyện', trigger: 'change' }
  ],
  xa_phuong: [
    { required: true, message: 'Vui lòng chọn Phường/Xã', trigger: 'change' }
  ],
  so_nha: [
    { required: true, message: 'Vui lòng nhập địa chỉ cụ thể', trigger: 'blur' },
    { min: 5, message: 'Địa chỉ phải có ít nhất 5 ký tự', trigger: 'blur' }
  ]
};

// Computed values for order summary
const subtotal = computed(() => {
  return orderItems.value.reduce((total, item) => {
    // Đảm bảo giá và số lượng đều là số
    const price = Number(item.gia || item.price || 0);
    const quantity = Number(item.so_luong || item.quantity || 1);
    return total + (price * quantity);
  }, 0);
});

// Calculate discount based on applied vouchers
const calculateDiscount = () => {
  // Kiểm tra đăng nhập - nếu chưa đăng nhập thì không áp dụng voucher
  if (!isLoggedIn.value) {
    return 0;
  }

  let totalDiscount = 0;
  const subTotal = Number(subtotal.value || 0);

  appliedCoupons.value.forEach(voucher => {
    let discountAmount = 0;

    if (voucher.loai === 'percent') {
      // Percentage discount
      const percentValue = Number(voucher.gia_tri || 0);
      discountAmount = subTotal * (percentValue / 100);

      // Cap discount at maximum value if set
      if (voucher.gia_tri_toi_da && discountAmount > Number(voucher.gia_tri_toi_da)) {
        discountAmount = Number(voucher.gia_tri_toi_da);
      }
    } else if (voucher.loai === 'fixed') {
      // Fixed amount discount
      discountAmount = Number(voucher.gia_tri || 0);
    }

    totalDiscount += discountAmount;
  });

  // QUAN TRỌNG: Voucher KHÔNG được vượt quá tổng tiền hàng (chưa tính ship)
  // Phí ship không được giảm bởi voucher!
  if (totalDiscount > subTotal) {
    console.log('[WARNING] Voucher vượt quá giá trị đơn hàng! Giới hạn:', subTotal);
    totalDiscount = subTotal;
  }

  return totalDiscount;
};

const discount = computed(() => {
  return Number(calculateDiscount() || 0);
});

const shippingFee = ref(0);

const calculateShippingFee = async () => {
  try {
    console.log('🚚 [SHIPPING] Bắt đầu tính phí vận chuyển');
    const kh = customer.value;

    // ✅ Validate customer info
    if (!kh.tinh_thanh_pho || !kh.quan_huyen) {
      console.log("⚠️ [SHIPPING] Chưa đủ thông tin địa chỉ");
      shippingFee.value = 0;
      return;
    }

    // ✅ Check for free shipping
    if (subtotal.value >= 2000000) {
      console.log("✅ [SHIPPING] Miễn phí vận chuyển (đơn > 2 triệu)");
      shippingFee.value = 0;
      return;
    }

    // ✅ Calculate real weight from cart items
    const totalWeight = orderItems.value.reduce((total, item) => {
      const itemWeight = item.can_nang || 500; // Default weight in grams
      const quantity = Number(item.so_luong || item.quantity || 1);
      return total + (quantity * itemWeight);
    }, 0);

    console.log(`📦 [SHIPPING] Total weight: ${totalWeight}g from ${orderItems.value.length} items`);

    // ✅ Use dynamic pickup location (can be configured later)
    const pickupProvince = "Hà Nội";
    const pickupDistrict = "Nam Từ Liêm";

    console.log(`📍 [SHIPPING] From: ${pickupProvince}, ${pickupDistrict} → To: ${kh.tinh_thanh_pho}, ${kh.quan_huyen}`);

    // Call API with real parameters
    const result = await banHangService.tinhPhiShip(
      pickupProvince,
      pickupDistrict,
      kh.tinh_thanh_pho,
      kh.quan_huyen,
      totalWeight,
      subtotal.value
    );

    console.log('💰 [SHIPPING] API result:', result);

    // ✅ Validate API response (result là NUMBER, không phải object)
    if (result && typeof result === 'number' && result > 0) {
      shippingFee.value = result;
      console.log(`✅ [SHIPPING] Phí vận chuyển: ${result} VNĐ`);
    } else if (result && !result.error) {
      // Fallback: nếu API upgrade lại trả object
      shippingFee.value = result.fee || result.ship_fee_only || 0;
      console.log(`✅ [SHIPPING] Phí vận chuyển: ${shippingFee.value} VNĐ`);
    } else {
      console.log('⚠️ [SHIPPING] API không trả về phí hợp lệ');
      shippingFee.value = 0;
      message.warning('Không thể tính phí vận chuyển, vui lòng thử lại');
    }

  } catch (error) {
    console.error('❌ [SHIPPING] Lỗi khi tính phí vận chuyển:', error);
    shippingFee.value = 0;

    // ✅ User-friendly error messages
    if (error.response?.status === 400) {
      message.error('Thông tin địa chỉ không hợp lệ. Vui lòng kiểm tra lại.');
    } else if (error.code === 'NETWORK_ERROR' || !navigator.onLine) {
      message.error('Mất kết nối. Vui lòng kiểm tra mạng và thử lại.');
    } else {
      message.error('Không thể tính phí vận chuyển. Vui lòng thử lại sau.');
    }
  }
};

const grandTotal = computed(() => {
  const subTotal = Number(subtotal.value || 0);
  const disc = Number(discount.value || 0);
  const shipping = Number(shippingFee.value || 0);

  // Tổng thanh toán = Tạm tính - Giảm giá + Phí ship
  // Đảm bảo không bao giờ âm
  const total = subTotal - disc + shipping;
  return Math.max(0, total); // Không cho phép âm
});

// Fetch customer data
const fetchCustomerData = async () => {
  try {
    // In a real app, this would fetch from an API
    // await store.getCustomerInfo();
    // customer.value = store.customerInfo;
    console.log('Fetched customer data');
  } catch (error) {
    console.error('Error fetching customer data:', error);
    message.error('Không thể tải thông tin khách hàng');
  }
};

// Kiểm tra trạng thái đăng nhập
const isLoggedIn = computed(() => {
  return sessionStorage.getItem('isLoggedIn') === 'true' || localStorage.getItem('isLoggedIn') === 'true';
});

// Helper function để lấy thông tin khách hàng từ storage
const getCustomerData = () => {
  // Thử lấy từ localStorage trước (nếu user chọn remember me)
  let customerDataStr = localStorage.getItem('khachHang');

  // Nếu không có trong localStorage, thử sessionStorage
  if (!customerDataStr) {
    customerDataStr = sessionStorage.getItem('khachHang');
  }

  if (!customerDataStr) {
    console.log('[DEBUG] Không tìm thấy khachHang trong storage');
    return null;
  }

  try {
    const data = JSON.parse(customerDataStr);
    console.log('[DEBUG] Customer data từ storage:', data);
    return data;
  } catch (error) {
    console.error('[DEBUG] Lỗi parse customer data:', error);
    return null;
  }
};

// Lấy danh sách địa chỉ khi KH đăng nhập
const fetchCustomerAddresses = async () => {
  try {
    console.log('[DEBUG] Bắt đầu fetchCustomerAddresses');

    // Sử dụng helper function
    const customerData = getCustomerData();

    if (!customerData) {
      console.log('[DEBUG] Không có thông tin khách hàng trong storage');
      return;
    }

    // Lấy idKhachHang từ customerData
    const idKhachHang = customerData.idKhachHang || customerData.id_khach_hang;

    if (!idKhachHang) {
      console.log('[DEBUG] Không tìm thấy idKhachHang trong customerData');
      console.log('[DEBUG] customerData:', customerData);
      return;
    }

    console.log('[DEBUG] ID Khách hàng:', idKhachHang);

    // Điền thông tin cơ bản
    customer.value.ho_ten = customerData.hoTen || customerData.ho_ten || '';
    customer.value.so_dien_thoai = customerData.soDienThoai || customerData.so_dien_thoai || customerData.sdt_nguoi_nhan || '';
    customer.value.email = customerData.email || '';

    // Gọi API lấy danh sách địa chỉ
    console.log('[DEBUG] Gọi API getDanhSachDiaChi với ID:', idKhachHang);
    await store.getDanhSachDiaChi(idKhachHang);
    console.log('[DEBUG] Danh sách địa chỉ từ API:', store.danhSachDiaChi);
    console.log('[DEBUG] Số lượng địa chỉ:', store.danhSachDiaChi ? store.danhSachDiaChi.length : 0);

    if (store.danhSachDiaChi && store.danhSachDiaChi.length > 0) {
      // Tìm index của địa chỉ mặc định
      const defaultAddressIndex = store.danhSachDiaChi.findIndex(addr => addr.dia_chi_mac_dinh === 'true');
      console.log('[DEBUG] Index địa chỉ mặc định:', defaultAddressIndex);

      // Nếu có địa chỉ mặc định, chọn nó. Nếu không, chọn địa chỉ đầu tiên
      const selectedIndex = defaultAddressIndex !== -1 ? defaultAddressIndex : 0;
      console.log('[DEBUG] Selected index:', selectedIndex);

      // Set selectedAddressId để chọn radio button
      selectedAddressId.value = selectedIndex;

      // Lấy địa chỉ được chọn
      const selectedAddr = store.danhSachDiaChi[selectedIndex];
      console.log('[DEBUG] Địa chỉ được chọn:', selectedAddr);

      // Cập nhật thông tin địa chỉ vào form
      if (selectedAddr) {
        customer.value = {
          ...customer.value, // Giữ thông tin cá nhân
          tinh_thanh_pho: selectedAddr.tinh_thanh_pho,
          quan_huyen: selectedAddr.quan_huyen,
          xa_phuong: selectedAddr.xa_phuong,
          so_nha: selectedAddr.so_nha
        };
        console.log('[DEBUG] Đã set địa chỉ mặc định:', selectedAddr);
      }
    } else {
      console.log('[DEBUG] KHÔNG CÓ ĐỊA CHỈ NÀO!');
    }
  } catch (error) {
    console.error('[DEBUG] Lỗi khi lấy danh sách địa chỉ:', error);
    message.error('Không thể tải địa chỉ khách hàng');
  }
};

// Watch selectedAddressId để update form khi user chọn địa chỉ khác
watch(selectedAddressId, (newIndex) => {
  if (newIndex !== null && store.danhSachDiaChi && store.danhSachDiaChi.length > 0) {
    const selectedAddr = store.danhSachDiaChi[newIndex];
    console.log('[DEBUG] User chọn địa chỉ index:', newIndex);
    console.log('[DEBUG] Địa chỉ được chọn:', selectedAddr);

    if (selectedAddr) {
      // Cập nhật form với địa chỉ được chọn
      customer.value.tinh_thanh_pho = selectedAddr.tinh_thanh_pho;
      customer.value.quan_huyen = selectedAddr.quan_huyen;
      customer.value.xa_phuong = selectedAddr.xa_phuong;
      customer.value.so_nha = selectedAddr.so_nha;

      console.log('[DEBUG] Đã update form với địa chỉ:', selectedAddr);

      // Trigger tính phí ship mới
      calculateShippingFee();
    }
  }
});

// ✅ Watch manual address input (for non-logged-in users)
watch(() => customer.value.quan_huyen, (newVal, oldVal) => {
  if (newVal && newVal !== oldVal && customer.value.tinh_thanh_pho) {
    console.log('🚚 [SHIPPING] Địa chỉ thay đổi, tính lại phí ship');
    calculateShippingFee();
  }
});

// Fetch order items from cart

// Fetch provinces from Vietnam location API
const fetchProvinces = async () => {
  try {
    loadingProvinces.value = true;
    const response = await axios.get('https://provinces.open-api.vn/api/p/');
    provinces.value = response.data;
    loadingProvinces.value = false;
  } catch (error) {
    console.error('Error fetching provinces:', error);
    message.error('Không thể tải danh sách tỉnh/thành phố');
    loadingProvinces.value = false;
  }
};

// Fetch districts based on selected province
const handleProvinceChange = async (provinceName) => {
  try {
    // Reset địa chỉ liên quan
    customer.value.quan_huyen = '';
    customer.value.xa_phuong = '';

    // Reset dữ liệu form thêm địa chỉ mới (nếu đang mở)
    addressForm.quan_huyen = '';
    addressForm.xa_phuong = '';

    // Reset danh sách quận/huyện, phường/xã
    districts.value = [];
    wards.value = [];

    loadingDistricts.value = true;
    const province = provinces.value.find(p => p.name === provinceName);
    if (province) {
      const response = await axios.get(`https://provinces.open-api.vn/api/p/${province.code}?depth=2`);
      districts.value = response.data.districts;
    }
    loadingDistricts.value = false;

    // ✅ Tính lại phí ship khi tỉnh thay đổi (sẽ reset về 0 vì chưa có huyện)
    calculateShippingFee();
  } catch (error) {
    console.error('Error fetching districts:', error);
    message.error('Không thể tải danh sách quận/huyện');
    loadingDistricts.value = false;
  }
};

// Fetch wards based on selected district
const handleDistrictChange = async (districtName) => {
  try {
    // Reset địa chỉ liên quan
    customer.value.xa_phuong = '';

    // Reset dữ liệu form thêm địa chỉ mới (nếu đang mở)
    addressForm.xa_phuong = '';

    // Reset danh sách phường/xã
    wards.value = [];

    loadingWards.value = true;
    const district = districts.value.find(d => d.name === districtName);
    if (district) {
      const response = await axios.get(`https://provinces.open-api.vn/api/d/${district.code}?depth=2`);
      wards.value = response.data.wards;
    }
    loadingWards.value = false;

    // ✅ Tính lại phí ship khi huyện thay đổi (có đủ tỉnh + huyện rồi)
    calculateShippingFee();
  } catch (error) {
    console.error('Error fetching wards:', error);
    message.error('Không thể tải danh sách phường/xã');
    loadingWards.value = false;
  }
};

// Edit existing address
const editAddress = (address) => {
  isEditingAddress.value = true;
  Object.assign(addressForm, {
    ...address,
    // Chuyển đổi 0/1 sang boolean
    dia_chi_mac_dinh: address.dia_chi_mac_dinh === 1
  });
  addressModalVisible.value = true;

  // Load districts and wards for the selected address
  if (addressForm.tinh_thanh_pho) {
    handleProvinceChange(addressForm.tinh_thanh_pho);
  }
  if (addressForm.quan_huyen) {
    handleDistrictChange(addressForm.quan_huyen);
  }
};



// Remove coupon
const removeCoupon = (index) => {
  appliedCoupons.value.splice(index, 1);
  message.success('Đã xóa mã giảm giá');
};

// ✅ Get applied voucher display info
const getAppliedVoucherInfo = () => {
  if (appliedCoupons.value.length === 0) return '';
  const voucher = appliedCoupons.value[0];
  const kieuGiam = voucher.kieuGiamGia || voucher.kieu_giam_gia || voucher.loai;
  const giaTriGiam = voucher.giaTriGiam || voucher.gia_tri_giam || voucher.gia_tri;
  const giaTriToiDa = voucher.giaTriToiDa || voucher.gia_tri_toi_da;

  if (kieuGiam === 'Phần trăm' || kieuGiam === 'percent') {
    return `Đã áp dụng: -${giaTriGiam}% ${giaTriToiDa ? `(tối đa ${formatCurrency(giaTriToiDa)})` : ''}`;
  } else {
    return `Đã áp dụng: -${formatCurrency(giaTriGiam)}`;
  }
};

// ✅ Check if voucher is valid
const isVoucherValid = (voucher) => {
  return true; // 🔓 BYPASS ALL VALIDATION

  const now = new Date();

  // ✅ Safe date parsing
  const startDate = voucher.ngay_bat_dau || voucher.ngayBatDau;
  const endDate = voucher.ngay_het_han || voucher.ngayHetHan;

  if (!startDate || !endDate) {
    return false; // No dates = invalid
  }

  const start = new Date(startDate);
  const end = new Date(endDate);

  // Check if dates are valid
  if (isNaN(start.getTime()) || isNaN(end.getTime())) {
    return false;
  }

  const status = voucher.trang_thai || voucher.trangThai;

  // ✅ Accept multiple valid status values
  const validStatuses = ['Đang diễn ra', 'Hoạt động', 'Active'];
  const isStatusValid = validStatuses.includes(status);

  // ✅ Date validation
  const isDateValid = now >= start && now <= end;

  // Debug in console
  if (!isStatusValid || !isDateValid) {
    console.log('⚠️ Voucher validation failed:', {
      ma: voucher.ma,
      status: status,
      isStatusValid,
      isDateValid,
      start: start.toISOString(),
      end: end.toISOString()
    });
  }

  return true; // 🔓 TEMP: Allow all vouchers
};

// ✅ Check if voucher is selected
const isVoucherSelected = (voucher) => {
  return appliedCoupons.value.some(v => v.id === voucher.id);
};

// ✅ Toggle voucher selection
const toggleVoucher = (voucher) => {
  console.log('🎫 [TOGGLE] Clicked:', voucher.ma, 'isValid:', isVoucherValid(voucher));

  if (!isVoucherValid(voucher)) {
    console.log('❌ [TOGGLE] Blocked');
    return;
  }

  const isSelected = isVoucherSelected(voucher);
  console.log('🎫 [TOGGLE] isSelected:', isSelected);

  if (isSelected) {
    appliedCoupons.value = appliedCoupons.value.filter(v => v.id !== voucher.id);
    console.log('✅ [TOGGLE] Removed');
    message.success('Đã bỏ chọn voucher');
  } else {
    appliedCoupons.value = [voucher];
    console.log('✅ [TOGGLE] Applied');
    message.success('Đã áp dụng voucher');
    voucherModalVisible.value = false;
  }
};

// Thêm vào sau các biến ref khác
const generatedInvoice = ref(null); // Lưu trữ đối tượng hóa đơn đã tạo

// Calculate complete order totals and create full invoice object
const calculateOrderTotals = () => {
  // Tạo đối tượng hóa đơn đầy đủ với tất cả thông tin cần thiết
  const currentDate = new Date();

  // Xác định phương thức thanh toán
  let paymentMethod = {
    loai: selectedPaymentMethod.value === 'cod' ? 'cod' : 'online',
    chi_tiet: selectedPaymentMethod.value,
    ten: selectedPaymentMethod.value === 'cod'
      ? 'Thanh toán khi nhận hàng'
      : (selectedPaymentMethod.value === 'vnpay' ? 'VNPAY' : 'PayOS')
  };

  const invoice = {
    // Thông tin đơn hàng
    hoaDon: {
      trang_thai: 'Hoàn thành',
      voucher: {
        id: isLoggedIn.value && appliedCoupons.value.length > 0 ? appliedCoupons.value[0].id : 0
      },
      isChuyen: false,
      khachHang: {
        idKhachHang: (() => {
          const customerData = getCustomerData();
          return customerData ? (customerData.idKhachHang || customerData.id_khach_hang || 0) : 0;
        })(),
      },
      id_khach_hang: (() => {
        const customerData = getCustomerData();
        return customerData ? (customerData.idKhachHang || customerData.id_khach_hang || 0) : 0;
      })(),
      sdt_nguoi_nhan: customer.value.so_dien_thoai,
      dia_chi: customer.value.so_nha + ', ' + customer.value.xa_phuong + ', ' + customer.value.quan_huyen + ', ' + customer.value.tinh_thanh_pho,
      email: customer.value.email,
      tong_tien_truoc_giam: subtotal.value,
      tong_tien_sau_giam: subtotal.value - discount.value,
      phi_van_chuyen: shippingFee.value,
      tong_tien_thanh_toan: grandTotal.value,
      ho_ten: customer.value.ho_ten,
      ghi_chu: orderNote.value,
      hinh_thuc_thanh_toan: selectedPaymentMethod.value === 'cod' ? 'Tiền mặt' : 'Chuyển khoản',
      // Thông tin thanh toán
      phuong_thuc_nhan_hang: "Giao hàng",
      // Tổng tiền đơn hàng
      tong_tien: {
        tam_tinh: Number(subtotal.value || 0),
        giam_gia: Number(discount.value || 0),
        phi_van_chuyen: Number(shippingFee.value || 0),
        tong_cong: Number(grandTotal.value || 0)
      },
    },
    hoaDonChiTiet: orderItems.value.map(item => ({
      chiTietSanPham: {
        id_chi_tiet_san_pham: item.id,
      },
      so_luong: item.so_luong,
      don_gia: item.gia * item.so_luong
    })),


    // Thông tin cho thanh toán online (nếu cần)
    payment_info: {
      productName: "Đơn hàng " + `MW-${currentDate.getTime()}`,
      description: `MenWear - ${orderItems.value.length} sản phẩm`,
      returnUrl: "http://localhost:5173/home",
      price: Number(grandTotal.value || 0),
      cancelUrl: "http://localhost:5173/thanhtoan-banhang"
    }
  };

  // Lưu hóa đơn vào biến ref để sử dụng sau này
  generatedInvoice.value = invoice;

  // Log để debug
  console.log('ĐỐI TƯỢNG HÓA ĐƠN ĐẦY ĐỦ:', JSON.stringify(invoice, null, 2));

  return invoice;
};

// ✅ Phase 3: ZaloPay Payment Handler
const handleZaloPayPayment = async () => {
  try {
    console.log('🚀 [ZALOPAY] Starting ZaloPay payment flow');

    // ✅ RESET: Clear old state trước khi tạo order mới
    createdInvoiceId.value = null;
    isConfirming.value = false;
    console.log('🔄 [ZALOPAY] Cleared old state');

    placing.value = true;

    // Step 1: Create order data
    const orderData = calculateOrderTotals();
    const hoaDon = {
      ...orderData.hoaDon,
      isChuyen: true,
      hinh_thuc_thanh_toan: 'Chuyển khoản',
      phuong_thuc_nhan_hang: 'ZaloPay'
    };

    // Step 2: Create order in database
    console.log('📝 [ZALOPAY] Creating order in database');
    const orderResponse = await banHangOnlineService.createPendingOrder(hoaDon);

    if (!orderResponse || !orderResponse.id_hoa_don) {
      throw new Error('Không thể tạo hóa đơn');
    }

    // Step 3: Create order details
    if (store.getIsThanhToanMuaNgay()) {
      await banHangOnlineService.createOrderChiTietMuaNgay(orderData.hoaDonChiTiet);
    } else {
      await banHangOnlineService.createOrderChiTiet(orderData.hoaDonChiTiet);
    }

    const idHoaDon = orderResponse.id_hoa_don;
    const maHoaDon = orderResponse.ma_hoa_don;
    console.log(`✅ [ZALOPAY] Order created: ${maHoaDon} (ID: ${idHoaDon})`);

    // Step 4: Initiate ZaloPay payment
    console.log('💳 [ZALOPAY] Creating ZaloPay transaction');
    const zaloPayResult = await thanhToanService.handleZaloPayPayment(
      idHoaDon,
      grandTotal.value
    );

    if (!zaloPayResult || !zaloPayResult.order_url) {
      throw new Error('Không thể tạo giao dịch ZaloPay');
    }

    // Step 5: Generate QR Code
    console.log('🔲 [ZALOPAY] Generating QR code');
    const qrDataUrl = await QRCode.toDataURL(zaloPayResult.order_url, {
      width: 300,
      margin: 2,
      color: { dark: '#000000', light: '#ffffff' }
    });

    // Step 6: Update state and show modal
    createdInvoiceId.value = idHoaDon;
    createdOrderCode.value = maHoaDon;
    zaloPayQRUrl.value = qrDataUrl;
    zaloPayModalVisible.value = true;
    paymentInProgress.value = true;
    currentStatus.value = 3; // Payment in progress

    // Save order code for success page
    localStorage.setItem('lastOrderCode', maHoaDon);
    store.setIsThanhToanMuaNgay(false);
    //thêm
    // store.setIsThanhToanMuaNgay(false);
    console.log('🐛 [DEBUG] Before polling start');

    // ✅ CHẠY START POLLING (handlers đã register trong onMounted)
    console.log('🔄 [ZALOPAY] Starting payment polling');
    await paymentPollingService.startPolling(idHoaDon, 'ZaloPay', {
      maxRetries: 40,  // 2 minutes
      retryDelay: 3000
    });

    console.log('✅ [ZALOPAY] Payment flow initiated successfully');

  } catch (error) {
    console.error('❌ [ZALOPAY] Payment error:', error);
    message.error('Không thể khởi tạo thanh toán ZaloPay: ' + error.message);

    // Cleanup on error
    zaloPayModalVisible.value = false;
    paymentInProgress.value = false;
  } finally {
    placing.value = false;
  }
};


// ✅ Phase 4: ZaloPay Status Handlers
const handleZaloPaySuccess = async () => {
  console.log('✅ [ZALOPAY] Payment successful');

  // ✅ COMPLETE IDEMPOTENCY: Block ALL duplicate calls
  if (isConfirming.value) {
    console.log('⏭️ [ZALOPAY] Already confirming, skipping duplicate call');
    return;
  }

  // Set flag IMMEDIATELY to block other calls
  isConfirming.value = true;

  // ✅ UPDATE ORDER: Gọi taoHoaDonWeb1 để update thành "Hoàn thành"
  try {
    console.log(`📝 [ZALOPAY] Confirming order ${createdInvoiceId.value}`);

    // Tạo hoaDon object với id_hoa_don + fields cần update
    const confirmData = {
      id_hoa_don: createdInvoiceId.value,
      isChuyen: true, // ✅ Always true - only runs once
      khachHang: {
        idKhachHang: customer.value.id || 0
      },
      phuong_thuc_nhan_hang: customer.value.deliveryMethod || 'Giao hàng'
    };

    await banHangOnlineService.confirmOrder(confirmData);
    console.log(`✅ [ZALOPAY] Order confirmed`);
  } catch (error) {
    console.error('❌ [ZALOPAY] Failed to confirm order:', error);
    // Continue anyway - backend callback cũng sẽ update
  }

  paymentInProgress.value = false;
  zaloPayModalVisible.value = false;
  currentStatus.value = 4; // Complete

  // ✅ Clear cart for both logged-in and guest users
  if (!store.getIsThanhToanMuaNgay()) {
    console.log('🛒 [CART] Clearing purchased items from cart...');

    // For guest users: Remove only purchased items from localStorage
    try {
      const cartData = localStorage.getItem('gb-sport-cart');
      if (cartData) {
        const cart = JSON.parse(cartData);
        console.log('📦 [CART] Current cart:', cart);
        console.log('🛍️ [CART] Items purchased:', orderItems.value);

        // Create a Set of purchased product IDs for fast lookup
        const purchasedProductIds = new Set(
          orderItems.value.map(item => item.id_chi_tiet_san_pham || item.productId || item.id)
        );

        // Filter out purchased items from cart
        const updatedCart = cart.filter(cartItem => {
          const cartProductId = cartItem.id_chi_tiet_san_pham || cartItem.productId || cartItem.id;
          const isPurchased = purchasedProductIds.has(cartProductId);

          if (isPurchased) {
            console.log(`🗑️ [CART] Removing purchased item: ${cartItem.ten_san_pham || cartItem.name}`);
          }

          return !isPurchased; // Keep items that were NOT purchased
        });

        // Save updated cart back to localStorage
        if (updatedCart.length > 0) {
          localStorage.setItem('gb-sport-cart', JSON.stringify(updatedCart));
          console.log(`✅ [CART] Updated localStorage cart (${updatedCart.length} items remaining)`);
        } else {
          localStorage.removeItem('gb-sport-cart');
          console.log('✅ [CART] All items purchased, removed localStorage cart');
        }
      }
    } catch (error) {
      console.error('❌ [CART] Failed to update localStorage cart:', error);
      // Fallback: just remove the entire cart
      localStorage.removeItem('gb-sport-cart');
    }

    // For logged-in users: Refresh cart from backend
    try {
      const userInfo = localStorage.getItem('userInfo');
      if (userInfo) {
        const customerData = JSON.parse(userInfo);
        const customerId = customerData?.idKhachHang || customerData?.id_khach_hang;

        if (customerId) {
          console.log('👤 [CART] Logged-in user detected, refreshing cart...');
          await store.getGioHang(customerId);
          console.log('✅ [CART] Refreshed cart from backend');
        }
      }
    } catch (error) {
      console.error('❌ [CART] Failed to refresh cart:', error);
    }

    // Dispatch event to force header cart badge update with actual count
    try {
      // Wait for Vue to finish DOM updates
      await nextTick();

      // Small delay to ensure localStorage writes complete
      setTimeout(async () => {
        const cartData = localStorage.getItem('gb-sport-cart');
        let remainingCount = 0;

        if (cartData) {
          const cart = JSON.parse(cartData);
          remainingCount = cart.reduce((total, item) => {
            return total + (item.quantity || item.so_luong || 1);
          }, 0);
        }

        // Check if user is logged in for backend count
        const userInfo = localStorage.getItem('userInfo');
        if (userInfo) {
          // For logged-in users, get count from store
          const storeCart = store.gioHang || [];
          remainingCount = storeCart.reduce((total, item) => {
            return total + (item.quantity || item.so_luong || 1);
          }, 0);
        }

        window.dispatchEvent(new CustomEvent('cart-updated', {
          detail: { count: remainingCount }
        }));
        console.log(`📢 [CART] Dispatched cart-updated event (count: ${remainingCount})`);
      }, 150); // 150ms delay to ensure all updates complete
    } catch (error) {
      console.error('❌ [CART] Failed to calculate cart count:', error);
      window.dispatchEvent(new CustomEvent('cart-updated', { detail: { count: 0 } }));
    }
  }

  // ✅ Show success modal
  showSuccessModal.value = true;
  successOrderCode.value = createdOrderCode.value || 'N/A';
};

const handleZaloPayFailure = (error) => {
  console.error('❌ [ZALOPAY] Payment failed:', error);

  paymentInProgress.value = false;
  zaloPayModalVisible.value = false;

  message.error('Thanh toán ZaloPay thất bại: ' + (error?.message || 'Vui lòng thử lại'));

  // Offer retry
  Modal.confirm({
    title: 'Thanh toán thất bại',
    content: 'Có lỗi xảy ra trong quá trình thanh toán. Bạn có muốn thử lại?',
    okText: 'Thử lại',
    cancelText: 'Hủy',
    onOk: () => handleZaloPayPayment(),
    onCancel: () => {
      currentStatus.value = 2;
    }
  });
};

const cancelZaloPay = () => {
  console.log('❌ [ZALOPAY] User cancelled payment');
  zaloPayModalVisible.value = false;
  paymentInProgress.value = false;
  qrCodeUrl.value = '';

  // Stop polling if active
  if (createdInvoiceId.value) {
    paymentPollingService.stop(createdInvoiceId.value);
  }
};

// ✅ Handle success modal close
const handleSuccessModalClose = () => {
  showSuccessModal.value = false;
  router.push('/home');
};


// Place order - updated with form validation
const placeOrder = async () => {
  try {
    // Validate form
    await customerForm.value.validate();

    // Kiểm tra phương thức thanh toán
    if (!selectedPaymentMethod.value) {
      message.warning('Vui lòng chọn phương thức thanh toán');
      return;
    }

    placing.value = true;

    // Tạo đối tượng hóa đơn đầy đủ
    const orderData = calculateOrderTotals();
    console.log('ĐỐI TƯỢNG HÓA ĐƠN ĐẦY ĐỦ:', JSON.stringify(orderData.hoaDon, null, 2));
    const hoaDon = orderData.hoaDon;
    const hoaDonChiTiet = orderData.hoaDonChiTiet;
    console.log('ĐỐI TƯỢNG HÓA ĐƠN CHI TIẾT:', JSON.stringify(hoaDonChiTiet, null, 2));

    // Lưu hóa đơn vào store nếu có (tùy chọn)
    if (store.setInvoiceData) {
      store.setInvoiceData(orderData);
    }

    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000));

    // Handle different payment methods
    if (selectedPaymentMethod.value === 'cod') {
      // COD payment - immediately mark as ordered
      try {
        // Tạo hóa đơn trong hệ thống
        const response = await banHangOnlineService.createOrder(hoaDon);
        let responseChiTiet;
        if (store.getIsThanhToanMuaNgay()) {
          responseChiTiet = await banHangOnlineService.createOrderChiTietMuaNgay(orderData.hoaDonChiTiet);
        } else {
          responseChiTiet = await banHangOnlineService.createOrderChiTiet(orderData.hoaDonChiTiet);
        }
        console.log('Response từ server:', response);
        console.log('Response chi tiết từ server:', responseChiTiet);

        // Lưu mã hóa đơn vào localStorage
        if (response && response.ma_hoa_don) {
          localStorage.setItem('lastOrderCode', response.ma_hoa_don);
          store.setIsThanhToanMuaNgay(false);
        }

        // Cập nhật trạng thái đơn hàng
        currentStatus.value = 3;

        // Hiển thị thông báo thành công và chuyển đến trang hoàn tất
        setTimeout(() => {
          currentStatus.value = 4;
          message.success('Đặt hàng thành công! Cảm ơn bạn đã mua hàng.');

          // Hiển thị modal xác nhận đặt hàng thành công
          Modal.success({
            title: 'Đặt hàng thành công',
            content: `Đơn hàng của bạn đã được tạo thành công với mã đơn hàng ${response.ma_hoa_don}. Chúng tôi sẽ liên hệ với bạn trong thời gian sớm nhất để xác nhận đơn hàng.`,
            okText: 'Theo dõi đơn hàng',
            onOk: () => {
              router.push(`/tracuudonhang-banhang?code=${response.ma_hoa_don}`);
            }
          });

          // Xóa giỏ hàng sau khi đặt hàng thành công

        }, 1000);
        if (response && responseChiTiet) {
          // Lấy danh sách sản phẩm đã thanh toán
          const paidProducts = orderData.hoaDonChiTiet.map(item => {
            return {
              id: item.chiTietSanPham.id_chi_tiet_san_pham,
              quantity: item.so_luong
            };
          });

          console.log('Sản phẩm đã thanh toán:', paidProducts);

          // Lấy giỏ hàng hiện tại
          const currentCart = JSON.parse(localStorage.getItem('gb-sport-cart') || '[]');
          console.log('Giỏ hàng hiện tại:', currentCart);

          // Tạo bản sao giỏ hàng để xử lý
          const updatedCart = [];

          // Xử lý từng sản phẩm trong giỏ hàng
          currentCart.forEach(cartItem => {
            // Get cart item ID (support multiple field names)
            const cartItemId = cartItem.id_chi_tiet_san_pham || cartItem.id || cartItem.productId;

            // Tìm sản phẩm tương ứng trong danh sách đã thanh toán
            const paidItem = paidProducts.find(paid => paid.id === cartItemId);

            if (paidItem) {
              // Nếu sản phẩm có trong đơn hàng, trừ số lượng
              const remainingQuantity = cartItem.quantity - paidItem.quantity;

              // Chỉ giữ lại trong giỏ hàng nếu số lượng > 0
              if (remainingQuantity > 0) {
                updatedCart.push({
                  ...cartItem,
                  quantity: remainingQuantity
                });
              }
            } else {
              // Nếu sản phẩm không có trong đơn hàng, giữ nguyên
              updatedCart.push(cartItem);
            }
          });

          console.log('Giỏ hàng sau khi cập nhật:', updatedCart);

          if (updatedCart.length > 0) {
            // Nếu còn sản phẩm trong giỏ hàng, cập nhật lại giỏ hàng
            localStorage.setItem('gb-sport-cart', JSON.stringify(updatedCart));
          } else {
            // Nếu không còn sản phẩm nào, xóa giỏ hàng
            localStorage.removeItem('gb-sport-cart');
          }

          // Dispatch event to update header cart badge
          await nextTick();
          setTimeout(() => {
            const remainingCount = updatedCart.reduce((total, item) => {
              return total + (item.quantity || item.so_luong || 1);
            }, 0);
            window.dispatchEvent(new CustomEvent('cart-updated', {
              detail: { count: remainingCount }
            }));
            console.log(`📢 [CART] Dispatched cart-updated event (count: ${remainingCount})`);
          }, 100);
        }
      } catch (error) {
        console.error('Lỗi khi tạo đơn hàng COD:', error);
        message.error('Có lỗi xảy ra khi tạo đơn hàng. Vui lòng thử lại sau.');
      }
    } else if (selectedPaymentMethod.value === 'online-qr') {
      // ✅ Phase 2: ZaloPay Integration - Call handler instead of old logic
      await handleZaloPayPayment();
    } else if (selectedPaymentMethod.value === 'payos') {
      try {
        hoaDon.isChuyen = true;
        hoaDon.hinh_thuc_thanh_toan = 'Chuyển khoản';
        hoaDon.phuong_thuc_nhan_hang = {
          loai: 'online',
          chi_tiet: 'payos',
          ten: 'PayOS'
        };
        console.log('Hóa đơn sau khi đã gán isChuyen = true:', JSON.stringify(hoaDon, null, 2));
        localStorage.setItem('hoaDon', JSON.stringify(hoaDon));
        localStorage.setItem('hoaDonChiTiet', JSON.stringify(orderData.hoaDonChiTiet));
        localStorage.setItem('isThanhToanMuaNgay', store.getIsThanhToanMuaNgay());
        // Đặt URL callback để xử lý sau khi thanh toán
        const returnUrl = window.location.origin + '/payment-callback';
        orderData.payment_info.returnUrl = returnUrl;

        // Chuyển đến trang thanh toán PayOS
        const responseThanhToan = await thanhToanService.handlePayOSPayment(orderData.payment_info);
        console.log('Response từ payos:', responseThanhToan);
        localStorage.setItem('responseThanhToan', JSON.stringify(responseThanhToan));
      } catch (error) {
        console.error('Lỗi khi xử lý thanh toán PayOS:', error);
        message.error('Có lỗi xảy ra khi xử lý thanh toán. Vui lòng thử lại sau.');
      }
    } else if (selectedPaymentMethod.value === 'vnpay') {
      // Redirect to VNPAY payment gateway
      message.info('Đang chuyển hướng đến cổng thanh toán VNPAY...');

      // Gán isChuyen = true cho hoaDon
      hoaDon.isChuyen = true;
      hoaDon.hinh_thuc_thanh_toan = 'Chuyển khoản';
      hoaDon.phuong_thuc_nhan_hang = {
        loai: 'online',
        chi_tiet: 'vnpay',
        ten: 'VNPAY'
      };

      const response = await banHangOnlineService.createOrder(hoaDon);
      let responseChiTiet;
      if (store.getIsThanhToanMuaNgay()) {
        responseChiTiet = await banHangOnlineService.createOrderChiTietMuaNgay(orderData.hoaDonChiTiet);
      } else {
        responseChiTiet = await banHangOnlineService.createOrderChiTiet(orderData.hoaDonChiTiet);
      }
      console.log('Response từ server:', response);
      console.log('Response chi tiết từ server:', responseChiTiet);
      if (response && responseChiTiet) {
        store.setIsThanhToanMuaNgay(false);
      }
      // Lưu hóa đơn đã được cập nhật vào localStorage
      localStorage.setItem('hoaDon', JSON.stringify(hoaDon));
      if (response && responseChiTiet) {
        // Lấy danh sách sản phẩm đã thanh toán
        const paidProducts = orderData.hoaDonChiTiet.map(item => {
          return {
            id: item.chiTietSanPham.id_chi_tiet_san_pham,
            quantity: item.so_luong
          };
        });

        console.log('Sản phẩm đã thanh toán:', paidProducts);

        // Lấy giỏ hàng hiện tại
        const currentCart = JSON.parse(localStorage.getItem('gb-sport-cart') || '[]');
        console.log('Giỏ hàng hiện tại:', currentCart);

        // Tạo bản sao giỏ hàng để xử lý
        const updatedCart = [];

        // Xử lý từng sản phẩm trong giỏ hàng
        currentCart.forEach(cartItem => {
          // Tìm sản phẩm tương ứng trong danh sách đã thanh toán
          const paidItem = paidProducts.find(paid => paid.id === cartItem.id);

          if (paidItem) {
            // Nếu sản phẩm có trong đơn hàng, trừ số lượng
            const remainingQuantity = cartItem.quantity - paidItem.quantity;

            // Chỉ giữ lại trong giỏ hàng nếu số lượng > 0
            if (remainingQuantity > 0) {
              updatedCart.push({
                ...cartItem,
                quantity: remainingQuantity
              });
            }
          } else {
            // Nếu sản phẩm không có trong đơn hàng, giữ nguyên
            updatedCart.push(cartItem);
          }
        });

        console.log('Giỏ hàng sau khi cập nhật:', updatedCart);

        if (updatedCart.length > 0) {
          // Nếu còn sản phẩm trong giỏ hàng, cập nhật lại giỏ hàng
          localStorage.setItem('gb-sport-cart', JSON.stringify(updatedCart));
        } else {
          // Nếu không còn sản phẩm nào, xóa giỏ hàng
          localStorage.removeItem('gb-sport-cart');
        }
      }
      // Đặt URL callback để xử lý sau khi thanh toán
      const returnUrl = window.location.origin + '/payment-callback';
      orderData.payment_info.returnUrl = returnUrl;
    }

    placing.value = false;

    // Trả về đối tượng hóa đơn để sử dụng bên ngoài nếu cần
    return generatedInvoice.value;
  } catch (error) {
    console.error('Lỗi khi đặt hàng:', error);

    // Hiển thị lỗi từ validation (nếu có)
    if (error.errorFields) {
      message.error('Vui lòng điền đầy đủ thông tin giao hàng');
    } else {
      message.error('Không thể đặt hàng. Vui lòng thử lại sau.');
    }

    placing.value = false;
    return null;
  }
};

// Xử lý khi thanh toán thành công từ modal
const handlePaymentSuccess = (data) => {
  console.log('Payment success:', data);

  currentStatus.value = 4;
  message.success('Thanh toán thành công!');

  Modal.success({
    title: 'Thanh toán thành công',
    content: `Đơn hàng của bạn đã được thanh toán thành công qua ${data.method === 'payos' ? 'PayOS' : 'ZaloPay'}. Cảm ơn bạn đã mua hàng!`,
    okText: 'Về trang chủ',
    onOk: () => {
      router.push('/home');
    }
  });
};

// Xử lý khi hủy thanh toán từ modal
const handlePaymentCancelled = () => {
  console.log('Payment cancelled');
  message.info('Bạn đã hủy thanh toán');
};

// Format currency
const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

// Hàm lấy ngày hiện tại theo múi giờ +7 (Việt Nam)
const getCurrentDateVN = () => {
  const date = new Date();
  // Chuyển đổi thành múi giờ Việt Nam (UTC+7)
  const vnTime = new Date(date.getTime() + (7 * 60 * 60 * 1000));
  return vnTime;
};

// Hàm định dạng ngày giờ theo định dạng Việt Nam
const formatDateVN = (dateString) => {
  const date = new Date(dateString);
  // Chuyển đổi thành múi giờ Việt Nam (UTC+7)
  const vnTime = new Date(date.getTime() + (7 * 60 * 60 * 1000));
  return vnTime.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
};

// Initialize data on component mount
onMounted(async () => {
  // ✅ REGISTER POLLING HANDLERS CHỈ 1 LẦN
  console.log('🔧 [INIT] Registering payment polling handlers');
  paymentPollingService.on('paymentSuccess', handleZaloPaySuccess);
  paymentPollingService.on('paymentFailure', handleZaloPayFailure);
  paymentPollingService.on('paymentTimeout', () => {
    console.log('⏰ [ZALOPAY] Payment timeout');
    paymentInProgress.value = false;
    zaloPayModalVisible.value = false;
    message.warning('Thời gian thanh toán đã hết. Vui lòng tạo đơn hàng mới.', 5);
    currentStatus.value = 2;
  });

  // Lấy dữ liệu sản phẩm từ store
  orderItems.value = store.checkoutItems || [];
  console.log('Order items từ store:', orderItems.value);

  // Đảm bảo rằng orderItems đã được tải và subtotal đã được tính toán
  await nextTick();

  // Tính toán subtotal
  const tongTien = orderItems.value.reduce((total, item) => {
    return total + (Number(item.gia || 0) * Number(item.so_luong || 1));
  }, 0);

  console.log('Tổng tiền đơn hàng:', tongTien);

  try {
    // Gọi API lấy voucher
    const voucherResponse = await store.getVoucherByGiaTruyen(tongTien);
    console.log('Voucher phù hợp:', voucherResponse);

    // Lưu vào biến availableVouchers để hiển thị
    if (voucherResponse && Array.isArray(voucherResponse)) {
      // Sắp xếp voucher theo số tiền giảm giảm dần (nhiều nhất lên đầu)
      availableVouchers.value = voucherResponse.sort((a, b) => {
        // Đảm bảo so_tien_giam là số
        const aValue = Number(a.so_tien_giam) || 0;
        const bValue = Number(b.so_tien_giam) || 0;
        return bValue - aValue;
      }).map(voucher => ({
        id: voucher.id_voucher,
        ma: voucher.ma_voucher,
        ten: voucher.ten_voucher,
        mo_ta: voucher.mo_ta,
        loai: voucher.kieu_giam_gia === 'Phần trăm' ? 'percent' : 'fixed',
        gia_tri: voucher.gia_tri_giam,
        dieu_kien: voucher.gia_tri_toi_thieu,
        gia_tri_toi_da: voucher.gia_tri_toi_da,
        ngay_bat_dau: voucher.ngay_tao,
        ngay_het_han: voucher.ngay_het_han,
        trang_thai: voucher.trang_thai,
        so_luong: voucher.so_luong,
        so_tien_giam: Number(voucher.so_tien_giam) || 0,
        gia_sau_giam: Number(voucher.gia_sau_giam) || 0
      }));

      console.log('Vouchers đã sắp xếp:', availableVouchers.value);

      // Tự động áp dụng voucher tốt nhất nếu có voucher hợp lệ
      if (availableVouchers.value.length > 0 && isVoucherValid(availableVouchers.value[0])) {
        // Xóa các voucher đã áp dụng trước để tránh trùng lặp
        appliedCoupons.value = [];
        // Áp dụng voucher tốt nhất
        appliedCoupons.value.push(availableVouchers.value[0]);
        message.success(`Đã tự động áp dụng voucher ${availableVouchers.value[0].ma} giảm ${formatCurrency(availableVouchers.value[0].so_tien_giam)}`);
      }
    }
  } catch (error) {
    console.error('Lỗi khi lấy voucher:', error);
  }

  calculateShippingFee();

  // Fetch initial data
  await Promise.all([
    fetchCustomerData(),
    fetchCustomerAddresses(),
    fetchProvinces()
  ]);
});

// Tính toán availableVouchers với voucher đã chọn được đưa lên đầu
const sortedVouchers = computed(() => {
  if (availableVouchers.value.length === 0) return [];

  // Tạo bản sao mảng để không ảnh hưởng đến mảng gốc
  const sorted = [...availableVouchers.value];

  // Nếu có voucher đã được chọn, đưa nó lên đầu tiên
  if (appliedCoupons.value.length > 0) {
    const selectedVoucherId = appliedCoupons.value[0].id;
    const selectedIndex = sorted.findIndex(v => v.id === selectedVoucherId);

    if (selectedIndex > -1) {
      // Đưa voucher đã chọn lên đầu danh sách
      const selectedVoucher = sorted.splice(selectedIndex, 1)[0];
      sorted.unshift(selectedVoucher);
    }
  }

  return sorted;
});

// Voucher management
const voucherModalVisible = ref(false);
const availableVouchers = ref([]);
const allVouchers = ref([]);

// Function to show voucher modal
const showVoucherModal = () => {
  voucherModalVisible.value = true;
};

// Filter valid vouchers based on current order and date
const filterValidVouchers = () => {
  const currentDate = getCurrentDateVN();

  availableVouchers.value = allVouchers.value.filter(voucher => {
    // Kiểm tra ngày hiệu lực
    const startDate = new Date(voucher.ngayBatDau);
    const endDate = new Date(voucher.ngayHetHan);
    const isDateValid = startDate <= currentDate && endDate >= currentDate;

    // Kiểm tra trạng thái voucher
    const isStatusValid = voucher.trangThai === 'Đang diễn ra';

    // Kiểm tra số lượng còn lại
    const hasQuantity = voucher.soLuong > 0;

    // Kiểm tra giá trị tối thiểu của đơn hàng
    const meetsMinValue = subtotal.value >= voucher.giaTriToiThieu;

    // Lưu thông tin kiểm tra vào voucher để hiển thị lý do nếu không thỏa mãn
    return isDateValid && isStatusValid && hasQuantity && meetsMinValue;
  }).map(voucher => {
    return {
      id: voucher.id,
      ma: voucher.maVoucher,
      ten: voucher.tenVoucher,
      mo_ta: voucher.moTa,
      loai: voucher.kieuGiamGia === 'Phần trăm' ? 'percent' : 'fixed',
      gia_tri: voucher.giaTriGiam,
      dieu_kien: voucher.giaTriToiThieu,
      gia_tri_toi_da: voucher.giaTriToiDa,
      ngay_bat_dau: voucher.ngayBatDau,
      ngay_bat_dau_fmt: formatDateVN(voucher.ngayBatDau),
      ngay_het_han: voucher.ngayHetHan,
      ngay_het_han_fmt: formatDateVN(voucher.ngayHetHan),
      trang_thai: voucher.trangThai,
      so_luong: voucher.soLuong
    };
  });

  console.log('Filtered vouchers:', availableVouchers.value);
};

// Watch for subtotal changes to intelligently update available and applied vouchers
watch(subtotal, async (newSubtotal) => {
  // Chỉ thực hiện khi đã đăng nhập và có sản phẩm trong giỏ
  if (!isLoggedIn.value) {
    availableVouchers.value = [];
    return;
  }

  try {
    const voucherResponse = newSubtotal > 0 ? await store.getVoucherByGiaTruyen(newSubtotal) : [];

    let newAvailableVouchers = [];
    if (voucherResponse && Array.isArray(voucherResponse)) {
      // Sắp xếp voucher theo mức giảm giá tốt nhất và map dữ liệu
      newAvailableVouchers = voucherResponse.sort((a, b) => {
        const aValue = Number(a.so_tien_giam) || 0;
        const bValue = Number(b.so_tien_giam) || 0;
        return bValue - aValue;
      }).map(voucher => ({
        id: voucher.id_voucher,
        ma: voucher.ma_voucher,
        ten: voucher.ten_voucher,
        mo_ta: voucher.mo_ta,
        loai: voucher.kieu_giam_gia === 'Phần trăm' ? 'percent' : 'fixed',
        gia_tri: voucher.gia_tri_giam,
        dieu_kien: voucher.gia_tri_toi_thieu,
        gia_tri_toi_da: voucher.gia_tri_toi_da,
        ngay_bat_dau: voucher.ngay_tao,
        ngay_het_han: voucher.ngay_het_han,
        trang_thai: voucher.trang_thai,
        so_luong: voucher.so_luong,
        so_tien_giam: Number(voucher.so_tien_giam) || 0,
      }));
    }

    availableVouchers.value = newAvailableVouchers;

    const currentVoucher = appliedCoupons.value.length > 0 ? appliedCoupons.value[0] : null;
    const bestVoucher = newAvailableVouchers.length > 0 ? newAvailableVouchers[0] : null;

    if (currentVoucher) {
      // Kiểm tra xem voucher đang áp dụng có còn trong danh sách hợp lệ không
      const isStillValid = newAvailableVouchers.some(v => v.id === currentVoucher.id);

      if (!isStillValid) {
        appliedCoupons.value = [];
        message.warning('Voucher đang áp dụng không còn hợp lệ và đã được gỡ bỏ.');

        // Nếu có voucher tốt nhất mới, tự động áp dụng
        if (bestVoucher) {
          appliedCoupons.value.push(bestVoucher);
          message.success(`Đã tự động áp dụng voucher tốt nhất: ${bestVoucher.ma}`);
        }
      }
    } else {
      // Nếu chưa có voucher nào, tự động áp dụng voucher tốt nhất (nếu có)
      if (bestVoucher) {
        appliedCoupons.value.push(bestVoucher);
        message.success(`Đã tự động áp dụng voucher: ${bestVoucher.ma}`);
      }
    }

  } catch (error) {
    console.error('Lỗi khi cập nhật danh sách voucher:', error);
    availableVouchers.value = [];
    appliedCoupons.value = [];
  }
}, { immediate: false }); // immediate: false để tránh chạy lần đầu khi component mount (đã có onMounted xử lý)

// Tính toán danh sách voucher hiển thị - giữ nguyên thứ tự từ API
const displayVouchers = computed(() => {
  return availableVouchers.value;
});

</script>

<style scoped>
/* General styles */
.checkout-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Roboto', sans-serif;
}

.breadcrumb-container {
  margin-bottom: 20px;
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
  color: #6c757d;
}

.breadcrumb-item a {
  color: #007bff;
  text-decoration: none;
}

.breadcrumb-item.active {
  color: #6c757d;
}

/* Order Timeline */
.order-timeline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  position: relative;
  padding: 0 20px;
}

.timeline-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 2;
}

.step-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  border: 2px solid #ddd;
  color: #6c757d;
  transition: all 0.3s ease;
}

.timeline-step.active .step-icon {
  background-color: #e6f7ff;
  border-color: #1890ff;
  color: #1890ff;
}

.timeline-step.completed .step-icon {
  background-color: #f6ffed;
  border-color: #52c41a;
  color: #52c41a;
}

.step-label {
  font-size: 14px;
  font-weight: 500;
  color: #6c757d;
  transition: all 0.3s ease;
}

.timeline-step.active .step-label {
  color: #1890ff;
  font-weight: 600;
}

.timeline-step.completed .step-label {
  color: #52c41a;
  font-weight: 600;
}

.timeline-connector {
  height: 2px;
  background-color: #ddd;
  flex: 1;
  margin: 0 10px;
  position: relative;
  top: -25px;
  z-index: 1;
  transition: all 0.3s ease;
}

.timeline-connector.active {
  background-color: #1890ff;
}

/* Main content layout */
.checkout-main-content {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
}

.checkout-info-section {
  flex: 2;
  min-width: 300px;
}

.checkout-summary-section {
  flex: 1;
  min-width: 300px;
}

.section-box {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  padding: 20px;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -1px;
  height: 2px;
  width: 50px;
  background-color: #1890ff;
}

/* Customer info */
.customer-info {
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 6px;
}

.info-group {
  margin-bottom: 15px;
}

.info-group p {
  margin: 8px 0;
  font-size: 14px;
}

.info-label {
  font-weight: 600;
  color: #555;
  margin-right: 10px;
  min-width: 100px;
  display: inline-block;
}

/* Address section */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  width: 100%;
}

.address-item {
  position: relative;
  transition: all 0.3s ease;
}

.address-radio {
  display: block;
  width: 100%;
}

.address-content {
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 6px;
  margin-left: 10px;
  transition: all 0.3s ease;
}

.address-radio:hover .address-content {
  background-color: #f0f7ff;
}

.address-radio:has(:checked) .address-content {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
}

.address-name {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 5px;
}

.address-phone {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.address-full {
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 10px;
}

.address-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.action-link {
  color: #1890ff;
  font-size: 14px;
  cursor: pointer;
}

.action-link:hover {
  text-decoration: underline;
}

.action-link.delete {
  color: #ff4d4f;
}

.action-link.delete:hover {
  color: #ff7875;
}

.add-address-button {
  margin-top: 15px;
}

/* Payment methods */
.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 15px;
  width: 100%;
}

.payment-method-item {
  position: relative;
  transition: all 0.3s ease;
}

.payment-radio {
  display: block;
  width: 100%;
}

.payment-content {
  display: flex;
  align-items: center;
  gap: 15px;
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 6px;
  margin-left: 10px;
  transition: all 0.3s ease;
}

.payment-radio:hover .payment-content {
  background-color: #f0f7ff;
}

.payment-radio:has(:checked) .payment-content {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
}

.payment-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1890ff;
}

.payment-info {
  flex: 1;
}

.payment-name {
  font-weight: 600;
  margin-bottom: 5px;
}

.payment-desc {
  font-size: 13px;
  color: #666;
}

.payment-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
}

.payment-logo img {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

/* Online payment methods */
.online-payment-options {
  margin-top: 15px;
  padding: 15px;
  background-color: #fafafa;
  border-radius: 6px;
  border: 1px dashed #ddd;
}

.online-methods {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.online-method-item {
  flex: 1;
  min-width: 150px;
}

.online-radio {
  display: block;
  width: 100%;
}

.online-content {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 6px;
  background-color: #fff;
  border: 1px solid #eee;
  transition: all 0.3s ease;
}

.online-radio:hover .online-content {
  background-color: #f0f7ff;
}

.online-radio:has(:checked) .online-content {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
}

.online-logo {
  width: 30px;
  height: 30px;
  object-fit: contain;
}

/* Order summary section */
.order-products {
  max-height: 350px;
  overflow-y: auto;
  margin-bottom: 20px;
  padding-right: 5px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.product-image {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-quantity {
  position: absolute;
  top: -5px;
  right: -5px;
  background-color: #1890ff;
  color: #fff;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.product-details {
  flex: 1;
}

.product-name {
  font-weight: 600;
  margin-bottom: 5px;
  font-size: 14px;
}

.product-variant {
  font-size: 13px;
  color: #666;
}

.product-price {
  font-weight: 600;
  color: #ff4d4f;
  font-size: 15px;
  white-space: nowrap;
}

/* Coupon section */
.coupon-section {
  margin-bottom: 20px;
}

/* Thông báo đăng nhập để sử dụng voucher */
.coupon-login-notice {
  margin-bottom: 20px;
  padding: 10px 15px;
  background-color: #f6ffed;
  border: 1px dashed #b7eb8f;
  border-radius: 6px;
  text-align: center;
}

.notice-text {
  color: #52c41a;
  font-size: 14px;
  margin: 0;
}

.applied-coupons {
  margin-top: 15px;
}

.applied-coupon {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
  background: linear-gradient(45deg, #f6ffed, #e6f7ff);
  border: 1px solid #b7eb8f;
  border-radius: 6px;
  margin-bottom: 10px;
  overflow: hidden;
  position: relative;
}

.coupon-info {
  display: flex;
  flex: 1;
}

.coupon-badge {
  width: 60px;
  background-color: #52c41a;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 600;
  padding: 12px 5px;
  text-align: center;
  text-transform: uppercase;
  position: relative;
}

.coupon-details {
  padding: 10px 15px;
  flex: 1;
}

.coupon-value {
  font-weight: 700;
  font-size: 16px;
  color: #52c41a;
  margin-bottom: 3px;
}

.coupon-code {
  font-weight: 600;
  margin-bottom: 3px;
  color: #555;
}

.coupon-desc {
  font-size: 12px;
  color: #666;
  margin: 0;
}

.remove-coupon-btn {
  margin-right: 5px;
  margin-top: -5px;
}

/* Voucher modal styling */
.voucher-modal {
  :deep(.ant-modal-content) {
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
    overflow: hidden;
  }

  :deep(.ant-modal-header) {
    background: linear-gradient(135deg, #1890ff, #096dd9);
    border-bottom: none;
    padding: 16px 24px;
  }

  :deep(.ant-modal-title) {
    color: white;
    font-weight: 600;
    font-size: 18px;
  }

  :deep(.ant-modal-body) {
    padding: 24px;
  }

  :deep(.ant-modal-close) {
    color: white;
  }

  :deep(.ant-modal-close:hover) {
    color: rgba(255, 255, 255, 0.85);
  }
}

.voucher-header {
  text-align: center;
  margin-bottom: 20px;
}

.voucher-header-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.voucher-header-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 0;
}

.voucher-list {
  max-height: 400px;
  overflow-y: auto;
  padding: 0 5px;
}

.voucher-item {
  display: flex;
  margin-bottom: 15px;
  border-radius: 10px;
  overflow: hidden;
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  cursor: pointer;
  position: relative;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.voucher-item:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-3px) scale(1.01);
}

/* Voucher không được chọn (khi có voucher khác được chọn) */
.voucher-item.not-selected {
  opacity: 0.7;
  filter: grayscale(0.4);
  transform: scale(0.98);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.voucher-item.not-selected:hover {
  opacity: 0.9;
  filter: grayscale(0.2);
  transform: translateY(-2px) scale(0.99);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
}

/* ========================================
   ✅ SUCCESS MODAL STYLES
   ======================================== */
.success-modal :deep(.ant-modal-content) {
  border-radius: 16px;
  overflow: hidden;
}

.success-modal-content {
  padding: 40px 24px 24px;
  text-align: center;
}

.success-icon-wrapper {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: successBounce 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.success-icon {
  font-size: 48px;
  color: white;
  animation: iconScale 0.6s ease-out 0.3s both;
}

.success-title {
  font-size: 24px;
  font-weight: 700;
  color: #262626;
  margin: 0 0 16px 0;
  animation: fadeInUp 0.5s ease-out 0.4s both;
}

.success-message {
  font-size: 15px;
  line-height: 1.6;
  color: #595959;
  margin: 0 0 32px 0;
  animation: fadeInUp 0.5s ease-out 0.5s both;
}

.success-message strong {
  color: #1890ff;
  font-weight: 600;
}

.success-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
  transition: all 0.3s ease;
  animation: fadeInUp 0.5s ease-out 0.6s both;
}

.success-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 77, 79, 0.4);
}

/* Animations */
@keyframes successBounce {
  0% {
    transform: scale(0);
    opacity: 0;
  }

  50% {
    transform: scale(1.1);
  }

  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes iconScale {
  0% {
    transform: scale(0);
  }

  100% {
    transform: scale(1);
  }
}

@keyframes fadeInUp {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }

  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.voucher-item.disabled {
  opacity: 0.6;
  filter: grayscale(0.8);
  cursor: not-allowed;
}

.voucher-item.selected {
  border: 2px solid #52c41a;
  background: linear-gradient(to right, rgba(246, 255, 237, 0.8), rgba(255, 255, 255, 0.9));
  animation: selected-pulse 2s infinite alternate;
  opacity: 1 !important;
  filter: none !important;
  transform: scale(1.02) !important;
  z-index: 2;
}

@keyframes selected-pulse {
  0% {
    box-shadow: 0 0 10px rgba(82, 196, 26, 0.3);
  }

  100% {
    box-shadow: 0 0 20px rgba(82, 196, 26, 0.8);
  }
}

.voucher-left {
  width: 110px;
  position: relative;
  background: linear-gradient(135deg, #FF9800, #F57C00);
  display: flex;
  align-items: center;
  justify-content: center;
  border-top-left-radius: 10px;
  border-bottom-left-radius: 10px;
  border-right: 2px dashed rgba(255, 255, 255, 0.8);
  overflow: hidden;
}

.voucher-left::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(45deg, rgba(255, 255, 255, 0.1) 25%, transparent 25%, transparent 50%, rgba(255, 255, 255, 0.1) 50%, rgba(255, 255, 255, 0.1) 75%, transparent 75%, transparent);
  background-size: 8px 8px;
  animation: moveBackground 20s linear infinite;
}

@keyframes moveBackground {
  0% {
    background-position: 0 0;
  }

  100% {
    background-position: 50px 50px;
  }
}

.voucher-left::after {
  content: "";
  position: absolute;
  right: -10px;
  height: 100%;
  width: 20px;
  background-image: radial-gradient(circle at 0 50%, transparent 10px, rgba(255, 255, 255, 0.1) 10px);
  background-size: 20px 20px;
  background-repeat: repeat-y;
}

.voucher-badge {
  color: white;
  font-size: 16px;
  font-weight: 700;
  text-align: center;
  text-transform: uppercase;
  padding: 10px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
  width: 100%;
  position: relative;
  z-index: 2;
}

.voucher-content {
  padding: 15px 20px;
  flex: 1;
  position: relative;
  background: linear-gradient(to right, rgba(255, 248, 225, 0.2), transparent);
}

.voucher-value {
  font-weight: 700;
  font-size: 24px;
  color: #FF9800;
  margin-bottom: 5px;
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: baseline;
}

.voucher-currency {
  font-size: 18px;
  font-weight: normal;
  margin-left: 2px;
  color: #FF9800;
}

.voucher-code {
  font-weight: 600;
  margin-bottom: 5px;
  color: #333;
  font-size: 15px;
}

.voucher-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.voucher-condition {
  font-size: 13px;
  color: #ff4d4f;
  margin-bottom: 5px;
  font-weight: 500;
}

.voucher-expiry {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.voucher-right {
  width: 100px;
  padding: 15px 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(250, 250, 250, 0.5);
}

.apply-btn {
  background: linear-gradient(135deg, #52c41a, #389e0d);
  border-color: #389e0d;
  color: white;
  font-weight: 600;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(56, 158, 13, 0.3);
}

.apply-btn:hover {
  background: linear-gradient(135deg, #73d13d, #52c41a);
  border-color: #52c41a;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(56, 158, 13, 0.4);
}

.remove-btn {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #666;
}

.remove-btn:hover {
  color: #ff4d4f;
  border-color: #ff4d4f;
  background: rgba(255, 77, 79, 0.05);
}

/* Hiệu ứng đặc biệt cho voucher tiết kiệm nhất (phần tử đầu tiên) */
.voucher-item:first-child {
  box-shadow: 0 5px 15px rgba(255, 152, 0, 0.3);
  border: 2px solid #ff9800;
  z-index: 1;
  opacity: 1 !important;
  filter: none !important;
}

.best-saving-tag {
  position: absolute;
  top: 15px;
  right: -35px;
  background: linear-gradient(135deg, #ff5722, #ff9800);
  color: white;
  padding: 5px 35px;
  font-size: 10px;
  font-weight: bold;
  transform: rotate(45deg);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  z-index: 3;
  width: 140px;
  text-align: center;
}

.voucher-item:first-child .voucher-left {
  background: linear-gradient(135deg, #FF9800, #FF5722);
  animation: colorShift 3s infinite alternate;
  border-right: 2px dashed white;
}

@keyframes colorShift {
  0% {
    background: linear-gradient(135deg, #FF9800, #FF5722);
  }

  50% {
    background: linear-gradient(135deg, #FF5722, #F44336);
  }

  100% {
    background: linear-gradient(135deg, #F44336, #FF9800);
  }
}

.voucher-item:first-child .voucher-badge {
  font-size: 17px;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.3);
}

.voucher-item:first-child .voucher-content {
  background: linear-gradient(to right, rgba(255, 243, 224, 0.5), rgba(255, 236, 179, 0.2));
}

.voucher-item:first-child .voucher-value {
  font-size: 26px;
  color: #FF5722;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
}

.voucher-item:first-child .voucher-currency {
  color: #FF5722;
  font-size: 20px;
}

.voucher-item:first-child:hover {
  transform: translateY(-5px) scale(1.03);
  box-shadow: 0 8px 25px rgba(255, 152, 0, 0.4);
}

.applied-voucher-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(to right, #f6ffed, #e6f7ff);
  border: 1px solid #b7eb8f;
  border-radius: 8px;
  padding: 12px 18px;
  margin-top: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  animation: glow 2s infinite alternate;
}

@keyframes glow {
  0% {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  }

  100% {
    box-shadow: 0 4px 15px rgba(82, 196, 26, 0.15);
  }
}

.banner-content {
  display: flex;
  align-items: center;
}

.banner-icon {
  color: #52c41a;
  font-size: 20px;
  margin-right: 10px;
}

.remove-btn-small {
  color: #ff4d4f;
  padding: 0 8px;
  font-size: 16px;
  height: 28px;
  width: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.remove-btn-small:hover {
  background-color: rgba(255, 77, 79, 0.15);
  color: #ff7875;
  transform: rotate(90deg);
}

/* Order summary */
.order-summary {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  font-size: 14px;
}

.item-label {
  color: #666;
}

.item-value {
  font-weight: 600;
  color: #333;
}

.item-value.discount {
  color: #52c41a;
}

.summary-item.total {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
  font-size: 16px;
}

.summary-item.total .item-label {
  color: #333;
  font-weight: 600;
}

.summary-item.total .item-value {
  color: #ff4d4f;
  font-size: 18px;
}

.order-actions {
  margin-top: 20px;
}

/* Address modal */
:deep(.ant-form-item) {
  margin-bottom: 15px;
}

:deep(.ant-modal-footer) {
  border-top: 1px solid #f0f0f0;
  padding: 10px 16px;
}

/* Responsive styles */
@media (max-width: 992px) {
  .checkout-main-content {
    flex-direction: column;
  }

  .checkout-info-section,
  .checkout-summary-section {
    width: 100%;
  }

  .online-methods {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .order-timeline {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
    padding-left: 30px;
  }

  .timeline-step {
    flex-direction: row;
    align-items: center;
    width: 100%;
  }

  .step-icon {
    margin-bottom: 0;
    margin-right: 15px;
  }

  .timeline-connector {
    height: 100%;
    width: 2px;
    position: absolute;
    left: 30px;
    top: 0;
    bottom: 0;
    margin: 0;
  }
}

@media (max-width: 576px) {
  .section-box {
    padding: 15px;
  }

  .product-item {
    flex-wrap: wrap;
  }

  .product-price {
    width: 100%;
    margin-top: 10px;
    text-align: right;
  }

  .step-label {
    font-size: 12px;
  }
}

/* Animation effects */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-box {
  animation: fadeIn 0.3s ease-in-out;
}

/* Select voucher button */
.select-voucher-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* CSS cho modal */
.address-modal,
.voucher-modal {

  /* Đảm bảo modal hiển thị lên trên */
  :deep(.ant-modal) {
    top: 50px;
  }

  :deep(.ant-modal-content) {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  }

  :deep(.ant-modal-header) {
    border-bottom: 1px solid #f0f0f0;
    padding: 16px 24px;
  }

  :deep(.ant-modal-title) {
    font-weight: 600;
    font-size: 16px;
  }
}

/* Styles cho địa chỉ đã chọn */
.subsection-title {
  font-size: 16px;
  font-weight: 500;
  margin: 20px 0 10px;
  color: #333;
}

.selected-address-info {
  border-top: 1px dashed #ddd;
  margin-top: 15px;
  padding-top: 15px;
}

.address-display {
  background-color: #f0f7ff;
  padding: 15px;
  border-radius: 6px;
  border-left: 3px solid #1890ff;
}

.address-display-name {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 5px;
}

.address-display-phone {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.address-display-full {
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 0;
}

/* Form styling */
.shipping-form {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}

.form-item {
  flex: 1;
  margin-bottom: 0;
}

.form-item.full-width {
  flex: 0 0 100%;
}

.form-item.checkbox-item {
  margin-top: 8px;
}

:deep(.ant-form-item-label) {
  padding-bottom: 4px;
}

:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

:deep(.ant-input),
:deep(.ant-select-selector) {
  border-radius: 6px;
  border-color: #d9d9d9;
  height: 40px;
  padding: 4px 11px;
  transition: all 0.3s;
}

:deep(.ant-input:hover),
:deep(.ant-select-selector:hover) {
  border-color: #40a9ff;
}

:deep(.ant-input:focus),
:deep(.ant-select-selector:focus) {
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

:deep(.ant-select-disabled .ant-select-selector) {
  background-color: #f5f5f5;
  border-color: #d9d9d9;
  color: rgba(0, 0, 0, 0.25);
}

:deep(.ant-checkbox-wrapper) {
  font-size: 14px;
  color: #666;
}

:deep(.ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #1890ff;
  border-color: #1890ff;
}

:deep(.ant-checkbox-wrapper:hover .ant-checkbox-inner),
:deep(.ant-checkbox:hover .ant-checkbox-inner) {
  border-color: #40a9ff;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
    gap: 16px;
  }

  .form-item {
    width: 100%;
  }

  .shipping-form {
    padding: 15px;
  }
}

.default-tag {
  font-size: 12px;
  color: #52c41a;
  font-weight: normal;
  margin-left: 8px;
}

.empty-address-message {
  color: #999;
  font-style: italic;
  margin-bottom: 15px;
}

.add-address-btn {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.section-header .section-title {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.section-header .section-title::after {
  display: none;
}

.voucher-header {
  text-align: center;
  margin-bottom: 20px;
}

.voucher-header-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.voucher-header-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 0;
}

.voucher-list {
  max-height: 400px;
  overflow-y: auto;
  padding: 0 5px;
}

.actual-discount {
  font-size: 14px;
  color: #ff4d4f;
  font-weight: normal;
  margin-left: 5px;
}

.voucher-item:first-child .actual-discount {
  color: #ff4500;
  font-weight: bold;
}

.apply-btn {
  background: linear-gradient(135deg, #52c41a, #389e0d);
  border-color: #389e0d;
  color: white;
  font-weight: 600;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(56, 158, 13, 0.3);
}

.apply-btn:hover {
  background: linear-gradient(135deg, #73d13d, #52c41a);
  border-color: #52c41a;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(56, 158, 13, 0.4);
}

.remove-btn {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #666;
}

.remove-btn:hover {
  color: #ff4d4f;
  border-color: #ff4d4f;
  background: rgba(255, 77, 79, 0.05);
}

.voucher-empty {
  text-align: center;
  padding: 40px 0;
}

.empty-img {
  width: 120px;
  margin-bottom: 20px;
  opacity: 0.8;
}

.voucher-actions {
  display: flex;
  justify-content: center;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.applied-voucher-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(to right, #f6ffed, #e6f7ff);
  border: 1px solid #b7eb8f;
  border-radius: 6px;
  padding: 10px 15px;
  margin-top: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.banner-content {
  display: flex;
  align-items: center;
}

.banner-icon {
  color: #52c41a;
  font-size: 18px;
  margin-right: 10px;
}

.remove-btn-small {
  color: #ff4d4f;
  padding: 0 8px;
  font-size: 16px;
  height: 24px;
  width: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-btn-small:hover {
  background-color: rgba(255, 77, 79, 0.1);
  color: #ff7875;
}

.voucher-left {
  width: 100px;
  position: relative;
  background: linear-gradient(135deg, #FF9800, #F57C00);
  display: flex;
  align-items: center;
  justify-content: center;
  border-top-left-radius: 8px;
  border-bottom-left-radius: 8px;
  border-right: 2px dashed rgba(255, 255, 255, 0.5);
}

.voucher-left::before {
  content: "";
  position: absolute;
  right: -8px;
  height: 100%;
  width: 16px;
  background-image: radial-gradient(circle at 0 50%, transparent 8px, rgba(255, 255, 255, 0.1) 8px);
  background-size: 16px 16px;
  background-repeat: repeat-y;
}

.voucher-badge {
  color: white;
  font-size: 14px;
  font-weight: 600;
  text-align: center;
  text-transform: uppercase;
  padding: 5px;
  width: 100%;
}

.voucher-item {
  display: flex;
  margin-bottom: 15px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  border: 1px solid #f0f0f0;
}

.voucher-item:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  transform: translateY(-3px);
}

.voucher-value {
  font-weight: 700;
  font-size: 20px;
  color: #FF9800;
  margin-bottom: 5px;
}

.voucher-currency {
  font-size: 16px;
  font-weight: normal;
}

/* Hiệu ứng đặc biệt cho voucher tốt nhất */
.voucher-item:first-child {
  border: 1px solid #FF9800;
  box-shadow: 0 5px 15px rgba(255, 153, 0, 0.2);
}

.voucher-item:first-child::after {
  content: "TIẾT KIỆM NHẤT";
  position: absolute;
  top: 10px;
  right: -35px;
  background: linear-gradient(135deg, #ff5722, #ff9800);
  color: white;
  padding: 5px 35px;
  font-size: 10px;
  font-weight: bold;
  transform: rotate(45deg);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  z-index: 1;
}

.applied-voucher-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 6px;
  padding: 10px 15px;
  margin-top: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.best-saving-tag {
  position: absolute;
  top: -10px;
  right: -10px;
  background-color: #ff4d4f;
  color: white;
  font-size: 12px;
  padding: 5px 10px;
  border-radius: 50%;
}

/* CSS cho phần địa chỉ */
.address-info {
  margin-bottom: 30px;
}

.address-info .section-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 25px;
  padding-bottom: 12px;
  position: relative;
  color: #333;
}

.address-info .section-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  height: 3px;
  width: 60px;
  background: #1890ff;
  border-radius: 3px;
}

.address-list {
  position: relative;
  padding-left: 15px;
}

.address-list::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 2px;
  background-color: #e8e8e8;
  height: 100%;
}

.address-item {
  position: relative;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.address-item::before {
  content: '';
  position: absolute;
  left: -21px;
  top: 15px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #1890ff;
  border: 2px solid #fff;
  box-shadow: 0 0 0 2px #1890ff;
  z-index: 2;
}

.address-radio {
  width: 100%;
  margin: 0;
}

.address-content {
  border-radius: 10px;
  overflow: hidden;
  padding: 20px;
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.address-radio:has(:checked) .address-content {
  background-color: #f0f9ff;
  border-color: #69c0ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.address-name {
  font-size: 16px;
  font-weight: 600;
  color: #0050b3;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.default-tag {
  margin-left: 8px;
  font-size: 12px;
  background-color: #52c41a;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: normal;
}

.address-phone {
  font-size: 14px;
  color: #555;
  margin-bottom: 5px;
}

.address-email {
  font-size: 14px;
  color: #555;
  margin-bottom: 8px;
}

.address-full {
  font-size: 14px;
  line-height: 1.5;
  padding-top: 8px;
  margin-bottom: 0;
  border-top: 1px dashed #d9d9d9;
  color: #666;
}

.empty-address-message {
  padding: 15px;
  text-align: center;
  color: #999;
  font-style: italic;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px dashed #d9d9d9;
}

/* Thêm CSS cho các ô input của form địa chỉ */
.customer-form {
  width: 100%;
}

.customer-form .ant-input,
.customer-form .ant-select,
.customer-form .ant-select-selector,
.customer-form .ant-textarea {
  height: 40px !important;
  border-radius: 8px;
  font-size: 14px;
  width: 100% !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.customer-form .ant-textarea {
  height: auto !important;
  min-height: 80px;
  resize: vertical;
}

.customer-form .ant-select-selector {
  padding: 0 11px;
  display: flex;
  align-items: center;
  height: 40px !important;
}

.customer-form .ant-select-selection-search {
  display: flex;
  align-items: center;
}

.customer-form .ant-form-item {
  margin-bottom: 20px;
  width: 100%;
}

.customer-form .ant-form-item-label {
  padding-bottom: 5px;
}

.customer-form .ant-form-item-label>label {
  font-weight: 500;
  color: #333;
}

.customer-form .ant-form-item-label>label.ant-form-item-required:not(.ant-form-item-required-mark-optional)::before {
  color: #f33b47;
}

.form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 10px;
  width: 100%;
}

.form-row .form-item {
  flex: 1;
  min-width: calc(50% - 10px);
  max-width: calc(50% - 10px);
  width: calc(50% - 10px);
}

.form-row .form-item.full-width {
  min-width: 100%;
  max-width: 100%;
  width: 100%;
}

.shipping-form {
  display: flex;
  flex-direction: column;
  width: 100%;
}

/* Sửa lỗi hiển thị không đồng nhất của select */
.ant-select {
  width: 100% !important;
}

.ant-select-single:not(.ant-select-customize-input) .ant-select-selector {
  width: 100% !important;
  height: 40px !important;
}

.ant-select-single .ant-select-selector .ant-select-selection-item,
.ant-select-single .ant-select-selector .ant-select-selection-placeholder {
  line-height: 40px !important;
}

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
  }

  .form-row .form-item {
    width: 100%;
    min-width: 100%;
    max-width: 100%;
  }
}

/* ✅ Phase 6: ZaloPay Modal Styles */
.zalopay-modal {
  padding: 24px 12px;
  text-align: center;

  .qr-section {
    .qr-header {
      margin-bottom: 20px;

      h3 {
        margin: 0 0 8px;
        color: #008fe5;
        font-size: 18px;
        font-weight: 600;
      }

      p {
        margin: 0;
        color: #666;
        font-size: 14px;
        line-height: 1.5;
      }
    }

    .qr-container {
      margin: 24px 0;
      padding: 24px;
      background: linear-gradient(135deg, #f5f7fa 0%, #e3e7f0 100%);
      border-radius: 16px;
      border: 2px dashed #d0d5dd;
      display: inline-block;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

      .qr-code {
        width: 250px;
        height: 250px;
        border: 3px solid #fff;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
        background: #fff;
      }
    }

    .payment-info {
      margin: 24px 0;
      padding: 16px 20px;
      background: #f8f9fa;
      border-radius: 10px;
      border-left: 4px solid #008fe5;

      .info-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin: 8px 0;
        font-size: 15px;

        .label {
          color: #666;
          font-weight: 500;
        }

        .value {
          color: #008fe5;
          font-weight: 600;
          font-size: 16px;
        }
      }
    }

    .payment-status {
      margin-top: 24px;
      padding: 16px;
      background: #fff9e6;
      border-radius: 8px;
      border: 1px solid #ffc107;

      p {
        margin: 12px 0 4px;
        font-size: 15px;
        font-weight: 600;
        color: #008fe5;
      }

      .status-note {
        font-size: 13px;
        font-weight: 400;
        color: #999;
        margin-top: 4px;
      }
    }
  }

  .modal-actions {
    margin-top: 24px;

    button {
      height: 44px;
      font-size: 15px;
      font-weight: 500;
    }
  }
}

/* Responsive design for mobile */
@media (max-width: 576px) {
  .zalopay-modal {
    padding: 16px 8px;

    .qr-section {
      .qr-container {
        padding: 16px;

        .qr-code {
          width: 200px;
          height: 200px;
        }
      }
    }
  }
}
</style>
