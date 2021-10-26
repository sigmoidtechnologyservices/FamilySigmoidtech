package app.family;

import android.os.Bundle;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import org.folg.gedcom.model.ChildRef;
import org.folg.gedcom.model.EventFact;
import org.folg.gedcom.model.Name;
import org.folg.gedcom.model.ParentFamilyRef;
import org.folg.gedcom.model.Person;
import org.folg.gedcom.model.SpouseFamilyRef;
import org.folg.gedcom.model.SpouseRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import app.family.constants.Gender;
import app.family.detail.Event;
import app.family.detail.Family;

import static app.family.Global.gc;

public class EditaIndividuo extends AppCompatActivity {

	Person p;
	String idIndi;
	String idFamiglia;
	int relazione;
	EditText dataNascita;
	PublisherDate publisherDateNascita;
	EditText luogoNascita;
	SwitchCompat bottonMorte;
	EditText dataMorte;
	PublisherDate publisherDateMorte;
	EditText luogoMorte;
	boolean nomeDaPieces; // Se il nome/cognome vengono dai pieces Given e Surname, lì devono tornare

	@Override
	protected void onCreate(Bundle bandolo) {
		super.onCreate(bandolo);
		U.gedcomSicuro(gc);
		setContentView( R.layout.edita_individuo );
		Bundle bundle = getIntent().getExtras();
		idIndi = bundle.getString("idIndividuo");
		idFamiglia = bundle.getString("idFamiglia");
		relazione = bundle.getInt("relazione", 0 );

		dataNascita = findViewById( R.id.data_nascita );
		publisherDateNascita = findViewById(R.id.editore_data_nascita);
		luogoNascita = findViewById(R.id.luogo_nascita);
		bottonMorte = findViewById( R.id.defunto );
		dataMorte = findViewById( R.id.data_morte );
		publisherDateMorte = findViewById( R.id.editore_data_morte );
		luogoMorte = findViewById( R.id.luogo_morte );

		disattivaMorte();

		// Nuovo individuo in relazione di parentela
		if( relazione > 0 ) {
			p = new Person();
			Person perno = gc.getPerson( idIndi );
			String cogno = "";
			// Cognome del fratello
			if( relazione == 2 ) { // = fratello
				cogno = U.cognome(perno);
			// Cognome del padre
			} else if( relazione == 4 ) { // = figlio da Diagramma o Individuo
				if( Gender.isMale(perno) )
					cogno = U.cognome( perno );
				else if( idFamiglia != null ) {
					org.folg.gedcom.model.Family fam = gc.getFamily(idFamiglia);
					if( fam != null && !fam.getHusbands(gc).isEmpty() )
						cogno = U.cognome( fam.getHusbands(gc).get(0) );
				}
			} else if( relazione == 6 ) { // = figlio da Famiglia
				org.folg.gedcom.model.Family fam = gc.getFamily(idFamiglia);
				if( !fam.getHusbands(gc).isEmpty() )
					cogno = U.cognome( fam.getHusbands(gc).get(0) );
				else if( !fam.getChildren(gc).isEmpty() )
					cogno = U.cognome( fam.getChildren(gc).get(0) );
			}
			((EditText)findViewById( R.id.cognome )).setText( cogno );
		// Nuovo individuo scollegato
		} else if ( idIndi.equals("TIZIO_NUOVO") ) {
			p = new Person();
		// Carica i dati di un individuo esistente da modificare
		} else {
			p = gc.getPerson(idIndi);
			// Nome e cognome
			if( !p.getNames().isEmpty() ) {
				String nome = "";
				String cognome = "";
				Name n = p.getNames().get( 0 );
				String epiteto = n.getValue();
				if( epiteto != null ) {
					nome = epiteto.replaceAll( "/.*?/", "" ).trim(); // rimuove il cognome '/.../'
					if( epiteto.indexOf('/') < epiteto.lastIndexOf('/') )
						cognome = epiteto.substring( epiteto.indexOf('/') + 1, epiteto.lastIndexOf('/') ).trim();
				} else {
					if( n.getGiven() != null ) {
						nome = n.getGiven();
						nomeDaPieces = true;
					}
					if( n.getSurname() != null ) {
						cognome = n.getSurname();
						nomeDaPieces = true;
					}
				}
				((EditText)findViewById( R.id.nome )).setText( nome );
				((EditText)findViewById( R.id.cognome )).setText( cognome );
			}
			// Sex
			switch( Gender.getGender(p) ) {
				case MALE:
					((RadioButton)findViewById( R.id.sesso1 )).setChecked(true);
					break;
				case FEMALE:
					((RadioButton)findViewById( R.id.sesso2 )).setChecked(true);
					break;
				case UNDEFINED:
					((RadioButton)findViewById( R.id.sesso3 )).setChecked(true);
			}
			// Nascita e morte
			for( EventFact fatto : p.getEventsFacts() ) {
				if( fatto.getTag().equals("BIRT") ) {
					if( fatto.getDate() != null )
						dataNascita.setText( fatto.getDate().trim() );
					if( fatto.getPlace() != null )
						luogoNascita.setText(fatto.getPlace().trim());
				}
				if( fatto.getTag().equals("DEAT") ) {
					bottonMorte.setChecked(true);
					attivaMorte();
					if( fatto.getDate() != null )
						dataMorte.setText( fatto.getDate().trim() );
					if( fatto.getPlace() != null )
						luogoMorte.setText(fatto.getPlace().trim());
				}
			}
		}
		publisherDateNascita.inizia( dataNascita );
		bottonMorte.setOnCheckedChangeListener( (coso, attivo) -> {
			if (attivo)
				attivaMorte();
			else
				disattivaMorte();
		});
		publisherDateMorte.inizia( dataMorte );
		luogoMorte.setOnEditorActionListener( (vista, actionId, keyEvent) -> {
			if( actionId == EditorInfo.IME_ACTION_DONE )
				salva();
			return false;
		});

		// Barra
		ActionBar barra = getSupportActionBar();
		View barraAzione = getLayoutInflater().inflate( R.layout.edit_bar, new LinearLayout(getApplicationContext()), false);
		barraAzione.findViewById( R.id.edita_annulla ).setOnClickListener( v -> onBackPressed() );
		barraAzione.findViewById(R.id.edita_salva).setOnClickListener( v -> salva() );
		barra.setCustomView( barraAzione );
		barra.setDisplayShowCustomEnabled( true );
	}

