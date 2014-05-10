package me.opsec.darkmatter;

import me.opsec.darkmatter.service.DarkService;
import me.opsec.darkmatter.service.DarkStorage;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class MainActivity extends Activity {

    private AlertDialog mMenu;
    private DarkStorage mStorage;

    private static final ComponentName LAUNCHER_COMPONENT_NAME = new ComponentName(
            "me.opsec.darkmatter", "me.opsec.darkmatter.Launcher");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStorage = new DarkStorage(this);

        // Make sure the services are started
        Intent intent = new Intent(this, DarkService.class);
        startService(intent);

        if (isLauncherIconVisible()) {
            hideLauncherIcon();
        } else {
            showGui();
        }
    }

    private void showGui() {
        if (!mStorage.isCreated()) {
            create();
        } else if (!mStorage.isOpen()) {
            open();
        } else {
            showDialog();
        }
    }

    private boolean isLauncherIconVisible() {
        int enabledSetting = getPackageManager()
                .getComponentEnabledSetting(LAUNCHER_COMPONENT_NAME);
        return enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    }

    private void hideLauncherIcon() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Important!");
        builder.setMessage("To launch the app again, dial phone number 12345.");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                getPackageManager().setComponentEnabledSetting(LAUNCHER_COMPONENT_NAME,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
                showGui();
            }
        });
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
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
            close();
            break;
        case 1:
            delete();
            break;
        }
        finish();
    }

    private void create() {
        Intent intent = new Intent(this, CreateActivity.class);
        startActivity(intent);
    }

    private void open() {
        Intent intent = new Intent(this, OpenActivity.class);
        startActivity(intent);
    }

    private void close() {
        Intent intent = new Intent(this, DarkService.class);
        intent.setAction(DarkService.ACTION_CLOSE);
        startService(intent);
    }

    private void delete() {
        Intent intent = new Intent(this, DarkService.class);
        intent.setAction(DarkService.ACTION_DELETE);
        startService(intent);
    }
}