package com.example.duanbe.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.duanbe.service.FavoriteService;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.DELETE })
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToFavorites(
            @RequestParam("idKhachHang") Integer idKhachHang,
            @RequestParam("idChiTietSanPham") Integer idChiTietSanPham) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate input
            if (idKhachHang == null || idChiTietSanPham == null) {
                response.put("error", true);
                response.put("message", "ID khách hàng và ID sản phẩm là bắt buộc");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> result = favoriteService.addFavorite(idKhachHang, idChiTietSanPham);

            if ("error".equals(result.get("status"))) {
                response.put("error", true);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }

            response.put("error", false);
            response.put("message", result.get("message"));
            response.put("totalFavorites", result.get("totalFavorites"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Lỗi khi thêm vào danh sách yêu thích: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeFromFavorites(
            @RequestParam("idKhachHang") Integer idKhachHang,
            @RequestParam("idChiTietSanPham") Integer idChiTietSanPham) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate input
            if (idKhachHang == null || idChiTietSanPham == null) {
                response.put("error", true);
                response.put("message", "ID khách hàng và ID sản phẩm là bắt buộc");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> result = favoriteService.removeFavorite(idKhachHang, idChiTietSanPham);

            if ("error".equals(result.get("status"))) {
                response.put("error", true);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }

            response.put("error", false);
            response.put("message", result.get("message"));
            response.put("totalFavorites", result.get("totalFavorites"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Lỗi khi xóa khỏi danh sách yêu thích: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkFavoriteStatus(
            @RequestParam("idKhachHang") Integer idKhachHang,
            @RequestParam("idChiTietSanPham") Integer idChiTietSanPham) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate input
            if (idKhachHang == null || idChiTietSanPham == null) {
                response.put("error", true);
                response.put("message", "ID khách hàng và ID sản phẩm là bắt buộc");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> result = favoriteService.checkFavoriteStatus(idKhachHang, idChiTietSanPham);

            if ("error".equals(result.get("status"))) {
                response.put("error", true);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }

            response.put("error", false);
            response.put("message", "Kiểm tra trạng thái yêu thích thành công");
            response.put("isFavorite", result.get("isFavorite"));
            response.put("totalFavorites", result.get("totalFavorites"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Lỗi khi kiểm tra trạng thái yêu thích: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/count/{idChiTietSanPham}")
    public ResponseEntity<Map<String, Object>> getProductFavoriteCount(
            @PathVariable("idChiTietSanPham") Integer idChiTietSanPham) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate input
            if (idChiTietSanPham == null) {
                response.put("error", true);
                response.put("message", "ID sản phẩm là bắt buộc");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> result = favoriteService.getFavoriteCount(idChiTietSanPham);

            if ("error".equals(result.get("status"))) {
                response.put("error", true);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }

            response.put("error", false);
            response.put("message", "Lấy số lượng yêu thích thành công");
            response.put("totalFavorites", result.get("totalFavorites"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Lỗi khi lấy số lượng yêu thích: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/customer/{idKhachHang}")
    public ResponseEntity<Map<String, Object>> getCustomerFavorites(@PathVariable("idKhachHang") Integer idKhachHang) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate input
            if (idKhachHang == null) {
                response.put("error", true);
                response.put("message", "ID khách hàng là bắt buộc");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> result = favoriteService.getCustomerFavorites(idKhachHang);

            if ("error".equals(result.get("status"))) {
                response.put("error", true);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }

            response.put("error", false);
            response.put("message", "Lấy danh sách yêu thích thành công");
            response.put("favorites", result.get("data"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Lỗi khi lấy danh sách yêu thích: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/customer-count/{idKhachHang}")
    public ResponseEntity<Map<String, Object>> getCustomerFavoritesCount(
            @PathVariable("idKhachHang") Integer idKhachHang) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate input
            if (idKhachHang == null) {
                response.put("error", true);
                response.put("message", "ID khách hàng là bắt buộc");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> result = favoriteService.getCustomerFavoritesCount(idKhachHang);

            if ("error".equals(result.get("status"))) {
                response.put("error", true);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }

            response.put("error", false);
            response.put("message", "Lấy số lượng yêu thích của khách hàng thành công");
            response.put("favoritesCount", result.get("favoritesCount"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Lỗi khi lấy số lượng yêu thích của khách hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/product-ids/{idKhachHang}")
    public ResponseEntity<Map<String, Object>> getCustomerFavoriteProductIds(
            @PathVariable("idKhachHang") Integer idKhachHang) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate input
            if (idKhachHang == null) {
                response.put("error", true);
                response.put("message", "ID khách hàng là bắt buộc");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> result = favoriteService.getCustomerFavoriteProductIds(idKhachHang);

            if ("error".equals(result.get("status"))) {
                response.put("error", true);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }

            response.put("error", false);
            response.put("message", "Lấy danh sách ID sản phẩm yêu thích thành công");
            response.put("productIds", result.get("data"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Lỗi khi lấy danh sách ID sản phẩm yêu thích: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