	void disattivaMorte() {
		findViewById(R.id.morte).setVisibility( View.GONE );
		luogoNascita.setImeOptions( EditorInfo.IME_ACTION_DONE );
		luogoNascita.setNextFocusForwardId( 0 );
		// Intercetta il 'Done' sulla tastiera
		luogoNascita.setOnEditorActionListener( (view, action, event) -> {
			if( action == EditorInfo.IME_ACTION_DONE )
				salva();
			return false;
		});
	}

	void attivaMorte() {
		luogoNascita.setImeOptions( EditorInfo.IME_ACTION_NEXT );
		luogoNascita.setNextFocusForwardId( R.id.data_morte );
		luogoNascita.setOnEditorActionListener( null );
		findViewById(R.id.morte).setVisibility( View.VISIBLE );
	}

	void salva() {
		U.gedcomSicuro(gc); // È capitato un crash perché qui gc era null

		// Nome
		String nome = ((EditText)findViewById(R.id.nome)).getText().toString();
		String cognome = ((EditText)findViewById(R.id.cognome)).getText().toString();
		Name name;
		if( p.getNames().isEmpty() ) {
			List<Name> nomi = new ArrayList<>();
			name = new Name();
			nomi.add( name );
			p.setNames( nomi );
		} else
			name = p.getNames().get(0);

		if( nomeDaPieces ) {
			name.setGiven( nome );
			name.setSurname( cognome );
		} else {
			name.setValue( nome + " /" + cognome + "/".trim() );
		}

		// Sesso
		String sessoScelto = null;
		if( ((RadioButton)findViewById(R.id.sesso1)).isChecked() )
			sessoScelto = "M";
		else if( ((RadioButton)findViewById(R.id.sesso2)).isChecked() )
			sessoScelto = "F";
		else if( ((RadioButton) findViewById(R.id.sesso3)).isChecked() )
			sessoScelto = "U";
		if( sessoScelto != null ) {
			boolean mancaSesso = true;
			for( EventFact fatto : p.getEventsFacts() ) {
				if (fatto.getTag().equals("SEX")) {
					fatto.setValue(sessoScelto);
					mancaSesso = false;
				}
			}
			if( mancaSesso ) {
				EventFact sesso = new EventFact();
				sesso.setTag( "SEX" );
				sesso.setValue( sessoScelto );
				p.addEventFact( sesso );
			}
			IndividualEvents.aggiornaRuoliConiugali(p);
		}

		// Nascita
		publisherDateNascita.chiudi();
		String data = dataNascita.getText().toString();
		String luogo = luogoNascita.getText().toString();
		boolean trovato = false;
		for (EventFact fatto : p.getEventsFacts()) {
			if( fatto.getTag().equals("BIRT") ) {
					/* TODO: if( data.isEmpty() && luogo.isEmpty() && tagTuttoVuoto(fatto) )
					    p.getEventsFacts().remove(fatto);
					    più in generale, eliminare un tag quando è vuoto */
				fatto.setDate( data );
				fatto.setPlace( luogo );
				Event.ripulisciTag( fatto );
				trovato = true;
			}
		}
		// Se c'è qualche dato da salvare crea il tag
		if( !trovato && ( !data.isEmpty() || !luogo.isEmpty() ) ) {
			EventFact nascita = new EventFact();
			nascita.setTag( "BIRT" );
			nascita.setDate( data );
			nascita.setPlace( luogo );
			Event.ripulisciTag( nascita );
			p.addEventFact( nascita );
		}

		// Morte
		publisherDateMorte.chiudi();
		data = dataMorte.getText().toString();
		luogo = luogoMorte.getText().toString();
		trovato = false;
		for( EventFact fatto : p.getEventsFacts() ) {
			if( fatto.getTag().equals("DEAT") ) {
				if( !bottonMorte.isChecked() ) {
					p.getEventsFacts().remove(fatto);
				} else {
					fatto.setDate( data );
					fatto.setPlace( luogo );
					Event.ripulisciTag( fatto );
				}
				trovato = true;
				break;
			}
		}
		if( !trovato && bottonMorte.isChecked() ) {
			EventFact morte = new EventFact();
			morte.setTag( "DEAT" );
			morte.setDate( data );
			morte.setPlace( luogo );
			Event.ripulisciTag( morte );
			p.addEventFact( morte );
		}

		// Finalizzazione individuo nuovo
		Object[] modificati = { p, null }; // il null serve per accogliere una eventuale Family
		if( idIndi.equals("TIZIO_NUOVO") || relazione > 0 ) {
			String nuovoId = U.nuovoId( gc, Person.class );
			p.setId( nuovoId );
			gc.addPerson( p );
			if( Global.settings.getCurrentTree().root == null )
				Global.settings.getCurrentTree().root = nuovoId;
			Global.settings.save();
			if( relazione >= 5 ) { // viene da Famiglia
				Family.aggrega( p, gc.getFamily(idFamiglia), relazione );
				modificati[1] = gc.getFamily(idFamiglia);
			} else if( relazione > 0 ) // viene da Diagramma o IndividuoFamiliari
				modificati = aggiungiParente( idIndi, nuovoId, idFamiglia, relazione, getIntent().getStringExtra("collocazione") );
		} else
			Global.indi = p.getId(); // per mostrarlo orgogliosi in Diagramma
		U.salvaJson(true, modificati);
		onBackPressed();
	}

