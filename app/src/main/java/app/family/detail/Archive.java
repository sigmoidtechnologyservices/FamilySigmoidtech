package app.family.detail;

import org.folg.gedcom.model.Repository;
import org.folg.gedcom.model.Source;
import java.util.ArrayList;
import java.util.List;
import app.family.Detail;
import app.family.Global;
import app.family.Warehouse;
import app.family.R;
import app.family.U;

public class Archive extends Detail {

	Repository a;

	@Override
	public void impagina() {
		setTitle( R.string.repository );
		a = (Repository) casta( Repository.class );
		mettiBava( "REPO", a.getId() );
		metti( getString(R.string.value), "Value", false, true );	// Not very standard Gedcom
		metti( getString(R.string.name), "Name" );
		metti( getString(R.string.address), a.getAddress() );
		metti( getString(R.string.www), "Www" );
		metti( getString(R.string.email), "Email" );
		metti( getString(R.string.telephone), "Phone" );
		metti( getString(R.string.fax), "Fax" );
		metti( getString(R.string.rin), "Rin", false, false );
		mettiEstensioni( a );
		U.mettiNote( box, a, true );
		U.cambiamenti( box, a.getChange() );

		// Collects and displays the sources citing this Repository
		List<Source> fontiCitanti = new ArrayList<>();
		for( Source source : Global.gc.getSources() )
			if( source.getRepositoryRef() != null && source.getRepositoryRef().getRef() != null
					&& source.getRepositoryRef().getRef().equals(a.getId()) )
				fontiCitanti.add( source );
		if( !fontiCitanti.isEmpty() )
			U.mettiDispensa( box, fontiCitanti.toArray(), R.string.sources );
		a.putExtension( "fonti", fontiCitanti.size() );
	}

	@Override
	public void elimina() {
		U.aggiornaDate( (Object[]) Warehouse.elimina( a ) );
	}
}
