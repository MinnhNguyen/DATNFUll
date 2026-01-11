package com.example.duanbe.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "chi_tiet_khuyen_mai")
public class ChiTietKhuyenMai {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_ctkm")
  private Integer id;
  @Column(name = "gia_sau_giam")
  private BigDecimal giaSauGiam;

  @ManyToOne
  @JoinColumn(name = "id_khuyen_mai")
  private KhuyenMai khuyenMai;

  @ManyToOne
  @JoinColumn(name = "id_chi_tiet_san_pham")
  private ChiTietSanPham chiTietSanPham;

  // Getters and Setters
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public BigDecimal getGiaSauGiam() {
    return giaSauGiam;
  }

  public void setGiaSauGiam(BigDecimal giaSauGiam) {
    this.giaSauGiam = giaSauGiam;
  }

  public KhuyenMai getKhuyenMai() {
    return khuyenMai;
  }

  public void setKhuyenMai(KhuyenMai khuyenMai) {
    this.khuyenMai = khuyenMai;
  }

  public ChiTietSanPham getChiTietSanPham() {
    return chiTietSanPham;
  }

  public void setChiTietSanPham(ChiTietSanPham chiTietSanPham) {
    this.chiTietSanPham = chiTietSanPham;
  }

  @PrePersist
  @PreUpdate
  public void capNhatGiaKhuyenMaiTuDong() {
    System.out.println("🔄 JPA Callback - Cập nhật giá khuyến mãi tự động");

    if (khuyenMai == null || chiTietSanPham == null) {
      System.out.println("⚠️ Thiếu dữ liệu khuyến mãi hoặc sản phẩm");
      return;
    }

    // Chỉ áp dụng nếu khuyến mãi đang diễn ra
    if (!"Đang diễn ra".equals(khuyenMai.getTrangThai())) {
      System.out.println("⚠️ Khuyến mãi không đang diễn ra: " + khuyenMai.getTrangThai());
      return;
    }

    // Tính giá mới
    BigDecimal giaMoi = tinhGiaSauGiamTuDong();

    if (giaMoi != null) {
      BigDecimal giaCu = this.giaSauGiam;
      this.giaSauGiam = giaMoi;

      System.out.println("💰 Cập nhật giá:");
      System.out.println("  - Sản phẩm ID: " + chiTietSanPham.getId_chi_tiet_san_pham());
      System.out.println("  - Giá cũ: " + giaCu);
      System.out.println("  - Giá mới: " + giaMoi);
    }
  }

  // ✅ NEW: Logic tính giá tự động
  private BigDecimal tinhGiaSauGiamTuDong() {
    BigDecimal giaGoc = chiTietSanPham.getGia_ban();

    if (giaGoc == null || giaGoc.compareTo(BigDecimal.ZERO) <= 0) {
      return null;
    }

    if ("Phần trăm".equals(khuyenMai.getKieuGiamGia())) {
      // Khuyến mãi theo %
      BigDecimal phanTramGiam = khuyenMai.getGiaTriGiam().divide(new BigDecimal("100"));
      BigDecimal giaGiam = giaGoc.multiply(phanTramGiam);

      // Kiểm tra giới hạn giảm tối đa
      if (khuyenMai.getGiaTriToiDa() != null &&
          giaGiam.compareTo(khuyenMai.getGiaTriToiDa()) > 0) {
        giaGiam = khuyenMai.getGiaTriToiDa();
      }

      BigDecimal giaSauGiam = giaGoc.subtract(giaGiam);
      return giaSauGiam.compareTo(BigDecimal.ZERO) >= 0 ? giaSauGiam : BigDecimal.ZERO;

    } else if ("Tiền mặt".equals(khuyenMai.getKieuGiamGia())) {
      // Khuyến mãi tiền mặt
      BigDecimal giaGiam = khuyenMai.getGiaTriGiam();

      // Không giảm nhiều hơn giá gốc
      if (giaGiam.compareTo(giaGoc) >= 0) {
        return BigDecimal.ZERO; // Hoặc return giaGoc
      }

      return giaGoc.subtract(giaGiam);
    }

    return giaGoc; // Không có khuyến mãi
  }

  @Override
  public String toString() {
    return "ChiTietKhuyenMai{" +
        "id=" + id +
        ", giaSauGiam=" + giaSauGiam +
        ", khuyenMai=" + khuyenMai +
        ", chiTietSanPham=" + chiTietSanPham +
        '}';
  }
}
