package com.example.plantify_user;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.plantify_user.adapter.ProductAdapter;
import com.example.plantify_user.model.ProductModel;
import com.example.plantify_user.products.product_info;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class home_layout extends Fragment {

    public home_layout() {
    }
    private TextView bestSellingText, housePlantText, outdoorGardenText;
    private RecyclerView housePlantsRecyclerView, bestSellingRecyclerView, outdoorGardenRecyclerView, productListedRecyclerView;
    private ArrayList<ProductModel> housePlantsList, bestSellingList, outdoorGardenList, productList, fullProductList;
    private ProductAdapter housePlantsAdapter, bestSellingAdapter, outdoorGardenAdapter, searchAdapter;
    private FirebaseDatabase firebaseDatabase;
    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_layout, container, false);

        firebaseDatabase = FirebaseDatabase.getInstance();

        searchView = view.findViewById(R.id.searchItem);
        bestSellingText = view.findViewById(R.id.bestSelling);
        housePlantText = view.findViewById(R.id.housePlant);
        outdoorGardenText = view.findViewById(R.id.outdoorGarden);
        housePlantsRecyclerView = view.findViewById(R.id.housePlantsRecyclerView);
        bestSellingRecyclerView = view.findViewById(R.id.bestSellingRecyclerView);
        outdoorGardenRecyclerView = view.findViewById(R.id.outdoorGardenRecyclerView);
        productListedRecyclerView = view.findViewById(R.id.productListedRecyclerView);

        housePlantsList = new ArrayList<>();
        bestSellingList = new ArrayList<>();
        outdoorGardenList = new ArrayList<>();
        productList = new ArrayList<>();
        fullProductList = new ArrayList<>();

        housePlantsAdapter = new ProductAdapter(getContext(), housePlantsList);
        bestSellingAdapter = new ProductAdapter(getContext(), bestSellingList);
        outdoorGardenAdapter = new ProductAdapter(getContext(), outdoorGardenList);
        searchAdapter = new ProductAdapter(getContext(), productList);

        housePlantsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        bestSellingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        outdoorGardenRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        productListedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        housePlantsRecyclerView.setAdapter(housePlantsAdapter);
        bestSellingRecyclerView.setAdapter(bestSellingAdapter);
        outdoorGardenRecyclerView.setAdapter(outdoorGardenAdapter);
        productListedRecyclerView.setAdapter(searchAdapter);

        housePlantsAdapter.setOnItemClickListener(product -> navigateToProductInfo(product.getKey()));
        bestSellingAdapter.setOnItemClickListener(product -> navigateToProductInfo(product.getKey()));
        outdoorGardenAdapter.setOnItemClickListener(product -> navigateToProductInfo(product.getKey()));
        searchAdapter.setOnItemClickListener(product -> navigateToProductInfo(product.getKey()));

        firebaseDatabase.getReference("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                housePlantsList.clear();
                bestSellingList.clear();
                outdoorGardenList.clear();
                productList.clear();
                fullProductList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ProductModel product = ds.getValue(ProductModel.class);
                    if (product != null) {
                        product.setKey(ds.getKey());
                        fullProductList.add(product);

                        switch (product.getCategory()) {
                            case "House Plants":
                                housePlantsList.add(product);
                                break;
                            case "Best Selling Plants":
                                bestSellingList.add(product);
                                break;
                            case "Outdoor Garden Plants":
                                outdoorGardenList.add(product);
                                break;
                        }
                    }
                }
                housePlantsAdapter.notifyDataSetChanged();
                bestSellingAdapter.notifyDataSetChanged();
                outdoorGardenAdapter.notifyDataSetChanged();
                productList.addAll(fullProductList);
                searchAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProductList(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProductList(newText);
                return true;
            }
        });

        return view;
    }

    private void filterProductList(String query) {
        if (query.isEmpty()) {
            productList.clear();
            productList.addAll(fullProductList);
            searchAdapter.notifyDataSetChanged();
            housePlantText.setVisibility(View.VISIBLE);
            bestSellingText.setVisibility(View.VISIBLE);
            outdoorGardenText.setVisibility(View.VISIBLE);
            housePlantsRecyclerView.setVisibility(View.VISIBLE);
            bestSellingRecyclerView.setVisibility(View.VISIBLE);
            outdoorGardenRecyclerView.setVisibility(View.VISIBLE);
            productListedRecyclerView.setVisibility(View.GONE);
        } else {
            ArrayList<ProductModel> filteredList = new ArrayList<>();
            for (ProductModel product : fullProductList) {
                if (product.getProductName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }

            productList.clear();
            productList.addAll(filteredList);
            searchAdapter.notifyDataSetChanged();

            housePlantText.setVisibility(View.GONE);
            bestSellingText.setVisibility(View.GONE);
            outdoorGardenText.setVisibility(View.GONE);
            housePlantsRecyclerView.setVisibility(View.GONE);
            bestSellingRecyclerView.setVisibility(View.GONE);
            outdoorGardenRecyclerView.setVisibility(View.GONE);
            productListedRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    private void navigateToProductInfo(String productKey) {
        replaceFragment(new product_info(productKey));
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
