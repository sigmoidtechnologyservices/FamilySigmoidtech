package app.family.detail;

import android.content.Intent;
import android.view.View;
import org.folg.gedcom.model.ChildRef;
import org.folg.gedcom.model.EventFact;
import org.folg.gedcom.model.ParentFamilyRef;
import org.folg.gedcom.model.Person;
import org.folg.gedcom.model.SpouseFamilyRef;
import org.folg.gedcom.model.SpouseRef;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import app.family.Detail;
import app.family.EditaIndividuo;
import app.family.Global;
import app.family.Individual;
import app.family.Memory;
import app.family.R;
import app.family.U;
import app.family.constants.Gender;
import app.family.constants.Relation;
import static app.family.Global.gc;

public class Family extends Detail {

	org.folg.gedcom.model.Family f;

	@Override
	public void impagina() {
		setTitle(R.string.family);
		f = (org.folg.gedcom.model.Family)casta(org.folg.gedcom.model.Family.class);
		mettiBava("FAM", f.getId());
		for( SpouseRef refMarito : f.getHusbandRefs() )
			member(refMarito, Relation.PARTNER);
		for( SpouseRef refMoglie : f.getWifeRefs() )
			member(refMoglie, Relation.PARTNER);
		for( ChildRef refFiglio : f.getChildRefs() )
			member(refFiglio, Relation.CHILD);
		for( EventFact ef : f.getEventsFacts() ) {
			metti(writeEventTitle(f, ef), ef);
		}
		mettiEstensioni(f);
		U.mettiNote(box, f, true);
		U.mettiMedia(box, f, true);
		U.citaFonti(box, f);
		U.cambiamenti(box, f.getChange());
	}

	// Add a member to the family
	void member(SpouseRef sr, Relation relation) {
		Person p = sr.getPerson(gc);
		if( p == null ) return;
		View vistaPersona = U.mettiIndividuo(box, p, getRole(p, f, relation, true));
		vistaPersona.setTag(R.id.tag_oggetto, p); // per il menu contestuale in Dettaglio
		/*  Ref nell'individuo verso la famiglia
			Se la stessa persona è presente più volte con lo stesso ruolo (parent/child) nella stessa famiglia
			i 2 loop seguenti individuano nella person il *primo* FamilyRef (INDI.FAMS / INDI.FAMC) che rimanda a quella famiglia
			Non prendono quello con lo stesso indice del corrispondente Ref nella famiglia  (FAM.HUSB / FAM.WIFE)
			Poteva essere un problema in caso di 'Scollega', ma non più perché tutto il contenuto di Famiglia viene ricaricato
		 */
		if( relation == Relation.PARTNER ) {
			for( SpouseFamilyRef sfr : p.getSpouseFamilyRefs() )
				if( sfr.getRef().equals(f.getId()) ) {
					vistaPersona.setTag(R.id.tag_spouse_family_ref, sfr);
					break;
				}
		} else if( relation == Relation.CHILD ) {
			for( ParentFamilyRef pfr : p.getParentFamilyRefs() )
				if( pfr.getRef().equals(f.getId()) ) {
					vistaPersona.setTag(R.id.tag_spouse_family_ref, pfr);
					break;
				}
		}
		vistaPersona.setTag(R.id.tag_spouse_ref, sr);
		registerForContextMenu(vistaPersona);
		vistaPersona.setOnClickListener(v -> {
			List<org.folg.gedcom.model.Family> parentFam = p.getParentFamilies(gc);
			List<org.folg.gedcom.model.Family> spouseFam = p.getSpouseFamilies(gc);
			// un coniuge con una o più famiglie in cui è figlio
			if( relation == Relation.PARTNER && !parentFam.isEmpty() ) {
				U.qualiGenitoriMostrare(this, p, 2);
			} // un figlio con una o più famiglie in cui è coniuge
			else if( relation == Relation.CHILD && !p.getSpouseFamilies(gc).isEmpty() ) {
				U.qualiConiugiMostrare(this, p, null);
			} // un figlio non sposato che ha più famiglie genitoriali
			else if( parentFam.size() > 1 ) {
				if( parentFam.size() == 2 ) { // Swappa tra le 2 famiglie genitoriali
					Global.indi = p.getId();
					Global.familyNum = parentFam.indexOf(f) == 0 ? 1 : 0;
					Memory.replacePrimo(parentFam.get(Global.familyNum));
					recreate();
				} else // Più di due famiglie
					U.qualiGenitoriMostrare( this, p, 2 );
			} // un coniuge senza genitori ma con più famiglie coniugali
			else if( spouseFam.size() > 1 ) {
				if( spouseFam.size() == 2 ) { // Swappa tra le 2 famiglie coniugali
					Global.indi = p.getId();
					org.folg.gedcom.model.Family altraFamiglia = spouseFam.get(spouseFam.indexOf(f) == 0 ? 1 : 0);
					Memory.replacePrimo(altraFamiglia);
					recreate();
				} else
					U.qualiConiugiMostrare(this, p, null);
			} else {
				Memory.setPrimo(p);
				startActivity(new Intent(this, Individual.class));
			}
		});
		if( unRappresentanteDellaFamiglia == null )
			unRappresentanteDellaFamiglia = p;
	}

