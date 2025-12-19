package com.example.quan_ly_tro.ui.khachthue;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.utils.FormatUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity thêm/sửa khách thuê
 */
public class ThemKhachThueActivity extends AppCompatActivity {
    
    private KhachThueViewModel viewModel;
    
    private TextView tvTitle;
    private ImageButton btnBack;
    private TextInputLayout tilPhong, tilHoTen, tilCccd, tilSdt, tilEmail;
    private TextInputLayout tilQueQuan, tilNgheNghiep, tilNgayVao, tilGhiChu;
    private AutoCompleteTextView dropdownPhong;
    private TextInputEditText edtHoTen, edtCccd, edtSdt, edtEmail;
    private TextInputEditText edtQueQuan, edtNgheNghiep, edtNgayVao, edtGhiChu;
    private MaterialButton btnHuy, btnLuu;
    
    private int khachThueId = -1;
    private KhachThue currentKhachThue;
    
    private List<Phong> phongList = new ArrayList<>();
    private Map<String, Integer> phongNameToIdMap = new HashMap<>();
    private long selectedNgayVao = System.currentTimeMillis();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_khach_thue);
        
        initViews();
        initViewModel();
        setupClickListeners();
        loadPhongDropdown();
        
        // Kiểm tra có phải sửa không
        khachThueId = getIntent().getIntExtra("khach_thue_id", -1);
        if (khachThueId != -1) {
            tvTitle.setText(R.string.khach_sua);
            loadKhachThueData();
        } else {
            // Set ngày vào mặc định là hôm nay
            edtNgayVao.setText(FormatUtils.formatDate(System.currentTimeMillis()));
        }
    }
    
    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        btnBack = findViewById(R.id.btn_back);
        
        tilPhong = findViewById(R.id.til_phong);
        tilHoTen = findViewById(R.id.til_ho_ten);
        tilCccd = findViewById(R.id.til_cccd);
        tilSdt = findViewById(R.id.til_sdt);
        tilEmail = findViewById(R.id.til_email);
        tilQueQuan = findViewById(R.id.til_que_quan);
        tilNgheNghiep = findViewById(R.id.til_nghe_nghiep);
        tilNgayVao = findViewById(R.id.til_ngay_vao);
        tilGhiChu = findViewById(R.id.til_ghi_chu);
        
        dropdownPhong = findViewById(R.id.dropdown_phong);
        edtHoTen = findViewById(R.id.edt_ho_ten);
        edtCccd = findViewById(R.id.edt_cccd);
        edtSdt = findViewById(R.id.edt_sdt);
        edtEmail = findViewById(R.id.edt_email);
        edtQueQuan = findViewById(R.id.edt_que_quan);
        edtNgheNghiep = findViewById(R.id.edt_nghe_nghiep);
        edtNgayVao = findViewById(R.id.edt_ngay_vao);
        edtGhiChu = findViewById(R.id.edt_ghi_chu);
        
        btnHuy = findViewById(R.id.btn_huy);
        btnLuu = findViewById(R.id.btn_luu);
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(KhachThueViewModel.class);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnHuy.setOnClickListener(v -> finish());
        btnLuu.setOnClickListener(v -> saveKhachThue());
        
        // Date picker cho ngày vào
        edtNgayVao.setOnClickListener(v -> showDatePicker());
        tilNgayVao.setEndIconOnClickListener(v -> showDatePicker());
    }
    
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedNgayVao);
        
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    selectedNgayVao = selected.getTimeInMillis();
                    edtNgayVao.setText(FormatUtils.formatDate(selectedNgayVao));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }
    
    private void loadPhongDropdown() {
        viewModel.getAllPhong().observe(this, phongs -> {
            if (phongs != null) {
                phongList.clear();
                phongNameToIdMap.clear();
                
                List<String> phongNames = new ArrayList<>();
                phongNames.add("-- Chọn phòng --");
                
                for (Phong phong : phongs) {
                    // Chỉ hiển thị phòng trống hoặc phòng hiện tại của khách
                    if (Phong.TRANG_THAI_TRONG.equals(phong.getTrangThai()) || 
                        (currentKhachThue != null && phong.getId() == currentKhachThue.getPhongId())) {
                        phongList.add(phong);
                        String displayName = "Phòng " + phong.getSoPhong();
                        phongNames.add(displayName);
                        phongNameToIdMap.put(displayName, phong.getId());
                    }
                }
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        phongNames
                );
                dropdownPhong.setAdapter(adapter);
                
                if (phongNames.size() > 0) {
                    dropdownPhong.setText(phongNames.get(0), false);
                }
            }
        });
    }
    
    private void loadKhachThueData() {
        viewModel.getKhachThueById(khachThueId).observe(this, khachThue -> {
            if (khachThue != null) {
                currentKhachThue = khachThue;
                
                edtHoTen.setText(khachThue.getHoTen());
                edtCccd.setText(khachThue.getCccd());
                edtSdt.setText(khachThue.getSoDienThoai());
                edtEmail.setText(khachThue.getEmail());
                edtQueQuan.setText(khachThue.getQueQuan());
                edtNgheNghiep.setText(khachThue.getNgheNghiep());
                edtGhiChu.setText(khachThue.getGhiChu());
                
                selectedNgayVao = khachThue.getNgayVao();
                edtNgayVao.setText(FormatUtils.formatDate(selectedNgayVao));
                
                // Load lại dropdown để hiển thị phòng hiện tại
                loadPhongDropdown();
                
                // Set phòng đã chọn
                if (khachThue.getPhongId() != null) {
                    for (Map.Entry<String, Integer> entry : phongNameToIdMap.entrySet()) {
                        if (entry.getValue().equals(khachThue.getPhongId())) {
                            dropdownPhong.setText(entry.getKey(), false);
                            break;
                        }
                    }
                }
            }
        });
    }
    
    private void saveKhachThue() {
        // Validate
        String hoTen = edtHoTen.getText().toString().trim();
        String cccd = edtCccd.getText().toString().trim();
        String sdt = edtSdt.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String queQuan = edtQueQuan.getText().toString().trim();
        String ngheNghiep = edtNgheNghiep.getText().toString().trim();
        String ghiChu = edtGhiChu.getText().toString().trim();
        String selectedPhongName = dropdownPhong.getText().toString();
        
        // Validate họ tên
        if (hoTen.isEmpty()) {
            tilHoTen.setError(getString(R.string.error_required));
            return;
        }
        tilHoTen.setError(null);
        
        // Validate SĐT
        if (sdt.isEmpty()) {
            tilSdt.setError(getString(R.string.error_required));
            return;
        }
        tilSdt.setError(null);
        
        // Lấy phòng ID
        Integer phongId = phongNameToIdMap.get(selectedPhongName);
        
        // Tạo hoặc cập nhật khách thuê
        KhachThue khachThue;
        if (khachThueId != -1 && currentKhachThue != null) {
            khachThue = currentKhachThue;
        } else {
            khachThue = new KhachThue();
        }
        
        khachThue.setHoTen(hoTen);
        khachThue.setCccd(cccd);
        khachThue.setSoDienThoai(sdt);
        khachThue.setEmail(email);
        khachThue.setQueQuan(queQuan);
        khachThue.setNgheNghiep(ngheNghiep);
        khachThue.setGhiChu(ghiChu);
        khachThue.setNgayVao(selectedNgayVao);
        khachThue.setPhongId(phongId);
        
        if (khachThueId != -1) {
            viewModel.update(khachThue);
        } else {
            viewModel.insert(khachThue);
        }
        
        Toast.makeText(this, R.string.msg_luu_thanh_cong, Toast.LENGTH_SHORT).show();
        finish();
    }
}
