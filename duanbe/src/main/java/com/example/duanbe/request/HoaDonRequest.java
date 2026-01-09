package com.example.duanbe.request;

import com.example.duanbe.entity.KhachHang;
import com.example.duanbe.entity.Voucher;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HoaDonRequest {
  private Integer id_hoa_don; //
  private String ma_hoa_don;//
  private KhachHang khachHang;//
  private String trang_thai;
  private Voucher voucher;
  private String sdt_nguoi_nhan;
  private String dia_chi;
  private String email;
  private BigDecimal tong_tien_truoc_giam;
  private BigDecimal phi_van_chuyen;
  private String ho_ten;
  private BigDecimal tong_tien_sau_giam;
  private String hinh_thuc_thanh_toan;
  private String phuong_thuc_nhan_hang;
  private String loai_hoa_don;
  private String ghi_chu;
  private Integer id_khach_hang;
  private Integer id_nhan_vien;
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime ngay_tao;
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime ngay_sua;
  private Boolean isChuyen;

  public Integer getId_hoa_don() {
    return id_hoa_don;
  }

  public void setId_hoa_don(Integer id_hoa_don) {
    this.id_hoa_don = id_hoa_don;
  }

  public String getMa_hoa_don() {
    return ma_hoa_don;
  }

  public void setMa_hoa_don(String ma_hoa_don) {
    this.ma_hoa_don = ma_hoa_don;
  }

  public KhachHang getKhachHang() {
    return khachHang;
  }

  public void setKhachHang(KhachHang khachHang) {
    this.khachHang = khachHang;
  }

  public String getTrang_thai() {
    return trang_thai;
  }

  public void setTrang_thai(String trang_thai) {
    this.trang_thai = trang_thai;
  }

  public Voucher getVoucher() {
    return voucher;
  }

  public void setVoucher(Voucher voucher) {
    this.voucher = voucher;
  }

  public String getSdt_nguoi_nhan() {
    return sdt_nguoi_nhan;
  }

  public void setSdt_nguoi_nhan(String sdt_nguoi_nhan) {
    this.sdt_nguoi_nhan = sdt_nguoi_nhan;
  }

  public String getDia_chi() {
    return dia_chi;
  }

  public void setDia_chi(String dia_chi) {
    this.dia_chi = dia_chi;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public BigDecimal getTong_tien_truoc_giam() {
    return tong_tien_truoc_giam;
  }

  public void setTong_tien_truoc_giam(BigDecimal tong_tien_truoc_giam) {
    this.tong_tien_truoc_giam = tong_tien_truoc_giam;
  }

  public BigDecimal getPhi_van_chuyen() {
    return phi_van_chuyen;
  }

  public void setPhi_van_chuyen(BigDecimal phi_van_chuyen) {
    this.phi_van_chuyen = phi_van_chuyen;
  }

  public String getHo_ten() {
    return ho_ten;
  }

  public void setHo_ten(String ho_ten) {
    this.ho_ten = ho_ten;
  }

  public BigDecimal getTong_tien_sau_giam() {
    return tong_tien_sau_giam;
  }

  public void setTong_tien_sau_giam(BigDecimal tong_tien_sau_giam) {
    this.tong_tien_sau_giam = tong_tien_sau_giam;
  }

  public String getHinh_thuc_thanh_toan() {
    return hinh_thuc_thanh_toan;
  }

  public void setHinh_thuc_thanh_toan(String hinh_thuc_thanh_toan) {
    this.hinh_thuc_thanh_toan = hinh_thuc_thanh_toan;
  }

  public String getPhuong_thuc_nhan_hang() {
    return phuong_thuc_nhan_hang;
  }

  public void setPhuong_thuc_nhan_hang(String phuong_thuc_nhan_hang) {
    this.phuong_thuc_nhan_hang = phuong_thuc_nhan_hang;
  }

  public String getLoai_hoa_don() {
    return loai_hoa_don;
  }

  public void setLoai_hoa_don(String loai_hoa_don) {
    this.loai_hoa_don = loai_hoa_don;
  }

  public String getGhi_chu() {
    return ghi_chu;
  }

  public void setGhi_chu(String ghi_chu) {
    this.ghi_chu = ghi_chu;
  }

  public Integer getId_khach_hang() {
    return id_khach_hang;
  }

  public void setId_khach_hang(Integer id_khach_hang) {
    this.id_khach_hang = id_khach_hang;
  }

  public Integer getId_nhan_vien() {
    return id_nhan_vien;
  }

  public void setId_nhan_vien(Integer id_nhan_vien) {
    this.id_nhan_vien = id_nhan_vien;
  }

  public LocalDateTime getNgay_tao() {
    return ngay_tao;
  }

  public void setNgay_tao(LocalDateTime ngay_tao) {
    this.ngay_tao = ngay_tao;
  }

  public LocalDateTime getNgay_sua() {
    return ngay_sua;
  }

  public void setNgay_sua(LocalDateTime ngay_sua) {
    this.ngay_sua = ngay_sua;
  }

  public Boolean getIsChuyen() {
    return isChuyen;
  }

  public void setIsChuyen(Boolean isChuyen) {
    this.isChuyen = isChuyen;
  }

}
