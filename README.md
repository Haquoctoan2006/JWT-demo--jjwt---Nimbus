# JWT Demo – WEBPR330479 (Lập trình Web)

Bài tập theo bài giảng "Json Web Token" (ThS. Nguyễn Hữu Trung):

1. **Làm bài tập ví dụ trong bài giảng JWT** – implement lại đầy đủ luồng
   đăng ký / đăng nhập / sinh JWT / filter xác thực / exception handler
   đúng như slide, dùng thư viện **`io.jsonwebtoken` (jjwt) 0.12.6**.
2. **Sử dụng thư viện Nimbus thay thế jjwt** – thêm `com.nimbusds:nimbus-jose-jwt`,
   cài đặt lại việc sinh/kiểm tra token bằng Nimbus JOSE+JWT, có thể bật/tắt
   bằng 1 dòng cấu hình mà không phải sửa Controller/Filter.

## Kiến trúc

Toàn bộ logic sinh/kiểm tra JWT được trừu tượng hoá qua interface
`vn.iotstar.services.TokenService` với 2 cài đặt:

| Class | Thư viện | Kích hoạt |
|---|---|---|
| `JjwtTokenService` | `io.jsonwebtoken:jjwt-*` (giống bài giảng) | `app.jwt.impl=jjwt` (mặc định) |
| `NimbusTokenService` | `com.nimbusds:nimbus-jose-jwt` | `app.jwt.impl=nimbus` |

Chỉ cần đổi property `app.jwt.impl` trong `src/main/resources/application.properties`
(hoặc truyền `--app.jwt.impl=nimbus` khi chạy), toàn bộ `Filter`, `Controller`
giữ nguyên vì chúng chỉ phụ thuộc vào interface `TokenService`.

Các thành phần khác bám sát bài giảng:
- `entity/User.java` – implements `UserDetails`
- `filter/JwtAuthenticationFilter.java`
- `configs/ApplicationConfiguration.java`, `configs/SecurityConfiguration.java`
- `controllers/AuthenticationController.java` (`/auth/signup`, `/auth/login`)
- `controllers/UserController.java` (`/users/me`, `/users/`)
- `exception/GlobalExceptionHandler.java`

Project dùng **H2 in-memory DB** để chạy ngay không cần cài MySQL (đổi sang
MySQL bằng cách sửa `application.properties`, phần cấu hình MySQL đã có sẵn,
comment lại giống slide).

## Yêu cầu môi trường

- JDK 17+
- Maven (hoặc dùng Maven Wrapper nếu bạn thêm `mvnw`)
- VSCode + extension **"Extension Pack for Java"** và **"Spring Boot Extension Pack"**

## Cách chạy trên VSCode

1. Clone repo:
   ```bash
   git clone <link-github-cua-ban>
   cd jwt-demo
   code .
   ```
2. Mở file `src/main/java/vn/iotstar/JwtDemoApplication.java`, bấm
   **Run** (nút play phía trên `main`) hoặc dùng terminal:
   ```bash
   mvn spring-boot:run
   ```
3. Ứng dụng chạy ở `http://localhost:8005`.

## Test API (Postman / Thunder Client)

1. **Đăng ký**: `POST /auth/signup`
   ```json
   { "email": "trungnh@hcmute.edu.vn", "password": "123456", "fullName": "Nguyễn Hữu Trung" }
   ```
2. **Đăng nhập**: `POST /auth/login`
   ```json
   { "email": "trungnh@hcmute.edu.vn", "password": "123456" }
   ```
   → trả về `{ "token": "...", "expiresIn": 3600000 }`
3. Copy `token`, gọi các API cần xác thực với header:
   `Authorization: Bearer <token>`
   - `GET /users/me`
   - `GET /users/`

## Chuyển đổi giữa jjwt và Nimbus

Mở `src/main/resources/application.properties`:
```properties
# dùng jjwt (giống bài giảng)
app.jwt.impl=jjwt

# hoặc dùng Nimbus JOSE+JWT
app.jwt.impl=nimbus
```
Chạy lại ứng dụng — toàn bộ API, filter, request/response giữ nguyên,
chỉ khác thư viện sinh/verify token bên dưới. Có thể decode token ở cả
2 chế độ tại https://jwt.io để so sánh header/payload/signature.

## Ghi chú bảo mật (theo đúng khuyến nghị trong bài giảng)

- Secret key nằm trong `application.properties` — **không commit secret thật**
  lên GitHub public, nên dùng biến môi trường khi deploy thật.
- Token có thời hạn (`security.jwt.expiration-time`), tránh để hạn quá dài.
- Luôn dùng HTTPS khi deploy, không lưu dữ liệu nhạy cảm trong localStorage.

## Lệnh chạy nhanh

```bash
mvn clean spring-boot:run                      # chạy với jjwt (mặc định)
mvn spring-boot:run -Dspring-boot.run.arguments=--app.jwt.impl=nimbus   # chạy với Nimbus
```

## Đẩy lên GitHub để nộp bài

```bash
git init
git add .
git commit -m "JWT demo: jjwt + Nimbus"
git branch -M main
git remote add origin https://github.com/<username>/jwt-demo.git
git push -u origin main
```
