package org.example.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.example.annotations.RequirePermission;
import org.example.security.SecurityUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SecurityAspect {
    @Pointcut("execution(* org.example.controller..*(..))")
    private void controllerLayer() {
    }

    @Before("controllerLayer() && @annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof SecurityUser securityUser)) {
            throw new AccessDeniedException("Unauthorized");
        }
        String requiredAuthority = String.format("%s_%s",
                requirePermission.resource(),
                requirePermission.permission());

        boolean hasPermission = securityUser.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals(requiredAuthority));

        if (!hasPermission) {
            throw new AccessDeniedException(
                    String.format("Access Denied: Missing authority '%s'", requiredAuthority)
            );
        }
    }
}
