package com.example.duanbe.repository;

import com.example.duanbe.entity.ChiTietGioHang;
import com.example.duanbe.response.GioHangWebResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import com.example.duanbe.entity.ChiTietGioHangId;
import org.springframework.data.repository.query.Param;

//sưa ngay 20/10
public interface GioHangWebRepo extends JpaRepository<ChiTietGioHang, ChiTietGioHangId> {
    @Query(nativeQuery = true, value = """
            WITH khuyen_mai_tot_nhat AS (
                SELECT
                    ctkm.id_chi_tiet_san_pham,
                    MIN(ctkm.gia_sau_giam) AS gia_km_tot_nhat,
                    (
                        SELECT TOP 1 km2.ten_khuyen_mai
                        FROM chi_tiet_khuyen_mai ctkm2
                        JOIN khuyen_mai km2 ON km2.id_khuyen_mai = ctkm2.id_khuyen_mai
                        WHERE ctkm2.id_chi_tiet_san_pham = ctkm.id_chi_tiet_san_pham
                          AND km2.trang_thai = N'Đang diễn ra'
                          -- ✅ TIMEZONE FIX: Convert DB datetime (no TZ) to Vietnam TZ before comparison
                          AND CAST(km2.ngay_bat_dau AT TIME ZONE 'SE Asia Standard Time' AS DATETIMEOFFSET) <= SWITCHOFFSET(SYSDATETIMEOFFSET(), '+07:00')
                          AND CAST(km2.ngay_het_han AT TIME ZONE 'SE Asia Standard Time' AS DATETIMEOFFSET) >= SWITCHOFFSET(SYSDATETIMEOFFSET(), '+07:00')
                        ORDER BY ctkm2.gia_sau_giam ASC
                    ) AS ten_khuyen_mai
                FROM chi_tiet_khuyen_mai ctkm
                JOIN khuyen_mai km ON km.id_khuyen_mai = ctkm.id_khuyen_mai
                WHERE km.trang_thai = N'Đang diễn ra'
                  -- ✅ TIMEZONE FIX: Convert DB datetime (no TZ) to Vietnam TZ before comparison
                  AND CAST(km.ngay_bat_dau AT TIME ZONE 'SE Asia Standard Time' AS DATETIMEOFFSET) <= SWITCHOFFSET(SYSDATETIMEOFFSET(), '+07:00')
                  AND CAST(km.ngay_het_han AT TIME ZONE 'SE Asia Standard Time' AS DATETIMEOFFSET) >= SWITCHOFFSET(SYSDATETIMEOFFSET(), '+07:00')
                GROUP BY ctkm.id_chi_tiet_san_pham
            )
            SELECT
                gh.id_gio_hang,
                ctsp.id_chi_tiet_san_pham,
                gh.id_khach_hang,
                sp.anh_dai_dien AS hinh_anh,
                sp.ten_san_pham,
                ms.ten_mau_sac,
                kt.gia_tri,
                kt.don_vi,
                ctgh.so_luong,
                ctsp.gia_ban,
                ctsp.trang_thai,
                km_tot.gia_km_tot_nhat AS gia_khuyen_mai,
                CASE
                    WHEN km_tot.gia_km_tot_nhat IS NOT NULL AND ctsp.gia_ban > 0
                    THEN CAST(((ctsp.gia_ban - km_tot.gia_km_tot_nhat) / ctsp.gia_ban * 100) AS INTEGER)
                    ELSE NULL
                END AS phan_tram_giam,
                km_tot.ten_khuyen_mai
            FROM gio_hang gh
            JOIN chi_tiet_gio_hang ctgh ON ctgh.id_gio_hang = gh.id_gio_hang
            JOIN chi_tiet_san_pham ctsp ON ctsp.id_chi_tiet_san_pham = ctgh.id_chi_tiet_san_pham
            JOIN khach_hang kh ON kh.id_khach_hang = gh.id_khach_hang
            JOIN san_pham sp ON sp.id_san_pham = ctsp.id_san_pham
            JOIN mau_sac ms ON ms.id_mau_sac = ctsp.id_mau_sac
            JOIN kich_thuoc kt ON kt.id_kich_thuoc = ctsp.id_kich_thuoc
            LEFT JOIN khuyen_mai_tot_nhat km_tot ON km_tot.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
            WHERE gh.id_khach_hang = :idKhachHang
            """)
    List<GioHangWebResponse> listGioHangByKhachHang(@Param("idKhachHang") Integer idKhachHang);

    @Query(nativeQuery = true, value = """
            select kh.id_khach_hang, dckh.so_nha, dckh.xa_phuong, dckh.quan_huyen, dckh.tinh_thanh_pho, dckh.dia_chi_mac_dinh from khach_hang kh
            join dia_chi_khach_hang dckh on dckh.id_khach_hang = kh.id_khach_hang
            where kh.id_khach_hang = :idKhachHang
            """)
    List<GioHangWebResponse> listDiaChiByKH(@Param("idKhachHang") Integer idKhachHang);

    // ✅ NEW: Check if CTSP exists in any cart (for deletion validation)
    @Query("SELECT COUNT(c) FROM ChiTietGioHang c WHERE c.chiTietSanPham.id_chi_tiet_san_pham = :idCTSP")
    Long countByCTSPId(@Param("idCTSP") Integer idCTSP);

    // ✅ NEW: Check if any CTSP of a product exists in carts
    @Query("SELECT COUNT(c) FROM ChiTietGioHang c WHERE c.chiTietSanPham.sanPham.id_san_pham = :idSanPham")
    Long countBySanPhamId(@Param("idSanPham") Integer idSanPham);
}
