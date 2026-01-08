<template>
    <div class="container mt-3">
        <!-- Breadcrumb -->
        <div class="breadcrumb-container">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a href="/home">Trang chủ</a></li>
                    <li class="breadcrumb-item"><a href="#">Sản phẩm</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Chi tiết sản phẩm</li>
                </ol>
            </nav>
        </div>

        <div class="product-main-content">
            <!-- Phần gallery hình ảnh -->
            <div class="product-gallery" ref="productGalleryRef">
                <div class="main-image-container">
                    <div class="image-zoom-container" ref="zoomContainer">
                        <div class="slider-container">
                            <!-- All images in the product gallery are rendered, but only current one is visible -->
                            <div v-for="(image, index) in product.hinh_anh" :key="index" class="slide-image" :class="{
                                'active': index === currentImageIndex,
                                'previous': isPreviousImage(index),
                                'next': isNextImage(index)
                            }" :style="{ 'background-image': `url(${image.url})` }" @mousemove="handleMouseMove"
                                @mouseleave="handleMouseLeave">
                            </div>
                        </div>
                        <div class="zoom-lens" ref="zoomLens" v-show="zoomActive"></div>
                        <div class="zoom-result" ref="zoomResult" v-show="zoomVisible"></div>
                    </div>
                    <div class="image-controls">
                        <button class="control-btn prev-btn" @click="prevImage" :disabled="currentImageIndex === 0">
                            <left-outlined />
                        </button>
                        <button class="control-btn fullscreen-btn" @click="showFullscreen = true">
                            <expand-outlined />
                        </button>
                        <button class="control-btn next-btn" @click="nextImage"
                            :disabled="currentImageIndex === product.hinh_anh.length - 1">
                            <right-outlined />
                        </button>
                    </div>
                </div>
                <div class="thumbnails-container">
                    <div class="thumbnail-wrapper" v-for="(image, index) in product.hinh_anh" :key="index"
                        @click="changeImage(index)" :class="{ active: currentImageIndex === index }">
                        <div class="color-indicator" :style="{ backgroundColor: image.color_code }"
                            :title="image.color_name">
                        </div>
                        <img :src="image.url" alt="Thumbnail" class="thumbnail-image">
                    </div>
                </div>
            </div>

            <!-- Phần thông tin sản phẩm -->
            <div class="product-info" ref="productInfoRef">
                <div class="product-badges">
                    <span class="badge badge-sale" v-if="product.giam_gia">-{{ discountPercent }}%</span>
                    <span class="badge badge-new" v-if="product.moi">Mới</span>
                </div>

                <h1 class="product-title">{{ product.ten_san_pham }}</h1>

                <div class="product-meta">
                    <div class="meta-item">
                        <span class="meta-label">Mã sản phẩm:</span>
                        <span class="meta-value">{{ product.ma_san_pham }}</span>
                    </div>
                    <div class="meta-item">
                        <span class="meta-label">Thương hiệu:</span>
                        <span class="meta-value">{{ product.thuong_hieu }}</span>
                    </div>
                    <div class="meta-item">
                        <span class="meta-label">Danh mục:</span>
                        <span class="meta-value">{{ product.danh_muc }}</span>
                    </div>
                    <div class="meta-item">
                        <span class="meta-label">Chất liệu:</span>
                        <span class="meta-value">{{ product.chat_lieu }}</span>
                    </div>
                </div>

                <div class="product-price">
                    <span class="current-price" @input="handleGiaBanInput" @blur="handleGiaBanBlur">{{
                        formatCurrency(product.gia_khuyen_mai) }}</span>
                    <span class="original-price" v-if="product.giam_gia">{{ formatCurrency(product.gia_goc) }}</span>
                    <span class="discount-percent" v-if="product.giam_gia">(-{{ discountPercent }}%)</span>
                </div>

                <div class="product-rating">
                    <div class="stars">
                        <star-filled v-for="i in Math.floor(product.danh_gia)" :key="'star-' + i" />
                        <star-two-tone v-if="product.danh_gia % 1 >= 0.5" twoToneColor="#ffc107" />
                        <star-outlined v-for="i in (5 - Math.ceil(product.danh_gia))" :key="'empty-star-' + i" />
                    </div>
                    <span class="rating-count">({{ product.so_luot_danh_gia }} đánh giá)</span>
                </div>

                <div class="product-colors">
                    <h3 class="option-title">Màu sắc:</h3>
                    <div class="color-options">
                        <div v-for="(color, index) in product.mau_sac" :key="'color-' + index" class="color-option"
                            :class="{ active: selectedColor === color.ma }" @click="selectColor(color)">
                            <div class="color-swatch" :style="{ backgroundColor: color.ma_mau }"></div>
                            <span class="color-name">{{ color.ten }}</span>
                        </div>
                    </div>
                </div>

                <div class="product-sizes">
                    <h3 class="option-title">Kích thước:</h3>
                    <div class="size-options">
                        <button v-for="(size, index) in availableSizes" :key="'size-' + index" class="size-option"
                            :class="{
                                active: selectedSize === size.ma,
                                disabled: !size.co_san
                            }" @click="selectSize(size)" :disabled="!size.co_san">
                            {{ size.ten }}
                            <span v-if="!size.co_san" class="size-unavailable">✕</span>
                        </button>
                    </div>
                </div>

                <div class="product-quantity">
                    <h3 class="option-title">Số lượng:</h3>
                    <div class="quantity-selector mb-3">
                        <button class="quantity-btn minus" @click="decreaseQuantity" :disabled="quantity <= 1">
                            <minus-outlined />
                        </button>
                        <input type="number" v-model.number="quantity" min="1" max="99" class="quantity-input">
                        <button class="quantity-btn plus" @click="increaseQuantity" :disabled="quantity >= 99">
                            <plus-outlined />
                        </button>
                    </div>
                    <span class="stock-status mt-2" v-if="displayStockStatus.check">{{ displayStockStatus.text }}</span>
                    <span class="stock-status low-stock mt-2"
                        v-else-if="displayStockStatus.text.includes('Còn') && !displayStockStatus.check">{{
                            displayStockStatus.text
                        }}</span>
                    <span class="stock-status out-of-stock" v-else>{{ displayStockStatus.text }}</span>
                </div>

                <div class="product-actions">
                    <button class="btn-add-to-cart" @click="addToCartFromDetail">
                        <shopping-cart-outlined />
                        Thêm vào giỏ hàng
                        <span v-if="cartItemCount > 0" class="cart-count-badge">{{ cartItemCount }}</span>
                    </button>
                    <button class="btn-buy-now" @click="buyNow">
                        <thunderbolt-outlined />
                        Mua ngay
                    </button>
                    <button class="btn-add-to-wishlist" @click="toggleWishlist">
                        <heart-filled v-if="isInWishlist" class="heart-icon filled" />
                        <heart-outlined v-else class="heart-icon outlined" />
                    </button>
                    <div class="wishlist-info">
                        <span class="wishlist-count">{{ product.so_luot_yeu_thich || 0 }}</span>
                        <span class="wishlist-text">lượt thích</span>
                    </div>
                </div>
                <div class="product-delivery-info">
                    <div class="delivery-item">
                        <i class="fas fa-truck"></i>
                        <span>Giao hàng miễn phí cho đơn hàng từ 2.000.000đ</span>
                    </div>
                    <!-- <div class="delivery-item">
                        <i class="fas fa-undo"></i>
                        <span>Đổi trả trong vòng 30 ngày</span>
                    </div> -->
                    <!-- <div class="delivery-item">
                        <i class="fas fa-shield-alt"></i>
                        <span>Bảo hành chính hãng 12 tháng</span>
                    </div> -->
                </div>
            </div>
        </div>

        <!-- Phần mô tả và tab -->
        <div class="product-details-tabs" ref="productTabsRef">
            <div class="tabs-header">
                <button class="tab-btn" :class="{ active: activeTab === 'description' }"
                    @click="changeTab('description')">
                    Mô tả sản phẩm
                </button>
                <button class="tab-btn" :class="{ active: activeTab === 'reviews' }" @click="changeTab('reviews')">
                    Đánh giá ({{ product.so_luot_danh_gia }})
                </button>
            </div>

            <div class="tabs-content">
                <div class="tab-pane" v-show="activeTab === 'description'">
                    <div class="product-description" v-html="product.mo_ta"></div>
                </div>


                <div class="tab-pane" v-show="activeTab === 'reviews'">
                    <div class="reviews-summary">
                        <div class="average-rating">
                            <div class="rating-number">{{ product.danh_gia.toFixed(1) }}</div>
                            <div class="rating-stars">
                                <star-filled v-for="i in Math.floor(product.danh_gia)" :key="'rev-star-' + i" />
                                <star-two-tone v-if="product.danh_gia % 1 >= 0.5" twoToneColor="#ffc107" />
                                <star-outlined v-for="i in (5 - Math.ceil(product.danh_gia))"
                                    :key="'rev-empty-star-' + i" />
                            </div>
                            <div class="rating-count">{{ product.so_luot_danh_gia }} đánh giá</div>
                        </div>

                        <div class="rating-bars">
                            <div class="rating-bar-item" v-for="i in 5" :key="'bar-' + i">
                                <div class="rating-label">{{ 6 - i }} sao</div>
                                <div class="rating-bar">
                                    <div class="rating-bar-fill"
                                        :style="{ width: (product.danh_gia_chi_tiet.chi_tiet[6 - i] ? product.danh_gia_chi_tiet.chi_tiet[6 - i].phan_tram : 0) + '%' }">
                                    </div>
                                </div>
                                <div class="rating-percent">{{ product.danh_gia_chi_tiet.chi_tiet[6 - i] ?
                                    product.danh_gia_chi_tiet.chi_tiet[6 - i].phan_tram : 0 }}%</div>
                            </div>
                        </div>

                        <div class="add-review-button-container">
                            <a-button type="primary" @click="openAddReviewModal" v-if="canAddReview">
                                <plus-outlined />
                                Thêm bình luận
                            </a-button>
                            <p class="login-to-review" v-else>
                                <a-alert type="info" show-icon>
                                    <template #message>Vui lòng <a @click="navigateToLogin">đăng nhập</a> để viết đánh
                                        giá</template>
                                </a-alert>
                            </p>
                        </div>
                    </div>

                    <div class="reviews-list">
                        <div class="review-item" v-for="(review, index) in product.danh_gia_chi_tiet.binh_luan"
                            :key="'review-' + index">
                            <div class="review-header">
                                <div class="reviewer-info">
                                    <div class="reviewer-avatar">
                                        <img :src="review.avatar" alt="Avatar">
                                    </div>
                                    <div class="reviewer-name">{{ review.ten_nguoi_dung }}</div>
                                </div>
                                <div class="review-actions">
                                    <div class="review-rating">
                                        <star-filled v-for="i in review.danh_gia" :key="'review-star-' + i" />
                                        <star-outlined v-for="i in (5 - review.danh_gia)"
                                            :key="'review-empty-star-' + i" />
                                    </div>
                                    <div class="review-buttons" v-if="isOwnReview(review)">
                                        <a-button type="text" size="small" @click="editReview(review)">
                                            <edit-outlined />
                                            Sửa
                                        </a-button>
                                    </div>
                                </div>
                            </div>
                            <div class="review-date">{{ review.ngay }}</div>
                            <div class="review-content">{{ review.noi_dung }}</div>
                            <div class="review-images" v-if="review.hinh_anh && review.hinh_anh.length">
                                <div class="review-image" v-for="(image, imgIndex) in review.hinh_anh"
                                    :key="'review-img-' + imgIndex">
                                    <img :src="image" alt="Review image">
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="no-reviews"
                        v-if="!product.danh_gia_chi_tiet.binh_luan || !product.danh_gia_chi_tiet.binh_luan.length">
                        <p>Chưa có đánh giá nào cho sản phẩm này.</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Sản phẩm tương tự -->
        <div class="rec-related-products-section" ref="recommendedProductsRef">
            <h2 class="rec-related-section-title">Sản phẩm tương tự</h2>
            <div class="rec-carousel-container" @mouseenter="showRecArrows = true" @mouseleave="showRecArrows = false">
                <button class="rec-custom-arrow rec-prev-arrow" @click="prevRecSlide"
                    :class="{ 'visible': showRecArrows }">
                    <left-outlined />
                </button>
                <button class="rec-custom-arrow rec-next-arrow" @click="nextRecSlide"
                    :class="{ 'visible': showRecArrows }">
                    <right-outlined />
                </button>
                <a-carousel ref="recCarousel" dots-class="custom-dots">
                    <div v-for="(slideGroup, groupIndex) in recProductSlides" :key="groupIndex">
                        <div class="row rec-related-products-grid">
                            <div class="col rec-product-card" v-for="(rec, index) in slideGroup" :key="index"
                                @mouseenter="activeRecProduct = rec.id" @mouseleave="activeRecProduct = null">
                                <div class="rec-product-image-container">
                                    <img class="rec-product-image" :src="rec.image" alt="Product image">
                                    <div class="rec-discount-badge" v-if="rec.discountPercent">
                                        -{{ rec.discountPercent }}%
                                    </div>
                                    <div class="rec-product-overlay" :class="{ 'active': activeRecProduct === rec.id }">
                                        <div class="rec-overlay-buttons">
                                            <router-link :to="{ name: 'sanPhamDetail-BanHang', params: { id: rec.id } }"
                                                class="rec-overlay-btn view-btn">
                                                <eye-outlined />
                                                <span>Xem</span>
                                            </router-link>
                                            <button class="rec-overlay-btn cart-btn" @click="showRecProductDetail(rec)">
                                                <shopping-cart-outlined />
                                                <span>Thêm</span>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                                <div class="rec-product-info">
                                    <div class="rec-product-price-row">
                                        <span class="rec-product-price">{{ rec.price }}</span>
                                        <span class="rec-product-old-price" v-if="rec.oldPrice">{{ rec.oldPrice
                                            }}</span>
                                        <span class="rec-product-discount" v-if="rec.discount">{{ rec.discount }}</span>
                                    </div>
                                    <h6 class="rec-product-name">{{ rec.name }}</h6>
                                    <div class="rec-product-meta">
                                        <span class="rec-product-brand">{{ rec.brand }}</span>
                                        <div class="rec-product-rating">
                                            <star-filled />
                                            <span>{{ rec.rating }} ({{ rec.reviews }})</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </a-carousel>
            </div>
            <!-- Modal xem nhanh sản phẩm tương tự -->
            <a-modal v-model:visible="recModalVisible" :title="selectedRecProduct?.name" width="800px" :footer="null"
                @cancel="handleRecModalCancel" :zIndex="9999" :maskStyle="{ zIndex: 9998 }"
                :wrapStyle="{ zIndex: 9999 }" centered :style="{ top: '20px' }">
                <div class="rec-product-detail-modal">
                    <div class="rec-product-detail-content">
                        <div class="rec-product-images">
                            <div class="rec-main-image">
                                <img :src="selectedRecProduct?.image" :alt="selectedRecProduct?.name">
                            </div>
                        </div>
                        <div class="rec-product-info-detail">
                            <div class="rec-price-section">
                                <span class="rec-current-price">{{ selectedRecProduct?.price }}</span>
                                <span class="rec-old-price" v-if="selectedRecProduct?.oldPrice">{{
                                    selectedRecProduct?.oldPrice
                                    }}</span>
                                <span class="rec-discount-badge" v-if="selectedRecProduct?.discount">{{
                                    selectedRecProduct?.discount }}</span>
                            </div>
                            <div class="rec-brand-section">
                                <span class="rec-brand-label">Thương hiệu:</span>
                                <span class="rec-brand-value">{{ selectedRecProduct?.brand }}</span>
                            </div>
                            <div class="rec-rating-section">
                                <div class="rec-rating">
                                    <star-filled />
                                    <span>{{ selectedRecProduct?.rating }} ({{ selectedRecProduct?.reviews }})</span>
                                </div>
                            </div>
                            <div class="rec-description-section">
                                <h4>Mô tả sản phẩm</h4>
                                <p>{{ selectedRecProduct?.description || 'Chưa có mô tả chi tiết' }}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </a-modal>
        </div>


        <!-- Modal xem ảnh toàn màn hình -->
        <div class="fullscreen-modal" v-if="showFullscreen" @click="showFullscreen = false">
            <div class="fullscreen-content" @click.stop>
                <button class="close-fullscreen" @click="showFullscreen = false">
                    <close-outlined />
                </button>
                <div class="fullscreen-image-container">
                    <div class="slider-container">
                        <div v-for="(image, index) in product.hinh_anh" :key="index"
                            class="slide-image fullscreen-slide" :class="{
                                'active': index === currentImageIndex,
                                'previous': isPreviousImage(index),
                                'next': isNextImage(index)
                            }" :style="{ 'background-image': `url(${image.url})` }">
                        </div>
                    </div>
                </div>
                <div class="fullscreen-thumbnails">
                    <div class="thumbnail-wrapper" v-for="(image, index) in product.hinh_anh" :key="index"
                        @click="changeImage(index)" :class="{ active: currentImageIndex === index }">
                        <div class="color-indicator" :style="{ backgroundColor: image.color_code }"
                            :title="image.color_name">
                        </div>
                        <img :src="image.url" alt="Thumbnail" class="thumbnail-image">
                    </div>
                </div>
                <div class="fullscreen-controls">
                    <button class="control-btn prev-btn" @click.stop="prevImage" :disabled="currentImageIndex === 0">
                        <left-outlined />
                    </button>
                    <button class="control-btn next-btn" @click.stop="nextImage"
                        :disabled="currentImageIndex === product.hinh_anh.length - 1">
                        <right-outlined />
                    </button>
                </div>
            </div>
        </div>

        <!-- Modal notificación carrito -->
        <a-modal v-model:open="cartNotification" :footer="null" :mask-closable="true" :width="400"
            class="cart-notification-modal">
            <div class="cart-notification">
                <div class="notification-header">
                    <check-circle-outlined class="success-icon" />
                    <h3>Đã thêm vào giỏ hàng!</h3>
                </div>

                <div class="notification-product" v-if="lastAddedProduct">
                    <img :src="lastAddedProduct.image" :alt="lastAddedProduct.name" class="product-thumbnail">
                    <div class="product-info">
                        <h4>{{ lastAddedProduct.name }}</h4>
                        <div class="product-variants" v-if="lastAddedProduct.color || lastAddedProduct.size">
                            <span v-if="lastAddedProduct.color">Màu: {{ lastAddedProduct.color }}</span>
                            <span v-if="lastAddedProduct.size">Size: {{ lastAddedProduct.size }}</span>
                        </div>
                        <div class="product-quantity-price">
                            <span class="quantity">SL: {{ lastAddedProduct.quantity }}</span>
                            <span class="price">{{ formatCurrency(lastAddedProduct.price) }}</span>
                        </div>
                    </div>
                </div>

                <div class="cart-summary">
                    <div class="summary-row">
                        <span>Giỏ hàng của bạn</span>
                        <span>{{ cartItems.length }} sản phẩm</span>
                    </div>
                    <div class="summary-row total">
                        <span>Tổng tiền:</span>
                        <span>{{ formatCurrency(cartTotal) }}</span>
                    </div>
                </div>

                <div class="notification-actions">
                    <a-button @click="cartNotification = false">
                        Tiếp tục mua sắm
                    </a-button>
                    <a-button type="primary" @click="viewCart">
                        Xem giỏ hàng
                    </a-button>
                </div>
            </div>
        </a-modal>

        <!-- Modal chỉnh sửa đánh giá -->
        <a-modal v-model:open="editReviewVisible" :footer="null" :mask-closable="true" :width="600" centered
            class="edit-review-modal">
            <div class="edit-review">
                <h3>Chỉnh sửa đánh giá</h3>
                <a-form layout="vertical">
                    <a-form-item label="Đánh giá">
                        <a-rate v-model:value="editReviewForm.rating" />
                    </a-form-item>
                    <a-form-item label="Nội dung">
                        <a-textarea v-model:value="editReviewForm.content" rows="4" />
                    </a-form-item>
                    <div class="edit-review-actions">
                        <a-button @click="cancelEditReview">Hủy</a-button>
                        <a-button type="primary" @click="saveEditedReview">Lưu</a-button>
                    </div>
                </a-form>
            </div>
        </a-modal>

        <!-- lềnh thêm mới -->
        <div v-if="isFavoritedVariant" class="favorited-variant-badge">
            Đây là sản phẩm yêu thích của bạn với màu sắc và kích thước này
        </div>

        <!-- Modal thêm bình luận mới -->
        <a-modal v-model:open="addReviewVisible" :footer="null" :mask-closable="true" :width="600" centered
            class="add-review-modal">
            <div class="add-review">
                <h3>Thêm bình luận mới</h3>
                <a-form layout="vertical">
                    <a-form-item label="Đánh giá sao">
                        <a-rate v-model:value="newReviewForm.rating" />
                    </a-form-item>
                    <a-form-item label="Nội dung bình luận">
                        <a-textarea v-model:value="newReviewForm.content" rows="4"
                            placeholder="Chia sẻ cảm nhận của bạn về sản phẩm này..." />
                    </a-form-item>
                    <div class="add-review-actions">
                        <a-button @click="cancelAddReview">Hủy</a-button>
                        <a-button type="primary" @click="submitNewReview">Gửi bình luận</a-button>
                    </div>
                </a-form>
            </div>
        </a-modal>
        <a-modal v-model:visible="showAddedToCartModal" title="Thêm vào giỏ hàng thành công" @ok="handleModalOk"
            @cancel="handleModalCancel">
            <!-- Nội dung modal -->
        </a-modal>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onBeforeUnmount } from 'vue';
