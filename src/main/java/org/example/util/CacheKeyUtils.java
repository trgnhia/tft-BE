package org.example.util;

public final class CacheKeyUtils {

    private CacheKeyUtils() {
    }

    public static String publishedItemsKey(int page,
                                           int size,
                                           String keyword,
                                           Long setId,
                                           String tier,
                                           String sortBy,
                                           String sortDir) {
        return String.format(
                "published:page:%d:size:%d:keyword:%s:set:%s:tier:%s:sort:%s:%s",
                page,
                size,
                normalize(keyword),
                normalize(setId),
                normalize(tier),
                normalize(sortBy),
                normalize(sortDir)
        );
    }

    private static String normalize(Object value) {
        if (value == null) {
            return "all";
        }
        String text = String.valueOf(value).trim();
        return text.isBlank() ? "all" : text;
    }
}
