package com.example.duanbe.util;

import com.example.duanbe.entity.KhuyenMai;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for promotion price calculations
 */
public class PriceCalculationUtil {

    /**
     * Tính giá gốc từ giá sau khuyến mãi (reverse calculation)
     * 
     * @param giaSauKhuyenMai Giá sau khi đã áp dụng khuyến mãi
     * @param khuyenMai       Thông tin khuyến mãi đã áp dụng
     * @return Giá gốc (trước khuyến mãi)
     */
    public static BigDecimal tinhGiaGocTuGiaSauKM(BigDecimal giaSauKhuyenMai, KhuyenMai khuyenMai) {
        if (giaSauKhuyenMai == null || khuyenMai == null) {
            return giaSauKhuyenMai;
        }

        String kieuGiamGia = khuyenMai.getKieuGiamGia();
        BigDecimal giaTriGiam = khuyenMai.getGiaTriGiam();
        BigDecimal giaTriToiDa = khuyenMai.getGiaTriToiDa();

        if ("Tiền mặt".equals(kieuGiamGia)) {
            // Dễ: giaSauKM = giaGoc - giaTriGiam
            // => giaGoc = giaSauKM + giaTriGiam
            return giaSauKhuyenMai.add(giaTriGiam);

        } else if ("Phần trăm".equals(kieuGiamGia)) {
            // Khó hơn vì có giới hạn tối đa
            // Case 1: Không có giới hạn hoặc chưa chạm giới hạn
            // giaSauKM = giaGoc - (giaGoc * %/100)
            // giaSauKM = giaGoc * (1 - %/100)
            // => giaGoc = giaSauKM / (1 - %/100)

            BigDecimal phanTram = giaTriGiam.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
            BigDecimal heSo = BigDecimal.ONE.subtract(phanTram); // 1 - %/100

            if (heSo.compareTo(BigDecimal.ZERO) <= 0) {
                // Edge case: giảm 100% hoặc hơn
                return giaSauKhuyenMai;
            }

            BigDecimal giaGocDuKien = giaSauKhuyenMai.divide(heSo, 2, RoundingMode.HALF_UP);

            // Kiểm tra xem có chạm giới hạn không
            if (giaTriToiDa != null) {
                BigDecimal soTienGiamDuKien = giaGocDuKien.multiply(phanTram);

                if (soTienGiamDuKien.compareTo(giaTriToiDa) > 0) {
                    // Case 2: Đã chạm giới hạn
                    // giaSauKM = giaGoc - giaTriToiDa
                    // => giaGoc = giaSauKM + giaTriToiDa
                    return giaSauKhuyenMai.add(giaTriToiDa);
                }
            }

            return giaGocDuKien.setScale(0, RoundingMode.HALF_UP);
        }

        // Không có khuyến mãi hoặc loại không xác định
        return giaSauKhuyenMai;
    }

    /**
     * Tính giá sau khuyến mãi từ giá gốc (forward calculation - for verification)
     * 
     * @param giaGoc    Giá gốc
     * @param khuyenMai Khuyến mãi cần áp dụng
     * @return Giá sau khuyến mãi
     */
    public static BigDecimal tinhGiaSauKM(BigDecimal giaGoc, KhuyenMai khuyenMai) {
        if (giaGoc == null || khuyenMai == null) {
            return giaGoc;
        }

        String kieuGiamGia = khuyenMai.getKieuGiamGia();
        BigDecimal giaTriGiam = khuyenMai.getGiaTriGiam();
        BigDecimal giaTriToiDa = khuyenMai.getGiaTriToiDa();

        if ("Tiền mặt".equals(kieuGiamGia)) {
            BigDecimal giaSauKM = giaGoc.subtract(giaTriGiam);
            return giaSauKM.compareTo(BigDecimal.ZERO) >= 0 ? giaSauKM : BigDecimal.ZERO;

        } else if ("Phần trăm".equals(kieuGiamGia)) {
            BigDecimal phanTram = giaTriGiam.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
            BigDecimal soTienGiam = giaGoc.multiply(phanTram);

            // Áp dụng giới hạn
            if (giaTriToiDa != null && soTienGiam.compareTo(giaTriToiDa) > 0) {
                soTienGiam = giaTriToiDa;
            }

            BigDecimal giaSauKM = giaGoc.subtract(soTienGiam);
            return giaSauKM.compareTo(BigDecimal.ZERO) >= 0 ? giaSauKM : BigDecimal.ZERO;
        }

        return giaGoc;
    }
}
