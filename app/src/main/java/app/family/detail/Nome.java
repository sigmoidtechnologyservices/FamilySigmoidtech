package app.family.detail;

import org.folg.gedcom.model.Name;
import org.folg.gedcom.model.Person;
import app.family.Detail;
import app.family.Global;
import app.family.Memory;
import app.family.R;
import app.family.U;
import static app.family.Global.gc;

public class Nome extends Detail {

	Name n;

	@Override
	public void impagina() {
		setTitle(R.string.name);
		mettiBava("NAME", null);
		n = (Name)casta(Name.class);
		if( Global.settings.expert )
			metti(getString(R.string.value), "Value");
		else {
			String nome = "";
			String cognome = "";
			String epiteto = n.getValue();
			if( epiteto != null ) {
				nome = epiteto.replaceAll("/.*?/", "").trim(); // Rimuove il cognome
				if( epiteto.indexOf('/') < epiteto.lastIndexOf('/') )
					cognome = epiteto.substring(epiteto.indexOf('/') + 1, epiteto.lastIndexOf('/')).trim();
			}
			creaPezzo(getString(R.string.given), nome, 4043, false);
			creaPezzo(getString(R.string.surname), cognome, 6064, false);
		}
		metti(getString(R.string.nickname), "Nickname");
		metti(getString(R.string.type), "Type", true, false); // _TYPE in GEDCOM 5.5, TYPE in GEDCOM 5.5.1
		metti(getString(R.string.prefix), "Prefix", Global.settings.expert, false);
		metti(getString(R.string.given), "Given", Global.settings.expert, false);
		metti(getString(R.string.surname_prefix), "SurnamePrefix", Global.settings.expert, false);
		metti(getString(R.string.surname), "Surname", Global.settings.expert, false);
		metti(getString(R.string.suffix), "Suffix", Global.settings.expert, false);
		metti(getString(R.string.married_name), "MarriedName", false, false); // _marrnm
		metti(getString(R.string.aka), "Aka", false, false); // _aka
		metti(getString(R.string.romanized), "Romn", Global.settings.expert, false);
		metti(getString(R.string.phonetic), "Fone", Global.settings.expert, false);
		mettiEstensioni(n);
		U.mettiNote(box, n, true);
		U.mettiMedia(box, n, true); // Mi sembra strano che un Name abbia Media.. comunque..
		U.citaFonti(box, n);
	}

	@Override
	public void elimina() {
		Person costui = gc.getPerson(Global.indi);
		costui.getNames().remove(n);
		U.aggiornaDate(costui);
		Memory.annullaIstanze(n);
	}
}