import { useIntersectionObserver } from '@vueuse/core';
import { useRoute, useRouter } from 'vue-router';
import {
    HeartFilled,
    HeartOutlined,
    LeftOutlined,
    RightOutlined,
    ExpandOutlined,
    CloseOutlined,
    StarFilled,
    StarOutlined,
    ShoppingCartOutlined,
    ThunderboltOutlined,
    PlusOutlined,
    MinusOutlined,
    StarTwoTone,
    EyeOutlined,
    CheckCircleOutlined,
    EditOutlined,
    DeleteOutlined
} from '@ant-design/icons-vue';
import { useGbStore } from '@/stores/gbStore';
import { message, notification, Modal } from 'ant-design-vue';
import axios from 'axios';
import { favoriteService } from '@/services/favoriteService';
import { reviewService } from '@/services/reviewService';
import { banHangOnlineService } from '@/services/banHangOnlineService';

// Lấy ID sản phẩm từ tham số URL
const route = useRoute();
const productId = computed(() => route.params.id);
const store = useGbStore();
const router = useRouter();

// Dữ liệu chi tiết sản phẩm
const productDetails = ref([]);
const selectedVariant = ref(null);

// Dữ liệu sản phẩm
const product = ref({
    ma_san_pham: '',
    ten_san_pham: '',
    thuong_hieu: '',
    danh_muc: '',
    chat_lieu: '',
    gia_goc: 0,
    gia_khuyen_mai: 0,
    giam_gia: false,
    phan_tram_giam_gia: 0,
    moi: false,
    danh_gia: 0,
    so_luot_danh_gia: 0,
    so_luong: 0,
    ngay_tao: '',
    mau_sac: [],
    kich_thuoc: [],
    mo_ta: '',
    thong_so_ky_thuat: [],
    hinh_anh: [],
    danh_gia_chi_tiet: {
        trung_binh: 0,
        tong_so: 0,
        chi_tiet: [],
        binh_luan: []
    },
    san_pham_lien_quan: []
});

// Thêm vào script setup
// Thêm ref để lưu trữ hình ảnh theo màu sắc
const imagesByColor = ref(new Map());
const allImages = ref([]);

// Cập nhật hàm fetchProductDetail để tạo map hình ảnh theo màu
// lềnh sửa hàm
const fetchProductDetail = async (id, route) => {
    try {
        isFavoritedVariant.value = false; // Reset trạng thái
        await store.getCTSPBySanPhamFull(id);

        if (store.cTSPBySanPhamFull && store.cTSPBySanPhamFull.length > 0) {
            productDetails.value = store.cTSPBySanPhamFull;
            selectedVariant.value = productDetails.value[0];
            organizeImagesByColor();
            updateProductFromVariant(selectedVariant.value);
            initializeColorAndSizeOptions();

            // Kiểm tra tham số variant
            const variantId = route.query.variant;
            if (variantId) {
                const variant = productDetails.value.find(v => v.id_chi_tiet_san_pham == variantId);
                if (variant) {
                    selectedColor.value = variant.id_mau_sac;
                    selectedColorName.value = variant.ten_mau_sac;
                    selectedSize.value = variant.id_kich_thuoc;
                    selectedSizeName.value = variant.gia_tri;
                    selectedVariant.value = variant;
                    isFavoritedVariant.value = true;
                }
            }

            if (selectedVariant.value && selectedVariant.value.id_chi_tiet_san_pham) {
                fetchProductReviews(selectedVariant.value.id_chi_tiet_san_pham);
            }
        } else {
            message.error('Không tìm thấy thông tin sản phẩm');
        }
    } catch (error) {
        console.error('Lỗi khi lấy thông tin sản phẩm:', error);
        message.error('Đã xảy ra lỗi khi tải thông tin sản phẩm');
    }
};

const cartItemCount = ref(0);
// Hàm tải giỏ hàng và cập nhật số lượng
const updateCartCount = async () => {
    try {
        // ✅ FIXED: Kiểm tra CẢ 2 loại đăng nhập
        const customerDataStr = sessionStorage.getItem('khachHang') || localStorage.getItem('khachHang');
        const adminDataStr = sessionStorage.getItem('userDetails') || localStorage.getItem('userDetails');

        let idKhachHang = null;
        let userType = '';

        if (customerDataStr) {
            const customerData = JSON.parse(customerDataStr);
            idKhachHang = customerData.idKhachHang;
            userType = 'CUSTOMER';
        } else if (adminDataStr) {
            const adminData = JSON.parse(adminDataStr);
            idKhachHang = adminData.idKhachHang;
            userType = 'ADMIN';
        }

        if (idKhachHang) {
            // ✅ Đã đăng nhập: lấy số lượng từ database
            console.log(`✅ [DETAIL ${userType}] Loading cart count from database for ID:`, idKhachHang);
            try {
                const response = await banHangOnlineService.getGioHang(idKhachHang);
                const cartItems = Array.isArray(response) ? response : (response.data || []);
                const totalItems = cartItems.reduce((total, item) => total + (Number(item.so_luong) || 0), 0);
                console.log(`✅ [DETAIL ${userType}] Database cart count:`, totalItems);
                cartItemCount.value = totalItems;

                // Phát sự kiện cập nhật giỏ hàng
                window.dispatchEvent(new CustomEvent('cart-updated', { detail: { count: totalItems } }));
            } catch (error) {
                console.error('❌ [DETAIL] Lỗi khi lấy số lượng giỏ hàng từ API:', error);
                cartItemCount.value = 0;
            }
        } else {
            // ❌ Chưa đăng nhập: lấy số lượng từ localStorage
            console.log('🔄 [DETAIL GUEST] Loading cart count from localStorage');
            const localCart = localStorage.getItem('gb-sport-cart');
            let totalItems = 0;

            if (localCart) {
                try {
                    const cartItems = JSON.parse(localCart);
                    totalItems = cartItems.reduce((total, item) => total + (Number(item.quantity) || 0), 0);
                } catch (error) {
                    console.error('Lỗi khi parse giỏ hàng từ localStorage:', error);
                }
            }

            console.log('🔄 [DETAIL GUEST] LocalStorage cart count:', totalItems);
            cartItemCount.value = totalItems;

            // Phát sự kiện cập nhật giỏ hàng
            window.dispatchEvent(new CustomEvent('cart-updated', { detail: { count: totalItems } }));
        }
    } catch (error) {
        console.error('❌ [DETAIL] Lỗi khi cập nhật số lượng giỏ hàng:', error);
        cartItemCount.value = 0;
    }
};
// Sửa hàm initializeColorAndSizeOptions để lưu trạng thái
const initializeColorAndSizeOptions = () => {
    // Tạo danh sách màu sắc duy nhất
    const uniqueColors = new Map();
    const uniqueSizes = new Map();

    productDetails.value.forEach(variant => {
        // Thêm màu sắc
        if (variant.id_mau_sac && !uniqueColors.has(variant.id_mau_sac)) {
            uniqueColors.set(variant.id_mau_sac, {
                ma: variant.id_mau_sac,
                ten: variant.ten_mau_sac || `Màu ${variant.id_mau_sac}`,
                ma_mau: getColorCode(variant.id_mau_sac)
            });
        }

        // Thêm kích thước
        if (variant.id_kich_thuoc && !uniqueSizes.has(variant.id_kich_thuoc)) {
            uniqueSizes.set(variant.id_kich_thuoc, {
                ma: variant.id_kich_thuoc,
                ten: variant.gia_tri || `Size ${variant.id_kich_thuoc}`,
                co_san: variant.trang_thai === 1 || variant.trang_thai === true,
                trang_thai: variant.trang_thai
            });
        }
    });

    // Cập nhật danh sách màu sắc và kích thước
    product.value.mau_sac = Array.from(uniqueColors.values());
    product.value.kich_thuoc = Array.from(uniqueSizes.values());

    console.log('Màu sắc:', product.value.mau_sac);
    console.log('Kích thước:', product.value.kich_thuoc);

    // Không tự động chọn màu và kích thước đầu tiên làm mặc định
    selectedColor.value = null;
    selectedColorName.value = '';
    selectedSize.value = null;
    selectedSizeName.value = '';
};

// Thêm lại hàm lấy mã màu
const getColorCode = (colorId) => {
    const colorMap = {
        1: '#000000', // Đen
        2: '#FFFFFF', // Trắng
        6: '#FF0000', // Đỏ
        5: '#0000FF', // Xanh dương
        7: '#FFFF00', // Vàng
        10: '#FF66FF', // Xanh lá
        8: '#FFA500', // Cam
        11: '#800080', // Tím
        15: '#A52A2A', // Nâu
        3: '#808080', // Xám
        4: '#03204c', // Xanh Navy
        9: '#FF0099', // Hồng
        12: '#fecca7', //Be
        14: '#26ec08', //Xanh neon
        // Thêm các màu khác nếu cần
    };

    return colorMap[colorId] || '#CCCCCC';
};

// Cập nhật organizeImagesByColor để gộp tất cả ảnh vào một danh sách chung
const organizeImagesByColor = () => {
    // Khởi tạo map mới
    imagesByColor.value = new Map();
    const uniqueImages = new Map(); // Map<url, imageObject> để loại bỏ trùng lặp
    const imagesByColorAndPriority = new Map(); // Map<colorId, Array<imageObject>>

    // Duyệt qua tất cả variants để lấy hình ảnh
    productDetails.value.forEach(variant => {
        if (!variant.hinh_anh) return;

        // Xử lý hình ảnh dựa trên loại dữ liệu
        let imageUrls = [];

        if (Array.isArray(variant.hinh_anh)) {
            imageUrls = variant.hinh_anh;
        } else if (typeof variant.hinh_anh === 'string') {
            if (variant.hinh_anh.includes(',')) {
                // Nếu là chuỗi URL phân tách bằng dấu phẩy, tách thành mảng
                imageUrls = variant.hinh_anh.split(',').map(url => url.trim()).filter(url => url);
            } else {
                // Nếu chỉ là một URL đơn
                imageUrls = [variant.hinh_anh];
            }
        }

        // Xử lý từng ảnh
        imageUrls.forEach((url, index) => {
            // Kiểm tra ảnh đã tồn tại chưa (dựa vào URL)
            if (uniqueImages.has(url)) {
                const existing = uniqueImages.get(url);
                // Nếu ảnh này là ảnh đầu tiên (ảnh chính) và existing chưa phải ảnh chính, cập nhật
                if (index === 0 && !existing.isPrimary) {
                    existing.isPrimary = true;
                }
                return; // Bỏ qua ảnh trùng
            }

            // Tạo object ảnh mới
            const imageObj = {
                id: `${variant.id_chi_tiet_san_pham}_${index}`,
                url: url,
                alt: `${variant.ten_san_pham} - ${variant.ten_mau_sac || 'Màu'} - Hình ${index + 1}`,
                color_id: variant.id_mau_sac,
                color_name: variant.ten_mau_sac || `Màu ${variant.id_mau_sac}`,
                color_code: variant.ma_mau_sac || '#CCCCCC', // ✅ Sử dụng mã màu từ backend
                isPrimary: index === 0, // Ảnh đầu tiên trong danh sách là ảnh chính
                chi_tiet_san_pham_id: variant.id_chi_tiet_san_pham
            };

            // Lưu vào Map unique
            uniqueImages.set(url, imageObj);

            // Thêm vào map theo màu sắc
            if (variant.id_mau_sac) {
                if (!imagesByColorAndPriority.has(variant.id_mau_sac)) {
                    imagesByColorAndPriority.set(variant.id_mau_sac, []);
                }
                imagesByColorAndPriority.get(variant.id_mau_sac).push(imageObj);
            }
        });
    });

    // Sắp xếp ảnh theo màu (ảnh chính trước, sau đó theo id)
    imagesByColorAndPriority.forEach((images, colorId) => {
        const sorted = images.sort((a, b) => {
            // Ảnh chính lên đầu
            if (a.isPrimary && !b.isPrimary) return -1;
            if (!a.isPrimary && b.isPrimary) return 1;
            // Nếu cùng isPrimary, giữ nguyên thứ tự
            return 0;
        });
        imagesByColor.value.set(colorId, sorted);
    });

    // Tạo danh sách tất cả ảnh theo thứ tự: màu đầu tiên trước, ảnh chính lên đầu
    allImages.value = [];

    // Lấy màu đầu tiên trong danh sách màu sắc
    const firstColorId = product.value.mau_sac?.[0]?.ma;

    // Thêm ảnh của màu đầu tiên trước (nếu có)
    if (firstColorId && imagesByColor.value.has(firstColorId)) {
        allImages.value.push(...imagesByColor.value.get(firstColorId));
    }

    // Thêm ảnh của các màu khác
    imagesByColor.value.forEach((images, colorId) => {
        if (colorId !== firstColorId) {
            allImages.value.push(...images);
        }
    });

    // Cập nhật hình ảnh cho sản phẩm - sử dụng tất cả hình ảnh
    product.value.hinh_anh = allImages.value;

    console.log('✅ Hình ảnh theo màu sắc:', imagesByColor.value);
    console.log('✅ Tổng số ảnh unique:', allImages.value.length);
    console.log('✅ Chi tiết ảnh đầu tiên:', allImages.value[0]);
};

// Cập nhật hàm cập nhật thông tin sản phẩm từ variant để KHÔNG thay đổi danh sách hình ảnh
const updateProductFromVariant = (variant) => {
    if (!variant) return;

    // Cập nhật thông tin cơ bản
    product.value.ma_san_pham = variant.ma_san_pham;
    product.value.ten_san_pham = variant.ten_san_pham;
    product.value.thuong_hieu = variant.ten_thuong_hieu;
    product.value.danh_muc = variant.ten_danh_muc;
    product.value.chat_lieu = variant.ten_chat_lieu;

    // DEBUG: Log giá từ backend
    console.log('=== GIÁ TỪ VARIANT ===');
    console.log('giaGoc:', variant.giaGoc);
    console.log('giaHienTai:', variant.giaHienTai);
    console.log('kieuKhuyenMai:', variant.kieuKhuyenMai);
    console.log('giaTriKhuyenMai:', variant.giaTriKhuyenMai);

    // Cập nhật giá
    // giaGoc = giá gốc ban đầu
    // giaHienTai = giá HIỆN TẠI (đã áp dụng khuyến mãi nếu có)
    product.value.gia_goc = variant.giaGoc || 0;
    product.value.gia_khuyen_mai = variant.giaHienTai || variant.giaGoc || 0;

    // Kiểm tra nếu có khuyến mãi (giaHienTai < giaGoc)
    if (variant.giaGoc && variant.giaHienTai && variant.giaHienTai < variant.giaGoc) {
        product.value.giam_gia = true;
        // Tính phần trăm giảm giá từ 2 giá
        product.value.phan_tram_giam_gia = Math.round(((variant.giaGoc - variant.giaHienTai) / variant.giaGoc) * 100);
    } else if (variant.kieuKhuyenMai && variant.giaTriKhuyenMai) {
        // Fallback: nếu có kieuKhuyenMai và giaTriKhuyenMai
        product.value.giam_gia = true;
        product.value.phan_tram_giam_gia = variant.giaTriKhuyenMai;
    } else {
        product.value.giam_gia = false;
        product.value.phan_tram_giam_gia = 0;
    }

    console.log('=== GIÁ SAU KHI CẬP NHẬT ===');
    console.log('gia_goc:', product.value.gia_goc);
    console.log('gia_khuyen_mai:', product.value.gia_khuyen_mai);
    console.log('giam_gia:', product.value.giam_gia);
    console.log('phan_tram_giam_gia:', product.value.phan_tram_giam_gia);


    // Cập nhật đánh giá
    product.value.danh_gia = variant.danh_gia_trung_binh || 0;
    product.value.so_luot_danh_gia = variant.so_luong_danh_gia || 0;

    // Cập nhật số lượng
    product.value.so_luong = variant.so_luong || 0;

    // Cập nhật ngày tạo và tính toán trường moi
    if (variant.ngay_tao) {
        product.value.ngay_tao = variant.ngay_tao;

        // Tính khoảng cách giữa ngày hiện tại và ngày tạo (múi giờ +7 Việt Nam)
        const ngayTao = new Date(variant.ngay_tao);

        // Tạo ngày hiện tại theo múi giờ +7 (Việt Nam)
        const now = new Date();
        const utc = now.getTime() + (now.getTimezoneOffset() * 60000);
        const ngayHienTai = new Date(utc + (7 * 60 * 60 * 1000));

        const soNgayChenhLech = Math.floor((ngayHienTai - ngayTao) / (1000 * 60 * 60 * 24));

        // Nếu < 10 ngày thì sản phẩm là mới
        product.value.moi = soNgayChenhLech < 10;

        console.log('Ngày tạo:', variant.ngay_tao);
        console.log('Ngày hiện tại (múi giờ +7):', ngayHienTai.toISOString());
        console.log('Số ngày chênh lệch:', soNgayChenhLech, 'Là sản phẩm mới:', product.value.moi);
    } else {
        product.value.moi = false;
    }

    // Cập nhật trạng thái
    product.value.trang_thai = variant.trang_thai;

    // KHÔNG cập nhật danh sách hình ảnh, chỉ thay đổi hình ảnh hiện tại
    findAndShowFirstImageOfColor(variant.id_mau_sac);

    console.log('Đã cập nhật sản phẩm:', product.value);
    product.value.mo_ta = variant.mo_ta || '';
};