	/** Find the role of a person from their relation with a family
	 * @param person
	 * @param family
	 * @param relation
	 * @param respectFamily The role to find is relative to the family (it becomes 'parent' with children)
	 * @return A descriptor text of the person's role
	 */
	public static String getRole(Person person, org.folg.gedcom.model.Family family, Relation relation, boolean respectFamily) {
		int role = 0;
		if( respectFamily && relation == Relation.PARTNER && family != null && !family.getChildRefs().isEmpty() )
			relation = Relation.PARENT;
		boolean married = U.areMarried(family);
		boolean divorced = U.areDivorced(family);
		if( Gender.isMale(person) ) {
			switch( relation ) {
				case PARENT: role = R.string.father; break;
				case SIBLING: role = R.string.brother; break;
				case HALF_SIBLING: role = R.string.half_brother; break;
				case PARTNER: role = married ? (divorced ? R.string.ex_husband : R.string.husband)
						: (divorced ? R.string.ex_male_partner : R.string.male_partner); break;
				case CHILD: role = R.string.son;
			}
		} else if( Gender.isFemale(person) ) {
			switch( relation ) {
				case PARENT: role = R.string.mother; break;
				case SIBLING: role = R.string.sister; break;
				case HALF_SIBLING: role = R.string.half_sister; break;
				case PARTNER: role = married ? (divorced ? R.string.ex_wife : R.string.wife)
						: (divorced ? R.string.ex_female_partner : R.string.female_partner); break;
				case CHILD: role = R.string.daughter;
			}
		} else {
			switch( relation ) {
				case PARENT: role = R.string.parent; break;
				case SIBLING: role = R.string.sibling; break;
				case HALF_SIBLING: role = R.string.half_sibling; break;
				case PARTNER: role = married ? (divorced ? R.string.ex_spouse : R.string.spouse)
						: (divorced ? R.string.ex_partner : R.string.partner); break;
				case CHILD: role = R.string.child;
			}
		}
		return Global.context.getString(role);
	}

	// Collega una persona ad una famiglia come genitore o figlio
	public static void aggrega(Person person, org.folg.gedcom.model.Family fam, int ruolo ) {
		switch( ruolo ) {
			case 5:	// Genitore
				// il ref dell'indi nella famiglia
				SpouseRef sr = new SpouseRef();
				sr.setRef(person.getId());
				EditaIndividuo.aggiungiConiuge(fam, sr);

				// il ref della famiglia nell'indi
				SpouseFamilyRef sfr = new SpouseFamilyRef();
				sfr.setRef( fam.getId() );
				//tizio.getSpouseFamilyRefs().add( sfr );	// no: con lista vuota UnsupportedOperationException
				//List<SpouseFamilyRef> listaSfr = tizio.getSpouseFamilyRefs();	// Non va bene:
				// quando la lista è inesistente, anzichè restituire una ArrayList restituisce una Collections$EmptyList che è IMMUTABILE cioè non ammette add()
				List<SpouseFamilyRef> listaSfr = new ArrayList<>( person.getSpouseFamilyRefs() );	// ok
				listaSfr.add( sfr );	// ok
				person.setSpouseFamilyRefs( listaSfr );
				break;
			case 6:	// Figlio
				ChildRef cr = new ChildRef();
				cr.setRef( person.getId() );
				fam.addChild( cr );
				ParentFamilyRef pfr = new ParentFamilyRef();
				pfr.setRef( fam.getId() );
				//tizio.getParentFamilyRefs().add( pfr );	// UnsupportedOperationException
				List<ParentFamilyRef> listaPfr = new ArrayList<>( person.getParentFamilyRefs() );
				listaPfr.add( pfr );
				person.setParentFamilyRefs( listaPfr );
		}
	}

