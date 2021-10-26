//RecyclerView adapter with media list

package app.family;

import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.folg.gedcom.model.Media;
import org.folg.gedcom.model.Person;
import java.util.List;
import app.family.detail.Immagine;
import app.family.visit.ListaMediaContenitore;
import app.family.visit.FindPila;

class AdapterGalleryMedia extends RecyclerView.Adapter<AdapterGalleryMedia.gestoreVistaMedia> {

	private List<ListaMediaContenitore.MedCont> listaMedia;
	private boolean dettagli;

	AdapterGalleryMedia(List<ListaMediaContenitore.MedCont> listaMedia, boolean dettagli ) {
		this.listaMedia = listaMedia;
		this.dettagli = dettagli;
	}

	@Override
	public gestoreVistaMedia onCreateViewHolder( ViewGroup parent, int tipo ) {
		View vista = LayoutInflater.from(parent.getContext()).inflate( R.layout.average_piece, parent, false );
		return new gestoreVistaMedia( vista, dettagli );
	}
	@Override
	public void onBindViewHolder( final gestoreVistaMedia gestore, int posizione ) {
		gestore.setta( posizione );
	}
	@Override
	public int getItemCount() {
		return listaMedia.size();
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public int getItemViewType(int position) {
		return position;
	}

	class gestoreVistaMedia extends RecyclerView.ViewHolder implements View.OnClickListener {
		View vista;
		boolean dettagli;
		Media media;
		Object contenitore;
		ImageView vistaImmagine;
		TextView vistaTesto;
		TextView vistaNumero;
		gestoreVistaMedia( View vista, boolean dettagli ) {
			super(vista);
			this.vista = vista;
			this.dettagli = dettagli;
			vistaImmagine = vista.findViewById( R.id.media_img );
			vistaTesto = vista.findViewById( R.id.media_testo );
			vistaNumero = vista.findViewById( R.id.media_num );
		}
		void setta( int posizione ) {
			media = listaMedia.get( posizione ).media;
			contenitore = listaMedia.get( posizione ).contenitore;
			if( dettagli ) {
				arredaMedia( media, vistaTesto, vistaNumero );
				vista.setOnClickListener( this );
				((Activity)vista.getContext()).registerForContextMenu( vista );
				vista.setTag( R.id.tag_oggetto, media );
				vista.setTag( R.id.tag_contenitore, contenitore );
				// Register context menu
				final AppCompatActivity attiva = (AppCompatActivity) vista.getContext();
				if( vista.getContext() instanceof Individual) { // Fragment individuoMedia
					attiva.getSupportFragmentManager()
							.findFragmentByTag( "android:switcher:" + R.id.schede_persona + ":0" )	// non garantito in futuro
							.registerForContextMenu( vista );
				} else if( vista.getContext() instanceof Principal ) // Fragment Galleria
					attiva.getSupportFragmentManager().findFragmentById( R.id.contenitore_fragment ).registerForContextMenu( vista );
				else	// in the AppCompatActivity
					attiva.registerForContextMenu( vista );
			} else {
				RecyclerView.LayoutParams parami = new RecyclerView.LayoutParams( RecyclerView.LayoutParams.WRAP_CONTENT, U.dpToPx(110) );
				int margin = U.dpToPx(5);
				parami.setMargins( margin, margin, margin, margin );
				vista.setLayoutParams( parami );
				vistaTesto.setVisibility( View.GONE );
				vistaNumero.setVisibility( View.GONE );
			}
			F.dipingiMedia( media, vistaImmagine, vista.findViewById(R.id.media_circolo) );
		}
		@Override
		public void onClick( View v ) {
			AppCompatActivity attiva = (AppCompatActivity) v.getContext();
			// Gallery in media object choice mode
// Return the id of a media object to MediaIndividual
			if( attiva.getIntent().getBooleanExtra( "galleriaScegliMedia", false ) ) {
				Intent intent = new Intent();
				intent.putExtra( "idMedia", media.getId() );
				attiva.setResult( Activity.RESULT_OK, intent );
				attiva.finish();
			//Gallery in normal mode opens Image
			} else {
				Intent intento = new Intent( v.getContext(), Immagine.class );
				if( media.getId() != null ) { // all Media records
					Memory.setPrimo( media );
				} else if( (attiva instanceof Individual && contenitore instanceof Person) // media di primo livello nell'Indi
						|| attiva instanceof Detail) { // normal opening in the Details
					Memory.aggiungi( media );
				} else { //from Galleria all simple media, or from IndividuoMedia the media under multiple levels
					new FindPila( Global.gc, media );
					if( attiva instanceof Principal ) // Only in the Gallery
						intento.putExtra( "daSolo", true ); // so then Image shows the pantry
				}
				v.getContext().startActivity( intento );
			}
		}
	}

	static void arredaMedia( Media media, TextView vistaTesto, TextView vistaNumero ) {
		String testo = "";
		if( media.getTitle() != null )
			testo = media.getTitle() + "\n";
		if( Global.settings.expert && media.getFile() != null ) {
			String file = media.getFile();
			file = file.replace( '\\', '/' );
			if( file.lastIndexOf('/') > -1 ) {
				if( file.length() > 1 && file.endsWith("/") ) // removes the last bar
					file = file.substring( 0, file.length()-1 );
				file = file.substring( file.lastIndexOf('/') + 1 );
			}
			testo += file;
		}
		if( testo.isEmpty() )
			vistaTesto.setVisibility( View.GONE );
		else {
			if( testo.endsWith("\n") )
				testo = testo.substring( 0, testo.length()-1 );
			vistaTesto.setText( testo );
		}
		if( media.getId() != null ) {
			vistaNumero.setText( String.valueOf(Gallery.popolarita(media)) );
			vistaNumero.setVisibility( View.VISIBLE );
		} else
			vistaNumero.setVisibility( View.GONE );
	}

	// This is only used to create a RecyclerView with media icons that is transparent to clicks
	// todo però impedisce lo scroll in Dettaglio
	static class RiciclaVista extends RecyclerView {
		boolean dettagli;
		public RiciclaVista( Context context, boolean dettagli) {
			super(context);
			this.dettagli = dettagli;
		}
		@Override
		public boolean onTouchEvent( MotionEvent e ) {
			super.onTouchEvent( e );
			return dettagli; //when it is false the grid does not intercept the click
		}
	}
}