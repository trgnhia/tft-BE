# Kế hoạch triển khai tính năng Import bỏ qua bản ghi lỗi và xuất file lỗi

## 1. Mục tiêu nghiệp vụ

- Khi import dữ liệu từ file, hệ thống không dừng toàn bộ nếu gặp bản ghi lỗi.
- Bản ghi lỗi phải được ghi rõ lý do lỗi vào cột cuối cùng của file kết quả lỗi.
- Bản ghi lỗi bị bỏ qua, hệ thống tiếp tục import bản ghi kế tiếp.
- Sau khi hoàn tất, người dùng nhận được file chứa các bản ghi lỗi để chỉnh sửa và import lại.

## 2. Phạm vi tính năng

- Áp dụng cho các màn hình import dữ liệu master/editor (champ, item, trait, set... theo module được chọn).
- Định dạng file đầu vào ưu tiên: Excel (.xlsx). Có thể mở rộng CSV ở giai đoạn sau.
- Chỉ thay đổi luồng import dữ liệu và phản hồi kết quả import, không thay đổi nghiệp vụ tạo/sửa dữ liệu thủ công hiện có.

## 3. Định nghĩa lỗi cần bắt

Mỗi dòng import có thể phát sinh một hoặc nhiều lỗi. Các nhóm lỗi chuẩn:

- Duplicate: trùng khóa nghiệp vụ (trong file hoặc trùng dữ liệu đã có DB).
- Format: sai định dạng (email, ngày tháng, số, enum...).
- Length: vượt giới hạn độ dài ký tự.
- Type: sai kiểu dữ liệu (ví dụ cột số nhưng nhập text).
- Required: thiếu dữ liệu bắt buộc.
- Reference: không tồn tại dữ liệu tham chiếu (FK, mã cha, danh mục liên quan).
- Business rule: vi phạm quy tắc nghiệp vụ đặc thù module.

Quy ước message lỗi:

- Dễ hiểu với người dùng nghiệp vụ.
- Có tên cột và lý do lỗi.
- Nếu nhiều lỗi trên một dòng, nối bằng dấu ; để người dùng xử lý một lần.

Ví dụ:

- name: bắt buộc; cost: phải là số nguyên; traitCode: không tồn tại

## 4. Yêu cầu đầu ra cho người dùng

- Nếu có lỗi: hệ thống trả file lỗi cùng cấu trúc file gốc và thêm cột cuối cùng tên Error Message.
- File lỗi chỉ chứa các dòng lỗi (không chứa dòng import thành công) để dễ sửa và import lại.
- Nếu không có lỗi: trả thống kê thành công, không cần file lỗi.
- Tên file lỗi đề xuất: import_errors_yyyyMMdd_HHmmss.xlsx.

## 5. Luồng xử lý nghiệp vụ đề xuất

1. Người dùng upload file import.
2. Hệ thống đọc header và map cột theo template.
3. Duyệt từng dòng dữ liệu theo thứ tự.
4. Validate từng dòng theo các nhóm lỗi chuẩn.
5. Nếu dòng hợp lệ:
   - Đưa vào danh sách ghi DB.
6. Nếu dòng lỗi:
   - Không ghi DB dòng đó.
   - Ghi thông tin lỗi vào Error Message của dòng đó trong bộ nhớ file lỗi.
7. Sau khi duyệt hết:
   - Batch insert/update các dòng hợp lệ.
   - Tạo file lỗi từ danh sách dòng lỗi (nếu có).
8. Trả kết quả tổng hợp cho UI:
   - totalRows, successRows, failedRows.
   - errorFileId hoặc errorFileDownloadUrl nếu failedRows > 0.

## 6. Thiết kế kỹ thuật ở mức plan (chưa code)

### 6.1. Cấu trúc thành phần

- ImportController: nhận file, gọi service import.
- ImportService: điều phối parse, validate, persist, build kết quả.
- RowValidator: tập hợp validator chung + validator theo module.
- ErrorFileBuilder: tạo file lỗi (xlsx/csv) với cột Error Message.
- ImportResult DTO: trả thống kê + thông tin tải file lỗi.

### 6.2. Mô hình dữ liệu xử lý trong bộ nhớ

- ImportRowContext:
  - rowNumber
  - rawData
  - mappedDto
  - errors (List<String>)
- ImportProcessingResult:
  - successRecords
  - failedRecords
  - errorFilePath hoặc object storage key

### 6.3. Transaction và tính nhất quán

- Không dùng 1 transaction cho toàn bộ file nếu yêu cầu bỏ qua dòng lỗi.
- Ghi DB theo batch cho nhóm dòng hợp lệ để tăng hiệu năng.
- Nếu lỗi hệ thống khi ghi DB (mất kết nối, deadlock):
  - Dừng phiên import.
  - Trả lỗi hệ thống, không phát sinh file lỗi nghiệp vụ cho phần chưa xử lý.

