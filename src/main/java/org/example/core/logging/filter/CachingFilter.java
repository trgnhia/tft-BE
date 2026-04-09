package org.example.core.logging.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class CachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String contentType = request.getContentType();
        String requestURI = request.getRequestURI();

        boolean hasBody = "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method);

        // dieu kien khong boc request
        boolean isMultipart = contentType != null && contentType.toLowerCase().startsWith("multipart/");
        boolean isImportApi = requestURI.contains("/import") || requestURI.contains("/upload");

        // chi boc neu co body va khong phai file upload
        HttpServletRequest requestToUse = request;
        if (hasBody && !isMultipart && !isImportApi && !(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(request);
        }

        // luon boc respon
        HttpServletResponse responseToUse = response;
        if (!(response instanceof ContentCachingResponseWrapper)) {
            responseToUse = new ContentCachingResponseWrapper(response);
        }

        try {
            filterChain.doFilter(requestToUse, responseToUse);
        } finally {
            // tra response
            if (responseToUse instanceof ContentCachingResponseWrapper wrappedResponse) {
                wrappedResponse.copyBodyToResponse();
            }
        }
    }
}