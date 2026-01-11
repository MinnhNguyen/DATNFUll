<template>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css">
  <div class="container-fluid main-content">
    <div v-if="loading" class="text-center">
      <a-spin tip="Đang tải dữ liệu..." />
    </div>
    <div v-else-if="store.hoaDonDetail">
      <div class="order-header">
        <a-row :gutter="16">
          <a-col :md="17">
            <h2 style="color: #ff6600;">Thông tin chi tiết đơn hàng {{ store.hoaDonDetail.loai_hoa_don }}
              #{{
                store.hoaDonDetail.ma_hoa_don || 'N/A' }}</h2>
          </a-col>
          <a-col :md="7" class="text-right">
            <a-button type="primary" @click="$router.push('/admin/quanlyhoadon')">Quay lại</a-button>
          </a-col>
        </a-row>
        <a-divider />
        <a-row>
          <a-col :md="22" class="text-center">
            <h4 style="margin-left: 90px;">Lịch sử trạng thái</h4>
          </a-col>
          <a-col :md="2" class="text-right"><a-button type="link" @click="openStatusHistoryDrawer">Xem chi
              tiết</a-button></a-col>
        </a-row>
        <br>
        <div class="order-status-timeline">
          <div class="timeline-track"></div>

          <!-- Main status steps (always show all 5 possible statuses) -->
          <div class="timeline-steps">
            <!-- Render các trạng thái động từ lịch sử trạng thái -->
            <div v-for="(status, index) in filteredTrangThaiHistory" :key="index" class="timeline-step" :class="{
              'active': store.hoaDonDetail.trang_thai === status.trang_thai,
              'completed': isStatusCompleted(status.trang_thai),
              'cancelled': status.trang_thai === 'Đã hủy'
            }">
              <div class="timeline-icon">
                <i :class="getIconForStatus(status.trang_thai)"></i>
              </div>
              <div class="timeline-content">
                <h4>{{ status.trang_thai }}</h4>
                <p v-if="status.ngay_chuyen">{{ formatDate(status.ngay_chuyen) }}</p>
              </div>
            </div>
            <!-- Chờ xác nhận -->
            <!-- <div class="timeline-step" :class="{
                            'active': store.hoaDonDetail.trang_thai === 'Chờ xác nhận',
                            'completed': isStatusCompleted('Chờ xác nhận')
                        }">
                            <div class="timeline-icon">
                                <i class="fas fa-hourglass-start"></i>
                            </div>
                            <div class="timeline-content">
                                <h4>Chờ xác nhận</h4>
                                <p v-if="getStatusDate('Chờ xác nhận')">{{ formatDate(getStatusDate('Chờ xác nhận')) }}
                                </p>
                            </div>
                        </div> -->

            <!-- Đã xác nhận -->
            <!-- <div class="timeline-step" :class="{
                            'active': store.hoaDonDetail.trang_thai === 'Đã xác nhận',
                            'completed': isStatusCompleted('Đã xác nhận')
                        }">
                            <div class="timeline-icon">
                                <i class="fas fa-check-circle"></i>
                            </div>
                            <div class="timeline-content">
                                <h4>Đã xác nhận</h4>
                                <p v-if="getStatusDate('Đã xác nhận')">{{ formatDate(getStatusDate('Đã xác nhận')) }}
                                </p>
                            </div>
                        </div> -->

            <!-- Chờ đóng gói -->
            <!-- <div class="timeline-step" :class="{
                            'active': store.hoaDonDetail.trang_thai === 'Chờ đóng gói',
                            'completed': isStatusCompleted('Chờ đóng gói')
                        }">
                            <div class="timeline-icon">
                                <i class="fas fa-box"></i>
                            </div>
                            <div class="timeline-content">
                                <h4>Chờ đóng gói</h4>
                                <p v-if="getStatusDate('Chờ đóng gói')">{{ formatDate(getStatusDate('Chờ đóng gói')) }}
                                </p>
                            </div>
                        </div> -->

            <!-- Đang giao -->
            <!-- <div class="timeline-step" :class="{
                            'active': store.hoaDonDetail.trang_thai === 'Đang giao',
                            'completed': isStatusCompleted('Đang giao')
                        }">
                            <div class="timeline-icon">
                                <i class="fas fa-truck"></i>
                            </div>
                            <div class="timeline-content">
                                <h4>Đang giao</h4>
                                <p v-if="getStatusDate('Đang giao')">{{ formatDate(getStatusDate('Đang giao')) }}</p>
                            </div>
                        </div> -->

            <!-- Hoàn thành -->
            <!-- <div class="timeline-step" :class="{
                            'active': store.hoaDonDetail.trang_thai === 'Hoàn thành',
                            'completed': isStatusCompleted('Hoàn thành')
                        }">
                            <div class="timeline-icon">
                                <i class="fas fa-flag-checkered"></i>
                            </div>
                            <div class="timeline-content">
                                <h4>Hoàn thành</h4>
                                <p v-if="getStatusDate('Hoàn thành')">{{ formatDate(getStatusDate('Hoàn thành')) }}</p>
                            </div>
                        </div> -->

            <!-- Đã hủy - chỉ hiển thị khi đơn hàng bị hủy -->
            <!-- <div v-if="store.hoaDonDetail.trang_thai === 'Đã hủy'" class="timeline-step cancelled active">
                            <div class="timeline-icon">
                                <i class="fas fa-times-circle"></i>
                            </div>
                            <div class="timeline-content">
                                <h4>Đã hủy</h4>
                                <p v-if="getStatusDate('Đã hủy')">{{ formatDate(getStatusDate('Đã hủy')) }}</p>
                            </div>
                        </div> -->
            <!-- Trả hàng -->
            <!-- <div class="timeline-step" :class="{
                            'active': store.hoaDonDetail.trang_thai === 'Trả hàng',
                        }">
                            <div class="timeline-icon">
                                <i class="fas fa-undo"></i>
                            </div>
                            <div class="timeline-content">
                                <h4>Trả hàng</h4>
                                <p v-if="getStatusDate('Trả hàng')">{{ formatDate(getStatusDate('Trả hàng')) }}</p>
                            </div>
                        </div> -->

          </div>


          <!-- Intermediate update points between main statuses -->
          <div class="update-markers">
            <div v-for="update in getUpdatedStatuses" :key="update.id" class="update-marker"
              :style="{ left: getUpdatePosition(update) + '%' }">
              <div class="update-icon">
                <i class="fas fa-sync-alt"></i>
              </div>
              <div class="update-tooltip">
                <h5>Đã cập nhật</h5>
                <p>{{ formatDateTime(update.ngay_chuyen) }}</p>
                <p v-if="update.nhan_vien_doi">Bởi: {{ update.nhan_vien_doi }}</p>
                <p v-if="update.noi_dung_doi">{{ update.noi_dung_doi }}</p>
              </div>
            </div>
          </div>
        </div>
        <a-divider />
        <!-- Drawer hiển thị chi tiết lịch sử trạng thái -->
        <a-drawer v-model:open="showStatusHistoryDrawer" title="Chi tiết lịch sử trạng thái" placement="right"
          :width="450">
          <a-timeline :pending="isPending ? 'Đang xử lý...' : null" :reverse="reverseTimeline">
            <template #pendingDot>
              <i v-if="isPending" class="fas fa-spinner fa-spin" />
            </template>
            <a-timeline-item v-for="(status, index) in sortedTrangThaiHistory" :key="index"
              :color="getTimelineColor(status.trang_thai)">
              <a-row>
                <a-col :md="15">
                  <h6 style="font-weight: bold;">{{ status.trang_thai }}</h6>
                </a-col>
                <a-col :md="9">{{ formatDateTime(status.ngay_chuyen) }}</a-col>
              </a-row>
              <p v-if="status.nhan_vien_doi">
                Nhân viên: {{ status.nhan_vien_doi }}
              </p>
              <p v-if="status.noi_dung_doi">
                Ghi chú: {{ status.noi_dung_doi }}
              </p>
            </a-timeline-item>
          </a-timeline>
          <a-button type="primary" style="margin-top: 16px" @click="toggleReverseTimeline">
            Đảo ngược thứ tự
          </a-button>
        </a-drawer>
        <div class="order-status">
          <!-- Nút Quay lại trạng thái ban đầu -->
          <!-- <a-form @submit="openStatusModal('revert')" v-if="showRevertButton" :inline="true">
                        <a-button type="warning" html-type="submit">
                            Quay lại trạng thái ban đầu
                        </a-button>
                    </a-form> -->
          <a-form @submit="openStatusModal('change')" :inline="true">
            <a-button type="primary" html-type="submit" :disabled="isCompletedOrCancelled"
              :class="{ 'disabled': isCompletedOrCancelled }">
              {{ nextStatusText }}
            </a-button>
          </a-form>
          <a-form @submit="openStatusModal('cancel')" :inline="true">
            <a-button type="text" html-type="submit" :disabled="cannotCancel" :class="{ 'disabled': cannotCancel }">
              Hủy đơn
            </a-button>
          </a-form>
          <a-form :inline="true">
            <a-button type="text" @click="openPrintConfirm">
              Xuất hóa đơn
            </a-button>
          </a-form>
          <a-modal v-model:open="showPrintConfirm" title="Xác nhận xuất hóa đơn" @ok="confirmPrint(true)"
            @cancel="confirmPrint(false)">
            <p>Bạn muốn xuất hóa đơn này phải không?</p>
            <template #footer>
              <a-button key="cancel" @click="confirmPrint(false)">Không</a-button>
              <a-button key="ok" type="primary" @click="confirmPrint(true)">Đúng</a-button>
            </template>
          </a-modal>
          <!-- Modal dùng chung cho thay đổi trạng thái -->
          <a-modal v-model:open="showStatusModal" :title="modalTitle" @ok="confirmStatusChange"
            @cancel="closeStatusModal">
            <a-form :model="statusForm" layout="vertical">
              <a-form-item label="Ghi chú" name="noiDungDoi">
                <a-input v-model:value="statusForm.noiDungDoi" placeholder="Chọn hoặc nhập nội dung" allow-clear
                  list="noiDungDoiOptions" @focus="handleFocus" @blur="handleBlur" />
                <datalist id="noiDungDoiOptions">
                  <option v-for="option in noiDungDoiOptions" :key="option" :value="option" />
                </datalist>
              </a-form-item>
            </a-form>
            <template #footer>
              <a-button key="cancel" @click="closeStatusModal">Hủy</a-button>
              <a-button key="ok" type="primary" @click="confirmStatusChange">Xác nhận</a-button>
            </template>
          </a-modal>
        </div>
      </div>

      <a-row :gutter="16">
        <a-col :md="24">
          <div class="info-box">
            <a-row>
              <a-col :span="12">
                <h5>Thông tin đơn hàng</h5>
              </a-col>
              <a-col :span="12" style="text-align: right;"><span :class="{
                'status-online': store.hoaDonDetail.loai_hoa_don === 'Online',
                'status-offline': store.hoaDonDetail.loai_hoa_don === 'Offline'
              }">{{ store.hoaDonDetail.loai_hoa_don }}
                </span>
              </a-col>
            </a-row>
            <hr>
            <a-row :gutter="16">
              <a-col :span="12">
                <p>Mã hóa đơn: {{ store.hoaDonDetail.ma_hoa_don || 'N/A' }}</p>
                <p>Trạng thái: {{ store.hoaDonDetail.trang_thai || 'N/A' }}</p>
                <p>Phương thức thanh toán: {{ store.hoaDonDetail.hinh_thuc_thanh_toan || 'Chưa xác định'
                }}</p>
              </a-col>
              <a-col :span="12">
                <p>Ngày tạo: {{ formatDateTime(store.hoaDonDetail.ngay_tao) }}</p>
                <!-- <p>Nhân viên tiếp nhận: {{ store.hoaDonDetail.ten_nhan_vien || 'Chưa xác định' }}</p> -->
                <p>Hình thức nhận hàng: {{ store.hoaDonDetail.phuong_thuc_nhan_hang || 'Chưa xác định'
                }}</p>
              </a-col>
            </a-row>
          </div>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :md="16">
          <div class="info-box">
            <a-row>
              <a-col :md="19">
                <h5>Thông tin sản phẩm</h5>
              </a-col>
              <a-col :md="5" class="text-right">
                <a-button type="primary" :disabled="cannotEditProduct" :class="{ 'disabled': cannotEditProduct }"
                  @click="showAddProductPopupFn">
                  Thêm sản phẩm
                </a-button>
              </a-col>
            </a-row>
            <hr>
            <a-table :columns="productColumns" :data-source="store.chiTietHoaDons" :pagination="false"
              row-key="id_chi_tiet_san_pham">
              <template #bodyCell="{ column, record, index }">
                <template v-if="column.key === 'id_chi_tiet_san_pham'">
                  <div style="text-align: center;">
                    {{ record.id_chi_tiet_san_pham || 'N/A' }}
                  </div>
                </template>
                <template v-if="column.key === 'san_pham'">
                  <div class="product-info">
                    <img :src="record.hinh_anh || '/images/default.jpg'" alt="Product" class="product-image">
                    <div class="product-details">

                      <div class="product-name">
                        {{ record.ten_san_pham || 'N/A' }}
                        <a-tag v-if="hasMultiplePrices(record)" color="orange" size="small">
                          <i class="fas fa-exclamation-triangle"></i> Đa giá
                        </a-tag>
                      </div>
                      <div class="product-specs">
                        Kích thước: {{ record.kich_thuoc || 'N/A' }},
                        Màu: {{ record.ten_mau_sac || 'N/A' }}
                      </div>
                    </div>
                  </div>
                </template>
                <template v-if="column.key === 'don_gia'">
                  <div>
                    <span v-if="record.gia_sau_km && record.gia_sau_km < record.gia_goc" style="color: red;">
                      {{ formatCurrency(record.gia_sau_km) }} VNĐ
                    </span>
                    <span v-else>
                      {{ formatCurrency(record.gia_goc) }} VNĐ
                    </span>
                    <div v-if="record.gia_sau_km && record.gia_sau_km < record.gia_goc" class="original-price">
                      {{ formatCurrency(record.gia_goc) }} VNĐ
                    </div>
                  </div>
                </template>
                <template v-if="column.key === 'so_luong'">
                  <div class="quantity-display">
                    <a-button @click="showDecreasePopup(index)" :disabled="cannotEditProduct">
                      <i class="fas fa-minus"></i>
                    </a-button>
                    <span>{{ record.so_luong }}</span>
                    <a-button @click="showIncreasePopup(index)" :disabled="cannotEditProduct">
                      <i class="fas fa-plus"></i>
                    </a-button>
                  </div>
                </template>
                <template v-if="column.key === 'thanh_tien'">
                  <span
                    :style="{ color: record.gia_sau_giam && record.gia_sau_giam < record.gia_ban ? 'red' : 'inherit' }">
                    {{ formatCurrency(record.don_gia) }} VNĐ
                  </span>
                </template>
                <template v-if="column.key === 'thao_tac'">
                  <a-button type="danger" size="small" @click="removeProduct(record, index)"
                    :disabled="cannotEditProduct">
                    <i class="fas fa-trash"></i>
                  </a-button>
                </template>
              </template>
            </a-table>
            <div class="total-section" v-if="store.hoaDonDetail.trang_thai !== 'Trả hàng'">
              <a-row>
                <a-col :md="16"></a-col>
                <a-col :md="4" style="text-align: left;">
                  <p>Tổng tiền hàng:</p>
                </a-col>
                <a-col :md="4" style="text-align: right;">
                  <h6>{{ formatCurrency(store.hoaDonDetail.tong_tien_truoc_giam) }} VNĐ</h6>
                </a-col>
              </a-row>
              <a-row>
                <a-col :md="7"></a-col>
                <a-col :md="9" style="text-align: left;color: red;">
                  <p>{{ store.hoaDonDetail.mo_ta }}</p>
                </a-col>
                <a-col :md="4" style="text-align: left;">
                  <p>Giảm giá:</p>
                </a-col>
                <a-col :md="4" style="text-align: right;color: red;">
                  <p>{{ (store.hoaDonDetail.giam_gia > 0 ? "-" : "") }} {{
                    formatCurrency(store.hoaDonDetail.giam_gia) }} VNĐ</p>
                </a-col>
              </a-row>
              <a-row v-if="store.hoaDonDetail?.phuong_thuc_nhan_hang === 'Giao hàng'">
                <a-col :md="16">
                  <img src="../../../images/logo/logo_GHTK.png" alt="GHTK Logo" class="ghtk-logo" />
                </a-col>
                <a-col :md="4" style="text-align: left;">
                  <p>Phí vận chuyển:</p>
                </a-col>
                <a-col :md="4" style="text-align: right;">
                  <p>+ {{ formatCurrency(store.hoaDonDetail.phi_van_chuyen) }} VNĐ</p>
                </a-col>
              </a-row>
              <a-row>
                <a-col :md="16"></a-col>
                <a-col :md="4" style="text-align: left;">
                  <p>Thành tiền:</p>
                </a-col>
                <a-col :md="4" style="text-align: right;">
                  <!-- ✅ UNIFIED: tong_tien_sau_giam chỉ có (Total - Voucher), cần cộng ship để hiển thị -->
                  <h6>{{ formatCurrency(store.hoaDonDetail.tong_tien_sau_giam +
                    store.hoaDonDetail.phi_van_chuyen) }} VNĐ</h6>
                </a-col>
              </a-row>
              <!-- Dòng text thanh toán thêm -->
              <a-row
                v-if="store.hoaDonDetail?.loai_hoa_don === 'Online' && store.hoaDonDetail?.hinh_thuc_thanh_toan === 'Chuyển khoản' && store.chiTietHoaDons[0]?.phu_thu > 0
                  || store.hoaDonDetail?.loai_hoa_don === 'Offline' && store.hoaDonDetail?.phuong_thuc_nhan_hang === 'Giao hàng' && store.chiTietHoaDons[0]?.phu_thu > 0">
                <a-col :md="16"></a-col>
                <a-col :md="4" style="text-align: left;">
                  <p style="color: #f5222d;">Đã thanh toán: </p>
                </a-col>
                <a-col :md="4" style="text-align: right;">
                  <p style="color: #f5222d;">{{ formatCurrency(store.hoaDonDetail.tong_tien_sau_giam -
                    store.chiTietHoaDons[0]?.phu_thu) }} VNĐ</p>
                </a-col>
              </a-row>
              <a-row
                v-if="store.hoaDonDetail?.loai_hoa_don === 'Online' && store.hoaDonDetail?.hinh_thuc_thanh_toan === 'Chuyển khoản' && store.chiTietHoaDons[0]?.phu_thu > 0
                  || store.hoaDonDetail?.loai_hoa_don === 'Offline' && store.hoaDonDetail?.phuong_thuc_nhan_hang === 'Giao hàng' && store.chiTietHoaDons[0]?.phu_thu > 0">
                <a-col :md="12"></a-col>
                <a-col :md="12" style="text-align: left;">
                  <p style="color: #f5222d;">Vui lòng thanh toán thêm {{
                    formatCurrency(store.chiTietHoaDons[0]?.phu_thu) }} VNĐ sau khi nhận hàng</p>
                </a-col>
                <!-- <a-col :md="4" style="text-align: right;">
                                    <p style="color: #f5222d;">+ {{ formatCurrency(store.chiTietHoaDons[0]?.phu_thu) }} VNĐ</p>
                                </a-col> -->
              </a-row>
            </div>
          </div>
          <div class="info-box" v-if="store.hoaDonDetail.trang_thai === 'Trả hàng'">
            <a-row>
              <a-col :md="19">
                <h5>Danh sách sản phẩm hoàn trả</h5>
              </a-col>
            </a-row>
            <hr>
            <a-table :columns="productColumnsHoan" :data-source="store.chiTietTraHangs" :pagination="false"
              row-key="id">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'san_pham'">
                  <img :src="record.hinh_anh || '/images/default.jpg'" alt="Product" class="product-image"
                    style="width: 50px; height: 50px; object-fit: cover; margin-right: 8px;" />
                  {{ record.ten_san_pham || 'N/A' }} <br />
                  Kích thước: {{ record.kich_thuoc || 'N/A' }}, Màu: {{ record.ten_mau_sac || 'N/A' }}
                </template>
                <template v-if="column.key === 'don_gia'">
                  {{ formatCurrency(record.don_gia) }} VNĐ
                </template>
                <template v-if="column.key === 'so_luong'">
                  <div class="quantity-display">
                    <span>{{ record.so_luong || 0 }}</span>
                  </div>
                </template>
                <template v-if="column.key === 'thanh_tien'">
                  {{ formatCurrency(record.tien_hoan) }} VNĐ
                </template>
                <template v-if="column.key === 'trang_thai'">
                  <span style="color: #ff6600;">Trả hàng</span>
                </template>
              </template>
            </a-table>

            <div class="total-section">
              <!-- Existing total section content -->
              <a-row>
                <a-col :md="16"></a-col>
                <a-col :md="4" style="text-align: left;">
                  <p>Tổng tiền hàng:</p>
                </a-col>
                <a-col :md="4" style="text-align: right;">
                  <h6>{{ formatCurrency(store.hoaDonDetail.tong_tien_truoc_giam) }} VNĐ</h6>
                </a-col>
              </a-row>
              <a-row>
                <a-col :md="7"></a-col>
                <a-col :md="9" style="text-align: left;color: red;">
                  <p>{{ store.hoaDonDetail.mo_ta }}</p>
                </a-col>
                <a-col :md="4" style="text-align: left;">
                  <p>Giảm giá:</p>
                </a-col>
                <a-col :md="4" style="text-align: right;color: red;">
                  <p>- {{
                    formatCurrency(store.hoaDonDetail.giam_gia) }} VNĐ</p>
                </a-col>
              </a-row>
              <a-row>
                <a-col :md="16"></a-col>
                <a-col :md="4" style="text-align: left;">
                  <p>Phí vận chuyển:</p>
                </a-col>
                <a-col :md="4" style="text-align: right;">
                  <p>+ {{ formatCurrency(store.hoaDonDetail.phi_van_chuyen) }} VNĐ</p>
                </a-col>
              </a-row>
              <a-row
                v-if="store.hoaDonDetail?.hinh_thuc_thanh_toan === 'Chuyển khoản' && store.chiTietHoaDons[0]?.phu_thu > 0">
                <a-col :md="16"></a-col>
                <a-col :md="4" style="text-align: left;">
                  <p style="color: #f5222d;">Đã thanh toán: </p>
                </a-col>
                <a-col :md="4" style="text-align: right;">
                  <p style="color: #f5222d;">{{ formatCurrency(store.hoaDonDetail.tong_tien_sau_giam -
                    store.chiTietHoaDons[0]?.phu_thu) }} VNĐ</p>
                </a-col>
              </a-row>
              <a-row
                v-if="store.hoaDonDetail?.hinh_thuc_thanh_toan === 'Chuyển khoản' && store.chiTietHoaDons[0]?.phu_thu > 0">
                <a-col :md="12"></a-col>
                <a-col :md="12" style="text-align: left;">
                  <p style="color: #f5222d;">Vui lòng thanh toán thêm {{
                    formatCurrency(store.chiTietHoaDons[0]?.phu_thu) }} VNĐ sau khi nhận hàng</p>
                </a-col>
                <!-- <a-col :md="4" style="text-align: right;">
                                    <p style="color: #f5222d;">+ {{ formatCurrency(store.chiTietHoaDons[0]?.phu_thu) }} VNĐ</p>
                                </a-col> -->
              </a-row>
              <a-row
                v-if="store.hoaDonDetail?.hinh_thuc_thanh_toan === 'Chuyển khoản' && store.chiTietHoaDons[0]?.phu_thu > 0">
                <a-col :md="16"></a-col>
                <a-col :md="4" style="text-align: left;">
                  <p style="color: #f5222d;">Đã thanh toán: </p>
                </a-col>
                <a-col :md="4" style="text-align: right;">
                  <p style="color: #f5222d;">{{ formatCurrency(store.hoaDonDetail.tong_tien_sau_giam -
                    store.chiTietHoaDons[0]?.phu_thu) }} VNĐ</p>
                </a-col>
              </a-row>
              <a-row
                v-if="store.hoaDonDetail?.hinh_thuc_thanh_toan === 'Chuyển khoản' && store.chiTietHoaDons[0]?.phu_thu > 0">
                <a-col :md="12"></a-col>
                <a-col :md="12" style="text-align: left;">
                  <p style="color: #f5222d;">Vui lòng thanh toán thêm {{
                    formatCurrency(store.chiTietHoaDons[0]?.phu_thu) }} VNĐ sau khi nhận hàng</p>
                </a-col>
                <!-- <a-col :md="4" style="text-align: right;">
                                    <p style="color: #f5222d;">+ {{ formatCurrency(store.chiTietHoaDons[0]?.phu_thu) }} VNĐ</p>
                                </a-col> -->
              </a-row>
              <template v-if="store.hoaDonDetail.trang_thai?.toLowerCase() === 'trả hàng'">
                <a-row>
                  <a-col :md="16"></a-col>
                  <a-col :md="4" style="text-align: left;">
                    <p>Tổng tiền khách đã thanh toán:</p>
                  </a-col>
                  <a-col :md="4" style="text-align: right;">
                    <p>{{ formatCurrency(store.hoaDonDetail.tong_tien_sau_giam) }} VNĐ</p>
                  </a-col>
                </a-row>
                <a-row>
                  <a-col :md="16"></a-col>
                  <a-col :md="4" style="text-align: left;">
                    <p>Tổng tiền hoàn trả lại khách:</p>
                  </a-col>
                  <a-col :md="4" style="text-align: right;">
                    <p>- {{formatCurrency(store.traHangs.reduce((total, traHang) => total +
                      traHang.tong_tien_hoan, 0))}} VNĐ</p>
                  </a-col>
                </a-row>
              </template>
              <a-row>
                <a-col :md="16"></a-col>
                <a-col :md="4" style="text-align: left;">
                  <p>Thành tiền:</p>
                </a-col>
                <a-col :md="4" style="text-align: right;">
                  <h6>{{
                    formatCurrency((store.hoaDonDetail.tong_tien_sau_giam) -
                      (store.traHangs.reduce((total,
                        traHang) => total + traHang.tong_tien_hoan, 0)))}} VNĐ</h6>
                </a-col>
              </a-row>
            </div>
          </div>
        </a-col>


        <a-col :md="8">
          <div class="info-box">
            <a-row>
              <a-col :md="16">
                <h5>Thông tin khách hàng</h5>
              </a-col>
              <a-col :md="8" class="text-right">
                <div v-if="!isEditingCustomer">
                  <a-button type="primary" :disabled="cannotEdit" :class="{ 'disabled': cannotEdit }"
                    @click="openDrawer">
                    Sửa
                  </a-button>
                </div>
              </a-col>
            </a-row>
            <hr>
            <div v-if="!isEditingCustomer">
              <p>Tên: {{ store.hoaDonDetail.ho_ten || 'Khách lẻ' }}</p>
              <p>Email: {{ store.hoaDonDetail.email || 'Chưa xác định' }}</p>
              <p>Phone: {{ store.hoaDonDetail.sdt_nguoi_nhan || 'Chưa xác định' }}</p>
              <p>Địa chỉ: {{ store.hoaDonDetail.dia_chi || 'Chưa xác định' }}</p>
            </div>
            <div v-else>
              <p>Tên: {{ store.hoaDonDetail.ho_ten || 'Khách lẻ' }}</p>
              <p>Email: {{ store.hoaDonDetail.email || 'Chưa xác định' }}</p>
              <p>Phone: {{ store.hoaDonDetail.sdt_nguoi_nhan || 'Chưa xác định' }}</p>
              <p>Địa chỉ: {{ store.hoaDonDetail.dia_chi || 'Chưa xác định' }}</p>
            </div>
            <!-- Drawer cho chỉnh sửa thông tin khách hàng -->
            <a-drawer v-model:open="isEditingCustomer" title="Chỉnh sửa thông tin khách hàng" placement="right"
              :width="500" @after-open-change="afterOpenChange">
              <a-form :model="editedCustomer" layout="vertical" @submit.prevent="saveCustomerInfo">
                <a-form-item label="Tên người nhận" name="hoTen">
                  <a-input v-model:value="editedCustomer.hoTen" placeholder="Nhập tên người nhận" />
                </a-form-item>
                <a-form-item label="Email" name="email">
                  <a-input v-model:value="editedCustomer.email" type="text" placeholder="Nhập email" />
                </a-form-item>
                <a-form-item label="Số điện thoại" name="sdtNguoiNhan">
                  <a-input v-model:value="editedCustomer.sdtNguoiNhan" placeholder="Nhập số điện thoại" />
                </a-form-item>
                <a-form-item label="Tỉnh/Thành phố" name="tinh">
                  <a-select v-model:value="editedCustomer.tinh" placeholder="Chọn tỉnh/thành phố" show-search
                    :filter-option="filterOption" @change="handleTinhChange" @focus="handleFocus" @blur="handleBlur">
                    <a-select-option v-for="tinh in tinhList" :key="tinh.id" :value="tinh.name">
                      {{ tinh.name }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="Quận/Huyện" name="huyen">
                  <a-select v-model:value="editedCustomer.huyen" placeholder="Chọn quận/huyện" show-search
                    :filter-option="filterOption" :disabled="!editedCustomer.tinh" @change="handleHuyenChange"
                    @focus="handleFocus" @blur="handleBlur">
                    <a-select-option v-for="huyen in huyenList" :key="huyen.id" :value="huyen.name">
                      {{ huyen.name }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="Xã/Phường" name="xa">
                  <a-select v-model:value="editedCustomer.xa" placeholder="Chọn xã/phường" show-search
                    :filter-option="filterOption" :disabled="!editedCustomer.huyen" @focus="handleFocus"
                    @blur="handleBlur">
                    <a-select-option v-for="xa in xaList" :key="xa.id" :value="xa.name">
                      {{ xa.name }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="Địa chỉ cụ thể" name="diaChiCuThe">
                  <a-input v-model:value="editedCustomer.diaChiCuThe" placeholder="Nhập địa chỉ cụ thể" />
                </a-form-item>

                <!-- Bảng danh sách địa chỉ -->
                <a-divider orientation="left">Danh sách địa chỉ</a-divider>
                <a-table :columns="addressColumns" :data-source="addressList" :pagination="false" row-key="id">
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'thao_tac'">
                      <a-button type="link" @click="selectAddress(record)">Chọn</a-button>
                    </template>
                  </template>
                  <template #emptyText>
                    <span>Không có dữ liệu</span>
                  </template>
                </a-table>

                <a-form-item>
                  <a-button type="primary" html-type="submit" style="margin-right:10px">Lưu</a-button>
                  <a-button type="default" @click="closeDrawer" style="margin-right:auto">Hủy</a-button>
                </a-form-item>
              </a-form>
            </a-drawer>
          </div>

          <div class="info-box">
            <a-row>
              <a-col :md="16">
                <h5>Ghi chú</h5>
              </a-col>
              <a-col :md="8" class="text-right">
                <div v-if="!isEditingNote">
                  <a-button type="primary" :disabled="cannotEdit" :class="{ 'disabled': cannotEdit }"
                    @click="startEditingNote">
                    Sửa
                  </a-button>
                </div>
                <div v-else class="edit-buttons">
                  <a-button type="primary" @click="saveNote">Lưu</a-button>
                  <a-button style="margin-left: 8px;" type="default" @click="cancelEditingNote">Hủy</a-button>
                </div>
              </a-col>
            </a-row>
            <hr>

            <div v-if="!isEditingNote">
              <a-textarea :value="store.hoaDonDetail.ghi_chu || 'Không có ghi chú'" :rows="2" readonly />
            </div>
            <div v-else>
              <a-form @submit="saveNote">
                <a-form-item>
                  <a-textarea v-model:value="editedNote" :rows="2" placeholder="Nhập ghi chú..." />
                </a-form-item>
              </a-form>
            </div>
          </div>
        </a-col>
      </a-row>

      <!-- Popup thêm sản phẩm -->
      <a-modal v-model:visible="showAddProductPopup" title="Danh sách sản phẩm" :footer="null" width="85%">
        <a-row :gutter="16" class="modal-header">
          <a-col :span="18">
            <a-input v-model:value="searchKeyword" @input="searchProducts" placeholder="Tìm kiếm sản phẩm..." />
          </a-col>
          <a-col :span="6" class="text-right">
            <a-button type="primary" @click="addSelectedProducts">Thêm sản phẩm</a-button>
          </a-col>
        </a-row>
        <a-table :columns="productPopupColumns" :data-source="store.listCTSP_HD" :pagination="false"
          row-key="id_chi_tiet_san_pham">
          <template #bodyCell="{ column, record, index }">
            <template v-if="column.key === 'stt'">
              {{ index + 1 + (store.currentPage * 5) }}
            </template>
            <template v-if="column.key === 'hinh_anh'">
              <a-image :width="50" :height="50" :src="record.hinh_anh || '/images/default.jpg'"
                alt="Hình ảnh sản phẩm" />
            </template>
            <template v-if="column.key === 'gia_ban'">
              <div>
                <span v-if="record.giaHienTai && record.giaHienTai < record.giaGoc" style="color: red;">
                  {{ formatCurrency(record.giaHienTai || 0) }}
                </span>
                <span v-else>
                  {{ formatCurrency(record.giaGoc || 0) }}
                </span>
                <div v-if="record.giaHienTai && record.giaHienTai < record.giaGoc" class="original-price">
                  {{ formatCurrency(record.giaGoc || 0) }}
                </div>
              </div>
            </template>
            <template v-if="column.key === 'so_luong'">
              {{ record.so_luong }}
              <span v-if="shouldCalculateSoLuongTon">
                (Khả dụng: {{ calculateSoLuongTon(record) }})
              </span>
            </template>
            <template v-if="column.key === 'so_luong_mua'">
              <div class="quantity-input">
                <a-button @click="decreaseQuantityPopup(index)" :disabled="quantities[index] <= 0">-</a-button>
                <a-input-number type="number" v-model:value="quantities[index]" :min="0"
                  :max="shouldCalculateSoLuongTon ? calculateSoLuongTon(record) : record.so_luong"
                  @change="validateQuantity(index, shouldCalculateSoLuongTon ? calculateSoLuongTon(record) : record.so_luong)" />
                <a-button @click="increaseQuantityPopup(index)"
                  :disabled="quantities[index] >= (shouldCalculateSoLuongTon ? calculateSoLuongTon(record) : record.so_luong)">+</a-button>
              </div>
            </template>
          </template>
        </a-table>
        <div class="pagination">
          <a-button @click="changePage(store.currentPage - 1)" :disabled="store.currentPage === 0">
            Trang trước
          </a-button>
          <span>Trang {{ store.currentPage + 1 }} / {{ store.totalPages }}</span>
          <a-button @click="changePage(store.currentPage + 1)" :disabled="store.currentPage >= store.totalPages - 1">
            Trang sau
          </a-button>
        </div>
        <a-button type="default" class="close-btn" @click="closeAddProductPopup">Đóng</a-button>
      </a-modal>

      <!-- Popup chỉnh sửa số lượng -->
      <a-modal v-model:visible="showQuantityPopup" width="350px" class="text-center"
        :title="popupType === 'decrease' ? 'Giảm số lượng' : 'Tăng số lượng'" :footer="null">
        <div class="popup-input">
          <label>
            <span v-if="shouldCalculateSoLuongTon">Số lượng sản phẩm
              (Khả dụng: {{ popupType === 'decrease' ? currentProduct.so_luong :
                calculateSoLuongTon(currentProduct)
              }})
            </span>
            <span v-else>Số lượng sản phẩm
              (Khả dụng: {{ popupType === 'decrease' ? currentProduct.so_luong :
                currentProduct.so_luong_con_lai || 0 }})
            </span>
          </label>
          <label style="width: 100px;">Số lượng:</label>
          <a-input-number v-if="shouldCalculateSoLuongTon" style="width: 150px;" type="number"
            v-model:value="quantityChange" :min="0"
            :max="popupType === 'decrease' ? currentProduct.so_luong : (shouldCalculateSoLuongTon ? calculateSoLuongTon(currentProduct) : currentProduct.so_luong)" />
          <a-input-number v-else style="width: 150px;" type="number" v-model:value="quantityChange" :min="0"
            :max="popupType === 'decrease' ? currentProduct.so_luong : (shouldCalculateSoLuongTon ? currentProduct.so_luong : currentProduct.so_luong_con_lai)" />
        </div>
        <div class="popup-actions">
          <a-button type="primary" @click="updateQuantity">
            <i :class="popupType === 'decrease' ? 'fas fa-minus' : 'fas fa-plus'"></i>
          </a-button>
          <a-button type="default" @click="closeQuantityPopup">Hủy</a-button>
        </div>
      </a-modal>

      <!-- ✅ NEW: Modal xác nhận thay đổi giá -->
      <a-modal v-model:visible="showPriceChangeModal" title="⚠️ Thông báo thay đổi giá" :maskClosable="false"
        width="500px">
        <div class="price-change-content">
          <p><strong>{{ priceChangeInfo.productName }}</strong> có sự thay đổi giá so với lần trước:</p>

          <div class="price-comparison">
            <div class="price-item old-price">
              <span>Giá cũ trong hóa đơn:</span>
              <span class="price-value">{{ formatCurrency(priceChangeInfo.oldPrice) }}</span>
            </div>
            <div class="price-item new-price">
              <span>Giá hiện tại:</span>
              <span class="price-value">{{ formatCurrency(priceChangeInfo.newPrice) }}</span>
            </div>
          </div>

          <div class="price-change-info">
            <p><i class="fas fa-info-circle"></i> Sản phẩm sẽ được thêm vào hóa đơn với mức giá mới.</p>
            <p><i class="fas fa-exclamation-triangle"></i> Phụ thu chỉ được tính khi cộng vào sản phẩm có
              cùng giá.</p>
          </div>
        </div>

        <template #footer>
          <a-button @click="cancelPriceChange" size="large">
            <i class="fas fa-times"></i> Hủy
          </a-button>
          <a-button type="primary" @click="confirmPriceChange" size="large">
            <i class="fas fa-check"></i> Đồng ý thêm
          </a-button>
        </template>
      </a-modal>

      <!-- ✅ NEW: Modal thông báo kết quả xử lý -->
      <a-modal v-model:visible="showProcessingResultModal" title="📋 Kết quả xử lý" :footer="null" width="450px">
        <div class="processing-result-content">
          <div class="result-item">
            <a-tag color="green" size="large">
              <i class="fas fa-plus-circle"></i>
              {{ processingResult.mergedProducts }} sản phẩm cộng số lượng
            </a-tag>
          </div>

          <div class="result-item">
            <a-tag color="blue" size="large">
              <i class="fas fa-file-alt"></i>
              {{ processingResult.newProducts }} sản phẩm thêm mới
            </a-tag>
          </div>

          <div v-if="processingResult.hasPriceConflict" class="result-item">
            <a-tag color="orange" size="large">
              <i class="fas fa-exclamation-triangle"></i>
              Có thay đổi giá
            </a-tag>
          </div>

          <div v-if="processingResult.phuThuApplied" class="result-item">
            <a-tag color="purple" size="large">
              <i class="fas fa-coins"></i>
              Đã tính phụ thu
            </a-tag>
          </div>
        </div>

        <div class="modal-footer">
          <a-button type="primary" @click="closeProcessingResult" block size="large">
            <i class="fas fa-check"></i> Đã hiểu
          </a-button>
        </div>
      </a-modal>

      <div class="notification">
        <template v-if="store.hoaDonDetail.trang_thai?.toLowerCase() === 'trả hàng'">
          HOÀN THÀNH ĐƠN HÀNG {{
            formatCurrency(
              store.hoaDonDetail.tong_tien_sau_giam -
              store.traHangs.reduce((total, traHang) => total + traHang.tong_tien_hoan, 0)
            )
          }} VNĐ
        </template>
        <template v-else>
          {{ store.hoaDonDetail.trang_thai?.toUpperCase() || 'ĐANG XỬ LÝ' }} ĐƠN HÀNG {{
            formatCurrency(store.hoaDonDetail.tong_tien_sau_giam + store.hoaDonDetail.phi_van_chuyen)
          }} VNĐ
        </template>
      </div>
    </div>
    <div v-else class="text-center">
      <p>Không tìm thấy hóa đơn.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick, h } from 'vue';
import { useGbStore } from '@/stores/gbStore';
import { useRoute, useRouter } from 'vue-router';
import { toast } from 'vue3-toastify';
import axios from 'axios';
import { jsPDF } from 'jspdf';
import '../../../config/fonts/Roboto-normal'
import '../../../config/fonts/Roboto-bold'
import logo from '../../../images/logo/anhLogoMenWear.png';
import QRCode from 'qrcode';

// Ant Design Vue components
import { Row as ARow, Col as ACol, Button as AButton, Divider as ADivider, Form as AForm, FormItem as AFormItem, Input as AInput, Textarea as ATextarea, Table as ATable, Modal as AModal, InputNumber as AInputNumber, Spin as ASpin, message } from 'ant-design-vue';
import { EditOutlined, ExclamationCircleOutlined, DeleteOutlined } from '@ant-design/icons-vue';
import { banHangService } from '@/services/banHangService';
import { hoaDonService } from '@/services/hoaDonService';

const store = useGbStore();
const route = useRoute();
const router = useRouter();
const loading = ref(true);

// Product table columns
const productColumns = [
  { title: 'Id', key: 'id_chi_tiet_san_pham', width: '10%' },
  { title: 'Sản phẩm', key: 'san_pham', width: '40%' },
  { title: 'Đơn giá', key: 'don_gia', width: '20%' },
  { title: 'Số lượng', key: 'so_luong', width: '10%' },
  { title: 'Tổng tiền', key: 'thanh_tien', width: '20%' },
  { title: 'Thao tác', key: 'thao_tac', width: '10%' },
];
const productColumnsHoan = [
  { title: 'Sản phẩm', key: 'san_pham', width: '35%' },
  { title: 'Đơn giá', key: 'don_gia', width: '20%' },
  { title: 'Số lượng', key: 'so_luong', width: '10%' },
  { title: 'Thành tiền', key: 'thanh_tien', width: '20%' },
  { title: 'Trạng thái', key: 'trang_thai', width: '15%' },
];
// Product popup table columns
const productPopupColumns = [
  { title: 'STT', key: 'stt', width: '2%' },
  { title: 'Hình ảnh', dataIndex: 'hinh_anh', key: 'hinh_anh', width: '10%' },
  { title: 'Tên sản phẩm', dataIndex: 'ten_san_pham', key: 'ten_san_pham', width: '20%' },
  { title: 'Danh mục', dataIndex: 'ten_danh_muc', key: 'ten_danh_muc', width: '15%' },
  { title: 'Màu sắc', dataIndex: 'ten_mau', key: 'ten_mau', width: '8%' },
  { title: 'Size', dataIndex: 'gia_tri', key: 'gia_tri', width: '7%' },
  { title: 'Số lượng', dataIndex: 'so_luong', key: 'so_luong', width: '8%' },
  { title: 'Giá bán(VNĐ)', key: 'gia_ban', width: '10%' },
  // { title: 'Trạng thái', dataIndex: 'trang_thai', key: 'trang_thai', width: '8%' },
  { title: 'Số lượng mua', key: 'so_luong_mua', width: '20%' },
];

// Danh sách trạng thái cho hình thức "Giao hàng"
const defaultStatusSteps = [
  { name: 'Đơn hàng đã đặt', backendStatus: 'Chờ xác nhận', icon: 'fas fa-file-alt' },
  { name: 'Đã xác nhận', backendStatus: 'Đã xác nhận', icon: 'fas fa-check' },
  { name: 'Đã cập nhật', backendStatus: 'Đã cập nhật', icon: 'fas fa-edit' },
  { name: 'Đang đóng gói', backendStatus: 'Chờ đóng gói', icon: 'fas fa-truck' },
  { name: 'Đã giao cho ĐVVC', backendStatus: 'Đang giao', icon: 'fas fa-truck' },
  { name: 'Hoàn thành', backendStatus: 'Hoàn thành', icon: 'fas fa-check-circle' },
  { name: 'Đã hủy', backendStatus: 'Đã hủy', icon: 'fas fa-times-circle' }
];

// Danh sách trạng thái cho "Nhận tại cửa hàng"
const storePickupStatusSteps = [
  { name: 'Chờ xác nhận', backendStatus: 'Chờ xác nhận', icon: 'fas fa-hourglass-start' },
  { name: 'Đã cập nhật', backendStatus: 'Đã cập nhật', icon: 'fas fa-edit' },
  { name: 'Hoàn thành', backendStatus: 'Hoàn thành', icon: 'fas fa-check-circle' },
  { name: 'Đã hủy', backendStatus: 'Đã hủy', icon: 'fas fa-times-circle' }
];

// // Computed property để quyết định danh sách trạng thái
// const computedStatusSteps = computed(() => {
//     if (store.hoaDonDetail?.phuong_thuc_nhan_hang === 'Nhận tại cửa hàng') {
//         return storePickupStatusSteps;
//     }
//     return defaultStatusSteps;
// });

// Address table columns
const addressColumns = [
  { title: 'Địa chỉ', dataIndex: 'dia_chi', key: 'dia_chi' },
  { title: 'Thao tác', key: 'thao_tac' },
];

// Danh sách tỉnh, huyện, xã từ API
const tinhList = ref([]);
const huyenList = ref([]);
const xaList = ref([]);

// Dữ liệu cho bảng danh sách địa chỉ
const addressList = computed(() => {
  return store.listDCKHinHD.map(item => ({
    id: item.idDiaChiKhachHang,
    dia_chi: `${item.soNha}, ${item.xaPhuong}, ${item.quanHuyen}, ${item.tinhThanhPho}`,
    soNha: item.soNha,
    xaPhuong: item.xaPhuong,
    quanHuyen: item.quanHuyen,
    tinhThanhPho: item.tinhThanhPho,
  }));
});

// Trạng thái chỉnh sửa thông tin khách hàng
const isEditingCustomer = ref(false);
const editedCustomer = ref({
  hoTen: '',
  email: '',
  sdtNguoiNhan: '',
  tinh: null,
  huyen: null,
  xa: null,
  diaChiCuThe: '',
  diaChi: '', // Địa chỉ đầy đủ (sẽ được ghép từ các trường)
});
// Hàm tìm kiếm cho select
const filterOption = (input, option) => {
  return option.value.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};

// Xử lý focus
const handleFocus = () => {
  console.log('Focus on select');
};

// Xử lý blur
const handleBlur = () => {
  console.log('Blur on select');
};

// Mở drawer và load dữ liệu
const openDrawer = async () => {
  // Lấy danh sách tỉnh trước khi mở drawer
  if (tinhList.value.length === 0) {
    await fetchTinhList();
  }
  // Load dữ liệu ban đầu
  editedCustomer.value = {
    hoTen: store.hoaDonDetail.ho_ten || '',
    email: store.hoaDonDetail.email || '',
    sdtNguoiNhan: store.hoaDonDetail.sdt_nguoi_nhan || '',
    tinh: null,
    huyen: null,
    xa: null,
    diaChiCuThe: '',
    diaChi: store.hoaDonDetail.dia_chi || '',
  };

  // Tách địa chỉ thành các trường nếu có
  if (store.hoaDonDetail.dia_chi) {
    const diaChiParts = store.hoaDonDetail.dia_chi.split(', ');
    console.log(diaChiParts.length);
    if (diaChiParts.length > 4 || diaChiParts.length === 4) {
      if (diaChiParts.length > 4) {
        editedCustomer.value.diaChiCuThe = diaChiParts[0] + ', ' + diaChiParts[1];
        editedCustomer.value.xa = diaChiParts[diaChiParts.length - 3];
        editedCustomer.value.huyen = diaChiParts[diaChiParts.length - 2];
        editedCustomer.value.tinh = diaChiParts[diaChiParts.length - 1];
      } if (diaChiParts.length === 4) {
        editedCustomer.value.diaChiCuThe = diaChiParts[0];
        editedCustomer.value.xa = diaChiParts[1];
        editedCustomer.value.huyen = diaChiParts[2];
        editedCustomer.value.tinh = diaChiParts[3];
      }
      // Load danh sách huyện và xã dựa trên tỉnh, huyện đã chọn
      if (editedCustomer.value.tinh) {
        const provinceCode = findProvinceCode(editedCustomer.value.tinh);
        if (provinceCode) {
          await fetchHuyenList(provinceCode);
        }
      }
      if (editedCustomer.value.huyen) {
        const districtCode = findDistrictCode(editedCustomer.value.huyen);
        if (districtCode) {
          await fetchXaList(districtCode);
        }
      }
    }
  }
  isEditingCustomer.value = true;
};

// Hàm chọn địa chỉ từ bảng
const selectAddress = (record) => {
  editedCustomer.value.diaChiCuThe = record.soNha;
  editedCustomer.value.xa = record.xaPhuong;
  editedCustomer.value.huyen = record.quanHuyen;
  editedCustomer.value.tinh = record.tinhThanhPho;

  // Load danh sách huyện và xã dựa trên tỉnh, huyện đã chọn
  const provinceCode = findProvinceCode(record.tinhThanhPho);
  if (provinceCode) {
    fetchHuyenList(provinceCode).then(() => {
      const districtCode = findDistrictCode(record.quanHuyen);
      if (districtCode) {
        fetchXaList(districtCode);
      }
    });
  }
};

// Đóng drawer
const closeDrawer = () => {
  isEditingCustomer.value = false;
  // Reset các trường
  editedCustomer.value = {
    hoTen: '',
    email: '',
    sdtNguoiNhan: '',
    tinh: null,
    huyen: null,
    xa: null,
    diaChiCuThe: '',
    diaChi: '',
  };
  huyenList.value = [];
  xaList.value = [];
};

// Lấy danh sách tỉnh từ API
const fetchTinhList = async () => {
  try {
    const response = await axios.get('https://provinces.open-api.vn/api/p/');
    tinhList.value = response.data;
  } catch (error) {
    console.error('Lỗi khi lấy danh sách tỉnh:', error);
    toast.error('Không thể lấy danh sách tỉnh!');
  }
};

// Lấy danh sách huyện theo tỉnh
const fetchHuyenList = async (provinceCode) => {
  try {
    const response = await axios.get(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`);
    huyenList.value = response.data.districts || [];
  } catch (error) {
    console.error('Lỗi khi lấy danh sách huyện:', error);
    toast.error('Không thể lấy danh sách huyện!');
  }
};

// Lấy danh sách xã theo huyện
const fetchXaList = async (districtCode) => {
  try {
    const response = await axios.get(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`);
    xaList.value = response.data.wards || [];
  } catch (error) {
    console.error('Lỗi khi lấy danh sách xã:', error);
    toast.error('Không thể lấy danh sách xã!');
  }
};

// Tìm mã tỉnh từ tên tỉnh
const findProvinceCode = (provinceName) => {
  const province = tinhList.value.find(tinh => tinh.name === provinceName);
  return province ? province.code : null;
};

// Tìm mã huyện từ tên huyện
const findDistrictCode = (districtName) => {
  const district = huyenList.value.find(huyen => huyen.name === districtName);
  return district ? district.code : null;
};
// Xử lý khi chọn tỉnh
const handleTinhChange = async (value) => {
  editedCustomer.value.huyen = null;
  editedCustomer.value.xa = null;
  xaList.value = [];

  const provinceCode = findProvinceCode(value);
  if (provinceCode) {
    await fetchHuyenList(provinceCode);
  }
};

// Xử lý khi chọn huyện
const handleHuyenChange = async (value) => {
  editedCustomer.value.xa = null;

  const districtCode = findDistrictCode(value);
  if (districtCode) {
    await fetchXaList(districtCode);
  }
};
const validateCustomerInfo = () => {
  const hoTen = editedCustomer.value.hoTen.trim();
  // Kiểm tra các trường bắt buộc không được để trống
  if (!hoTen) {
    message.error('Vui lòng nhập tên người nhận!');
    return false;
  } else if (!/^[\p{L}]+([\s][\p{L}]+)*$/u.test(hoTen)) {
    message.error('Tên người nhận không hợp lệ!');
    return false;
  } else if (hoTen.length < 4) {
    message.error('Tên người nhận phải có ít nhất 4 ký tự!');
    return false;
  } else if (hoTen.length > 60) {
    message.error('Tên người nhận không được vượt quá 60 ký tự!');
    return false;
  }
  if (!editedCustomer.value.email) {
    message.error('Vui lòng nhập email!');
    return false;
  }
  // Kiểm tra định dạng email
  const emailRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
  if (!emailRegex.test(editedCustomer.value.email)) {
    message.error('Email không đúng định dạng!');
    return false;
  }
  if (!editedCustomer.value.sdtNguoiNhan) {
    message.error('Vui lòng nhập số điện thoại!');
    return false;
  }
  // Kiểm tra định dạng số điện thoại Việt Nam
  const phoneRegex = /^(0)(3[2-9]|5[2689]|7[06-9]|8[1-9]|9[0-9])[0-9]{7}$/;
  if (!phoneRegex.test(editedCustomer.value.sdtNguoiNhan)) {
    message.error('Số điện thoại không đúng định dạng!(VD: 0912345678)');
    return false;
  }
  if (!editedCustomer.value.tinh) {
    message.error('Vui lòng chọn tỉnh/thành phố!');
    return false;
  }
  if (!editedCustomer.value.huyen) {
    message.error('Vui lòng chọn quận/huyện!');
    return false;
  }
  if (!editedCustomer.value.xa) {
    message.error('Vui lòng chọn xã/phường!');
    return false;
  }
  if (!editedCustomer.value.diaChiCuThe) {
    message.error('Vui lòng nhập địa chỉ cụ thể!');
    return false;
  }
  return true;
};
// Lưu thông tin khách hàng
const saveCustomerInfo = () => {
  if (!validateCustomerInfo()) {
    return;
  }
  AModal.confirm({
    title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
      h(EditOutlined, { style: 'color: #faad14; font-size: 22px;' }),
      h('span', { style: 'font-size: 16px; font-weight: 600;' }, 'Cập nhật thông tin KH')
    ]),
    content: () => h('div', { style: 'padding: 8px 0;' }, [
      h('p', { style: 'margin: 0 0 12px 0; font-size: 14px;' }, 'Phí vận chuyển có thể thay đổi khi bạn cập nhật địa chỉ.'),
      h('div', { style: 'background: #fffbe6; padding: 12px; border-radius: 6px; border: 1px solid #ffe58f;' }, [
        h('div', { style: 'display: flex; align-items: center; gap: 8px; color: #d48806;' }, [
          h(ExclamationCircleOutlined, { style: 'font-size: 14px;' }),
          h('span', { style: 'font-size: 13px;' }, 'Phí vận chuyển sẽ được tính lại')
        ])
      ])
    ]),
    okText: 'Cập nhật',
    cancelText: 'Hủy',
    okButtonProps: { size: 'large', style: { height: '38px' } },
    cancelButtonProps: { size: 'large', style: { height: '38px' } },
    centered: true,
    width: 480,
    onOk: async () => {
      // Ghép địa chỉ từ các trường
      if (
        editedCustomer.value.tinh &&
        editedCustomer.value.huyen &&
        editedCustomer.value.xa &&
        editedCustomer.value.diaChiCuThe
      ) {
        editedCustomer.value.diaChi = `${editedCustomer.value.diaChiCuThe}, ${editedCustomer.value.xa}, ${editedCustomer.value.huyen}, ${editedCustomer.value.tinh}`;
      } else {
        editedCustomer.value.diaChi = editedCustomer.value.diaChiCuThe || '';
      }

      // Tính phí vận chuyển
      const phiVanChuyen = await calculatePhiVanChuyen(true);
      console.log('Phí vận chuyển tính được khi cập nhật thông tin địa chỉ:', phiVanChuyen);
      // Gọi hàm cập nhật thông tin khách hàng
      store.updateCustomerInfo(store.hoaDonDetail.ma_hoa_don, {
        hoTen: editedCustomer.value.hoTen,
        email: editedCustomer.value.email,
        sdtNguoiNhan: editedCustomer.value.sdtNguoiNhan,
        diaChi: editedCustomer.value.diaChi,
      }, phiVanChuyen);
      closeDrawer();
    },
  });
};
// Hàm xử lý sau khi drawer mở/đóng
const afterOpenChange = (visible) => {
  if (!visible) {
    closeDrawer();
  }
};

