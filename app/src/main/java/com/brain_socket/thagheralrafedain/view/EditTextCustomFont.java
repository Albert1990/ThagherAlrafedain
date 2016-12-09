package com.brain_socket.thagheralrafedain.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;

import com.brain_socket.thagheralrafedain.R;


public class EditTextCustomFont extends EditText
{
		
	public EditTextCustomFont(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}
	public EditTextCustomFont(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}
	
	private void init(AttributeSet attrs) 
	{ 
		try {
			if(!isInEditMode()) {
				// get the typed array for the custom attrs
			    TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomFontTextView);
			    // get fontId set in the XML
			    int fontId = a.getInteger(R.styleable.CustomFontTextView_fontId, 0);
			    // check fontId if equal to any or the predefined ids for the custom fonts
			    switch (fontId) {
				case 1:
					this.setTypeface(TextViewCustomFont.getTFRegular(getContext()));
					break;	
				case 2:
					this.setTypeface(TextViewCustomFont.getTFBold(getContext()));
					break;	
				case 3:
				}
			    //Don't forget this
			    a.recycle();
			}
		}
		catch (Exception ignored) {}
	}
	
	
}
