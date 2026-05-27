package com.bomber.clipreader;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;

import java.io.File;
import java.io.FileWriter;

/**
 * Activity trong suốt (transparent) - không hiển thị gì trên màn hình.
 *
 * Flow hoạt động:
 * 1. Được mở bởi ClipReceiver khi nhận broadcast từ ADB
 * 2. Vì là Activity foreground -> Android 12 cho phép đọc clipboard
 * 3. Đọc nội dung clipboard hiện tại
 * 4. Ghi ra file /sdcard/clipboard.txt (overwrite)
 * 5. Tự đóng ngay lập tức (finish())
 *
 * Người dùng gần như không thấy gì vì Activity hoàn toàn trong suốt
 * và tự đóng trong vài millisecond.
 */
public class ClipActivity extends Activity {

    // Tên file output cố định
    private static final String OUTPUT_FILE = "clipboard.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String clipText = "";

        try {
            // Lấy ClipboardManager từ system service
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

            if (cm != null && cm.hasPrimaryClip()) {
                ClipData clip = cm.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    // Lấy item đầu tiên trong clipboard (thường chỉ có 1)
                    CharSequence text = clip.getItemAt(0).getText();
                    if (text != null) {
                        clipText = text.toString();
                    }
                }
            }
        } catch (Exception e) {
            // Ghi lỗi vào file để debug từ ADB
            clipText = "ERROR:" + e.getMessage();
        }

        // Ghi nội dung clipboard ra /sdcard/clipboard.txt
        writeToFile(clipText);

        // Tự đóng Activity ngay lập tức
        finish();
    }

    /**
     * Ghi nội dung ra file.
     * Ưu tiên getExternalFilesDir (không cần permission, ADB đọc được).
     * Path: /sdcard/Android/data/com.bomber.clipreader/files/clipboard.txt
     *
     * @param content Nội dung cần ghi (clipboard text)
     */
    private void writeToFile(String content) {
        // Cách 1: App-specific external storage (không cần permission)
        // ADB đọc: adb shell cat /sdcard/Android/data/com.bomber.clipreader/files/clipboard.txt
        try {
            File dir = getExternalFilesDir(null);
            if (dir != null) {
                File file = new File(dir, OUTPUT_FILE);
                FileWriter writer = new FileWriter(file, false);
                writer.write(content);
                writer.flush();
                writer.close();
                return; // Thành công, không cần fallback
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cách 2: Internal files dir (luôn ghi được, nhưng cần run-as để đọc)
        try {
            File file = new File(getFilesDir(), OUTPUT_FILE);
            FileWriter writer = new FileWriter(file, false);
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
