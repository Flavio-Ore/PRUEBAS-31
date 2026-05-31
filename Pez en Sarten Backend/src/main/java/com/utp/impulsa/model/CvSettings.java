package com.utp.impulsa.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "cv_settings")
public class CvSettings {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "theme_color", nullable = false, length = 10)
    private String themeColor = "#4F46E5";

    @Column(name = "font_family", nullable = false, length = 50)
    private String fontFamily = "font-sans";

    @Column(name = "spacing", nullable = false, length = 50)
    private String spacing = "space-y-5";

    public CvSettings() {
    }

    public CvSettings(UUID userId) {
        this.userId = userId;
        this.themeColor = "#4F46E5";
        this.fontFamily = "font-sans";
        this.spacing = "space-y-5";
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public String getSpacing() {
        return spacing;
    }

    public void setSpacing(String spacing) {
        this.spacing = spacing;
    }
}
