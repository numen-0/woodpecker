package com.example.woodpecker;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EditorActivity extends AppCompatActivity {
    private MorseCharSet morseCharSet;
    private MorseCharSet editMorseCharSet;
    private RecyclerView recyclerView;
    private MorseAdapter adapter;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(this::showPopupMenu);

        Button button = findViewById(R.id.backButton);
        button.setOnClickListener(v -> openMainActivity());

        this.morseCharSet = (MorseCharSet) getIntent().getSerializableExtra("morseCharset");
        assert this.morseCharSet != null;
        this.editMorseCharSet = morseCharSet;

        recyclerView = findViewById(R.id.mcRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set the adapter for RecyclerView with item click listener
        // Handle item click: show edit options for rep or sequence
        adapter = new MorseAdapter(this, editMorseCharSet.getCharset(), this::showEditMorseCharDialog);
        recyclerView.setAdapter(adapter);

        tv = findViewById(R.id.selected_standard);
        tv.setText(this.editMorseCharSet.name);

        button = findViewById(R.id.addButton);
        button.setOnClickListener(v -> showAddMorseCharDialog());
    }

    public void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("morseCharset", this.morseCharSet);
        startActivity(intent);
        finish();
    }

    final static int MENU_DELETE = R.id.menu_delete;
    final static int MENU_NEW = R.id.menu_new;
    final static int MENU_EDIT = R.id.menu_edit;

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.editor_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            final int id = item.getItemId();
            if (id == MENU_NEW) {
                showCreateStandardDialog();
                return true;
            } else if (id == MENU_DELETE) {
                showDeleteStandardDialog();
                return true;
            } else if (id == MENU_EDIT) {
                showEditStandardDialog();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void showCreateStandardDialog() {
        EditText input = new EditText(this);
        // input.setHint("Enter new standard name");

        new AlertDialog.Builder(this)
                .setTitle(R.string.msg_create_mc)
                .setView(input)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newStandard = input.getText().toString().trim();
                    if (newStandard.isEmpty()) {
                        Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    MyDB db = new MyDB(this);
                    if (db.createMorseCharset(newStandard)) {
                        loadStandardForEditing(newStandard);
                        Toast.makeText(this, R.string.ok_new_std, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.error_std_exists, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showDeleteStandardDialog() {
        ArrayList<String> standardNames = new MyDB(this).getAllMorseCharsets();

        if (standardNames.isEmpty()) {
            Toast.makeText(this, R.string.error_no_stds, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] standardArray = standardNames.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle(R.string.msg_select_std_del)
                .setItems(standardArray, (dialog, which) -> confirmDeleteStandard(standardNames.get(which)))
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void confirmDeleteStandard(String standard) {
        MyDB db = new MyDB(this);

        ArrayList<String> remainingStandards = db.getAllMorseCharsets();
        if ( remainingStandards.size() <= 1 ) {
            Toast.makeText(this, R.string.error_one_std, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.msg_confirm_del)
                .setMessage(getString(R.string.msg_are_you_sure) + " '" + standard + "'?")
                .setPositiveButton(R.string.delete_mc, (dialog, which) -> {
                    if ( !db.deleteMorseCharset(standard) ) {
                        Toast.makeText(this, R.string.error_std_404, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(this, R.string.ok_std_deleted, Toast.LENGTH_SHORT).show();

                    if ( editMorseCharSet.name.equals(standard) ) {
                        ArrayList<String> updatedStandards = db.getAllMorseCharsets();
                        String newSelectedStandard = updatedStandards.get(0);

                        loadStandardForEditing(newSelectedStandard); // Load the new standard for editing
                        if ( morseCharSet.name.equals(standard) ) {
                            morseCharSet = editMorseCharSet;
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void showEditStandardDialog() {
        ArrayList<String> standardNames = new MyDB(this).getAllMorseCharsets();

        if (standardNames.isEmpty()) {
            Toast.makeText(this, R.string.error_no_stds, Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert list to array for AlertDialog
        String[] standardArray = standardNames.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle(R.string.msg_select_std_edit)
                .setItems(standardArray, (dialog, which) -> loadStandardForEditing(standardNames.get(which)))
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadStandardForEditing(String standard) {
        tv.setText(standard);
        MyDB myDB = new MyDB(this);
        this.editMorseCharSet = myDB.loadMorseCharset(standard);

        // Handle item click: show edit options for rep or sequence
        adapter = new MorseAdapter(this, editMorseCharSet.getCharset(), this::showEditMorseCharDialog);
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged(); // Notify adapter about data change
    }

    private void showEditMorseCharDialog(MorseCharSet.MorseChar morseChar) {
        EditText input = new EditText(this);
        input.setText(morseChar.rep);

        onMorseCharClicked(morseChar);
    }

    private void onMorseCharClicked(MorseCharSet.MorseChar morseChar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.edit_mc) + ": '" + morseChar.rep + "'");

        builder.setItems(new String[]{
                getString(R.string.edit_rep),
                getString(R.string.edit_seq),
                getString(R.string.edit_del) // New delete option
        }, (dialog, which) -> {
            switch (which) {
                case 0: // "Edit Rep"
                    showEditDialog(getString(R.string.edit_rep), morseChar, true);
                    break;
                case 1: // "Edit Sequence"
                    showEditDialog(getString(R.string.edit_seq), morseChar, false);
                    break;
                case 2: // "Delete Character"
                    showDeleteConfirmationDialog(morseChar);
                    break;
            }
        });

        builder.show();
    }

    private void showEditDialog(String title, MorseCharSet.MorseChar mc, boolean isRep) {
        final EditText input = new EditText(this);
        input.setText( isRep ? mc.rep : mc.sequence );

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setView(input)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newValue = input.getText().toString().trim().toUpperCase();

                    if ( !isValidInput(isRep, newValue) ) { // Validate input
                        Toast.makeText(this, R.string.invalid_input, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if ( isRep && !editMorseCharSet.updateRep(newValue, mc.sequence) ) {
                        Toast.makeText(this, R.string.invalid_input, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if ( !isRep && !editMorseCharSet.updateSeq(mc.rep, newValue) ) {
                        Toast.makeText(this, R.string.invalid_input, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    MyDB db = new MyDB(this); // Save updated charset to DB
                    db.saveMorseCharset(editMorseCharSet.name, editMorseCharSet.getCharset());

                    adapter.setMorseCharset(editMorseCharSet.getCharset());

                    Toast.makeText(this, title + " " + getString(R.string.ok_updated), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void showDeleteConfirmationDialog(MorseCharSet.MorseChar morseChar) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_mc) + " '" + morseChar.rep + "'?")
                .setPositiveButton(R.string.delete_mc, (dialog, which) -> {
                    editMorseCharSet.remove(morseChar);
                    MyDB db = new MyDB(this);
                    db.saveMorseCharset(editMorseCharSet.name, editMorseCharSet.getCharset()); // Save updated charset

                    adapter.setMorseCharset(editMorseCharSet.getCharset());
                    adapter.notifyDataSetChanged(); // Refresh RecyclerView

                    Toast.makeText(this, R.string.char_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showAddMorseCharDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.add_new_morse_char));

        // Create input fields
        EditText inputRep = new EditText(this);
        inputRep.setHint(getString(R.string.enter_character));

        EditText inputSeq = new EditText(this);
        inputSeq.setHint(getString(R.string.enter_morse_code));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(inputRep);
        layout.addView(inputSeq);

        builder.setView(layout);

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String rep = inputRep.getText().toString().trim();
            String sequence = inputSeq.getText().toString().trim().toUpperCase();

            if (!isValidInput(true, rep) || !isValidInput(false, sequence)) {
                Toast.makeText(this, R.string.invalid_input, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!editMorseCharSet.add(rep, sequence)) {
                Toast.makeText(this, getString(R.string.duplicated_entry), Toast.LENGTH_SHORT).show();
                return;
            }

            MyDB db = new MyDB(this);
            db.saveMorseCharset(editMorseCharSet.name, editMorseCharSet.getCharset());

            adapter.setMorseCharset(editMorseCharSet.getCharset());

            Toast.makeText(this, getString(R.string.char_added), Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private boolean isValidInput(boolean isRep, String input) {
        if (isRep) {
            return !input.isEmpty();
        } else {
            return input.matches("[.-]+");
        }
    }
}