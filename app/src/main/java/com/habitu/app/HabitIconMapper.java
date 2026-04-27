package com.habitu.app;

public class HabitIconMapper {

    public static int getIcon(String habit) {
        if (habit == null) return R.drawable.ic_sprout;
        String h = habit.toLowerCase();
        if (h.contains("run"))                              return R.drawable.ic_run;
        if (h.contains("stud"))                            return R.drawable.ic_study;
        if (h.contains("gym"))                             return R.drawable.ic_gym;
        if (h.contains("meditat") || h.contains("well")
                || h.contains("spa") || h.contains("yoga")) return R.drawable.ic_meditation;
        if (h.contains("nutrit") || h.contains("eat")
                || h.contains("food"))                     return R.drawable.ic_nutrition;
        if (h.contains("water") || h.contains("hydrat"))  return R.drawable.ic_water;
        if (h.contains("read"))                            return R.drawable.ic_reading;
        if (h.contains("sleep"))                           return R.drawable.ic_sleep;
        return R.drawable.ic_sprout;
    }
}
