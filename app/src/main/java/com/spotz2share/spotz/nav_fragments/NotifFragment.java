package com.spotz2share.spotz.nav_fragments;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.spotz2share.spotz.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by spotzdevelopment on 12/8/2017.
 */

public class NotifFragment extends Fragment implements WeekView.EventClickListener, MonthLoader.MonthChangeListener{


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notif, container, false);

        // Get a reference for the week view in the layout.
        WeekView mWeekView = (WeekView)view.findViewById(R.id.weekView);
// Set an action when any event is clicked.
        mWeekView.setOnEventClickListener(this);

// The week view has infinite scrolling horizontally. We have to provide the events of a
// month every time the month changes on the week view.
        //mWeekView.setMonthChangeListener(mMonthChangeListener);

// Set long press listener for events.
        //mWeekView.setEventLongPressListener(mEventLongPressListene);

        mWeekView.setMonthChangeListener(this);
        return view;
    }


    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(getActivity(), "Clicked " + event.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        // Populate the week view with some events.
        List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth-1);
        startTime.set(Calendar.YEAR, newYear);
        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR, 1);
        endTime.set(Calendar.MONTH, newMonth-1);
        WeekViewEvent event = new WeekViewEvent(1, "test", startTime, endTime);
        event.setColor(getResources().getColor(R.color.colorPrimary));
        events.add(event);



        return events;
    }
}
