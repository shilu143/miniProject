package org.iitrpr;

public class TestingStuffs {
    enum Status {
        A("A", 10),
        A_minus("A-", 9),
        B("B", 8),
        B_minus("B-", 7),
        C("C", 6),
        C_minus("C-", 5),
        D("D", 4),
        E("E", 2),
        F("F", 0);

        private final String key;
        private final Integer value;

        Status(String key, Integer value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }
        public Integer getValue() {
            return value;
        }
    }

    public static void main(String[] args) {
    }
}
