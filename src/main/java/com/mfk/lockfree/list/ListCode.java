package com.mfk.lockfree.list;

/**
 * Status codes used in {@link LockFreeConcurrentList} APIs
 *
 * @author farhankhan.
 */
public enum ListCode {
    SUCCESS(0, "Success"),
    NULL(1, "Please refrain from using null."),
    EMPTY_LIST(2, "Cannot perform requested operation because the list is empty."),
    NOT_FOUND(3, "Given item not found in the list.");

    private final int code;
    private final String description;

    ListCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
