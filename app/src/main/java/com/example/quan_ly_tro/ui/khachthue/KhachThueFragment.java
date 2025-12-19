package com.example.quan_ly_tro.ui.khachthue;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.adapter.KhachThueAdapter;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment hiển thị danh sách khách thuê với tìm kiếm
 */
public class KhachThueFragment extends Fragment implements KhachThueAdapter.OnKhachThueClickListener {
    
    private KhachThueViewModel viewModel;
    private KhachThueAdapter adapter;
    
    private RecyclerView rvKhachThue;
    private LinearLayout layoutEmpty;
    private FloatingActionButton fabAdd;
    
    // Search views
    private ImageButton btnSearch;
    private TextInputLayout searchLayout;
    private TextInputEditText edtSearch;
    private boolean isSearchVisible = false;
    
    private Map<Integer, String> phongMap = new HashMap<>();
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_khach_thue, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initRecyclerView();
        initViewModel();
        setupClickListeners();
        setupSearch();
        observeData();
    }
    
    private void initViews(View view) {
        rvKhachThue = view.findViewById(R.id.rv_khach_thue);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        fabAdd = view.findViewById(R.id.fab_add);
        
        // Search views
        btnSearch = view.findViewById(R.id.btn_search);
        searchLayout = view.findViewById(R.id.search_layout);
        edtSearch = view.findViewById(R.id.edt_search);
    }
    
    private void initRecyclerView() {
        adapter = new KhachThueAdapter(this);
        rvKhachThue.setLayoutManager(new LinearLayoutManager(getContext()));
        rvKhachThue.setAdapter(adapter);
        
        // Apply layout animation for smooth item appearance
        rvKhachThue.setLayoutAnimation(android.view.animation.AnimationUtils.loadLayoutAnimation(
                requireContext(), R.anim.layout_animation_fall_down));
        
        // Animate FAB
        fabAdd.startAnimation(android.view.animation.AnimationUtils.loadAnimation(
                requireContext(), R.anim.fab_scale_up));
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(KhachThueViewModel.class);
    }
    
    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> openThemKhachThue());
        
        View btnAddEmpty = getView().findViewById(R.id.btn_add_empty);
        if (btnAddEmpty != null) {
            btnAddEmpty.setOnClickListener(v -> openThemKhachThue());
        }
    }
    
    private void setupSearch() {
        btnSearch.setOnClickListener(v -> toggleSearch());
        
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setSearchQuery(s.toString());
            }
        });
    }
    
    private void toggleSearch() {
        isSearchVisible = !isSearchVisible;
        if (isSearchVisible) {
            searchLayout.setVisibility(View.VISIBLE);
            edtSearch.requestFocus();
        } else {
            searchLayout.setVisibility(View.GONE);
            edtSearch.setText("");
            viewModel.setSearchQuery("");
        }
    }
    
    private void openThemKhachThue() {
        Intent intent = new Intent(getActivity(), ThemKhachThueActivity.class);
        startActivity(intent);
    }
    
    private void observeData() {
        // Load danh sách phòng để map
        viewModel.getAllPhong().observe(getViewLifecycleOwner(), phongList -> {
            if (phongList != null) {
                phongMap.clear();
                for (Phong phong : phongList) {
                    phongMap.put(phong.getId(), phong.getSoPhong());
                }
                adapter.setPhongMap(phongMap);
            }
        });
        
        // Load danh sách khách thuê với search filter
        viewModel.getFilteredKhachThue().observe(getViewLifecycleOwner(), khachThueList -> {
            if (khachThueList != null && !khachThueList.isEmpty()) {
                adapter.submitList(khachThueList);
                rvKhachThue.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
            } else {
                rvKhachThue.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
    
    @Override
    public void onKhachThueClick(KhachThue khachThue) {
        Intent intent = new Intent(getActivity(), ThemKhachThueActivity.class);
        intent.putExtra("khach_thue_id", khachThue.getId());
        startActivity(intent);
    }
    
    @Override
    public void onKhachThueLongClick(KhachThue khachThue) {
        String[] options = khachThue.isDangThue() ? 
                new String[]{"Sửa thông tin", "Trả phòng", "Xóa"} :
                new String[]{"Sửa thông tin", "Xóa"};
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(khachThue.getHoTen())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Sửa
                        onKhachThueClick(khachThue);
                    } else if (khachThue.isDangThue() && which == 1) {
                        // Trả phòng
                        showTraPhongDialog(khachThue);
                    } else {
                        // Xóa
                        showDeleteDialog(khachThue);
                    }
                })
                .show();
    }
    
    private void showTraPhongDialog(KhachThue khachThue) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xác nhận trả phòng")
                .setMessage("Khách " + khachThue.getHoTen() + " sẽ trả phòng?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    viewModel.traPhong(khachThue);
                })
                .show();
    }
    
    private void showDeleteDialog(KhachThue khachThue) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa khách thuê")
                .setMessage("Bạn có chắc muốn xóa " + khachThue.getHoTen() + "?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.delete(khachThue);
                })
                .show();
    }
}
