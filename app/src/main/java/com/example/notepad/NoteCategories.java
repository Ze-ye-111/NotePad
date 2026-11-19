package com.example.notepad;

public final class NoteCategories {
    public static final String ALL = "全部分类";
    public static final String UNCATEGORIZED = "未分类";
    public static final String STUDY = "学习";
    public static final String WORK = "工作";
    public static final String LIFE = "生活";
    public static final String IDEA = "灵感";
    public static final String PRIVATE = "私密笔记本";

    private NoteCategories() {
    }

    public static String safe(String category) {
        if (category == null || category.trim().isEmpty()) {
            return UNCATEGORIZED;
        }
        return category.trim();
    }
}
