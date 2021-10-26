package app.family;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import org.folg.gedcom.model.CharacterSet;
import org.folg.gedcom.model.Family;
import org.folg.gedcom.model.Gedcom;
import org.folg.gedcom.model.GedcomVersion;
import org.folg.gedcom.model.Generator;
import org.folg.gedcom.model.Header;
import org.folg.gedcom.model.Person;
import org.folg.gedcom.model.Submitter;
import java.io.File;
import java.util.Locale;
import app.family.visit.ListaMedia;

public class InfoTree extends AppCompatActivity {

	Gedcom gc;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.info_tree);
		LinearLayout scatola = findViewById(R.id.info_scatola);

		final int treeId = getIntent().getIntExtra("idAlbero", 1);
		final Settings.Tree questoAlbero = Global.settings.getTree(treeId);
		final File file = new File(getFilesDir(), treeId + ".json");
		String i = getText(R.string.title) + ": " + questoAlbero.title;
		if( !file.exists() ) {
			i += "\n\n" + getText(R.string.item_exists_but_file) + "\n" + file.getAbsolutePath();
		} else  {
			i += "\n" + getText(R.string.file) + ": " + file.getAbsolutePath();
			gc = Trees.apriGedcomTemporaneo(treeId, false);
			if( gc == null )
				i += "\n\n" + getString(R.string.no_useful_data);
			else {
				// Aggiornamento dei dati automatico o su richiesta
				if( questoAlbero.persons < 100 ) {
					refreshData(gc, questoAlbero);
				} else {
					Button bottoneAggiorna = findViewById(R.id.info_aggiorna);
					bottoneAggiorna.setVisibility(View.VISIBLE);
					bottoneAggiorna.setOnClickListener(v -> {
						refreshData(gc, questoAlbero);
						recreate();
					});
				}
				i += "\n\n" + getText(R.string.persons) + ": "+ questoAlbero.persons
					+ "\n" + getText(R.string.families) + ": "+ gc.getFamilies().size()
					+ "\n" + getText(R.string.generations) + ": "+ questoAlbero.generations
					+ "\n" + getText(R.string.media) + ": "+ questoAlbero.media
					+ "\n" + getText(R.string.sources) + ": "+ gc.getSources().size()
					+ "\n" + getText(R.string.repositories) + ": "+ gc.getRepositories().size();
				if( questoAlbero.root != null ) {
					i += "\n" + getText(R.string.root) + ": " + U.epiteto(gc.getPerson(questoAlbero.root));
				}
				if( questoAlbero.shares != null && !questoAlbero.shares.isEmpty() ) {
					i += "\n\n" + getText(R.string.shares) + ":";
					for( Settings.Share share : questoAlbero.shares ) {
						i += "\n" + dataIdVersoData(share.dateId);
						if( gc.getSubmitter(share.submitter) != null )
							i += " - " + nomeAutore( gc.getSubmitter(share.submitter) );
					}
				}
			}
		}
		((TextView)findViewById(R.id.info_statistiche)).setText( i );

		Button bottoneHeader = scatola.findViewById( R.id.info_gestisci_testata );
		if( gc != null ) {
			Header h = gc.getHeader();
			if( h == null) {
				bottoneHeader.setText( R.string.create_header );
				bottoneHeader.setOnClickListener( view -> {
					gc.setHeader( TreeNew.creaTestata( file.getName() ) );
					U.salvaJson(gc, treeId);
					recreate();
				});
			} else {
				scatola.findViewById( R.id.info_testata ).setVisibility( View.VISIBLE );
				if( h.getFile() != null )
					poni( getText(R.string.file),  h.getFile() );
				if( h.getCharacterSet() != null ) {
					poni( getText(R.string.characrter_set), h.getCharacterSet().getValue() );
					poni( getText(R.string.version), h.getCharacterSet().getVersion() );
				}
				spazio();   // uno spazietto
				poni( getText(R.string.language), h.getLanguage() );
				spazio();
				poni( getText(R.string.copyright), h.getCopyright() );
				spazio();
				if (h.getGenerator() != null) {
					poni( getText(R.string.software), h.getGenerator().getName() != null ? h.getGenerator().getName() : h.getGenerator().getValue() );
					poni( getText(R.string.version), h.getGenerator().getVersion() );
					if( h.getGenerator().getGeneratorCorporation() != null ) {
						poni( getText(R.string.corporation), h.getGenerator().getGeneratorCorporation().getValue() );
						if( h.getGenerator().getGeneratorCorporation().getAddress() != null )
							poni( getText(R.string.address), h.getGenerator().getGeneratorCorporation().getAddress().getDisplayValue() ); // non è male
						poni( getText(R.string.telephone), h.getGenerator().getGeneratorCorporation().getPhone() );
						poni( getText(R.string.fax), h.getGenerator().getGeneratorCorporation().getFax() );
					}
					spazio();
					if( h.getGenerator().getGeneratorData() != null ) {
						poni( getText(R.string.source), h.getGenerator().getGeneratorData().getValue() );
						poni( getText(R.string.date), h.getGenerator().getGeneratorData().getDate() );
						poni( getText(R.string.copyright), h.getGenerator().getGeneratorData().getCopyright() );
					}
				}
				spazio();
				if( h.getSubmitter(gc) != null )
					poni( getText( R.string.submitter ), nomeAutore(h.getSubmitter(gc)) ); // todo: renderlo cliccabile?
				if( gc.getSubmission() != null )
					poni( getText(R.string.submission), gc.getSubmission().getDescription() ); // todo: cliccabile
				spazio();
				if( h.getGedcomVersion() != null ) {
					poni( getText(R.string.gedcom), h.getGedcomVersion().getVersion() );
					poni( getText(R.string.form), h.getGedcomVersion().getForm() );
				}
				poni( getText(R.string.destination), h.getDestination() );
				spazio();
				if( h.getDateTime() != null ) {
					poni( getText(R.string.date), h.getDateTime().getValue() );
					poni( getText(R.string.time), h.getDateTime().getTime() );
				}
				spazio();
				for( Extension est : U.trovaEstensioni(h) ) {	// ogni estensione nella sua riga
					poni( est.nome, est.testo );
				}
				spazio();
				if( righetta != null )
					((TableLayout)findViewById( R.id.info_tabella ) ).removeView( righetta );

				// Bottone per aggiorna l'header GEDCOM coi parametri di Family Gem
				bottoneHeader.setOnClickListener( view -> {
					h.setFile(treeId + ".json");
					CharacterSet caratteri = h.getCharacterSet();
					if( caratteri == null ) {
						caratteri = new CharacterSet();
						h.setCharacterSet( caratteri );
					}
					caratteri.setValue( "UTF-8" );
					caratteri.setVersion( null );

					Locale loc = new Locale( Locale.getDefault().getLanguage() );
					h.setLanguage( loc.getDisplayLanguage(Locale.ENGLISH) );

					Generator programma = h.getGenerator();
					if( programma == null ) {
						programma = new Generator();
						h.setGenerator( programma );
					}
					programma.setValue( "FAMILY_GEM" );
					programma.setName( getString(R.string.app_name) );
					//programma.setVersion( BuildConfig.VERSION_NAME ); // lo farà salvaJson()
					programma.setGeneratorCorporation( null );

					GedcomVersion versioneGc = h.getGedcomVersion();
					if( versioneGc == null ) {
						versioneGc = new GedcomVersion();
						h.setGedcomVersion( versioneGc );
					}
					versioneGc.setVersion( "5.5.1" );
					versioneGc.setForm( "LINEAGE-LINKED" );
					h.setDestination( null );

					U.salvaJson(gc, treeId);
					recreate();
				});

				U.mettiNote(scatola, h, true);
			}
			// Estensioni del Gedcom, ovvero tag non standard di livello 0 zero
			for( Extension est : U.trovaEstensioni(gc) ) {
				U.metti( scatola, est.nome, est.testo );
			}
		} else
			bottoneHeader.setVisibility(View.GONE);
	}

	String dataIdVersoData(String id) {
		if( id == null ) return "";
		return id.substring(0, 4) + "-" + id.substring(4, 6) + "-" + id.substring(6, 8) + " "
				+ id.substring(8, 10) + ":" + id.substring(10, 12) + ":" + id.substring(12);
	}

	static String nomeAutore( Submitter autor ) {
		String nome = autor.getName();
		if( nome == null )
			nome = "[" + Global.context.getString(R.string.no_name) + "]";
		else if( nome.isEmpty() )
			nome = "[" + Global.context.getString(R.string.empty_name) + "]";
		return nome;
	}

	// Refresh the data displayed below the tree title in Alberi list
	static void refreshData(Gedcom gedcom, Settings.Tree treeItem) {
		treeItem.persons = gedcom.getPeople().size();
		treeItem.generations = quanteGenerazioni(gedcom, U.getRootId(gedcom, treeItem));
		ListaMedia visitaMedia = new ListaMedia(gedcom, 0);
		gedcom.accept(visitaMedia);
		treeItem.media = visitaMedia.lista.size();
		Global.settings.save();
	}

	boolean testoMesso;  // impedisce di mettere più di uno spazio() consecutivo
	void poni(CharSequence titolo, String testo) {
		if( testo != null ) {
			TableRow riga = new TableRow(getApplicationContext());
			TextView cella1 = new TextView(getApplicationContext());
			cella1.setTextSize(14);
			cella1.setTextColor(Color.BLACK);
			cella1.setTypeface(null, Typeface.BOLD);
			cella1.setPadding(0, 0, 10, 0);
			cella1.setGravity(Gravity.END);
			cella1.setText(titolo);
			riga.addView(cella1);
			TextView cella2 = new TextView(getApplicationContext());
			cella2.setTextSize(14);
			cella2.setTextColor(Color.BLACK);
			cella2.setPadding(0, 0, 0, 0);
			cella2.setText(testo);
			riga.addView(cella2);
			((TableLayout)findViewById(R.id.info_tabella)).addView(riga);
			testoMesso = true;
		}
	}

	TableRow righetta;
	void spazio() {
		if( testoMesso ) {
			righetta = new TableRow(getApplicationContext());
			View cella = new View(getApplicationContext());
			cella.setBackgroundResource(R.color.primario);
			righetta.addView(cella);
			TableRow.LayoutParams param = (TableRow.LayoutParams)cella.getLayoutParams();
			param.weight = 1;
			param.span = 2;
			param.height = 1;
			param.topMargin = 5;
			param.bottomMargin = 5;
			cella.setLayoutParams(param);
			((TableLayout)findViewById(R.id.info_tabella)).addView(righetta);
			testoMesso = false;
		}
	}

	public static int quanteGenerazioni(Gedcom gc, String radice) {
		if( gc.getPeople().isEmpty() )
			return 0;
		genMin = 0;
		genMax = 0;
		risaliGenerazioni(gc.getPerson(radice), gc, 0);
		// Rimuove dalle persone l'estensione 'gen' per permettere successivi conteggi
		for( Person p : gc.getPeople() ) {
			p.getExtensions().remove("gen");
			if( p.getExtensions().isEmpty() )
				p.setExtensions(null);
		}
		return 1 - genMin + genMax;
	}

	static int genMin;
	static int genMax;

	// riceve una Person e trova il numero della generazione di antenati più remota
	static void risaliGenerazioni(Person p, Gedcom gc, int gen) {
		if( gen < genMin )
			genMin = gen;
		// aggiunge l'estensione per indicare che è passato da questa Persona
		p.putExtension("gen", gen);
		// se è un capostipite va a contare le generazioni di discendenti
		if( p.getParentFamilies(gc).isEmpty() )
			discendiGenerazioni(p, gc, gen);
		for( Family f : p.getParentFamilies(gc) ) {
			// intercetta eventuali fratelli del capostipite
			if( f.getHusbands(gc).isEmpty() && f.getWives(gc).isEmpty() ) {
				for( Person frate : f.getChildren(gc) )
					if( frate.getExtension("gen") == null )
						discendiGenerazioni(frate, gc, gen);
			}
			for( Person padre : f.getHusbands(gc) )
				if( padre.getExtension("gen") == null )
					risaliGenerazioni(padre, gc, gen - 1);
			for( Person madre : f.getWives(gc) )
				if( madre.getExtension("gen") == null )
					risaliGenerazioni(madre, gc, gen - 1);
		}
	}

	// riceve una Person e trova il numero della generazione più remota di discendenti
	static void discendiGenerazioni(Person p, Gedcom gc, int gen) {
		if( gen > genMax )
			genMax = gen;
		p.putExtension("gen", gen);
		for( Family fam : p.getSpouseFamilies(gc) ) {
			// individua anche la famiglia dei coniugi
			for( Person moglie : fam.getWives(gc) )
				if( moglie.getExtension("gen") == null )
					risaliGenerazioni(moglie, gc, gen);
			for( Person marito : fam.getHusbands(gc) )
				if( marito.getExtension("gen") == null )
					risaliGenerazioni(marito, gc, gen);
			for( Person figlio : fam.getChildren(gc) )
				discendiGenerazioni(figlio, gc, gen + 1);
		}
	}

	// freccia indietro nella toolbar come quella hardware
	@Override
	public boolean onOptionsItemSelected(MenuItem i) {
		onBackPressed();
		return true;
	}
}
