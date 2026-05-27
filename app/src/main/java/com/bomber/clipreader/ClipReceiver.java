package com.bomber.clipreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver nhận lệnh từ ADB để khởi động ClipActivity.
 *
 * Cách gọi từ ADB:
 *     adb shell am broadcast -a com.bomber.clipreader.GET
 *
 * Lý do cần Receiver + Activity (không đọc clipboard trực tiếp trong Receiver):
 * - Trên Android 12+, Receiver là background context -> không được đọc clipboard
 * - Phải start một Activity (foreground) để có quyền đọc
 * - ClipActivity là Activity trong suốt, mở-đọc-ghi-đóng trong vài ms
 */
public class ClipReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Tạo Intent mở ClipActivity với flag NEW_TASK (bắt buộc khi start từ Receiver)
        Intent actIntent = new Intent(context, ClipActivity.class);
        actIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        actIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); // Không hiệu ứng chuyển cảnh
        actIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); // Không hiển thị trong recent

        try {
            context.startActivity(actIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
