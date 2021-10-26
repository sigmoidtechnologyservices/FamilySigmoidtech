package app.family.detail;

import org.folg.gedcom.model.Note;
import org.folg.gedcom.model.SourceCitation;
import org.folg.gedcom.model.SourceCitationContainer;
import app.family.Detail;
import app.family.Memory;
import app.family.R;
import app.family.U;
import static app.family.Global.gc;

public class QuoteSource extends Detail {

	SourceCitation c;

	@Override
	public void impagina() {
		mettiBava( "SOUR" );
		c = (SourceCitation) casta( SourceCitation.class );
		if( c.getSource(gc) != null ) {  // source CITATION valida
			setTitle( R.string.source_citation );
			U.mettiFonte( box, c.getSource(gc), true );
		} else if( c.getRef() != null ) {  // source CITATION di una fonte inesistente (magari eliminata)
			setTitle( R.string.inexistent_source_citation );
		} else {	// source NOTE
			setTitle( R.string.source_note );
			metti( getString(R.string.value), "Value", true, true );
		}
		metti( getString(R.string.page), "Page", true, true );
		metti( getString(R.string.date), "Date" );
		metti( getString(R.string.text), "Text", true, true );	// vale sia per sourceNote che per sourceCitation
		//c.getTextOrValue();	praticamente inutile
		//if( c.getDataTagContents() != null )
		//	U.metti( box, "Data Tag Contents", c.getDataTagContents().toString() );	// COMBINED DATA TEXT
		metti( getString(R.string.certainty), "Quality" );	// un numero da 0 a 3
		//metti( "Ref", "Ref", false, false ); // l'id della fonte
		mettiEstensioni( c );
		U.mettiNote( box, c, true );
		U.mettiMedia( box, c, true );
	}

	@Override
	public void elimina() {
		Object contenitore = Memory.oggettoContenitore();
		if( contenitore instanceof Note )	// Note non extende SourceCitationContainer
			((Note)contenitore).getSourceCitations().remove( c );
		else
			((SourceCitationContainer)contenitore).getSourceCitations().remove( c );
		U.aggiornaDate( Memory.oggettoCapo() );
		Memory.annullaIstanze(c);
	}
}
