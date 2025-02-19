package com.example.styletimeandroidapp.utils;

import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.HashSet;
import java.util.Set;

/**
 * Decorator for highlighting booked appointments in the calendar.
 */
public class DayViewDecoratorImpl implements DayViewDecorator {

    private final int color;
    private final HashSet<CalendarDay> dates;

    public DayViewDecoratorImpl(int color, Set<CalendarDay> dates) {
        this.color = color;
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(color)); // Changes text color
    }
}
