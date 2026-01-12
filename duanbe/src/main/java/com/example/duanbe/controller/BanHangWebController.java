package com.example.duanbe.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import com.example.duanbe.config.TimezoneConfig;
import com.example.duanbe.entity.ChiTietSanPham;
import com.example.duanbe.entity.HoaDon;
import com.example.duanbe.entity.HoaDonChiTiet;
import com.example.duanbe.entity.TheoDoiDonHang;
import com.example.duanbe.entity.Voucher;
import com.example.duanbe.repository.ChiTietSanPhamRepo;
import com.example.duanbe.repository.GioHangRepository;
import com.example.duanbe.repository.GioHangWebRepo;
import com.example.duanbe.repository.HoaDonChiTietRepo;
import com.example.duanbe.repository.HoaDonRepo;
import com.example.duanbe.repository.KhachHangRepo;
import com.example.duanbe.repository.TheoDoiDonHangRepo;
import com.example.duanbe.repository.VoucherRepository;
import com.example.duanbe.request.HoaDonRequest;
import com.example.duanbe.response.HoaDonChiTietResponse;
import com.example.duanbe.response.HoaDonResponse;
import com.example.duanbe.response.VoucherBHResponse;
import com.example.duanbe.service.GioHangService;
import com.example.duanbe.service.VoucherService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
    RequestMethod.PUT, RequestMethod.DELETE })
@RequestMapping("/banhangweb")
public class BanHangWebController {
  @Autowired
  HoaDonRepo hoaDonRepo;
  @Autowired
  VoucherRepository voucherRepository;
  @Autowired
  HoaDonChiTietRepo hoaDonChiTietRepo;
  @Autowired
  ChiTietSanPhamRepo chiTietSanPhamRepo;
  @Autowired
  TheoDoiDonHangRepo theoDoiDonHangRepo;
  @Autowired
  private JavaMailSender mailSender;
  @Autowired
  private VoucherService voucherService;
  @Autowired
  private KhachHangRepo khachHangRepo;
  @Autowired
  private GioHangRepository gioHangRepository;
  @Autowired
  private GioHangWebRepo gioHangWebRepo;
  @Autowired
  private GioHangService gioHangService;
  private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  private String generateUniqueMaHoaDon() {
    Random random = new Random();
    String maHoaDon;
    boolean isDuplicate;
    do {
      StringBuilder code = new StringBuilder("HD");
      for (int i = 0; i < 6; i++) {
        code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
      }
      maHoaDon = code.toString();

      final String finalMaHoaDon = maHoaDon;
      isDuplicate = hoaDonRepo.findAll().stream()
          .anyMatch(hd -> finalMaHoaDon.equalsIgnoreCase(hd.getMa_hoa_don()));

    } while (isDuplicate);

    return maHoaDon;
  }

  Integer idHoaDon = 0;
  Integer idKhachHang = 0;
  Boolean xacNhan = false;

  // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_QL', 'ROLE_NV')") public
  @PostMapping("/taoHoaDonWeb")
  public ResponseEntity<?> taoHoaDonWeb(@RequestBody HoaDonRequest hoaDon) {
    HoaDon hoaDonAdd = new HoaDon();
    BeanUtils.copyProperties(hoaDon, hoaDonAdd);
    hoaDonAdd.setMa_hoa_don(generateUniqueMaHoaDon());
    hoaDonAdd.setLoai_hoa_don("Online");
    hoaDonAdd.setPhuong_thuc_nhan_hang("Giao hàng");
    hoaDonAdd.setSdt(hoaDon.getSdt_nguoi_nhan());
    hoaDonAdd.setNgay_tao(LocalDateTime.now());
    hoaDonAdd.setNgay_sua(LocalDateTime.now());

    // 🔍 DEBUG LOGGING - STEP 1: Check delivery method from request
    System.out.println("🔍 [DEBUG] BanHangWebController.createOrder() - Checking delivery method");
    System.out.println("  - Delivery method from request: '" + hoaDon.getPhuong_thuc_nhan_hang() + "'");

    // ❌ REMOVED HARDCODE - Use FE value instead
    // OLD: hoaDonAdd.setPhuong_thuc_nhan_hang("Giao hàng");
    hoaDonAdd.setPhuong_thuc_nhan_hang(hoaDon.getPhuong_thuc_nhan_hang());

    System.out.println("  - Setting delivery method to: '" + hoaDonAdd.getPhuong_thuc_nhan_hang() + "'");

    hoaDonAdd.setVoucher(
        hoaDon.getVoucher().getId() != 0 ? voucherRepository.findById(hoaDon.getVoucher().getId()).get()
            : null);
    hoaDonAdd.setKhachHang(
        hoaDon.getId_khach_hang() == 0 ? null : khachHangRepo.findById(hoaDon.getId_khach_hang()).get());
    hoaDonRepo.save(hoaDonAdd);

    // 🔍 DEBUG LOGGING - STEP 2: After Save Verification
    System.out.println("🔍 [DEBUG] BanHangWebController - State after save:");
    System.out.println("  - Saved Invoice ID: " + hoaDonAdd.getId_hoa_don());
    System.out.println("  - Saved Delivery Method: '" + hoaDonAdd.getPhuong_thuc_nhan_hang() + "'");
    System.out.println("  - Saved Order Type: '" + hoaDonAdd.getLoai_hoa_don() + "'");

    idHoaDon = hoaDonAdd.getId_hoa_don();
    idKhachHang = hoaDonAdd.getKhachHang() == null || hoaDonAdd.getKhachHang().getIdKhachHang() == null ? 0
        : hoaDonAdd.getKhachHang().getIdKhachHang();
    xacNhan = hoaDon.getIsChuyen();
    TheoDoiDonHang theoDoiDonHang = new TheoDoiDonHang();
    theoDoiDonHang.setHoaDon(hoaDonAdd);
    theoDoiDonHang.setTrang_thai("Chờ xác nhận");
    theoDoiDonHang.setNgay_chuyen(LocalDateTime.now());
    theoDoiDonHangRepo.save(theoDoiDonHang);
    if (hoaDon.getVoucher().getId() != 0) {
      updateVoucherSoLuong(hoaDonAdd.getVoucher().getId());
    }
    sendEmail(hoaDonAdd.getEmail(), hoaDonAdd.getMa_hoa_don());
    if (hoaDon.getIsChuyen()) {
      TheoDoiDonHang theoDoiDonHang1 = new TheoDoiDonHang();
      theoDoiDonHang1.setHoaDon(hoaDonAdd);
      theoDoiDonHang1.setTrang_thai("Đã xác nhận");
      theoDoiDonHang1.setNgay_chuyen(LocalDateTime.now());
      theoDoiDonHangRepo.save(theoDoiDonHang1);
    }

    // ✅ FIXED: Return simple Map instead of entity to avoid Jackson lazy-loading
    // issues
    return ResponseEntity.ok(Map.of(
        "id_hoa_don", hoaDonAdd.getId_hoa_don(),
        "ma_hoa_don", hoaDonAdd.getMa_hoa_don(),
        "trang_thai", hoaDonAdd.getTrang_thai(),
        "id_khach_hang", idKhachHang,
        "tong_tien_sau_giam",
        hoaDonAdd.getTong_tien_sau_giam() != null ? hoaDonAdd.getTong_tien_sau_giam() : BigDecimal.ZERO));
  }