const sortedTrangThaiHistory = computed(() => {
  return [...store.trangThaiHistory].sort((a, b) => {
    return new Date(a.ngay_chuyen) - new Date(b.ngay_chuyen);
  });
});

// const getIconForStatus = (trangThai) => {
//     const statusSteps = store.hoaDonDetail?.phuong_thuc_nhan_hang === 'Nhận tại cửa hàng'
//         ? storePickupStatusSteps
//         : defaultStatusSteps;
//     const status = statusSteps.find(s => s.backendStatus === trangThai);
//     return status ? status.icon : 'fas fa-question'; // Icon mặc định nếu không tìm thấy
// };

const isCompletedOrCancelled = computed(() => {
  const trangThai = store.hoaDonDetail?.trang_thai;
  return trangThai === 'Hoàn thành' || trangThai === 'Đã hủy' || trangThai === 'Trả hàng';
});

const cannotCancel = computed(() => {
  const trangThai = store.hoaDonDetail?.trang_thai;
  if (store.hoaDonDetail?.phuong_thuc_nhan_hang === 'Nhận tại cửa hàng') {
    return ['Hoàn thành', 'Đã hủy', 'Trả hàng'].includes(trangThai);
  }
  return ['Đang giao', 'Đã nhận hàng', 'Hoàn thành', 'Đã hủy', 'Trả hàng'].includes(trangThai);
});
const cannotDecreaseOrRemoveProduct = computed(() => {
  const hinhThucThanhToan = store.hoaDonDetail?.hinh_thuc_thanh_toan;
  const loaiHoaDon = store.hoaDonDetail?.loai_hoa_don;
  if (loaiHoaDon === 'Offline' || (loaiHoaDon === 'Online' && hinhThucThanhToan === 'Chuyển khoản')) {
    return true;
  }
  if (loaiHoaDon === 'Online' && hinhThucThanhToan === 'Tiền mặt') {
    return false;
  }
  return true;
});