// Hàm mới để tìm và hiển thị ảnh đầu tiên của màu được chọn
const findAndShowFirstImageOfColor = (colorId) => {
    // Kiểm tra xem có ảnh cho màu này không
    if (!imagesByColor.value.has(colorId)) {
        console.log('❌ Không tìm thấy ảnh cho màu:', colorId);
        return;
    }

    const imagesForColor = imagesByColor.value.get(colorId);
    if (imagesForColor.length === 0) {
        console.log('❌ Danh sách ảnh rỗng cho màu:', colorId);
        return;
    }

    // Lấy ảnh đầu tiên (đã được sắp xếp, ảnh chính ở đầu)
    const firstImage = imagesForColor[0];

    // Tìm index của ảnh này trong danh sách tất cả ảnh
    const firstImageIndex = allImages.value.findIndex(img => img.url === firstImage.url);

    if (firstImageIndex !== -1 && firstImageIndex !== currentImageIndex.value) {
        // Thêm một trì hoãn nhỏ để tạo hiệu ứng mượt hơn
        setTimeout(() => {
            currentImageIndex.value = firstImageIndex;
        }, 100);
        console.log('✅ Chuyển đến ảnh của màu', colorId, '(', firstImage.color_name, ')');
        console.log('✅ Vị trí ảnh:', firstImageIndex);
        console.log('✅ Là ảnh chính:', firstImage.isPrimary);
    }
};

// Cập nhật hàm chọn màu sắc để lưu cả id và tên
const selectColor = (color) => {
    selectedColor.value = color.ma;
    selectedColorName.value = color.ten;

    // Tìm và hiển thị ảnh đầu tiên của màu được chọn
    findAndShowFirstImageOfColor(color.ma);

    // Luôn reset size selection khi chọn màu mới
    selectedSize.value = null;
    selectedSizeName.value = '';

    // Tìm variant phù hợp với màu đã chọn (không dùng kích thước vì đã reset)
    const variantWithColor = productDetails.value.find(variant => variant.id_mau_sac === color.ma);
    if (variantWithColor) {
        selectedVariant.value = variantWithColor;
        updateProductFromVariant(variantWithColor);
        console.log('Đã tìm variant với màu:', variantWithColor);
    }
};

// Sửa hàm selectSize để kiểm tra đơn giản hơn
const selectSize = (size) => {
    // Kiểm tra size có available không
    if (!size.co_san) {
        console.log('Size này không khả dụng:', size.ten);
        notification.warning({
            message: 'Kích thước không khả dụng',
            description: `Size ${size.ten} hiện tạm hết hàng.`,
            placement: 'topRight',
            duration: 3,
            style: { zIndex: 1500 }
        });
        return;
    }

    selectedSize.value = size.ma;
    selectedSizeName.value = size.ten;

    // Tìm variant phù hợp với kích thước đã chọn và màu hiện tại
    updateSelectedVariant();
};

// Cập nhật hàm updateSelectedVariant để xử lý tốt hơn
const updateSelectedVariant = () => {
    // Tìm variant phù hợp với màu và kích thước đã chọn
    const matchedVariant = productDetails.value.find(variant =>
        variant.id_mau_sac === selectedColor.value &&
        variant.id_kich_thuoc === selectedSize.value
    );

    if (matchedVariant) {
        selectedVariant.value = matchedVariant;
        updateProductFromVariant(matchedVariant);

        // Fetch reviews for the new selected variant
        if (matchedVariant.id_chi_tiet_san_pham) {
            fetchProductReviews(matchedVariant.id_chi_tiet_san_pham);
        }

        console.log('Đã tìm thấy variant phù hợp:', matchedVariant);
    } else {
        console.log('Không tìm thấy variant phù hợp với màu:', selectedColor.value, 'và kích thước:', selectedSize.value);
        // Tìm variant chỉ với màu đã chọn
        const variantWithColor = productDetails.value.find(variant => variant.id_mau_sac === selectedColor.value);
        if (variantWithColor) {
            selectedVariant.value = variantWithColor;
            updateProductFromVariant(variantWithColor);

            // Fetch reviews for the variant with matching color
            if (variantWithColor.id_chi_tiet_san_pham) {
                fetchProductReviews(variantWithColor.id_chi_tiet_san_pham);
            }

            console.log('Đã tìm variant với màu:', variantWithColor);
        }
    }
};

// Cập nhật canAddToCart để kiểm tra kỹ hơn về trạng thái
const canAddToCart = computed(() => {
    if (!selectedColor.value || !selectedSize.value) {
        return false;
    }

    // Kiểm tra có biến thể phù hợp không
    const matchedVariant = productDetails.value.find(variant =>
        variant.id_mau_sac === selectedColor.value &&
        variant.id_kich_thuoc === selectedSize.value
    );

    // Kiểm tra đầy đủ cả trạng thái và số lượng
    return matchedVariant &&
        (matchedVariant.trang_thai === 1 || matchedVariant.trang_thai === true) &&
        matchedVariant.so_luong > 0;
});

// Thêm hàm để lấy số lượng còn lại của variant hiện tại
const getCurrentVariantStock = () => {
    const matchedVariant = productDetails.value.find(variant =>
        variant.id_mau_sac === selectedColor.value &&
        variant.id_kich_thuoc === selectedSize.value
    );

    return matchedVariant ? matchedVariant.so_luong : 0;
};

// Giới hạn số lượng tối đa có thể chọn
const maxAvailableQuantity = computed(() => {
    return getCurrentVariantStock();
});

// Cập nhật hàm tăng số lượng để kiểm tra số lượng tồn kho
const increaseQuantity = () => {
    // Kiểm tra đã chọn màu và kích thước chưa
    if (!selectedColor.value && !selectedSize.value) {
        notification.warning({
            message: 'Thông tin sản phẩm chưa đầy đủ',
            description: 'Vui lòng chọn màu sắc và kích thước trước khi thay đổi số lượng',
            placement: 'topRight',
            duration: 3,
            style: {
                zIndex: 1500
            }
        });
        return;
    }

    if (!selectedColor.value) {
        notification.warning({
            message: 'Chưa chọn màu sắc',
            description: 'Vui lòng chọn màu sắc trước khi thay đổi số lượng',
            placement: 'topRight',
            duration: 3,
            style: {
                zIndex: 1500
            }
        });
        return;
    }

    if (!selectedSize.value) {
        notification.warning({
            message: 'Chưa chọn kích thước',
            description: 'Vui lòng chọn kích thước trước khi thay đổi số lượng',
            placement: 'topRight',
            duration: 3,
            style: {
                zIndex: 1500
            }
        });
        return;
    }

    // Tìm variant phù hợp để lấy số lượng tối đa
    const matchedVariant = productDetails.value.find(variant =>
        variant.id_mau_sac === selectedColor.value &&
        variant.id_kich_thuoc === selectedSize.value
    );

    if (!matchedVariant) {
        notification.warning({
            message: 'Không có sản phẩm phù hợp',
            description: 'Sản phẩm với màu sắc và kích thước này không có sẵn',
            placement: 'topRight',
            duration: 3,
            style: {
                zIndex: 1500
            }
        });
        return;
    }

    // Kiểm tra số lượng tồn kho
    if (quantity.value < matchedVariant.so_luong) {
        quantity.value++;
    } else {
        notification.warning({
            message: 'Giới hạn số lượng',
            description: `Chỉ còn ${matchedVariant.so_luong} sản phẩm trong kho`,
            placement: 'topRight',
            duration: 3,
            style: {
                zIndex: 1500
            }
        });
    }
};

// lềnh thêm mới
watch(selectedVariant, () => {
    if (selectedVariant.value) {
        checkWishlistStatus();
    }
});

// Theo dõi thay đổi của ID sản phẩm
watch(productId, (newId) => {
    if (newId) {
        fetchProductDetail(newId, route);
    }
}, { immediate: true });

// State cho gallery
const currentImageIndex = ref(0);
const showFullscreen = ref(false);
const zoomActive = ref(false);
const zoomPosition = ref({ x: 0, y: 0 });
const zoomVisible = ref(false);
// thêm dòng mới lềnh
const isFavoritedVariant = ref(false);

// Tính toán hình ảnh hiện tại
const currentImage = computed(() => {
    if (product.value.hinh_anh && product.value.hinh_anh.length > 0) {
        return product.value.hinh_anh[currentImageIndex.value].url;
    }
    return '';
});

// State cho thông tin sản phẩm
const selectedColor = ref(null);
const selectedColorName = ref(null);
const selectedSize = ref(null);
const selectedSizeName = ref(null);
const quantity = ref(1);
const isInWishlist = ref(false);

// State cho tabs
const activeTab = ref('description');

// Tính toán phần trăm giảm giá
const discountPercent = computed(() => {
    if (product.value.giam_gia && product.value.gia_goc > 0 && product.value.gia_khuyen_mai > 0) {
        return Math.round(((product.value.gia_goc - product.value.gia_khuyen_mai) / product.value.gia_goc) * 100);
    }
    return 0;
});

// Tính toán trạng thái tồn kho
const stockStatus = computed(() => {
    if (product.value.so_luong <= 0) {
        return { text: 'Hết hàng', class: 'out-of-stock' };
    } else if (product.value.so_luong < 10) {
        return { text: `Còn ${product.value.so_luong} sản phẩm`, class: 'low-stock' };
    } else {
        return { text: `Có ${product.value.so_luong} sản phẩm có sẵn`, class: '', check: true };
    }
});

// Xử lý chuyển ảnh
const changeImage = (index) => {
    // If clicking the same image, do nothing
    if (currentImageIndex.value === index) return;

    // Set slide direction based on index comparison
    slideDirection.value = index > currentImageIndex.value ? 'slide-right' : 'slide-left';

    // Delay the actual transition for smoother effect
    setTimeout(() => {
        currentImageIndex.value = index;
    }, 80);
};

const nextImage = () => {
    if (product.value.hinh_anh && product.value.hinh_anh.length > 0) {
        slideDirection.value = 'slide-right';
        // Update all slide classes before changing index
        setTimeout(() => {
            currentImageIndex.value = (currentImageIndex.value + 1) % product.value.hinh_anh.length;
        }, 50);
    }
};

const prevImage = () => {
    if (product.value.hinh_anh && product.value.hinh_anh.length > 0) {
        slideDirection.value = 'slide-left';
        // Update all slide classes before changing index
        setTimeout(() => {
            currentImageIndex.value = (currentImageIndex.value - 1 + product.value.hinh_anh.length) % product.value.hinh_anh.length;
        }, 50);
    }
};

// Xử lý zoom ảnh
const handleMouseMove = (event) => {
    if (!zoomActive.value) return;

    // Dừng slideshow khi đang zoom
    stopSlideshow();

    const imageContainer = event.currentTarget;
    const { left, top, width, height } = imageContainer.getBoundingClientRect();

    const x = (event.clientX - left) / width;
    const y = (event.clientY - top) / height;

    zoomPosition.value = { x, y };

    // Hiển thị kết quả zoom
    const zoomResult = document.querySelector('.zoom-result');
    if (zoomResult && currentImage.value) {
        zoomResult.style.backgroundImage = `url(${currentImage.value})`;
        zoomResult.style.backgroundPosition = `${x * 100}% ${y * 100}%`;
        zoomResult.style.backgroundSize = '300%';
    }
};

const handleMouseEnter = () => {
    zoomActive.value = true;
    zoomVisible.value = true;
    // Dừng slideshow khi hover lên ảnh
    stopSlideshow();
};

const handleMouseLeave = () => {
    zoomActive.value = false;
    zoomVisible.value = false;
    // Bắt đầu lại slideshow khi di chuột ra khỏi ảnh
    startSlideshow();
};

// Xử lý số lượng
const decreaseQuantity = () => {
    if (quantity.value > 1) {
        quantity.value--;
    }
};

// Xử lý thêm vào giỏ hàng
const addToCart = (product) => {
    // Tạo đối tượng sản phẩm để thêm vào giỏ hàng
    const cartItem = {
        id: product.id_chi_tiet_san_pham || product.id_san_pham,
        name: product.ten_san_pham || product.ten,
        image: product.hinh_anh,
        price: product.gia_khuyen_mai || product.gia_ban,
        originalPrice: product.gia_goc,
        quantity: 1,
        maxQuantity: product.so_luong || 10, // Số lượng tối đa có thể mua
        color: product.ten_mau_sac || '',
        size: product.gia_tri || ''
    };

    // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
    const existingItemIndex = cartItems.value.findIndex(item => item.id === cartItem.id);

    if (existingItemIndex >= 0) {
        // Nếu sản phẩm đã có trong giỏ hàng, tăng số lượng
        if (cartItems.value[existingItemIndex].quantity < cartItems.value[existingItemIndex].maxQuantity) {
            cartItems.value[existingItemIndex].quantity += 1;
            showSuccessMessage('Đã tăng số lượng sản phẩm trong giỏ hàng!');

            // Gán sản phẩm vừa thêm
            lastAddedProduct.value = { ...cartItems.value[existingItemIndex] };
        } else {
            showWarningMessage('Số lượng sản phẩm đã đạt mức tối đa!');
            return;
        }
    } else {
        // Nếu sản phẩm chưa có trong giỏ hàng, thêm mới
        cartItems.value.push(cartItem);
        showSuccessMessage('Đã thêm sản phẩm vào giỏ hàng!');

        // Gán sản phẩm vừa thêm
        lastAddedProduct.value = { ...cartItem };
    }

    // Lưu giỏ hàng vào localStorage
    saveCartToLocalStorage();

    // Hiển thị modal thông báo
    cartNotification.value = true;

    // Tự động đóng modal sau 5 giây
    setTimeout(() => {
        cartNotification.value = false;
    }, 5000);
};

