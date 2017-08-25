package com.wilddog.conversation.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;


import com.wilddog.conversation.R;
import com.wilddog.conversation.fragments.OnlineFragment;
import com.wilddog.conversation.utils.CommonUtil;

public class SideBar extends View {
	private char[] l;
	private SectionIndexer sectionIndexter = null;
	private ListView list;
	private TextView mDialogText;
	private int choose = -1;
	private int m_nItemHeight = CommonUtil.dipToPixel(getContext(), 15);

	public SideBar(Context context) {
		super(context);
		init();
	}

	public SideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		l = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
				'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
				'W', 'X', 'Y', 'Z', '#' };
	}

	public SideBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void setListView(ListView _list) {
		list = _list;
	}

	public void setTextView(TextView mDialogText) {
		this.mDialogText = mDialogText;
	}

	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		int i = (int) event.getY();
		int idx = i / m_nItemHeight;
		if (idx >= l.length) {
			idx = l.length - 1;
		} else if (idx < 0) {
			idx = 0;
		}
		if (event.getAction() == MotionEvent.ACTION_DOWN
				|| event.getAction() == MotionEvent.ACTION_MOVE) {
			choose = idx;
			invalidate();
			if (sectionIndexter == null) {
				OnlineFragment.MyAdapter ha = (OnlineFragment.MyAdapter) list
						.getAdapter();
				sectionIndexter = (SectionIndexer) ha;
			}
			int position = sectionIndexter.getPositionForSection(l[idx]);
			if (position == -1) {
				return true;
			}
			list.setSelection(position);
		}

		return true;
	}
	Paint paint = new Paint();
	protected void onDraw(Canvas canvas) {
		for (int i = 0; i < l.length; i++) {
			float widthCenter = getMeasuredWidth() / 2;
			if (i == choose) {
				resetCirclePaint();
				canvas.drawCircle(widthCenter, m_nItemHeight
						+ (i * m_nItemHeight)-8,16,paint);
				resetSelectTextPaint();
				canvas.drawText(String.valueOf(l[i]), widthCenter, m_nItemHeight
						+ (i * m_nItemHeight), paint);

			}else {
				resetTextPaint();
				canvas.drawText(String.valueOf(l[i]), widthCenter, m_nItemHeight
						+ (i * m_nItemHeight), paint);
			}
			paint.reset();
		}
		super.onDraw(canvas);
	}

    private void resetTextPaint(){
		paint.setColor(getResources().getColor(R.color.text_color));
		paint.setTextSize(CommonUtil.dipToPixel(getContext(), 12));
		Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
		paint.setTypeface(font);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setTextAlign(Paint.Align.CENTER);
	}

	private void  resetCirclePaint(){
		paint.setColor(getResources().getColor(R.color.btn_orange_clicked));
/*		paint.setTextSize(CommonUtil.dipToPixel(getContext(), 12));
		Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
		paint.setTypeface(font);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setTextAlign(Paint.Align.CENTER);*/
	}
	private void  resetSelectTextPaint(){
		paint.setColor(Color.WHITE);
		paint.setTextSize(CommonUtil.dipToPixel(getContext(), 12));
		Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
		paint.setTypeface(font);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setTextAlign(Paint.Align.CENTER);
	}

}
