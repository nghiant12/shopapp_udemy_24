package com.project.shopapp.components;

import com.project.shopapp.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LocalizationUtil {
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    public String getMessage(String messageKey, Object... params) {
        HttpServletRequest req = WebUtils.getCurrentRequest();
        Locale locale = localeResolver.resolveLocale(req);
        return messageSource.getMessage(messageKey, params, locale);
    }
}