// Xử lý thêm vào giỏ hàng từ trang chi tiết sản phẩm
const addToCartFromDetail = async () => {
    // Kiểm tra đã chọn màu và kích thước chưa
    if (!selectedColor.value && !selectedSize.value) {
        notification.warning({
            message: 'Thông tin sản phẩm chưa đầy đủ',
            description: 'Vui lòng chọn màu sắc và kích thước',
            placement: 'topRight',
            duration: 3,
            style: { zIndex: 1500 }
        });
        return;
    }

    if (!selectedColor.value) {
        notification.warning({
            message: 'Chưa chọn màu sắc',
            description: 'Vui lòng chọn màu sắc cho sản phẩm',
            placement: 'topRight',
            duration: 3,
            style: { zIndex: 1500 }
        });
        return;
    }

    if (!selectedSize.value) {
        notification.warning({
            message: 'Chưa chọn kích thước',
            description: 'Vui lòng chọn kích thước cho sản phẩm',
            placement: 'topRight',
            duration: 3,
            style: { zIndex: 1500 }
        });
        return;
    }

    // Tìm variant phù hợp
    const matchedVariant = productDetails.value.find(variant =>
        variant.id_mau_sac === selectedColor.value &&
        variant.id_kich_thuoc === selectedSize.value
    );

    if (!matchedVariant) {
        notification.warning({
            message: 'Không có sản phẩm phù hợp',
            description: 'Sản phẩm với màu sắc và kích thước này không có sẵn',
            placement: 'topRight',
            duration: 3,
            style: { zIndex: 1500 }
        });
        return;
    }

    try {
        // Lưu id chi tiết sản phẩm để sử dụng
        const idChiTietSanPham = matchedVariant.id_chi_tiet_san_pham;

        // 1. Kiểm tra trạng thái của sản phẩm qua API
        await store.getTrangThaiCTSP(idChiTietSanPham);
        const isActive = store.trangThaiCTSP;

        if (!isActive) {
            notification.warning({
                message: 'Sản phẩm không khả dụng',
                description: 'Sản phẩm này hiện không hoạt động',
                placement: 'topRight',
                duration: 3,
                style: { zIndex: 1500 }
            });
            return;
        }

        // 2. Kiểm tra số lượng tối đa - cần gọi API để lấy maxSoLuongSP mới nhất
        await store.getMaxSoLuongSP(idChiTietSanPham);
        const maxQuantity = store.maxSoLuongSP;

        if (maxQuantity <= 0) {
            notification.warning({
                message: 'Hết hàng',
                description: 'Sản phẩm này hiện đã hết hàng',
                placement: 'topRight',
                duration: 3,
                style: { zIndex: 1500 }
            });
            return;
        }

        // Sau khi đã validate xong và có selectedVariant
        selectedVariant.value = matchedVariant;

        // Tạo style cho modal với z-index cao
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

        // Hiển thị modal xác nhận trước
        Modal.confirm({
            title: 'Xác nhận thêm vào giỏ hàng',
            content: 'Bạn có muốn thêm sản phẩm này vào giỏ hàng không?',
            okText: 'Đồng ý',
            cancelText: 'Hủy',
            zIndex: 9999, // Đặt z-index cao hơn
            onOk: async () => {
                // ✅ FIXED: Kiểm tra CẢ 2 loại đăng nhập
                console.log("=== DEBUG: Thực thi addToCartFromDetail ===");

                // Check customer login (từ login() trong gbStore.js line 1750-1754)
                const customerDataStr = sessionStorage.getItem('khachHang') || localStorage.getItem('khachHang');

                // Check admin/staff login (từ loginNV() trong gbStore.js line 1819-1821)
                const adminDataStr = sessionStorage.getItem('userDetails') || localStorage.getItem('userDetails');

                let idKhachHang = null;
                let userType = '';

                if (customerDataStr) {
                    try {
                        const customerData = JSON.parse(customerDataStr);
                        idKhachHang = customerData.idKhachHang;
                        userType = 'CUSTOMER';
                    } catch (e) {
                        console.error('Error parsing khachHang:', e);
                    }
                } else if (adminDataStr) {
                    try {
                        const adminData = JSON.parse(adminDataStr);
                        idKhachHang = adminData.idKhachHang;
                        userType = 'ADMIN';
                    } catch (e) {
                        console.error('Error parsing userDetails:', e);
                    }
                }

                console.log(`User type: ${userType}, ID khách hàng: ${idKhachHang}`);

                if (idKhachHang) {
                    try {
                        // ✅ Đã đăng nhập - Thêm vào database
                        console.log(`✅ [${userType}] Thêm vào giỏ hàng database:`, { idKhachHang, idChiTietSanPham, quantity: quantity.value });

                        // Lấy giỏ hàng hiện tại của khách hàng
                        const cartResponse = await banHangOnlineService.getGioHang(idKhachHang);

                        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
                        const existingCartItem = cartResponse && Array.isArray(cartResponse)
                            ? cartResponse.find(item => item.id_chi_tiet_san_pham === idChiTietSanPham)
                            : null;

                        // Kiểm tra điều kiện số lượng
                        const currentQuantityInCart = existingCartItem ? existingCartItem.so_luong : 0;
                        const totalQuantity = currentQuantityInCart + quantity.value;

                        // Kiểm tra nếu tổng số lượng vượt quá số lượng tối đa cho phép
                        if (maxQuantity !== undefined && totalQuantity > maxQuantity) {
                            notification.warning({
                                message: 'Vượt quá số lượng cho phép',
                                description: `Số lượng tối đa có thể đặt là ${maxQuantity} sản phẩm. Hiện tại đã có ${currentQuantityInCart} sản phẩm trong giỏ hàng.`,
                                placement: 'topRight',
                                duration: 3,
                                style: { zIndex: 1500 }
                            });
                            return;
                        }

                        // Gọi API thêm vào giỏ hàng
                        await store.getGioHangByIdKH(
                            idKhachHang,
                            idChiTietSanPham,
                            quantity.value
                        );

                        // ✅ THÊM: Dispatch event để sync header cart count
                        window.dispatchEvent(new CustomEvent('cart-updated', {
                            detail: {
                                action: 'item_added_from_detail',
                                userType: userType,
                                itemId: idChiTietSanPham,
                                quantity: quantity.value
                            }
                        }));

                        notification.success({
                            message: 'Thêm vào giỏ hàng',
                            description: `Đã thêm sản phẩm vào giỏ hàng tài khoản`,
                            placement: 'topRight',
                        });

                        console.log(`✅ [${userType}] Thêm vào giỏ thành công!`);
                    } catch (error) {
                        console.error('❌ Lỗi khi thêm vào giỏ hàng:', error);
                        notification.error({
                            message: 'Lỗi',
                            description: 'Có lỗi xảy ra khi thêm vào giỏ hàng',
                            placement: 'topRight',
                            duration: 3,
                            style: { zIndex: 1500 }
                        });
                    }
                } else {
                    // Logic cho người dùng chưa đăng nhập - kiểm tra localStorage
                    const savedCart = localStorage.getItem('gb-sport-cart');
                    let cartItems = [];

                    if (savedCart) {
                        try {
                            cartItems = JSON.parse(savedCart);
                        } catch (error) {
                            console.error('Lỗi khi đọc giỏ hàng từ localStorage:', error);
                            cartItems = [];
                        }
                    }

                    // Kiểm tra sản phẩm đã tồn tại trong giỏ hàng chưa
                    const existingItemIndex = cartItems.findIndex(item => item.id === idChiTietSanPham);

                    // Kiểm tra điều kiện số lượng
                    const currentQuantityInCart = existingItemIndex >= 0 ? cartItems[existingItemIndex].quantity : 0;
                    const totalQuantity = currentQuantityInCart + quantity.value;

                    // Kiểm tra nếu tổng số lượng vượt quá số lượng tối đa cho phép
                    if (maxQuantity !== undefined && totalQuantity > maxQuantity) {
                        notification.warning({
                            message: 'Vượt quá số lượng cho phép',
                            description: `Số lượng tối đa có thể đặt là ${maxQuantity} sản phẩm. Hiện tại đã có ${currentQuantityInCart} sản phẩm trong giỏ hàng.`,
                            placement: 'topRight',
                            duration: 3,
                            style: { zIndex: 1500 }
                        });
                        return;
                    }

                    const cartItem = {
                        id: idChiTietSanPham,
                        name: product.value.ten_san_pham,
                        image: product.value.hinh_anh[0]?.url || '',
                        price: product.value.gia_khuyen_mai || product.value.gia_ban_hien_tai,
                        originalPrice: product.value.gia_goc,
                        quantity: quantity.value,
                        maxQuantity: maxQuantity,
                        color: selectedColorName.value,
                        size: selectedSizeName.value,
                        trang_thai: matchedVariant.trang_thai
                    };

                    // Cập nhật hoặc thêm mới sản phẩm vào giỏ hàng
                    if (existingItemIndex >= 0) {
                        // Nếu sản phẩm đã có trong giỏ hàng, tăng số lượng
                        cartItems[existingItemIndex].quantity += quantity.value;
                        // Cập nhật lại giá trị maxQuantity cho sản phẩm
                        cartItems[existingItemIndex].maxQuantity = maxQuantity;
                    } else {
                        // Nếu sản phẩm chưa có, thêm mới vào giỏ hàng
                        cartItems.push(cartItem);
                    }

                    // Lưu giỏ hàng vào localStorage
                    localStorage.setItem('gb-sport-cart', JSON.stringify(cartItems));

                    // Cập nhật số lượng sau khi thêm vào localStorage
                    await updateCartCount();

                    notification.success({
                        message: 'Thêm vào giỏ hàng',
                        description: 'Đã thêm sản phẩm vào giỏ hàng thành công',
                        placement: 'topRight',
                        duration: 3,
                        style: { zIndex: 1500 }
                    });
                }
            }
        });
    } catch (error) {
        console.error('Lỗi khi xử lý thêm vào giỏ hàng:', error);
        notification.error({
            message: 'Lỗi',
            description: 'Có lỗi xảy ra khi xử lý thêm vào giỏ hàng',
            placement: 'topRight',
            duration: 3,
            style: { zIndex: 1500 }
        });
    }
};

// Sửa lại hàm mua ngay để hiển thị thông báo cụ thể
const buyNow = () => {
    // Kiểm tra đã chọn màu và kích thước chưa
    if (!selectedColor.value && !selectedSize.value) {
        notification.warning({
            message: 'Thông tin sản phẩm chưa đầy đủ',
            description: 'Vui lòng chọn màu sắc và kích thước',
            placement: 'topRight',
            duration: 3,
            style: {
                zIndex: 1500
            }
        });
        return;
    }

    if (!selectedColor.value) {
        notification.warning({
            message: 'Chưa chọn màu sắc',
            description: 'Vui lòng chọn màu sắc cho sản phẩm',
            placement: 'topRight',
            duration: 3,
            style: {
                zIndex: 1500
            }
        });
        return;
    }

    if (!selectedSize.value) {
        notification.warning({
            message: 'Chưa chọn kích thước',
            description: 'Vui lòng chọn kích thước cho sản phẩm',
            placement: 'topRight',
            duration: 3,
            style: {
                zIndex: 1500
            }
        });
        return;
    }

    // Tìm variant phù hợp
    const matchedVariant = productDetails.value.find(variant =>
        variant.id_mau_sac === selectedColor.value &&
        variant.id_kich_thuoc === selectedSize.value
    );

    if (!matchedVariant) {
        notification.warning({
            message: 'Không có sản phẩm phù hợp',
            description: 'Sản phẩm với màu sắc và kích thước này không có sẵn',
            placement: 'topRight',
            duration: 3,
            style: {
                zIndex: 1500
            }
        });
        return;
    }

    // Kiểm tra trạng thái của sản phẩm
    if (!matchedVariant.trang_thai && matchedVariant.trang_thai !== 1) {
        notification.warning({
            message: 'Sản phẩm không khả dụng',
            description: 'Sản phẩm này hiện không có sẵn để bán',
            placement: 'topRight',
            duration: 3,
            style: {
                zIndex: 1500
            }
        });
        return;
    }

    // Kiểm tra số lượng
    if (matchedVariant.so_luong <= 0) {
        notification.warning({
            message: 'Hết hàng',
            description: 'Sản phẩm này hiện đã hết hàng',
            placement: 'topRight',
            duration: 3,
            style: {
                zIndex: 1500
            }
        });
        return;
    }

    // Kiểm tra số lượng còn lại
    if (quantity.value > matchedVariant.so_luong) {
        notification.warning({
            message: 'Vượt quá số lượng trong kho',
            description: `Chỉ còn ${matchedVariant.so_luong} sản phẩm trong kho`,
            placement: 'topRight',
            duration: 3,
            style: {
                zIndex: 1500
            }
        });
        return;
    }

    // Nếu đã qua hết các kiểm tra, tiếp tục với variant đã tìm thấy
    selectedVariant.value = matchedVariant;

    // Xử lý mua ngay
    console.log('Mua ngay:', {
        product_id: productId.value,
        variant_id: selectedVariant.value.id_chi_tiet_san_pham,
        color: selectedColorName.value,
        size: selectedSizeName.value,
        quantity: quantity.value,
        price: product.value.gia_khuyen_mai || product.value.gia_ban_hien_tai,
        originalPrice: product.value.gia_goc
    });

    // Lấy URL hình ảnh an toàn
    let imageUrl = '';
    if (product.value) {
        // Kiểm tra nếu hinh_anh là string (trường hợp 1 ảnh)
        if (typeof product.value.hinh_anh === 'string') {
            imageUrl = product.value.hinh_anh;
        }
        // Kiểm tra nếu hinh_anh là mảng (trường hợp nhiều ảnh)
        else if (Array.isArray(product.value.hinh_anh) && product.value.hinh_anh.length > 0) {
            if (product.value.hinh_anh[currentImageIndex.value]?.url) {
                imageUrl = product.value.hinh_anh[currentImageIndex.value].url;
            } else {
                // Nếu phần tử hiện tại không có URL, thử lấy URL trực tiếp
                imageUrl = product.value.hinh_anh[currentImageIndex.value] || product.value.hinh_anh[0] || '';
            }
        }
        // Nếu không có ảnh, dùng ảnh mặc định
        if (!imageUrl) {
            imageUrl = 'http://res.cloudinary.com/dtwsqkqpc/image/upload/v1742823877/oionww3qsqhfwvuvxeko.jpg';
        }
    }

    const checkoutItem = {
        id: selectedVariant.value.id_chi_tiet_san_pham,
        product_id: productId.value,
        ten_san_pham: product.value.ten_san_pham,
        hinh_anh: imageUrl,
        ten_mau_sac: selectedColorName.value,
        ten_kich_thuoc: selectedSizeName.value,
        gia: product.value.gia_khuyen_mai || product.value.gia_ban_hien_tai,
        so_luong: quantity.value,
        original_price: product.value.gia_goc
    };

    // Lưu vào store để trang thanh toán có thể sử dụng
    store.setCheckoutItems([checkoutItem]);
    store.setIsThanhToanMuaNgay(true);
    // Chuyển hướng đến trang thanh toán
    router.push('/thanhtoan-banhang');
};

// Hiển thị thông báo thành công
const showSuccessMessage = (message) => {
    if (window.antd && window.antd.message) {
        window.antd.message.success(message);
    } else {
        alert(message);
    }
};

// Hiển thị thông báo cảnh báo
const showWarningMessage = (message) => {
    if (window.antd && window.antd.message) {
        window.antd.message.warning(message);
    } else {
        alert(message);
    }
};

// Tải giỏ hàng từ localStorage
const loadCartFromLocalStorage = () => {
    const savedCart = localStorage.getItem('gb-sport-cart');
    if (savedCart) {
        try {
            const parsedCart = JSON.parse(savedCart);
            console.log('Đã tải giỏ hàng từ localStorage:', parsedCart);

            // Đảm bảo các giá trị số là số
            cartItems.value = parsedCart.map(item => ({
                ...item,
                price: Number(item.price || 0),
                quantity: Number(item.quantity || 1),
                maxQuantity: Number(item.maxQuantity || 10),
                originalPrice: Number(item.originalPrice || 0)
            }));
        } catch (error) {
            console.error('Lỗi khi tải giỏ hàng:', error);
            cartItems.value = [];
        }
    }
};

// Lưu giỏ hàng vào localStorage
const saveCartToLocalStorage = () => {
    // Đảm bảo tất cả các giá trị số được lưu dưới dạng số
    const cartToSave = cartItems.value.map(item => ({
        ...item,
        price: Number(item.price || 0),
        quantity: Number(item.quantity || 1),
        maxQuantity: Number(item.maxQuantity || 10),
        originalPrice: Number(item.originalPrice || 0)
    }));

    console.log('Lưu giỏ hàng vào localStorage:', cartToSave);
    localStorage.setItem('gb-sport-cart', JSON.stringify(cartToSave));

    // Phát ra sự kiện custom để cập nhật giỏ hàng
    window.dispatchEvent(new CustomEvent('cart-updated'));
};

// Tải giỏ hàng khi component được tạo
onMounted(() => {
    // Lấy thông tin sản phẩm khi component được mount
    if (productId.value) {
        fetchProductDetail(productId.value, route).then(() => {
            // Sau khi lấy thông tin sản phẩm thành công, kiểm tra trạng thái yêu thích
            if (selectedVariant.value) {
                checkWishlistStatus();
            }
        });
    }

    // Tải giỏ hàng từ localStorage
    loadCartFromLocalStorage();

    // Thiết lập hiệu ứng scroll animation
    setupScrollAnimation(productGalleryRef);
    setupScrollAnimation(productInfoRef, 100);
    setupScrollAnimation(productTabsRef, 200);
    setupScrollAnimation(recommendedProductsRef, 300);

    // Thêm event listener để cập nhật dữ liệu khi tab được focus lại
    window.addEventListener('focus', handleTabFocusRefresh);
    document.addEventListener('visibilitychange', handleVisibilityChange);

    // Cập nhật itemsPerSlide dựa trên kích thước màn hình
    const updateItemsPerSlide = () => {
        if (window.innerWidth <= 576) {
            itemsPerSlide.value = 1;
        } else if (window.innerWidth <= 768) {
            itemsPerSlide.value = 2;
        } else if (window.innerWidth <= 992) {
            itemsPerSlide.value = 3;
        } else if (window.innerWidth <= 1200) {
            itemsPerSlide.value = 4;
        } else {
            itemsPerSlide.value = 5;
        }
    };

    updateItemsPerSlide();
    window.addEventListener('resize', updateItemsPerSlide);
    // Cấu hình thông báo để nổi bật hơn
    message.config({
        top: '80px',
        duration: 3,
        maxCount: 3,
        rtl: false,
    });

    // Khởi tạo slideshow khi component mounted
    startSlideshow();
});

// Dọn dẹp event listeners khi component bị hủy
onBeforeUnmount(() => {
    stopSlideshow();
    window.removeEventListener('focus', handleTabFocusRefresh);
    document.removeEventListener('visibilitychange', handleVisibilityChange);
});

// Biến để theo dõi thời gian cuối cùng refresh
const lastRefreshTime = ref(Date.now());
const REFRESH_THRESHOLD = 30000; // Chỉ refresh lại nếu đã qua 30 giây

// Hàm xử lý khi tab được focus lại
const handleTabFocusRefresh = () => {
    const currentTime = Date.now();
    // Chỉ refresh nếu đã qua ngưỡng thời gian
    if (currentTime - lastRefreshTime.value > REFRESH_THRESHOLD && productId.value) {
        console.log('Tab được focus lại - Refreshing product data...');
        lastRefreshTime.value = currentTime;

        // Lưu lại giá trị color và size hiện tại
        const currentColor = selectedColor.value;
        const currentSize = selectedSize.value;

        // Refresh dữ liệu sản phẩm
        fetchProductDetail(productId.value, route).then(() => {
            // Sau khi refresh, kiểm tra lại trạng thái của sản phẩm
            if (currentColor && currentSize) {
                const refreshedVariant = productDetails.value.find(v =>
                    v.id_mau_sac === currentColor && v.id_kich_thuoc === currentSize);

                if (refreshedVariant) {
                    // Nếu còn tồn tại, kiểm tra có thay đổi không
                    if ((!refreshedVariant.trang_thai && refreshedVariant.trang_thai !== 1) || refreshedVariant.so_luong <= 0) {
                        notification.warning({
                            message: 'Sản phẩm đã thay đổi',
                            description: 'Trạng thái hoặc số lượng sản phẩm đã thay đổi. Vui lòng kiểm tra lại.',
                            placement: 'topRight',
                            duration: 5
                        });
                    }
                } else {
                    // Nếu không tìm thấy variant đã chọn
                    notification.warning({
                        message: 'Sản phẩm không khả dụng',
                        description: 'Phiên bản sản phẩm bạn đã chọn không còn khả dụng.',
                        placement: 'topRight',
                        duration: 5
                    });
                    // Reset selection
                    selectedColor.value = null;
                    selectedSize.value = null;
                }
            }

            // Cập nhật lại trạng thái yêu thích
            if (selectedVariant.value) {
                checkWishlistStatus();
            }
        });

        // Cập nhật số lượng giỏ hàng
        updateCartCount();
    }
};

// lềnh thêm mới
watch(selectedVariant, () => {
    if (selectedVariant.value) {
        checkWishlistStatus();
    }
});

// Kiểm tra trạng thái yêu thích từ server và localStorage
const checkWishlistStatus = async () => {
    try {
        if (!store.userDetails || !store.userDetails.idKhachHang || !selectedVariant.value) {
            isInWishlist.value = false;
            return;
        }

        const idKhachHang = store.userDetails.idKhachHang;
        const idChiTietSanPham = selectedVariant.value.id_chi_tiet_san_pham;

        const response = await favoriteService.checkFavoriteStatus(idKhachHang, idChiTietSanPham);
        if (response.status === 'success') {
            isInWishlist.value = response.isFavorite;
            product.value.so_luot_yeu_thich = response.totalFavorites;
            saveWishlistToLocalStorage(idChiTietSanPham, response.isFavorite);
        }
    } catch (error) {
        console.error('Lỗi khi kiểm tra trạng thái yêu thích:', error);
        // Fallback to localStorage
        if (selectedVariant.value) {
            const wishlist = JSON.parse(localStorage.getItem('gb-sport-wishlist') || '[]');
            isInWishlist.value = wishlist.includes(selectedVariant.value.id_chi_tiet_san_pham);
        }
    }
};

