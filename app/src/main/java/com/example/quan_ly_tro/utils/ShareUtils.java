package com.example.quan_ly_tro.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;

/**
 * Utility class để chia sẻ thông tin qua SMS, Zalo, và các ứng dụng khác
 */
public class ShareUtils {
    
    /**
     * Gửi hóa đơn qua SMS
     */
    public static void sendInvoiceSms(Context context, HoaDon hoaDon, 
                                       Phong phong, KhachThue khachThue) {
        if (khachThue == null || khachThue.getSoDienThoai() == null) {
            Toast.makeText(context, "Không có số điện thoại khách thuê", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String message = createInvoiceMessage(hoaDon, phong);
        
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + khachThue.getSoDienThoai()));
        smsIntent.putExtra("sms_body", message);
        
        try {
            context.startActivity(smsIntent);
        } catch (Exception e) {
            Toast.makeText(context, "Không thể mở ứng dụng SMS", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Gửi hóa đơn qua Zalo (nếu đã cài đặt)
     */
    public static void sendInvoiceZalo(Context context, HoaDon hoaDon, 
                                        Phong phong, KhachThue khachThue) {
        String message = createInvoiceMessage(hoaDon, phong);
        
        // Check if Zalo is installed
        if (isAppInstalled(context, "com.zing.zalo")) {
            Intent zaloIntent = new Intent(Intent.ACTION_SEND);
            zaloIntent.setType("text/plain");
            zaloIntent.setPackage("com.zing.zalo");
            zaloIntent.putExtra(Intent.EXTRA_TEXT, message);
            
            try {
                context.startActivity(zaloIntent);
            } catch (Exception e) {
                // Fallback to general share
                shareGeneral(context, message);
            }
        } else {
            Toast.makeText(context, "Zalo chưa được cài đặt", Toast.LENGTH_SHORT).show();
            shareGeneral(context, message);
        }
    }
    
    /**
     * Chia sẻ qua các ứng dụng khác (general share)
     */
    public static void shareGeneral(Context context, String message) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
    }
    
    /**
     * Chia sẻ hóa đơn qua bất kỳ ứng dụng nào
     */
    public static void shareInvoice(Context context, HoaDon hoaDon, Phong phong) {
        String message = createInvoiceMessage(hoaDon, phong);
        shareGeneral(context, message);
    }
    
    /**
     * Tạo nội dung tin nhắn hóa đơn
     */
    private static String createInvoiceMessage(HoaDon hoaDon, Phong phong) {
        StringBuilder sb = new StringBuilder();
        sb.append("📝 THÔNG BÁO HÓA ĐƠN\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n");
        sb.append("Phòng: ").append(phong != null ? phong.getSoPhong() : "N/A").append("\n");
        sb.append("Kỳ thanh toán: ").append(hoaDon.getThangNam()).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n");
        sb.append("💰 TỔNG TIỀN: ").append(FormatUtils.formatCurrency(hoaDon.getTongTien())).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n");
        
        if (HoaDon.TRANG_THAI_CHUA_THANH_TOAN.equals(hoaDon.getTrangThai())) {
            sb.append("⚠️ Trạng thái: CHƯA THANH TOÁN\n");
            sb.append("Vui lòng thanh toán sớm. Xin cảm ơn!");
        } else {
            sb.append("✅ Trạng thái: ĐÃ THANH TOÁN\n");
            sb.append("Cảm ơn quý khách!");
        }
        
        return sb.toString();
    }
    
    /**
     * Kiểm tra app đã được cài đặt hay chưa
     */
    private static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Chia sẻ thông tin phòng
     */
    public static void shareRoomInfo(Context context, Phong phong) {
        StringBuilder sb = new StringBuilder();
        sb.append("🏠 THÔNG TIN PHÒNG TRỌ\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n");
        sb.append("Phòng: ").append(phong.getSoPhong()).append("\n");
        sb.append("Loại: ").append(phong.getLoaiPhong()).append("\n");
        sb.append("Diện tích: ").append(phong.getDienTich()).append(" m²\n");
        sb.append("Giá thuê: ").append(FormatUtils.formatCurrency(phong.getGiaThue())).append("/tháng\n");
        sb.append("Trạng thái: ").append(phong.getTrangThai()).append("\n");
        
        if (phong.getMoTa() != null && !phong.getMoTa().isEmpty()) {
            sb.append("Mô tả: ").append(phong.getMoTa()).append("\n");
        }
        
        shareGeneral(context, sb.toString());
    }
}
