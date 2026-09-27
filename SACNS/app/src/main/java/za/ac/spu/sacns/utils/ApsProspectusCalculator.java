package za.ac.spu.sacns.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Sol Plaatje University Admission Point Score (APS) Calculator
 * Based strictly on the Sol Plaatje University 2026 Undergraduate Prospectus (Page 4).
 */
public class ApsProspectusCalculator {

    public static class SubjectEntry implements Serializable {
        private String subjectName;
        private int mark; // 0 - 100%
        private boolean isHomeLanguage; // gets language bonus if HL
        private boolean isPureMaths; // gets maths bonus if Pure Maths
        private boolean isLifeOrientation;

        public SubjectEntry(String subjectName, int mark, boolean isHomeLanguage, boolean isPureMaths, boolean isLifeOrientation) {
            this.subjectName = subjectName;
            this.mark = Math.max(0, Math.min(100, mark));
            this.isHomeLanguage = isHomeLanguage;
            this.isPureMaths = isPureMaths;
            this.isLifeOrientation = isLifeOrientation;
        }

        public String getSubjectName() { return subjectName; }
        public int getMark() { return mark; }
        public void setMark(int mark) { this.mark = Math.max(0, Math.min(100, mark)); }
        public boolean isHomeLanguage() { return isHomeLanguage; }
        public boolean isPureMaths() { return isPureMaths; }
        public boolean isLifeOrientation() { return isLifeOrientation; }

        public int getAchievementLevel() {
            if (mark >= 80) return 7;
            if (mark >= 70) return 6;
            if (mark >= 60) return 5;
            if (mark >= 50) return 4;
            if (mark >= 40) return 3;
            if (mark >= 30) return 2;
            return 1;
        }

        public int getPoints() {
            if (isLifeOrientation) {
                return getLifeOrientationPoints(mark);
            }
            int base = getStandardPoints(mark);
            int bonus = 0;
            if (isPureMaths) {
                bonus += getMathematicsBonus(mark);
            }
            if (isHomeLanguage) {
                bonus += getLanguageBonus(mark);
            }
            return base + bonus;
        }

        public int getBasePoints() {
            if (isLifeOrientation) return 0;
            return getStandardPoints(mark);
        }

        public int getBonusPoints() {
            if (isLifeOrientation) return 0;
            int bonus = 0;
            if (isPureMaths) bonus += getMathematicsBonus(mark);
            if (isHomeLanguage) bonus += getLanguageBonus(mark);
            return bonus;
        }
    }

    public static class ApsResult implements Serializable {
        public int totalAps;
        public int basePoints;
        public int mathBonusPoints;
        public int languageBonusPoints;
        public int lifeOrientationPoints;
        public boolean englishRequirementMet;
        public String englishFeedback;
        public List<SubjectResult> subjects = new ArrayList<>();
    }

    public static class SubjectResult implements Serializable {
        public String name;
        public int mark;
        public int level;
        public int points;
        public int bonus;
    }

    /**
     * Standard SPU Point Score (Prospectus 2026, Page 4):
     * 90 - 100%: 8 points
     * 80 - 89%:  7 points
     * 70 - 79%:  6 points
     * 60 - 69%:  5 points
     * 50 - 59%:  4 points
     * 40 - 49%:  3 points
     * 30 - 39%:  2 points
     * 0 - 29%:   1 point
     */
    public static int getStandardPoints(int mark) {
        if (mark >= 90) return 8;
        if (mark >= 80) return 7;
        if (mark >= 70) return 6;
        if (mark >= 60) return 5;
        if (mark >= 50) return 4;
        if (mark >= 40) return 3;
        if (mark >= 30) return 2;
        return 1;
    }

    /**
     * Additional Points for Mathematics (Pure Maths):
     * 70 - 100%: +2
     * 40 - 69%:  +1
     * 0 - 39%:    0
     */
    public static int getMathematicsBonus(int mark) {
        if (mark >= 70) return 2;
        if (mark >= 40) return 1;
        return 0;
    }

    /**
     * Additional Points for Language taken as Home Language (HL):
     * 70 - 100%: +2
     * 40 - 69%:  +1
     * 0 - 39%:    0
     */
    public static int getLanguageBonus(int mark) {
        if (mark >= 70) return 2;
        if (mark >= 40) return 1;
        return 0;
    }

    /**
     * Points for Life Orientation:
     * 90 - 100%: 4 points
     * 80 - 89%:  3 points
     * 70 - 79%:  2 points
     * 60 - 69%:  1 point
     * Below 60%: 0 points
     */
    public static int getLifeOrientationPoints(int mark) {
        if (mark >= 90) return 4;
        if (mark >= 80) return 3;
        if (mark >= 70) return 2;
        if (mark >= 60) return 1;
        return 0;
    }

    /**
     * Calculates complete SPU APS Result from a list of subjects according to the 2026 prospectus.
     */
    public static ApsResult calculate(List<SubjectEntry> subjects, boolean isEnglishHL, int englishMark) {
        ApsResult result = new ApsResult();
        int total = 0;
        int baseSum = 0;
        int mathBonusSum = 0;
        int langBonusSum = 0;
        int loSum = 0;

        for (SubjectEntry sub : subjects) {
            SubjectResult sr = new SubjectResult();
            sr.name = sub.getSubjectName();
            sr.mark = sub.getMark();
            sr.level = sub.getAchievementLevel();

            if (sub.isLifeOrientation()) {
                int loPts = getLifeOrientationPoints(sub.getMark());
                sr.points = loPts;
                sr.bonus = 0;
                loSum += loPts;
                total += loPts;
            } else {
                int base = getStandardPoints(sub.getMark());
                int bonus = 0;
                if (sub.isPureMaths()) {
                    int mBonus = getMathematicsBonus(sub.getMark());
                    bonus += mBonus;
                    mathBonusSum += mBonus;
                }
                if (sub.isHomeLanguage()) {
                    int lBonus = getLanguageBonus(sub.getMark());
                    bonus += lBonus;
                    langBonusSum += lBonus;
                }
                sr.points = base + bonus;
                sr.bonus = bonus;
                baseSum += base;
                total += (base + bonus);
            }
            result.subjects.add(sr);
        }

        result.totalAps = total;
        result.basePoints = baseSum;
        result.mathBonusPoints = mathBonusSum;
        result.languageBonusPoints = langBonusSum;
        result.lifeOrientationPoints = loSum;

        // Verify English general requirement (Prospectus Page 4):
        // English HL: Level 4 (50%+) OR English FAL: Level 5 (60%+)
        if (isEnglishHL) {
            result.englishRequirementMet = (englishMark >= 50);
            result.englishFeedback = result.englishRequirementMet ?
                    "English HL Level " + (englishMark >= 80 ? "7" : englishMark >= 70 ? "6" : englishMark >= 60 ? "5" : "4") + " meets SPU English minimum (Level 4+)" :
                    "English HL mark (" + englishMark + "%) is below SPU minimum Level 4 (50%)";
        } else {
            result.englishRequirementMet = (englishMark >= 60);
            result.englishFeedback = result.englishRequirementMet ?
                    "English FAL Level " + (englishMark >= 80 ? "7" : englishMark >= 70 ? "6" : "5") + " meets SPU English minimum (Level 5+)" :
                    "English FAL mark (" + englishMark + "%) is below SPU minimum Level 5 (60%)";
        }

        return result;
    }
}
