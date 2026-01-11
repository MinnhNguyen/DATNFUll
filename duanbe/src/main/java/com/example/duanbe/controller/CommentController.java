package com.example.duanbe.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.duanbe.entity.BinhLuan;
import com.example.duanbe.response.BinhLuanRepository;
import com.example.duanbe.response.ReviewResponse;
import com.example.duanbe.service.ReviewService;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE })
@RequestMapping("/api/reviews")
public class CommentController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BinhLuanRepository binhLuanRepository;

    @GetMapping("/product/{idChiTietSanPham}")
    public ResponseEntity<ReviewResponse> getProductReviews(
            @PathVariable("idChiTietSanPham") Integer idChiTietSanPham) {
        try {
            ReviewResponse response = reviewService.getProductReviews(idChiTietSanPham);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ReviewResponse errorResponse = new ReviewResponse();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addComment(@RequestBody Map<String, Object> commentRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate required fields
            if (!commentRequest.containsKey("id_khach_hang") ||
                    !commentRequest.containsKey("id_chi_tiet_san_pham") ||
                    !commentRequest.containsKey("danh_gia") ||
                    !commentRequest.containsKey("binh_luan")) {

                response.put("error", true);
                response.put("message",
                        "Thiếu thông tin bắt buộc: id_khach_hang, id_chi_tiet_san_pham, danh_gia, binh_luan");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate rating
            try {
                Integer rating = Integer.parseInt(commentRequest.get("danh_gia").toString());
                if (rating < 1 || rating > 5) {
                    response.put("error", true);
                    response.put("message", "Đánh giá phải từ 1 đến 5 sao");
                    return ResponseEntity.badRequest().body(response);
                }
            } catch (NumberFormatException e) {
                response.put("error", true);
                response.put("message", "Đánh giá không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> result = reviewService.addReview(commentRequest);

            if ((Boolean) result.get("error")) {
                response.put("error", true);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }

            response.put("error", false);
            response.put("message", result.get("message"));
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Lỗi khi thêm bình luận: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/update/{reviewId}")
    public ResponseEntity<Map<String, Object>> updateComment(
            @PathVariable("reviewId") String reviewId,
            @RequestBody Map<String, Object> commentRequest) {

        Map<String, Object> response = new HashMap<>();
        try {
            // Check if comment exists and belongs to user (basic check)
            Integer idBinhLuan;
            try {
                idBinhLuan = Integer.parseInt(reviewId);
            } catch (NumberFormatException e) {
                response.put("error", true);
                response.put("message", "ID bình luận không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<BinhLuan> existingComment = binhLuanRepository.findById(idBinhLuan);
            if (!existingComment.isPresent()) {
                response.put("error", true);
                response.put("message", "Không tìm thấy bình luận");
                return ResponseEntity.notFound().build();
            }

            // Validate rating if provided
            if (commentRequest.containsKey("danh_gia")) {
                try {
                    Integer rating = Integer.parseInt(commentRequest.get("danh_gia").toString());
                    if (rating < 1 || rating > 5) {
                        response.put("error", true);
                        response.put("message", "Đánh giá phải từ 1 đến 5 sao");
                        return ResponseEntity.badRequest().body(response);
                    }
                } catch (NumberFormatException e) {
                    response.put("error", true);
                    response.put("message", "Đánh giá không hợp lệ");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            Map<String, Object> result = reviewService.updateReview(reviewId, commentRequest);

            if ((Boolean) result.get("error")) {
                response.put("error", true);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }

            response.put("error", false);
            response.put("message", result.get("message"));
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Lỗi khi cập nhật bình luận: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable("reviewId") String reviewId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate review ID
            Integer idBinhLuan;
            try {
                idBinhLuan = Integer.parseInt(reviewId);
            } catch (NumberFormatException e) {
                response.put("error", true);
                response.put("message", "ID bình luận không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if comment exists
            Optional<BinhLuan> existingComment = binhLuanRepository.findById(idBinhLuan);
            if (!existingComment.isPresent()) {
                response.put("error", true);
                response.put("message", "Không tìm thấy bình luận");
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> result = reviewService.deleteReview(reviewId);

            if ((Boolean) result.get("error")) {
                response.put("error", true);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }

            response.put("error", false);
            response.put("message", result.get("message"));
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Lỗi khi xóa bình luận: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/can-review")
    public ResponseEntity<Map<String, Object>> canUserReview(
            @RequestParam("idKhachHang") Integer idKhachHang,
            @RequestParam("idChiTietSanPham") Integer idChiTietSanPham) {

        Map<String, Object> response = new HashMap<>();
        try {
            boolean canReview = reviewService.canCustomerReviewProduct(idKhachHang, idChiTietSanPham);

            response.put("error", false);
            response.put("message", "Kiểm tra quyền đánh giá thành công");
            response.put("canReview", canReview);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Lỗi khi kiểm tra quyền đánh giá: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/customer/{idKhachHang}")
    public ResponseEntity<Map<String, Object>> getCustomerComments(@PathVariable("idKhachHang") Integer idKhachHang) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<BinhLuan> comments = binhLuanRepository.findAllByIdKhachHang(idKhachHang);

            response.put("error", false);
            response.put("message", "Lấy danh sách bình luận thành công");
            response.put("data", comments);
            response.put("totalComments", comments.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Lỗi khi lấy danh sách bình luận: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
