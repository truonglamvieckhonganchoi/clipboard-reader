# ClipReader - Android Clipboard Reader via ADB

App Android nho (20KB) doc clipboard tren Android 12+ qua ADB broadcast.

## Cach hoat dong

1. ADB gui broadcast `com.bomber.clipreader.GET`
2. App mo Activity trong suot (foreground) -> Android 12 cho phep doc clipboard
3. Doc clipboard -> ghi ra `/sdcard/clipboard.txt`
4. Tu dong dong Activity

## Cach su dung

### Cai APK vao gia lap:
```bash
adb -s emulator-5556 install clipreader-debug.apk
```

### Cap quyen storage (chay 1 lan):
```bash
adb -s emulator-5556 shell pm grant com.bomber.clipreader android.permission.WRITE_EXTERNAL_STORAGE
adb -s emulator-5556 shell pm grant com.bomber.clipreader android.permission.READ_EXTERNAL_STORAGE
```

### Lay clipboard:
```bash
adb -s emulator-5556 shell am broadcast -a com.bomber.clipreader.GET
sleep 1
adb -s emulator-5556 shell cat /sdcard/clipboard.txt
```

## Build APK

### Tu dong qua GitHub Actions:
1. Push repo len GitHub
2. Vao tab Actions -> workflow "Build APK" se tu chay
3. Download APK tu muc Artifacts

### Thu cong (can JDK 17):
```bash
gradle wrapper
./gradlew assembleDebug
# APK o: app/build/outputs/apk/debug/app-debug.apk
```

## Luu y

- App can quyen WRITE_EXTERNAL_STORAGE de ghi file
- Tren Android 12+, chi app foreground moi doc duoc clipboard
- Activity trong suot mo-doc-ghi-dong trong vai millisecond
- Neu /sdcard khong ghi duoc, fallback sang /data/local/tmp/clipboard.txt