// Hàm xử lý khi visibility thay đổi (chuyển tab)
const handleVisibilityChange = () => {
    if (document.visibilityState === 'visible') {
        handleTabFocusRefresh();
    }
};

// Xử lý chuyển tab
const changeTab = (tab) => {
    activeTab.value = tab;
};

// State cho carousel sản phẩm đề xuất
const currentSlide = ref(0);
const itemsPerSlide = ref(5);
const totalSlides = computed(() => {
    if (product.value.san_pham_lien_quan && product.value.san_pham_lien_quan.length > 0) {
        return Math.ceil(product.value.san_pham_lien_quan.length / itemsPerSlide.value);
    }
    return 0;
});

// Xử lý chuyển slide
const nextSlide = () => {
    if (currentSlide.value < totalSlides.value - 1) {
        currentSlide.value++;
    } else {
        currentSlide.value = 0;
    }
};

const prevSlide = () => {
    if (currentSlide.value > 0) {
        currentSlide.value--;
    } else {
        currentSlide.value = totalSlides.value - 1;
    }
};

// Tính toán các sản phẩm hiển thị trong slide hiện tại
const visibleProducts = computed(() => {
    if (product.value.san_pham_lien_quan && product.value.san_pham_lien_quan.length > 0) {
        const start = currentSlide.value * itemsPerSlide.value;
        const end = start + itemsPerSlide.value;
        return product.value.san_pham_lien_quan.slice(start, end);
    }
    return [];
});

// Định dạng tiền tệ
const formatCurrency = (value) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

// Hiệu ứng scroll animation
const productGalleryRef = ref(null);
const productInfoRef = ref(null);
const productTabsRef = ref(null);
const recommendedProductsRef = ref(null);

const setupScrollAnimation = (target, delay = 0) => {
    const { stop } = useIntersectionObserver(
        target,
        ([{ isIntersecting }]) => {
            if (isIntersecting) {
                if (target.value) {
                    target.value.style.opacity = '1';
                    target.value.style.transform = 'translateY(0)';
                }
                stop();
            }
        },
        { threshold: 0.1 }
    );
};

// Add new ref for tracking slide direction in the script section
const slideDirection = ref('slide-right');

// Add the preload function in the script section
// Helper function to preload next and previous images
const getPreloadImages = () => {
    if (!product.value.hinh_anh || product.value.hinh_anh.length <= 1) return [];

    const preloadUrls = [];
    const currentIdx = currentImageIndex.value;
    const totalImages = product.value.hinh_anh.length;

    // Add next image
    const nextIdx = (currentIdx + 1) % totalImages;
    preloadUrls.push(product.value.hinh_anh[nextIdx].url);

    // Add previous image
    const prevIdx = (currentIdx - 1 + totalImages) % totalImages;
    preloadUrls.push(product.value.hinh_anh[prevIdx].url);

    return preloadUrls;
};

// Add helper functions in the script section to determine previous and next images
// Helper functions to determine previous and next images
const isPreviousImage = (index) => {
    if (slideDirection.value === 'slide-right') {
        return index === ((currentImageIndex.value - 1 + product.value.hinh_anh.length) % product.value.hinh_anh.length);
    }
    return false;
};

const isNextImage = (index) => {
    if (slideDirection.value === 'slide-left') {
        return index === ((currentImageIndex.value + 1) % product.value.hinh_anh.length);
    }
    return false;
};

// Giỏ hàng
const cartItems = ref([]);
const cartCount = computed(() => {
    return cartItems.value.reduce((total, item) => total + item.quantity, 0);
});

// Modal notificación carrito
const cartNotification = ref(false);
const lastAddedProduct = ref(null);

// Tính tổng tiền của giỏ hàng
const cartTotal = computed(() => {
    return cartItems.value.reduce((total, item) => total + (item.price * item.quantity), 0);
});

// Chuyển đến trang giỏ hàng
const viewCart = () => {
    router.push('/giohang-banhang');
};

// Thêm phương thức tăng số lượt yêu thích
const increaseFavoriteCount = (isAdded, newCount) => {
    // Update the product's favorite count to the new value from the API
    if (newCount !== undefined) {
        if (!product.value.so_luot_yeu_thich) {
            product.value.so_luot_yeu_thich = 0;
        }
        product.value.so_luot_yeu_thich = newCount;
        console.log('Updated product favorite count to:', newCount);
    }
};

// Thêm phương thức để chuyển đến trang chi tiết sản phẩm
const viewProduct = (product) => {
    if (product && product.id) {
        router.push(`/sanpham/${product.id}`);
    }
};

// Lưu danh sách yêu thích vào localStorage
const saveWishlistToLocalStorage = (productId, isFavorite) => {
    // Lưu danh sách ID sản phẩm yêu thích
    const wishlist = JSON.parse(localStorage.getItem('gb-sport-wishlist') || '[]');
    if (isFavorite) {
        if (!wishlist.includes(productId)) {
            wishlist.push(productId);
        }
    } else {
        const index = wishlist.indexOf(productId);
        if (index !== -1) {
            wishlist.splice(index, 1);
        }
    }
    localStorage.setItem('gb-sport-wishlist', JSON.stringify(wishlist));

    // Lưu số lượt yêu thích của sản phẩm hiện tại
    const productFavorites = JSON.parse(localStorage.getItem('gb-sport-product-favorites') || '{}');
    productFavorites[productId] = product.value.so_luot_yeu_thich;
    localStorage.setItem('gb-sport-product-favorites', JSON.stringify(productFavorites));

    console.log('Đã lưu số lượt yêu thích:', productFavorites[productId], 'cho sản phẩm ID:', productId);
};

// Thêm hàm fetchProductReviews để lấy đánh giá sản phẩm
const fetchProductReviews = async (idChiTietSanPham) => {
    try {
        const reviewData = await reviewService.getProductReviews(idChiTietSanPham);

        if (!reviewData.error) {
            // Update product with review data
            product.value.danh_gia = reviewData.averageRating;
            product.value.so_luot_danh_gia = reviewData.totalReviews;

            // Update the detailed rating information
            product.value.danh_gia_chi_tiet = {
                trung_binh: reviewData.averageRating,
                tong_so: reviewData.totalReviews,
                chi_tiet: {
                    5: { phan_tram: reviewData.ratingDistribution[5] || 0 },
                    4: { phan_tram: reviewData.ratingDistribution[4] || 0 },
                    3: { phan_tram: reviewData.ratingDistribution[3] || 0 },
                    2: { phan_tram: reviewData.ratingDistribution[2] || 0 },
                    1: { phan_tram: reviewData.ratingDistribution[1] || 0 }
                },
                binh_luan: reviewData.reviews || []
            };

            console.log('Đã tải đánh giá sản phẩm:', product.value.danh_gia_chi_tiet);
        } else {
            console.error('Lỗi khi tải đánh giá sản phẩm');
            product.value.danh_gia_chi_tiet = {
                trung_binh: 0,
                tong_so: 0,
                chi_tiet: {
                    5: { phan_tram: 0 },
                    4: { phan_tram: 0 },
                    3: { phan_tram: 0 },
                    2: { phan_tram: 0 },
                    1: { phan_tram: 0 }
                },
                binh_luan: []
            };
        }
    } catch (error) {
        console.error('Lỗi khi tải đánh giá sản phẩm:', error);
    }
};

// Thêm hàm kiểm tra xem đánh giá có phải của người dùng hiện tại không
const isOwnReview = (review) => {
    if (!store.userDetails || !store.userDetails.idKhachHang) return false;

    // Kiểm tra nếu review có id_khach_hang trực tiếp
    if (review.id_khach_hang) {
        return review.id_khach_hang === store.userDetails.idKhachHang;
    }

    // Hoặc kiểm tra từ id kết hợp (nếu id có dạng "{idKhachHang}-{idChiTietSanPham}")
    if (review.id && typeof review.id === 'string') {
        const parts = review.id.split('-');
        if (parts.length > 0) {
            const reviewUserId = parseInt(parts[0]);
            return reviewUserId === store.userDetails.idKhachHang;
        }
    }

    return false;
};

// Modal cho chỉnh sửa đánh giá
const editReviewVisible = ref(false);
const editingReview = ref(null);
const editReviewForm = ref({
    rating: 5,
    content: ''
});

// Mở modal chỉnh sửa đánh giá
const editReview = (review) => {
    editingReview.value = review;
    editReviewForm.value = {
        rating: review.danh_gia,
        content: review.noi_dung
    };
    editReviewVisible.value = true;
};

// Lưu đánh giá đã chỉnh sửa
const saveEditedReview = async () => {
    try {
        if (!editingReview.value || !selectedVariant.value) return;

        const reviewId = editingReview.value.id;

        // Kiểm tra dữ liệu trước khi gửi
        if (!editReviewForm.value.content.trim() || !editReviewForm.value.rating) {
            message.warning('Vui lòng điền đầy đủ nội dung đánh giá và xếp hạng');
            return;
        }

        // Kiểm tra trạng thái da_chinh_sua
        if (editingReview.value.da_chinh_sua === 1) {
            message.warning('Bình luận này đã được chỉnh sửa trước đó. Hệ thống chỉ cho phép chỉnh sửa một lần.');
            return;
        }

        // Chuẩn bị dữ liệu đánh giá
        const reviewData = {
            id: reviewId,
            id_chi_tiet_san_pham: selectedVariant.value.id_chi_tiet_san_pham,
            id_khach_hang: store.userDetails.idKhachHang,
            danh_gia: editReviewForm.value.rating,
            binh_luan: editReviewForm.value.content,
            ngay_cap_nhat: new Date().toISOString(),
            da_chinh_sua: 1
        };

        // Hiển thị modal xác nhận trước khi lưu
        const antd = await import('ant-design-vue');
        antd.Modal.confirm({
            title: 'Xác nhận chỉnh sửa',
            content: 'Bạn có chắc chắn muốn lưu các chỉnh sửa này?',
            okText: 'Xác nhận',
            cancelText: 'Hủy',
            centered: true,
            async onOk() {
                // Hiển thị loading khi đang cập nhật
                const loadingKey = 'editReviewLoading';
                message.loading({ content: 'Đang cập nhật đánh giá...', key: loadingKey, duration: 0 });

                try {
                    const response = await reviewService.updateProductReview(reviewId, reviewData);
                    if (response && !response.error) {
                        message.success({ content: 'Đã cập nhật đánh giá thành công', key: loadingKey });
                        editReviewVisible.value = false;
                        editingReview.value = null;
                        editReviewForm.value = { rating: 5, content: '' };
                        await fetchProductReviews(selectedVariant.value.id_chi_tiet_san_pham);
                    } else {
                        message.error({ content: response?.message || 'Không thể cập nhật đánh giá', key: loadingKey });
                    }
                } catch (error) {
                    console.error('Lỗi khi cập nhật đánh giá:', error);
                    message.error('Đã xảy ra lỗi khi cập nhật đánh giá');
                }
            }
        });
    } catch (error) {
        console.error('Lỗi:', error);
        message.error('Đã xảy ra lỗi không mong muốn');
    }
};

// Hủy chỉnh sửa đánh giá
const cancelEditReview = () => {
    editReviewVisible.value = false;
    editingReview.value = null; // Đảm bảo reset giá trị
    editReviewForm.value = { rating: 5, content: '' }; // Reset form
};

// Xác nhận trước khi xóa đánh giá
const confirmDeleteReview = (review) => {
    if (window.antd && window.antd.Modal) {
        window.antd.Modal.confirm({
            title: 'Xác nhận xóa',
            content: 'Bạn có chắc chắn muốn xóa đánh giá này?',
            okText: 'Xóa',
            okType: 'danger',
            cancelText: 'Hủy',
            onOk() {
                deleteReview(review);
            }
        });
    } else {
        if (confirm('Bạn có chắc chắn muốn xóa đánh giá này?')) {
            deleteReview(review);
        }
    }
};

// Xóa đánh giá
const deleteReview = async (review) => {
    try {
        if (!review || !review.id || !selectedVariant.value) return;

        const response = await reviewService.deleteProductReview(review.id);

        if (response && !response.error) {
            message.success('Đã xóa đánh giá thành công');

            // Cập nhật lại danh sách đánh giá
            await fetchProductReviews(selectedVariant.value.id_chi_tiet_san_pham);
        } else {
            message.error('Không thể xóa đánh giá');
        }
    } catch (error) {
        console.error('Lỗi khi xóa đánh giá:', error);
        message.error('Đã xảy ra lỗi khi xóa đánh giá');
    }
};

// Thêm phương thức mở modal thêm bình luận
const openAddReviewModal = () => {
    if (!store.userDetails || !store.userDetails.idKhachHang) {
        message.warning('Vui lòng đăng nhập để thêm bình luận');
        return;
    }

    // Mở modal thêm bình luận
    addReviewVisible.value = true;
    newReviewForm.value = { rating: 5, content: '' }; // Reset form
};

// Thêm phương thức điều hướng đến trang đăng nhập
const navigateToLogin = () => {
    router.push('/dangnhap');
};

// Tính toán trạng thái có thể thêm bình luận
const canAddReview = computed(() => {
    return store.userDetails && store.userDetails.idKhachHang;
});

// Modal cho thêm bình luận mới
const addReviewVisible = ref(false);
const newReviewForm = ref({
    rating: 5,
    content: ''
});

// Hủy thêm bình luận mới
const cancelAddReview = () => {
    addReviewVisible.value = false;
    newReviewForm.value = { rating: 5, content: '' }; // Reset form
};

// Gửi bình luận mới
const submitNewReview = async () => {
    try {
        if (!selectedVariant.value) return;

        // Kiểm tra dữ liệu trước khi gửi
        if (!newReviewForm.value.content.trim() || !newReviewForm.value.rating) {
            message.warning('Vui lòng điền đầy đủ nội dung bình luận và xếp hạng');
            return;
        }

        // Debug
        console.log("Đánh giá chi tiết:", product.value.danh_gia_chi_tiet);
        console.log("User:", store.userDetails);

        // Kiểm tra xem người dùng đã bình luận sản phẩm này chưa
        const hasReviewed = checkIfUserAlreadyReviewed();
        console.log("Đã bình luận:", hasReviewed);

        const canReview = await reviewService.checkCanReviewProduct(
            store.userDetails.idKhachHang,
            selectedVariant.value.id_chi_tiet_san_pham
        )

        if (hasReviewed) {
            const antd = await import('ant-design-vue');
            antd.Modal.error({
                title: 'Không thể bình luận',
                content: 'Bạn đã bình luận sản phẩm này rồi. Mỗi người dùng chỉ có thể bình luận một lần.',
                okText: 'Đã hiểu',
                centered: true
            });
            addReviewVisible.value = false;
            return;
        }

        // Kiểm tra xem người dùng có thể bình luận hay không
        if (!canReview) {
            const antd = await import('ant-design-vue');
            antd.Modal.error({
                title: 'Không thể bình luận',
                content: 'Bạn không thể bình luận sản phẩm này. Vì bạn chưa mua sản phẩm này hoặc đơn hàng chưa giao thành công.',
                okText: 'Đã hiểu',
                centered: true
            });
            addReviewVisible.value = false;
            return;
        }

        // Chuẩn bị dữ liệu bình luận
        const reviewData = {
            id_chi_tiet_san_pham: selectedVariant.value.id_chi_tiet_san_pham,
            id_khach_hang: store.userDetails.idKhachHang,
            danh_gia: newReviewForm.value.rating,
            binh_luan: newReviewForm.value.content,
            ngay_tao: new Date().toISOString()
        };

        // Xác nhận
        const antd = await import('ant-design-vue');
        antd.Modal.confirm({
            title: 'Xác nhận đánh giá',
            content: 'Bạn có chắc chắn muốn gửi đánh giá này?',
            okText: 'Xác nhận',
            cancelText: 'Hủy',
            centered: true,
            async onOk() {
                const loadingKey = 'addReviewLoading';
                message.loading({ content: 'Đang gửi bình luận...', key: loadingKey, duration: 0 });

                try {
                    const response = await reviewService.addProductReview(reviewData);
                    if (response && !response.error) {
                        message.success({ content: 'Đã gửi bình luận thành công', key: loadingKey });
                        addReviewVisible.value = false;
                        newReviewForm.value = { rating: 5, content: '' };
                        await fetchProductReviews(selectedVariant.value.id_chi_tiet_san_pham);
                    } else {
                        message.destroy(loadingKey);
                        antd.Modal.error({
                            title: 'Lỗi',
                            content: response?.message || 'Không thể gửi bình luận',
                            okText: 'Đã hiểu',
                            centered: true
                        });
                    }
                } catch (error) {
                    console.error('Lỗi khi gửi bình luận:', error);
                    antd.Modal.error({
                        title: 'Lỗi',
                        content: 'Đã xảy ra lỗi khi gửi bình luận',
                        okText: 'Đã hiểu',
                        centered: true
                    });
                }
            }
        });
    } catch (error) {
        console.error('Lỗi:', error);
        message.error('Đã xảy ra lỗi không mong muốn');
    }
};

// Kiểm tra xem người dùng đã bình luận sản phẩm này chưa
const checkIfUserAlreadyReviewed = () => {
    // Kiểm tra nếu danh sách bình luận không tồn tại hoặc rỗng
    if (!product.value.danh_gia_chi_tiet ||
        !product.value.danh_gia_chi_tiet.binh_luan ||
        product.value.danh_gia_chi_tiet.binh_luan.length === 0) {
        return false;
    }

    // Kiểm tra nếu không có thông tin người dùng
    if (!store.userDetails || !store.userDetails.idKhachHang) {
        return false;
    }

    // Tìm bình luận của người dùng hiện tại
    const userReview = product.value.danh_gia_chi_tiet.binh_luan.find(review =>
        review.id_khach_hang === store.userDetails.idKhachHang
    );

    return !!userReview; // Trả về true nếu tìm thấy bình luận của người dùng
}
// Thêm computed property để lọc size dựa trên màu sắc đã chọn
const availableSizes = computed(() => {
    // Nếu chưa chọn màu, hiển thị tất cả size
    if (!selectedColor.value) {
        return product.value.kich_thuoc;
    }

    // Nếu đã chọn màu, lọc các size có trong màu đó
    const sizesForSelectedColor = [];
    const sizeMap = new Map(); // Map: id_kich_thuoc -> { hasAvailable, variants }

    // Bước 1: Tổng hợp tất cả variants theo size cho màu đã chọn
    productDetails.value.forEach(variant => {
        if (variant.id_mau_sac === selectedColor.value) {
            if (!sizeMap.has(variant.id_kich_thuoc)) {
                sizeMap.set(variant.id_kich_thuoc, {
                    hasAvailable: false,
                    variants: []
                });
            }

            const sizeData = sizeMap.get(variant.id_kich_thuoc);
            sizeData.variants.push(variant);

            // Kiểm tra xem variant này có available không
            if ((variant.trang_thai === 1 || variant.trang_thai === true) && variant.so_luong > 0) {
                sizeData.hasAvailable = true;
            }
        }
    });

    // Bước 2: Tạo danh sách size với trạng thái đúng
    sizeMap.forEach((sizeData, sizeId) => {
        const sizeInfo = product.value.kich_thuoc.find(size => size.ma === sizeId);

        if (sizeInfo) {
            sizesForSelectedColor.push({
                ma: sizeInfo.ma,
                ten: sizeInfo.ten,
                co_san: sizeData.hasAvailable,
                trang_thai: sizeData.hasAvailable ? 1 : 0
            });
        }
    });

    console.log('Các size có sẵn cho màu', selectedColorName.value, ':', sizesForSelectedColor);
    return sizesForSelectedColor;
});

