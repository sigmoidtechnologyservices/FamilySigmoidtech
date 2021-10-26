package app.family;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.folg.gedcom.model.Media;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import app.family.visit.ListaMedia;
import app.family.visit.ListNote;
import static app.family.Global.gc;

public class Principal extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	DrawerLayout scatolissima;
	Toolbar toolbar;
	NavigationView menuPrincipe;
	List<Integer> idMenu = Arrays.asList( R.id.nav_diagramma, R.id.nav_persone, R.id.nav_famiglie,
			R.id.nav_media, R.id.nav_note, R.id.nav_fonti, R.id.nav_archivi, R.id.nav_autore );
	List<Class> frammenti = Arrays.asList( Diagram.class, Registry.class, church.class,
			Gallery.class, Notebook.class, Library.class, Warehouse.class, Podium.class );

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.principe);

		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		scatolissima = findViewById(R.id.scatolissima);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, scatolissima, toolbar, R.string.drawer_open, R.string.drawer_close );
		scatolissima.addDrawerListener(toggle);
		toggle.syncState();

		menuPrincipe = findViewById(R.id.menu);
		menuPrincipe.setNavigationItemSelectedListener(this);
		Global.principalView = scatolissima;
		U.gedcomSicuro( gc );
		furnishMenu();

		if( savedInstanceState == null ) {  // load the home only the first time, not rotating the screen
			Fragment fragment;
			String backName = null; // Label to locate diagram in the backstack of the fragments
			if( getIntent().getBooleanExtra("anagrafeScegliParente",false) )
				fragment = new Registry();
			else if( getIntent().getBooleanExtra("galleriaScegliMedia",false) )
				fragment = new Gallery();
			else if( getIntent().getBooleanExtra("bibliotecaScegliFonte",false) )
				fragment = new Library();
			else if( getIntent().getBooleanExtra("quadernoScegliNota",false) )
				fragment = new Notebook();
			else if( getIntent().getBooleanExtra("magazzinoScegliArchivio",false) )
				fragment = new Warehouse();
			else { // normal opening
				fragment = new Diagram();
				backName = "diagram";
			}
			getSupportFragmentManager().beginTransaction().replace(R.id.contenitore_fragment, fragment)
					.addToBackStack(backName).commit();
		}

		menuPrincipe.getHeaderView(0).findViewById(R.id.menu_alberi).setOnClickListener(v -> {
			scatolissima.closeDrawer(GravityCompat.START);
			startActivity(new Intent(Principal.this, Trees.class));
		});

		// It hides the most difficult menu items
		if( !Global.settings.expert ) {
			Menu menu = menuPrincipe.getMenu();
			menu.findItem(R.id.nav_fonti).setVisible(false);
			menu.findItem(R.id.nav_archivi).setVisible(false);
			menu.findItem(R.id.nav_autore).setVisible(false);
		}
	}

	// Almost always called except onBackPressed
	@Override
	public void onAttachFragment(@NonNull Fragment fragment) {
		super.onAttachFragment(fragment);
		if( !(fragment instanceof New_Relative) )
			aggiornaInterfaccia(fragment);
	}

	// Update contents when back with backPressed ()
	@Override
	public void onRestart() {
		super.onRestart();
		if( Global.edited ) {
			Fragment attuale = getSupportFragmentManager().findFragmentById(R.id.contenitore_fragment);
			if( attuale instanceof Diagram ) {
				((Diagram)attuale).forceDraw = true; // So he redraws the diagram
			} else if( attuale instanceof Registry) {
				// Update persons list
				Registry registry = (Registry)attuale;
				if( registry.people.size() == 0 ) // Probably it's a Collections.EmptyList
					registry.people = gc.getPeople(); // replace it with the real ArrayList
				registry.adapter.notifyDataSetChanged();
				registry.arredaBarra();
			} else if( attuale instanceof Gallery) {
				((Gallery)attuale).ricrea();
			} else {
				recreate(); // this should gradually disappear
			}
			Global.edited = false;
			furnishMenu(); // basically just to show the Save button
		}
	}

	// It receives a class like 'Diagram.class' and tells if it is the currently visible fragment on the scene
	private boolean frammentoAttuale(Class classe) {
		Fragment attuale = getSupportFragmentManager().findFragmentById(R.id.contenitore_fragment);
		return classe.isInstance(attuale);
	}

	// Update title, random image, 'Save' button in menu header, and menu items count
	void furnishMenu() {
		NavigationView navigation = scatolissima.findViewById(R.id.menu);
		View menuHeader = navigation.getHeaderView(0);
		ImageView imageView = menuHeader.findViewById( R.id.menu_immagine );
		TextView mainTitle = menuHeader.findViewById( R.id.menu_titolo );
		imageView.setVisibility( ImageView.GONE );
		mainTitle.setText( "" );
		if( Global.gc != null ) {
			ListaMedia cercaMedia = new ListaMedia( Global.gc, 3 );
			Global.gc.accept( cercaMedia );
			if( cercaMedia.lista.size() > 0 ) {
				int caso = new Random().nextInt( cercaMedia.lista.size() );
				for( Media med : cercaMedia.lista )
					if( --caso < 0 ) { // arriva a -1
						F.dipingiMedia( med, imageView, null );
						imageView.setVisibility( ImageView.VISIBLE );
						break;
					}
			}
			mainTitle.setText( Global.settings.getCurrentTree().title);
			if( Global.settings.expert ) {
				TextView treeNumView = menuHeader.findViewById(R.id.menu_number);
				treeNumView.setText(String.valueOf(Global.settings.openTree));
				treeNumView.setVisibility(ImageView.VISIBLE);
			}
			// Put count of existing records in menu items
			Menu menu = navigation.getMenu();
			for( int i = 1; i <= 7; i++ ) {
				int count = 0;
				switch( i ) {
					case 1: count = gc.getPeople().size(); break;
					case 2: count = gc.getFamilies().size(); break;
					case 3:
						ListaMedia mediaList = new ListaMedia(gc, 0);
						gc.accept(mediaList);
						count = mediaList.lista.size();
						break;
					case 4:
						ListNote notesList = new ListNote();
						gc.accept(notesList);
						count = notesList.listaNote.size() + gc.getNotes().size();
						break;
					case 5: count = gc.getSources().size(); break;
					case 6: count = gc.getRepositories().size(); break;
					case 7: count = gc.getSubmitters().size();
				}
				TextView countView = menu.getItem(i).getActionView().findViewById(R.id.menu_item_text);
				if( count > 0 )
					countView.setText(String.valueOf(count));
				else
					countView.setVisibility(View.GONE);
			}
		}
		// Save button
		Button saveButton = menuHeader.findViewById( R.id.menu_salva );
		saveButton.setOnClickListener( view -> {
			view.setVisibility( View.GONE );
			U.salvaJson( Global.gc, Global.settings.openTree);
			scatolissima.closeDrawer(GravityCompat.START);
			Global.daSalvare = false;
			Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
		});
		saveButton.setOnLongClickListener( vista -> {
			PopupMenu popup = new PopupMenu(this, vista);
			popup.getMenu().add(0, 0, 0, R.string.revert);
			popup.show();
			popup.setOnMenuItemClickListener( item -> {
				if( item.getItemId() == 0 ) {
					Trees.apriGedcom(Global.settings.openTree, false);
					U.qualiGenitoriMostrare(this, null, 0); // Simply reload the diagram
					scatolissima.closeDrawer(GravityCompat.START);
					saveButton.setVisibility(View.GONE);
					Global.daSalvare = false;
				}
				return true;
			});
			return true;
		});
		if( Global.daSalvare )
			saveButton.setVisibility( View.VISIBLE );
	}

	// Highlight menu item and show / hide toolbar
	void aggiornaInterfaccia(Fragment fragment) {
		if( fragment == null )
			fragment = getSupportFragmentManager().findFragmentById(R.id.contenitore_fragment);
		if( fragment != null ) {
			int numFram = frammenti.indexOf(fragment.getClass());
			if( menuPrincipe != null )
				menuPrincipe.setCheckedItem(idMenu.get(numFram));
			if( toolbar == null )
				toolbar = findViewById(R.id.toolbar);
			if( toolbar != null )
				toolbar.setVisibility(numFram == 0 ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public void onBackPressed() {
		if( scatolissima.isDrawerOpen(GravityCompat.START) ) {
			scatolissima.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
			if( getSupportFragmentManager().getBackStackEntryCount() == 0 ) {
				// Return to Trees instead of reviewing the first backstack diagram
				super.onBackPressed();
			} else
				aggiornaInterfaccia(null);
		}
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		Fragment fragment = null;
		try {
			fragment = (Fragment) frammenti.get( idMenu.indexOf(item.getItemId()) ).newInstance();
		} catch(Exception e) {}
		if( fragment != null ) {
			if( fragment instanceof Diagram ) {
				int cosaAprire = 0; // Show the diagram without asking for multiple parents
				// If I'm already in diagram and I click Diagram, it shows the root person
				if( frammentoAttuale(Diagram.class) ) {
					Global.indi = Global.settings.getCurrentTree().root;
					cosaAprire = 1; // Eventualmente chiede dei molteplici genitori
				}
				U.qualiGenitoriMostrare( this, Global.gc.getPerson(Global.indi), cosaAprire );
			} else {
				FragmentManager fm = getSupportFragmentManager();
				// Removes previous fragment from the story if it is the same one we are about to see
				if( frammentoAttuale(fragment.getClass()) ) fm.popBackStack();
				fm.beginTransaction().replace( R.id.contenitore_fragment, fragment ).addToBackStack(null).commit();
			}
		}
		scatolissima.closeDrawer(GravityCompat.START);
		return true;
	}

	// Automatically open the 'Sort by' sub-menu
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		MenuItem item0 = menu.getItem(0);
		if( item0.getTitle().equals(getString(R.string.order_by)) ) {
			item0.setVisible(false); // a little hack to prevent options menu to appear
			new Handler().post(() -> {
				item0.setVisible(true);
				menu.performIdentifierAction(item0.getItemId(), 0);
			});
		}
		return super.onMenuOpened(featureId, menu);
	}
}
