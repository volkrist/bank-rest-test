package com.example.bankcards.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.net.URI;
import java.util.Locale;

/**
 * Открывает Swagger UI в браузере по умолчанию после старта приложения (только профиль dev).
 */
@Profile("dev")
@Component
@Slf4j
public class BrowserLauncher {

    @Value("${server.port:8080}")
    private int serverPort;

    @EventListener(ApplicationReadyEvent.class)
    public void openSwaggerWhenReady() {
        if (GraphicsEnvironment.isHeadless()) {
            log.debug("Browser auto-open skipped: headless environment");
            return;
        }
        String url = "http://localhost:" + serverPort + "/swagger-ui/index.html";
        // Небольшая задержка, чтобы сервер точно успел принять соединения
        Thread launcher = new Thread(() -> {
            try {
                Thread.sleep(1500);
                openBrowser(url);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.debug("Browser auto-open interrupted: {}", e.getMessage());
            }
        });
        launcher.setDaemon(true);
        launcher.start();
    }

    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                log.info("Swagger UI opened: {}", url);
                return;
            }
        } catch (Exception e) {
            log.debug("Desktop.browse failed: {}", e.getMessage());
        }
        // Запасной вариант для Windows (IDE/терминал без Desktop)
        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            try {
                new ProcessBuilder("cmd", "/c", "start", "", url).start();
                log.info("Swagger UI opened (cmd): {}", url);
            } catch (Exception e) {
                log.debug("Browser auto-open skipped: {}", e.getMessage());
            }
        }
    }
}
