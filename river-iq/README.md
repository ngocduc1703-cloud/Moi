# Qua Sông IQ

Game trí tuệ Java/Swing bằng tiếng Việt, gồm 6 thử thách qua sông tăng dần độ khó. Toàn bộ đồ họa được vẽ bằng Java2D nên source gọn, chạy offline và không cần tải asset ngoài.

## Tính năng

* 6 bộ luật khác nhau: Sói–Dê–Bắp cải, thú cưng, học giả–Robot, ba cặp đôi và gia đình–cảnh sát.
* Gợi ý thông minh bằng thuật toán tìm kiếm BFS từ đúng trạng thái hiện tại.
* Hoàn tác, chơi lại, chấm 1–3 sao, mở khóa màn và tự lưu kỷ lục.
* Giao diện co giãn theo cửa sổ, hiệu ứng sóng nước/thuyền và điều khiển chuột hoặc bàn phím.
* Không Internet, quảng cáo, tài khoản hoặc thư viện đồ họa bên ngoài.

## Mở source bằng NetBeans

1. Cài JDK 21 và Apache NetBeans có hỗ trợ Java/Maven.
2. Trong NetBeans chọn **File → Open Project**.
3. Chọn thư mục `river-iq` chứa tệp `pom.xml` này.
4. Nhấp phải project **Qua Sông IQ** → **Clean and Build**.
5. Nhấp **Run** để chơi. Cấu hình chạy đã có sẵn trong `nbactions.xml`.

NetBeans sẽ tự nhận cấu trúc Maven; không cần tạo project mới hoặc thêm JAR thủ công.

## Build và chạy bằng dòng lệnh

```sh
cd /workspace/Moi
source scripts/cloud-java-env.sh
cd river-iq
mvn "${MAVEN_SETTINGS[@]}" -Dmaven.repo.local="$CACHE_DIR/m2" clean package
java -jar target/river-iq-1.0.0.jar
```

Build đồng thời desktop JAR, Windows launcher và APK Android:

```sh
mvn "${MAVEN_SETTINGS[@]}" -Dmaven.repo.local="$CACHE_DIR/m2" clean package -Pandroid,windows
bash scripts/build-windows.sh
python3 scripts/package-source.py
```

Đầu ra nằm trong `dist`: APK Android, bộ cài Windows kèm Java và ZIP source NetBeans.

Kiểm thử:

```sh
mvn "${MAVEN_SETTINGS[@]}" -Dmaven.repo.local="$CACHE_DIR/m2" test
```

Xuất ảnh không cần màn hình:

```sh
java -jar target/river-iq-1.0.0.jar --screenshot home.png
java -jar target/river-iq-1.0.0.jar --screenshot game.png game
```

## Điều khiển

* Nhấp nhân vật để chọn hoặc bỏ chọn, sau đó nhấp **Qua bờ**.
* `Enter`: đi thuyền; `H`: gợi ý; `Z`: hoàn tác; `R`: chơi lại; `Esc`: chọn màn.
* Tiến độ lưu tại `~/.qua-song-iq/progress.properties`.

## Cấu trúc source

* `PuzzleCatalog.java`: nhân vật, luật và nội dung các màn.
* `GameState.java`: trạng thái, di chuyển, hoàn tác và kiểm tra luật.
* `Solver.java`: bộ giải BFS và hệ thống gợi ý.
* `RiverIQApp.java`: cửa sổ Swing và toàn bộ đồ họa Java2D.
* `PuzzleTest.java`: kiểm tra mọi màn có lời giải và mọi bước đi đều hợp lệ.

Yêu cầu chạy: Java 8 trở lên. Khuyến nghị JDK 21 để phát triển.
