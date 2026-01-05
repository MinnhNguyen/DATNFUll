package com.example.duanbe.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.duanbe.entity.ChiTietKhuyenMai;
import com.example.duanbe.entity.ChiTietSanPham;
import com.example.duanbe.repository.ChiTietKhuyenMaiRepo;
import com.example.duanbe.repository.ChiTietSanPhamRepo;
import com.example.duanbe.repository.HoaDonChiTietRepo;
import com.example.duanbe.repository.HoaDonRepo;
import com.example.duanbe.repository.KhuyenMaiRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * UpdateAll
 */
@Component
@Slf4j
public class UpdateAll {
  @Autowired
  ChiTietSanPhamRepo chiTietSanPhamRepo;
  @Autowired
  ChiTietKhuyenMaiRepo chiTietKhuyenMaiRepo;
  @Autowired
  KhuyenMaiRepository khuyenMaiRepository;
  @Autowired
  HoaDonChiTietRepo hoaDonChiTietRepo;
  @Autowired
  HoaDonRepo hoaDonRepo;

  @Transactional
  public void updateAll(Integer idCTSP) {
    System.out.println("🔄 [UpdateAll] updateAll được gọi cho CTSP ID: " + idCTSP);
    updateKhuyenMai(idCTSP);
  }

  private void updateKhuyenMai(Integer idCTSP) {
    System.out.println("🔍 [UpdateAll] Bắt đầu updateKhuyenMai cho CTSP ID: " + idCTSP);

    List<ChiTietKhuyenMai> listCTKM = chiTietKhuyenMaiRepo
        .findAllByChiTietSanPhamId(idCTSP, "Đang diễn ra");

    System.out.println("📦 [UpdateAll] Tìm thấy " + listCTKM.size() + " khuyến mãi cho CTSP #" + idCTSP);

    if (listCTKM.isEmpty()) {
      System.out.println("⚠️ [UpdateAll] Không tìm thấy khuyến mãi nào!");
      return;
    }

    for (ChiTietKhuyenMai ctkm : listCTKM) {
      System.out.println("  🎁 [UpdateAll] Đang cập nhật CTKM #" + ctkm.getId()
          + " - KM: " + ctkm.getKhuyenMai().getTenKhuyenMai()
          + " - Giá cũ: " + ctkm.getGiaSauGiam());

      ctkm.capNhatGiaKhuyenMaiTuDong(); // Gọi method trong entity
      chiTietKhuyenMaiRepo.save(ctkm);

      System.out.println("  ✅ [UpdateAll] Đã lưu CTKM #" + ctkm.getId() + " - Giá mới: " + ctkm.getGiaSauGiam());
    }

    System.out.println("✅ [UpdateAll] Hoàn thành updateKhuyenMai");
  }

  public BigDecimal[] tinhGiaCuoiCung(Integer idCTSP) {
    try {
      System.out.println("🔄 Bắt đầu tính giá cho CTSP ID: " + idCTSP);

      // 1. Mock repositories (thay thế bằng service injection)
      ChiTietSanPham ctsp = chiTietSanPhamRepo.findById(idCTSP).orElse(null);
      List<ChiTietKhuyenMai> allKM = chiTietKhuyenMaiRepo.findAllActiveByCTSP(idCTSP);

      if (ctsp == null) {
        return new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO };
      }

      BigDecimal giaGoc = ctsp.getGia_ban();
      LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

      // 2. Lọc tất cả KM và filter hợp lệ
      List<ChiTietKhuyenMai> kmHopLe = allKM.stream()
          .filter(km -> isValidKhuyenMai(km, now))
          .collect(Collectors.toList());

      // 3. Xử lý logic
      BigDecimal giaCuoiCung = tinhGiaCuoiCungInternal(ctsp, kmHopLe);

      return new BigDecimal[] { giaGoc, giaCuoiCung };

    } catch (Exception e) {
      System.err.println("❌ Lỗi tính giá: " + e.getMessage());
      return new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO };
    }
  }

  /**
   * 🔍 Filter và xử lý logic khuyến mãi
   */
  private BigDecimal tinhGiaCuoiCungInternal(ChiTietSanPham ctsp, List<ChiTietKhuyenMai> kmHopLe) {
    BigDecimal giaCuoiCung = ctsp.getGia_ban();

    for (ChiTietKhuyenMai km : kmHopLe) {
      BigDecimal giaSauKM = tinhGiaSauKM(ctsp, km);

      // Luôn chọn giá thấp nhất (giảm nhiều nhất)
      if (giaSauKM.compareTo(giaCuoiCung) < 0) {
        giaCuoiCung = giaSauKM;
        System.out.println("  📉 Cập nhật - Giá KM: " + km.getKhuyenMai().getTenKhuyenMai());
        System.out.println("  💰 Giá sau KM: " + giaSauKM);
      }
    }

    return giaCuoiCung;
  }

  /**
   * 📊 Tính giá sau khuyến mãi cho một khuyến mãi
   */
  private BigDecimal tinhGiaSauKM(ChiTietSanPham ctsp, ChiTietKhuyenMai km) {
    if ("Phần trăm".equals(km.getKhuyenMai().getKieuGiamGia())) {
      // Khuyến mãi theo %
      BigDecimal phanTram = km.getKhuyenMai().getGiaTriGiam().divide(new BigDecimal("100"));
      BigDecimal giaGiam = ctsp.getGia_ban().multiply(phanTram);

      // Kiểm tra giới hạn
      if (km.getKhuyenMai().getGiaTriToiDa() != null
          && giaGiam.compareTo(km.getKhuyenMai().getGiaTriToiDa()) > 0) {
        giaGiam = km.getKhuyenMai().getGiaTriToiDa();
      }

      BigDecimal giaSauKM = ctsp.getGia_ban().subtract(giaGiam);
      return giaSauKM.compareTo(BigDecimal.ZERO) >= 0 ? giaSauKM : BigDecimal.ZERO;

    } else if ("Tiền mặt".equals(km.getKhuyenMai().getKieuGiamGia())) {
      // Khuyến mãi tiền mặt
      BigDecimal giaGiam = km.getKhuyenMai().getGiaTriGiam();

      if (giaGiam.compareTo(ctsp.getGia_ban()) >= 0) {
        return BigDecimal.ZERO; // Không giảm
      }

      BigDecimal giaSauKM = ctsp.getGia_ban().subtract(giaGiam);
      return giaSauKM.compareTo(BigDecimal.ZERO) >= 0 ? giaSauKM : BigDecimal.ZERO;

    } else {
      // Không có khuyến mãi hợp lệ
      return ctsp.getGia_ban();
    }
  }

  /**
   * 🕐 Kiểm tra khuyến mãi hợp lệ
   */
  private boolean isValidKhuyenMai(ChiTietKhuyenMai km, LocalDateTime now) {
    return "Đang diễn ra".equals(km.getKhuyenMai().getTrangThai()) &&
        km.getKhuyenMai().getNgayBatDau() != null && km.getKhuyenMai().getNgayHetHan() != null &&
        km.getKhuyenMai().getNgayBatDau().toLocalDateTime().isBefore(now) &&
        km.getKhuyenMai().getNgayHetHan().toLocalDateTime().isAfter(now);
  }
}
