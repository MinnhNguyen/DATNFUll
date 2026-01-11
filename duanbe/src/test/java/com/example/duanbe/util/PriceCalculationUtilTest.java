package com.example.duanbe.util;

import com.example.duanbe.entity.KhuyenMai;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for PriceCalculationUtil
 */
class PriceCalculationUtilTest {

    @Test
    void testTinhGiaGoc_TienMat() {
        // Arrange: Giảm 100k tiền mặt
        KhuyenMai km = new KhuyenMai();
        km.setKieuGiamGia("Tiền mặt");
        km.setGiaTriGiam(new BigDecimal("100000"));

        BigDecimal giaSauKM = new BigDecimal("900000"); // Giá sau giảm

        // Act
        BigDecimal giaGoc = PriceCalculationUtil.tinhGiaGocTuGiaSauKM(giaSauKM, km);

        // Assert
        assertEquals(new BigDecimal("1000000"), giaGoc);
    }

    @Test
    void testTinhGiaGoc_PhanTram_KhongGioiHan() {
        // Arrange: Giảm 20% không giới hạn
        KhuyenMai km = new KhuyenMai();
        km.setKieuGiamGia("Phần trăm");
        km.setGiaTriGiam(new BigDecimal("20")); // 20%
        km.setGiaTriToiDa(null); // Không giới hạn

        BigDecimal giaSauKM = new BigDecimal("800000"); // Giá sau giảm 20%

        // Act
        BigDecimal giaGoc = PriceCalculationUtil.tinhGiaGocTuGiaSauKM(giaSauKM, km);

        // Assert: 800,000 / 0.8 = 1,000,000
        assertEquals(new BigDecimal("1000000"), giaGoc);
    }

    @Test
    void testTinhGiaGoc_PhanTram_CoGioiHan_ChamGioiHan() {
        // Arrange: Giảm 20% giới hạn 150k
        KhuyenMai km = new KhuyenMai();
        km.setKieuGiamGia("Phần trăm");
        km.setGiaTriGiam(new BigDecimal("20")); // 20%
        km.setGiaTriToiDa(new BigDecimal("150000")); // Giảm tối đa 150k

        BigDecimal giaSauKM = new BigDecimal("850000"); // Giá sau giảm 150k

        // Act
        BigDecimal giaGoc = PriceCalculationUtil.tinhGiaGocTuGiaSauKM(giaSauKM, km);

        // Assert: 850,000 + 150,000 = 1,000,000
        assertEquals(new BigDecimal("1000000"), giaGoc);
    }

    @Test
    void testTinhGiaGoc_PhanTram_CoGioiHan_ChuaChamGioiHan() {
        // Arrange: Giảm 10% giới hạn 200k (nhưng chỉ giảm 50k)
        KhuyenMai km = new KhuyenMai();
        km.setKieuGiamGia("Phần trăm");
        km.setGiaTriGiam(new BigDecimal("10")); // 10%
        km.setGiaTriToiDa(new BigDecimal("200000")); // Giảm tối đa 200k (nhưng không chạm)

        BigDecimal giaSauKM = new BigDecimal("450000"); // Giá sau giảm 10% (50k)

        // Act
        BigDecimal giaGoc = PriceCalculationUtil.tinhGiaGocTuGiaSauKM(giaSauKM, km);

        // Assert: 450,000 / 0.9 = 500,000
        assertEquals(new BigDecimal("500000"), giaGoc);
    }

    @Test
    void testRoundTrip_TienMat() {
        // Arrange
        KhuyenMai km = new KhuyenMai();
        km.setKieuGiamGia("Tiền mặt");
        km.setGiaTriGiam(new BigDecimal("100000"));

        BigDecimal giaGocBanDau = new BigDecimal("1000000");

        // Act: Forward -> Reverse
        BigDecimal giaSauKM = PriceCalculationUtil.tinhGiaSauKM(giaGocBanDau, km);
        BigDecimal giaGocTinhLai = PriceCalculationUtil.tinhGiaGocTuGiaSauKM(giaSauKM, km);

        // Assert
        assertEquals(giaGocBanDau, giaGocTinhLai);
    }

    @Test
    void testRoundTrip_PhanTram() {
        // Arrange
        KhuyenMai km = new KhuyenMai();
        km.setKieuGiamGia("Phần trăm");
        km.setGiaTriGiam(new BigDecimal("20"));
        km.setGiaTriToiDa(new BigDecimal("150000"));

        BigDecimal giaGocBanDau = new BigDecimal("1000000");

        // Act: Forward -> Reverse
        BigDecimal giaSauKM = PriceCalculationUtil.tinhGiaSauKM(giaGocBanDau, km);
        BigDecimal giaGocTinhLai = PriceCalculationUtil.tinhGiaGocTuGiaSauKM(giaSauKM, km);

        // Assert
        assertEquals(giaGocBanDau, giaGocTinhLai);
    }
}