const cannotEditProduct = computed(() => {
  const trangThai = store.hoaDonDetail?.trang_thai;
  if (!trangThai) return true;

  // Nếu trạng thái là "Đã cập nhật", kiểm tra trạng thái trước đó
  let effectiveStatus = trangThai;
  if (trangThai === 'Đã cập nhật') {
    const previousStatus = store.trangThaiHistory
      .filter(history => history.trang_thai !== 'Đã cập nhật')
      .sort((a, b) => new Date(b.ngay_chuyen) - new Date(a.ngay_chuyen))[0]?.trang_thai;
    effectiveStatus = previousStatus || 'Chờ xác nhận';
  }

  if (store.hoaDonDetail?.phuong_thuc_nhan_hang === 'Nhận tại cửa hàng') {
    return !['Chờ xác nhận'].includes(effectiveStatus);
  }

  // Mod: Chỉ cho phép sửa khi trạng thái là "Chờ xác nhận"
  return !['Chờ xác nhận'].includes(effectiveStatus);
});

const cannotEdit = computed(() => {
  const trangThai = store.hoaDonDetail?.trang_thai;
  if (!trangThai) return true;

  // Nếu trạng thái là "Đã cập nhật", kiểm tra trạng thái trước đó
  let effectiveStatus = trangThai;
  if (trangThai === 'Đã cập nhật') {
    const previousStatus = store.trangThaiHistory
      .filter(history => history.trang_thai !== 'Đã cập nhật')
      .sort((a, b) => new Date(b.ngay_chuyen) - new Date(a.ngay_chuyen))[0]?.trang_thai;
    effectiveStatus = previousStatus || 'Chờ xác nhận';
  }

  if (store.hoaDonDetail?.phuong_thuc_nhan_hang === 'Nhận tại cửa hàng') {
    return !['Chờ xác nhận'].includes(effectiveStatus);
  }
  return !['Chờ xác nhận', 'Đã xác nhận', 'Chờ đóng gói'].includes(effectiveStatus);
});

