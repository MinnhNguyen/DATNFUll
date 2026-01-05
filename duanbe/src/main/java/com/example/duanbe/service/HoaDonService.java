package com.example.duanbe.service;

import com.example.duanbe.entity.HoaDon;
import com.example.duanbe.entity.HoaDonChiTiet;
import com.example.duanbe.repository.HoaDonChiTietRepo;
import com.example.duanbe.repository.HoaDonRepo;
import com.example.duanbe.response.HoaDonResponse;
import com.itextpdf.text.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class HoaDonService {
    @Autowired
    private HoaDonRepo hoaDonRepo;
    @Autowired
    private HoaDonChiTietRepo hoaDonChiTietRepo;

    // lềnh thay đổi
    public List<HoaDonResponse> getHoaDonByKhachHangId(Integer idKhachHang) {
        return hoaDonRepo.findHoaDonWithLatestStatusByKhachHangId(idKhachHang);
    }

    public int countHoaDonByKhachHangId(Integer idKhachHang) {
        return hoaDonRepo.countByKhachHangId(idKhachHang);
    }

    public void updateHDCTByIdCTSP(Integer idCTSP, BigDecimal giaMoi) {
        List<HoaDonChiTiet> hoaDonChiTietList = hoaDonChiTietRepo.findHDCTDangChoByCTSP(idCTSP);
        if (hoaDonChiTietList.isEmpty()) {
            return;
        }
        for (HoaDonChiTiet hoaDonChiTiet : hoaDonChiTietList) {
            if (hoaDonChiTiet.getChiTietSanPham().getId_chi_tiet_san_pham() == idCTSP ||
                    hoaDonChiTiet.getChiTietSanPham().getId_chi_tiet_san_pham().equals(idCTSP)) {
                hoaDonChiTiet.setDon_gia(giaMoi);
            }
            hoaDonChiTietRepo.save(hoaDonChiTiet);
        }
    }
}