// Biến lưu trữ interval cho slideshow
const slideshowInterval = ref(null);
const slideshowDelay = 3000; // 3 giây cho mỗi slide

// Bắt đầu slideshow
const startSlideshow = () => {
    // Chỉ chạy slideshow khi có nhiều hơn 1 ảnh
    if (!product.value.hinh_anh || product.value.hinh_anh.length <= 1) return;

    // Xóa interval cũ nếu có
    stopSlideshow();

    // Tạo interval mới
    slideshowInterval.value = setInterval(() => {
        // Tăng chỉ số ảnh hiện tại, quay lại 0 nếu đã đến ảnh cuối
        if (currentImageIndex.value < product.value.hinh_anh.length - 1) {
            currentImageIndex.value++;
        } else {
            currentImageIndex.value = 0;
        }
    }, slideshowDelay);
};

// Dừng slideshow
const stopSlideshow = () => {
    if (slideshowInterval.value) {
        clearInterval(slideshowInterval.value);
        slideshowInterval.value = null;
    }
};

// Dọn dẹp khi component unmounted
onBeforeUnmount(() => {
    stopSlideshow();
});

// Thêm hàm validate giá bán
const validateGiaBan = (value) => {
    const numValue = parseFloat(String(value).replace(/,/g, ''));

    if (isNaN(numValue) || numValue === null) {
        return false;
    }

    if (numValue < 1000) {
        return false;
    }

    if (numValue > 100000000) {
        return false;
    }

    return true;
};

const handleGiaBanInput = (value) => {
    // Convert value to number for validation
    const numValue = parseFloat(String(value).replace(/,/g, ''));

    if (!isNaN(numValue)) {
        if (validateGiaBan(numValue)) {
            product.value.gia_goc = numValue;
            product.value.gia_khuyen_mai = numValue;
        } else {
            // If invalid, reset to minimum valid price
            product.value.gia_goc = 1000;
            product.value.gia_khuyen_mai = 1000;
        }
    }
};

// Add event handler for blur event
const handleGiaBanBlur = (e) => {
    const inputValue = e.target.value;
    if (!inputValue) {
        product.value.gia_goc = 1000;
        product.value.gia_khuyen_mai = 1000;
        return;
    }

    const numValue = parseFloat(String(inputValue).replace(/,/g, ''));
    if (!validateGiaBan(numValue)) {
        product.value.gia_goc = 1000;
        product.value.gia_khuyen_mai = 1000;
    }
};

const showAddedToCartModal = ref(false);

const handleModalOk = () => {
    showAddedToCartModal.value = false;
};

const handleModalCancel = () => {
    showAddedToCartModal.value = false;
};

// Theo dõi khi giỏ hàng thay đổi
watch(() => store.cartUpdated, () => {
    updateCartCount();
});

// Gọi khi component được mount
onMounted(() => {
    updateCartCount();
});
// Phần sản phẩm liên quan

// Dữ liệu sản phẩm tương tự (bạn thay bằng dữ liệu thực tế)
const recProducts = ref([
    // Ví dụ mẫu, bạn thay bằng dữ liệu thực tế
    // {
    //   id: 1,
    //   image: 'link_ảnh',
    //   price: '200.000₫',
    //   oldPrice: '250.000₫',
    //   discountPercent: 20,
    //   discount: '-20%',
    //   name: 'Áo thể thao nam',
    //   brand: 'Nike',
    //   rating: 4.5,
    //   reviews: 12,
    //   description: 'Áo thể thao chất liệu tốt...'
    // }
]);

// Số sản phẩm mỗi slide
const recProductsPerSlide = 5;

// Chia sản phẩm thành các slide
const recProductSlides = computed(() => {
    const slides = [];
    const products = [...recProducts.value];
    const totalNeeded = Math.ceil(products.length / recProductsPerSlide) * recProductsPerSlide;
    if (totalNeeded > products.length) {
        const extraNeeded = totalNeeded - products.length;
        const extraProducts = products.slice(0, extraNeeded).map(product => ({ ...product }));
        products.push(...extraProducts);
    }
    for (let i = 0; i < products.length; i += recProductsPerSlide) {
        slides.push(products.slice(i, i + recProductsPerSlide));
    }
    return slides;
});

// Carousel & trạng thái
const recCarousel = ref(null);
const showRecArrows = ref(false);
const activeRecProduct = ref(null);



// Sử dụng Intersection Observer để theo dõi khi phần tử xuấn hiện trong viewport
onMounted(async () => {
    // ✅ NEW: Listen for cart-updated events from other components
    window.addEventListener('cart-updated', async (event) => {
        console.log('🔔 [DETAIL] Received cart-updated event:', event.detail);
        // Refresh badge count from database/localStorage
        await updateCartCount();
    });

    await store.getSanPhamBySP('quần,áo');
    // Chuyển đổi dữ liệu từ API sang định dạng phù hợp với template
    if (store.listSanPhamBanHang && store.listSanPhamBanHang.length > 0) {
        bestSellingProducts.value = store.listSanPhamBanHang.map(item => ({
            id: item.id_san_pham || item.id,
            image: item.hinh_anh || 'http://res.cloudinary.com/dtwsqkqpc/image/upload/v1742823877/oionww3qsqhfwvuvxeko.jpg',
            price: `${item.gia_khuyen_mai_cao_nhat?.toLocaleString()}₫` || '0₫',
            oldPrice: item.gia_max > item.gia_khuyen_mai_cao_nhat ? `${item.gia_max.toLocaleString()}₫` : null,
            discountPercent: item.gia_max && item.gia_khuyen_mai_cao_nhat ?
                Math.round(((item.gia_max - item.gia_khuyen_mai_cao_nhat) / item.gia_max) * 100) : 0,
            discount: item.gia_max && item.gia_khuyen_mai_cao_nhat ?
                `-${Math.round(((item.gia_max - item.gia_khuyen_mai_cao_nhat) / item.gia_max) * 100)}%` : null,
            name: item.ten_san_pham || 'Sản phẩm không tên',
            brand: item.ten_thuong_hieu || 'Chưa có thương hiệu',
            rating: item.danh_gia || 0,
            reviews: item.so_luong_danh_gia || 0
        }));
        console.log('Dữ liệu quần đã chuyển đổi:', bestSellingProducts.value);
    } else {
        console.log('Không có dữ liệu quần từ API');
    }

    const { stop } = useIntersectionObserver(
        sectionRef,
        ([{ isIntersecting }]) => {
            if (isIntersecting) {
                isVisible.value = true;
                stop(); // Dừng quan sát sau khi đã hiển thị
            }
        },
        { threshold: 0.2 } // Hiển thị khi ít nhất 20% phần tử xuất hiện trong viewport
    );
});
const bestSellingProducts = ref([]);
// Modal xem nhanh
const recModalVisible = ref(false);
const selectedRecProduct = ref(null);

const prevRecSlide = () => {
    if (recCarousel.value) recCarousel.value.prev();
};
const nextRecSlide = () => {
    if (recCarousel.value) recCarousel.value.next();
};

const showRecProductDetail = (rec) => {
    selectedRecProduct.value = rec;
    recModalVisible.value = true;
};
const handleRecModalCancel = () => {
    recModalVisible.value = false;
    selectedRecProduct.value = null;
};

// Khai báo sectionRef
const sectionRef = ref(null);
// Hàm để thêm sản phẩm vào giỏ hàng (đổi tên từ addToCart thành addToCartDetail)
const addToCartDetail = async (idChiTietSanPham, quantityToAdd) => {
    try {
        console.log("=== DEBUG: Thực thi addToCartDetail ===");
        console.log("ID chi tiết sản phẩm:", idChiTietSanPham);
        console.log("Số lượng thêm vào:", quantityToAdd);

        // ✅ FIXED: Kiểm tra CẢ 2 loại đăng nhập
        // Check customer login (từ login() trong gbStore.js line 1750-1754)
        const customerDataStr = sessionStorage.getItem('khachHang') || localStorage.getItem('khachHang');

        // Check admin/staff login (từ loginNV() trong gbStore.js line 1819-1821)  
        const adminDataStr = sessionStorage.getItem('userInfo') || localStorage.getItem('userInfo');

        let idKhachHang = null;
        let userType = '';

        if (customerDataStr) {
            try {
                const customerData = JSON.parse(customerDataStr);
                idKhachHang = customerData.idKhachHang;
                userType = 'CUSTOMER';
            } catch (e) {
                console.error('Error parsing khachHang:', e);
            }
        } else if (adminDataStr) {
            try {
                const adminData = JSON.parse(adminDataStr);
                idKhachHang = adminData.id_khach_hang;
                userType = 'ADMIN';
            } catch (e) {
                console.error('Error parsing userInfo:', e);
            }
        }

        console.log(`User type: ${userType}, ID khách hàng: ${idKhachHang}`);

        if (idKhachHang) {
            // ✅ Đã đăng nhập - Thêm vào database qua API
            console.log(`✅ [${userType}] Thêm vào giỏ hàng database:`, { idKhachHang, idChiTietSanPham, quantityToAdd });

            const result = await store.getGioHangByIdKH(
                idKhachHang,
                idChiTietSanPham,
                quantityToAdd
            );
            console.log("Kết quả thêm vào giỏ hàng qua API:", result);

            // Cập nhật số lượng sản phẩm trong giỏ hàng (header)
            await updateCartCount();
            console.log("Đã cập nhật số lượng giỏ hàng");

            // Hiển thị thông báo thành công
            notification.success({
                message: 'Thêm vào giỏ hàng thành công',
                description: `Đã thêm ${quantityToAdd} sản phẩm vào giỏ hàng tài khoản`,
                placement: 'topRight',
                duration: 3
            });

            // Lưu thông tin sản phẩm vừa thêm vào để hiển thị thông báo sau khi refresh
            saveLastAddedProduct(idChiTietSanPham, quantityToAdd);
        } else {
            // Khách - Lưu vào localStorage
            console.log("Thêm vào giỏ hàng local");
            const savedCart = localStorage.getItem('gb-sport-cart');
            let cartItems = [];

            if (savedCart) {
                try {
                    cartItems = JSON.parse(savedCart);
                    console.log("Giỏ hàng hiện tại từ localStorage:", cartItems);
                } catch (error) {
                    console.error('Lỗi khi đọc giỏ hàng từ localStorage:', error);
                    cartItems = [];
                }
            }

            // Kiểm tra sản phẩm đã tồn tại trong giỏ hàng chưa
            const existingItemIndex = cartItems.findIndex(item =>
                item.id_chi_tiet_san_pham === idChiTietSanPham
            );
            console.log("Index của sản phẩm trong giỏ hàng (nếu có):", existingItemIndex);

            // Tìm variant để lấy thông tin chi tiết
            const variant = productDetails.value.find(v =>
                v.id_chi_tiet_san_pham === idChiTietSanPham
            );
            console.log("Thông tin variant được chọn:", variant);

            if (!variant) {
                throw new Error('Không tìm thấy thông tin sản phẩm');
            }

            // Tạo thông tin sản phẩm để lưu vào giỏ hàng
            const cartItem = {
                id_chi_tiet_san_pham: idChiTietSanPham,
                name: product.value.ten_san_pham,
                image: getProductImageUrl(),
                price: product.value.gia_khuyen_mai || product.value.gia_ban_hien_tai,
                originalPrice: product.value.gia_goc,
                quantity: quantityToAdd,
                maxQuantity: variant.so_luong,
                color: selectedColorName.value,
                size: selectedSizeName.value,
                color_id: selectedColor.value,
                size_id: selectedSize.value
            };
            console.log("Dữ liệu sản phẩm chuẩn bị thêm vào giỏ hàng:", cartItem);

            // Cập nhật hoặc thêm mới sản phẩm vào giỏ hàng
            if (existingItemIndex >= 0) {
                // Nếu sản phẩm đã có trong giỏ hàng, tăng số lượng
                console.log("Tăng số lượng sản phẩm đã có từ", cartItems[existingItemIndex].quantity, "lên", cartItems[existingItemIndex].quantity + quantityToAdd);
                cartItems[existingItemIndex].quantity += quantityToAdd;
                // Cập nhật lại giá trị maxQuantity cho sản phẩm
                cartItems[existingItemIndex].maxQuantity = variant.so_luong;
            } else {
                // Nếu sản phẩm chưa có, thêm mới vào giỏ hàng
                console.log("Thêm mới sản phẩm vào giỏ hàng");
                cartItems.push(cartItem);
            }

            // Lưu giỏ hàng vào localStorage
            console.log("Lưu giỏ hàng vào localStorage:", cartItems);
            localStorage.setItem('gb-sport-cart', JSON.stringify(cartItems));

            // Cập nhật số lượng trong giỏ hàng
            await updateCartCount();

            // Hiển thị thông báo thành công
            notification.success({
                message: 'Thêm vào giỏ hàng thành công',
                description: `Đã thêm ${quantityToAdd} sản phẩm vào giỏ hàng`,
                placement: 'topRight',
                duration: 3
            });

            // Lưu thông tin sản phẩm vừa thêm vào để hiển thị thông báo sau khi refresh
            saveLastAddedProduct(idChiTietSanPham, quantityToAdd);
        }

        // Trở về số lượng mặc định sau khi thêm vào giỏ hàng
        quantity.value = 1;
    } catch (error) {
        console.error('Lỗi khi thêm vào giỏ hàng:', error);
        notification.error({
            message: 'Thêm vào giỏ hàng thất bại',
            description: 'Đã xảy ra lỗi khi thêm sản phẩm vào giỏ hàng',
            placement: 'topRight',
            duration: 3
        });
    }
};

// Xử lý thêm vào danh sách yêu thích
const toggleWishlist = async () => {
    try {
        if (!store.userDetails || !store.userDetails.idKhachHang) {
            message.warning('Vui lòng đăng nhập để thêm sản phẩm vào danh sách yêu thích');
            return;
        }

        if (!selectedVariant.value) {
            message.warning('Vui lòng chọn biến thể sản phẩm trước khi thêm vào yêu thích');
            return;
        }

        const idKhachHang = store.userDetails.idKhachHang;
        const idChiTietSanPham = selectedVariant.value.id_chi_tiet_san_pham;
        const oldWishlistState = isInWishlist.value;

        // Optimistic update
        isInWishlist.value = !isInWishlist.value;

        try {
            let response;
            if (isInWishlist.value) {
                response = await favoriteService.addToFavorite(idKhachHang, idChiTietSanPham);
                if (response.status === 'success') {
                    message.success('Đã thêm vào danh sách yêu thích');
                    product.value.so_luot_yeu_thich = response.totalFavorites;
                    saveWishlistToLocalStorage(idChiTietSanPham, true);
                }
            } else {
                response = await favoriteService.removeFromFavorite(idKhachHang, idChiTietSanPham);
                if (response.status === 'success') {
                    message.success('Đã xóa khỏi danh sách yêu thích');
                    product.value.so_luot_yeu_thich = response.totalFavorites;
                    saveWishlistToLocalStorage(idChiTietSanPham, false);
                }
            }
        } catch (error) {
            console.error('Lỗi khi thao tác với danh sách yêu thích:', error);
            message.error(error.response?.data?.message || 'Có lỗi xảy ra khi thực hiện thao tác');
            isInWishlist.value = oldWishlistState; // Restore previous state
        }
    } catch (error) {
        console.error('Lỗi:', error);
        message.error('Đã xảy ra lỗi không mong muốn');
        isInWishlist.value = !isInWishlist.value; // Revert UI state
    }
};

// Thêm một computed property để tính số lượng hiển thị dựa vào trạng thái lựa chọn
const displayStockStatus = computed(() => {
    // Trường hợp chưa chọn màu
    if (!selectedColor.value) {
        return {
            check: false,
            text: 'Vui lòng chọn màu sắc'
        };
    }

    // Trường hợp đã chọn màu nhưng chưa chọn size
    if (selectedColor.value && !selectedSize.value) {
        // Tính tổng số lượng của tất cả biến thể có màu đã chọn
        const totalQuantityByColor = productDetails.value
            .filter(item => item.id_mau_sac === selectedColor.value)
            .reduce((total, item) => total + (item.so_luong || 0), 0);

        if (totalQuantityByColor <= 0) {
            return {
                check: false,
                text: 'Hết hàng'
            };
        } else if (totalQuantityByColor < 5) {
            return {
                check: false,
                text: `Còn ${totalQuantityByColor} sản phẩm có sẵn`
            };
        } else {
            return {
                check: true,
                text: `Có ${totalQuantityByColor} sản phẩm có sẵn`
            };
        }
    }

    // Trường hợp đã chọn cả màu và size
    if (selectedColor.value && selectedSize.value) {
        // Tìm biến thể phù hợp
        const selectedVariant = productDetails.value.find(
            item => item.id_mau_sac === selectedColor.value && item.id_kich_thuoc === selectedSize.value
        );

        if (!selectedVariant) {
            return {
                check: false,
                text: 'Không tìm thấy sản phẩm'
            };
        }

        const currentQuantity = selectedVariant.so_luong || 0;

        if (currentQuantity <= 0) {
            return {
                check: false,
                text: 'Hết hàng'
            };
        } else if (currentQuantity < 5) {
            return {
                check: false,
                text: `Còn ${currentQuantity} sản phẩm`
            };
        } else {
            return {
                check: true,
                text: `Còn ${currentQuantity} sản phẩm`
            };
        }
    }

    return {
        check: false,
        text: 'Vui lòng chọn kích thước'
    };
});

</script>