const nextStatusText = computed(() => {
  const phuongThucNhanHang = store.hoaDonDetail?.phuong_thuc_nhan_hang;
  const trangThai = store.hoaDonDetail?.trang_thai;

  if (!phuongThucNhanHang || !trangThai) return 'Chuyển trạng thái';

  // Xác định trạng thái trước "Đã cập nhật" (nếu trạng thái hiện tại là "Đã cập nhật")
  let effectiveStatus = trangThai;
  if (trangThai === 'Đã cập nhật') {
    const previousStatus = store.trangThaiHistory
      .filter(history => history.trang_thai !== 'Đã cập nhật')
      .sort((a, b) => new Date(b.ngay_chuyen) - new Date(a.ngay_chuyen))[0]?.trang_thai;
    effectiveStatus = previousStatus || 'Chờ xác nhận'; // Mặc định là "Chờ xác nhận" nếu không tìm thấy
  }

  if (phuongThucNhanHang === 'Nhận tại cửa hàng') {
    if (effectiveStatus === 'Chờ xác nhận') return 'Hoàn thành';
    return 'Hoàn thành';
  }

  switch (effectiveStatus) {
    case 'Chờ xác nhận':
      return 'Xác nhận đơn hàng';
    case 'Đã xác nhận':
      return 'Chuẩn bị hàng';
    case 'Chờ đóng gói':
      return 'Giao cho ĐVVC';
    case 'Đang giao':
      return 'Hoàn thành';
    default:
      return 'Hoàn thành';
  }
});

const nextStatusValue = computed(() => {
  const phuongThucNhanHang = store.hoaDonDetail?.phuong_thuc_nhan_hang;
  const trangThai = store.hoaDonDetail?.trang_thai;

  if (!phuongThucNhanHang || !trangThai) return '';

  // Xác định trạng thái trước "Đã cập nhật"
  let effectiveStatus = trangThai;
  if (trangThai === 'Đã cập nhật') {
    const previousStatus = store.trangThaiHistory
      .filter(history => history.trang_thai !== 'Đã cập nhật')
      .sort((a, b) => new Date(b.ngay_chuyen) - new Date(a.ngay_chuyen))[0]?.trang_thai;
    effectiveStatus = previousStatus || 'Chờ xác nhận'; // Mặc định là "Chờ xác nhận" nếu không tìm thấy
  }

  if (phuongThucNhanHang === 'Nhận tại cửa hàng') {
    if (effectiveStatus === 'Chờ xác nhận') return 'Hoàn thành';
    return 'Hoàn thành';
  }

  switch (effectiveStatus) {
    case 'Chờ xác nhận':
      return 'Đã xác nhận';
    case 'Đã xác nhận':
      return 'Chờ đóng gói';
    case 'Chờ đóng gói':
      return 'Đang giao';
    case 'Đang giao':
      return 'Hoàn thành';
    default:
      return 'Hoàn thành';
  }
});

const formatCurrency = (value) => {
  return value ? new Intl.NumberFormat('vi-VN').format(value) : '0';
};
// format chỉ hiện hh:mm dd-mm-yyyy
const formatDate = (date) => {
  if (!date) return 'N/A';
  const d = new Date(date);
  return d.toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
};
// format chỉ hiện hh:mm:ss dd-mm-yyyy
const formatDateTime = (date) => {
  if (!date) return 'N/A';
  const d = new Date(date);
  return d.toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
};

const getStatusClass = (trangThai) => {
  if (trangThai === 'Đã hủy') {
    return 'text-danger';
  }
  return 'text-success';
};

// Trạng thái chỉnh sửa ghi chú
const isEditingNote = ref(false);
const editedNote = ref('');

// Bắt đầu chỉnh sửa ghi chú
const startEditingNote = () => {
  editedNote.value = store.hoaDonDetail.ghi_chu || '';
  isEditingNote.value = true;
};

// Lưu ghi chú
const saveNote = () => {
  // Loại bỏ khoảng trắng thừa trong chuỗi
  const trimmedNote = editedNote.value.trim().replace(/\s+/g, ' ');

  // Kiểm tra nếu ghi chú chỉ chứa khoảng trắng
  if (editedNote.value && !trimmedNote) {
    message.error('Ghi chú không được chứa khoảng trắng!');
    return;
  }

  // Kiểm tra độ dài ghi chú sau khi loại bỏ khoảng trắng thừa
  if (trimmedNote.length > 100) {
    message.error('Ghi chú không được vượt quá 100 ký tự!');
    return;
  }
  AModal.confirm({
    title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
      h(EditOutlined, { style: 'color: #1890ff; font-size: 22px;' }),
      h('span', { style: 'font-size: 16px; font-weight: 600;' }, 'Cập nhật ghi chú')
    ]),
    content: () => h('div', { style: 'padding: 8px 0;' }, [
      h('p', { style: 'margin: 0; font-size: 14px;' }, 'Bạn có muốn cập nhật ghi chú cho hóa đơn này không?')
    ]),
    okText: 'Cập nhật',
    cancelText: 'Hủy',
    okButtonProps: { size: 'large', style: { height: '38px' } },
    cancelButtonProps: { size: 'large', style: { height: '38px' } },
    centered: true,
    width: 420,
    onOk: () => {
      store.updateNote(store.hoaDonDetail.ma_hoa_don, trimmedNote);
      isEditingNote.value = false;
    },
  });
};

// Hủy chỉnh sửa ghi chú
const cancelEditingNote = () => {
  isEditingNote.value = false;
};

// Trạng thái popup thêm sản phẩm
const showAddProductPopup = ref(false);
const searchKeyword = ref('');
const quantities = ref([]);

// Tính số lượng tồn (khả dụng)
const calculateSoLuongTon = (record) => {
  const productInList = store.listCTSP_HD.find(item => item.id_chi_tiet_san_pham === record.id_chi_tiet_san_pham);
  const soLuongBanDau = productInList ? productInList.so_luong : record.so_luong;

  const hdct = store.chiTietHoaDons.find(item => item.id_chi_tiet_san_pham === record.id_chi_tiet_san_pham);
  const soLuongHienCo = hdct ? hdct.so_luong : 0;

  const soLuongTon = (soLuongBanDau || 0) - soLuongHienCo;
  // console.log('record.id_chi_tiet_san_pham:', record.id_chi_tiet_san_pham, 'soLuongBanDau:', soLuongBanDau, 'soLuongHienCo:', soLuongHienCo, 'soLuongTon:', soLuongTon);
  return Math.max(soLuongTon, 0);
};
// Tính tổng số lượng sản phẩm trong hóa đơn
const calculateTotalQuantity = () => {
  return store.chiTietHoaDons.reduce((total, item) => total + (item.so_luong || 0), 0);
};
// Hiển thị popup thêm sản phẩm
const showAddProductPopupFn = async () => {
  await store.getAllCTSP_HD(0, 5, '');
  quantities.value = new Array(store.listCTSP_HD.length).fill(0);
  showAddProductPopup.value = true;
};

