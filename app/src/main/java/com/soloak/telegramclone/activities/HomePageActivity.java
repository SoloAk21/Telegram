 package com.soloak.telegramclone.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.StructuredQuery;
import com.soloak.telegramclone.adapters.PagerAdapter;
import com.soloak.telegramclone.R;
import com.soloak.telegramclone.databinding.ActivityHomePageBinding;
import com.soloak.telegramclone.fragments.AllFragment;
import com.soloak.telegramclone.fragments.ChannelsFragment;
import com.soloak.telegramclone.fragments.GroupsFragment;
import com.soloak.telegramclone.fragments.MessageFragment;
import com.soloak.telegramclone.fragments.UnreadFragment;
import com.soloak.telegramclone.models.Contact;
import com.soloak.telegramclone.models.ContactsUtils;
import com.soloak.telegramclone.utilities.Constants;
import com.soloak.telegramclone.utilities.PreferenceManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


 public class HomePageActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
private ActivityHomePageBinding binding;
    DatabaseReference contactsRef;
    PreferenceManager preferencesManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



     binding = ActivityHomePageBinding.inflate(getLayoutInflater());
     setContentView(binding.getRoot());


         contactsRef = FirebaseDatabase.getInstance().getReference("contacts");



        setSupportActionBar(binding.appBarHomePage.toolbar);
        binding.appBarHomePage.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        ViewPager viewPager = findViewById(R.id.pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new AllFragment(), "All");
        adapter.addFragment(new MessageFragment(), "Massage");
        adapter.addFragment(new ChannelsFragment(), "Channels");
        adapter.addFragment(new GroupsFragment(), "Groups");
        adapter.addFragment(new UnreadFragment(), "Unread");




        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);


        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home_page);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        // Inflate the header layout to access its views
        View headerView = navigationView.getHeaderView(0);
        LinearLayout contact = headerView.findViewById(R.id.contacts);
        LinearLayout logoutBtn = headerView.findViewById(R.id.logout);






        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ContactsActivity.class);
                startActivity(intent);
            }
        });

        List<Contact> deviceContacts = ContactsUtils.getDeviceContacts(getApplicationContext());
        saveContactsToFirebase(deviceContacts);




    }


     private void saveContactsToFirebase(List<Contact> contacts) {
         SharedPreferences sharedPreferences = getSharedPreferences("LoginStatus", MODE_PRIVATE);
         String userPhoneNumber = sharedPreferences.getString("userPhoneNumber", "");

         for (Contact contact : contacts) {
             String normalizedPhoneNumber = normalizePhoneNumber(contact.getPhoneNumber());

             if (!contactExists(userPhoneNumber, normalizedPhoneNumber)) {
                 String contactId = contactsRef.child(userPhoneNumber).push().getKey();
                 Map<String, Object> contactData = new HashMap<>();
                 contactData.put("name", contact.getName());
                 contactData.put("phoneNumber", normalizedPhoneNumber);

                 contactsRef.child(userPhoneNumber).child(contactId).setValue(contactData);
             } else {
                 updateExistingContact(userPhoneNumber, normalizedPhoneNumber, contact.getName());
             }
         }
     }

     private void signOut() {
         FirebaseFirestore database = FirebaseFirestore.getInstance();
         DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                 preferencesManager.getString(Constants.KEY_USER_ID)
         );

         HashMap<String,Object> updates = new HashMap<>();
         updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
         documentReference.update(updates).addOnCompleteListener(unused->{
             preferencesManager.clear();
             startActivity((new Intent(getApplicationContext(),LogInActivity.class)));
             finish();
         }).addOnFailureListener(e->{
             Toast.makeText(this, "Unable to sign out", Toast.LENGTH_SHORT).show();
         });
     }
     private String normalizePhoneNumber(String phoneNumber) {
         // Remove non-numeric characters and add country code if necessary
         return phoneNumber.replaceAll("[^0-9]", "");
     }

     private boolean contactExists(String userPhoneNumber, String normalizedPhoneNumber) {
         final boolean[] exists = {false};

         contactsRef.child(userPhoneNumber).orderByChild("phoneNumber").equalTo(normalizedPhoneNumber)
                 .addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         exists[0] = dataSnapshot.exists();
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {
                         // Handle any errors during the query (optional)
                     }
                 });

         return exists[0];
     }

     private void updateExistingContact(String userPhoneNumber, String normalizedPhoneNumber, String newName) {
         contactsRef.child(userPhoneNumber).orderByChild("phoneNumber").equalTo(normalizedPhoneNumber)
                 .addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         if (dataSnapshot.exists()) {
                             // Iterate through the results (there should be only one)
                             for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                 // Get the contact ID
                                 String contactId = snapshot.getKey();

                                 // Update the contact name
                                 contactsRef.child(userPhoneNumber).child(contactId).child("name").setValue(newName);
                             }
                         }
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {
                         // Handle any errors during the query (optional)
                     }
                 });
     }

     private List<Contact> getDeviceContacts() {

         return null;
     }

     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_page, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home_page);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }



}