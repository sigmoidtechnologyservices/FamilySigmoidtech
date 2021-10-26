package app.family.detail;

import org.folg.gedcom.model.Address;
import app.family.Detail;
import app.family.Memory;
import app.family.R;
import app.family.U;

public class Indirizzo extends Detail {

	Address a;

	@Override
	public void impagina() {
		setTitle( R.string.address );
		mettiBava( "ADDR" );
		a = (Address) casta( Address.class );
		metti( getString(R.string.value), "Value", false, true );	// Fortemente deprecato in favore dell'indirizzo frammentato
		metti( getString(R.string.name), "Name", false, false );	// _name non standard
		metti( getString(R.string.line_1), "AddressLine1" );
		metti( getString(R.string.line_2), "AddressLine2" );
		metti( getString(R.string.line_3), "AddressLine3" );
		metti( getString(R.string.postal_code), "PostalCode" );
		metti( getString(R.string.city), "City" );
		metti( getString(R.string.state), "State" );
		metti( getString(R.string.country), "Country" );
		mettiEstensioni( a );
	}

	@Override
	public void elimina() {
		eliminaIndirizzo( Memory.oggettoContenitore() );
		U.aggiornaDate( Memory.oggettoCapo() );
		Memory.annullaIstanze(a);
	}
}
