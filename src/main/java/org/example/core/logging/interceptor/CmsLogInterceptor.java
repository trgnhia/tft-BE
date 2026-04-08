package org.example.core.logging.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.logging.InternalLogQueue;
import org.example.entities.CmsLog;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class CmsLogInterceptor implements HandlerInterceptor {

    private final InternalLogQueue internalLogQueue;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("INTERCEPTOR Đã tóm được Request vào API: {}", request.getRequestURI());
        request.setAttribute("startTime", Instant.now());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.info("INTERCEPTOR Đang xử lý log sau khi API chạy xong...");
        String method = request.getMethod();

        // Chỉ log các thao tác thay đổi dữ liệu
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return;
        }

        try {
            Instant startTime = (Instant) request.getAttribute("startTime");
            Instant endTime = Instant.now();
            int durationMs = (int) (endTime.toEpochMilli() - startTime.toEpochMilli());

            // 1. TỰ ĐỘNG PHÂN LOẠI ACTION_NAME
            String actionName = switch (method.toUpperCase()) {
                case "POST" -> "THÊM MỚI";
                case "PUT", "PATCH" -> "CẬP NHẬT";
                case "DELETE" -> "XÓA";
                default -> "THAO TÁC KHÁC";
            };

            // 2. Lấy Body từ Wrapper
            String requestBody = "";
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    requestBody = new String(buf, StandardCharsets.UTF_8);
                }
            }

            // 3. Lấy User đang thao tác
            String username = "anonymous";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                username = auth.getName();
            }

            // 4. Đóng gói Log
            CmsLog realLog = new CmsLog();
            realLog.setUsername(username);
            realLog.setEndpoint(request.getRequestURI());
            realLog.setHttpMethod(method);

            // Lưu nhãn Tiếng Việt vào đây
            realLog.setActionName(actionName);

            realLog.setIpAddress(request.getRemoteAddr());
            realLog.setRequestBody(requestBody);
            realLog.setResultStatus(response.getStatus());
            realLog.setStartTime(startTime);
            realLog.setEndTime(endTime);
            realLog.setDurationMs(durationMs);

            String errorMessage = null;
            if (ex != null) {
                errorMessage = ex.getMessage();
            } else {
                // Móc lỗi từ GlobalExceptionHandler ra
                Object customError = request.getAttribute("errorMessage");
                if (customError != null) {
                    errorMessage = customError.toString();
                } else {
                    Object defaultError = request.getAttribute("jakarta.servlet.error.message");
                    if (defaultError != null && !defaultError.toString().isBlank()) {
                        errorMessage = defaultError.toString();
                    }
                }
            }

            // Cắt bớt nếu lỗi quá dài tránh tràn DB
            if (errorMessage != null && errorMessage.length() > 500) {
                errorMessage = errorMessage.substring(0, 497) + "...";
            }

            // Ghi lỗi vào DB
            realLog.setErrorMessage(errorMessage);

            // 5. Đẩy vào Queue
            internalLogQueue.push(realLog);

        } catch (Exception e) {
            log.error("Lỗi trong quá trình ghi CMS Log bất đồng bộ", e);
        }
    }
}