  @PostMapping("/taoHoaDonWebTreo")
  public ResponseEntity<?> taoHoaDonWebTreo(@RequestBody HoaDonRequest hoaDon) {
    HoaDon hoaDonAdd = new HoaDon();
    BeanUtils.copyProperties(hoaDon, hoaDonAdd);
    hoaDonAdd.setMa_hoa_don(generateUniqueMaHoaDon());
    hoaDonAdd.setLoai_hoa_don("Online");
    hoaDonAdd.setPhuong_thuc_nhan_hang("Giao hàng");
    hoaDonAdd.setNgay_tao(LocalDateTime.now(TimezoneConfig.VIETNAM_ZONE));
    hoaDonAdd.setNgay_sua(LocalDateTime.now(TimezoneConfig.VIETNAM_ZONE));

    hoaDonAdd.setSdt(hoaDon.getSdt_nguoi_nhan());

    // 🔍 DEBUG LOGGING - STEP 1: Check delivery method from request
    System.out.println("🔍 [DEBUG] BanHangWebController.createOrder() - Checking delivery method");
    System.out.println("  - Delivery method from request: '" + hoaDon.getPhuong_thuc_nhan_hang() + "'");

    // ❌ REMOVED HARDCODE - Use FE value instead
    // OLD: hoaDonAdd.setPhuong_thuc_nhan_hang("Giao hàng");
    hoaDonAdd.setPhuong_thuc_nhan_hang(hoaDon.getPhuong_thuc_nhan_hang());

    System.out.println("  - Setting delivery method to: '" + hoaDonAdd.getPhuong_thuc_nhan_hang() + "'");

    hoaDonAdd.setVoucher(
        hoaDon.getVoucher().getId() != 0 ? voucherRepository.findById(hoaDon.getVoucher().getId()).get()
            : null);
    hoaDonAdd.setKhachHang(
        hoaDon.getId_khach_hang() == 0 ? null : khachHangRepo.findById(hoaDon.getId_khach_hang()).get());
    hoaDonAdd.setTrang_thai("Đang chờ thanh toán");
    hoaDonRepo.save(hoaDonAdd);

    // 🔍 DEBUG LOGGING - STEP 2: After Save Verification
    System.out.println("🔍 [DEBUG] BanHangWebController - State after save:");
    System.out.println("  - Saved Invoice ID: " + hoaDonAdd.getId_hoa_don());
    System.out.println("  - Saved Delivery Method: '" + hoaDonAdd.getPhuong_thuc_nhan_hang() + "'");
    System.out.println("  - Saved Order Type: '" + hoaDonAdd.getLoai_hoa_don() + "'");

    idHoaDon = hoaDonAdd.getId_hoa_don();
    idKhachHang = hoaDonAdd.getKhachHang() == null || hoaDonAdd.getKhachHang().getIdKhachHang() == null ? 0
        : hoaDonAdd.getKhachHang().getIdKhachHang();
    xacNhan = hoaDon.getIsChuyen();
    TheoDoiDonHang theoDoiDonHang = new TheoDoiDonHang();
    theoDoiDonHang.setHoaDon(hoaDonAdd);
    theoDoiDonHang.setTrang_thai("Chờ xác nhận");
    theoDoiDonHang.setNgay_chuyen(LocalDateTime.now());
    theoDoiDonHangRepo.save(theoDoiDonHang);
    if (hoaDon.getVoucher().getId() != 0) {
      updateVoucherSoLuong(hoaDonAdd.getVoucher().getId());
    }
    // sendEmail(hoaDonAdd.getEmail(), hoaDonAdd.getMa_hoa_don());
    // if (hoaDon.getIsChuyen()) {
    // TheoDoiDonHang theoDoiDonHang1 = new TheoDoiDonHang();
    // theoDoiDonHang1.setHoaDon(hoaDonAdd);
    // theoDoiDonHang1.setTrang_thai("Đã xác nhận");
    // theoDoiDonHang1.setNgay_chuyen(LocalDateTime.now(TimezoneConfig.VIETNAM_ZONE));
    // theoDoiDonHangRepo.save(theoDoiDonHang1);
    // }

    // ✅ FIXED: Return simple Map instead of entity to avoid Jackson lazy-loading
    // issues--
    return ResponseEntity.ok(Map.of(
        "id_hoa_don", hoaDonAdd.getId_hoa_don(),
        "ma_hoa_don", hoaDonAdd.getMa_hoa_don(),
        "trang_thai", hoaDonAdd.getTrang_thai(),
        "id_khach_hang", idKhachHang,
        "tong_tien_sau_giam",
        hoaDonAdd.getTong_tien_sau_giam() != null ? hoaDonAdd.getTong_tien_sau_giam() : BigDecimal.ZERO));
  }

  private void updateVoucherSoLuong(Integer idVoucher) {
    Voucher vc = voucherRepository.findById(idVoucher).get();
    vc.setSoLuong(vc.getSoLuong() - 1);
    voucherRepository.save(vc);
  }

