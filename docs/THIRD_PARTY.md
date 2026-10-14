# Thành phần bên thứ ba

Tên, hình vẽ vector, bố cục và source của Vườn Ghép Ba được tạo riêng. Game lấy cảm hứng từ thể loại tile triple match, không sử dụng hình ảnh, nhạc hoặc source của Tile Explorer.

* **Eclipse Temurin OpenJDK JRE 21.0.12.1+1**, Windows x64: GPLv2 với Classpath Exception. Bản Windows đi kèm đầy đủ thư mục `runtime/legal`, `runtime/NOTICE` và thông tin `runtime/release`. Source và bản phân phối: https://github.com/adoptium/temurin21-binaries/releases/tag/jdk-21.0.12.1%2B1 ; source OpenJDK tương ứng: https://github.com/adoptium/jdk21u/tree/jdk-21.0.12.1%2B1 . Các DLL và thành phần được liệt kê trong `runtime/legal` giữ nguyên giấy phép tương ứng.
* **Launch4j 3.50**, công cụ build: BSD-3-Clause. Header nhúng trong EXE này dùng giấy phép dạng MIT với điều khoản không dùng tên tác giả để quảng bá; văn bản gốc `head/LICENSE.txt` được giữ trong `launch4j-license/HEADER-LICENSE.txt`, cùng giấy phép MinGW. Xem https://launch4j.sourceforge.net/ .
* **NSIS 3.11**, công cụ tạo installer: zlib/libpng và giấy phép của các plugin/thành phần được dùng; xem https://nsis.sourceforge.io/License .
* **JUnit 4.13.2** (chỉ dùng kiểm thử): Eclipse Public License 1.0; **Hamcrest 1.3** (chỉ dùng kiểm thử): BSD-3-Clause. Không đóng gói hai thư viện này trong game.
* Maven và Android SDK/build-tools là công cụ phát triển, không đóng gói vào APK hoặc source ZIP. SDK dùng theo điều khoản của Android SDK: https://developer.android.com/studio/terms .

APK không có thư viện bên thứ ba: chỉ source Java của game, tài nguyên vector riêng và API hệ thống Android. Hiệu ứng âm thanh máy tính được tổng hợp trong mã; Android dùng ToneGenerator của hệ thống.