// Đóng popup thêm sản phẩm
const closeAddProductPopup = () => {
  showAddProductPopup.value = false;
  searchKeyword.value = '';
  quantities.value = new Array(store.listCTSP_HD.length).fill(0);
};

// Tìm kiếm sản phẩm
const searchProducts = async () => {
  await store.getAllCTSP_HD(0, 5, searchKeyword.value);
  quantities.value = new Array(store.listCTSP_HD.length).fill(0);
};

// Thay đổi trang
const changePage = async (page) => {
  if (page >= 0 && page < store.totalPages) {
    await store.getAllCTSP_HD(page, 5, searchKeyword.value);
    quantities.value = new Array(store.listCTSP_HD.length).fill(0);
  }
};
const shouldCalculateSoLuongTon = computed(() => {
  const trangThai = store.hoaDonDetail?.trang_thai;
  if (!trangThai) return false;

  // Nếu trạng thái là "Đã cập nhật", kiểm tra trạng thái trước đó
  let effectiveStatus = trangThai;
  if (trangThai === 'Đã cập nhật') {
    const previousStatus = store.trangThaiHistory
      .filter(history => history.trang_thai !== 'Đã cập nhật')
      .sort((a, b) => new Date(b.ngay_chuyen) - new Date(a.ngay_chuyen))[0]?.trang_thai;
    effectiveStatus = previousStatus || 'Chờ xác nhận';
  }

  // Chỉ tính số lượng khả dụng nếu trạng thái là "Chờ xác nhận"
  return effectiveStatus === 'Chờ xác nhận';
});
// Tăng số lượng trong Popup
const increaseQuantityPopup = (index) => {
  const max = shouldCalculateSoLuongTon.value ? store.listCTSP_HD[index].so_luong : calculateSoLuongTon(store.listCTSP_HD[index]);
  if (quantities.value[index] < max) {
    quantities.value[index]++;
  }
};

// Giảm số lượng trong Popup
const decreaseQuantityPopup = (index) => {
  if (quantities.value[index] > 0) {
    quantities.value[index]--;
  }
};

// Validate số lượng
const validateQuantity = (index, max) => {
  if (quantities.value[index] < 0) {
    quantities.value[index] = 0;
  }
  if (shouldCalculateSoLuongTon.value && quantities.value[index] > max) {
    toast.error(`Số lượng mua không được vượt quá ${max}`);
    quantities.value[index] = max;
  }
};

// Thêm sản phẩm vào hóa đơn
const addSelectedProducts = async () => {
  const selectedProducts = store.listCTSP_HD
    .map((item, index) => ({
      idCTSP: item.id_chi_tiet_san_pham,
      soLuongMua: quantities.value[index]
    }))
    .filter(product => product.soLuongMua > 0);

  if (selectedProducts.length === 0) {
    toast.error('Vui lòng chọn ít nhất một sản phẩm để thêm!');
    return;
  }

  // Chỉ validate số lượng khả dụng nếu trạng thái là "Chờ xác nhận"
  for (const product of selectedProducts) {
    const ctsp = store.listCTSP_HD.find(item => item.id_chi_tiet_san_pham === product.idCTSP);
    const maxQuantity = shouldCalculateSoLuongTon.value ? ctsp.so_luong : calculateSoLuongTon(ctsp);
    if (product.soLuongMua > maxQuantity) {
      toast.error(`Số lượng sản phẩm ${ctsp.ten_san_pham} vượt quá số lượng tối đa (${maxQuantity})`);
      return;
    }
  }
  // Gửi yêu cầu thêm sản phẩm vào hóa đơn
  const response = await store.addProductsToInvoice(store.hoaDonDetail.ma_hoa_don, selectedProducts);

  // ✅ NEW: Xử lý kết quả từ backend
  if (response && response.success) {
    // Hiển thị kết quả xử lý nếu có thay đổi
    if (response.hasPriceConflict || response.mergedProducts > 0 || response.newProducts > 0) {
      showProcessingResult({
        mergedProducts: response.mergedProducts,
        newProducts: response.newProducts,
        hasPriceConflict: response.hasPriceConflict,
        phuThuApplied: response.phuThuApplied
      });
    } else {
      toast.success('Thêm sản phẩm thành công!');
    }

    // Gọi lại API để cập nhật danh sách sản phẩm
    await store.getAllCTSP_HD(0, 100, searchKeyword.value);

    // Reset số lượng và đóng popup
    quantities.value = new Array(store.listCTSP_HD.length).fill(0);

    // Tính phí vận chuyển
    const phiVanChuyen = await calculatePhiVanChuyen();
    console.log('Phí vận chuyển tính được khi thêm sản phẩm:', phiVanChuyen);
    await hoaDonService.updatePhiShip(store.hoaDonDetail.ma_hoa_don, phiVanChuyen);
    await store.getHoaDonDetail(store.hoaDonDetail.ma_hoa_don);
    closeAddProductPopup();
  } else {
    toast.error(response.message || 'Thêm sản phẩm thất bại!');
  }
};

// Xóa sản phẩm khỏi hóa đơn
const removeProduct = async (item, index) => {
  if (cannotDecreaseOrRemoveProduct.value) {
    toast.error('Không thể xóa sản phẩm vì hóa đơn này đã thanh toán!');
    return;
  }
  // Validate: Không cho phép xóa nếu chỉ còn 1 sản phẩm trong hóa đơn
  if (store.chiTietHoaDons.length === 1) {
    toast.error('Hóa đơn phải có tối thiểu 1 sản phẩm!');
    return;
  }
  // Hiển thị modal xác nhận xóa
  AModal.confirm({
    title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
      h(DeleteOutlined, { style: 'color: #ff4d4f; font-size: 22px;' }),
      h('span', { style: 'font-size: 16px; font-weight: 600;' }, 'Xóa sản phẩm')
    ]),
    content: () => h('div', { style: 'padding: 8px 0;' }, [
      h('p', { style: 'margin: 0 0 12px 0; font-size: 14px;' }, `Bạn có chắc chắn muốn xóa "${item.ten_san_pham}" không?`),
      h('div', { style: 'background: #fff1f0; padding: 12px; border-radius: 6px; border: 1px solid #ffccc7;' }, [
        h('div', { style: 'display: flex; align-items: center; gap: 8px; color: #cf1322;' }, [
          h(ExclamationCircleOutlined, { style: 'font-size: 14px;' }),
          h('span', { style: 'font-size: 13px;' }, 'Phí vận chuyển có thể thay đổi')
        ])
      ])
    ]),
    okText: 'Xóa',
    cancelText: 'Hủy',
    okButtonProps: { danger: true, size: 'large', style: { height: '38px' } },
    cancelButtonProps: { size: 'large', style: { height: '38px' } },
    centered: true,
    width: 450,
    onOk: async () => {
      try {
        const response = await store.removeProductFromInvoice(
          store.hoaDonDetail.ma_hoa_don,
          item.id_chi_tiet_san_pham
        );
        if (response.error) {
          toast.error('Xóa sản phẩm khỏi hóa đơn thất bại');
          return;
        }
        // Xóa sản phẩm khỏi danh sách
        store.chiTietHoaDons.splice(index, 1);
        await store.getHoaDonDetail(store.hoaDonDetail.ma_hoa_don);
        toast.success('Xóa sản phẩm khỏi hóa đơn thành công');
        // Tính phí vận chuyển
        const phiVanChuyen = await calculatePhiVanChuyen();
        console.log('Phí vận chuyển tính được khi xóa sản phẩm:', phiVanChuyen);
        await hoaDonService.updatePhiShip(store.hoaDonDetail.ma_hoa_don, phiVanChuyen);
        await store.getHoaDonDetail(store.hoaDonDetail.ma_hoa_don);
      } catch (error) {
        console.error('Lỗi khi xóa sản phẩm:', error);
        toast.error('Có lỗi xảy ra khi xóa sản phẩm');
      }
    },
  });
};

const showStatusModal = ref(false);
const modalTitle = ref('');
const modalAction = ref(''); // 'change', 'revert', hoặc 'cancel'
const statusForm = ref({
  noiDungDoi: ''
});

// Danh sách gợi ý cho noi_dung_doi
const noiDungDoiOptions = [
  'Xác nhận đơn hàng',
  'Chuẩn bị hàng',
  'Giao cho đơn vị vận chuyển',
  'Hoàn thành đơn hàng',
  'Hủy đơn hàng',
  'Quay lại trạng thái ban đầu'
];

// Hàm mở modal
const openStatusModal = (action) => {
  modalAction.value = action;
  if (action === 'change') {
    modalTitle.value = `Xác nhận chuyển trạng thái thành "${nextStatusValue.value}"`;
  } else if (action === 'revert') {
    modalTitle.value = 'Xác nhận quay lại trạng thái "Chờ xác nhận"';
  } else if (action === 'cancel') {
    if (cannotDecreaseOrRemoveProduct.value) {
      toast.error('Không thể hủy đơn hàng vì hóa đơn đã thanh toán!');
      return;
    }
    modalTitle.value = 'Xác nhận hủy đơn hàng';
  }

  statusForm.value.noiDungDoi = ''; // Reset nội dung

  showStatusModal.value = true;
};

// Hàm đóng modal
const closeStatusModal = () => {
  showStatusModal.value = false;
  modalAction.value = '';
  modalTitle.value = '';
  statusForm.value = { noiDungDoi: '' };
};
// Hàm xác nhận thay đổi trạng thái
const confirmStatusChange = () => {
  if (!statusForm.value.noiDungDoi || statusForm.value.noiDungDoi.trim() === '') {
    statusForm.value.noiDungDoi = 'Không có ghi chú';
  }

  if (modalAction.value === 'change') {
    store.changeTrangThaiHoaDon(
      store.hoaDonDetail.ma_hoa_don,
      nextStatusValue.value,
      '',
      statusForm.value.noiDungDoi
    );
  } else if (modalAction.value === 'revert') {
    store.revertToInitialStatus(
      store.hoaDonDetail.ma_hoa_don,
      '',
      statusForm.value.noiDungDoi
    );
  } else if (modalAction.value === 'cancel') {
    store.cancelHoaDon(
      store.hoaDonDetail.ma_hoa_don,
      '',
      statusForm.value.noiDungDoi
    );
  }

  closeStatusModal();
};
// Thêm vào phần đầu của <script setup>
const showStatusHistoryDrawer = ref(false);
const reverseTimeline = ref(true);

// Computed property để kiểm tra trạng thái pending
const isPending = computed(() => {
  const currentStatus = store.hoaDonDetail?.trang_thai;
  return currentStatus !== 'Hoàn thành' && currentStatus !== 'Đã hủy';
});

// Hàm mở drawer
const openStatusHistoryDrawer = () => {
  showStatusHistoryDrawer.value = true;
};
// Hàm đảo ngược thứ tự timeline
const toggleReverseTimeline = () => {
  reverseTimeline.value = !reverseTimeline.value;
};

// Hàm lấy màu cho timeline item dựa trên trạng thái
const getTimelineColor = (trangThai) => {
  switch (trangThai) {
    case 'Chờ xác nhận':
      return 'blue';
    case 'Đã xác nhận':
      return 'green';
    case 'Chờ đóng gói':
      return 'orange';
    case 'Đang giao':
      return 'purple';
    case 'Hoàn thành':
      return 'green';
    case 'Đã hủy':
      return 'red';
    case 'Đã cập nhật':
      return 'gray';
    default:
      return 'blue';
  }
};
// Trạng thái popup chỉnh sửa số lượng
const showQuantityPopup = ref(false);
const popupType = ref('');
const currentIndex = ref(null);
const quantityChange = ref(0);
const currentProduct = ref(null);

// Hiển thị popup tăng số lượng
const showIncreasePopup = async (index) => {
  currentIndex.value = index;
  popupType.value = 'increase';
  quantityChange.value = 0;
  currentProduct.value = store.chiTietHoaDons[index];
  console.log('currentProduct:', store.chiTietHoaDons[index]);

  if (!store.listCTSP_HD || store.listCTSP_HD.length === 0) {
    await store.getAllCTSP_HD(0, 5, '');
  }

  // ✅ KIỂM TRA SỐ LƯỢNG CÒN LẠI THỰC TẾ (không chỉ kiểm tra null)
  // Nếu trạng thái "Chờ xác nhận" -> dùng so_luong_ton_kho
  // Các trạng thái khác -> tính số lượng còn lại = tồn kho - đã đặt
  const soLuongConLai = shouldCalculateSoLuongTon.value
    ? (currentProduct.value.so_luong_ton_kho || 0)
    : calculateSoLuongTon(currentProduct.value);

  // Kiểm tra sản phẩm hết hàng hoặc bị tắt
  if (soLuongConLai <= 0 || currentProduct.value.so_luong_ton_kho === null || !currentProduct.value.trang_thai_ctsp) {
    message.error('Sản phẩm này đã hết hàng hoặc đã ngừng bán. Không thể thêm sản phẩm nữa!')
    return;
  }

  showQuantityPopup.value = true;
};

// Hiển thị popup giảm số lượng
const showDecreasePopup = async (index) => {
  if (cannotDecreaseOrRemoveProduct.value) {
    toast.error('Không thể giảm số lượng sản phẩm vì hóa đơn này đã thanh toán!');
    return;
  }
  currentIndex.value = index;
  popupType.value = 'decrease';
  quantityChange.value = 0;
  currentProduct.value = store.chiTietHoaDons[index];

  if (!store.listCTSP_HD || store.listCTSP_HD.length === 0) {
    await store.getAllCTSP_HD(0, 5, '');
  }

  showQuantityPopup.value = true;
};

// Đóng popup chỉnh sửa số lượng
const closeQuantityPopup = () => {
  showQuantityPopup.value = false;
  currentIndex.value = null;
  popupType.value = '';
  quantityChange.value = 0;
};

const updateProductQuantityInInvoice = async (item, newQuantity) => {
  try {
    const response = await store.updateProductQuantity(
      store.hoaDonDetail.ma_hoa_don,
      item.id_chi_tiet_san_pham,
      newQuantity - item.so_luong // Chênh lệch số lượng
    );

    if (response.error) {
      toast.error(`Cập nhật số lượng sản phẩm "${item.ten_san_pham}" thất bại`);
      return;
    }

    toast.success(
      `Số lượng sản phẩm "${item.ten_san_pham}" đã được cập nhật thành ${newQuantity}`
    );

    // Cập nhật lại chi tiết hóa đơn
    await store.getHoaDonDetail(store.hoaDonDetail.ma_hoa_don);
  } catch (error) {
    console.error('Lỗi khi cập nhật số lượng sản phẩm:', error);
    toast.error('Có lỗi xảy ra khi cập nhật số lượng sản phẩm');
  }
};
const updateQuantity = async () => {
  const index = currentIndex.value;
  const item = store.chiTietHoaDons[index];
  const change = quantityChange.value;

  if (change <= 0) {
    message.error('Vui lòng nhập số lượng, tối thiểu là 1');
    return;
  }

  if (popupType.value === 'increase') {
    // Tính số lượng tối đa
    const maxQuantity = shouldCalculateSoLuongTon.value ? item.so_luong_ton_kho : calculateSoLuongTon(item);
    if (change > maxQuantity) {
      toast.error(`Số lượng thêm không được vượt quá ${maxQuantity}`);
      return;
    }

    try {
      const response = await store.updateProductQuantity(
        store.hoaDonDetail.ma_hoa_don,
        item.id_chi_tiet_san_pham,
        change
      );

      if (response.error) {
        toast.error('Cập nhật số lượng thất bại');
        return;
      }

      await store.getHoaDonDetail(store.hoaDonDetail.ma_hoa_don);
      toast.success(`Đã thêm ${change} sản phẩm thành công`);
      // Tính phí vận chuyển
      const phiVanChuyen = await calculatePhiVanChuyen();
      console.log('Phí vận chuyển tính được khi + số lượng:', phiVanChuyen);
      await hoaDonService.updatePhiShip(store.hoaDonDetail.ma_hoa_don, phiVanChuyen);
      await store.getHoaDonDetail(store.hoaDonDetail.ma_hoa_don);
    } catch (error) {
      console.error('Lỗi khi thêm số lượng:', error);
      toast.error('Có lỗi xảy ra khi thêm số lượng');
    }
  } else if (popupType.value === 'decrease') {
    const totalQuantity = calculateTotalQuantity();
    const newQuantity = totalQuantity - change;
    if (newQuantity < 1) {
      toast.error('Hóa đơn phải có tối thiểu 1 sản phẩm với số lượng tối thiểu là 1!');
      return;
    }
    if (change >= item.so_luong) {
      toast.error(`Số lượng giảm phải nhỏ hơn ${item.so_luong} hoặc hãy xóa sản phẩm khỏi hóa đơn!`);
      return;
    }
    try {
      const response = await store.updateProductQuantity(
        store.hoaDonDetail.ma_hoa_don,
        item.id_chi_tiet_san_pham,
        -change
      );
      if (response.error) {
        toast.error('Cập nhật số lượng thất bại');
        return;
      }
      await store.getHoaDonDetail(store.hoaDonDetail.ma_hoa_don);
      toast.success(`Đã giảm ${change} sản phẩm thành công`);
      // Tính phí vận chuyển
      const phiVanChuyen = await calculatePhiVanChuyen();
      console.log('Phí vận chuyển tính được khi - số lượng:', phiVanChuyen);
      await hoaDonService.updatePhiShip(store.hoaDonDetail.ma_hoa_don, phiVanChuyen);
      await store.getHoaDonDetail(store.hoaDonDetail.ma_hoa_don);
    } catch (error) {
      console.error('Lỗi khi giảm số lượng:', error);
      toast.error('Có lỗi xảy ra khi giảm số lượng');
    }
  }

  closeQuantityPopup();
};

