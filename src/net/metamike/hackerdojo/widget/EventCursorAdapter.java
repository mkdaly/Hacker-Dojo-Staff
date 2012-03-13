package net.metamike.hackerdojo.widget;

import java.text.DateFormat;

import net.metamike.hackerdojo.widget.Event.EventStatus;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class EventCursorAdapter extends CursorAdapter {
	LayoutInflater inflater;

	public EventCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public EventCursorAdapter(Context context, Cursor c) {
		super(context, c);
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder)view.getTag();
		
		DateFormat df = android.text.format.DateFormat.getMediumDateFormat(context);
		//TODO: Show time in addition to the date
		//DateFormat tf = android.text.format.DateFormat.getTimeFormat(context);
		Event e = EventDBAdapter.getEventObjectFromCursor(cursor);
		SpannableStringBuilder ssb = new SpannableStringBuilder();
		ssb.append(e.getName()).append(" in ").append(e.getRoom());
		switch (e.getStatus()) {
			case PAST:
				ssb.setSpan(new ForegroundColorSpan(holder.nameView.getCurrentTextColor() & 0x80FFFFFF), 0, ssb.length(), 0);
				break;
			case CANCELED:
				ssb.setSpan(new StrikethroughSpan(), 0, ssb.length(), 0);
				break;
			
		}
		holder.nameView.setText(ssb);
		holder.timeView.setText(df.format(e.getStart().toMillis(false)) + " to " + df.format(e.getEnd().toMillis(false)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = inflater.inflate(R.layout.event, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.nameView = (TextView)v.findViewById(R.id.nameTextView);
		holder.timeView = (TextView)v.findViewById(R.id.timeTextView);
		v.setTag(holder);
		return v;
	}
	
	/* From the EfficientAdapter API demo */
	static class ViewHolder {
		TextView nameView;
		TextView timeView;
	}
}
