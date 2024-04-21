package ru.yandex.practicum.filmorate.controller;

import org.apache.logging.log4j.CloseableThreadContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class ThreadContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try (final CloseableThreadContext.Instance ignored = CloseableThreadContext
                .put("REQUEST", UUID.randomUUID().toString())) {
            filterChain.doFilter(request, response);
        }
    }
}