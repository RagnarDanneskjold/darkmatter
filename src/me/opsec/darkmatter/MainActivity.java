package me.opsec.darkmatter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class MainActivity extends Activity {

    private AlertDialog mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDialog();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dark Matter");
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // Close on back-button to get back to previous app
                finish();
            }
        });

        String[] items = getResources().getStringArray(R.array.actions);

        // Need to go through an adapter like this, since using the array
        // directly will show a radio button on each line
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);

        mMenu = builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                onListItemClick(item);
                dialog.dismiss();
            }
        }).create();

        mMenu.show();
    }

    protected void onListItemClick(int item) {
        switch (item) {
        case 0:
            create();
            break;
        case 1:
            open();
            break;
        case 2:
            close();
            break;
        case 3:
            delete();
            break;
        }
    }

    private void create() {
        Intent intent = new Intent(this, DarkService.class);
        intent.setAction(DarkService.ACTION_CREATE);
        intent.putExtra(DarkService.EXTRA_VOLUME_PATH, "");
        intent.putExtra(DarkService.EXTRA_SIZE, 42);
        intent.putExtra(DarkService.EXTRA_PASS_1, "pass1");
        intent.putExtra(DarkService.EXTRA_PASS_2, "pass2");
        startService(intent);
    }

    private void open() {
        Intent intent = new Intent(this, DarkService.class);
        intent.setAction(DarkService.ACTION_OPEN);
        intent.putExtra(DarkService.EXTRA_VOLUME_PATH, "");
        startService(intent);
    }

    private void close() {
        Intent intent = new Intent(this, DarkService.class);
        intent.setAction(DarkService.ACTION_CLOSE);
        intent.putExtra(DarkService.EXTRA_VOLUME_PATH, "");
        startService(intent);
    }

    private void delete() {
        Intent intent = new Intent(this, DarkService.class);
        intent.setAction(DarkService.ACTION_DELETE);
        intent.putExtra(DarkService.EXTRA_MOUNT_PATH, "");
        startService(intent);
    }
}