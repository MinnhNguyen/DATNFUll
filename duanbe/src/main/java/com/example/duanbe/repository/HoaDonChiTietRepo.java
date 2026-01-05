package com.example.duanbe.repository;

import com.example.duanbe.entity.HoaDonChiTiet;
import com.example.duanbe.response.ChiTietTraHangResponse;
import com.example.duanbe.response.HoaDonChiTietResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface HoaDonChiTietRepo extends JpaRepository<HoaDonChiTiet, Integer> {
    @Query(value = """
                SELECT
                    -- Chi tiết hóa đơn
                    hdct.id_hoa_don_chi_tiet,
                    hdct.id_hoa_don,
                    hdct.so_luong,
                    hdct.don_gia,

                    -- Sản phẩm
                    ctsp.id_chi_tiet_san_pham,
                    sp.ten_san_pham,
                    sp.ma_san_pham,
                    sp.anh_dai_dien as hinh_anh,
                    ms.ten_mau_sac,
                    kt.gia_tri AS kich_thuoc,
                    ha.anh_chinh,

                    -- Tồn kho
                    ctsp.so_luong AS so_luong_ton_kho,
                    ctsp.trang_thai AS trang_thai_ctsp,

                    -- 💰 GIÁ ĐƠN VỊ (từ DB - đã lưu)
                    ROUND(hdct.don_gia / NULLIF(hdct.so_luong, 0), 0) AS gia_don_vi_luu,

                    -- 🎁 TÍNH NGƯỢC GIÁ GỐC từ giá đã lưu
                    CASE
                        WHEN active_km.kieu_giam_gia = N'Tiền mặt' THEN
                            -- Giá gốc = Giá sau KM + Số tiền giảm
                            ROUND(hdct.don_gia / NULLIF(hdct.so_luong, 0), 0) + active_km.gia_tri_giam

                        WHEN active_km.kieu_giam_gia = N'Phần trăm' THEN
                            -- Kiểm tra xem có chạm giới hạn không
                            CASE
                                -- TH1: Chạm giới hạn -> Giá gốc = Giá sau KM + Giới hạn
                                WHEN active_km.gia_tri_toi_da IS NOT NULL
                                    AND (ROUND(hdct.don_gia / NULLIF(hdct.so_luong, 0), 0) * active_km.gia_tri_giam / 100) > active_km.gia_tri_toi_da
                                THEN
                                    ROUND(hdct.don_gia / NULLIF(hdct.so_luong, 0), 0) + active_km.gia_tri_toi_da

                                -- TH2: Chưa chạm giới hạn -> Giá gốc = Giá sau KM / (1 - %/100)
                                ELSE
                                    ROUND(
                                        ROUND(hdct.don_gia / NULLIF(hdct.so_luong, 0), 0)
                                        / NULLIF((100 - active_km.gia_tri_giam) / 100.0, 0)
                                    , 0)
                            END

                        -- Không có KM -> Giá gốc = Giá đã lưu
                        ELSE
                            ROUND(hdct.don_gia / NULLIF(hdct.so_luong, 0), 0)
                    END AS gia_goc,

                    -- 🏷️ GIÁ SAU KHUYẾN MÃI (chính là giá đã lưu)
                    ROUND(hdct.don_gia / NULLIF(hdct.so_luong, 0), 0) AS gia_sau_km,

                    -- ✅ FLAG có khuyến mãi
                    CASE
                        WHEN active_km.id_khuyen_mai IS NOT NULL THEN CAST(1 AS BIT)
                        ELSE CAST(0 AS BIT)
                    END AS co_khuyen_mai,

                    -- 📝 Tên khuyến mãi
                    active_km.ten_khuyen_mai

                FROM hoa_don_chi_tiet hdct
                JOIN chi_tiet_san_pham ctsp ON hdct.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
                JOIN san_pham sp ON ctsp.id_san_pham = sp.id_san_pham
                LEFT JOIN kich_thuoc kt ON ctsp.id_kich_thuoc = kt.id_kich_thuoc
                LEFT JOIN mau_sac ms ON ctsp.id_mau_sac = ms.id_mau_sac
                LEFT JOIN hinh_anh ha ON ctsp.id_chi_tiet_san_pham = ha.id_chi_tiet_san_pham AND ha.anh_chinh = 1

                -- ✅ TÌM KHUYẾN MÃI ĐÃ ÁP DỤNG (so khớp giá)
                LEFT JOIN (
                    SELECT
                        ctkm.id_chi_tiet_san_pham,
                        ctkm.gia_sau_giam,
                        km.id_khuyen_mai,
                        km.ten_khuyen_mai,
                        km.kieu_giam_gia,
                        km.gia_tri_giam,
                        km.gia_tri_toi_da
                    FROM chi_tiet_khuyen_mai ctkm
                    JOIN khuyen_mai km ON ctkm.id_khuyen_mai = km.id_khuyen_mai
                ) active_km ON ctsp.id_chi_tiet_san_pham = active_km.id_chi_tiet_san_pham
                    -- So khớp giá: giá trong HDCT = giá sau KM trong CTKM
                    AND ROUND(hdct.don_gia / NULLIF(hdct.so_luong, 0), 0) = active_km.gia_sau_giam

                WHERE hdct.id_hoa_don = :idHoaDon
                ORDER BY hdct.id_hoa_don_chi_tiet
            """, nativeQuery = true)
    List<HoaDonChiTietResponse> findHoaDonChiTietById(
            @Param("idHoaDon") Integer idHoaDon);

    // ✅ NEW: Check if CTSP exists in any order (for deletion validation)
    @Query("SELECT COUNT(h) FROM HoaDonChiTiet h WHERE h.chiTietSanPham.id_chi_tiet_san_pham = :idCTSP")
    Long countByCTSPId(@Param("idCTSP") Integer idCTSP);

    // ✅ NEW: Check if any CTSP of a product exists in orders
    @Query("SELECT COUNT(h) FROM HoaDonChiTiet h WHERE h.chiTietSanPham.sanPham.id_san_pham = :idSanPham")
    Long countBySanPhamId(@Param("idSanPham") Integer idSanPham);

    @Modifying
    @Transactional
    @Query(value = """
            BEGIN TRANSACTION;

            	DECLARE @SOLUONG INT = :soLuong;
            	DECLARE @IDCTSP INT = :idCTSP;
            	DECLARE @IDHD INT = :idHoaDon;

            	UPDATE hoa_don_chi_tiet
                SET
                    so_luong = so_luong + @SOLUONG,
                    don_gia = (so_luong + @SOLUONG) * (SELECT gia_ban FROM chi_tiet_san_pham WHERE id_chi_tiet_san_pham = @IDCTSP)
                WHERE id_chi_tiet_san_pham = @IDCTSP
                AND id_hoa_don = @IDHD;

            	UPDATE chi_tiet_san_pham\s
            	SET\s
            		so_luong = so_luong - @SOLUONG
            	WHERE id_chi_tiet_san_pham = @IDCTSP;

            	UPDATE hoa_don
            	SET
            		tong_tien_truoc_giam = phi_van_chuyen + (SELECT TOP 1 SUM(don_gia) FROM hoa_don_chi_tiet hdct WHERE hdct.id_hoa_don = @IDHD),
            		tong_tien_sau_giam = phi_van_chuyen +\s
            		(SELECT TOP 1 SUM(don_gia) FROM hoa_don_chi_tiet hdct WHERE hdct.id_hoa_don = @IDHD) -
            		(SELECT COALESCE((
            			SELECT vc.gia_tri_toi_da\s
            			FROM hoa_don hd\s
            			LEFT JOIN voucher vc ON vc.id_voucher = hd.id_voucher
            			WHERE hd.tong_tien_truoc_giam >= vc.gia_tri_toi_thieu
            			AND hd.id_hoa_don = @IDHD
            		), 0) AS GiaTriToiDa)
            	WHERE id_hoa_don = @IDHD;
            COMMIT;
            """, nativeQuery = true)
    void addSLGH(@Param("idCTSP") Integer idCTSP, @Param("idHoaDon") Integer idHoaDon,
            @Param("soLuong") Integer soLuong);

    @Modifying
    @Transactional
    @Query(value = """
            BEGIN TRANSACTION;

                DECLARE @SOLUONG INT = :soLuong;
            	DECLARE @IDCTSP INT = :idCTSP;
            	DECLARE @IDHD INT = :idHoaDon;

                DECLARE @GIABAN DECIMAL(18, 2);
                SELECT @GIABAN = gia_ban FROM chi_tiet_san_pham WHERE id_chi_tiet_san_pham = @IDCTSP;

                DECLARE @PHIVANCHUYEN DECIMAL(18, 2);
                SELECT @PHIVANCHUYEN = phi_van_chuyen FROM hoa_don WHERE id_hoa_don = @IDHD;

                UPDATE hoa_don_chi_tiet
                SET
                    so_luong = so_luong - @SOLUONG,
                    don_gia = (so_luong - @SOLUONG) * @GIABAN
                WHERE id_hoa_don = @IDHD AND id_chi_tiet_san_pham = @IDCTSP;

                DECLARE @TONGTIENTRUOCGIAM DECIMAL(18, 2);
                SELECT @TONGTIENTRUOCGIAM = @PHIVANCHUYEN + SUM(don_gia)
                FROM hoa_don_chi_tiet
                WHERE id_hoa_don = @IDHD
                GROUP BY id_hoa_don;

                DECLARE @GIATRIVOUCHER DECIMAL(18, 2);
                SELECT @GIATRIVOUCHER = COALESCE(vc.gia_tri_toi_da, 0)
                FROM hoa_don hd
                LEFT JOIN voucher vc ON vc.id_voucher = hd.id_voucher
                WHERE hd.tong_tien_truoc_giam >= vc.gia_tri_toi_thieu
                    AND hd.id_hoa_don = @IDHD;

                UPDATE hoa_don
                SET
                    tong_tien_truoc_giam = @TONGTIENTRUOCGIAM,
                    tong_tien_sau_giam = @TONGTIENTRUOCGIAM - @GIATRIVOUCHER
                WHERE id_hoa_don = @IDHD;

                UPDATE chi_tiet_san_pham
                SET
                    so_luong = so_luong + @SOLUONG
                WHERE id_chi_tiet_san_pham = @IDCTSP;

            COMMIT;
            """, nativeQuery = true)
    void removeSPGH(@Param("idCTSP") Integer idCTSP, @Param("idHoaDon") Integer idHoaDon,
            @Param("soLuong") Integer soLuong);

    @Query(value = """
            select top 1 sum(don_gia) from hoa_don_chi_tiet hdct where hdct.id_hoa_don = :idHD
            """, nativeQuery = true)
    BigDecimal getDonGiaTongByIDHD(@Param("idHD") Integer idHD);

    @Query("SELECT h FROM HoaDonChiTiet h WHERE h.chiTietSanPham.id_chi_tiet_san_pham = :idChiTietSanPham AND h.hoaDon.id_hoa_don = :idHoaDon")
    Optional<HoaDonChiTiet> findByChiTietSanPhamIdAndHoaDonId(@Param("idChiTietSanPham") Integer idChiTietSanPham,
            @Param("idHoaDon") Integer idHoaDon);

    @Query("SELECT COALESCE(SUM(hdct.don_gia), 0) FROM HoaDonChiTiet hdct WHERE hdct.hoaDon.id_hoa_don = :idHoaDon")
    BigDecimal sumDonGiaByHoaDonId(@Param("idHoaDon") Integer idHoaDon);

    @Query(value = """
            SELECT
            \thdct.id_hoa_don_chi_tiet,
            \thdct.id_hoa_don,
            \tctsp.id_chi_tiet_san_pham,
            \tsp.ma_san_pham,
            \tsp.ten_san_pham,
            \tsp.anh_dai_dien as hinh_anh,
            \thdct.so_luong,
            \tctsp.so_luong AS so_luong_ton,
            \tCOALESCE((
            \t\tSELECT MIN(ctkm.gia_sau_giam)
            \t\tFROM chi_tiet_khuyen_mai ctkm
            \t\tJOIN khuyen_mai km ON ctkm.id_khuyen_mai = km.id_khuyen_mai
            \t\tWHERE ctkm.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
            \t\t\tAND km.trang_thai = N'Đang diễn ra'
            \t\t\tAND DATEADD(HOUR, 7, GETDATE()) BETWEEN km.ngay_bat_dau AND km.ngay_het_han
                ), ctsp.gia_ban) AS gia_ban,
            \thdct.don_gia,
            \tms.ten_mau_sac,
            \tkt.gia_tri,
            \tctsp.trang_thai AS trang_thai_ctsp,
            \tsp.trang_thai AS trang_thai_san_pham,
            \tctsp.so_luong AS so_luong_ton_kho
            FROM hoa_don_chi_tiet hdct
            JOIN chi_tiet_san_pham ctsp ON ctsp.id_chi_tiet_san_pham = hdct.id_chi_tiet_san_pham
            JOIN san_pham sp ON sp.id_san_pham = ctsp.id_san_pham
            LEFT JOIN kich_thuoc kt ON kt.id_kich_thuoc = ctsp.id_kich_thuoc
            LEFT JOIN mau_sac ms ON ms.id_mau_sac = ctsp.id_mau_sac
            WHERE hdct.id_hoa_don = :idHD
            """, nativeQuery = true)
    List<HoaDonChiTietResponse> getSPGH(Integer idHD);

    @Modifying
    @Transactional
    @Query(value = """
            BEGIN TRY
                BEGIN TRANSACTION;

                DECLARE @SOLUONG INT = :soLuong;
                DECLARE @IDCTSP INT = :idCTSP;
                DECLARE @IDHD INT = :idHD;
                DECLARE @TongTienTruocGiam DECIMAL(12,2);
                DECLARE @GiaTriGiamVoucher DECIMAL(12,2);
                DECLARE @SoLuongTon INT;

                -- Kiểm tra hóa đơn tồn tại
                IF NOT EXISTS (SELECT 1 FROM hoa_don WHERE id_hoa_don = @IDHD)
                    THROW 50001, N'Hóa đơn không tồn tại!', 1;

                -- Kiểm tra số lượng tồn kho
                SELECT @SoLuongTon = so_luong FROM chi_tiet_san_pham WHERE id_chi_tiet_san_pham = @IDCTSP;
                IF @SoLuongTon IS NULL
                    THROW 50002, N'Sản phẩm không tồn tại!', 1;
                IF @SoLuongTon < @SOLUONG
                    THROW 50003, N'Số lượng tồn kho không đủ!', 1;

                -- Lấy giá trị giảm từ voucher
                SELECT @GiaTriGiamVoucher = ISNULL(vc.gia_tri_giam, 0)
                FROM hoa_don hd
                LEFT JOIN voucher vc ON vc.id_voucher = hd.id_voucher
                WHERE hd.id_hoa_don = @IDHD;

                DECLARE @GiaSauGiam DECIMAL(12,2) = :giaBan;

                -- Cập nhật hoặc thêm mới chi tiết hóa đơn
                IF EXISTS (
                    SELECT 1
                    FROM hoa_don_chi_tiet
                    WHERE id_hoa_don = @IDHD
                    AND id_chi_tiet_san_pham = @IDCTSP
                )
                BEGIN
                    UPDATE hoa_don_chi_tiet
                    SET so_luong = so_luong + @SOLUONG,
                        don_gia = (so_luong + @SOLUONG) * @GiaSauGiam
                    WHERE id_chi_tiet_san_pham = @IDCTSP
                    AND id_hoa_don = @IDHD;
                END
                ELSE
                BEGIN
                    INSERT INTO hoa_don_chi_tiet (id_hoa_don, id_chi_tiet_san_pham, so_luong, don_gia)
                    VALUES (@IDHD, @IDCTSP, @SOLUONG, @SOLUONG * @GiaSauGiam);
                END;

                -- Cập nhật số lượng tồn kho
                UPDATE chi_tiet_san_pham
                SET so_luong = so_luong - @SOLUONG
                WHERE id_chi_tiet_san_pham = @IDCTSP;

                -- Tính tổng tiền trước giảm
                SELECT @TongTienTruocGiam = hd.phi_van_chuyen + ISNULL(SUM(hdct.don_gia), 0)
                FROM hoa_don hd
                LEFT JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = hd.id_hoa_don
                WHERE hd.id_hoa_don = @IDHD
                GROUP BY hd.id_hoa_don, hd.phi_van_chuyen;

                -- Cập nhật tổng tiền hóa đơn
                UPDATE hoa_don
                SET tong_tien_truoc_giam = @TongTienTruocGiam,
                    tong_tien_sau_giam = @TongTienTruocGiam - @GiaTriGiamVoucher
                WHERE id_hoa_don = @IDHD;

                COMMIT;
            END TRY
            BEGIN CATCH
                ROLLBACK;
                THROW;
            END CATCH;
            """, nativeQuery = true)
    void addSPHD(@RequestParam("idHoaDon") Integer idHD,
            @RequestParam("idCTSP") Integer idCTSP,
            @RequestParam("soLuong") Integer soLuong,
            @RequestParam("giaBan") BigDecimal giaBan);

    @Modifying
    @Transactional
    @Query(value = """
            BEGIN TRANSACTION;

            -- Khai báo các biến
            DECLARE @SOLUONG INT = :soLuong; -- Số lượng sản phẩm cần giảm
            DECLARE @IDCTSP INT = :idCTSP;  -- ID chi tiết sản phẩm
            DECLARE @IDHD INT = :idHD;   -- ID hóa đơn

            -- Khai báo biến để tìm voucher tốt nhất và tổng tiền trước giảm
            DECLARE @TongTienTruocGiam DECIMAL(18,2);
            DECLARE @GiaTriGiamVoucher DECIMAL(18,2); -- Biến để lưu giá trị giảm từ voucher
            DECLARE @PHIVANCHUYEN DECIMAL(18,2);

            IF NOT EXISTS (SELECT 1 FROM hoa_don WHERE id_hoa_don = @IDHD)
            BEGIN
                PRINT N'Hóa đơn không tồn tại!';
                ROLLBACK;
                RETURN;
            END;

            -- Tính giá sau khi áp dụng khuyến mãi cho sản phẩm
            DECLARE @GiaSauGiam DECIMAL(18,2);

            SELECT @GiaSauGiam = ( select
                CASE\s
                    WHEN km.kieu_giam_gia = N'Phần trăm' AND km.trang_thai = N'Đang diễn ra' THEN\s
                        IIF(gia_ban - IIF((gia_ban * COALESCE(km.gia_tri_giam, 0) / 100) > COALESCE(km.gia_tri_toi_da, gia_ban),\s
                            COALESCE(km.gia_tri_toi_da, gia_ban),\s
                            (gia_ban * COALESCE(km.gia_tri_giam, 0) / 100)) < 0,\s
                            0,\s
                            gia_ban - IIF((gia_ban * COALESCE(km.gia_tri_giam, 0) / 100) > COALESCE(km.gia_tri_toi_da, gia_ban),\s
                                COALESCE(km.gia_tri_toi_da, gia_ban),\s
                                (gia_ban * COALESCE(km.gia_tri_giam, 0) / 100)))
                    WHEN km.kieu_giam_gia = N'Tiền mặt' AND km.trang_thai = N'Đang diễn ra' THEN\s
                        IIF(gia_ban - IIF(COALESCE(km.gia_tri_giam, 0) > COALESCE(km.gia_tri_toi_da, gia_ban),\s
                            COALESCE(km.gia_tri_toi_da, gia_ban),\s
                            COALESCE(km.gia_tri_giam, 0)) < 0,\s
                            0,\s
                            gia_ban - IIF(COALESCE(km.gia_tri_giam, 0) > COALESCE(km.gia_tri_toi_da, gia_ban),\s
                                COALESCE(km.gia_tri_toi_da, gia_ban),\s
                                COALESCE(km.gia_tri_giam, 0)))
                    ELSE gia_ban
                END AS gia_sau_giam
            FROM chi_tiet_san_pham ctsp
            FULL OUTER JOIN san_pham sp ON sp.id_san_pham = ctsp.id_san_pham
            FULL OUTER JOIN chi_tiet_khuyen_mai ctkm ON ctkm.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
            FULL OUTER JOIN khuyen_mai km ON km.id_khuyen_mai = ctkm.id_khuyen_mai
            WHERE ctsp.trang_thai = 1 AND ctsp.id_chi_tiet_san_pham = @IDCTSP)

            -- Lấy phí vận chuyển từ hoa_don
            SELECT @PHIVANCHUYEN = phi_van_chuyen FROM hoa_don WHERE id_hoa_don = @IDHD;

            -- Kiểm tra xem sản phẩm đã tồn tại trong chi tiết hóa đơn chưa
            IF EXISTS (
                SELECT 1\s
                FROM hoa_don_chi_tiet\s
                WHERE id_hoa_don = @IDHD\s
                AND id_chi_tiet_san_pham = @IDCTSP
            )
            BEGIN
                -- Kiểm tra số lượng hiện tại đủ để giảm không
                DECLARE @SoLuongHienTai INT;
                SELECT @SoLuongHienTai = so_luong\s
                FROM hoa_don_chi_tiet\s
                WHERE id_hoa_don = @IDHD\s
                AND id_chi_tiet_san_pham = @IDCTSP;

                IF @SoLuongHienTai < @SOLUONG
                BEGIN
                    PRINT N'Số lượng trong hóa đơn không đủ để giảm!';
                    ROLLBACK;
                    RETURN;
                END;

                -- Cập nhật số lượng và đơn giá trong hoa_don_chi_tiet
                UPDATE hoa_don_chi_tiet
                SET
                    so_luong = so_luong - @SOLUONG,
                    don_gia = (so_luong - @SOLUONG) * @GiaSauGiam
                WHERE id_hoa_don = @IDHD\s
                AND id_chi_tiet_san_pham = @IDCTSP;

                -- Nếu số lượng sau khi giảm bằng 0, xóa bản ghi
                DELETE FROM hoa_don_chi_tiet
                WHERE id_hoa_don = @IDHD\s
                AND id_chi_tiet_san_pham = @IDCTSP\s
                AND so_luong = 0;
            END
            ELSE
            BEGIN
                PRINT N'Sản phẩm không tồn tại trong hóa đơn để giảm!';
                ROLLBACK;
                RETURN;
            END;

            -- Tính tổng tiền trước giảm sau khi giảm sản phẩm
            SELECT @TongTienTruocGiam = @PHIVANCHUYEN + ISNULL(SUM(don_gia), 0)
            FROM hoa_don hd
            LEFT JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = hd.id_hoa_don
            WHERE hd.id_hoa_don = @IDHD
            GROUP BY hd.id_hoa_don, hd.phi_van_chuyen;

            -- Cập nhật tổng tiền trong hoa_don
            UPDATE hoa_don
            SET
                tong_tien_truoc_giam = @TongTienTruocGiam,
                tong_tien_sau_giam = @TongTienTruocGiam
            WHERE id_hoa_don = @IDHD;

            -- Cập nhật số lượng trong chi_tiet_san_pham
            UPDATE chi_tiet_san_pham
            SET
                so_luong = so_luong + @SOLUONG
            WHERE id_chi_tiet_san_pham = @IDCTSP;


            COMMIT;
            """, nativeQuery = true)
    void giamSPHD(@RequestParam(value = "idHoaDon") Integer idHD,
            @RequestParam(value = "idCTSP") Integer idCTSP,
            @RequestParam(value = "soLuong") Integer soLuong,
            @RequestParam(value = "giaBan") BigDecimal giaBan);

    @Modifying
    @Transactional
    @Query(value = """
                BEGIN TRY
                    BEGIN TRANSACTION;
                    DECLARE @IDCTSP INT = :idCTSP;
                    DECLARE @IDHD INT = :idHD;
                    DECLARE @TongTienTruocGiam DECIMAL(18,2) = 0;
                    DECLARE @PHIVANCHUYEN DECIMAL(18,2) = 0;
                    DECLARE @SoLuongXoa INT = 0;

                    IF NOT EXISTS (SELECT 1 FROM hoa_don WHERE id_hoa_don = @IDHD)
                        THROW 50001, N'Hóa đơn không tồn tại!', 1;

                    IF NOT EXISTS (
                        SELECT 1
                        FROM hoa_don_chi_tiet
                        WHERE id_hoa_don = @IDHD
                          AND id_chi_tiet_san_pham = @IDCTSP
                    )
                        THROW 50002, N'Sản phẩm không tồn tại trong hóa đơn để xóa!', 1;

                    SELECT @SoLuongXoa = so_luong
                    FROM hoa_don_chi_tiet
                    WHERE id_hoa_don = @IDHD
                      AND id_chi_tiet_san_pham = @IDCTSP;

                    DELETE FROM hoa_don_chi_tiet
                    WHERE id_hoa_don = @IDHD
                      AND id_chi_tiet_san_pham = @IDCTSP;

                    SELECT @PHIVANCHUYEN = ISNULL(phi_van_chuyen, 0)
                    FROM hoa_don
                    WHERE id_hoa_don = @IDHD;

                    SELECT @TongTienTruocGiam = ISNULL(SUM(ISNULL(don_gia, 0)), 0)
                    FROM hoa_don_chi_tiet
                    WHERE id_hoa_don = @IDHD;

                    SET @TongTienTruocGiam = ISNULL(@TongTienTruocGiam, 0) + ISNULL(@PHIVANCHUYEN, 0);

                    UPDATE hoa_don
                    SET tong_tien_truoc_giam = @TongTienTruocGiam,
                        tong_tien_sau_giam = @TongTienTruocGiam
                    WHERE id_hoa_don = @IDHD;

                    UPDATE chi_tiet_san_pham
                    SET so_luong = so_luong + @SoLuongXoa
                    WHERE id_chi_tiet_san_pham = @IDCTSP;

                    COMMIT;
                END TRY
                BEGIN CATCH
                    ROLLBACK;
                    THROW;
                END CATCH;
            """, nativeQuery = true)
    void xoaSPKhoiHD(@Param("idHD") Integer idHoaDon, @Param("idCTSP") Integer idChiTietSanPham);

    @Modifying
    @Transactional
    @Query(value = """
            delete hoa_don_chi_tiet
            where id_hoa_don = :idHD
            """, nativeQuery = true)
    void deleteHDCTById(@RequestParam("idHoaDon") Integer idHD);

    @Query("SELECT h FROM HoaDonChiTiet h WHERE h.hoaDon.id_hoa_don = :idHoaDon")
    List<HoaDonChiTiet> findByIdHoaDon(@Param("idHoaDon") Integer idHoaDon);

    @Query(value = """
            SELECT * FROM hoa_don_chi_tiet
            WHERE id_hoa_don = :idHoaDon
            AND id_chi_tiet_san_pham = :idChiTietSanPham
            """, nativeQuery = true)
    List<HoaDonChiTiet> findByHoaDonAndChiTietSanPham(
            @Param("idHoaDon") Integer idHoaDon,
            @Param("idChiTietSanPham") Integer idChiTietSanPham);

    // ✅ NEW: Tìm sản phẩm theo cả ID và đơn giá để xử lý trường hợp đa giá
    @Query(value = """
            SELECT * FROM hoa_don_chi_tiet
            WHERE id_hoa_don = :idHoaDon
            AND id_chi_tiet_san_pham = :idChiTietSanPham
            AND don_gia = :donGia
            """, nativeQuery = true)
    Optional<HoaDonChiTiet> findByHoaDonAndChiTietSanPhamAndDonGia(
            @Param("idHoaDon") Integer idHoaDon,
            @Param("idChiTietSanPham") Integer idChiTietSanPham,
            @Param("donGia") BigDecimal donGia);

    // ✅ NEW: Lấy tất cả các dòng của cùng một sản phẩm trong hóa đơn
    @Query(value = """
            SELECT * FROM hoa_don_chi_tiet
            WHERE id_hoa_don = :idHoaDon
            AND id_chi_tiet_san_pham = :idChiTietSanPham
            ORDER BY id_hoa_don_chi_tiet
            """, nativeQuery = true)
    List<HoaDonChiTiet> findAllByHoaDonAndChiTietSanPham(
            @Param("idHoaDon") Integer idHoaDon,
            @Param("idChiTietSanPham") Integer idChiTietSanPham);

    @Query(value = """
            select hdct.id_hoa_don_chi_tiet, hdct.id_hoa_don, hdct.id_chi_tiet_san_pham, hdct.don_gia, hdct.so_luong from hoa_don_chi_tiet hdct join hoa_don hd on hd.id_hoa_don = hdct.id_hoa_don
            where hd.trang_thai=N'Đang chờ'
            and hdct.id_chi_tiet_san_pham=:idCTSP
            """, nativeQuery = true)

    List<HoaDonChiTiet> findHDCTDangChoByCTSP(@Param("idCTSP") Integer idCTSP);

}
