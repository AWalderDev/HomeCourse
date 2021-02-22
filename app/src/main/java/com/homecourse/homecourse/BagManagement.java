package com.homecourse.homecourse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.sql.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BagManagement extends AppCompatActivity implements BagBackDialog.BagBackDialogListener {

    /*initialize keys for reading and writing fields in database*/
    //private static final String KEY_FIRSTNAME ="firstname";
    private static final String KEY_LASTNAME ="lastname";
    //private static final String KEY_BAGSLOT ="bagslot";
    //private static final String KEY_FULLNAME ="fullname";
    private static final String KEY_ROUNDTYPE ="roundtype";
    private static final String KEY_CADDY ="caddy";
    private static final String KEY_OUT ="oncourse";
    private static final String KEY_OFFSITE = "offsite";

    //initialize views/items
    private EditText editTextNameSearch;
    private TextView nameTextView;
    private TextView bagSlotTextView;
    private RelativeLayout roundSelectLayout;
    private RecyclerView recyclerViewNames;
    private RecyclerView recyclerViewBagsOut;
    private Spinner roundTypeSpinner;
    private CheckBox caddyCheckBox;
    private String fullName;
    private Button onCourseBtn;
    private Button offSiteBtn;


    /*get instance of database and get reference to uid's collection*/
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private CollectionReference noteRef = db.collection(uid);

    //initialize card adapter for recycler view
    private CardAdapter adapterNames;
    private CardAdapter adapterBags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set content view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bag_layout);

        //assign views to corresponding object variables
        editTextNameSearch = findViewById(R.id.bag_name_search_edit);
        nameTextView = findViewById(R.id.text_view_name);
        bagSlotTextView = findViewById(R.id.text_view_bag_slot);
        roundSelectLayout = findViewById(R.id.round_select_layout);
        recyclerViewNames = findViewById(R.id.bag_recycler_view);
        recyclerViewBagsOut = findViewById(R.id.bag_recycler_view_out_bags);
        roundTypeSpinner = findViewById(R.id.spinner);
        caddyCheckBox = findViewById(R.id.checkBoxCaddy);
        onCourseBtn = findViewById(R.id.on_course_btn);
        offSiteBtn = findViewById(R.id.off_site_btn);



        //populate recyclerviews and set round type select box invisible
        setViewInvisible(roundSelectLayout);
        setUpRecyclerViewNames();
        setUpRecyclerViewBags();

        //set up spinner in round type select layout
        setUpSpinner();

        //disable on course button to show what data is in the bags recycler view
        onCourseBtn.setEnabled(false);

    }

    //make so not crash on orientation change
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.bag_layout);
    }

    //set up names recycler view
    public void setUpRecyclerViewNames(){
        //initialize query
        Query query = noteRef.orderBy(KEY_LASTNAME, Query.Direction.ASCENDING);

        //assign query to recyclerview options
        FirestoreRecyclerOptions<Card> options = new FirestoreRecyclerOptions.Builder<Card>().setQuery(query, Card.class).build();

        //initialize card adapter
        adapterNames = new CardAdapter(options);

        //assign adapter and layout manager to recyclerview
        RecyclerView recyclerView = recyclerViewNames;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapterNames.setOnItemClickListener(new CardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                //set round type box visible
                setViewVisible(roundSelectLayout);

                //take document snapshot and convert to card class to use getters from class to populate name, bag slot text views
                Card card = documentSnapshot.toObject(Card.class);

                //grab name from selected card and set text views in roundTypeLayout
                nameTextView.setText(capitalizeFirstLetter(card.getFirstname()) + " " + capitalizeFirstLetter(card.getLastname()));
                bagSlotTextView.setText("Bag Slot: " + card.getBagslot());
                fullName = card.getFirstname() + card.getLastname();


            }
        });

        recyclerView.setAdapter(adapterNames);
    }

    //set up bags recycler view
    public void setUpRecyclerViewBags(){
        //initialize query
        Query query = noteRef.whereEqualTo(KEY_OUT, "true");

        //assign query to recycler view options
        FirestoreRecyclerOptions<Card> options = new FirestoreRecyclerOptions.Builder<Card>().setQuery(query, Card.class).build();

        //initialize card adapter
        adapterBags = new CardAdapter(options);

        //assign adapter and layout manager to recyclerview
        RecyclerView recyclerView = recyclerViewBagsOut;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapterBags);

        adapterBags.setOnItemClickListener(new CardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                //take document snapshot and convert to card class to use getters from class to populate name, bag slot text views
                Card card = documentSnapshot.toObject(Card.class);

                //open dialog for removing bags from out list
                openBagBackDialog();

                //grab name from selected card and set text views in roundTypeLayout
                //nameTextView.setText(capitalizeFirstLetter(card.getFirstname()) + " " + capitalizeFirstLetter(card.getLastname()));
                //bagSlotTextView.setText("Bag Slot: " + card.getBagslot());
                //fullName = card.getFirstname() + card.getLastname();


            }
        });
    }


    public void updateRecyclerViewNames(View v){

        adapterNames.stopListening();

        Query query;
        String nameSearchEntry = editTextNameSearch.getText().toString().toLowerCase();

        if(editTextNameSearch.getText().toString().isEmpty()){
            query = noteRef.orderBy(KEY_LASTNAME, Query.Direction.ASCENDING);
        }else {
            query = noteRef.whereGreaterThanOrEqualTo(KEY_LASTNAME, nameSearchEntry);
        }

        //.startAt(lastNameSearchEntry)
        //.endAt(lastNameSearchEntry + "\uf8ff");

        FirestoreRecyclerOptions<Card> options = new FirestoreRecyclerOptions.Builder<Card>().setQuery(query, Card.class).build();

        adapterNames = new CardAdapter(options);

        RecyclerView recyclerView = recyclerViewNames;

        adapterNames.setOnItemClickListener(new CardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                //set round type box visible
                setViewVisible(roundSelectLayout);

                //take document snapshot and convert to card class to use getters from class to populate name, bag slot text views
                Card card = documentSnapshot.toObject(Card.class);

                //display name and bag slot selected
                nameTextView.setText(capitalizeFirstLetter(card.getFirstname()) + " " + capitalizeFirstLetter(card.getLastname()));
                bagSlotTextView.setText("Bag Slot: " + card.getBagslot());

                //empty search bar and call update to go back to full list of members
                editTextNameSearch.setText("");
                fullName = card.getFirstname() + card.getLastname();
                updateRecyclerViewNames(recyclerViewNames);



            }
        });

        recyclerView.setAdapter(adapterNames);
        adapterNames.startListening();

        closeKeyboard();
    }

    public void updateRecyclerViewBags(View v){

        adapterBags.stopListening();

        Query query;

        if(onCourseBtn.isEnabled()){
            query = noteRef.whereEqualTo(KEY_OUT, "true");
            onCourseBtn.setEnabled(false);
            offSiteBtn.setEnabled(true);
        }else {
            query = noteRef.whereEqualTo(KEY_OFFSITE, "true");
            onCourseBtn.setEnabled(true);
            offSiteBtn.setEnabled(false);
        }

        //.startAt(lastNameSearchEntry)
        //.endAt(lastNameSearchEntry + "\uf8ff");

        FirestoreRecyclerOptions<Card> options = new FirestoreRecyclerOptions.Builder<Card>().setQuery(query, Card.class).build();

        adapterBags = new CardAdapter(options);

        RecyclerView recyclerView = recyclerViewBagsOut;

        adapterBags.setOnItemClickListener(new CardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                //take document snapshot and convert to card class to use getters from class to populate name
                Card card = documentSnapshot.toObject(Card.class);
                fullName = card.getFirstname() + card.getLastname();

                openBagBackDialog();






            }
        });

        recyclerView.setAdapter(adapterBags);
        adapterBags.startListening();
        updateRecyclerViewNames(recyclerViewNames);
        closeKeyboard();
    }

    public void openBagBackDialog() {
        BagBackDialog bagBackDialog = new BagBackDialog();
        bagBackDialog.show(getSupportFragmentManager(), "bag back dialog");
    }



    public void recordBagsBack() {
        noteRef.document(fullName).update(KEY_OUT, "false")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(BagManagement.this, "", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BagManagement.this, "Error! 2", Toast.LENGTH_SHORT).show();
                    }
                });
        noteRef.document(fullName).update(KEY_OFFSITE, "false")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(BagManagement.this, "", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BagManagement.this, "Error! 2", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void submitRound(View v){

        //Get spinner selection
        String spinnerSelection = roundTypeSpinner.getSelectedItem().toString();

        //get current date to record round data in database organized by date
        Date currentDate = Calendar.getInstance().getTime();

        if(caddyCheckBox.isChecked()){

            Map<String, Object> roundData = new HashMap<>();
            roundData.put(KEY_ROUNDTYPE, spinnerSelection);
            roundData.put(KEY_CADDY, "true");

            Map<String, Object> outMarker = new HashMap<>();
            outMarker.put(KEY_OUT, "true");

            noteRef.document(fullName).collection("rounds").document(currentDate.toString()).set(roundData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(BagManagement.this, "Round Recorded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BagManagement.this, "Error! 1", Toast.LENGTH_SHORT).show();
                        }
                    });
            noteRef.document(fullName).update(KEY_OUT, "true")
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(BagManagement.this, "", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BagManagement.this, "Error! 2", Toast.LENGTH_SHORT).show();
                        }
                    });
        }else if (caddyCheckBox.isChecked() == false){

            Map<String, Object> roundData = new HashMap<>();
            roundData.put(KEY_ROUNDTYPE, spinnerSelection);
            roundData.put(KEY_CADDY, "false");

            noteRef.document(fullName).collection("rounds").document(currentDate.toString()).set(roundData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(BagManagement.this, "Round Recorded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BagManagement.this, "Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
            noteRef.document(fullName).update(KEY_OUT, "true")
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(BagManagement.this, "", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BagManagement.this, "Error! 2", Toast.LENGTH_SHORT).show();
                        }
                    });
            }

        //if taking bag off site record it
        if (spinnerSelection.equals("Taking Bag Off-Site")){
            noteRef.document(fullName).update(KEY_OFFSITE, "true")
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(BagManagement.this, "", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BagManagement.this, "Error! 2", Toast.LENGTH_SHORT).show();
                        }
                    });
            noteRef.document(fullName).update(KEY_OUT, "false")
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(BagManagement.this, "", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BagManagement.this, "Error! 2", Toast.LENGTH_SHORT).show();
                        }
                    });

        }

        setViewInvisible(roundSelectLayout);


    }

    public void setUpSpinner(){
        //initialize array list with spinner choices
        ArrayList<String> spinnerArray = new ArrayList<String>();

        //populate array with choices
        spinnerArray = populateArray(spinnerArray);

        //initialize and assign adapter to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roundTypeSpinner.setAdapter(adapter);
    }

    public ArrayList<String> populateArray(ArrayList<String> al){
        al.add("r-18");
        al.add("r-9");
        al.add("p-9");
        al.add("p-18");
        al.add("Walking");
        al.add("Range");
        al.add("Taking Bag Off-Site");
        return al;
    }

    //method to set each first letter of string to capitalize(used for names)
    public String capitalizeFirstLetter(String str){
        String[] strArray = str.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap + " ");
        }
        return builder.toString();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapterNames.startListening();
        adapterBags.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapterNames.stopListening();
        adapterBags.stopListening();
    }

    //Make view/layout/item invisible
    public void setViewInvisible(View v){
        v.setVisibility(View.INVISIBLE);
    }

    //Make view/layout/item visible
    public void setViewVisible(View v){
        v.setVisibility(View.VISIBLE);
    }

    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if(view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    @Override
    public void storeBag(String name, String bagSlot) {

    }
}
