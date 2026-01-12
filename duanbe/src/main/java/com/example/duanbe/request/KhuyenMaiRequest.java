package com.example.duanbe.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KhuyenMaiRequest {
    private Integer id;
    private String maKhuyenMai;
    private String tenKhuyenMai;
    private String moTa;

    // ✅ TIMEZONE FIX: Nhận LocalDateTime từ frontend (format: yyyy-MM-ddTHH:mm:ss)
    // Service layer sẽ convert sang OffsetDateTime với timezone Asia/Ho_Chi_Minh
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime ngayBatDau;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime ngayHetHan;

    private BigDecimal giaTriGiam;
    private String kieuGiamGia;
    private String trangThai;
    private BigDecimal giaTriToiDa;
}
