package app.family.detail;

import org.folg.gedcom.model.Submitter;
import app.family.Detail;
import app.family.Podium;
import app.family.R;
import app.family.U;

public class Author extends Detail {

	Submitter a;

	@Override
	public void impagina() {
		setTitle( R.string.submitter );
		a = (Submitter) casta( Submitter.class );
		mettiBava( "SUBM", a.getId() );
		metti( getString(R.string.value), "Value", false, true );   // Value?
		metti( getString(R.string.name), "Name" );
		metti( getString(R.string.address), a.getAddress() );
		metti( getString(R.string.www), "Www" );
		metti( getString(R.string.email), "Email" );
		metti( getString(R.string.telephone), "Phone" );
		metti( getString(R.string.fax), "Fax" );
		metti( getString(R.string.language), "Language" );
		metti( getString(R.string.rin), "Rin", false, false );
		mettiEstensioni( a );
		U.cambiamenti( box, a.getChange() );
	}

	@Override
	public void elimina() {
		// We remind you that at least one author must be specified
		// it does not update the date of any record
		Podium.eliminaAutore( a );
	}
}
