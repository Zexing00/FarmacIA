package com.example.farmacia.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.farmacia.dao.PillboxDAO;
import com.example.farmacia.model.Medication;
import com.example.farmacia.receiver.AlarmReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmScheduler {

    public static void scheduleAllAlarmsForUser(Context context, int userId, String userName) {
        PillboxDAO pillboxDAO = new PillboxDAO(context);
        pillboxDAO.open();
        List<Medication> medications = pillboxDAO.getMedicationsByUserId(userId);
        pillboxDAO.close();

        for (Medication med : medications) {
            if (med.getWeeklyDose() != null && !med.getWeeklyDose().isEmpty()) {
                scheduleAlarmsForMedication(context, med, userName);
            }
        }
    }

    public static void cancelAllAlarmsForUser(Context context, int userId) {
        PillboxDAO pillboxDAO = new PillboxDAO(context);
        pillboxDAO.open();
        List<Medication> medications = pillboxDAO.getMedicationsByUserId(userId);
        pillboxDAO.close();

        for (Medication med : medications) {
            if (med.getWeeklyDose() != null && !med.getWeeklyDose().isEmpty()) {
                cancelAlarmsForMedication(context, med);
            }
        }
    }

    private static void scheduleAlarmsForMedication(Context context, Medication medication, String userName) {
        String dose = medication.getWeeklyDose();
        String[] parts = dose.split(" a las ");
        if (parts.length != 2) return;

        String frequencyLabel = parts[0];
        String[] hours = parts[1].split(",");

        ArrayList<Integer> calendarDays = getCalendarDays(frequencyLabel);

        for (String hourStr : hours) {
            String trimmedHourStr = hourStr.trim();
            String[] time = trimmedHourStr.split(":");
            if (time.length != 2) continue;

            try {
                int hour = Integer.parseInt(time[0]);
                int minute = Integer.parseInt(time[1]);

                if (calendarDays == null) { // Everyday
                    scheduleSingleAlarm(context, medication.getName(), userName, -1, hour, minute);
                } else { // Specific days
                    for (Integer dayOfWeek : calendarDays) {
                        scheduleSingleAlarm(context, medication.getName(), userName, dayOfWeek, hour, minute);
                    }
                }
            } catch (NumberFormatException e) { /* Ignore parsing errors */ }
        }
    }

    private static void cancelAlarmsForMedication(Context context, Medication medication) {
        String dose = medication.getWeeklyDose();
        String[] parts = dose.split(" a las ");
        if (parts.length != 2) return;

        String frequencyLabel = parts[0];
        String[] hours = parts[1].split(",");

        ArrayList<Integer> calendarDays = getCalendarDays(frequencyLabel);

        for (String hourStr : hours) {
            String trimmedHourStr = hourStr.trim();
            String[] time = trimmedHourStr.split(":");
            if (time.length != 2) continue;

            try {
                int hour = Integer.parseInt(time[0]);
                int minute = Integer.parseInt(time[1]);

                if (calendarDays == null) { // Everyday
                    cancelSingleAlarm(context, medication.getName(), -1, hour, minute);
                } else { // Specific days
                    for (Integer dayOfWeek : calendarDays) {
                        cancelSingleAlarm(context, medication.getName(), dayOfWeek, hour, minute);
                    }
                }
            } catch (NumberFormatException e) { /* Ignore parsing errors */ }
        }
    }

    private static ArrayList<Integer> getCalendarDays(String frequencyLabel) {
        if ("Todos los días".equals(frequencyLabel)) {
            return null;
        }
        ArrayList<Integer> calendarDays = new ArrayList<>();
        String[] daysAbbr = frequencyLabel.split(",");
        for (String day : daysAbbr) {
            switch (day.trim()) {
                case "Lu": calendarDays.add(Calendar.MONDAY); break;
                case "Ma": calendarDays.add(Calendar.TUESDAY); break;
                case "Mi": calendarDays.add(Calendar.WEDNESDAY); break;
                case "Ju": calendarDays.add(Calendar.THURSDAY); break;
                case "Vi": calendarDays.add(Calendar.FRIDAY); break;
                case "Sa": calendarDays.add(Calendar.SATURDAY); break;
                case "Do": calendarDays.add(Calendar.SUNDAY); break;
            }
        }
        return calendarDays;
    }

    private static void scheduleSingleAlarm(Context context, String medName, String userName, int dayOfWeek, int hour, int minute) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = createPendingIntent(context, medName, userName, dayOfWeek, hour, minute, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (dayOfWeek != -1) {
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        }

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            if (dayOfWeek == -1) calendar.add(Calendar.DAY_OF_YEAR, 1);
            else calendar.add(Calendar.DAY_OF_YEAR, 7);
        }

        long interval = (dayOfWeek == -1) ? AlarmManager.INTERVAL_DAY : AlarmManager.INTERVAL_DAY * 7;
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, pi);
    }

    private static void cancelSingleAlarm(Context context, String medName, int dayOfWeek, int hour, int minute) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = createPendingIntent(context, medName, "", dayOfWeek, hour, minute, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pi);
    }

    private static PendingIntent createPendingIntent(Context context, String medName, String userName, int dayOfWeek, int hour, int minute, int flags) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("MED_NAME", medName);
        intent.putExtra("USER_NAME", userName);

        int requestCode = (medName + dayOfWeek + hour + minute).hashCode();
        return PendingIntent.getBroadcast(context, requestCode, intent, flags);
    }
}
