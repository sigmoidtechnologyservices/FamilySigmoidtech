// Gestisce le pile di oggetti gerarchici per scrivere la bava in Dettaglio

package app.family;

import org.folg.gedcom.model.Address;
import org.folg.gedcom.model.Change;
import org.folg.gedcom.model.EventFact;
import org.folg.gedcom.model.GedcomTag;
import org.folg.gedcom.model.Media;
import org.folg.gedcom.model.Name;
import org.folg.gedcom.model.Note;
import org.folg.gedcom.model.Person;
import org.folg.gedcom.model.Repository;
import org.folg.gedcom.model.RepositoryRef;
import org.folg.gedcom.model.SourceCitation;
import org.folg.gedcom.model.Submitter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import app.family.detail.Archive;
import app.family.detail.Archive_Ref;
import app.family.detail.Author;
import app.family.detail.Changes;
import app.family.detail.QuoteSource;
import app.family.detail.Extension;
import app.family.detail.Event;
import app.family.detail.Family;
import app.family.detail.Source;
import app.family.detail.Immagine;
import app.family.detail.Indirizzo;
import app.family.detail.Nome;
import app.family.detail.Nota;

public class Memory {

	static Map<Class,Class> classi = new HashMap<>();
	private static final Memory MEMORY = new Memory();
	List<Pila> lista = new ArrayList<>();

	Memory() {
		classi.put( Person.class, Individual.class );
		classi.put( Repository.class, Archive.class );
		classi.put( RepositoryRef.class, Archive_Ref.class );
		classi.put( Submitter.class, Author.class );
		classi.put( Change.class, Changes.class );
		classi.put( SourceCitation.class, QuoteSource.class );
		classi.put( GedcomTag.class, Extension.class );
		classi.put( EventFact.class, Event.class );
		classi.put( org.folg.gedcom.model.Family.class, Family.class );
		classi.put( org.folg.gedcom.model.Source.class, Source.class );
		classi.put( Media.class, Immagine.class );
		classi.put( Address.class, Indirizzo.class );
		classi.put( Name.class, Nome.class );
		classi.put( Note.class, Nota.class );
	}

	// Restituisce l'ultima pila creata se ce n'è almeno una
	// oppure ne restituisce una vuota giusto per non restituire null
	static Pila getPila() {
		if( MEMORY.lista.size() > 0 )
			return MEMORY.lista.get( MEMORY.lista.size() - 1 );
		else
			return new Pila(); // una pila vuota che non viene aggiunta alla lista
	}

	public static Pila addPila() {
		Pila pila = new Pila();
		MEMORY.lista.add( pila );
		return pila;
	}

	// Aggiunge il primo oggetto in una nuova pila
	public static void setPrimo( Object oggetto ) {
		setPrimo( oggetto, null );
	}

	public static void setPrimo( Object oggetto, String tag ) {
		addPila();
		Passo passo = aggiungi( oggetto );
		if( tag != null )
			passo.tag = tag;
		else if( oggetto instanceof Person )
			passo.tag = "INDI";
		//stampa("setPrimo");
	}

	// Aggiunge un oggetto alla fine dell'ultima pila esistente
	public static Passo aggiungi( Object oggetto ) {
		Passo passo = new Passo();
		passo.oggetto = oggetto;
		getPila().add( passo );
		//stampa("aggiungi");
		return passo;
	}

	// Mette il primo oggetto se non ci sono pile oppure sostituisce il primo oggetto nell'ultima pila esistente
	// In altre parole mette il primo oggetto senza aggiungere ulteriori pile
	public static void replacePrimo( Object oggetto ) {
		String tag = oggetto instanceof org.folg.gedcom.model.Family ? "FAM" : "INDI";
		if( MEMORY.lista.size() == 0 ) {
			setPrimo( oggetto, tag );
		} else {
			getPila().clear();
			Passo passo = aggiungi( oggetto );
			passo.tag = tag;
		}
		//stampa("replacePrimo");
	}

	// L'oggetto contenuto nel primo passo della pila
	public static Object oggettoCapo() {
		if( getPila().size() > 0 )
			return getPila().firstElement().oggetto;
		else
			return null;
	}

	// L'oggetto nel passo precedente all'ultimo
	public static Object oggettoContenitore() {
		if( getPila().size() > 1 )
			return getPila().get( getPila().size() - 2 ).oggetto;
		else
			return null;
	}

	// L'oggetto nell'ultimo passo
	public static Object getOggetto() {
		if( getPila().size() == 0 )
			return null;
		else
			return getPila().peek().oggetto;
	}

	static void arretra() {
		while( getPila().size() > 0 && getPila().lastElement().filotto )
			getPila().pop();
		if( getPila().size() > 0 )
			getPila().pop();
		if( getPila().isEmpty() )
			MEMORY.lista.remove( getPila() );
		//stampa("arretra");
	}

	// Quando un oggetto viene eliminato, lo rende null in tutti i passi,
	// e anche gli oggetti negli eventuali passi seguenti vengono annullati.
	public static void annullaIstanze( Object oggio ) {
		for( Pila pila : MEMORY.lista ) {
			boolean seguente = false;
			for( Passo passo : pila ) {
				if( passo.oggetto != null && (passo.oggetto.equals(oggio) || seguente) ) {
					passo.oggetto = null;
					seguente = true;
				}
			}
		}
	}

	public static void stampa( String intro ) {
		if( intro != null )
			s.l( intro );
		for( Pila pila : MEMORY.lista ) {
			for( Passo passo : pila ) {
				String filotto = passo.filotto ? "< " : "";
				if( passo.tag != null )
					s.p( filotto + passo.tag + " " );
				else if( passo.oggetto != null )
					s.p( filotto + passo.oggetto.getClass().getSimpleName() + " " );
				else
					s.p( filotto + "Null" ); // capita in rarissimi casi
			}
			s.l( "" );
		}
		s.l("- - - -");
	}

	static class Pila extends Stack<Passo> {}

	public static class Passo {
		public Object oggetto;
		public String tag;
		public boolean filotto; // TrovaPila lo setta true quindi onBackPressed la pila va eliminata in blocco
	}
}