  private void updateSoLuongSanPham(List<HoaDonChiTiet> list) {
    // ✅ STOCK CONCURRENCY FIX: Validate stock BEFORE deducting
    for (HoaDonChiTiet hdct : list) {
      ChiTietSanPham ctsp = chiTietSanPhamRepo.findById(hdct.getChiTietSanPham().getId_chi_tiet_san_pham()).get();

      // Check if stock is sufficient
      if (ctsp.getSo_luong() < hdct.getSo_luong()) {
        throw new com.example.duanbe.exception.InsufficientStockException(
            String.format("Sản phẩm \"%s\" chỉ còn %d trong kho, không đủ để bán %d!",
                ctsp.getSanPham().getTen_san_pham(),
                ctsp.getSo_luong(),
                hdct.getSo_luong()),
            ctsp.getId_chi_tiet_san_pham(),
            hdct.getSo_luong(),
            ctsp.getSo_luong());
      }
    }

    // ✅ If all items have sufficient stock, deduct
    for (HoaDonChiTiet hdct : list) {
      ChiTietSanPham ctsp = chiTietSanPhamRepo.findById(hdct.getChiTietSanPham().getId_chi_tiet_san_pham()).get();
      ctsp.setSo_luong(ctsp.getSo_luong() - hdct.getSo_luong());
      // ⛔ KHÔNG tự động tắt trạng thái khi hết hàng - để admin quản lý thủ công
      chiTietSanPhamRepo.save(ctsp);
    }
  }

  @PostMapping("/taoHoaDonWeb1")
  public ResponseEntity<?> taoHoaDonWeb1(@RequestBody HoaDonRequest hoaDon) {
    // HoaDon hoaDonAdd = new HoaDon();
    // BeanUtils.copyProperties(hoaDon, hoaDonAdd);
    if (hoaDon.getId_hoa_don() == null || hoaDon.getId_hoa_don() == 0) {
      return ResponseEntity.badRequest().build();
    }
    HoaDon hoaDonAdd = hoaDonRepo.findById(hoaDon.getId_hoa_don()).get();
    // hoaDonAdd.setMa_hoa_don(generateUniqueMaHoaDon());
    hoaDonAdd.setLoai_hoa_don("Online");
    // hoaDonAdd.setNgay_tao(LocalDateTime.now());
    hoaDonAdd.setNgay_sua(LocalDateTime.now(TimezoneConfig.VIETNAM_ZONE));
    // 🔍 DEBUG LOGGING - Check delivery method from request
    System.out.println("🔍 [DEBUG] BanHangWebController - Checking delivery method");
    System.out.println("  - Delivery method from request: '" + hoaDon.getPhuong_thuc_nhan_hang() + "'");

    // ❌ REMOVED HARDCODE - Use FE value instead
    // OLD: hoaDonAdd.setPhuong_thuc_nhan_hang("Giao hàng");
    hoaDonAdd.setPhuong_thuc_nhan_hang(hoaDon.getPhuong_thuc_nhan_hang());

    System.out.println("  - Setting delivery method to: '" + hoaDonAdd.getPhuong_thuc_nhan_hang() + "'");
    // hoaDonAdd.setVoucher(
    // hoaDon.getVoucher().getId() != null ?
    // voucherRepository.findById(hoaDon.getVoucher().getId()).get()
    // : null);
    hoaDonAdd.setKhachHang(hoaDon.getKhachHang().getIdKhachHang() == 0 ? null
        : khachHangRepo.findById(hoaDon.getKhachHang().getIdKhachHang()).get());
    hoaDonAdd.setTrang_thai("Hoàn thành");
    hoaDonRepo.save(hoaDonAdd);

    // 🔍 DEBUG LOGGING - STEP 2: After Save Verification (suaHoaDon)
    System.out.println("🔍 [DEBUG] BanHangWebController.suaHoaDon() - State after save:");
    System.out.println("  - Saved Invoice ID: " + hoaDonAdd.getId_hoa_don());
    System.out.println("  - Saved Delivery Method: '" + hoaDonAdd.getPhuong_thuc_nhan_hang() + "'");
    System.out.println("  - Saved Order Type: '" + hoaDonAdd.getLoai_hoa_don() + "'");

    idHoaDon = hoaDonAdd.getId_hoa_don();
    if (hoaDon.getIsChuyen()) {
      // ✅ CHECK: Prevent duplicate tracking entry
      List<TheoDoiDonHang> existingTracking = theoDoiDonHangRepo.findByIdHoaDon(hoaDonAdd.getId_hoa_don());
      boolean alreadyConfirmed = existingTracking.stream()
          .anyMatch(t -> "Đã xác nhận".equals(t.getTrang_thai()));

      if (!alreadyConfirmed) {
        TheoDoiDonHang theoDoiDonHang = new TheoDoiDonHang();
        theoDoiDonHang.setHoaDon(hoaDonAdd);
        theoDoiDonHang.setTrang_thai("Đã xác nhận");
        theoDoiDonHang.setNgay_chuyen(LocalDateTime.now(TimezoneConfig.VIETNAM_ZONE));
        theoDoiDonHangRepo.save(theoDoiDonHang);
        sendEmail(hoaDonAdd.getEmail(), hoaDonAdd.getMa_hoa_don());
        System.out.println("✅ Created new tracking entry: Đã xác nhận");
      } else {
        System.out.println("⏭️ Skipped duplicate tracking - already confirmed by callback");
      }
    }
    return ResponseEntity.ok(hoaDonAdd);
  }

