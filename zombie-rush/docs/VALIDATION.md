# Xác thực Đại Lộ Zombie 1.0.0

Các kiểm tra trong cloud Linux x64:

* JDK Temurin 21.0.12.1+1, Maven 3.9.9, Android Platform 35/Build Tools 35.0.0, Launch4j 3.50 và NSIS 3.11.
* JUnit: **18 bài kiểm thử, 0 thất bại, 0 lỗi, 0 bỏ qua**. Kiểm tra bước thời gian cố định; chạm/giữ để nhảy; các thành viên nhảy nối tiếp; thu xu một lần; rủ thành viên và giới hạn đàn; va chạm/rơi; vượt rào/hố; lật xe; khiên/nam châm/khổng lồ; đường chạy dài và tái sử dụng đối tượng; tạm dừng và lưu xu không trùng; mua nâng cấp; xử lý dữ liệu lưu hỏng; giới hạn thời gian frame bị treo.
* Bài chạy dài mô phỏng 240 giây: đối tượng thế giới được tái sử dụng, đàn không vượt 12, tốc độ tăng theo quãng đường và có thu xu/lật xe.
* `mvn clean package -Pandroid,windows`: core, desktop và Android build thành công. APK package `vn.zombierush.runner`, minSdk 23, targetSdk 35, chữ ký v1/v2/v3 được xác minh.
* Cửa sổ Swing thật dưới Xvfb: nhận mouse press/release, xác nhận nhân vật nhảy, render và xác nhận có key binding SPACE. Kết quả PASS. Script chờ tín hiệu `-displayfd` của Xvfb trước khi mở Java.
* Đã xuất và kiểm tra ảnh màn hình chính/bàn chạy ở 1920 × 1080. Source dùng chung hệ tọa độ 960 × 540, scale theo cửa sổ và vùng an toàn Android.
* Installer NSIS đóng gói launcher, JRE Windows x64, giấy phép và trình gỡ cài đặt riêng của game. Đã kiểm tra header PE của installer/launcher và checksum runtime.
* ZIP source được giải nén vào thư mục mới có khoảng trắng, build lại offline từ Maven cache với cả profile Android/Windows: 18 kiểm thử đạt, dựng được APK và EXE. Smoke cửa sổ/chuột/SPACE từ chính bản source giải nén cũng PASS.
* Đã sửa tình huống treo khởi tạo MouseEvent trên X11 bằng cách nạp lớp sự kiện trước khi mở cửa sổ; kiểm thử vẫn thực hiện đầy đủ thao tác nhảy/thả và SPACE, không bỏ qua bước tương tác.

Giới hạn: môi trường Linux không thay thế kiểm thử cài đặt và chơi trên Windows/điện thoại Android thật. APK dùng khóa phát triển; EXE chưa ký Authenticode. Không có Google Play release.
