package com.example.usermanagementmodule.book;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.usermanagementmodule.utils.FirebaseServices;
import com.example.usermanagementmodule.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllDataFragment extends Fragment {
    private static final int GALLERY_REQUEST_CODE =123 ;
    Context context;
    private FirebaseServices fbs;
    private ArrayList<Book> books;
    private RecyclerView rvBook;
    private BookAdapter adapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AllDataFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllRestaurantsFragment.
     */
    // TODO: Rename and change types and number of parameters


    public static AddDataFragment newInstance(String param1, String param2) {
        AddDataFragment fragment = new AddDataFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_data, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
    }
    public void conect()
    {
        fbs = FirebaseServices.getInstance();
        books = new ArrayList<>();
        rvBook = getView().findViewById(R.id.rvbooksFragment);
        adapter = new BookAdapter(getActivity(),books);
        rvBook.setAdapter(adapter);
        rvBook.setHasFixedSize(true);
        rvBook.setLayoutManager(new LinearLayoutManager(getActivity()));
        fbs.getFire().collection("furniture").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (DocumentSnapshot dataSnapshot: queryDocumentSnapshots.getDocuments()){
                    Book bok = dataSnapshot.toObject(Book.class);

                    books.add(bok);
                }

                adapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "No data available", Toast.LENGTH_SHORT).show();
                Log.e("AllRestaurantsFragment", e.getMessage());
            }
        });
    }
}