  @PostMapping("/taoHoaDonChiTiet")
  public ResponseEntity<?> taoHoaDonChiTiet(@RequestBody List<HoaDonChiTiet> hoaDonChiTiets) {
    // ✅ STOCK CONCURRENCY FIX: Validate stock BEFORE creating order (for both COD
    // and Online)
    for (HoaDonChiTiet hdct : hoaDonChiTiets) {
      ChiTietSanPham ctsp = chiTietSanPhamRepo.findById(hdct.getChiTietSanPham().getId_chi_tiet_san_pham())
          .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

      // Check if stock is sufficient
      if (ctsp.getSo_luong() < hdct.getSo_luong()) {
        throw new com.example.duanbe.exception.InsufficientStockException(
            String.format("Sản phẩm \"%s\" chỉ còn %d trong kho, không đủ để bán %d!",
                ctsp.getSanPham().getTen_san_pham(),
                ctsp.getSo_luong(),
                hdct.getSo_luong()),
            ctsp.getId_chi_tiet_san_pham(),
            hdct.getSo_luong(),
            ctsp.getSo_luong());
      }
    }

    ArrayList<HoaDonChiTiet> listHdct = new ArrayList<>();
    for (HoaDonChiTiet hdct : hoaDonChiTiets) {
      HoaDonChiTiet hoaDonChiTietAdd = new HoaDonChiTiet();
      hoaDonChiTietAdd.setHoaDon(hoaDonRepo.findById(idHoaDon).get());
      System.out.println("id Hoá đơn: fdfdfd: " + idHoaDon);
      hoaDonChiTietAdd.setChiTietSanPham(
          chiTietSanPhamRepo.findById(hdct.getChiTietSanPham().getId_chi_tiet_san_pham()).orElseThrow());
      hoaDonChiTietAdd.setSo_luong(hdct.getSo_luong());
      hoaDonChiTietAdd.setDon_gia(hdct.getDon_gia());

      hoaDonChiTietRepo.save(hoaDonChiTietAdd);
      listHdct.add(hoaDonChiTietAdd);
    }
    if (xacNhan) {
      updateSoLuongSanPham(listHdct);
    }
    if (idKhachHang != 0) {
      for (HoaDonChiTiet hdct : listHdct) {
        gioHangService.xoaSanPhamKhoiGioHang(idKhachHang, hdct.getChiTietSanPham().getId_chi_tiet_san_pham());
      }
    }
    return ResponseEntity.ok(listHdct);
  }

