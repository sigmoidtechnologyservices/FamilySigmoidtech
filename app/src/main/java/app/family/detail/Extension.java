package app.family.detail;

import org.folg.gedcom.model.GedcomTag;
import app.family.Detail;
import app.family.Memory;
import app.family.R;
import app.family.U;

public class Extension extends Detail {

	GedcomTag e;

	@Override
	public void impagina() {
		setTitle( getString( R.string.extension ) );
		e = (GedcomTag) casta( GedcomTag.class );
		mettiBava( e.getTag() );
		metti( getString(R.string.id), "Id", false, false );
		metti( getString(R.string.value), "Value", true, true );
		metti( "Ref", "Ref", false, false );
		metti( "ParentTagName", "ParentTagName", false, false ); // non ho capito se viene usato o no
		for( GedcomTag figlio : e.getChildren() ) {
			String testo = U.scavaEstensione(figlio,0);
			if( testo.endsWith("\n") )
				testo = testo.substring( 0, testo.length()-1 );
			creaPezzo( figlio.getTag(), testo, figlio, true );
		}
	}

	@Override
	public void elimina() {
		U.eliminaEstensione( e, Memory.oggettoContenitore(), null );
		U.aggiornaDate( Memory.oggettoCapo() );
	}
}
