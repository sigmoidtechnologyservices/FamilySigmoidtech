package app.family.detail;

import android.view.Menu;
import org.folg.gedcom.model.Change;
import org.folg.gedcom.model.DateTime;
import app.family.Detail;
import app.family.R;
import app.family.U;

public class Changes extends Detail {

	Change c;

	@Override
	public void impagina() {
		setTitle(R.string.change_date);
		mettiBava("CHAN");
		c = (Change)casta(Change.class);
		DateTime dateTime = c.getDateTime();
		if( dateTime != null ) {
			if( dateTime.getValue() != null )
				U.metti(box, getString(R.string.value), dateTime.getValue());
			if( dateTime.getTime() != null )
				U.metti(box, getString(R.string.time), dateTime.getTime());
		}
		mettiEstensioni(c);
		U.mettiNote(box, c, true);
	}

	//There is no need for a menu here
	@Override
	public boolean onCreateOptionsMenu( Menu m ) {
		return false;
	}
}
