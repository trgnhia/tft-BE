# TFT CMS Backend

Dự án là backend Spring Boot cho hệ thống quản lý dữ liệu Teamfight Tactics (TFT). Ứng dụng cung cấp API public để frontend đọc dữ liệu game và API CMS để quản trị nội dung, người dùng, phân quyền, upload tài nguyên, import dữ liệu và giao tiếp realtime.

## Tính năng chính

### Quản lý dữ liệu TFT

- Quản lý mùa/phiên bản game (sets), bao gồm danh sách public, danh sách CMS, tạo mới, cập nhật và xóa mềm.
- Quản lý tướng (champions) theo set, gồm thông tin cơ bản, cost, slug, mã định danh, ảnh, chỉ số JSON và liên kết hệ/tộc.
- Quản lý hệ/tộc (traits), gồm tên, slug, loại trait, icon, mô tả, breakpoint JSON và dữ liệu theo từng set.
- Quản lý trang bị (items), gồm ảnh, mô tả, tier, chỉ số và hiệu ứng lưu dạng JSON.
- Quản lý đội hình (team comps), gồm tên, slug, style, tier, set liên quan và danh sách tướng trong đội hình.
- Quản lý gợi ý trang bị cho từng tướng, hỗ trợ thứ tự ưu tiên và dữ liệu public/CMS riêng.

### API public và API CMS

- API public phục vụ người dùng cuối đọc dữ liệu đã publish như champions, traits, items, sets và team comps.
- API CMS phục vụ quản trị viên thao tác dữ liệu: tạo, cập nhật, xóa mềm, khôi phục, xóa hàng loạt, tìm kiếm, lọc và phân trang.
- Một số module có endpoint thống kê tổng quan để hỗ trợ dashboard CMS.

### Xác thực và phân quyền

- Hỗ trợ đăng ký, đăng nhập, refresh token và logout bằng JWT.
- Hỗ trợ lưu access token/refresh token qua cookie ở nhóm API `auth2`.
- Quản lý người dùng CMS: tạo user, cập nhật hồ sơ, đổi mật khẩu, reset mật khẩu, gán role, xóa mềm và khôi phục.
- Quản lý role và permission theo mô hình RBAC.
- Kiểm tra quyền bằng annotation `@RequirePermission`; role admin được bỏ qua kiểm tra chi tiết, các role khác cần đúng quyền theo resource/action.

### Import, upload và quản trị tài nguyên

- Hỗ trợ import champion từ file CSV/Excel, có xử lý header, validate từng dòng, trả kết quả lỗi theo dòng và hỗ trợ tải file template.
- Hỗ trợ import user qua file upload.
- Hỗ trợ upload/xóa ảnh cho champion và icon cho trait.
- File upload được lưu theo thư mục cấu hình và chuyển đổi thành public URL để frontend sử dụng.

### Chat và thông báo realtime

- Cấu hình WebSocket STOMP tại endpoint `/ws`.
- Client gửi message qua prefix `/app`, server broadcast qua `/topic` và gửi message riêng qua `/user/queue`.
- Hỗ trợ chat 1-1 giữa user CMS, gồm conversation, participant và message.
- Hỗ trợ REST API để lấy danh sách cuộc trò chuyện và lịch sử tin nhắn.
- Hỗ trợ notification realtime cho CMS qua WebSocket, đồng thời lưu notification vào database.

### Logging, audit và hạ tầng

- Ghi log thao tác CMS qua interceptor/filter, lưu endpoint, method, username, IP, body request, trạng thái xử lý, lỗi và thời gian thực thi.
- Các entity chính có hỗ trợ audit như created/updated/deleted và created_by/updated_by tùy module.
- Sử dụng Flyway để quản lý migration PostgreSQL.
- Chuẩn hóa response API bằng `ApiResponse` và `PageResponse`.
- Có global exception handler cho REST API và WebSocket.
- Hỗ trợ i18n message tiếng Việt/tiếng Anh.

## Công nghệ sử dụng

- Java 17
- Spring Boot 3
- Spring Web, Spring Security, Spring Data JPA, Spring WebSocket, Spring AOP
- PostgreSQL
- Flyway
- JWT
- Lombok
- MapStruct
- Redis
- Apache POI và Apache Commons CSV
- Springdoc OpenAPI/Swagger UI
