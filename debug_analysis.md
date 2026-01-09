# Customer Reset Issue Analysis

## Problem Summary
When user:
1. Creates invoice 
2. Selects customer
3. Clicks "Lưu thông tin khách hàng" (Save customer info) 
4. Switches shipping method from "Giao hàng" to "Nhận tại cửa hàng"
5. The customer still gets reset to "Khách lẻ" on UI

## Code Flow Analysis

### 1. Frontend Flow (handlePhuongThucChange)
```javascript
// User switches shipping method radio button
handlePhuongThucChange() -> 
  store.setTrangThaiNhanHang(idHoaDon, phuongThuc, phiVanChuyen) ->
  updateInvoiceFromResponse(response)
```

### 2. Backend Flow (setTrangThaiNhanHang)
```java
// BanHangController.setTrangThaiNhanHang()
1. Find HoaDon by ID
2. Set phuong_thuc_nhan_hang
3. If "Nhận tại cửa hàng": clear dia_chi and phi_van_chuyen
4. Save to database
5. Call updateTongTienHoaDon() to recalculate totals
6. Return getFullInvoiceResponse(idHD)
```

### 3. SQL Query (findHoaDonById)
```sql
SELECT id_hoa_don,ma_hoa_don,kh.id_khach_hang,kh.ho_ten as ten_khach_hang,
       hd.ngay_tao,hd.ngay_sua,hd.trang_thai,
       vc.id_voucher,vc.ten_voucher,hd.sdt as sdt_nguoi_nhan,
       hd.sdt as so_dien_thoai,hd.dia_chi,hd.email,tong_tien_truoc_giam,
       phi_van_chuyen,hd.ho_ten,tong_tien_sau_giam,hinh_thuc_thanh_toan,
       phuong_thuc_nhan_hang
FROM hoa_don hd
LEFT JOIN khach_hang kh ON hd.id_khach_hang = kh.id_khach_hang
LEFT JOIN voucher vc ON hd.id_voucher = vc.id_voucher
WHERE hd.id_hoa_don = :idHd
```

## Key Findings

### 1. Backend SHOULD NOT clear customer data
- `setTrangThaiNhanHang()` only updates shipping method and related fields
- Does NOT touch `id_khach_hang`, `ho_ten`, or customer-related fields
- Customer data should remain intact

### 2. SQL Query Returns Customer Data from TWO Sources
- `kh.id_khach_hang` and `kh.ho_ten` from khach_hang table (NULL if no linked customer)
- `hd.ho_ten` from hoa_don table (always populated, defaults to 'Khách lẻ')

### 3. Previous Fix Applied
In `updateInvoiceFromResponse()`, there's logic to preserve customer data:
```javascript
id_khach_hang: response.id_khach_hang !== null 
    ? response.id_khach_hang 
    : targetTab.hd.id_khach_hang,  // Preserve existing
```

## Why Issue Persists

### Possibility 1: Backend Returns Incorrect Data
- Backend might return `id_khach_hang = null` (customer disassociated)
- Backend might return `ho_ten = 'Khách lẻ'` (overwritten)
- `updateTongTienHoaDon()` might have side effects

### Possibility 2: Race Condition
- Multiple API calls updating same invoice simultaneously
- localStorage interference
- Other functions calling `updateInvoiceFromResponse()` after

### Possibility 3: Database Level Issue
- Database triggers clearing customer data
- Concurrent transactions
- Entity lifecycle callbacks

## Recommended Debug Steps

### 1. Add Comprehensive Logging
In `handlePhuongThucChange()`:
```javascript
console.log('📋 BEFORE API call:', {
    id_khach_hang: activeTabData.value.hd.id_khach_hang,
    ho_ten: activeTabData.value.hd.ho_ten,
    ten_khach_hang: activeTabData.value.hd.ten_khach_hang
});

// After API response
console.log('📦 API Response:', {
    id_khach_hang: response.id_khach_hang,
    ho_ten: response.ho_ten,
    ten_khach_hang: response.ten_khach_hang
});

// After update
console.log('📋 AFTER update:', {
    id_khach_hang: activeTabData.value.hd.id_khach_hang,
    ho_ten: activeTabData.value.hd.ho_ten,
    ten_khach_hang: activeTabData.value.hd.ten_khach_hang
});
```

### 2. Backend Logging
In `BanHangController.setTrangThaiNhanHang()`:
```java
// Before update
System.out.println("BEFORE: id_khach_hang=" + hoaDon.getId_khach_hang() + 
                   ", ho_ten=" + hoaDon.getHo_ten());

// After save
System.out.println("AFTER: id_khach_hang=" + hoaDon.getId_khach_hang() + 
                   ", ho_ten=" + hoaDon.getHo_ten());

// In response
System.out.println("RESPONSE: id_khach_hang=" + response.getId_khach_hang() + 
                   ", ho_ten=" + response.getHo_ten());
```

### 3. Check for Other API Calls
- Monitor Network tab in browser
- Look for any other API calls happening around the same time
- Check if form component or other watchers are triggering updates

### 4. Database State Check
Verify what's actually in the database:
```sql
SELECT id_khach_hang, ho_ten FROM hoa_don WHERE id_hoa_don = [ID];
SELECT id_khach_hang, ho_ten FROM khach_hang WHERE id_khach_hang = [ID];
```

## Likely Root Cause

Based on code analysis, the most likely cause is:
1. Backend is incorrectly returning `id_khach_hang = null` or `ho_ten = 'Khách lẻ'`
2. OR there's a race condition where another process is clearing customer data
3. OR `updateTongTienHoaDon()` has an unexpected side effect

The fix in `updateInvoiceFromResponse()` only handles when API returns null, 
but if API returns wrong data (not null), it will still override existing data.