// Computed property để kiểm tra điều kiện hiển thị nút "Quay lại"
const showRevertButton = computed(() => {
  const currentStatus = store.hoaDonDetail?.trang_thai;

  // Nếu trạng thái hiện tại là "Đã cập nhật", kiểm tra trạng thái trước đó
  if (currentStatus === 'Đã cập nhật') {
    const previousStatus = store.trangThaiHistory
      .filter(history => history.trang_thai !== 'Đã cập nhật') // Bỏ qua các trạng thái "Đã cập nhật"
      .sort((a, b) => new Date(b.ngay_chuyen) - new Date(a.ngay_chuyen))[0]?.trang_thai; // Lấy trạng thái trước đó

    return previousStatus === 'Đã xác nhận'; // Hiển thị nút nếu trạng thái trước đó là "Đã xác nhận"
  }

  // Nếu trạng thái hiện tại không phải "Đã cập nhật", kiểm tra trực tiếp
  return currentStatus === 'Đã xác nhận';
});

// Trạng thái modal xác nhận in hóa đơn
const showPrintConfirm = ref(false);

// ✅ NEW: Modal xác nhận thay đổi giá
const showPriceChangeModal = ref(false);
const priceChangeInfo = ref({
  productName: '',
  oldPrice: 0,
  newPrice: 0,
  idCTSP: 0
});

// ✅ NEW: Modal kết quả xử lý
const showProcessingResultModal = ref(false);
const processingResult = ref({
  mergedProducts: 0,
  newProducts: 0,
  hasPriceConflict: false,
  phuThuApplied: false
});

// Hàm mở modal xác nhận in
const openPrintConfirm = () => {
  showPrintConfirm.value = true;
};

// Hàm xử lý xác nhận in
const confirmPrint = (shouldPrint) => {
  showPrintConfirm.value = false;
  if (shouldPrint) {
    printInvoice();
    toast.success('Xuất hóa đơn thành công!');
    return;
  }
  toast.warning('Xuất hóa đơn không thành công!');
};
const printInvoice = async () => {
  const doc = new jsPDF();
  doc.setFont("Roboto");
  const logoWidth = 60;
  const logoHeight = 20;
  const pageWidth = doc.internal.pageSize.getWidth();
  const logoX = (pageWidth - logoWidth) / 2;
  doc.addImage(logo, 'PNG', logoX, 15, logoWidth, logoHeight); // Logo gần chữ hơn
  const qrCodeDataUrl = await QRCode.toDataURL(store.hoaDonDetail.ma_hoa_don || 'N/A');
  doc.addImage(qrCodeDataUrl, 'PNG', 15, 10, 40, 40); // QR code gần chữ hơn
  doc.setFontSize(18);
  doc.setFont("Roboto", "bold");
  doc.text("HÓA ĐƠN BÁN HÀNG", 105, 45, { align: "center" }); // Chỉnh vị trí chữ "Hóa đơn bán hàng"
  // Thông tin cửa hàng
  doc.setFontSize(16);
  doc.setFont("Roboto", "bold");
  doc.text("Cửa hàng MenWear", 105, 55, { align: "center" });
  doc.setFontSize(10);
  doc.setFont("Roboto", "normal");
  doc.text("Địa chỉ: Phương Canh, Nam Từ Liêm, Hà Nội", 105, 63, { align: "center" });
  doc.text("Điện thoại: 0397572262", 105, 69, { align: "center" });
  // Vẽ đường kẻ ngang
  doc.setLineWidth(0.5);
  doc.line(20, 73, 190, 73);
  // Thông tin hóa đơn
  let y = 120;
  doc.setFontSize(12);
  doc.setFont("Roboto", "normal");
  doc.text(`Mã hóa đơn: ${store.hoaDonDetail.ma_hoa_don || 'N/A'}`, 20, 86);
  doc.text(`Tên nhân viên: ${store.hoaDonDetail.ten_nhan_vien || 'N/A'}`, 20, 94);
  doc.text(`Ngày: ${formatDateTime(store.hoaDonDetail.ngay_tao)}`, 20, 102);
  doc.text(`Tên khách hàng: ${store.hoaDonDetail.ho_ten || 'Khách lẻ'}`, 20, 110);
  // Kiểm tra nếu là đơn Online/Offline và giao hàng thì hiển thị thêm số điện thoại và địa chỉ
  if (store.hoaDonDetail.loai_hoa_don === 'Online' || store.hoaDonDetail.loai_hoa_don === 'Offline' && store.hoaDonDetail.phuong_thuc_nhan_hang === 'Giao hàng') {
    doc.text(`SĐT: ${store.hoaDonDetail.sdt_nguoi_nhan || ''}`, 110, 110, { align: "left" });
    doc.text(`Địa chỉ: ${store.hoaDonDetail.dia_chi || ''}`, 20, 118);
    y = 126; // cập nhật vị trí `y` sau địa chỉ
  } else {
    y = 118; // nếu không có địa chỉ, dòng sản phẩm bắt đầu ngay sau tên khách hàng
  }
  // Danh sách sản phẩm
  doc.setFontSize(12);
  doc.setFont("Roboto", "bold");
  doc.text("Thông tin sản phẩm", 20, y);
  // Tiêu đề bảng
  // y += 10;
  doc.setFontSize(10);
  doc.setFont("Roboto", "bold");
  doc.text("Số lượng", 110, y, { align: "center" });
  doc.text("Đơn giá", 140, y, { align: "center" });
  doc.text("Tổng tiền", 180, y, { align: "center" });
  // Vẽ đường kẻ ngang dưới tiêu đề bảng
  y += 2;
  doc.setLineWidth(0.2);
  doc.line(20, y, 190, y);
  // Danh sách sản phẩm
  y += 6;
  doc.setFontSize(10);
  doc.setFont("Roboto", "normal");
  store.chiTietHoaDons.forEach((item, index) => {
    // Tên sản phẩm
    const productName = `${index + 1}. ${item.ten_san_pham}`;
    const colorSize = `(Màu: ${item.ten_mau_sac} - Size: ${item.kich_thuoc})`;

    const productLines = doc.splitTextToSize(productName, 80);
    const colorSizeLines = doc.splitTextToSize(colorSize, 80);

    doc.text(productLines, 20, y);
    y += productLines.length * 6; // tăng dòng nếu tên sản phẩm dài

    doc.setFontSize(9);
    doc.setTextColor(100); // màu xám nhẹ cho dòng (Màu - Size)
    doc.text(colorSizeLines, 20, y);
    doc.setFontSize(10);
    doc.setTextColor(0); // Reset màu và size về bình thường
    doc.text(`${item.so_luong}`, 110, y, { align: "center" });

    // Đơn giá
    const donGia = item.gia_sau_km && item.gia_sau_km < item.gia_goc ? item.gia_sau_km : item.gia_goc;
    if (item.gia_sau_giam && item.gia_sau_giam < item.gia_ban) {
      doc.setTextColor(255, 0, 0); // Màu đỏ
    }
    doc.text(`${formatCurrency(donGia)} VNĐ`, 140, y, { align: "center" });
    doc.setTextColor(0); // Reset màu về đen

    // Thành tiền
    if (item.gia_sau_giam && item.gia_sau_giam < item.gia_ban) {
      doc.setTextColor(255, 0, 0); // Màu đỏ
    }
    doc.text(`${formatCurrency(item.don_gia)} VNĐ`, 180, y, { align: "center" });
    doc.setTextColor(0); // Reset màu về đen

    y += productLines.length * 6 + 4;

    // Hiển thị giá gốc nếu có khuyến mãi
    if (item.gia_sau_km && item.gia_sau_km < item.gia_goc) {
      doc.setFontSize(8);
      doc.setTextColor(150); // Màu xám
      doc.text(`Giá gốc: ${formatCurrency(item.gia_goc)} VNĐ`, 140, y - 6, { align: "center" });
      doc.setTextColor(0); // Reset màu về đen
      doc.setFontSize(10);
      y += 4;
    }
  });
  // Vẽ đường kẻ ngang sau danh sách sản phẩm
  doc.setLineWidth(0.2);
  doc.line(20, y, 190, y);
  // Tổng tiền
  y += 10;
  if (store.hoaDonDetail.trang_thai?.toLowerCase() !== 'trả hàng') {
    doc.setFontSize(12);
    doc.setFont("Roboto", "normal");
    doc.text(`Tổng tiền hàng:`, 115, y, { align: "left" });
    doc.text(`${formatCurrency(store.hoaDonDetail.tong_tien_truoc_giam)} VNĐ`, 190, y, { align: "right" });

    y += 6;
    const giamGia = (store.hoaDonDetail.tong_tien_truoc_giam || 0) -
      (store.hoaDonDetail.tong_tien_sau_giam || 0);
    doc.text(`Giảm giá:`, 115, y, { align: "left" });
    doc.text(`-${formatCurrency(giamGia)} VNĐ`, 190, y, { align: "right" });

    if (store.hoaDonDetail.phuong_thuc_nhan_hang === 'Giao hàng') {
      y += 6;
      doc.text(`Phí vận chuyển:`, 115, y, { align: "left" });
      doc.text(`+${formatCurrency(store.hoaDonDetail.phi_van_chuyen || 0)} VNĐ`, 190, y, { align: "right" });
    }

    y += 6;
    doc.setFont("Roboto", "bold");
    doc.text(`Thành tiền:`, 115, y, { align: "left" });
    doc.text(`${formatCurrency(store.hoaDonDetail.tong_tien_sau_giam + store.hoaDonDetail.phi_van_chuyen)} VNĐ`, 190, y, { align: "right" });
    // Thêm dòng Vui lòng thanh toán thêm
    if (store.hoaDonDetail?.loai_hoa_don === 'Online' && store.hoaDonDetail?.hinh_thuc_thanh_toan === 'Chuyển khoản' && store.chiTietHoaDons[0]?.phu_thu > 0
      || store.hoaDonDetail?.loai_hoa_don === 'Offline' && store.hoaDonDetail?.phuong_thuc_nhan_hang === 'Giao hàng' && store.chiTietHoaDons[0]?.phu_thu > 0) {
      y += 6;
      doc.setFont("Roboto", "normal");
      doc.setTextColor(255, 0, 0);
      doc.text(`Vui lòng thanh toán thêm:`, 115, y, { align: "left" });
      doc.text(`${formatCurrency(store.chiTietHoaDons[0]?.phu_thu)} VNĐ`, 190, y, { align: "right" });
      doc.setTextColor(0);
    }
  }
  // Chân trang
  // Nếu trạng thái là "Trả hàng", thêm danh sách sản phẩm hoàn trả
  if (store.hoaDonDetail.trang_thai?.toLowerCase() === 'trả hàng' && store.chiTietTraHangs.length > 0) {
    // Vẽ đường kẻ ngang trước phần sản phẩm hoàn trả
    doc.setLineWidth(0.2);
    doc.line(20, y, 190, y);

    // Tiêu đề phần hoàn trả
    y += 6;
    doc.setFontSize(12);
    doc.setFont("Roboto", "bold");
    doc.text("Thông tin sản phẩm hoàn trả", 20, y);

    // Header bảng
    // y += 8;
    doc.setFontSize(10);
    doc.setFont("Roboto", "bold");
    doc.text("Số lượng", 110, y, { align: "center" });
    doc.text("Đơn giá", 140, y, { align: "center" });
    doc.text("Tổng tiền hoàn", 180, y, { align: "center" });

    // Đường kẻ ngang dưới tiêu đề
    y += 2;
    doc.setLineWidth(0.2);
    doc.line(20, y, 190, y);

    // Danh sách sản phẩm hoàn trả
    y += 6;
    doc.setFontSize(10);
    doc.setFont("Roboto", "normal");
    store.chiTietTraHangs.forEach((item, index) => {
      // Tên sản phẩm
      const productName = `${index + 1}. ${item.ten_san_pham}`;
      const colorSize = `(Màu: ${item.ten_mau_sac} - Size: ${item.kich_thuoc})`;

      const productLines = doc.splitTextToSize(productName, 80);
      const colorSizeLines = doc.splitTextToSize(colorSize, 80);

      doc.text(productLines, 20, y);
      y += productLines.length * 6; // tăng dòng nếu tên sản phẩm dài

      doc.setFontSize(9);
      doc.setTextColor(100); // màu xám nhẹ cho dòng (Màu - Size)
      doc.text(colorSizeLines, 20, y);
      doc.setFontSize(10);
      doc.setTextColor(0); // Reset màu và size về bình thường

      // Số lượng
      doc.text(`${item.so_luong}`, 110, y - (colorSizeLines.length - 1) * 6, { align: "center" });
      // Đơn giá
      doc.text(`${formatCurrency(item.don_gia)} VNĐ`, 140, y - (colorSizeLines.length - 1) * 6, { align: "center" });
      // Tổng tiền hoàn
      doc.text(`${formatCurrency(item.tien_hoan)} VNĐ`, 180, y - (colorSizeLines.length - 1) * 6, { align: "center" });

      y += colorSizeLines.length * 6 + 4; // sau khi xong màu size thì nhảy dòng thêm
    });

    // Đường kẻ ngang sau danh sách
    doc.setLineWidth(0.2);
    doc.line(20, y, 190, y);

    // Tổng tiền hoàn trả
    y += 10;
    doc.setFontSize(12);
    doc.setFont("Roboto", "normal");
    doc.text(`Tổng tiền hàng:`, 90, y);
    doc.text(`${formatCurrency(store.hoaDonDetail.tong_tien_truoc_giam)} VNĐ`, 190, y, { align: "right" });

    y += 6;
    const giamGia = (store.hoaDonDetail.tong_tien_truoc_giam || 0) + (store.hoaDonDetail.phi_van_chuyen || 0) - (store.hoaDonDetail.tong_tien_sau_giam || 0);
    doc.text(`Giảm giá:`, 90, y);
    doc.text(`-${formatCurrency(giamGia)} VNĐ`, 190, y, { align: "right" });

    y += 6;
    doc.text(`Phí vận chuyển:`, 90, y);
    doc.text(`${formatCurrency(store.hoaDonDetail.phi_van_chuyen || 0)} VNĐ`, 190, y, { align: "right" });

    y += 6;
    doc.text(`Tổng tiền khách đã thanh toán:`, 90, y);
    doc.text(`${formatCurrency(store.hoaDonDetail.tong_tien_sau_giam)} VNĐ`, 190, y, { align: "right" });

    y += 6;
    const tongTienHoan = store.traHangs.reduce((total, traHang) => total + traHang.tong_tien_hoan, 0);
    doc.text(`Tổng tiền hoàn trả lại khách:`, 90, y);
    doc.text(`${formatCurrency(tongTienHoan)} VNĐ`, 190, y, { align: "right" });

    y += 6;
    doc.setFont("Roboto", "bold");
    const thanhTien = (store.hoaDonDetail.tong_tien_sau_giam || 0) - tongTienHoan;
    doc.text(`Thành tiền:`, 90, y);
    doc.text(`${formatCurrency(thanhTien)} VNĐ`, 190, y, { align: "right" });
  }
  y += 10;
  doc.setFontSize(10);
  doc.setFont("Roboto", "normal");
  if (store.hoaDonDetail.trang_thai === 'Đã hủy') {
    doc.setTextColor(255, 0, 0); // Màu đỏ
    doc.text("Hóa đơn đã hủy!", 105, y, { align: "center" });
  } else {
    doc.text("Cảm ơn Quý Khách, hẹn gặp lại!", 105, y, { align: "center" });
  }
  // Lưu file PDF
  doc.save(`HoaDon_${store.hoaDonDetail.ma_hoa_don}.pdf`);
};
const validateProductsInInvoice = async () => {
  const invalidProducts = [];
  // Lấy trạng thái hiện tại
  const currentStatus = store.hoaDonDetail?.trang_thai;

  // Nếu trạng thái là các trạng thái đã chốt đơn, bỏ qua kiểm tra
  if (['Chờ xác nhận', 'Đã cập nhật', 'Đã xác nhận', 'Chờ đóng gói', 'Đang giao', 'Hoàn thành', 'Đã hủy', 'Trả hàng'].includes(currentStatus)) {
    return;
  }

  for (const item of store.chiTietHoaDons) {
    const productInStore = store.listCTSP_HD.find(
      (product) => product.id_chi_tiet_san_pham === item.id_chi_tiet_san_pham
    );

    if (!productInStore || !productInStore.trang_thai || productInStore.trang_thai !== 'Hoạt động') {
      // Sản phẩm ngừng hoạt động
      invalidProducts.push({
        ...item,
        reason: 'Sản phẩm đã ngừng hoạt động',
      });

      // Gọi hàm xóa sản phẩm
      await removeProduct(item, store.chiTietHoaDons.indexOf(item));
    } else if (productInStore.so_luong < item.so_luong) {
      if (productInStore.so_luong === 0) {
        // Sản phẩm hết hàng trong kho
        invalidProducts.push({
          ...item,
          reason: 'Sản phẩm đã hết hàng trong kho',
        });

        // Gọi hàm xóa sản phẩm
        await removeProduct(item, store.chiTietHoaDons.indexOf(item));
      } else {
        // Số lượng tồn kho ít hơn số lượng trong hóa đơn
        invalidProducts.push({
          ...item,
          reason: `Số lượng tồn kho không đủ (Tồn kho: ${productInStore.so_luong})`,
        });

        // Gọi hàm cập nhật số lượng
        await updateProductQuantityInInvoice(item, productInStore.so_luong);
      }
    }
  }

  if (invalidProducts.length > 0) {
    invalidProducts.forEach((product) => {
      toast.error(
        `Sản phẩm "${product.ten_san_pham}" gặp vấn đề: ${product.reason}`
      );
    });
  }
};
const getPreviousStatus = () => {
  if (!store.trangThaiHistory || store.trangThaiHistory.length === 0) return null;

  // Lọc bỏ các trạng thái "Đã cập nhật" và sắp xếp theo thời gian
  const filteredHistory = store.trangThaiHistory
    .filter(status => status.trang_thai !== 'Đã cập nhật')
    .sort((a, b) => new Date(b.ngay_chuyen) - new Date(a.ngay_chuyen));

  // Lấy trạng thái gần nhất
  return filteredHistory.length > 0 ? filteredHistory[0].trang_thai : null;
};
// Hàm tách địa chỉ
const tachDiaChi = (diaChi) => {
  if (!diaChi) return null;
  const parts = diaChi.split(', ').map(part => part.trim());
  if (parts.length < 4) return null;
  return {
    tinh: parts[parts.length - 1],
    huyen: parts[parts.length - 2],
    xa: parts[parts.length - 3],
    diaChiCuThe: parts.slice(0, parts.length - 3).join(', '),
  };
};
// ✅ Helper: Tính phí ship fallback khi GHTK API lỗi
const calculateFallbackShippingFee = (province) => {
  // Danh sách tỉnh/thành miền Bắc (30,000 VNĐ)
  const mienBac = ['Hà Nội', 'Hải Phòng', 'Hải Dương', 'Hưng Yên', 'Bắc Ninh',
    'Vĩnh Phúc', 'Thái Nguyên', 'Phú Thọ', 'Bắc Giang', 'Quảng Ninh',
    'Lạng Sơn', 'Cao Bằng', 'Bắc Kạn', 'Ninh Bình', 'Nam Định'];

  // Danh sách tỉnh miền Trung (50,000 VNĐ)
  const mienTrung = ['Thanh Hóa', 'Nghệ An', 'Hà Tĩnh', 'Quảng Bình', 'Quảng Trị',
    'Thừa Thiên Huế', 'Đà Nẵng', 'Quảng Nam', 'Quảng Ngãi',
    'Bình Định', 'Phú Yên', 'Khánh Hòa'];

  const cleanProvince = province.replace(/^(Tỉnh|Thành phố)\s+/i, '').trim();

  if (cleanProvince === 'Hà Nội') return 0; // Miễn phí nội thành Hà Nội
  if (mienBac.includes(cleanProvince)) return 30000; // Miền Bắc
  if (mienTrung.includes(cleanProvince)) return 50000; // Miền Trung
  return 70000; // Miền Nam (mặc định)
};

