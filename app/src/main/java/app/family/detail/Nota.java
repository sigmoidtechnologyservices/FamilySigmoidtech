package app.family.detail;

import android.app.Activity;
import org.folg.gedcom.model.Note;
import app.family.Detail;
import app.family.Global;
import app.family.Memory;
import app.family.R;
import app.family.U;
import app.family.visit.References_Note;

public class Nota extends Detail {

	Note n;

	@Override
	public void impagina() {
		n = (Note)casta(Note.class);
		if( n.getId() == null ) {
			setTitle(R.string.note);
			mettiBava("NOTE");
		} else {
			setTitle(R.string.shared_note);
			mettiBava("NOTE", n.getId());
		}
		metti(getString(R.string.text), "Value", true, true);
		metti(getString(R.string.rin), "Rin", false, false);
		mettiEstensioni(n);
		U.citaFonti(box, n);
		U.cambiamenti(box, n.getChange());
		if( n.getId() != null ) {
			References_Note rifNota = new References_Note(Global.gc, n.getId(), false);
			if( rifNota.tot > 0 )
				U.mettiDispensa(box, rifNota.capostipiti.toArray(), R.string.shared_by);
		} else if( ((Activity)box.getContext()).getIntent().getBooleanExtra("daQuaderno", false) ) {
			U.mettiDispensa(box, Memory.oggettoCapo(), R.string.written_in);
		}
	}

	@Override
	public void elimina() {
		U.aggiornaDate(U.eliminaNota(n, null));
	}
}
