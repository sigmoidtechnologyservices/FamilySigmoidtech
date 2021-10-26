// Lista delle Fonti (Sources)
// A differenza di Chiesa utilizza un adapter per il RecyclerView

package app.family;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import org.folg.gedcom.model.EventFact;
import org.folg.gedcom.model.Family;
import org.folg.gedcom.model.Name;
import org.folg.gedcom.model.Note;
import org.folg.gedcom.model.NoteContainer;
import org.folg.gedcom.model.Person;
import org.folg.gedcom.model.SourceCitation;
import org.folg.gedcom.model.SourceCitationContainer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import app.family.detail.Source;
import app.family.visit.ListQuotesSource;
import static app.family.Global.gc;

public class Library extends Fragment {

	private List<org.folg.gedcom.model.Source> listaFonti;
	private BibliotecAdapter adattatore;
	private int ordine;

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle bandolo ) {
		listaFonti = gc.getSources();
		((AppCompatActivity) getActivity()).getSupportActionBar().setTitle( listaFonti.size() + " " +
				getString(listaFonti.size()==1 ? R.string.source : R.string.sources).toLowerCase() );
		if( listaFonti.size() > 1 )
			setHasOptionsMenu(true);
		View vista = inflater.inflate(R.layout.recycle_view, container, false);
		RecyclerView vistaFonti = vista.findViewById( R.id.riciclatore );
		adattatore = new BibliotecAdapter();
		vistaFonti.setAdapter( adattatore );
		vista.findViewById( R.id.fab ).setOnClickListener( v -> nuovaFonte( getContext(), null ) );
		return vista;
	}

	public class BibliotecAdapter extends RecyclerView.Adapter<GestoreFonte> implements Filterable {
		@Override
		public GestoreFonte onCreateViewHolder( ViewGroup parent, int tipo ) {
			View vistaFonte = LayoutInflater.from( parent.getContext() )
					.inflate(R.layout.library_piece, parent, false);
			registerForContextMenu( vistaFonte );
			return new GestoreFonte( vistaFonte );
		}
		@Override
		public void onBindViewHolder( GestoreFonte gestore, int posizione ) {
			org.folg.gedcom.model.Source source = listaFonti.get(posizione);
			gestore.vistaId.setText( source.getId() );
			gestore.vistaId.setVisibility( ordine == 1 || ordine == 2 ? View.VISIBLE : View.GONE  );
			gestore.vistaTitolo.setText( titoloFonte(source) );
			Object volte = source.getExtension("citaz");
			// Conta delle citazioni con il mio metodo
			if( volte == null ) {
				volte = quanteCitazioni(source);
				source.putExtension("citaz", volte );
			}
			gestore.vistaVolte.setText( String.valueOf(volte) );
		}
		// Filtra i titoli delle fonti in base alle parole cercate
		@Override
		public Filter getFilter() {
			return new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence charSequence) {
					String query = charSequence.toString();
					if (query.isEmpty()) {
						listaFonti = gc.getSources();
					} else {
						List<org.folg.gedcom.model.Source> filteredList = new ArrayList<>();
						for (org.folg.gedcom.model.Source font : gc.getSources()) {
							if( titoloFonte(font).toLowerCase().contains(query.toLowerCase()) ) {
								filteredList.add(font);
							}
						}
						listaFonti = filteredList;
					}
					ordinaFonti(); // Riducendo la query riordina quelli che appaiono
					FilterResults filterResults = new FilterResults();
					filterResults.values = listaFonti;
					return filterResults;
				}
				@Override
				protected void publishResults(CharSequence cs, FilterResults fr) {
					notifyDataSetChanged();
				}
			};
		}
		@Override
		public int getItemCount() {
			return listaFonti.size();
		}
	}

	class GestoreFonte extends RecyclerView.ViewHolder implements View.OnClickListener {
		TextView vistaId;
		TextView vistaTitolo;
		TextView vistaVolte;
		GestoreFonte( View vista ) {
			super( vista );
			vistaId = vista.findViewById( R.id.biblioteca_id );
			vistaTitolo = vista.findViewById( R.id.biblioteca_titolo );
			vistaVolte = vista.findViewById( R.id.biblioteca_volte );
			vista.setOnClickListener(this);
		}
		@Override
		public void onClick( View v ) {
			// Restituisce l'id di una source a Individuo e Dettaglio
			if( getActivity().getIntent().getBooleanExtra("bibliotecaScegliFonte",false) ) {
				Intent intent = new Intent();
				intent.putExtra("idFonte", vistaId.getText().toString() );
				getActivity().setResult( Activity.RESULT_OK, intent );
				getActivity().finish();
			} else {
				org.folg.gedcom.model.Source source = gc.getSource( vistaId.getText().toString() );
				Memory.setPrimo(source);
				startActivity( new Intent( getContext(), Source.class ) );
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().getIntent().removeExtra("bibliotecaScegliFonte");
	}

	// Mette in ordine le fonti secondo uno dei criteri
	// L'ordine poi diventa permanente nel Json
	private void ordinaFonti() {
		if( ordine > 0 ) {
			if( ordine == 5 || ordine == 6 ) {
				for( org.folg.gedcom.model.Source source : listaFonti ) {
					if( source.getExtension("citaz") == null )
						source.putExtension( "citaz", quanteCitazioni(source) );
				}
			}
			Collections.sort( listaFonti, (f1, f2) -> {
				switch( ordine ) {
					case 1:	// Ordina per id numerico
						return U.soloNumeri(f1.getId()) - U.soloNumeri(f2.getId());
					case 2:
						return U.soloNumeri(f2.getId()) - U.soloNumeri(f1.getId());
					case 3:	// Ordine alfabeto dei titoli
						return titoloFonte(f1).compareToIgnoreCase( titoloFonte(f2) );
					case 4:
						return titoloFonte(f2).compareToIgnoreCase( titoloFonte(f1) );
					case 5:	// Ordina per numero di citazioni
						return U.castaJsonInt(f1.getExtension("citaz")) - U.castaJsonInt(f2.getExtension("citaz"));
					case 6:
						return U.castaJsonInt(f2.getExtension("citaz")) - U.castaJsonInt(f1.getExtension("citaz"));
				}
				return 0;
			});
		}
	}

	// Restituisce il titolo della source
	static String titoloFonte(org.folg.gedcom.model.Source fon ) {
		String tit = "";
		if( fon != null )
			if( fon.getAbbreviation() != null )
				tit = fon.getAbbreviation();
			else if( fon.getTitle() != null )
				tit = fon.getTitle();
			else if( fon.getText() != null ) {
				tit = fon.getText().replaceAll("\n", " ");
				//tit = tit.length() > 35 ? tit.substring(0,35)+"…" : tit;
			} else if( fon.getPublicationFacts() != null ) {
				tit = fon.getPublicationFacts().replaceAll("\n", " ");
			}
		return tit;
	}

	// Restituisce quante volte una source viene citata nel Gedcom
	// Ho provato a riscriverlo come Visitor, che però è molto più lento
	private int quante;
	private int quanteCitazioni(org.folg.gedcom.model.Source fon ) {
		quante = 0;
		for( Person p : Global.gc.getPeople() ) {
			cita( p, fon );
			for( Name n : p.getNames() )
				cita( n, fon );
			for( EventFact ef : p.getEventsFacts() )
				cita( ef, fon );
		}
		for( Family f : Global.gc.getFamilies() ) {
			cita( f, fon );
			for( EventFact ef : f.getEventsFacts() )
				cita( ef, fon );
		}
		for( Note n : Global.gc.getNotes() )
			cita( n, fon );
		return quante;
	}

	// riceve un Object (Person, Name, EventFact...) e conta quante volte è citata la source
	private void cita(Object ogg, org.folg.gedcom.model.Source source) {
		List<SourceCitation> listaSc;
		if( ogg instanceof Note )	// se è una Nota
			listaSc = ((Note) ogg).getSourceCitations();
		else {
			for( Note n : ((NoteContainer) ogg).getNotes() )
				cita( n, source);
			listaSc = ((SourceCitationContainer) ogg).getSourceCitations();
		}
		for( SourceCitation sc : listaSc ) {
			if( sc.getRef() != null )
				if( sc.getRef().equals(source.getId()) )
					quante++;
		}
	}

	static void nuovaFonte( Context contesto, Object contenitore ){
		org.folg.gedcom.model.Source source = new org.folg.gedcom.model.Source();
		source.setId( U.nuovoId( gc, org.folg.gedcom.model.Source.class ) );
		source.setTitle( "" );
		gc.addSource(source);
		if( contenitore != null ) {
			SourceCitation citaFonte = new SourceCitation();
			citaFonte.setRef( source.getId() );
			if( contenitore instanceof Note ) ((Note)contenitore).addSourceCitation( citaFonte );
			else ((SourceCitationContainer)contenitore).addSourceCitation( citaFonte );
		}
		U.salvaJson( true, source);
		Memory.setPrimo(source);
		contesto.startActivity( new Intent( contesto, Source.class ) );
	}

	// Elimina la source, i Ref in tutte le SourceCitation che puntano ad essa, e le SourceCitation vuote
	// Restituisce un array dei capostipiti modificati
	// Todo le citazioni alla Source eliminata diventano Fonte-nota a cui bisognerebbe poter riattaccare una Source
	public static Object[] eliminaFonte(org.folg.gedcom.model.Source fon ) {
		ListQuotesSource citazioni = new ListQuotesSource( gc, fon.getId() );
		for( ListQuotesSource.Tripletta cita : citazioni.lista ) {
			SourceCitation sc = cita.citazione;
			sc.setRef( null );
			// Se la SourceCitation non contiene altro si può eliminare
			boolean eliminabile = true;
			if( sc.getPage()!=null || sc.getDate()!=null || sc.getText()!=null || sc.getQuality()!=null
					|| !sc.getAllNotes(gc).isEmpty() || !sc.getAllMedia(gc).isEmpty() || !sc.getExtensions().isEmpty() )
				eliminabile = false;
			if( eliminabile ) {
				Object contenitore = cita.contenitore;
				List<SourceCitation> lista;
				if( contenitore instanceof Note )
					lista = ((Note)contenitore).getSourceCitations();
				else
					lista = ((SourceCitationContainer)contenitore).getSourceCitations();
				lista.remove( sc );
				if( lista.isEmpty() ) {
					if( contenitore instanceof Note )
						((Note)contenitore).setSourceCitations( null );
					else
						((SourceCitationContainer)contenitore).setSourceCitations( null );
				}
			}
		}
		gc.getSources().remove( fon );
		if( gc.getSources().isEmpty() )
			gc.setSources( null );
		gc.createIndexes();	// necessario
		Memory.annullaIstanze( fon );
		return citazioni.getCapi();
	}

	// menu opzioni nella toolbar
	@Override
	public void onCreateOptionsMenu( Menu menu, MenuInflater inflater ) {
		SubMenu subMenu = menu.addSubMenu(R.string.order_by);
		subMenu.add(0, 1, 0, R.string.id);
		subMenu.add(0, 2, 0, R.string.title);
		subMenu.add(0, 3, 0, R.string.citations);

		// Ricerca nella Biblioteca
		inflater.inflate(R.menu.cerca, menu);
		final SearchView vistaCerca = (SearchView)menu.findItem(R.id.ricerca).getActionView();
		vistaCerca.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange(String query) {
				adattatore.getFilter().filter(query);
				return true;
			}
			@Override
			public boolean onQueryTextSubmit(String q) {
				vistaCerca.clearFocus();
				return false;
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		int id = item.getItemId();
		if( id > 0 && id <= 3 ) {
			if( ordine == id*2-1 )
				ordine++;
			else if( ordine == id*2 )
				ordine--;
			else
				ordine = id*2-1;
			ordinaFonti();
			adattatore.notifyDataSetChanged();
			return true;
		}
		return false;
	}

	private String idFonte;
	@Override
	public void onCreateContextMenu( ContextMenu menu, View vista, ContextMenu.ContextMenuInfo info ) {
		idFonte = ((TextView)vista.findViewById(R.id.biblioteca_id)).getText().toString();
		menu.add(0, 0, 0, R.string.delete );
	}
	@Override
	public boolean onContextItemSelected( MenuItem item ) {
		if( item.getItemId() == 0 ) {	// Elimina
			Object[] oggetti = eliminaFonte( gc.getSource(idFonte) );
			U.salvaJson( false, oggetti );
			getActivity().recreate();
		} else {
			return false;
		}
		return true;
	}
}