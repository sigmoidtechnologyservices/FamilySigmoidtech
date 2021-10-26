package app.family;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.folg.gedcom.model.Name;
import org.folg.gedcom.model.ParentFamilyRef;
import org.folg.gedcom.model.Person;
import org.folg.gedcom.model.SpouseFamilyRef;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import app.family.detail.Family;

import static app.family.Global.gc;

public class church extends Fragment {

	private LinearLayout scatola;
	private List<org.folg.gedcom.model.Family> listaFamiglie;
	private int ordine;
	private boolean gliIdsonoNumerici;
	
	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle stato ) {
		View vista = inflater.inflate( R.layout.warehouse, container, false );
		scatola = vista.findViewById( R.id.magazzino_scatola );
		if( gc != null ) {
			listaFamiglie = gc.getFamilies();
			((AppCompatActivity)getActivity()).getSupportActionBar().setTitle( listaFamiglie.size() + " "
					+ getString(listaFamiglie.size()==1 ? R.string.family : R.string.families).toLowerCase() );
			for( org.folg.gedcom.model.Family fam : listaFamiglie )
				mettiFamiglia( scatola, fam );
			if( listaFamiglie.size() > 1 )
				setHasOptionsMenu( true );
			gliIdsonoNumerici = verificaIdNumerici();
			vista.findViewById( R.id.fab ).setOnClickListener( v -> {
				org.folg.gedcom.model.Family nuovaFamily = nuovaFamiglia(true);
				U.salvaJson( true, nuovaFamily);
				// Se torna subito indietro in Chiesa rinfresca la lista con la famiglia vuota
				Memory.setPrimo(nuovaFamily);
				startActivity( new Intent( getContext(), Family.class ) );
			});
		}
		return vista;
	}

	void mettiFamiglia(LinearLayout scatola, org.folg.gedcom.model.Family fam ) {
		View vistaFamiglia = LayoutInflater.from(scatola.getContext()).inflate( R.layout.piece_family, scatola, false );
		scatola.addView( vistaFamiglia );
		String genitori = "";
		for( Person marito : fam.getHusbands(gc) )
			genitori += U.epiteto( marito ) + "\n";
		for( Person moglie : fam.getWives(gc) )
			genitori += U.epiteto( moglie ) + "\n";
		if( !genitori.isEmpty() )
			genitori = genitori.substring( 0, genitori.length() - 1 );
		((TextView)vistaFamiglia.findViewById( R.id.famiglia_genitori )).setText( genitori );
		String figli = "";
		for( Person figlio : fam.getChildren(gc) )
			figli += U.epiteto( figlio ) + "\n";
		if( !figli.isEmpty() )
			figli = figli.substring( 0, figli.length() - 1 );
		TextView testoFigli = vistaFamiglia.findViewById( R.id.famiglia_figli );
		if( figli.isEmpty() ) {
			vistaFamiglia.findViewById( R.id.famiglia_strut ).setVisibility( View.GONE );
			testoFigli.setVisibility( View.GONE );
		} else
			testoFigli.setText( figli );
		registerForContextMenu( vistaFamiglia );
		vistaFamiglia.setOnClickListener( v -> {
			Memory.setPrimo( fam );
			scatola.getContext().startActivity( new Intent( scatola.getContext(), Family.class ) );
		});
		vistaFamiglia.setTag( fam.getId() );	// solo per il menu contestuale Elimina qui in Chiesa
	}

	// Delete a family, removing the refs from members
	static void deleteFamily(org.folg.gedcom.model.Family family) {
		if( family == null ) return;
		Set<Person> membri = new HashSet<>();
		// Remove references to the family from family members
		for( Person marito : family.getHusbands(gc) ) {
			Iterator<SpouseFamilyRef> refi = marito.getSpouseFamilyRefs().iterator();
			while( refi.hasNext() ) {
				SpouseFamilyRef sfr = refi.next();
				if( sfr.getRef().equals(family.getId()) ) {
					refi.remove();
					membri.add( marito );
				}
			}
		}
		for( Person moglie : family.getWives(gc) ) {
			Iterator<SpouseFamilyRef> refi = moglie.getSpouseFamilyRefs().iterator();
			while( refi.hasNext() ) {
				SpouseFamilyRef sfr = refi.next();
				if( sfr.getRef().equals(family.getId()) ) {
					refi.remove();
					membri.add( moglie );
				}
			}
		}
		for( Person figlio : family.getChildren(gc) ) {
			Iterator<ParentFamilyRef> refi = figlio.getParentFamilyRefs().iterator();
			while( refi.hasNext() ) {
				ParentFamilyRef pfr = refi.next();
				if( pfr.getRef().equals(family.getId()) ) {
					refi.remove();
					membri.add( figlio );
				}
			}
		}
		// The family is deleted
		gc.getFamilies().remove(family);
		gc.createIndexes();	// necessario per aggiornare gli individui
		Memory.annullaIstanze(family);
		Global.familyNum = 0; // Nel caso fortuito che sia stata eliminata proprio questa famiglia
		U.salvaJson(true, membri.toArray(new Object[0]));
	}

	static org.folg.gedcom.model.Family nuovaFamiglia(boolean aggiungi ) {
		org.folg.gedcom.model.Family nuova = new org.folg.gedcom.model.Family();
		nuova.setId( U.nuovoId( gc, org.folg.gedcom.model.Family.class ));
		if( aggiungi )
			gc.addFamily( nuova );
		return nuova;
	}

	private View vistaScelta;
	@Override
	public void onCreateContextMenu( ContextMenu menu, View vista, ContextMenu.ContextMenuInfo info ) {
		vistaScelta = vista;
		menu.add(0, 0, 0, R.string.delete );
	}
	@Override
	public boolean onContextItemSelected( MenuItem item ) {
		if( item.getItemId() == 0 ) {	// Elimina
			org.folg.gedcom.model.Family fam = gc.getFamily( (String)vistaScelta.getTag() );
			if( fam.getHusbandRefs().size() + fam.getWifeRefs().size() + fam.getChildRefs().size() > 0 ) {
				new AlertDialog.Builder(getContext()).setMessage( R.string.really_delete_family )
						.setPositiveButton(android.R.string.yes, (dialog, i) -> {
							deleteFamily(fam);
							getActivity().recreate();
						}).setNeutralButton(android.R.string.cancel, null).show();
			} else {
				deleteFamily(fam);
				getActivity().recreate();
			}
		} else {
			return false;
		}
		return true;
	}

	// Verifica se tutti gli id delle famiglie contengono numeri
	// Appena un id contiene solo lettere restituisce falso
	boolean verificaIdNumerici() {
		esterno:
		for( org.folg.gedcom.model.Family f : gc.getFamilies() ) {
			for( char c : f.getId().toCharArray() ) {
				if (Character.isDigit(c))
					continue esterno;
			}
			return false;
		}
		return true;
	}

	// Cognome della persona
	String cognome(Person tizio) {
		if( !tizio.getNames().isEmpty() ) {
			Name epiteto = tizio.getNames().get(0);
			if( epiteto.getSurname() != null )
				return epiteto.getSurname().toLowerCase();
			else if( epiteto.getValue() != null ) {
				String tutto = epiteto.getValue();
				if( tutto.lastIndexOf('/') - tutto.indexOf('/') > 1 )	// se c'è un cognome tra i due '/'
					return tutto.substring( tutto.indexOf('/')+1, tutto.lastIndexOf('/') ).toLowerCase();
			}
		}
		return null;
	}

	// Restituisce una stringa con cognome principale della famiglia
	private String cognomeDiFamiglia(org.folg.gedcom.model.Family fam) {
		if( !fam.getHusbands(gc).isEmpty() )
			return( cognome(fam.getHusbands(gc).get(0)) );
		if( !fam.getWives(gc).isEmpty() )
			return( cognome(fam.getWives(gc).get(0)) );
		if( !fam.getChildren(gc).isEmpty() )
			return( cognome(fam.getChildren(gc).get(0)) );
		return null;
	}

	// Conta quanti familiari in una famiglia
	private int quantiFamiliari(org.folg.gedcom.model.Family fam) {
		return fam.getHusbandRefs().size() + fam.getWifeRefs().size() + fam.getChildRefs().size();
	}

	void ordinaFamiglie() {
		if( ordine > 0 ) {  // 0 ovvero rimane l'ordinamento già esistente
			Collections.sort(listaFamiglie, ( f1, f2 ) -> {
				switch( ordine ) {
					case 1: // Ordina per ID
						if( gliIdsonoNumerici )
							return U.soloNumeri(f1.getId()) - U.soloNumeri(f2.getId());
						else
							return f1.getId().compareToIgnoreCase(f2.getId());
					case 2:
						if( gliIdsonoNumerici )
							return U.soloNumeri(f2.getId()) - U.soloNumeri(f1.getId());
						else
							return f2.getId().compareToIgnoreCase(f1.getId());
					case 3: // Ordina per cognome
						String cognome1 = cognomeDiFamiglia(f1);
						String cognome2 = cognomeDiFamiglia(f2);
						if (cognome1 == null) // i nomi null vanno in fondo
							return cognome2 == null ? 0 : 1;
						if (cognome2 == null)
							return -1;
						return cognome1.compareTo(cognome2);
					case 4:
						String cognom1 = cognomeDiFamiglia(f1);
						String cognom2 = cognomeDiFamiglia(f2);
						if (cognom1 == null)
							return cognom2 == null ? 0 : 1;
						if (cognom2 == null)
							return -1;
						return cognom2.compareTo(cognom1);
					case 5:	// Ordina per numero di familiari
						return quantiFamiliari(f1) - quantiFamiliari(f2);
					case 6:
						return quantiFamiliari(f2) - quantiFamiliari(f1);
				}
				return 0;
			});
		}
	}

	@Override
	public void onCreateOptionsMenu( Menu menu, MenuInflater inflater ) {
		SubMenu subMenu = menu.addSubMenu(R.string.order_by);
		subMenu.add(0, 1, 0, R.string.id);
		subMenu.add(0, 2, 0, R.string.surname);
		subMenu.add(0, 3, 0, R.string.number_members);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if( id > 0 && id <= 3 ) {
			if( ordine == id * 2 - 1 )
				ordine++;
			else if( ordine == id * 2 )
				ordine--;
			else
				ordine = id * 2 - 1;
			ordinaFamiglie();
			scatola.removeAllViews();
			for( org.folg.gedcom.model.Family fam : listaFamiglie )
				mettiFamiglia(scatola, fam);
			//U.salvaJson( false ); // dubbio se metterlo per salvare subito il riordino delle famiglie
			return true;
		}
		return false;
	}
}