// Hàm tính phí vận chuyển
const calculatePhiVanChuyen = async (useEditedCustomer = false) => {
  // 🔍 DEBUG: Log giá trị tổng tiền trước giảm
  console.log('🔍 [calculatePhiVanChuyen] Checking shipping fee...');
  console.log('  - tong_tien_truoc_giam:', store.hoaDonDetail.tong_tien_truoc_giam);
  console.log('  - Ngưỡng miễn phí ship: 2,000,000 VNĐ');

  if (store.hoaDonDetail.tong_tien_truoc_giam >= 2000000) {
    console.log('  ✅ MIỄN PHÍ SHIP (>= 2 triệu)');
    return 0;
  }

  console.log('  💰 CÓ PHÍ SHIP (< 2 triệu) - Đang tính toán...');

  const weight = 500; // 500g mỗi sản phẩm
  const tongTienHoaDon = store.hoaDonDetail.tong_tien_sau_giam - store.hoaDonDetail.phi_van_chuyen;

  // Lấy thông tin địa chỉ
  let diaChi;
  if (useEditedCustomer) {
    diaChi = editedCustomer.value;
  } else {
    diaChi = tachDiaChi(store.hoaDonDetail.dia_chi);
  }

  if (!diaChi || !diaChi.tinh || !diaChi.huyen) {
    console.warn('⚠️ Không có địa chỉ giao hàng hợp lệ, phí = 0');
    return 0;
  }

  try {
    const tinhThanhPho = diaChi.tinh || '';
    const quanHuyen = diaChi.huyen || '';

    // ✅ Loại bỏ tiền tố trước khi gửi đến GHTK API
    const cleanProvince = tinhThanhPho.replace(/^(Tỉnh|Thành phố)\s+/i, '').trim();
    const cleanDistrict = quanHuyen.replace(/^(Quận|Huyện|Thị xã|Thành phố)\s+/i, '').trim();

    console.log('📍 Địa chỉ:', cleanProvince, cleanDistrict);
    console.log('📦 Dữ liệu:', { weight, value: tongTienHoaDon });

    const response = await banHangService.tinhPhiShip(
      'Hà Nội',
      'Nam Từ Liêm',
      cleanProvince,
      cleanDistrict,
      weight,
      Math.round(tongTienHoaDon)
    );

    // ✅ Kiểm tra response có lỗi không
    if (response && response.error) {
      console.warn('⚠️ GHTK API lỗi, dùng phí ship fallback');
      const fallbackFee = calculateFallbackShippingFee(tinhThanhPho);
      console.log('  💰 Phí ship fallback:', fallbackFee, 'VNĐ');
      toast.warning(`Không thể kết nối GHTK API. Sử dụng phí ship tạm thời: ${fallbackFee.toLocaleString()} VNĐ`);
      return fallbackFee;
    }

    // ✅ Response hợp lệ
    const fee = response.fee || response.ship_fee_only || response;
    console.log('  ✅ Phí ship từ GHTK:', fee, 'VNĐ');
    return fee || 0;

  } catch (error) {
    console.error('❌ Lỗi khi tính phí vận chuyển:', error);

    // ✅ FALLBACK: Dùng phí ship cố định khi API lỗi
    const fallbackFee = calculateFallbackShippingFee(diaChi.tinh);
    console.log('  💰 Phí ship fallback (API error):', fallbackFee, 'VNĐ');
    toast.warning(`Lỗi kết nối GHTK. Sử dụng phí ship tạm thời: ${fallbackFee.toLocaleString()} VNĐ`);
    return fallbackFee;
  }
};
// Computed property để lọc và sắp xếp lịch sử trạng thái (loại bỏ "Đã cập nhật")
const filteredTrangThaiHistory = computed(() => {
  if (!store.trangThaiHistory || store.trangThaiHistory.length === 0) return [];

  // Lọc bỏ trạng thái "Đã cập nhật" và sắp xếp theo thời gian tăng dần
  return store.trangThaiHistory
    .filter(history => history.trang_thai !== 'Đã cập nhật')
    .sort((a, b) => new Date(a.ngay_chuyen) - new Date(b.ngay_chuyen));
});
// Hàm lấy icon tương ứng với trạng thái
const getIconForStatus = (status) => {
  switch (status) {
    case 'Chờ xác nhận':
      return 'fas fa-hourglass-start';
    case 'Đã xác nhận':
      return 'fas fa-check-circle';
    case 'Chờ đóng gói':
      return 'fas fa-box';
    case 'Đang giao':
      return 'fas fa-truck';
    case 'Hoàn thành':
      return 'fas fa-flag-checkered';
    case 'Đã hủy':
      return 'fas fa-times-circle';
    case 'Trả hàng':
      return 'fas fa-undo';
    default:
      return 'fas fa-question-circle'; // Icon mặc định nếu không xác định được trạng thái
  }
};

onMounted(async () => {
  const maHoaDon = route.params.maHoaDon;
  if (maHoaDon) {
    loading.value = true;
    await store.getHoaDonDetail(maHoaDon);
    await store.getAllCTSP_HD(0, 1000, '');
    await validateProductsInInvoice();
    loading.value = false;
    console.log('trang_thai:', store.hoaDonDetail?.trang_thai);
    console.log('phuong_thuc_nhan_hang:', store.hoaDonDetail?.phuong_thuc_nhan_hang);
    console.log('nextStatusText:', nextStatusText.value);
    console.log('Danh sách chi tiết hóa đơn:', store.chiTietHoaDons);
    console.log('Mô tả: ', store.hoaDonDetail?.mo_ta);
  }
});

// Add these new computed properties and methods to the script section

// Get only the "Đã cập nhật" statuses from history
const getUpdatedStatuses = computed(() => {
  if (!store.trangThaiHistory) return [];
  return store.trangThaiHistory.filter(history => history.trang_thai === 'Đã cập nhật');
});

// ✅ NEW: Computed property để kiểm tra sản phẩm có đa giá không
const productsWithMultiplePrices = computed(() => {
  if (!store.chiTietHoaDons) return new Set();

  // Nhóm sản phẩm theo id_chi_tiet_san_pham
  const productGroups = {};
  store.chiTietHoaDons.forEach(item => {
    const idCTSP = item.id_chi_tiet_san_pham;
    if (!productGroups[idCTSP]) {
      productGroups[idCTSP] = new Set();
    }
    productGroups[idCTSP].add(item.don_gia / item.so_luong); // Đơn giá mỗi sản phẩm
  });

  // Lấy ra các sản phẩm có nhiều hơn 1 giá
  const multiplePriceIds = new Set();
  Object.keys(productGroups).forEach(idCTSP => {
    if (productGroups[idCTSP].size > 1) {
      multiplePriceIds.add(parseInt(idCTSP));
    }
  });

  return multiplePriceIds;
});

// ✅ NEW: Helper function để kiểm tra sản phẩm có đa giá không
const hasMultiplePrices = (record) => {
  return productsWithMultiplePrices.value.has(record.id_chi_tiet_san_pham);
};

// Check if a status is completed (past status)
const isStatusCompleted = (status) => {
  if (!store.trangThaiHistory || store.trangThaiHistory.length === 0) return false;

  // Define order for main statuses
  const statusOrder = {
    'Chờ xác nhận': 1,
    'Đã xác nhận': 2,
    'Chờ đóng gói': 3,
    'Đang giao': 4,
    'Hoàn thành': 5
  };

  // Current status order
  const currentStatusOrder = statusOrder[store.hoaDonDetail.trang_thai] || 0;
  const checkStatusOrder = statusOrder[status] || 0;

  // Status is completed if it comes before current status
  if (checkStatusOrder < currentStatusOrder) return true;

  // Or if the status exists in history (and is not the current status)
  return store.trangThaiHistory.some(
    history => history.trang_thai === status && history.trang_thai !== store.hoaDonDetail.trang_thai
  );
};

// Get the date when a status occurred
const getStatusDate = (status) => {
  if (!store.trangThaiHistory) return null;

  const statusHistory = store.trangThaiHistory.find(h => h.trang_thai === status);
  return statusHistory ? statusHistory.ngay_chuyen : null;
};

// Calculate position for update markers based on surrounding main statuses
const getUpdatePosition = (update) => {
  if (!update || !update.ngay_chuyen) return 50;

  const statusPoints = {
    'Chờ xác nhận': 0,
    'Đã xác nhận': 25, //20
    'Chờ đóng gói': 50, //40
    'Đang giao': 75, //60
    'Hoàn thành': 100, //80
    'Trả hàng': 100 //100
  };

  const orderedStatuses = [...store.trangThaiHistory]
    .filter(s => s.ngay_chuyen)
    .sort((a, b) => new Date(a.ngay_chuyen).getTime() - new Date(b.ngay_chuyen).getTime());

  const updateIndex = orderedStatuses.findIndex(s =>
    s.trang_thai === 'Đã cập nhật' &&
    s.ngay_chuyen === update.ngay_chuyen
  );
  if (updateIndex === -1) return 50;

  let prevIndex = -1;
  for (let i = updateIndex - 1; i >= 0; i--) {
    if (statusPoints[orderedStatuses[i].trang_thai] !== undefined) {
      prevIndex = i;
      break;
    }
  }

  let nextIndex = -1;
  for (let i = updateIndex + 1; i < orderedStatuses.length; i++) {
    if (statusPoints[orderedStatuses[i].trang_thai] !== undefined) {
      nextIndex = i;
      break;
    }
  }

  if (prevIndex !== -1 && nextIndex !== -1) {
    const prev = orderedStatuses[prevIndex];
    const next = orderedStatuses[nextIndex];

    const prevPoint = statusPoints[prev.trang_thai];
    const nextPoint = statusPoints[next.trang_thai];

    const prevDate = new Date(prev.ngay_chuyen);
    const nextDate = new Date(next.ngay_chuyen);
    const updateDate = new Date(update.ngay_chuyen);

    const total = nextDate - prevDate;
    const progress = updateDate - prevDate;

    if (total > 0 && progress >= 0) {
      let position = prevPoint + ((nextPoint - prevPoint) * (progress / total));

      const tolerance = 4;
      if (Math.abs(position - prevPoint) < tolerance) {
        position = prevPoint + tolerance;
      } else if (Math.abs(position - nextPoint) < tolerance) {
        position = nextPoint - tolerance;
      }

      return Math.max(0, Math.min(100, position));
    } else {
      return (prevPoint + nextPoint) / 2;
    }
  }

  if (prevIndex !== -1) {
    let position = statusPoints[orderedStatuses[prevIndex].trang_thai] + 12.5;
    const tolerance = 5;
    if (Math.abs(position - statusPoints[orderedStatuses[prevIndex].trang_thai]) < tolerance) {
      position += tolerance;
    }
    return Math.max(0, Math.min(100, position));
  }
  if (nextIndex !== -1) {
    let position = statusPoints[orderedStatuses[nextIndex].trang_thai] - 12.5;
    const tolerance = 5;
    if (Math.abs(position - statusPoints[orderedStatuses[nextIndex].trang_thai]) < tolerance) {
      position -= tolerance;
    }
    return Math.max(0, Math.min(100, position));
  }

  return 50;
};

// ✅ NEW: Methods xử lý modal thay đổi giá
const showPriceChangeConfirmation = (productData) => {
  priceChangeInfo.value = {
    productName: productData.ten_san_pham,
    oldPrice: productData.oldPrice,
    newPrice: productData.newPrice,
    idCTSP: productData.idCTSP
  };
  showPriceChangeModal.value = true;
};

const confirmPriceChange = () => {
  showPriceChangeModal.value = false;
  // Backend đã xử lý rồi, chỉ cần đóng modal
};

const cancelPriceChange = () => {
  showPriceChangeModal.value = false;
  // Có thể thêm logic để xóa sản phẩm khỏi danh sách chọn nếu cần
};

