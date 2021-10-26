//DialogFragment which creates the dialogue to connect a relative in expert mode
package app.family;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import androidx.annotation.Keep;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import org.folg.gedcom.model.Family;
import org.folg.gedcom.model.Person;
import java.util.ArrayList;
import java.util.List;

public class New_Relative extends DialogFragment {

	private Person perno;
	private Family famPrefFiglio; // Family as a child to possibly show first in the spinner
	private Family famPrefSposo; //Family as a spouse to possibly show first in the spinner
	private boolean parenteNuovo;
	private Fragment frammento;
	private AlertDialog dialog;
	private Spinner spinner;
	private List<VoceFamiglia> voci = new ArrayList<>();
	private int relazione;

	public New_Relative(Person perno, Family preferitaFiglio, Family preferitaSposo, boolean nuovo, Fragment frammento) {
		this.perno = perno;
		famPrefFiglio = preferitaFiglio;
		famPrefSposo = preferitaSposo;
		parenteNuovo = nuovo;
		this.frammento = frammento;
	}

	// Zero-argument constructor: nececessary to re-instantiate this fragment (e.g. rotating the device screen)
	@Keep // Request to don't remove when minify
	public New_Relative() {}

	@Override
	public Dialog onCreateDialog(Bundle bundle) {
		// Recreate dialog
		if( bundle != null ) {
			perno = Global.gc.getPerson(bundle.getString("idPerno"));
			famPrefFiglio = Global.gc.getFamily(bundle.getString("idFamFiglio"));
			famPrefSposo = Global.gc.getFamily(bundle.getString("idFamSposo"));
			parenteNuovo = bundle.getBoolean("nuovo");
			frammento = getActivity().getSupportFragmentManager().getFragment(bundle, "frammento");
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		//builder.setTitle( nuovo ? R.string.new_relative : R.string.link_person );
		View vista = requireActivity().getLayoutInflater().inflate(R.layout.new_relative, null);
		// Spinner per scegliere la famiglia
		spinner = vista.findViewById(R.id.nuovoparente_famiglie);
		ArrayAdapter<VoceFamiglia> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		((View)spinner.getParent()).setVisibility( View.GONE ); // initially the spinner is hidden

		RadioButton ruolo1 = vista.findViewById(R.id.nuovoparente_1);
		ruolo1.setOnCheckedChangeListener((r, selected) -> {
			if( selected ) popolaSpinner(1);
		});
		RadioButton ruolo2 = vista.findViewById(R.id.nuovoparente_2);
		ruolo2.setOnCheckedChangeListener((r, selected) -> {
			if( selected ) popolaSpinner(2);
		});
		RadioButton ruolo3 = vista.findViewById(R.id.nuovoparente_3);
		ruolo3.setOnCheckedChangeListener((r, selected) -> {
			if( selected ) popolaSpinner(3);
		});
		RadioButton ruolo4 = vista.findViewById(R.id.nuovoparente_4);
		ruolo4.setOnCheckedChangeListener((r, selected) -> {
			if( selected ) popolaSpinner(4);
		});

		builder.setView(vista).setPositiveButton(android.R.string.ok, (dialog, id) -> {
			// Set some values that will be passed to EditaIndividuo or Anagrafe and will arrive at addParente ()
			Intent intento = new Intent();
			intento.putExtra("idIndividuo", perno.getId());
			intento.putExtra("relazione", relazione);
			VoceFamiglia voceFamiglia = (VoceFamiglia)spinner.getSelectedItem();
			if( voceFamiglia.famiglia != null )
				intento.putExtra("idFamiglia", voceFamiglia.famiglia.getId());
			else if( voceFamiglia.genitore != null ) // I use 'collocation' to convey the id of the parent (the third actor of the scene)
				intento.putExtra("collocazione", "NUOVA_FAMIGLIA_DI" + voceFamiglia.genitore.getId());
			else if( voceFamiglia.esistente ) //conveys to Anagrafe the intention to join an existing family
				intento.putExtra("collocazione", "FAMIGLIA_ESISTENTE");
			if( parenteNuovo ) { // Connect new person
				intento.setClass(getContext(), EditaIndividuo.class);
				startActivity(intento);
			} else { // Link existing person
				intento.putExtra("anagrafeScegliParente", true);
				intento.setClass(getContext(), Principal.class);
				if( frammento != null )
					frammento.startActivityForResult(intento, 1401);
				else
					getActivity().startActivityForResult(intento, 1401);
			}
		}).setNeutralButton(R.string.cancel, null);
		dialog = builder.create();
		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); // Initially disabled
	}