### 6.4. Logging và audit

- Log cấp INFO:
  - importId, userId, module, totalRows, successRows, failedRows, durationMs.
- Log cấp WARN cho dòng lỗi nghiệp vụ (không stacktrace lớn):
  - importId, rowNumber, errorSummary.
- Log cấp ERROR cho lỗi hệ thống:
  - importId, exception root cause.

## 7. API contract đề xuất

### 7.1. API import

- Method: POST
- Endpoint: /api/v1/{module}/import
- Request: multipart/form-data, file
- Response JSON:
  - importId
  - totalRows
  - successRows
  - failedRows
  - errorFileId (nullable)
  - message

### 7.2. API tải file lỗi

- Method: GET
- Endpoint: /api/v1/import-errors/{errorFileId}/download
- Response: file stream

## 8. UI/UX đề xuất

- Sau import hiển thị popup hoặc toast tổng kết:
  - Tổng số dòng, thành công, lỗi.
- Nếu có lỗi, hiển thị nút Tải file lỗi.
- Hướng dẫn người dùng: sửa file lỗi và import lại.

## 9. Kế hoạch triển khai theo giai đoạn

### Giai đoạn 1: Phân tích và chốt yêu cầu

- Chốt module áp dụng đầu tiên (ví dụ: champ).
- Chốt template import chuẩn (header, kiểu dữ liệu, rule).
- Chốt quy ước message lỗi song ngữ nếu cần i18n.

Deliverable:

- Tài liệu mapping cột và business rules cho module đầu tiên.

### Giai đoạn 2: Thiết kế kỹ thuật chi tiết

- Thiết kế DTO parse/import theo module.
- Thiết kế bộ validator và chuẩn hóa mã lỗi.
- Thiết kế format file lỗi và naming policy.

Deliverable:

- Technical design note + API contract final.

### Giai đoạn 3: Phát triển backend

- Xây luồng parse, validate theo dòng, bỏ qua dòng lỗi.
- Xây luồng tạo file lỗi và API download.
- Bổ sung log/audit import.

Deliverable:

- API import hoạt động end-to-end.

### Giai đoạn 4: Tích hợp frontend

- Gọi API import.
- Hiển thị thống kê kết quả.
- Tải file lỗi khi có failedRows.

Deliverable:

- Màn hình import có phản hồi đầy đủ cho user.

### Giai đoạn 5: Kiểm thử và rollout

- Unit test cho validator từng nhóm lỗi.
- Integration test cho import nhiều dòng (pass/fail mix).
- UAT với file thật từ nghiệp vụ.
- Theo dõi log sau release.

Deliverable:

- Test report + checklist release.

## 10. Test scenarios bắt buộc

- File 100 dòng: 95 hợp lệ, 5 lỗi, hệ thống vẫn import 95 dòng.
- Một dòng có nhiều lỗi: ghi đủ lỗi trong Error Message.
- Trùng trong file và trùng DB đều được bắt đúng.
- Sai kiểu dữ liệu và sai định dạng ngày/số.
- Header sai template: từ chối import ngay từ đầu, trả message rõ ràng.
- File rỗng hoặc chỉ có header.
- File rất lớn: kiểm tra hiệu năng và memory.

## 11. Phi chức năng và rủi ro

- Hiệu năng: giới hạn kích thước file và số dòng tối đa mỗi lần import.
- Bảo mật: kiểm tra extension + MIME type, chống upload file độc hại.
- Lưu trữ file lỗi: local hoặc object storage, có thời hạn xóa (ví dụ 7 ngày).
- Đồng thời: nhiều user import cùng lúc, cần importId độc lập.

## 12. Các quyết định cần chốt với nghiệp vụ

- Import theo chế độ nào: chỉ tạo mới hay upsert (create/update).
- Khi trùng khóa nghiệp vụ, ưu tiên update hay báo lỗi bỏ qua.
- Message lỗi hiển thị tiếng Việt, tiếng Anh, hay theo locale.
- File lỗi chỉ chứa dòng lỗi hay chứa toàn bộ dòng kèm trạng thái.
- SLA xử lý với file lớn (ví dụ 10k, 50k dòng).

## 13. Tiêu chí hoàn thành (Definition of Done)

- Import không dừng khi gặp lỗi dòng.
- Dòng lỗi bị bỏ qua và có Error Message rõ ràng.
- User tải được file lỗi sau import.
- Có thống kê tổng hợp chính xác.
- Có test cover cho các nhóm lỗi chính.
- Có tài liệu hướng dẫn sử dụng cho user nghiệp vụ.
