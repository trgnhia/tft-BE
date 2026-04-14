# Phân tích dự án & Document về luồng WebSocket

Dự án hiện tại là một ứng dụng Spring Boot phục vụ cho hệ thống CMS. 

## 1. Hiện trạng dự án - Kiến trúc & Luồng xử lý
- **Framework & Công nghệ**: Spring Boot, Spring Data JPA, Lombok, Websocket (STOMP). Cấu trúc dự án tuân theo mô hình Controller - Service - Repository.
- **WebSocket Config**: Cấu hình tại `WebSocketConfig.java`, public endpoint ở `/ws`, sử dụng cấu hình STOMP với application prefix `/app` (thường dùng cho FE gọi lên) và broker prefix `/topic` (broadcast), `/queue` (point-to-point / user2user private message).
- **Tính năng Notification (Hiện tại)**: Đang được xây dựng dưới dạng broadcast cho toàn bộ các thiết bị đang kết nối CMS. Tại `NotificationServiceImpl.java`, khi có thông báo mới, hệ thống sẽ lưu Data và gọi API qua `SimpMessagingTemplate.convertAndSend("/topic/cms-notifications", response)` để bắn tín hiệu tới tất cả client Frontend đang subscribe tới channel này. 

## 2. Luồng xử lý (Dự kiến) - Chat 1-1 giữa account CMS

Dựa trên cấu hình Spring Websocket mặc định có sẵn và các Table dự kiến, mô hình hoạt động của ứng dụng Chat 1-1 sẽ diễn ra như sau:

### Database Design (Dự kiến)
**1. Bảng `conversations`**
Chứa thông tin quản lý cơ bản một phiên chat.
- `id`: Primary Key
- `created_at`: Thời gian bắt đầu
- `updated_at`: Cập nhật khi có tin nhắn mới

**2. Bảng `conversation_participants`**
Quản lý user tham gia vào chat 1-1. Thực chất với chat 1-1 sẽ có 2 participants tương ứng 1 conversation_id.
- `id`: Primary Key
- `conversation_id`: Ngoại kiểm xuất phát từ bảng `conversations`
- `user_id`: ID của User trong CMS
- `joined_at`: Thời điểm join

**3. Bảng `messages`**
Lưu trữ toàn bộ nội dung trong lịch sử tin nhắn.
- `id`: Primary Key
- `conversation_id`: Trỏ khóa về `conversations.id`
- `sender_id`: Khóa ngoại về bảng `users.id` gốc (người gửi)
- `content`: Nội dung chat
- `is_read`: Boolean check tin nhắn đã xem chưa
- `created_at`: Log thời gian bắn tin

### Luồng WebSocket STOMP (Flow Chat)

#### Bước 1: Khởi tạo kết nối & Subscribe (Client Side)
- Sau khi khởi động FE CMS, **User A** và **User B** sẽ kết nối với Websocket ở đường dẫn `/ws`.
- Mỗi Frontend phải tự động subscribe 2 địa chỉ chính:
  1. `/topic/cms-notifications`: Dành cho Notification dùng chung.
  2. `/user/queue/messages`: Dành cho tin nhắn cá nhân riêng tư. (User Destination Prefix của server đang để là `/user`).

#### Bước 2: Trigger gửi tin nhắn (User A -> User B)
- Khi **User A** ở Frontend chat riêng cho **User B**. Client A sẽ đóng gói payload (Sender, Receiver, Message, v.v...) dạng JSON và STOMP send thẳng lên WebSocket controller của Server với config:
  - **Path**: `/app/chat.send`

#### Bước 3: Backend xử lý (`ChatController`)
- Một class Spring Controller tại phía Backend (có gắn `@MessageMapping("/chat.send")`) bắt sự kiện truyền dữ liệu của **User A**.
- **Xử lý DB Service**: 
  - Validation check.
  - Insert dòng Data mới tạo vào table `messages`.
  - Cập nhật dòng `updated_at` trong bảng `conversations`.
- **Đẩy dữ liệu Websocket lại**:
  - Gọi method: `simpMessagingTemplate.convertAndSendToUser(userB_Identify, "/queue/messages", payload)`
  - *Lưu ý thiết kế*: `userB_Identify` phải là thuộc tính định danh User (ví dụ như mã ID dạng string hay username string định ra ở Spring Security Principal) giúp hệ thống khoanh vùng được chính xác user. Broker tự sinh ra endpoint `/user/{userB_Identify}/queue/messages` dành riệng cho User B.

#### Bước 4: Nhận tin nhắn (User B)
- Nhờ cơ chế nhận dạng trong STOMP Spring, event này sẽ map chính xác session được khởi tạo bởi **User B**.
- **User B** (đang Subscribe ở `/user/queue/messages`) nhận được Payload tin nhắn, thực hiện render UI append đoạn chat mới ra màn hình.

### Kết Luận
Việc hệ thống tận dụng Spring Websocket theo hình mẫu này sẽ quản lý chặt chẽ được `Channel` cho Notification Broadcast và `User Private Channel` cho tính năng nhắn tin cá nhân. Thậm chí nếu có thiết kế Group Chat nâng cao thì vẫn có thể dùng thêm endpoint `/topic/...` linh hoạt mà không sợ nhiễu traffic.
