# StudentDispatch Desktop (Windows EXE/MSI)

## ساخت خروجی روی ویندوز (لوکال)
1) نصب JDK 17 (یا 21)
2) نصب Gradle (اگر نداری): با Chocolatey
   - PowerShell (Admin): `choco install gradle -y`
3) داخل پوشه پروژه:
   - `gradle --version`
   - `gradle packageReleaseExe`
   - یا `gradle packageReleaseMsi`

خروجی‌ها:
- `build/compose/binaries/main-release/exe/`
- `build/compose/binaries/main-release/msi/`

## Build در GitHub Actions
فایل workflow آماده است: `.github/workflows/build-desktop.yml`
بعد از Push، از تب Actions، artifact را دانلود کن.
