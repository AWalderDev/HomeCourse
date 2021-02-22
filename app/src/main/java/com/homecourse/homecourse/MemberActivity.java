package com.homecourse.homecourse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemberActivity extends AppCompatActivity{
    private static final String TAG = "MemberActivity";
    /*initialize keys for reading and writing fields in database*/
    private static final String KEY_FIRSTNAME ="firstname";
    private static final String KEY_LASTNAME ="lastname";
    private static final String KEY_BAGSLOT ="bagslot";
    private static final String KEY_FULLNAME ="fullname";

    /*Initialize EditTexts and TextViews*/
    private EditText editTextFirstNameAdd;
    private EditText editTextLastNameAdd;
    //private EditText editTextBagSlot;
    //private TextView textViewData;
    private TextView textViewFirstNameEdit;
    private TextView textViewLastNameEdit;
    private EditText editTextBagSlotEdit;
    private LinearLayout editMemberView;
    private LinearLayout memberWholeLayout;
    private Button btnSaveChanges;
    private Button btnAddMember;
    private Button btnDeleteMember;
    private TextView textViewEditMember;
    private TextView textViewAddMember;
    private EditText searchFirstName;
    private EditText searchLastName;



    /*get instance of database and get reference to uid's collection*/
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private CollectionReference noteRef = db.collection(uid);

    private CardAdapter adapter;
    private CardAdapter sadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_management_layout);

            //connect layout elements to initialized objects
        searchFirstName = findViewById(R.id.edit_text_first_name);
        searchLastName= findViewById(R.id.edit_text_last_name);
        textViewEditMember = findViewById(R.id.text_view_edit_member_header);
        textViewAddMember = findViewById(R.id.text_view_add_member_header);
        editTextFirstNameAdd = findViewById(R.id.edit_text_first_name_add);
        editTextLastNameAdd = findViewById(R.id.edit_text_last_name_add);
        //editTextBagSlot = findViewById(R.id.edit_text_bag_slot);
        //textViewData = findViewById(R.id.text_view_data);
        textViewFirstNameEdit = findViewById(R.id.edit_text_first_name_edit);
        textViewLastNameEdit = findViewById(R.id.edit_text_last_name_edit);
        editTextBagSlotEdit = findViewById(R.id.edit_text_bag_slot_edit);
        editMemberView = findViewById(R.id.editMemberView);
        btnSaveChanges = findViewById(R.id.saveChangesBtn);
        btnAddMember = findViewById(R.id.addMemberBtn);
        btnDeleteMember = findViewById(R.id.deleteMemberBtn);
        memberWholeLayout = findViewById(R.id.member_whole_layout);

        //set layouts that need to be invisible
        btnDeleteMember.setVisibility(View.GONE);
        btnAddMember.setVisibility(View.GONE);
        btnSaveChanges.setVisibility(View.GONE);
        editMemberView.setVisibility(View.INVISIBLE);
        textViewEditMember.setVisibility(View.GONE);
        textViewAddMember.setVisibility(View.GONE);
        textViewLastNameEdit.setVisibility(View.GONE);
        textViewFirstNameEdit.setVisibility(View.GONE);
        editTextLastNameAdd.setVisibility(View.GONE);
        editTextFirstNameAdd.setVisibility(View.GONE);
        setUpRecyclerView();

    }
    //make so not crash on orientation change
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.member_management_layout);
    }
    /*searchLastName.addTextChangedListener(new TextWatcher() {

        public void onTextChanged(CharSequence s, int start, int before,
        int count) {

        }



        public void beforeTextChanged(CharSequence s, int start, int count,
        int after) {

        }

        public void afterTextChanged(Editable s) {
            if(!s.equals("") ) {
                search(s.toString());
            }
        }
    });

    private void search(String s){
        Query query = noteRef.whereGreaterThanOrEqualTo(KEY_LASTNAME, searchLastName.getText().toString())
                .startAt(s)
                .endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener(){
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot){
               if(dataSnapshot.hasChildren()){

               }
           }
        });
    }*/

    public void setUpRecyclerView(){
        //Query query = noteRef.whereGreaterThanOrEqualTo(KEY_LASTNAME, searchLastName.getText().toString());
        Query query = noteRef.orderBy(KEY_LASTNAME, Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Card> options = new FirestoreRecyclerOptions.Builder<Card>().setQuery(query, Card.class).build();

        adapter = new CardAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.member_recycler_view);
        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new CardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                //set edit member layout visible
                setEditMemberVisible(memberWholeLayout);

                //take document snapshot and convert to card class to use getters from class to populate fn, ln, bagslot
                Card card = documentSnapshot.toObject(Card.class);

                textViewFirstNameEdit.setText(card.getFirstname());
                textViewLastNameEdit.setText(card.getLastname());
                editTextBagSlotEdit.setText(card.getBagslot());


            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();

    }

    public void updateRecyclerView(View v){
        adapter.stopListening();
        String lastNameSearchEntry = searchLastName.getText().toString().toLowerCase();

        Query query = noteRef.whereGreaterThanOrEqualTo(KEY_LASTNAME, lastNameSearchEntry);
                //.startAt(lastNameSearchEntry)
                //.endAt(lastNameSearchEntry + "\uf8ff");

        FirestoreRecyclerOptions<Card> options = new FirestoreRecyclerOptions.Builder<Card>().setQuery(query, Card.class).build();

        adapter = new CardAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.member_recycler_view);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.setOnItemClickListener(new CardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                //set edit member layout visible
                setEditMemberVisible(memberWholeLayout);

                //take document snapshot and convert to card class to use getters from class to populate fn, ln, bagslot
                Card card = documentSnapshot.toObject(Card.class);

                textViewFirstNameEdit.setText(card.getFirstname());
                textViewLastNameEdit.setText(card.getLastname());
                editTextBagSlotEdit.setText(card.getBagslot());


            }
        });
    }
    /*public void updateSearchRecyclerView(View v) {
        Query query = noteRef.whereGreaterThanOrEqualTo(KEY_LASTNAME, searchLastName.getText().toString());

        FirestoreRecyclerOptions<Card> options = new FirestoreRecyclerOptions.Builder<Card>().setQuery(query, Card.class).build();

        adapter = new CardAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.member_recycler_view);
        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new CardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                //set edit member layout visible
                setEditMemberVisible(memberWholeLayout);

                //take document snapshot and convert to card class to use getters from class to populate fn, ln, bagslot
                Card card = documentSnapshot.toObject(Card.class);

                textViewFirstNameEdit.setText(card.getFirstname());
                textViewLastNameEdit.setText(card.getLastname());
                editTextBagSlotEdit.setText(card.getBagslot());


            }
        });
    }*/

    public void setAddMemberVisible(View v){
        editTextFirstNameAdd.setText("");
        editTextLastNameAdd.setText("");
        editTextBagSlotEdit.setText("");
        textViewEditMember.setVisibility(View.GONE);
        textViewFirstNameEdit.setVisibility(View.GONE);
        textViewLastNameEdit.setVisibility(View.GONE);
        textViewAddMember.setVisibility(View.VISIBLE);
        editMemberView.setVisibility(View.VISIBLE);
        btnAddMember.setVisibility(View.VISIBLE);
        editTextFirstNameAdd.setVisibility(View.VISIBLE);
        editTextLastNameAdd.setVisibility(View.VISIBLE);
        btnDeleteMember.setVisibility(View.GONE);
        btnSaveChanges.setVisibility(View.GONE);
    }
    public void setEditMemberVisible(View v){
        textViewAddMember.setVisibility(View.GONE);
        textViewEditMember.setVisibility(View.VISIBLE);
        editMemberView.setVisibility(View.VISIBLE);
        editTextLastNameAdd.setVisibility(View.GONE);
        editTextFirstNameAdd.setVisibility(View.GONE);
        btnAddMember.setVisibility(View.GONE);
        btnSaveChanges.setVisibility(View.VISIBLE);
        btnDeleteMember.setVisibility(View.VISIBLE);
        textViewLastNameEdit.setVisibility(View.VISIBLE);
        textViewFirstNameEdit.setVisibility(View.VISIBLE);
    }

    public void deleteMember(View v){
        String firstName = textViewFirstNameEdit.getText().toString();
        String lastName = textViewLastNameEdit.getText().toString();

        noteRef.document(firstName + lastName)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
        editMemberView.setVisibility(View.INVISIBLE);
    }

    public void editMember(View v) {
        String firstName = textViewFirstNameEdit.getText().toString().toLowerCase();
        String lastName = textViewLastNameEdit.getText().toString().toLowerCase();
        String bagSlot = editTextBagSlotEdit.getText().toString().toLowerCase();
        String fullName = firstName + " " + lastName;

        if (bagSlot == ""){
            Map<String, Object> member = new HashMap<>();
            member.put(KEY_FIRSTNAME, firstName);
            member.put(KEY_LASTNAME, lastName);
            member.put(KEY_FULLNAME, fullName);

            /*Reference collection in noteRef, and creating a document for the member in that collection*/
            noteRef.document(firstName + lastName).set(member)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MemberActivity.this, "Member Added", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MemberActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, e.toString());
                        }
                    });
        }else{
            Map<String, Object> member = new HashMap<>();
            member.put(KEY_FIRSTNAME, firstName);
            member.put(KEY_LASTNAME, lastName);
            member.put(KEY_FULLNAME, fullName);
            member.put(KEY_BAGSLOT, bagSlot);

            /*Reference collection in noteRef, and creating a document for the member in that collection*/
            noteRef.document(firstName + lastName).set(member)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MemberActivity.this, "Member Added", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MemberActivity.this, "Error! CODE: 6942", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, e.toString());
                        }
                    });
        }
        editMemberView.setVisibility(View.INVISIBLE);
    }

    public void addMember(View v) {

        String firstName = editTextFirstNameAdd.getText().toString().toLowerCase();
        String lastName = editTextLastNameAdd.getText().toString().toLowerCase();
        String bagSlot = editTextBagSlotEdit.getText().toString().toLowerCase();
        String fullName = firstName + " " + lastName;

        if (bagSlot.isEmpty() || firstName.isEmpty() || lastName.isEmpty()){
            Toast.makeText(MemberActivity.this, "PLEASE FILL IN ALL INFORMATION", Toast.LENGTH_SHORT).show();
        }else{
            Map<String, Object> member = new HashMap<>();
            member.put(KEY_FIRSTNAME, firstName);
            member.put(KEY_LASTNAME, lastName);
            member.put(KEY_FULLNAME, fullName);
            member.put(KEY_BAGSLOT, bagSlot);

            /*Reference collection in noteRef, and creating a document for the member in that collection*/
            noteRef.document(firstName + lastName).set(member)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MemberActivity.this, "Member Added", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MemberActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, e.toString());
                        }
                    });
            editMemberView.setVisibility(View.INVISIBLE);
        }

    }

    /*public void searchMember (View v) {
        String firstName = editTextFirstName.getText().toString();
        String lastName = editTextLastName.getText().toString();

        noteRef.document(firstName + lastName).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            String firstName = documentSnapshot.getString(KEY_FIRSTNAME);
                            String lastName = documentSnapshot.getString(KEY_LASTNAME);
                            String bagSlot = documentSnapshot.getString(KEY_BAGSLOT);

                            textViewData.setText("First Name: " + firstName + "\n" + "Last Name: " + lastName);
                            editTextFirstNameEdit.setText(firstName);
                            editTextLastNameEdit.setText(lastName);
                            editTextBagSlotEdit.setText(bagSlot);
                        }else{
                            Toast.makeText(MemberActivity.this, "No Member Found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MemberActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });
    }*/
}
