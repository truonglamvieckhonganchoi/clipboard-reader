package com.bomber.clipreader;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

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
 *
 * QUAN TRỌNG: Android 12 chỉ cho đọc clipboard khi Activity có window focus.
 * Nên GET mode phải chờ onWindowFocusChanged(true) trước khi đọc.
 */
public class ClipActivity extends Activity {

    private static final String OUTPUT_FILE = "clipboard.txt";
    private static final String EXTRA_TEXT = "text";

    // Lưu mode để xử lý trong onWindowFocusChanged
    private String textToSet = null;
    private boolean isSetMode = false;
    private boolean hasProcessed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra Intent có chứa text để SET không
        Intent intent = getIntent();
        if (intent != null) {
            textToSet = intent.getStringExtra(EXTRA_TEXT);
        }
        isSetMode = (textToSet != null);

        // SET mode có thể chạy ngay trong onCreate (không bị restrict)
        if (isSetMode) {
            setClipboard(textToSet);
            hasProcessed = true;
            finish();
        }
        // GET mode: PHẢI chờ window focus mới đọc được clipboard trên Android 12
    }

    /**
     * Android 12 chỉ cho phép đọc clipboard khi Activity có window focus.
     * Đây là callback đảm bảo Activity đã thực sự foreground + focused.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Chỉ xử lý GET mode, và chỉ 1 lần khi có focus
        if (hasFocus && !isSetMode && !hasProcessed) {
            hasProcessed = true;
            // Delay nhỏ 100ms để đảm bảo system đã cập nhật clipboard access
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getClipboard();
                    finish();
                }
            }, 100);
        }
    }

    /**
     * GET MODE: Đọc clipboard và ghi nội dung ra file.
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
     * SET MODE: Ghi text vào clipboard giả lập.
     */
    private void setClipboard(String text) {
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (cm != null) {
                ClipData clip = ClipData.newPlainText("ClipReader", text);
                cm.setPrimaryClip(clip);
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
     */
    private void writeToFile(String content) {
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

