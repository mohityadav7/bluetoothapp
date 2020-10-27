package com.example.bluetoothapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class PairedDevicePreference extends Preference {

    private PairedDevicePreferenceListener listener = null;

    public void setOnClickListener(PairedDevicePreferenceListener listener) {
        this.listener = listener;
    }

    public interface PairedDevicePreferenceListener {
        void onWidgetClick(View view);

        void onPreferenceClick(View view);
    }

    public PairedDevicePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.paired_device_preference_layout);
        setWidgetLayoutResource(R.layout.preference_widget_layout);
    }

    public PairedDevicePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.paired_device_preference_layout);
        setWidgetLayoutResource(R.layout.preference_widget_layout);
    }

    public PairedDevicePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.paired_device_preference_layout);
        setWidgetLayoutResource(R.layout.preference_widget_layout);
    }

    public PairedDevicePreference(Context context) {
        super(context);
        setLayoutResource(R.layout.paired_device_preference_layout);
        setWidgetLayoutResource(R.layout.preference_widget_layout);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setClickable(false);
        LinearLayout widgetFrame = findFrameLayoutWidget(holder.itemView);
        RelativeLayout preferenceLayout = findPreferenceLayout(holder.itemView);
        if (widgetFrame != null) {
            widgetFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onWidgetClick(view);
                    }
                }
            });
        }
        if (preferenceLayout != null) {
            preferenceLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onPreferenceClick(view);
                    }
                }
            });
        }
    }

    private LinearLayout findFrameLayoutWidget(View view) {
        if (view instanceof ViewGroup && ((ViewGroup) view).getChildCount() > 1) {
            return (LinearLayout) ((ViewGroup) view).getChildAt(2);
        }
        return null;
    }

    private RelativeLayout findPreferenceLayout(View view) {
        if (view instanceof ViewGroup && ((ViewGroup) view).getChildCount() > 0) {
            return (RelativeLayout) ((ViewGroup) view).getChildAt(1);
        }
        return null;
    }
}
