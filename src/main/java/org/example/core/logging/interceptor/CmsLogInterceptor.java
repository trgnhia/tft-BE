package org.example.core.logging.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class CmsLogInterceptor implements HandlerInterceptor {

    private final InternalLogQueue internalLogQueue;
    private final ObjectMapper objectMapper;

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

        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return;
        }

        try {
            Instant startTime = (Instant) request.getAttribute("startTime");
            Instant endTime = Instant.now();
            int durationMs = (int) (endTime.toEpochMilli() - startTime.toEpochMilli());

            String actionName = switch (method.toUpperCase()) {
                case "POST" -> "THÊM MỚI";
                case "PUT", "PATCH" -> "CẬP NHẬT";
                case "DELETE" -> "XÓA";
                default -> "THAO TÁC KHÁC";
            };

            String requestBody = "";
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    requestBody = new String(buf, StandardCharsets.UTF_8);
                }
            }

            String username = "anonymous";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                username = auth.getName();
            }

            int status = response.getStatus();
            String errorMessage = null;

            if (ex != null) {
                errorMessage = ex.getMessage();
            } else if (status < 200 || status >= 300) {
                // Kiểm tra xem Response có được bọc không
                if (response instanceof ContentCachingResponseWrapper responseWrapper) {
                    byte[] responseArray = responseWrapper.getContentAsByteArray();
                    if (responseArray.length > 0) {
                        try {
                            String responseStr = new String(responseArray, responseWrapper.getCharacterEncoding());
                            JsonNode jsonNode = objectMapper.readTree(responseStr);

                            if (jsonNode.has("message")) {
                                errorMessage = jsonNode.get("message").asText();
                            }
                        } catch (Exception e) {
                            log.warn("Không thể parse JSON từ response body để lấy lỗi", e);
                        }
                    }
                }

//                // Backup: Nếu parse fail (hoặc không phải JSON), vẫn móc lỗi từ request attribute như cũ
//                if (errorMessage == null) {
//                    Object customError = request.getAttribute("errorMessage");
//                    if (customError != null) errorMessage = customError.toString();
//                }
            }

            if (errorMessage != null && errorMessage.length() > 500) {
                errorMessage = errorMessage.substring(0, 497) + "...";
            }

            CmsLog realLog = new CmsLog();
            realLog.setUsername(username);
            realLog.setEndpoint(request.getRequestURI());
            realLog.setHttpMethod(method);
            realLog.setActionName(actionName);
            realLog.setIpAddress(request.getRemoteAddr());
            realLog.setRequestBody(requestBody);
            realLog.setResultStatus(status);
            realLog.setStartTime(startTime);
            realLog.setEndTime(endTime);
            realLog.setDurationMs(durationMs);
            realLog.setErrorMessage(errorMessage);

            // Đẩy vào Queue
            internalLogQueue.push(realLog);

        } catch (Exception e) {
            log.error("Lỗi trong quá trình ghi CMS Log bất đồng bộ", e);
        }
    }
}