  @PostMapping("/taoHoaDonChiTietMuaNgay")
  public ResponseEntity<?> taoHoaDonChiTietMuaNgay(@RequestBody List<HoaDonChiTiet> hoaDonChiTiets) {
    // ✅ STOCK CONCURRENCY FIX: Validate stock BEFORE creating order
    for (HoaDonChiTiet hdct : hoaDonChiTiets) {
      ChiTietSanPham ctsp = chiTietSanPhamRepo.findById(hdct.getChiTietSanPham().getId_chi_tiet_san_pham())
          .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

      // Check if stock is sufficient
      if (ctsp.getSo_luong() < hdct.getSo_luong()) {
        throw new com.example.duanbe.exception.InsufficientStockException(
            String.format("Sản phẩm \"%s\" chỉ còn %d trong kho, không đủ để bán %d!",
                ctsp.getSanPham().getTen_san_pham(),
                ctsp.getSo_luong(),
                hdct.getSo_luong()),
            ctsp.getId_chi_tiet_san_pham(),
            hdct.getSo_luong(),
            ctsp.getSo_luong());
      }
    }

    ArrayList<HoaDonChiTiet> listHdct = new ArrayList<>();
    for (HoaDonChiTiet hdct : hoaDonChiTiets) {
      HoaDonChiTiet hoaDonChiTietAdd = new HoaDonChiTiet();
      hoaDonChiTietAdd.setHoaDon(hoaDonRepo.findById(idHoaDon).get());
      System.out.println("id Hoá đơn: fdfdfd: " + idHoaDon);
      hoaDonChiTietAdd.setChiTietSanPham(
          chiTietSanPhamRepo.findById(hdct.getChiTietSanPham().getId_chi_tiet_san_pham()).orElseThrow());
      hoaDonChiTietAdd.setSo_luong(hdct.getSo_luong());
      hoaDonChiTietAdd.setDon_gia(hdct.getDon_gia());

      hoaDonChiTietRepo.save(hoaDonChiTietAdd);
      listHdct.add(hoaDonChiTietAdd);
    }
    if (xacNhan) {
      updateSoLuongSanPham(listHdct);
    }
    return ResponseEntity.ok(listHdct);
  }

  //
  @PostMapping("/suaHoaDon")
  public ResponseEntity<?> suaHoaDon(@RequestBody HoaDon hoaDon) {
    System.out.println("idHoaDonSua" + hoaDon.getId_hoa_don());
    HoaDon hoaDonAdd = new HoaDon();
    BeanUtils.copyProperties(hoaDon, hoaDonAdd);
    hoaDonAdd.setMa_hoa_don(generateUniqueMaHoaDon());
    hoaDonAdd.setLoai_hoa_don("Online");
    hoaDonAdd.setNgay_sua(LocalDateTime.now());

    // 🔍 DEBUG LOGGING - STEP 1: Check delivery method from request (suaHoaDon)
    System.out.println("🔍 [DEBUG] BanHangWebController.suaHoaDon() - Checking delivery method");
    System.out.println("  - Delivery method from request: '" + hoaDon.getPhuong_thuc_nhan_hang() + "'");

    // ❌ REMOVED HARDCODE - Use FE value instead
    // OLD: hoaDonAdd.setPhuong_thuc_nhan_hang("Giao hàng");
    hoaDonAdd.setPhuong_thuc_nhan_hang(hoaDon.getPhuong_thuc_nhan_hang());

    System.out.println("  - Setting delivery method to: '" + hoaDonAdd.getPhuong_thuc_nhan_hang() + "'");

    hoaDonAdd.setVoucher(
        hoaDon.getVoucher().getId() != null ? voucherRepository.findById(hoaDon.getVoucher().getId()).get()
            : null);
    hoaDonRepo.save(hoaDonAdd);

    // 🔍 DEBUG LOGGING - STEP 2: After Save Verification (suaHoaDon)
    System.out.println("🔍 [DEBUG] BanHangWebController.suaHoaDon() - State after save:");
    System.out.println("  - Saved Invoice ID: " + hoaDonAdd.getId_hoa_don());
    System.out.println("  - Saved Delivery Method: '" + hoaDonAdd.getPhuong_thuc_nhan_hang() + "'");
    System.out.println("  - Saved Order Type: '" + hoaDonAdd.getLoai_hoa_don() + "'");

    idHoaDon = hoaDonAdd.getId_hoa_don();
    TheoDoiDonHang theoDoiDonHang = new TheoDoiDonHang();
    theoDoiDonHang.setHoaDon(hoaDonAdd);
    theoDoiDonHang.setTrang_thai("Đã xác nhận");
    theoDoiDonHang.setNgay_chuyen(LocalDateTime.now());
    theoDoiDonHangRepo.save(theoDoiDonHang);
    sendEmail(hoaDonAdd.getEmail(), hoaDonAdd.getMa_hoa_don());
    return ResponseEntity.ok(hoaDonAdd);
  }