<style scoped>
/* Kiểu dáng chung */
.product-detail-container {
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
    color: #f80f0fe5;
    text-decoration: none;
}

.breadcrumb-item.active {
    color: #6c757d;
}

.product-main-content {
    display: flex;
    flex-wrap: wrap;
    gap: 30px;
    margin-bottom: 30px;
}

/* Kiểu dáng gallery */
.product-gallery {
    flex: 1;
    min-width: 300px;
    max-width: 600px;
    opacity: 0;
    transform: translateY(30px);
    transition: opacity 0.5s ease, transform 0.5s ease;
}

.main-image-container {
    position: relative;
    margin-bottom: 15px;
    border: 1px solid #eee;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.image-zoom-container {
    position: relative;
    width: 100%;
    height: 0;
    padding-bottom: 100%;
    overflow: hidden;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: box-shadow 0.3s ease;
    background-color: #f8f8f8;
    /* Add background color as fallback */
}

.main-product-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    transform-origin: center center;
    transition: transform 0.5s ease;
    backface-visibility: hidden;
    /* Prevent flickering */
}

.zoom-lens {
    position: absolute;
    border: 2px solid #fff;
    width: 100px;
    height: 100px;
    cursor: none;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
    pointer-events: none;
}

.zoom-result {
    position: absolute;
    top: 0;
    right: -320px;
    width: 300px;
    height: 300px;
    border: 1px solid #ddd;
    background-repeat: no-repeat;
    z-index: 100;
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.2);
    pointer-events: none;
}

@media (max-width: 992px) {
    .zoom-result {
        display: none !important;
    }
}

.image-controls {
    position: absolute;
    bottom: 15px;
    left: 0;
    right: 0;
    display: flex;
    justify-content: center;
    gap: 15px;
}

.control-btn {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: rgba(255, 255, 255, 0.8);
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.control-btn:hover {
    background-color: #fff;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
}

.control-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    transform: none;
}

.control-btn svg,
.control-btn .anticon {
    font-size: 18px;
    color: #333;
}

.thumbnails-container {
    display: flex;
    gap: 10px;
    overflow-x: auto;
    padding-bottom: 10px;
    scrollbar-width: thin;
    scrollbar-color: #007bff #f0f0f0;
}

.thumbnails-container::-webkit-scrollbar {
    height: 6px;
}

.thumbnails-container::-webkit-scrollbar-track {
    background: #f0f0f0;
    border-radius: 10px;
}

.thumbnails-container::-webkit-scrollbar-thumb {
    background-color: #007bff;
    border-radius: 10px;
}

.thumbnail-wrapper {
    width: 80px;
    height: 80px;
    border: 2px solid transparent;
    border-radius: 4px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    flex-shrink: 0;
    position: relative;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.thumbnail-wrapper.active {
    border-color: #007bff;
    transform: scale(1.08);
    box-shadow: 0 4px 8px rgba(0, 123, 255, 0.3);
    z-index: 1;
}

.thumbnail-wrapper:hover:not(.active) {
    transform: translateY(-4px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.thumbnail-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.color-indicator {
    position: absolute;
    top: 5px;
    right: 5px;
    width: 15px;
    height: 15px;
    border-radius: 50%;
    border: 1px solid #fff;
    box-shadow: 0 0 3px rgba(0, 0, 0, 0.3);
    z-index: 5;
}

/* Kiểu dáng modal xem toàn màn hình */
.fullscreen-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.9);
    z-index: 1050;
    display: flex;
    align-items: center;
    justify-content: center;
    animation: fadeIn 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.fullscreen-image {
    max-width: 100%;
    max-height: 80vh;
    object-fit: contain;
    animation: zoomIn 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    border-radius: 4px;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        backdrop-filter: blur(0);
    }

    to {
        opacity: 1;
        backdrop-filter: blur(5px);
    }
}

@keyframes zoomIn {
    from {
        transform: scale(0.9);
        opacity: 0;
    }

    to {
        transform: scale(1);
        opacity: 1;
    }
}

/* Nâng cao hiệu ứng khi hover lên hình ảnh chính */
.image-zoom-container {
    position: relative;
    width: 100%;
    height: 0;
    padding-bottom: 100%;
    overflow: hidden;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: box-shadow 0.3s ease;
}

.image-zoom-container:hover {
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.main-product-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
}

.image-zoom-container:hover .main-product-image:not(:active) {
    transform: scale(1.03);
}

/* Animation cho color indicator */
.color-indicator {
    position: absolute;
    top: 5px;
    right: 5px;
    width: 15px;
    height: 15px;
    border-radius: 50%;
    border: 1px solid #fff;
    box-shadow: 0 0 3px rgba(0, 0, 0, 0.3);
    z-index: 5;
    transition: all 0.3s ease;
}

.thumbnail-wrapper:hover .color-indicator {
    transform: scale(1.2);
}

.thumbnail-wrapper.active .color-indicator {
    transform: scale(1.3);
    box-shadow: 0 0 6px rgba(0, 0, 0, 0.4);
}

/* Animation cho color selection */
.color-option {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 5px;
    cursor: pointer;
    padding: 5px;
    border-radius: 4px;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    border: 2px solid transparent;
}

.color-option.active {
    border-color: #007bff;
    background-color: rgba(0, 123, 255, 0.05);
    transform: translateY(-2px) scale(1.05);
    box-shadow: 0 4px 8px rgba(0, 123, 255, 0.2);
}

.color-option:hover:not(.active) {
    transform: translateY(-4px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.color-swatch {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    border: 1px solid #ddd;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
}

.color-option:hover .color-swatch {
    transform: scale(1.1);
}

.color-option.active .color-swatch {
    transform: scale(1.15);
    box-shadow: 0 3px 6px rgba(0, 0, 0, 0.2);
}

/* Kiểu dáng phần thông tin sản phẩm */
.product-info {
    flex: 1;
    min-width: 300px;
    opacity: 0;
    transform: translateY(30px);
    transition: opacity 0.5s ease, transform 0.5s ease;
}

.product-badges {
    display: flex;
    gap: 10px;
    margin-bottom: 10px;
}

.badge {
    padding: 5px 10px;
    border-radius: 4px;
    font-size: 14px;
    font-weight: bold;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.badge-sale {
    background-color: #ff69b4;
    color: #fff;
}

.badge-new {
    background-color: #4CAF50;
    color: #fff;
}

.product-title {
    font-size: 28px;
    font-weight: bold;
    margin-bottom: 15px;
    color: #333;
    position: relative;
    padding-bottom: 10px;
}

.product-title::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 60px;
    height: 3px;
    background-color: #007bff;
}

.product-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    margin-bottom: 20px;
    background-color: #f9f9f9;
    padding: 15px;
    border-radius: 8px;
}

.meta-item {
    display: flex;
    align-items: center;
    gap: 5px;
}

.meta-label {
    font-weight: bold;
    color: #6c757d;
}

.meta-value {
    color: #333;
}

.product-price {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
}

.current-price {
    font-size: 28px;
    font-weight: bold;
    color: #ff5722;
}

.original-price {
    font-size: 18px;
    color: #6c757d;
    text-decoration: line-through;
}

.discount-percent {
    font-size: 18px;
    color: #ff69b4;
    font-weight: bold;
}

.product-rating {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
}

.stars {
    display: flex;
    gap: 5px;
    color: #ffc107;
}

.rating-count {
    font-size: 14px;
    color: #6c757d;
}

.product-colors {
    margin-bottom: 20px;
}

.option-title {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 10px;
    color: #333;
}

.color-options {
    display: flex;
    gap: 15px;
    flex-wrap: wrap;
}

.product-sizes {
    margin-bottom: 20px;
}

.size-options {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
}

.size-option {
    padding: 10px 20px;
    border: 1px solid #ddd;
    border-radius: 4px;
    cursor: pointer;
    background-color: #fff;
    transition: all 0.2s ease;
    font-weight: bold;
}

.size-option.active {
    background-color: #007bff;
    color: #fff;
    border-color: #007bff;
}

.size-option:hover:not(.active):not(.disabled) {
    border-color: #007bff;
    color: #007bff;
}

.size-option.disabled {
    opacity: 0.5;
    cursor: not-allowed;
    background-color: #f5f5f5;
    color: #999;
    position: relative;
}

.size-unavailable {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: #ff4d4f;
    font-weight: bold;
    font-size: 16px;
    z-index: 1;
}

.product-quantity {
    margin-bottom: 20px;
}

.quantity-selector {
    display: flex;
    align-items: center;
    gap: 10px;
}

.quantity-btn {
    width: 36px;
    height: 36px;
    border: 1px solid #ddd;
    border-radius: 50%;
    background-color: #fff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
}

.quantity-btn:hover:not(:disabled) {
    background-color: #007bff;
    color: #fff;
    border-color: #007bff;
}

.quantity-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.quantity-input {
    width: 60px;
    height: 36px;
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 5px 10px;
    font-size: 16px;
    text-align: center;
}

.stock-status {
    margin-left: 15px;
    font-size: 14px;
    color: #28a745;
}

.stock-status.low-stock {
    color: #ff9800;
}

.stock-status.out-of-stock {
    color: #f44336;
}

.product-actions {
    display: flex;
    gap: 15px;
    margin-bottom: 30px;
}

.btn-add-to-cart,
.btn-buy-now {
    padding: 12px 25px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 16px;
    font-weight: bold;
    display: flex;
    align-items: center;
    gap: 8px;
    transition: all 0.3s ease;
}

.btn-add-to-cart {
    background-color: #007bff;
    color: #fff;
    position: relative;
}

.btn-add-to-cart:hover:not(:disabled) {
    background-color: #0056b3;
    transform: translateY(-3px);
    box-shadow: 0 5px 15px rgba(0, 123, 255, 0.3);
}

.btn-buy-now {
    background-color: #ff5722;
    color: #fff;
}

.btn-buy-now:hover:not(:disabled) {
    background-color: #e64a19;
    transform: translateY(-3px);
    box-shadow: 0 5px 15px rgba(255, 87, 34, 0.3);
}

.btn-add-to-cart:disabled,
.btn-buy-now:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
    box-shadow: none;
}

.btn-add-to-wishlist {
    width: 45px;
    height: 45px;
    border: 1px solid #ddd;
    border-radius: 50%;
    background-color: #fff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
    box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
}

.btn-add-to-wishlist:hover {
    transform: translateY(-3px);
    box-shadow: 0 5px 15px rgba(255, 77, 79, 0.3);
    border-color: #ff4d4f;
}

.btn-add-to-wishlist:active {
    transform: translateY(0);
}

.heart-icon {
    font-size: 22px;
    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.heart-icon.filled {
    color: #ff4d4f;
    animation: heartBeat 0.3s ease-in-out;
}

.heart-icon.outlined {
    color: #777;
}

.btn-add-to-wishlist:hover .heart-icon.outlined {
    color: #ff4d4f;
}

@keyframes heartBeat {
    0% {
        transform: scale(0);
    }

    50% {
        transform: scale(1.2);
    }

    100% {
        transform: scale(1);
    }
}

.product-delivery-info {
    background-color: #f8f9fa;
    padding: 15px;
    border-radius: 8px;
    margin-bottom: 20px;
}

.delivery-item {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;
}

.delivery-item:last-child {
    margin-bottom: 0;
}

.delivery-item i {
    font-size: 18px;
    color: #007bff;
    width: 24px;
    text-align: center;
}

.delivery-item span {
    font-size: 14px;
    color: #333;
}

/* Hiệu ứng animation */
@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* Responsive */
@media (max-width: 992px) {
    .product-actions {
        flex-wrap: wrap;
    }

    .btn-add-to-cart,
    .btn-buy-now {
        flex: 1;
    }
}

@media (max-width: 576px) {
    .product-title {
        font-size: 24px;
    }

    .current-price {
        font-size: 24px;
    }

    .product-actions {
        flex-direction: column;
    }

    .btn-add-to-cart,
    .btn-buy-now {
        width: 100%;
        padding: 10px 15px;
        font-size: 14px;
    }
}

/* Kiểu dáng phần mô tả và tab */
.product-details-tabs {
    margin-top: 40px;
    margin-bottom: 40px;
    opacity: 0;
    transform: translateY(30px);
    transition: opacity 0.5s ease, transform 0.5s ease;
}

.tabs-header {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    border-bottom: 1px solid #ddd;
    overflow-x: auto;
    scrollbar-width: thin;
    scrollbar-color: #007bff #f0f0f0;
}

.tabs-header::-webkit-scrollbar {
    height: 6px;
}

.tabs-header::-webkit-scrollbar-track {
    background: #f0f0f0;
    border-radius: 10px;
}

.tabs-header::-webkit-scrollbar-thumb {
    background-color: #007bff;
    border-radius: 10px;
}

.tab-btn {
    padding: 12px 25px;
    border: none;
    background-color: transparent;
    cursor: pointer;
    font-size: 16px;
    font-weight: bold;
    color: #6c757d;
    position: relative;
    transition: all 0.3s ease;
    white-space: nowrap;
}

.tab-btn::after {
    content: '';
    position: absolute;
    bottom: -1px;
    left: 0;
    width: 0;
    height: 3px;
    background-color: #007bff;
    transition: width 0.3s ease;
}

.tab-btn:hover {
    color: #007bff;
}

.tab-btn:hover::after {
    width: 100%;
}

.tab-btn.active {
    color: #007bff;
}

.tab-btn.active::after {
    width: 100%;
}

.tabs-content {
    border: 1px solid #ddd;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.tab-pane {
    padding: 25px;
    animation: fadeIn 0.3s ease;
}

.product-description {
    font-size: 16px;
    line-height: 1.6;
    color: #333;
}

.product-description h3 {
    font-size: 22px;
    margin-bottom: 15px;
    color: #333;
}

.product-description h4 {
    font-size: 18px;
    margin-bottom: 10px;
    margin-top: 20px;
    color: #333;
}

.product-description p {
    margin-bottom: 15px;
}

.product-description ul {
    margin-bottom: 15px;
    padding-left: 20px;
}

.product-description li {
    margin-bottom: 8px;
}

.specifications-table {
    width: 100%;
    border-collapse: collapse;
}

.specifications-table tr:nth-child(odd) {
    background-color: #f9f9f9;
}

.specifications-table td {
    padding: 12px 15px;
    border-bottom: 1px solid #eee;
}

.spec-name {
    font-weight: bold;
    width: 30%;
    color: #555;
}

.spec-value {
    color: #333;
}

.reviews-summary {
    display: flex;
    flex-wrap: wrap;
    gap: 30px;
    margin-bottom: 30px;
    padding-bottom: 20px;
    border-bottom: 1px solid #eee;
}

.average-rating {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    min-width: 150px;
}

.rating-number {
    font-size: 48px;
    font-weight: bold;
    color: #333;
    line-height: 1;
}

.rating-stars {
    display: flex;
    gap: 5px;
    color: #ffc107;
    font-size: 20px;
}

.rating-count {
    font-size: 16px;
    color: #6c757d;
}

.rating-bars {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
    min-width: 250px;
}

.rating-bar-item {
    display: flex;
    align-items: center;
    gap: 10px;
}

.rating-label {
    font-size: 14px;
    color: #333;
    width: 50px;
}

.rating-bar {
    flex: 1;
    height: 8px;
    background-color: #eee;
    border-radius: 4px;
    overflow: hidden;
}

.rating-bar-fill {
    height: 100%;
    background-color: #ffc107;
    border-radius: 4px;
    transition: width 0.5s ease;
}

.rating-percent {
    font-size: 14px;
    color: #6c757d;
    width: 40px;
    text-align: right;
}

.add-review-button-container {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    margin-top: 20px;
    padding: 15px 0;
    border-top: 1px dashed #eee;
}

.add-review-button-container .ant-btn-primary {
    padding: 0 25px;
    height: 42px;
    font-size: 16px;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 8px;
    border-radius: 6px;
    transition: all 0.3s ease;
}

.add-review-button-container .ant-btn-primary:hover {
    transform: translateY(-3px);
    box-shadow: 0 5px 15px rgba(0, 123, 255, 0.3);
}

.login-to-review {
    width: 100%;
}

.login-to-review a {
    color: #007bff;
    text-decoration: underline;
    cursor: pointer;
    font-weight: 500;
}

.login-to-review a:hover {
    color: #0056b3;
}

.reviews-list {
    margin-bottom: 20px;
}

.review-item {
    padding: 20px 0;
    border-bottom: 1px solid #eee;
    animation: fadeIn 0.3s ease;
}

.review-item:last-child {
    border-bottom: none;
}

.review-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
}

.reviewer-info {
    display: flex;
    align-items: center;
    gap: 10px;
}

.reviewer-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    overflow: hidden;
    border: 2px solid #eee;
}

.reviewer-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.reviewer-name {
    font-size: 16px;
    font-weight: bold;
    color: #333;
}

.review-actions {
    display: flex;
    align-items: center;
    gap: 10px;
}

.review-rating {
    display: flex;
    gap: 3px;
    color: #ffc107;
}

.review-buttons {
    display: flex;
    gap: 5px;
}

.review-date {
    font-size: 14px;
    color: #6c757d;
    margin-bottom: 10px;
}

.review-content {
    font-size: 16px;
    line-height: 1.5;
    color: #333;
    margin-bottom: 15px;
}

.review-images {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
}

.review-image {
    width: 100px;
    height: 100px;
    border-radius: 4px;
    overflow: hidden;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    cursor: pointer;
    transition: transform 0.2s ease;
}

.review-image:hover {
    transform: scale(1.05);
}

.review-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.no-reviews {
    padding: 30px;
    text-align: center;
    color: #6c757d;
}

@keyframes fadeIn {
    from {
        opacity: 0;
    }

    to {
        opacity: 1;
    }
}

/* Responsive cho phần tab */
@media (max-width: 992px) {
    .reviews-summary {
        flex-direction: column;
        gap: 20px;
    }

    .average-rating {
        align-items: flex-start;
    }
}

@media (max-width: 768px) {
    .tab-btn {
        padding: 10px 15px;
        font-size: 14px;
    }

    .review-image {
        width: 80px;
        height: 80px;
    }

    .fullscreen-thumbnails .thumbnail-wrapper {
        width: 60px;
        height: 60px;
    }
}

@media (max-width: 576px) {
    .tab-pane {
        padding: 15px;
    }

    .product-description {
        font-size: 14px;
    }

    .review-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
    }
}

/* Kiểu dáng phần sản phẩm đề xuất */
.recommended-products {
    margin-top: 40px;
    margin-bottom: 40px;
    opacity: 0;
    transform: translateY(30px);
    transition: opacity 0.5s ease, transform 0.5s ease;
}

.section-title {
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 20px;
    position: relative;
    padding-bottom: 10px;
    color: #333;
}

