package com.almende.demo.conferenceApp;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class KnownNamesDialog extends DialogPreference {
	private View myView=null;
	public KnownNamesDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
		 setDialogLayoutResource(R.layout.add_knownnames_conference_app);
	}
	@Override
	public void onClick(DialogInterface dialog, int which) {
		System.err.println("Dialog clicked:"+which);
		if (which == DialogInterface.BUTTON_POSITIVE){
			System.err.println("is ok button1");
			String name = ((EditText)myView.findViewById(R.id.knownName)).getText().toString();
			String reason = ((EditText)myView.findViewById(R.id.reason)).getText().toString();
			EveService.myAgent.addKnownName(name,reason);
		}
		super.onClick(dialog, which);
	}
	@Override
    public void onBindDialogView(View view){
        this.myView=view;
    }
}