	// Rimuove il singolo SpouseFamilyRef dall'individuo e il corrispondente SpouseRef dalla famiglia
	public static void scollega( SpouseFamilyRef sfr, SpouseRef sr ) {
		// Dalla persona alla famiglia
		Person pers = sr.getPerson( gc );
		pers.getSpouseFamilyRefs().remove( sfr );
		if( pers.getSpouseFamilyRefs().isEmpty() )
			pers.setSpouseFamilyRefs( null ); // Eventuale lista vuota viene eliminata
		pers.getParentFamilyRefs().remove( sfr );
		if( pers.getParentFamilyRefs().isEmpty() )
			pers.setParentFamilyRefs( null );
		// Dalla famiglia alla persona
		org.folg.gedcom.model.Family fam = sfr.getFamily( gc );
		fam.getHusbandRefs().remove( sr );
		if( fam.getHusbandRefs().isEmpty() )
			fam.setHusbandRefs( null );
		fam.getWifeRefs().remove( sr );
		if( fam.getWifeRefs().isEmpty() )
			fam.setWifeRefs( null );
		fam.getChildRefs().remove( sr );
		if( fam.getChildRefs().isEmpty() )
			fam.setChildRefs( null );
	}

	// Rimuove TUTTI i ref di un individuo in una famiglia
	public static void scollega( String idIndi, org.folg.gedcom.model.Family fam ) {
		// Rimuove i ref dell'indi nella famiglia
		Iterator<SpouseRef> refiSposo = fam.getHusbandRefs().iterator();
		while( refiSposo.hasNext() )
			if( refiSposo.next().getRef().equals(idIndi) )
				refiSposo.remove();
		if( fam.getHusbandRefs().isEmpty() )
			fam.setHusbandRefs( null ); // Elimina eventuale lista vuota

		refiSposo = fam.getWifeRefs().iterator();
		while( refiSposo.hasNext() )
			if( refiSposo.next().getRef().equals(idIndi) )
				refiSposo.remove();
		if( fam.getWifeRefs().isEmpty() )
			fam.setWifeRefs( null );

		Iterator<ChildRef> refiFiglio = fam.getChildRefs().iterator();
		while( refiFiglio.hasNext() )
			if( refiFiglio.next().getRef().equals(idIndi) )
				refiFiglio.remove();
		if( fam.getChildRefs().isEmpty() )
			fam.setChildRefs( null );

		// Rimuove i ref della famiglia nell'indi
		Person person = gc.getPerson(idIndi);
		Iterator<SpouseFamilyRef> iterSfr = person.getSpouseFamilyRefs().iterator();
		while( iterSfr.hasNext() )
			if( iterSfr.next().getRef().equals(fam.getId()) )
				iterSfr.remove();
		if( person.getSpouseFamilyRefs().isEmpty() )
			person.setSpouseFamilyRefs( null );

		Iterator<ParentFamilyRef> iterPfr = person.getParentFamilyRefs().iterator();
		while( iterPfr.hasNext() )
			if( iterPfr.next().getRef().equals(fam.getId()) )
				iterPfr.remove();
		if( person.getParentFamilyRefs().isEmpty() )
			person.setParentFamilyRefs( null );
	}
}