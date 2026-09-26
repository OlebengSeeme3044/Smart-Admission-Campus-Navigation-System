package za.ac.spu.sacns.models;

import java.io.Serializable;

public class DashboardModule implements Serializable {
    private String id;
    private String title;
    private String description;
    private String iconEmoji;
    private int iconResId;
    private String badgeText;
    private int accentColorRes;
    private Class<?> targetActivityClass;

    public DashboardModule(String id, String title, String description, int iconResId, String badgeText, int accentColorRes, Class<?> targetActivityClass) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.badgeText = badgeText;
        this.accentColorRes = accentColorRes;
        this.targetActivityClass = targetActivityClass;
    }

    public DashboardModule(String id, String title, String description, String iconEmoji, String badgeText, int accentColorRes, Class<?> targetActivityClass) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconEmoji = iconEmoji;
        this.badgeText = badgeText;
        this.accentColorRes = accentColorRes;
        this.targetActivityClass = targetActivityClass;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIconEmoji() {
        return iconEmoji;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public int getAccentColorRes() {
        return accentColorRes;
    }

    public Class<?> getTargetActivityClass() {
        return targetActivityClass;
    }
}