	/** Aggiunge un nuovo individuo in relazione di parentela con 'perno', eventualmente all'interno della famiglia fornita.
	 * @param idFamiglia Id della famiglia di destinazione. Se è null si crea una nuova famiglia
	 * @param collocazione Sintetizza come è stata individuata la famiglia e quindi cosa fare delle persone coinvolte
 	 */
	static Object[] aggiungiParente(String idPerno, String nuovoId, String idFamiglia, int relazione, String collocazione) {
		Global.indi = idPerno;
		Person nuovo = gc.getPerson( nuovoId );
		// Si crea una nuova famiglia in cui finiscono sia Perno che Nuovo
		if( collocazione != null && collocazione.startsWith("NUOVA_FAMIGLIA_DI") ) { // Contiene l'id del genitore di cui creare una nuova famiglia
			idPerno = collocazione.substring(17); // il genitore diventa effettivamente il perno
			relazione = relazione == 2 ? 4 : relazione; // anziché un fratello a perno, è come se mettessimo un figlio al genitore
		}
		// In Anagrafe è stata individuata la famiglia in cui finirà perno
		else if( collocazione != null && collocazione.equals("FAMIGLIA_ESISTENTE") ) {
			nuovoId = null;
			nuovo = null;
		}
		// Nuovo è accolto nella famiglia di Perno
		else if( idFamiglia != null ) {
			idPerno = null; // perno è già presente nella sua famiglia e non va riaggiunto
		}
		org.folg.gedcom.model.Family family = idFamiglia != null ? gc.getFamily(idFamiglia) : church.nuovaFamiglia(true);;
		Person perno = gc.getPerson( idPerno );
		SpouseRef refSposo1 = new SpouseRef(), refSposo2 = new SpouseRef();
		ChildRef refFiglio1 = new ChildRef(), refFiglio2 = new ChildRef();
		ParentFamilyRef refFamGenitori = new ParentFamilyRef();
		SpouseFamilyRef refFamSposi = new SpouseFamilyRef();
		refFamGenitori.setRef( family.getId() );
		refFamSposi.setRef( family.getId() );

		// Popolamento dei ref
		switch (relazione) {
			case 1: // Genitore
				refSposo1.setRef(nuovoId);
				refFiglio1.setRef(idPerno);
				if (nuovo != null) nuovo.addSpouseFamilyRef( refFamSposi );
				if (perno != null) perno.addParentFamilyRef( refFamGenitori );
				break;
			case 2: // Fratello
				refFiglio1.setRef(idPerno);
				refFiglio2.setRef(nuovoId);
				if (perno != null) perno.addParentFamilyRef( refFamGenitori );
				if (nuovo != null) nuovo.addParentFamilyRef( refFamGenitori );
				break;
			case 3: // Compagno
				refSposo1.setRef(idPerno);
				refSposo2.setRef(nuovoId);
				if (perno != null) perno.addSpouseFamilyRef( refFamSposi );
				if (nuovo != null) nuovo.addSpouseFamilyRef( refFamSposi );
				break;
			case 4: // Figlio
				refSposo1.setRef(idPerno);
				refFiglio1.setRef(nuovoId);
				if (perno != null) perno.addSpouseFamilyRef( refFamSposi );
				if (nuovo != null) nuovo.addParentFamilyRef( refFamGenitori );
		}

		if( refSposo1.getRef() != null )
			aggiungiConiuge(family, refSposo1);
		if( refSposo2.getRef() != null )
			aggiungiConiuge(family, refSposo2);
		if( refFiglio1.getRef() != null )
			family.addChild(refFiglio1);
		if( refFiglio2.getRef() != null )
			family.addChild(refFiglio2);

		if( (relazione == 1 || relazione == 2) ) // Farà comparire la famiglia selezionata
			Global.familyNum = gc.getPerson(Global.indi).getParentFamilies(gc).indexOf(family);
		else
			Global.familyNum = 0; // eventuale reset

		Set<Object> cambiati = new HashSet<>();
		if( perno != null && nuovo != null )
			Collections.addAll(cambiati, family, perno, nuovo);
		else if( perno != null )
			Collections.addAll(cambiati, family, perno);
		else if( nuovo != null )
			Collections.addAll(cambiati, family, nuovo);
		return cambiati.toArray();
	}

	// Aggiunge il coniuge in una famiglia: sempre e solo in base al sesso
	public static void aggiungiConiuge(org.folg.gedcom.model.Family family, SpouseRef sr) {
		Person person = Global.gc.getPerson(sr.getRef());
		if( Gender.isFemale(person) ) family.addWife(sr);
		else family.addHusband(sr);
	}
}