const showProcessingResult = (result) => {
  processingResult.value = {
    mergedProducts: result.mergedProducts || 0,
    newProducts: result.newProducts || 0,
    hasPriceConflict: result.hasPriceConflict || false,
    phuThuApplied: result.phuThuApplied || false
  };
  showProcessingResultModal.value = true;
};

const closeProcessingResult = () => {
  showProcessingResultModal.value = false;
};
</script>

<style scoped>
.main-content {
  padding: 20px;
}

.order-header {
  background-color: #f8f9fa;
  padding: 10px;
  margin-bottom: 20px;
}

.status-icons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin: 40px 0;
  position: relative;
  flex-wrap: nowrap;
  overflow-x: auto;
  padding: 20px 0;
  /* Hide scrollbar but keep functionality */
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.status-icons::-webkit-scrollbar {
  display: none;
}

/* Column for each status */
.status-icons .col {
  position: relative;
  z-index: 2;
  min-width: 120px;
  transition: all 0.4s ease;
  transform-origin: center;
}

.status-icons .col:hover {
  transform: translateY(-5px) scale(1.05);
}

.status-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px 10px;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  border: 1px solid rgba(0, 0, 0, 0.05);
  min-height: 120px;
}

.status-icon:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

/* Connection line between status icons */
.status-icon:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 100%;
  width: 30px;
  height: 3px;
  background: linear-gradient(90deg, #1890ff 0%, rgba(24, 144, 255, 0.4) 100%);
  transform: translateY(-50%);
  z-index: 1;
}

/* Icon container */
.status-icon i {
  font-size: 28px;
  margin-bottom: 12px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f0f8ff;
  color: #1890ff;
  margin-top: -30px;
  border: 4px solid #fff;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.4s ease;
}

.status-icon:hover i {
  transform: rotate(360deg);
  background: #1890ff;
  color: #fff;
}

/* Status text */
.status-icon p {
  font-weight: 600;
  margin: 0 0 5px;
  font-size: 14px;
  color: #333;
  text-align: center;
  transition: all 0.3s ease;
}

.status-icon:hover p {
  color: #1890ff;
}

/* Date text */
.status-icon small {
  display: block;
  font-size: 12px;
  color: #888;
  text-align: center;
  transition: all 0.3s ease;
}

/* Active status styles */
.status-icon.active {
  border-color: #1890ff;
  background: #f0f8ff;
  transform: scale(1.05);
}

.status-icon.active i {
  background: #1890ff;
  color: #fff;
  animation: pulse 2s infinite;
}

/* Completed status styles */
.status-icon.completed i {
  background: #52c41a;
  color: #fff;
}

.status-icon.completed::after {
  background: linear-gradient(90deg, #52c41a 0%, rgba(82, 196, 26, 0.4) 100%);
}

/* Text status colors */
.text-danger i {
  background: #ffccc7;
  color: #f5222d;
}

.text-danger:hover i {
  background: #f5222d;
  color: #fff;
}

.text-success i {
  background: #d9f7be;
  color: #52c41a;
}

.text-success:hover i {
  background: #52c41a;
  color: #fff;
}

/* Animation keyframes */
@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(24, 144, 255, 0.7);
  }

  70% {
    box-shadow: 0 0 0 10px rgba(24, 144, 255, 0);
  }

  100% {
    box-shadow: 0 0 0 0 rgba(24, 144, 255, 0);
  }
}

/* Responsive styles */
@media (max-width: 768px) {
  .status-icons {
    padding: 30px 0 10px;
    justify-content: flex-start;
  }

  .status-icon {
    min-height: 100px;
    padding: 10px 8px;
  }

  .status-icon i {
    font-size: 20px;
    width: 50px;
    height: 50px;
    margin-top: -25px;
  }

  .status-icon:not(:last-child)::after {
    width: 20px;
  }
}

.info-box {
  background-color: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;
}

.info-box h5 {
  margin-top: 0;
  color: #1f1f1f;
  font-weight: 500;
}

.product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  margin-right: 10px;
}

.total-section {
  margin-top: 10px;
  text-align: right;
}

.notification {
  background-color: #fff1f0;
  padding: 10px;
  border-radius: 4px;
  text-align: center;
  margin-top: 20px;
  color: #cf1322;
  font-weight: 500;
}

.text-danger {
  color: #ff4d4f;
}

.text-success {
  color: #52c41a;
}

.quantity-display {
  display: flex;
  align-items: center;
  gap: 5px;
}

.quantity-display span {
  min-width: 30px;
  text-align: center;
}

.quantity-input {
  display: flex;
  align-items: center;
  gap: 5px;
}

.modal-header {
  margin-bottom: 20px;
}

.close-btn {
  display: block;
  margin: 20px auto 0;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
}

.popup-input {
  margin: 10px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.popup-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.text-right {
  text-align: right;
}

/* Style cho drawer */
:deep(.ant-drawer-body) {
  padding: 20px;
}

:deep(.ant-form-item) {
  margin-bottom: 16px;
}

:deep(.ant-select) {
  width: 100%;
}

/* Đảm bảo select có chiều cao đồng bộ */
:deep(.ant-select-selector) {
  height: 40px;
  display: flex;
  align-items: center;
}

:deep(.ant-input-number) {
  width: 100%;
}

/* Style cho bảng danh sách địa chỉ */
:deep(.ant-table) {
  margin-bottom: 20px;
}

/* Định dạng timeline trong drawer */
:deep(.ant-timeline-item-content) {
  margin-left: 20px;
}

:deep(.ant-timeline-item-tail) {
  border-left: 2px solid #d9d9d9;
}

:deep(.ant-timeline-item-pending .ant-timeline-item-tail) {
  border-left: 2px dashed #d9d9d9;
}

:deep(.fa-spinner) {
  font-size: 16px;
  color: #1890ff;
}

/* =================code mới================== */
.order-status {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10px;
  margin: 10px 0;
}

.order-status a-form {
  margin-right: 10px;
  display: inline-block;
}

.order-status .ant-btn {
  margin-right: 0;
}

/* Để các button trong a-form inline với nhau */
:deep(.ant-form-inline) {
  display: inline-flex;
  margin-right: 0;
  margin-bottom: 0;
}

/* Ensure forms are displayed side by side */
:deep(.order-status > *) {
  margin-bottom: 0;
  margin-right: 10px;
}

/* Status strip styling */
.status-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 15px 0;
  padding: 5px;
  background: #f7f9fc;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.status-strip .btn {
  position: relative;
  transition: all 0.3s ease;
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
  min-width: 100px;
  overflow: hidden;
}

.status-strip .btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(120deg, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.2) 50%, rgba(255, 255, 255, 0) 100%);
  transform: translateX(-100%);
  transition: transform 0.6s;
}

.status-strip .btn:hover::before {
  transform: translateX(100%);
}

.status-strip .btn-primary {
  background-color: #1890ff;
  border-color: #1890ff;
  color: white;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.35);
}

.status-strip .btn-outline-primary {
  color: #1890ff;
  border-color: #1890ff;
  background-color: white;
}

.status-strip .btn-outline-primary:hover {
  background-color: rgba(24, 144, 255, 0.08);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

/* Badge styling */
:deep(.ant-badge) {
  display: inline-block;
}

:deep(.ant-badge .ant-badge-count) {
  position: absolute;
  top: -8px;
  right: -8px;
  height: 22px;
  padding: 0 7px;
  color: #fff;
  font-weight: bold;
  font-size: 12px;
  background: #ff4d4f;
  border-radius: 11px;
  box-shadow: 0 0 0 1px #fff;
  z-index: 5;
  min-width: 22px;
  transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
}

:deep(.ant-badge-count-sm) {
  height: 18px;
  padding: 0 5px;
  font-size: 11px;
  border-radius: 9px;
}

:deep(.status-strip .ant-badge:hover .ant-badge-count) {
  transform: scale(1.1);
  box-shadow: 0 0 0 2px #fff;
}

/* Animation for switching between statuses */
.status-strip .btn-primary,
.status-strip .btn-outline-primary {
  transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
}

/* Status colors for different types */
.status-strip button[data-status="Chờ xác nhận"] .ant-badge-count {
  background-color: #faad14;
}

.status-strip button[data-status="Đã xác nhận"] .ant-badge-count {
  background-color: #1890ff;
}

.status-strip button[data-status="Đang giao"] .ant-badge-count {
  background-color: #722ed1;
}

.status-strip button[data-status="Hoàn thành"] .ant-badge-count {
  background-color: #52c41a;
}

.status-strip button[data-status="Đã hủy"] .ant-badge-count {
  background-color: #ff4d4f;
}

.order-status-timeline {
  position: relative;
  margin: 60px 0 80px;
  padding: 0 20px;
  width: 100%;
}

.timeline-track {
  position: absolute;
  top: 40px;
  left: 0;
  right: 0;
  height: 6px;
  background: linear-gradient(90deg, #f5f5f5, #f0f0f0);
  border-radius: 3px;
  z-index: 1;
}

.timeline-steps {
  display: flex;
  justify-content: space-between;
  position: relative;
  z-index: 2;
}

.timeline-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  min-width: 120px;
  margin: 0 5px;
  flex: 1;
}

.timeline-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 15px;
  transition: all 0.4s cubic-bezier(0.215, 0.61, 0.355, 1);
  position: relative;
  border: 3px solid #e8e8e8;
  z-index: 2;
}

.timeline-icon i {
  font-size: 32px;
  color: #b0b0b0;
  transition: all 0.4s ease;
}

.timeline-content {
  text-align: center;
  max-width: 150px;
  transition: all 0.3s ease;
}

.timeline-content h4 {
  margin: 0 0 5px;
  font-size: 16px;
  font-weight: 600;
  color: #666;
  transition: all 0.3s ease;
}

.timeline-content p {
  margin: 0;
  font-size: 13px;
  color: #888;
}

/* Active status styling */
.timeline-step.active .timeline-icon {
  border-color: #1890ff;
  box-shadow: 0 8px 16px rgba(24, 144, 255, 0.3);
  transform: translateY(-10px) scale(1.05);
  animation: pulse-blue 2s infinite;
}

.timeline-step.active .timeline-icon i {
  color: #1890ff;
}

.timeline-step.active .timeline-content h4 {
  color: #1890ff;
  font-weight: 700;
  font-size: 18px;
}

/* Completed status styling */
.timeline-step.completed .timeline-icon {
  border-color: #52c41a;
  background-color: #f6ffed;
}

.timeline-step.completed .timeline-icon i {
  color: #52c41a;
}

.timeline-step.completed .timeline-content h4 {
  color: #52c41a;
}

/* Connection line styling */
.timeline-step::before {
  content: '';
  position: absolute;
  top: 40px;
  width: 100%;
  height: 6px;
  background-color: #e8e8e8;
  z-index: 1;
}

.timeline-step.completed::before,
.timeline-step.active::before {
  background: linear-gradient(90deg, #52c41a, #1890ff);
}

.timeline-step:first-child::before {
  left: 50%;
  width: 50%;
}

.timeline-step:last-child::before {
  width: 50%;
  right: 50%;
}

/* Update markers styling */
.update-markers {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 3;
}

.update-marker {
  position: absolute;
  top: 34px;
  /* Align with timeline track */
}

.update-icon {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background-color: #722ed1;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(114, 46, 209, 0.4);
  cursor: pointer;
  border: 2px solid #fff;
  transition: all 0.3s ease;
  overflow: hidden;
}

.update-icon i {
  font-size: 8px;
  color: white;
}

.update-marker:hover .update-icon {
  transform: scale(1.2);
}

.update-tooltip {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%) scale(0);
  min-width: 180px;
  background-color: rgba(0, 0, 0, 0.85);
  color: #fff;
  padding: 10px 15px;
  border-radius: 6px;
  font-size: 12px;
  z-index: 10;
  opacity: 0;
  pointer-events: none;
  transition: all 0.3s ease;
  box-shadow: 0 5px 10px rgba(0, 0, 0, 0.2);
}

.update-tooltip h5 {
  margin: 0 0 5px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.update-tooltip p {
  margin: 2px 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.update-marker:hover .update-tooltip {
  opacity: 1;
  transform: translateX(-50%) scale(1);
}

.update-tooltip::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 6px solid transparent;
  border-top-color: rgba(0, 0, 0, 0.85);
}

.original-price {
  text-decoration: line-through;
  color: gray;
  font-size: 12px;
  /* Kích thước chữ nhỏ hơn */
}

/* Animation for active status */
@keyframes pulse-blue {
  0% {
    box-shadow: 0 0 0 0 rgba(24, 144, 255, 0.5);
  }

  70% {
    box-shadow: 0 0 0 15px rgba(24, 144, 255, 0);
  }

  100% {
    box-shadow: 0 0 0 0 rgba(24, 144, 255, 0);
  }
}

/* Responsive styles */
/* @media (max-width: 992px) {
    .timeline-step {
        min-width: 80px;
    }

    .timeline-icon {
        width: 60px;
        height: 60px;
    }

    .timeline-icon i {
        font-size: 26px;
    }

    .timeline-content h4 {
        font-size: 14px;
    }
} */

@media (max-width: 767px) {
  .timeline-steps {
    flex-wrap: nowrap;
    overflow-x: auto;
    padding-bottom: 20px;
    justify-content: flex-start;
  }

  .timeline-step {
    min-width: 140px;
    flex: 0 0 auto;
  }

  .timeline-track {
    height: 4px;
  }

  .update-marker {
    top: 32px;
  }

  .original-price {
    text-decoration: line-through;
    color: gray;
    font-size: 12px;
    /* Kích thước chữ nhỏ hơn */
  }
}

.status-online {
  display: inline-block;
  padding: 4px 8px;
  border: 2px solid #007bff;
  /* Màu xanh dương */
  border-radius: 12px;
  color: #007bff;
  font-weight: bold;
  text-align: center;
  background-color: rgba(0, 123, 255, 0.1);
  /* Nền xanh nhạt */
}

/* Trạng thái Offline */
.status-offline {
  display: inline-block;
  padding: 4px 8px;
  border: 2px solid #28a745;
  /* Màu xanh lá */
  border-radius: 12px;
  color: #28a745;
  font-weight: bold;
  text-align: center;
  background-color: rgba(40, 167, 69, 0.1);
  /* Nền xanh lá nhạt */
}

.ghtk-logo {
  width: 100px;
  /* Chiều rộng logo */
  height: 20px;
  /* Chiều cao logo */
  object-fit: contain;
  /* Đảm bảo logo không bị méo */
}

/* ------------------------------------------------------------------------------ */
/* .timeline-step.cancelled .timeline-icon {
    background-color: #fff1f0;
    color: #f5222d;
    border-color: #f5222d;
}

.timeline-step.cancelled.active .timeline-icon {
    background-color: #f5222d;
    color: #fff;
}

.timeline-step.cancelled .timeline-content h4 {
    color: #f5222d;
} */

/* @media (max-width: 768px) {
    .timeline-steps {
        flex-wrap: nowrap;
        overflow-x: auto;
        padding-bottom: 20px;
        justify-content: flex-start;
    }

    .timeline-step {
        min-width: 140px;
        flex: 0 0 auto;
    }

    .timeline-track {
        height: 4px;
    }

    .update-marker {
        top: 32px;
    }
} */

/* ✅ NEW: CSS cho modal thay đổi giá */
.price-change-content {
  padding: 20px 0;
}

.price-comparison {
  margin: 20px 0;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  overflow: hidden;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  font-size: 14px;
}

.price-item.old-price {
  background-color: #fff2f0;
  border-bottom: 1px solid #d9d9d9;
}

.price-item.new-price {
  background-color: #f6ffed;
}

.price-value {
  font-weight: bold;
  font-size: 16px;
}

.old-price .price-value {
  color: #ff4d4f;
}

.new-price .price-value {
  color: #52c41a;
}

.price-change-info {
  margin-top: 20px;
  padding: 15px;
  background-color: #f0f7ff;
  border-radius: 6px;
  border-left: 4px solid #1890ff;
}

.price-change-info p {
  margin: 8px 0;
  font-size: 13px;
  color: #595959;
}

.price-change-info i {
  margin-right: 8px;
  color: #1890ff;
}

/* ✅ NEW: CSS cho modal kết quả xử lý */
.processing-result-content {
  padding: 20px 0;
}

.result-item {
  margin: 12px 0;
  text-align: center;
}

.result-item .ant-tag {
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 20px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.modal-footer {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* ✅ NEW: CSS cho sản phẩm hiển thị */
.product-info {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.product-details {
  flex: 1;
}

.product-name {
  font-weight: 500;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.product-specs {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

/* Hiệu ứng hover cho tag đa giá */
.product-name .ant-tag {
  transition: all 0.3s ease;
}

.product-name .ant-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.3);
}
</style>
