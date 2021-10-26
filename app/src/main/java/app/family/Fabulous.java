package app.family;

// Fumetto con un suggerimento che compare sopra al FAB

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Fabulous {

	private final View baloon;

	public Fabulous(Context contesto, int textId ) {
		this( contesto, contesto.getString( textId ) );
	}

	public Fabulous(Context contesto, String testo ) {
		Activity attivita = (Activity) contesto;
		baloon = attivita.getLayoutInflater().inflate( R.layout.fabulous, null );
		baloon.setVisibility( View.INVISIBLE );
		((LinearLayout)attivita.findViewById( R.id.fab_box )).addView( baloon, 0,
				new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT) );
		((TextView)baloon.findViewById( R.id.fabuloso_text )).setText( testo );
		baloon.setOnTouchListener( (vista, evento) -> {
			hide();
			return true;
		});
		attivita.findViewById( R.id.fab ).setOnTouchListener( (vista, evento) -> {
			hide();
			//vista.performClick();
			return false; // Per eseguire il click dopo
		});
	}

	public void show() {
		new Handler( Looper.myLooper()).postDelayed( () -> { // compare dopo un secondo
			baloon.setVisibility( View.VISIBLE );
		}, 1000);
	}

	public void hide() {
		baloon.setVisibility( View.INVISIBLE );
	}
}
