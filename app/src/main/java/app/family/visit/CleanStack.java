// Strettamente connesso a TrovaPila, individua gli oggetti da tenere nella pila

package app.family.visit;

class CleanStack extends VisitorTotal {

	private Object scopo;
	boolean daEliminare = true;

	CleanStack(Object scopo ) {
		this.scopo = scopo;
	}

	@Override
	boolean visita( Object oggetto, boolean capo ) { // il boolean qui è inutilizzato
		if( oggetto.equals(scopo) )
			daEliminare = false;
		return true;
	}
}

