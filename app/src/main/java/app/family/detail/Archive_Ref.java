package app.family.detail;

import android.content.Context;
import android.content.Intent;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.folg.gedcom.model.Repository;
import org.folg.gedcom.model.RepositoryRef;
import org.folg.gedcom.model.Source;
import app.family.Detail;
import app.family.Memory;
import app.family.R;
import app.family.U;
import static app.family.Global.gc;

public class Archive_Ref extends Detail {

	RepositoryRef r;

	@Override
	public void impagina() {
		mettiBava( "REPO" );
		r = (RepositoryRef) casta( RepositoryRef.class );
		if( r.getRepository(gc) != null ) {  // valido
			setTitle( R.string.repository_citation );
			View cartaRepo = mettiArchivio(box,r.getRepository(gc));
			cartaRepo.setTag( R.id.tag_oggetto, r.getRepository(gc) );	// per il menu contestuale todo ancora necessario?
			registerForContextMenu( cartaRepo );
		} else if( r.getRef() != null ) {  // di un archivio inesistente (magari eliminato)
			setTitle( R.string.inexistent_repository_citation );
		} else { // senza ref??
			setTitle( R.string.repository_note );
		}
		metti( getString(R.string.value), "Value", false, true );
		metti( getString(R.string.call_number), "CallNumber" );
		metti( getString(R.string.media_type), "MediaType" );
		mettiEstensioni( r );
		U.mettiNote( box, r, true );
	}

	public static View mettiArchivio( LinearLayout scatola, final Repository repo ) {
		final Context contesto = scatola.getContext();
		View cartaRepo = LayoutInflater.from(contesto).inflate( R.layout.piece_source, scatola, false );
		scatola.addView( cartaRepo );
		((TextView) cartaRepo.findViewById( R.id.fonte_testo ) ).setText( repo.getName() );
		((CardView) cartaRepo).setCardBackgroundColor( contesto.getResources().getColor(R.color.archivio) );
		cartaRepo.setOnClickListener( v -> {
			Memory.setPrimo( repo );
			contesto.startActivity( new Intent( contesto, Archive.class ) );
		});
		return cartaRepo;
	}

	@Override
	public void elimina() {
		// Elimina la citazione all'archivio a aggiorna la data della source che la conteneva
		Source contenitore = (Source) Memory.oggettoContenitore();
		contenitore.setRepositoryRef( null );
		U.aggiornaDate( contenitore );
		Memory.annullaIstanze(r);
	}
}