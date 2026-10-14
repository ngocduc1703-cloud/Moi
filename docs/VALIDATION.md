# Xác thực bản 1.0.0

## Đã thực hiện trong cloud Linux x64

* JDK Eclipse Temurin 21.0.12.1+1, Maven 3.9.9; Android Platform 35, Build Tools 35.0.0; NSIS 3.11, Launch4j 3.50.
* `mvn clean package -Pandroid,windows`: cả project gốc và ba module `core`, `desktop`, `android` build thành công.
* Giải nén ZIP source giao cho người dùng vào một thư mục mới có khoảng trắng, chạy lại `mvn -o clean package -Pandroid,windows` với SDK và Maven cache đã cài: build cả APK/EXE thành công, 9 bài kiểm thử đều đạt. ZIP source không chứa build cache, target hoặc khóa ký.
* JUnit: **9 bài kiểm thử, 0 lỗi, 0 thất bại, 0 bỏ qua**. Bài sinh màn giải hết 30 màn × 30 seed = 900 bàn; bài xáo trộn kiểm tra thêm 50 ván có khay đã chứa ô, giữ số lượng từng loại và giải hết bàn sau xáo trộn.
* Kiểm tra mở khóa màn bằng thao tác click trên Scene, tự lưu và đọc lại tiến độ, xử lý dữ liệu lưu hỏng; kiểm tra thất bại tại 7 ô và hoàn tác để tiếp tục.
* Desktop dưới Xvfb: mở cửa sổ Swing thật, render, gửi MouseEvent tới tọa độ một ô và xác nhận số lượt tăng. Kết quả **PASS**.
* Đã xuất và kiểm tra hình màn hình chính, bàn chơi ở độ phân giải 960 × 1640. Giao diện scale đồng nhất theo kích thước cửa sổ và nhận chuột/cảm ứng theo cùng hệ tọa độ.
* APK đã được biên dịch thành DEX, căn chỉnh và ký; `apksigner verify --verbose` xác nhận chữ ký v1/v2/v3. Package `vn.garden.tiles`, minSdk 23 (Android 6), targetSdk 35, activity khởi chạy `vn.garden.android.MainActivity`.
* EXE launcher và installer có header Windows PE hợp lệ. Installer NSIS đóng gói launcher, runtime Windows x64, giấy phép, shortcut và trình gỡ cài đặt. Portable ZIP có đầy đủ Java runtime.
* Các bản tải về JDK/Maven/JRE/SDK được kiểm tra checksum gốc; gói NSIS/Xvfb qua chỉ mục Debian có chữ ký. Không tắt kiểm tra TLS/chữ ký để dựng bản này.

## Cần kiểm tra trên thiết bị

Chưa chạy APK trên điện thoại/Android emulator hoặc chạy EXE/installer trên Windows trong môi trường Linux này. Kiểm thử Java dùng chung và kiểm tra định dạng/chữ ký không thay thế việc kiểm thử cài đặt trên các thiết bị đó.

Checklist khi có thiết bị: cài game, chơi màn 1, thử âm thanh và tạm dừng, hoàn thành màn để mở khóa, đóng/mở để kiểm tra lưu tiến độ; trên Windows thử shortcut và gỡ cài đặt, trên Android thử nút Back, vùng an toàn quanh camera/status bar và cảm ứng ở các tỷ lệ màn hình.

APK dùng khóa phát triển; EXE chưa có chữ ký Authenticode. Chưa đưa game lên Google Play hoặc công bố release GitHub.
