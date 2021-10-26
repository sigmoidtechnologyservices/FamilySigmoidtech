package app.family;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.folg.gedcom.model.Person;
import org.folg.gedcom.model.SpouseFamilyRef;
import java.util.Collections;
import java.util.List;
import app.family.constants.Relation;
import app.family.detail.Family;

import static app.family.Global.gc;

public class IndividualFamily extends Fragment {

	private View vistaFamiglia;
	Person uno;

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		vistaFamiglia = inflater.inflate(R.layout.individual_card, container, false);
		if( gc != null ) {
			uno = gc.getPerson( Global.indi);
			if( uno != null ) {
				/* ToDo Mostrare/poter settare nelle famiglie geniotriali il pedigree, in particolare 'adopted'
				LinearLayout scatola = vistaFamiglia.findViewById( R.id.contenuto_scheda );
				for( ParentFamilyRef pfr : uno.getParentFamilyRefs() ) {
					U.metti( scatola, "Ref", pfr.getRef() );
					U.metti( scatola, "Primary", pfr.getPrimary() ); // Custom tag _PRIM _PRIMARY
					U.metti( scatola, "Relationship Type", pfr.getRelationshipType() ); // Tag PEDI (pedigree)
					for( Estensione altroTag : U.trovaEstensioni( pfr ) )
						U.metti( scatola, altroTag.nome, altroTag.testo );
				} */
				// Famiglie di origine: genitori e fratelli
				List<org.folg.gedcom.model.Family> listaFamiglie = uno.getParentFamilies(gc);
				for( org.folg.gedcom.model.Family family : listaFamiglie ) {
					for( Person padre : family.getHusbands(gc) )
						createCard(padre, Relation.PARENT, family);
					for( Person madre : family.getWives(gc) )
						createCard(madre, Relation.PARENT, family);
					for( Person fratello : family.getChildren(gc) ) // solo i figli degli stessi due genitori, non i fratellastri
						if( !fratello.equals(uno) )
							createCard(fratello, Relation.SIBLING, family);
				}
				// Fratellastri e sorellastre
				for( org.folg.gedcom.model.Family family : uno.getParentFamilies(gc) ) {
					for( Person padre : family.getHusbands(gc) ) {
						List<org.folg.gedcom.model.Family> famigliePadre = padre.getSpouseFamilies(gc);
						famigliePadre.removeAll(listaFamiglie);
						for( org.folg.gedcom.model.Family fam : famigliePadre )
							for( Person fratellastro : fam.getChildren(gc) )
								createCard(fratellastro, Relation.HALF_SIBLING, fam);
					}
					for( Person madre : family.getWives(gc) ) {
						List<org.folg.gedcom.model.Family> famiglieMadre = madre.getSpouseFamilies(gc);
						famiglieMadre.removeAll(listaFamiglie);
						for( org.folg.gedcom.model.Family fam : famiglieMadre )
							for( Person fratellastro : fam.getChildren(gc) )
								createCard(fratellastro, Relation.HALF_SIBLING, fam);
					}
				}
				// Coniugi e figli
				for( org.folg.gedcom.model.Family family : uno.getSpouseFamilies(gc) ) {
					for( Person marito : family.getHusbands(gc) )
						if( !marito.equals(uno) )
							createCard(marito, Relation.PARTNER, family);
					for( Person moglie : family.getWives(gc) )
						if( !moglie.equals(uno) )
							createCard(moglie, Relation.PARTNER, family);
					for( Person figlio : family.getChildren(gc) ) {
						createCard(figlio, Relation.CHILD, family);
					}
				}
			}
		}
		return vistaFamiglia;
	}

	void createCard(final Person person, Relation relation, org.folg.gedcom.model.Family family) {
		LinearLayout scatola = vistaFamiglia.findViewById(R.id.contenuto_scheda);
		View vistaPersona = U.mettiIndividuo(scatola, person, Family.getRole(person, family, relation, false));
		vistaPersona.setOnClickListener(v -> {
			getActivity().finish(); // Rimuove l'attività attale dallo stack
			Memory.replacePrimo(person);
			Intent intento = new Intent(getContext(), Individual.class);
			intento.putExtra("scheda", 2); // apre la scheda famiglia
			startActivity(intento);
		});
		registerForContextMenu(vistaPersona);
		vistaPersona.setTag(R.id.tag_famiglia, family); // Il principale scopo di questo tag è poter scollegare l'individuo dalla famiglia
		                                               // ma è usato anche qui sotto per spostare i molteplici matrimoni
	}

	private void spostaRiferimentoFamiglia(int direzione) {
		Collections.swap(uno.getSpouseFamilyRefs(), posFam, posFam + direzione);
		U.salvaJson(true, uno);
		refresh();
	}

	// Menu contestuale
	private String idIndividuo;
	private Person pers;
	private org.folg.gedcom.model.Family familia;
	private int posFam;
	@Override
	public void onCreateContextMenu( ContextMenu menu, View vista, ContextMenu.ContextMenuInfo info ) {
		idIndividuo = (String)vista.getTag();
		pers = gc.getPerson(idIndividuo);
		familia = (org.folg.gedcom.model.Family)vista.getTag( R.id.tag_famiglia );
		// posizione della famiglia coniugale per chi ne ha più di una
		posFam = -1;
		if( uno.getSpouseFamilyRefs().size() > 1 && !familia.getChildren(gc).contains(pers) ) { // solo i coniugi, non i figli
			List<SpouseFamilyRef> refi = uno.getSpouseFamilyRefs();
			for( SpouseFamilyRef sfr : refi )
				if( sfr.getRef().equals(familia.getId()) )
					posFam = refi.indexOf(sfr);
		}
		// Meglio usare numeri che non confliggano con i menu contestuali delle altre schede individuo
		menu.add(0, 300, 0, R.string.diagram);
		String[] familyLabels = Diagram.getFamilyLabels(getContext(), pers, familia);
		if( familyLabels[0] != null )
			menu.add(0, 301, 0, familyLabels[0]);
		if( familyLabels[1] != null )
			menu.add(0, 302, 0, familyLabels[1]);
		if( posFam > 0 )
			menu.add(0, 303, 0, R.string.move_before);
		if( posFam >= 0 && posFam < uno.getSpouseFamilyRefs().size() - 1 )
			menu.add(0, 304, 0, R.string.move_after);
		menu.add(0, 305, 0, R.string.modify);
		menu.add(0, 306, 0, R.string.unlink);
		if( !pers.equals(uno) ) // Qui non può eliminare sè stesso
			menu.add(0, 307, 0, R.string.delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int id = item.getItemId();
		if( id == 300 ) { // Diagramma
			U.qualiGenitoriMostrare(getContext(), pers, 1);
		} else if( id == 301 ) { // Famiglia come figlio
			U.qualiGenitoriMostrare(getContext(), pers, 2);
		} else if( id == 302 ) { // Famiglia come coniuge
			U.qualiConiugiMostrare(getContext(), pers, familia);
		} else if( id == 303 ) { // Sposta su
			spostaRiferimentoFamiglia(-1);
		} else if( id == 304 ) { // Sposta giù
			spostaRiferimentoFamiglia(1);
		} else if( id == 305 ) { // Modifica
			Intent intento = new Intent(getContext(), EditaIndividuo.class);
			intento.putExtra("idIndividuo", idIndividuo);
			startActivity(intento);
		} else if( id == 306 ) { // Scollega da questa famiglia
			Family.scollega(idIndividuo, familia);
			refresh();
			U.controllaFamiglieVuote(getContext(), this::refresh, false, familia);
			U.salvaJson(true, familia, pers);
		} else if( id == 307 ) { // Elimina
			new AlertDialog.Builder(getContext()).setMessage(R.string.really_delete_person)
					.setPositiveButton(R.string.delete, (dialog, i) -> {
						Registry.eliminaPersona(getContext(), idIndividuo);
						refresh();
						U.controllaFamiglieVuote(getContext(), this::refresh, false, familia);
					}).setNeutralButton(R.string.cancel, null).show();
		} else {
			return false;
		}
		return true;
	}

	// Rinfresca il contenuto del frammento Familiari
	void refresh() {
		FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
		fragmentManager.beginTransaction().detach(this).commit();
		fragmentManager.beginTransaction().attach(this).commit();
		requireActivity().invalidateOptionsMenu();
		// todo aggiorna la data cambiamento nella scheda Fatti
	}
}