	@Override
	public void onSaveInstanceState(Bundle bandolo) {
		bandolo.putString("idPerno", perno.getId());
		if( famPrefFiglio != null )
			bandolo.putString("idFamFiglio", famPrefFiglio.getId());
		if( famPrefSposo != null )
			bandolo.putString("idFamSposo", famPrefSposo.getId());
		bandolo.putBoolean("nuovo", parenteNuovo);
		//Save the fragment's instance
		if( frammento != null )
			getActivity().getSupportFragmentManager().putFragment(bandolo, "frammento", frammento);
	}

	//It says if there is empty space in a family to add one of the two parents
	boolean carenzaConiugi(Family fam) {
		return fam.getHusbandRefs().size() + fam.getWifeRefs().size() < 2;
	}

	private void popolaSpinner( int relazione) {
		this.relazione = relazione;
		voci.clear();
		int select = -1; // Index of the item to be selected in the spinner
		                 // If -1 remains, it selects the first entry of the spinner
		switch( relazione ) {
			case 1: // Parent
				for( Family fam : perno.getParentFamilies(Global.gc) ) {
					voci.add( new VoceFamiglia(getContext(),fam) );
					if( (fam.equals(famPrefFiglio)   //Select the preferred family in which he is a child
							|| select < 0)           // or the first available
							&& carenzaConiugi(fam) ) // if they have empty parental space
						select = voci.size() - 1;
				}
				voci.add( new VoceFamiglia(getContext(),false) );
				if( select < 0 )
					select = voci.size() - 1; // Select "New family"
				break;
			case 2: // Brother
				for( Family fam : perno.getParentFamilies(Global.gc) ) {
					voci.add( new VoceFamiglia(getContext(),fam) );
					for( Person padre : fam.getHusbands(Global.gc) ) {
						for( Family fam2 : padre.getSpouseFamilies(Global.gc) )
							if( !fam2.equals(fam) )
								voci.add( new VoceFamiglia(getContext(),fam2) );
						voci.add( new VoceFamiglia(getContext(),padre) );
					}
					for( Person madre : fam.getWives(Global.gc) ) {
						for( Family fam2 : madre.getSpouseFamilies(Global.gc) )
							if( !fam2.equals(fam) )
								voci.add( new VoceFamiglia(getContext(),fam2) );
						voci.add( new VoceFamiglia(getContext(),madre) );
					}
				}
				voci.add( new VoceFamiglia(getContext(),false) );
				// Select the preferred family as a child
				select = 0;
				for( VoceFamiglia voce : voci )
					if( voce.famiglia != null && voce.famiglia.equals(famPrefFiglio) ) {
						select = voci.indexOf(voce);
						break;
					}
				break;
			case 3: // Spouse
			case 4: // Son
				for( Family fam : perno.getSpouseFamilies(Global.gc) ) {
					voci.add( new VoceFamiglia(getContext(),fam) );
					if( (voci.size() > 1 && fam.equals(famPrefSposo)) // Select preferred family as spouse (except the first one)
							|| (carenzaConiugi(fam) && select < 0) ) // Select the first family where spouses are missing
						select = voci.size() - 1;
				}
				voci.add( new VoceFamiglia(getContext(),perno) );
				if( select < 0 )
					select = voci.size() - 1; // Select "New family of ..."
				// For a child, select the preferred family (if it exists) otherwise the first
				if( relazione == 4 ) {
					select = 0;
					for( VoceFamiglia voce : voci )
						if( voce.famiglia != null && voce.famiglia.equals(famPrefSposo) ) {
							select = voci.indexOf(voce);
							break;
						}
				}
		}
		if( !parenteNuovo ) {
			voci.add( new VoceFamiglia(getContext(), true) );
		}
		ArrayAdapter<VoceFamiglia> adapter = (ArrayAdapter) spinner.getAdapter();
		adapter.clear();
		adapter.addAll(voci);
		((View)spinner.getParent()).setVisibility( View.VISIBLE );
		spinner.setSelection(select);
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
	}

	// Class for family list entries in dialogs "To which family do you want to add ...?"
	static class VoceFamiglia {
		Context contesto;
		Family famiglia;
		Person genitore;
		boolean esistente; //pin will try to fit into the existing family

		// Existing family
		VoceFamiglia(Context contesto, Family famiglia) {
			this.contesto = contesto;
			this.famiglia = famiglia;
		}

		// New family of a parent
		VoceFamiglia(Context contesto, Person genitore) {
			this.contesto = contesto;
			this.genitore = genitore;
		}

		//New empty family (false) OR family acquired by recipient (true)
		VoceFamiglia(Context contesto, boolean esistente) {
			this.contesto = contesto;
			this.esistente = esistente;
		}

		@Override
		public String toString() {
			if( famiglia != null)
				return U.testoFamiglia(contesto, Global.gc, famiglia, true);
			else if( genitore != null )
				return contesto.getString(R.string.new_family_of, U.epiteto(genitore));
			else if( esistente )
				return contesto.getString(R.string.existing_family);
			else
				return contesto.getString(R.string.new_family);
		}
	}
}