  private void sendEmail(String toEmail, String maHoaDon) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      String tenDN = toEmail.split("@")[0];
      helper.setTo(toEmail);
      helper.setSubject("✓ Xác nhận đơn hàng #" + maHoaDon + " - MenWear");

      // Professional HTML Email Template
      String body = "<!DOCTYPE html>"
          + "<html lang='vi'>"
          + "<head>"
          + "<meta charset='UTF-8'>"
          + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
          + "<style>"
          + "body { margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4; }"
          + ".email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }"
          + ".header { background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%); padding: 40px 20px; text-align: center; }"
          + ".logo { width: 180px; height: auto; margin-bottom: 20px; }"
          + ".header-text { color: #ffffff; font-size: 24px; font-weight: 700; margin: 0; letter-spacing: 2px; }"
          + ".content { padding: 40px 30px; }"
          + ".greeting { font-size: 18px; color: #2c3e50; margin-bottom: 20px; font-weight: 600; }"
          + ".message-box { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 12px; padding: 25px; margin: 25px 0; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3); }"
          + ".order-code-label { color: #ffffff; font-size: 14px; opacity: 0.9; margin-bottom: 8px; }"
          + ".order-code { color: #ffffff; font-size: 32px; font-weight: 700; letter-spacing: 3px; margin: 0; text-align: center; }"
          + ".info-card { background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 20px 0; border-radius: 8px; }"
          + ".info-title { color: #2c3e50; font-size: 16px; font-weight: 600; margin-bottom: 12px; }"
          + ".info-text { color: #555555; line-height: 1.8; margin: 8px 0; }"
          + ".button-container { text-align: center; margin: 30px 0; }"
          + ".track-button { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #ffffff; padding: 15px 40px; text-decoration: none; border-radius: 50px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4); transition: transform 0.3s ease; }"
          + ".track-button:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5); }"
          + ".support-box { background-color: #fff3cd; border: 1px solid #ffc107; border-radius: 8px; padding: 15px; margin: 25px 0; }"
          + ".support-text { color: #856404; margin: 5px 0; font-size: 14px; }"
          + ".footer { background-color: #2c3e50; color: #ffffff; padding: 30px; text-align: center; }"
          + ".footer-text { margin: 8px 0; font-size: 14px; opacity: 0.9; }"
          + ".social-links { margin: 20px 0; }"
          + ".social-link { display: inline-block; margin: 0 10px; color: #ffffff; text-decoration: none; }"
          + ".divider { height: 1px; background: linear-gradient(90deg, transparent, #ddd, transparent); margin: 30px 0; }"
          + "</style>"
          + "</head>"
          + "<body>"
          + "<div class='email-container'>"

          // Header with Logo
          + "<div class='header'>"
          + "<img src='https://res.cloudinary.com/dryt7bnjl/image/upload/v1768019485/anhLogoMenWear-1-removebg-preview_zoeaui.png' alt='MenWear Logo' class='logo'>"
          // + "<h1 class='header-text'>MENWEAR</h1>"
          + "</div>"

          // Main Content
          + "<div class='content'>"
          + "<p class='greeting'>Xin chào " + tenDN + ",</p>"
          + "<p class='info-text'>Cảm ơn bạn đã tin tưởng và mua sắm tại <strong>MenWear</strong> - Thương hiệu áo sơ mi nam cao cấp.</p>"

