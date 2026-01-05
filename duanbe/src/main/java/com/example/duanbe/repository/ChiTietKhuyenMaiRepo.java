package com.example.duanbe.repository;

import com.example.duanbe.entity.ChiTietKhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChiTietKhuyenMaiRepo extends JpaRepository<ChiTietKhuyenMai, Integer> {
  @Query("SELECT c FROM ChiTietKhuyenMai c JOIN FETCH c.chiTietSanPham WHERE c.khuyenMai.id = :khuyenMaiId")
  List<ChiTietKhuyenMai> findByKhuyenMaiId(Integer khuyenMaiId);

  void deleteByKhuyenMaiId(Integer khuyenMaiId);

  // ✅ FIXED: Sử dụng parameter thay vì hardcode để tránh encoding issue
  @Query("SELECT ckm FROM ChiTietKhuyenMai ckm " +
      "JOIN FETCH ckm.khuyenMai km " +
      "JOIN FETCH ckm.chiTietSanPham ctsp " +
      "WHERE ctsp.id_chi_tiet_san_pham = :idChiTietSanPham " +
      "AND km.ngayBatDau <= CURRENT_TIMESTAMP " +
      "AND km.ngayHetHan >= CURRENT_TIMESTAMP " +
      "AND km.trangThai = :trangThai")
  List<ChiTietKhuyenMai> findAllByChiTietSanPhamId(
      @Param("idChiTietSanPham") Integer idChiTietSanPham,
      @Param("trangThai") String trangThai);

  @Query("SELECT ckm FROM ChiTietKhuyenMai ckm WHERE ckm.chiTietSanPham.id_chi_tiet_san_pham = :idChiTietSanPham")
  List<ChiTietKhuyenMai> findAllActiveByCTSP(@Param("idChiTietSanPham") Integer idChiTietSanPham);
}
