package com.example.driver;

/**
 * Created by Gumiero on 26/09/2014.
 */
import android.content.Context;
import android.database.Cursor;
import android.provider.Browser;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

public class TextCursorAdapter extends SimpleCursorAdapter {

    private Cursor c;
    private Context context;

    public TextCursorAdapter(Context context, int layout, Cursor c,
                             String[] from, int[] to, int flags) {

        super(context, layout, c, from, to, flags);
        this.c = c;
        this.context = context;

    }

    public View getView(int pos, View inView, ViewGroup parent) {
        View v = inView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.details_fragment, null);
        }
        this.c.moveToPosition(pos);
        String bookmark = this.c.getString(this.c.getColumnIndex(Browser.BookmarkColumns.TITLE));
        byte[] favicon = this.c.getBlob(this.c.getColumnIndex(Browser.BookmarkColumns.FAVICON));
        if (favicon != null) {
        //    ImageView iv = (ImageView) v.findViewById(R.id.bimage);
        //    iv.setImageBitmap(BitmapFactory.decodeByteArray(favicon, 0, favicon.length));
        }
        //TextView bTitle = (TextView) v.findViewById(R.id.btitle);
        //bTitle.setText(bookmark);
        return(v);
    }

}