.section-title::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 60px;
    height: 3px;
    background-color: #007bff;
}

.products-carousel {
    position: relative;
    padding: 20px;
    border: 1px solid #eee;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.carousel-control {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: #fff;
    border: none;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10;
    transition: all 0.2s ease;
}

.carousel-control:hover:not(:disabled) {
    background-color: #007bff;
    color: #fff;
    transform: translateY(-50%) scale(1.1);
}

.carousel-control:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.carousel-control.prev {
    left: 10px;
}

.carousel-control.next {
    right: 10px;
}

.carousel-container {
    overflow: hidden;
}

.carousel-track {
    display: flex;
    gap: 20px;
    transition: transform 0.5s ease;
}

.product-card {
    flex: 0 0 20%;
    max-width: 20%;
    animation: fadeIn 0.3s ease;
}

.product-card-image {
    position: relative;
    width: 100%;
    height: 0;
    padding-bottom: 100%;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.product-card-image img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
}

.product-card:hover .product-card-image img {
    transform: scale(1.05);
}

.product-card-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.product-card:hover .product-card-overlay {
    opacity: 1;
}

.overlay-btn {
    padding: 8px 15px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    font-weight: bold;
    display: flex;
    align-items: center;
    gap: 5px;
    margin: 5px;
    transition: all 0.2s ease;
}

.overlay-btn.view {
    background-color: #fff;
    color: #333;
}

.overlay-btn.view:hover {
    background-color: #f8f9fa;
    transform: translateY(-2px);
}

.overlay-btn.cart {
    background-color: #007bff;
    color: #fff;
}

.overlay-btn.cart:hover {
    background-color: #0056b3;
    transform: translateY(-2px);
}

.product-card-badge {
    position: absolute;
    top: 10px;
    left: 10px;
    padding: 5px 10px;
    border-radius: 4px;
    background-color: #ff69b4;
    color: #fff;
    font-size: 14px;
    font-weight: bold;
    z-index: 1;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.product-card-info {
    padding: 15px 10px;
}

.product-card-title {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 10px;
    color: #333;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-clamp: 2;
    overflow: hidden;
    height: 40px;
}

.product-card-price {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
}

.product-card-price .current-price {
    font-size: 18px;
    font-weight: bold;
    color: #ff5722;
}

.product-card-price .original-price {
    font-size: 14px;
    color: #6c757d;
    text-decoration: line-through;
}

/* Responsive cho phần sản phẩm đề xuất */
@media (max-width: 1200px) {
    .product-card {
        flex: 0 0 25%;
        max-width: 25%;
    }
}

@media (max-width: 992px) {
    .product-card {
        flex: 0 0 33.333%;
        max-width: 33.333%;
    }
}

@media (max-width: 768px) {
    .product-card {
        flex: 0 0 50%;
        max-width: 50%;
    }
}

@media (max-width: 576px) {
    .product-card {
        flex: 0 0 100%;
        max-width: 100%;
    }
}

/* Animation cho chuyển đổi hình ảnh */
.fade-slide-enter-active {
    transition: all 0.4s ease;
}

.fade-slide-leave-active {
    transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
}

.fade-slide-enter-from {
    transform: translateX(10px);
    opacity: 0;
}

.fade-slide-leave-to {
    transform: translateX(-10px);
    opacity: 0;
}

/* Add directional slide transitions at the end of the CSS section */
/* Right to left transition */
.slide-right-enter-active,
.slide-right-leave-active,
.slide-left-enter-active,
.slide-left-leave-active {
    transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    backface-visibility: hidden;
    /* Prevent flickering */
    will-change: transform, opacity;
    /* Performance optimization */
}

/* Adjust the enter/leave positions to ensure overlap */
.slide-right-enter-from {
    transform: translateX(100%);
    opacity: 1;
    /* Start fully visible */
}

.slide-right-leave-to {
    transform: translateX(-100%);
    opacity: 1;
    /* End fully visible */
}

.slide-left-enter-from {
    transform: translateX(-100%);
    opacity: 1;
    /* Start fully visible */
}

.slide-left-leave-to {
    transform: translateX(100%);
    opacity: 1;
    /* End fully visible */
}

/* Left to right transition */
.slide-left-enter-from {
    transform: translateX(-100%);
    opacity: 0;
}

.slide-left-leave-to {
    transform: translateX(100%);
    opacity: 0;
}

/* Add the new slider styles at the end of the CSS section */
.slider-container {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    overflow: hidden;
}

.slide-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-size: cover;
    background-position: center;
    opacity: 0;
    transform: translateX(100%);
    transition: transform 0.5s cubic-bezier(0.25, 0.8, 0.25, 1), opacity 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
    pointer-events: none;
}

.slide-image.active {
    opacity: 1;
    transform: translateX(0);
    z-index: 2;
    pointer-events: auto;
}

.slide-image.previous {
    opacity: 1;
    transform: translateX(-100%);
    z-index: 1;
}

.slide-image.next {
    opacity: 1;
    transform: translateX(100%);
    z-index: 1;
}

/* Add styles for fullscreen slider */
.fullscreen-image-container {
    position: relative;
    width: 100%;
    height: 80vh;
    margin-bottom: 20px;
    overflow: hidden;
}

.fullscreen-slide {
    background-position: center;
    background-repeat: no-repeat;
    background-size: contain;
}

.cart-count-badge {
    position: absolute;
    top: -8px;
    right: -8px;
    background-color: #ff3a3a;
    color: white;
    font-size: 10px;
    font-weight: bold;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    animation: bounce 1s infinite;
}

@keyframes bounce {

    0%,
    100% {
        transform: translateY(0);
    }

    50% {
        transform: translateY(-3px);
    }
}

/* Cart notification modal */
:deep(.cart-notification-modal .ant-modal-content) {
    border-radius: 10px;
    overflow: hidden;
    z-index: 10000;
}

:deep(.cart-notification-modal .ant-modal-body) {
    padding: 0;
}

.cart-notification {
    padding: 0;
}

.notification-header {
    background-color: #f6ffed;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 15px;
}

.notification-header h3 {
    margin: 0;
    color: #52c41a;
    font-size: 18px;
}

.success-icon {
    color: #52c41a;
    font-size: 24px;
}

.notification-product {
    display: flex;
    padding: 0 20px 15px;
    border-bottom: 1px solid #eee;
}

.product-thumbnail {
    width: 70px;
    height: 70px;
    object-fit: cover;
    border-radius: 6px;
    border: 1px solid #eee;
    margin-right: 15px;
}

.product-info {
    flex: 1;
}

.product-info h4 {
    margin: 0 0 5px;
    font-size: 14px;
    font-weight: 500;
}

.product-variants {
    font-size: 12px;
    color: #666;
    margin-bottom: 8px;
}

.product-variants span {
    margin-right: 10px;
}

.product-quantity-price {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
}

.price {
    font-weight: 600;
    color: #ff4d4f;
}

.cart-summary {
    padding: 15px 20px;
    border-bottom: 1px solid #eee;
}

.summary-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
    font-size: 14px;
}

.summary-row.total {
    font-weight: 600;
    font-size: 16px;
}

.notification-actions {
    display: flex;
    justify-content: space-between;
    gap: 10px;
    padding: 15px 20px 20px;
}

.notification-actions button {
    flex: 1;
}

/* Thêm CSS cho hiển thị số lượt thích bên ngoài trái tim */
.wishlist-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-left: 5px;
}

.wishlist-count {
    font-weight: 600;
    font-size: 16px;
    color: #333;
}

.wishlist-text {
    font-size: 12px;
    color: #666;
}

/* Modal chỉnh sửa đánh giá */
:deep(.edit-review-modal .ant-modal-content) {
    border-radius: 10px;
    overflow: hidden;
}

:deep(.edit-review-modal .ant-modal-body) {
    padding: 20px;
}

/* Fix vị trí modal ở giữa màn hình */
:deep(.edit-review-modal) {
    display: flex;
    align-items: center;
    justify-content: center;
}

:deep(.ant-modal) {
    top: 0;
    padding-bottom: 0;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: center;
}

:deep(.ant-rate) {
    font-size: 26px;
    z-index: 1001;
    /* Tăng z-index để đảm bảo có thể chọn được */
    position: relative;
}

:deep(.ant-rate-star) {
    cursor: pointer !important;
    margin-right: 8px;
}

.edit-review {
    padding: 0;
}

.edit-review h3 {
    margin-bottom: 20px;
    font-size: 18px;
    font-weight: bold;
    color: #333;
}

.edit-review-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 20px;
}

/* Modal thêm bình luận mới */
:deep(.add-review-modal .ant-modal-content) {
    border-radius: 10px;
    overflow: hidden;
}

:deep(.add-review-modal .ant-modal-body) {
    padding: 20px;
}

/* Fix vị trí modal ở giữa màn hình */
:deep(.add-review-modal) {
    display: flex;
    align-items: center;
    justify-content: center;
}

.add-review {
    padding: 0;
}

.add-review h3 {
    margin-bottom: 20px;
    font-size: 18px;
    font-weight: bold;
    color: #333;
}

.add-review-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 20px;
}

/* Thêm vào phần <style> của component */
:deep(.custom-warning-modal) {
    display: flex;
    align-items: center;
    justify-content: center;
}

:deep(.custom-warning-modal .ant-modal-content) {
    padding: 30px 20px;
    border-radius: 12px;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

:deep(.custom-warning-modal .ant-modal-confirm-body-wrapper) {
    text-align: center;
}

:deep(.custom-warning-modal .ant-modal-confirm-title) {
    font-size: 22px;
    margin-bottom: 15px;
}

:deep(.custom-warning-modal .ant-modal-confirm-content) {
    font-size: 16px;
    margin-top: 10px;
    margin-bottom: 20px;
}

:deep(.custom-warning-modal .ant-modal-confirm-btns) {
    justify-content: center;
    margin-top: 20px;
}

:deep(.custom-warning-modal .ant-btn) {
    min-width: 120px;
    height: 40px;
    font-size: 16px;
    border-radius: 8px;
}

/* lềnh thêm mới */
.favorited-variant-badge {
    background-color: #e53935;
    color: #fff;
    padding: 5px 10px;
    border-radius: 4px;
    font-size: 14px;
    margin-bottom: 10px;
}

/* ==== SẢN PHẨM TƯƠNG TỰ - KHÔNG TRÙNG BIẾN ==== */
.rec-related-products-section {
    padding: 2rem 0;
    font-family: 'Montserrat', sans-serif;
    background-color: #f8f9fa;
    opacity: 0;
    transform: translateY(30px);
    transition: opacity 0.8s ease, transform 0.8s ease;
}

.rec-related-products-section.visible {
    opacity: 1;
    transform: translateY(0);
}

.rec-related-container {
    max-width: 1280px;
    margin: 0 auto;
}

.rec-related-section-header {
    text-align: center;
}

.rec-related-section-title {
    font-size: 1.5rem;
    font-weight: 600;
    color: #333;
    position: relative;
    display: inline-block;
}

.rec-related-section-divider {
    height: 3px;
    width: 100px;
    background-color: #3a86ff;
    margin: 0 auto;
}

.rec-related-products-grid {
    padding: 1rem;
    display: flex;
    flex-wrap: wrap;
}

.rec-product-card {
    position: relative;
    flex: 0 0 20%;
    max-width: 20%;
    padding: 15px;
    border-radius: 8px;
    margin-bottom: 20px;
    background-color: #fff;
    cursor: pointer;
    opacity: 0;
    transform: translateY(20px);
    transition: transform 0.3s ease, box-shadow 0.3s ease, opacity 0.5s ease;
}

.rec-product-card:hover {
    transform: translateY(-10px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
    z-index: 5;
}

.rec-related-products-section.visible .rec-product-card {
    opacity: 1;
    transform: translateY(0);
}

.rec-product-image-container {
    position: relative;
    overflow: hidden;
    border-radius: 8px;
    margin-bottom: 12px;
}

.rec-product-image {
    width: 100%;
    height: auto;
    aspect-ratio: 1/1;
    object-fit: cover;
    transition: transform 0.5s ease;
}

.rec-product-card:hover .rec-product-image {
    transform: scale(1.05);
}

.rec-discount-badge {
    position: absolute;
    top: 10px;
    right: 10px;
    background-color: #ff3a3a;
    color: white;
    font-weight: 600;
    font-size: 12px;
    padding: 5px 10px;
    border-radius: 4px;
    z-index: 2;
}

.rec-product-overlay {
    position: absolute;
    bottom: -50px;
    left: 0;
    width: 100%;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
    padding: 15px;
    transition: all 0.3s ease;
    opacity: 0;
}

.rec-product-overlay.active {
    bottom: 0;
    opacity: 1;
}

.rec-overlay-buttons {
    display: flex;
    justify-content: space-around;
}

.rec-overlay-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    background: white;
    border: none;
    border-radius: 20px;
    padding: 5px 12px;
    font-size: 12px;
    font-weight: 500;
    color: #333;
    cursor: pointer;
    transition: all 0.2s ease;
    text-decoration: none;
}

.rec-overlay-btn:hover {
    background: #3a86ff;
    color: white;
}

.rec-overlay-btn span {
    margin-left: 5px;
}

.rec-product-info {
    padding: 0 5px;
}

.rec-product-price-row {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
}

.rec-product-price {
    font-size: 15px;
    font-weight: 600;
    color: #333;
    margin-right: 8px;
}

.rec-product-old-price {
    font-size: 12px;
    color: #999;
    text-decoration: line-through;
    margin-right: 8px;
}

.rec-product-discount {
    font-size: 11px;
    font-weight: 600;
    color: white;
    background-color: #ff3a3a;
    padding: 2px 6px;
    border-radius: 10px;
}

.rec-product-name {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin-bottom: 8px;
    line-height: 1.4;
    height: 40px;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
}

.rec-product-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.rec-product-brand {
    font-size: 12px;
    font-weight: 500;
    color: #666;
}

.rec-product-rating {
    display: flex;
    align-items: center;
    font-size: 12px;
    color: #666;
}

.rec-product-rating :deep(svg) {
    color: #ffc107;
    margin-right: 3px;
    font-size: 14px;
}

.rec-carousel-container {
    position: relative;
    padding: 0 50px;
    margin-bottom: 30px;
}

.rec-custom-arrow {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 40px;
    height: 40px;
    background: rgba(0, 0, 0, 0.5);
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;
    border: none;
    font-size: 18px;
    transition: all 0.3s ease;
    opacity: 0;
    visibility: hidden;
}

.rec-custom-arrow:hover {
    background: rgba(0, 0, 0, 0.7);
}

.rec-custom-arrow.visible {
    opacity: 1;
    visibility: visible;
}

.rec-prev-arrow {
    left: 10px;
}

.rec-next-arrow {
    right: 10px;
}

:deep(.rec-ant-carousel) {
    width: 100%;
}

:deep(.rec-ant-carousel .slick-dots-bottom) {
    bottom: -25px;
}

:deep(.rec-ant-carousel .slick-dots li button) {
    background: #d9d9d9;
    opacity: 0.4;
}

:deep(.rec-ant-carousel .slick-dots li.slick-active button) {
    background: #3a86ff;
    opacity: 1;
}

/* Modal styles */
.rec-product-detail-modal {
    padding: 20px;
    position: relative;
    z-index: 10000;
}

:deep(.rec-ant-modal) {
    z-index: 9999 !important;
    padding-top: 20px !important;
}

:deep(.rec-ant-modal-mask) {
    z-index: 9998 !important;
}

:deep(.rec-ant-modal-wrap) {
    z-index: 9999 !important;
}

:deep(.rec-ant-modal-content) {
    position: relative;
    z-index: 10000;
    margin-top: 0 !important;
}

:deep(.rec-ant-modal-header) {
    position: relative;
    z-index: 10000;
}

:deep(.rec-ant-modal-body) {
    position: relative;
    z-index: 10000;
}

.rec-product-detail-content {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 30px;
}

.rec-product-images {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.rec-main-image {
    width: 100%;
    aspect-ratio: 1;
    border-radius: 8px;
    overflow: hidden;
}

.rec-main-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.rec-thumbnail-images {
    display: flex;
    gap: 10px;
    overflow-x: auto;
    padding: 5px 0;
}

.rec-thumbnail-images img {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 4px;
    cursor: pointer;
    transition: opacity 0.3s ease;
}

.rec-thumbnail-images img:hover {
    opacity: 0.8;
}

.rec-product-info-detail {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.rec-price-section {
    display: flex;
    align-items: center;
    gap: 10px;
}

.rec-current-price {
    font-size: 24px;
    font-weight: 600;
    color: #333;
}

.rec-old-price {
    font-size: 16px;
    color: #999;
    text-decoration: line-through;
}

.rec-brand-section {
    display: flex;
    align-items: center;
    gap: 10px;
}

.rec-brand-label {
    color: #666;
}

.rec-brand-value {
    font-weight: 500;
    color: #333;
}

.rec-rating-section {
    display: flex;
    align-items: center;
    gap: 5px;
}

.rec-rating {
    display: flex;
    align-items: center;
    gap: 5px;
}

.rec-rating :deep(svg) {
    color: #ffc107;
}

.rec-description-section h4 {
    margin-bottom: 10px;
    color: #333;
}

.rec-description-section p {
    color: #666;
    line-height: 1.6;
}

.rec-variants-section {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.rec-color-variants h4,
.rec-size-variants h4 {
    margin-bottom: 10px;
    color: #333;
}

.rec-color-options {
    display: flex;
    gap: 10px;
}

.rec-color-option {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.3s ease;
}

.rec-color-option.selected {
    border-color: #3a86ff;
}

.rec-size-options {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
}

.rec-size-option {
    padding: 5px 15px;
    border: 1px solid #ddd;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.rec-size-option.selected {
    background-color: #3a86ff;
    color: white;
    border-color: #3a86ff;
}

.rec-quantity-section h4 {
    margin-bottom: 10px;
    color: #333;
}

.rec-quantity-controls {
    display: flex;
    align-items: center;
    gap: 10px;
}

.rec-quantity-value {
    min-width: 40px;
    text-align: center;
    font-weight: 500;
}

.rec-action-buttons {
    margin-top: 20px;
}

.rec-action-buttons button {
    width: 100%;
    height: 40px;
}

@media (max-width: 1200px) {
    .rec-product-card {
        flex: 0 0 25%;
        max-width: 25%;
    }
}

@media (max-width: 992px) {
    .rec-product-card {
        flex: 0 0 33.333%;
        max-width: 33.333%;
    }
}

@media (max-width: 768px) {
    .rec-product-card {
        flex: 0 0 50%;
        max-width: 50%;
    }

    .rec-product-detail-content {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 576px) {
    .rec-product-card {
        flex: 0 0 100%;
        max-width: 100%;
    }
}
</style>
