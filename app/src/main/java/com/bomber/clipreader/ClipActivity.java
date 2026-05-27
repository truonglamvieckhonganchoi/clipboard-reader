package com.bomber.clipreader;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;

import java.io.File;
import java.io.FileWriter;

/**
 * Activity trong suốt (transparent) - hỗ trợ 2 chế độ:
 *
 * 1. GET MODE (mặc định): Đọc clipboard và ghi ra file
 *    Lệnh: adb shell am start -n com.bomber.clipreader/.ClipActivity
 *    Output: /storage/emulated/0/Android/data/com.bomber.clipreader/files/clipboard.txt
 *
 * 2. SET MODE: Ghi text vào clipboard của giả lập
 *    Lệnh: adb shell am start -n com.bomber.clipreader/.ClipActivity --es text "noi_dung"
 *    Sau lệnh này, clipboard giả lập sẽ chứa "noi_dung"
 *
 * Cả 2 mode đều mở Activity foreground (Android 12 yêu cầu) rồi tự đóng ngay.
 */
public class ClipActivity extends Activity {

    // Tên file output cho GET mode
    private static final String OUTPUT_FILE = "clipboard.txt";

    // Key của Intent extra cho SET mode
    private static final String EXTRA_TEXT = "text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra Intent có chứa text để SET không
        Intent intent = getIntent();
        String textToSet = null;
        if (intent != null) {
            textToSet = intent.getStringExtra(EXTRA_TEXT);
        }

        if (textToSet != null) {
            // SET MODE: Ghi text vào clipboard giả lập
            setClipboard(textToSet);
        } else {
            // GET MODE: Đọc clipboard giả lập rồi ghi ra file
            getClipboard();
        }

        // Tự đóng Activity ngay lập tức
        finish();
    }

    /**
     * GET MODE: Đọc clipboard và ghi nội dung ra file.
     * File output: /storage/emulated/0/Android/data/com.bomber.clipreader/files/clipboard.txt
     */
    private void getClipboard() {
        String clipText = "";

        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (cm != null && cm.hasPrimaryClip()) {
                ClipData clip = cm.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    CharSequence text = clip.getItemAt(0).getText();
                    if (text != null) {
                        clipText = text.toString();
                    }
                }
            }
        } catch (Exception e) {
            clipText = "ERROR:" + e.getMessage();
        }

        writeToFile(clipText);
    }

    /**
     * SET MODE: Ghi text từ Intent extra vào clipboard giả lập.
     *
     * @param text Nội dung cần ghi vào clipboard
     */
    private void setClipboard(String text) {
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (cm != null) {
                // Tạo ClipData kiểu text thuần và set vào clipboard
                ClipData clip = ClipData.newPlainText("ClipReader", text);
                cm.setPrimaryClip(clip);

                // Ghi xác nhận thành công ra file để PC verify được
                writeToFile("SET_OK:" + text);
            } else {
                writeToFile("ERROR:ClipboardManager_null");
            }
        } catch (Exception e) {
            writeToFile("ERROR:" + e.getMessage());
        }
    }

    /**
     * Ghi nội dung ra file trong app-specific external storage.
     * Path: /sdcard/Android/data/com.bomber.clipreader/files/clipboard.txt
     * Không cần permission đặc biệt, ADB đọc trực tiếp được.
     *
     * @param content Nội dung cần ghi
     */
    private void writeToFile(String content) {
        // Cách 1: App-specific external storage (ADB đọc được, không cần permission)
        try {
            File dir = getExternalFilesDir(null);
            if (dir != null) {
                File file = new File(dir, OUTPUT_FILE);
                FileWriter writer = new FileWriter(file, false);
                writer.write(content);
                writer.flush();
                writer.close();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cách 2: Internal files dir (fallback nếu external không khả dụng)
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