          // Order Code Box
          + "<div class='message-box'>"
          + "<div class='order-code-label'>Mã đơn hàng của bạn</div>"
          + "<h2 class='order-code'>" + maHoaDon + "</h2>"
          + "</div>"

          // Order Info
          + "<div class='info-card'>"
          + "<div class='info-title'>📦 Thông tin đơn hàng</div>"
          + "<p class='info-text'>✓ Đơn hàng của bạn đã được tiếp nhận và đang được xử lý</p>"
          + "<p class='info-text'>✓ Bạn sẽ nhận được email xác nhận khi đơn hàng được giao</p>"
          + "<p class='info-text'>✓ Thời gian giao hàng dự kiến: 2-3 ngày làm việc</p>"
          + "</div>"

          // Track Order Button
          + "<div class='button-container'>"
          + "<a href='http://localhost:5173/tracuudonhang-banhang?code=" + maHoaDon
          + "' class='track-button'>Theo dõi đơn hàng</a>"
          + "</div>"

          + "<div class='divider'></div>"

          // Support Info
          + "<div class='support-box'>"
          + "<p class='support-text'><strong>💬 Cần hỗ trợ?</strong></p>"
          + "<p class='support-text'>Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ:</p>"
          + "<p class='support-text'>📞 Hotline: 1900-xxxx | 📧 Email: support@menwear.vn</p>"
          + "</div>"

          // Closing
          + "<p class='info-text' style='margin-top: 30px;'>Trân trọng,</p>"
          + "<p class='info-text'><strong>Đội ngũ MenWear</strong></p>"
          + "</div>"

          // Footer
          + "<div class='footer'>"
          + "<p class='footer-text'><strong>MENWEAR</strong> - Phong cách lịch lãm, đẳng cấp quý ông</p>"
          + "<div class='social-links'>"
          + "<a href='#' class='social-link'>Facebook</a> | "
          + "<a href='#' class='social-link'>Instagram</a> | "
          + "<a href='#' class='social-link'>Zalo</a>"
          + "</div>"
          + "<p class='footer-text'>© 2024 MenWear. All rights reserved.</p>"
          + "<p class='footer-text' style='font-size: 12px; opacity: 0.7;'>Email này được gửi tự động, vui lòng không trả lời.</p>"
          + "</div>"

          + "</div>"
          + "</body>"
          + "</html>";

      helper.setText(body, true);
      mailSender.send(message);
    } catch (MessagingException e) {
      e.printStackTrace();
    }
  }

  @GetMapping("/thongTinHoaDonChiTiet")
  public List<HoaDonChiTietResponse> getTraCuuDonHang(@RequestParam("maHoaDon") String maHoaDon) {
    return hoaDonRepo.listThongTinHoaDon(maHoaDon);
  }

  @GetMapping("/thongTinTimeLine")
  public List<HoaDonChiTietResponse> getThongTinDonHang(@RequestParam("maHoaDon") String maHoaDon) {
    return hoaDonRepo.listTrangThaiTimeLineBanHangWeb(maHoaDon);
  }

  @GetMapping("/thongTinHoaDon")
  public HoaDonResponse getHoaDonByMaHoaDon(@RequestParam("maHoaDon") String maHoaDon) {
    return hoaDonRepo.getHoaDonByMaHoaDon(maHoaDon);
  }

  @GetMapping("/thongTinKhachHang")
  public List<HoaDonChiTietResponse> getThongTinKhachHang(@RequestParam("maHoaDon") String maHoaDon) {
    return hoaDonRepo.listThongTinKhachHang(maHoaDon);
  }

  @GetMapping("/voucherTheoGiaTruyen")
  public List<VoucherBHResponse> voucherTheoGiaTruyen(@RequestParam("giaTruyen") BigDecimal giaTruyen) {
    return voucherService.listVoucherTheoGiaTruyen(giaTruyen);
  }

  @GetMapping("/trangThaiCTSP")
  public Boolean getTrangThai(@RequestParam("idCTSP") Integer idCTSP) {
    return chiTietSanPhamRepo.findById(idCTSP).get().getTrang_thai() ? true : false;
  }
}
