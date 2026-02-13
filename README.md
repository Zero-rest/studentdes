# StudentDispatchDesktop

نسخه دسکتاپ (Windows) از اپلیکیشن StudentDispatch.

## خروجی گرفتن EXE/MSI روی Windows

### پیش‌نیاز
- نصب JDK 17
- نصب Gradle (یا استفاده از GitHub Actions)

### ساخت خروجی محلی
در پوشه پروژه:

```bat
gradle packageReleaseExe
gradle packageReleaseMsi
```

خروجی‌ها در مسیر زیر قرار می‌گیرند:
- `build\compose\binaries\main-release\exe\`
- `build\compose\binaries\main-release\msi\`

## Build در GitHub
فایل workflow آماده است: `.github/workflows/build-desktop.yml`
با هر push روی `main`، خروجی‌ها به صورت Artifact آپلود می‌شوند.
