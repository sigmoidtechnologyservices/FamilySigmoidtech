package app.family;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class Tombstone extends AppCompatActivity {

	@Override
	protected void onCreate( Bundle bandolo ) {
		super.onCreate( bandolo );
		setContentView( R.layout.tombstone);

		TextView versione = findViewById( R.id.lapide_versione );
		versione.setText( getString(R.string.version_name,BuildConfig.VERSION_NAME) );

		TextView collega = findViewById( R.id.lapide_link );
		collega.setPaintFlags( collega.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG );
		collega.setOnClickListener( v -> startActivity(
				new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.familygem.app")) )
		);
	}
}
