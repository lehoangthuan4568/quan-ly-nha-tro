package com.example.quan_ly_tro.ui.hoadon;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.quan_ly_tro.data.database.entity.DichVu;
import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.data.database.entity.ThuChi;
import com.example.quan_ly_tro.data.repository.DichVuRepository;
import com.example.quan_ly_tro.data.repository.HoaDonRepository;
import com.example.quan_ly_tro.data.repository.PhongRepository;
import com.example.quan_ly_tro.data.repository.ThuChiRepository;

import java.util.List;

/**
 * ViewModel cho quản lý Hóa đơn
 */
public class HoaDonViewModel extends AndroidViewModel {
    
    private final HoaDonRepository hoaDonRepository;
    private final PhongRepository phongRepository;
    private final DichVuRepository dichVuRepository;
    private final ThuChiRepository thuChiRepository;
    
    private final LiveData<List<HoaDon>> allHoaDon;
    private final MutableLiveData<String> filterTrangThai = new MutableLiveData<>();
    private final LiveData<List<HoaDon>> filteredHoaDon;
    
    public HoaDonViewModel(@NonNull Application application) {
        super(application);
        hoaDonRepository = new HoaDonRepository(application);
        phongRepository = new PhongRepository(application);
        dichVuRepository = new DichVuRepository(application);
        thuChiRepository = new ThuChiRepository(application);
        
        allHoaDon = hoaDonRepository.getAllHoaDon();
        
        // Filter theo trạng thái thanh toán
        filteredHoaDon = Transformations.switchMap(filterTrangThai, trangThai -> {
            if (trangThai == null || trangThai.isEmpty()) {
                return allHoaDon;
            } else if (trangThai.equals(HoaDon.TRANG_THAI_CHUA_THANH_TOAN)) {
                return hoaDonRepository.getHoaDonChuaThanhToan();
            } else {
                return hoaDonRepository.getHoaDonDaThanhToan();
            }
        });
        
        filterTrangThai.setValue("");
    }
    
    public LiveData<List<HoaDon>> getAllHoaDon() {
        return allHoaDon;
    }
    
    public LiveData<List<HoaDon>> getFilteredHoaDon() {
        return filteredHoaDon;
    }
    
    public void setFilter(String trangThai) {
        filterTrangThai.setValue(trangThai != null ? trangThai : "");
    }
    
    public LiveData<HoaDon> getHoaDonById(int id) {
        return hoaDonRepository.getHoaDonById(id);
    }
    
    public LiveData<List<HoaDon>> getHoaDonByPhong(int phongId) {
        return hoaDonRepository.getHoaDonByPhong(phongId);
    }
    
    public LiveData<List<Phong>> getAllPhong() {
        return phongRepository.getAllPhong();
    }
    
    public LiveData<List<Phong>> getPhongDangThue() {
        return phongRepository.getPhongByTrangThai(Phong.TRANG_THAI_DANG_THUE);
    }
    
    public LiveData<List<DichVu>> getAllDichVu() {
        return dichVuRepository.getAllDichVu();
    }
    
    public void insert(HoaDon hoaDon) {
        hoaDonRepository.insert(hoaDon);
    }
    
    public void update(HoaDon hoaDon) {
        hoaDonRepository.update(hoaDon);
    }
    
    public void delete(HoaDon hoaDon) {
        hoaDonRepository.delete(hoaDon);
    }
    
    public void thanhToan(HoaDon hoaDon) {
        // Cập nhật trạng thái hóa đơn
        hoaDon.setTrangThai(HoaDon.TRANG_THAI_DA_THANH_TOAN);
        hoaDon.setNgayThanhToan(System.currentTimeMillis());
        hoaDonRepository.update(hoaDon);
        
        // Tự động tạo khoản THU vào quản lý Thu/Chi
        ThuChi thuChi = new ThuChi();
        thuChi.setLoai(ThuChi.LOAI_THU);
        thuChi.setDanhMuc(ThuChi.DANH_MUC_TIEN_THUE);
        thuChi.setSoTien(hoaDon.getTongTien());
        thuChi.setMoTa("Thu tiền hóa đơn tháng " + hoaDon.getThangNam());
        thuChi.setPhongId(hoaDon.getPhongId());
        thuChi.setNgayGiaoDich(System.currentTimeMillis());
        thuChiRepository.insert(thuChi);
        
        // Trigger LiveData refresh by re-setting the filter
        String currentFilter = filterTrangThai.getValue();
        filterTrangThai.setValue(currentFilter != null ? currentFilter : "");
    }
}

