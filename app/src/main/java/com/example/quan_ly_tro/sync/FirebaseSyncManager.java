package com.example.quan_ly_tro.sync;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.data.database.entity.ThuChi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager class để đồng bộ dữ liệu giữa Room Database và Firebase Firestore
 */
public class FirebaseSyncManager {
    
    private static final String TAG = "FirebaseSyncManager";
    
    private static final String COLLECTION_PHONG = "phong";
    private static final String COLLECTION_KHACH_THUE = "khach_thue";
    private static final String COLLECTION_HOA_DON = "hoa_don";
    private static final String COLLECTION_THU_CHI = "thu_chi";
    
    private final Context context;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final AppDatabase database;
    
    private boolean isSyncing = false;
    
    public FirebaseSyncManager(Context context) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.database = AppDatabase.getDatabase(context);
    }
    
    /**
     * Kiểm tra xem user đã đăng nhập chưa
     */
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }
    
    /**
     * Lấy user ID hiện tại
     */
    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
    
    /**
     * Đăng nhập ẩn danh (cho demo)
     */
    public void signInAnonymously(OnSyncCallback callback) {
        auth.signInAnonymously()
                .addOnSuccessListener(result -> {
                    Log.d(TAG, "Đăng nhập ẩn danh thành công: " + result.getUser().getUid());
                    callback.onSuccess("Đăng nhập thành công");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Đăng nhập thất bại: " + e.getMessage());
                    callback.onError("Đăng nhập thất bại: " + e.getMessage());
                });
    }
    
    /**
     * Đăng xuất
     */
    public void signOut() {
        auth.signOut();
    }
    
    /**
     * Sync tất cả dữ liệu lên Firebase
     */
    public void syncToCloud(OnSyncCallback callback) {
        if (!isLoggedIn()) {
            callback.onError("Chưa đăng nhập");
            return;
        }
        
        if (isSyncing) {
            callback.onError("Đang đồng bộ...");
            return;
        }
        
        isSyncing = true;
        String userId = getCurrentUserId();
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Sync Phong
                List<Phong> phongList = database.phongDao().getAllPhongSync();
                for (Phong phong : phongList) {
                    syncPhongToCloud(userId, phong);
                }
                
                // Sync Khach Thue
                List<KhachThue> khachThueList = database.khachThueDao().getAllKhachThueSync();
                for (KhachThue khachThue : khachThueList) {
                    syncKhachThueToCloud(userId, khachThue);
                }
                
                // Sync Hoa Don
                List<HoaDon> hoaDonList = database.hoaDonDao().getAllHoaDonSync();
                for (HoaDon hoaDon : hoaDonList) {
                    syncHoaDonToCloud(userId, hoaDon);
                }
                
                // Sync Thu Chi
                List<ThuChi> thuChiList = database.thuChiDao().getAllThuChiSync();
                for (ThuChi thuChi : thuChiList) {
                    syncThuChiToCloud(userId, thuChi);
                }
                
                isSyncing = false;
                callback.onSuccess("Đồng bộ thành công!");
                
            } catch (Exception e) {
                isSyncing = false;
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }
    
    private void syncPhongToCloud(String userId, Phong phong) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", phong.getId());
        data.put("soPhong", phong.getSoPhong());
        data.put("loaiPhong", phong.getLoaiPhong());
        data.put("giaThue", phong.getGiaThue());
        data.put("dienTich", phong.getDienTich());
        data.put("trangThai", phong.getTrangThai());
        data.put("moTa", phong.getMoTa());
        data.put("lastUpdated", System.currentTimeMillis());
        
        firestore.collection("users").document(userId)
                .collection(COLLECTION_PHONG)
                .document(String.valueOf(phong.getId()))
                .set(data, SetOptions.merge())
                .addOnFailureListener(e -> Log.e(TAG, "Sync phong failed: " + e.getMessage()));
    }
    
    private void syncKhachThueToCloud(String userId, KhachThue khachThue) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", khachThue.getId());
        data.put("phongId", khachThue.getPhongId());
        data.put("hoTen", khachThue.getHoTen());
        data.put("soDienThoai", khachThue.getSoDienThoai());
        data.put("cccd", khachThue.getCccd());
        data.put("ngayVao", khachThue.getNgayVao());
        data.put("ngayVao", khachThue.getNgayVao());
        data.put("lastUpdated", System.currentTimeMillis());
        
        firestore.collection("users").document(userId)
                .collection(COLLECTION_KHACH_THUE)
                .document(String.valueOf(khachThue.getId()))
                .set(data, SetOptions.merge())
                .addOnFailureListener(e -> Log.e(TAG, "Sync khach thue failed: " + e.getMessage()));
    }
    
    private void syncHoaDonToCloud(String userId, HoaDon hoaDon) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", hoaDon.getId());
        data.put("phongId", hoaDon.getPhongId());
        data.put("khachThueId", hoaDon.getKhachThueId());
        data.put("thangNam", hoaDon.getThangNam());
        data.put("tongTien", hoaDon.getTongTien());
        data.put("trangThai", hoaDon.getTrangThai());
        data.put("ngayTao", hoaDon.getNgayTao());
        data.put("lastUpdated", System.currentTimeMillis());
        
        firestore.collection("users").document(userId)
                .collection(COLLECTION_HOA_DON)
                .document(String.valueOf(hoaDon.getId()))
                .set(data, SetOptions.merge())
                .addOnFailureListener(e -> Log.e(TAG, "Sync hoa don failed: " + e.getMessage()));
    }
    
    private void syncThuChiToCloud(String userId, ThuChi thuChi) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", thuChi.getId());
        data.put("loai", thuChi.getLoai());
        data.put("moTa", thuChi.getMoTa());
        data.put("soTien", thuChi.getSoTien());
        data.put("ngayGiaoDich", thuChi.getNgayGiaoDich());
        data.put("lastUpdated", System.currentTimeMillis());
        
        firestore.collection("users").document(userId)
                .collection(COLLECTION_THU_CHI)
                .document(String.valueOf(thuChi.getId()))
                .set(data, SetOptions.merge())
                .addOnFailureListener(e -> Log.e(TAG, "Sync thu chi failed: " + e.getMessage()));
    }
    
    /**
     * Callback interface for sync operations
     */
    public interface OnSyncCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}
