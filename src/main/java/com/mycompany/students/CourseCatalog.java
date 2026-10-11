package com.mycompany.students;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CourseCatalog {

    private static final Map<String, String> COURSE_NAMES;

    static {
        Map<String, String> courses = new LinkedHashMap<>();
        courses.put("AB COMM", "Bachelor of Arts in Communication");
        courses.put("AB ENGLISH", "Bachelor of Arts in English");
        courses.put("BSA", "Bachelor of Science in Accountancy");
        courses.put("BSAGRI", "Bachelor of Science in Agriculture");
        courses.put("BS ARCH", "Bachelor of Science in Architecture");
        courses.put("BSBIO", "Bachelor of Science in Biology");
        courses.put("BSBA",
                "Bachelor of Science in Business Administration");
        courses.put("BSCHEM", "Bachelor of Science in Chemistry");
        courses.put("BSCE",
                "Bachelor of Science in Civil Engineering");
        courses.put("BSCPE",
                "Bachelor of Science in Computer Engineering");
        courses.put("BSCS",
                "Bachelor of Science in Computer Science");
        courses.put("BSCRIM", "Bachelor of Science in Criminology");
        courses.put("BSEE",
                "Bachelor of Science in Electrical Engineering");
        courses.put("BSECE",
                "Bachelor of Science in Electronics Engineering");
        courses.put("BS ENTREP",
                "Bachelor of Science in Entrepreneurship");
        courses.put("BS ENV SCI",
                "Bachelor of Science in Environmental Science");
        courses.put("BSHM",
                "Bachelor of Science in Hospitality Management");
        courses.put("BSIS",
                "Bachelor of Science in Information Systems");
        courses.put("BSIT",
                "Bachelor of Science in Information Technology");
        courses.put("BS MATH",
                "Bachelor of Science in Mathematics");
        courses.put("BSME",
                "Bachelor of Science in Mechanical Engineering");
        courses.put("BSMT",
                "Bachelor of Science in Medical Technology");
        courses.put("BS MID", "Bachelor of Science in Midwifery");
        courses.put("BSN", "Bachelor of Science in Nursing");
        courses.put("BS PSYCH", "Bachelor of Science in Psychology");
        courses.put("BS SW", "Bachelor of Science in Social Work");
        courses.put("BSTM",
                "Bachelor of Science in Tourism Management");
        courses.put("BEED",
                "Bachelor of Elementary Education");
        courses.put("BPED",
                "Bachelor of Physical Education");
        courses.put("BSED",
                "Bachelor of Secondary Education");
        COURSE_NAMES = Collections.unmodifiableMap(courses);
    }

    private CourseCatalog() {
    }

    public static List<String> getCourseCodes() {
        return Collections.unmodifiableList(
                Arrays.asList(COURSE_NAMES.keySet().toArray(new String[0]))
        );
    }

    public static String canonicalCode(String course) {
        if (course == null) {
            return null;
        }

        String normalized = course.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        switch (normalized) {
            case "BSIT":
                return "BSIT";
            case "BSCS":
                return "BSCS";
            case "BSIS":
                return "BSIS";
            case "CRIM":
            case "CRIMINOLOGY":
            case "BS CRIM":
                return "BSCRIM";
            case "BSN":
            case "NURSING":
                return "BSN";
            case "BSED":
            case "EDUCATION":
            case "SECONDARY EDUCATION":
                return "BSED";
            case "BEED":
            case "ELEMENTARY EDUCATION":
                return "BEED";
            case "BSBA":
                return "BSBA";
            case "BSA":
                return "BSA";
            case "BSHM":
                return "BSHM";
            case "BSTM":
                return "BSTM";
            default:
                break;
        }

        if (COURSE_NAMES.containsKey(normalized)) {
            return normalized;
        }
        for (Map.Entry<String, String> entry : COURSE_NAMES.entrySet()) {
            if (entry.getValue().toUpperCase(Locale.ROOT).equals(normalized)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static String getDisplayName(String course) {
        String code = canonicalCode(course);
        return code == null ? course : COURSE_NAMES.get(code);
    }
}
