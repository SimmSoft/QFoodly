package com.example.qfoodly.ui.home;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qfoodly.R;
import com.example.qfoodly.data.Product;
import com.example.qfoodly.data.ProductDataSource;
import com.example.qfoodly.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private ProductAdapter adapter;
    private boolean isGridView = false;
    private ProductDataSource.SortOrder currentSortOrder = ProductDataSource.SortOrder.DEFAULT;
    private ProductDataSource dataSource;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HomeViewModelFactory factory = new HomeViewModelFactory(requireActivity().getApplication());
        homeViewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
        dataSource = new ProductDataSource(requireContext());

        adapter = new ProductAdapter(
                product -> {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("product", product);
                    NavHostFragment.findNavController(HomeFragment.this)
                            .navigate(R.id.action_nav_home_to_productDetailFragment, bundle);
                },
                product -> {
                    // Checkbox changed callback
                    dataSource.open();
                    dataSource.markAsUsed(product.getId(), product.isUsed());
                    dataSource.close();
                }
        );

        setupRecyclerView();
        setupFab();
        setupMenu();
        setupSortButton();
        setupSearch();
        setupFilterButtons();

        homeViewModel.products.observe(getViewLifecycleOwner(), products -> {
            adapter.submitList(products);
            binding.totalProductsText.setText("Total Products: " + products.size());
        });
    }

    private void setupSearch() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {}
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                homeViewModel.setSearchQuery(charSequence.toString());
            }
        });
    }

    private void setupRecyclerView() {
        binding.recyclerviewHome.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerviewHome.setAdapter(adapter);

        RecyclerView.ItemAnimator animator = binding.recyclerviewHome.getItemAnimator();
        if (animator instanceof DefaultItemAnimator) {
            animator.setAddDuration(1200);
        }
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_nav_home_to_nav_add_product));
    }

    private void setupMenu() {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_switch_layout) {
                    isGridView = !isGridView;

                    adapter.setViewType(isGridView ? ProductAdapter.VIEW_TYPE_GRID : ProductAdapter.VIEW_TYPE_LIST);
                    binding.recyclerviewHome.setLayoutManager(
                            isGridView ? new GridLayoutManager(getContext(), 2) : new LinearLayoutManager(getContext())
                    );

                    menuItem.setIcon(isGridView ? R.drawable.ic_listview_black_24dp : R.drawable.ic_gridview_black_24dp);

                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void setupSortButton() {
        binding.sortButton.setOnClickListener(v -> {
            if(currentSortOrder == ProductDataSource.SortOrder.DEFAULT) {
                currentSortOrder = ProductDataSource.SortOrder.PRICE_ASC;
            }
            else if (currentSortOrder == ProductDataSource.SortOrder.PRICE_ASC) {
                currentSortOrder = ProductDataSource.SortOrder.PRICE_DESC;
            }
            else if (currentSortOrder == ProductDataSource.SortOrder.PRICE_DESC) {
                currentSortOrder = ProductDataSource.SortOrder.DEFAULT;
            }
            homeViewModel.setSortOrder(currentSortOrder);
        });
    }

    private void setupFilterButtons() {
        binding.btnFilterAll.setOnClickListener(v -> {
            homeViewModel.setStatusFilter(ProductDataSource.StatusFilter.ALL);
            updateFilterButtonStates(ProductDataSource.StatusFilter.ALL);
        });

        binding.btnFilterActive.setOnClickListener(v -> {
            homeViewModel.setStatusFilter(ProductDataSource.StatusFilter.ACTIVE);
            updateFilterButtonStates(ProductDataSource.StatusFilter.ACTIVE);
        });

        binding.btnFilterUsed.setOnClickListener(v -> {
            homeViewModel.setStatusFilter(ProductDataSource.StatusFilter.USED);
            updateFilterButtonStates(ProductDataSource.StatusFilter.USED);
        });

        updateFilterButtonStates(ProductDataSource.StatusFilter.ALL);
    }

    private void updateFilterButtonStates(ProductDataSource.StatusFilter activeFilter) {
        int primaryColor = getContext().getColor(R.color.primary_light);
        int grayColor = getContext().getColor(android.R.color.darker_gray);
        
        updateButtonStyle(binding.btnFilterAll, activeFilter == ProductDataSource.StatusFilter.ALL, primaryColor, grayColor);
        updateButtonStyle(binding.btnFilterActive, activeFilter == ProductDataSource.StatusFilter.ACTIVE, primaryColor, grayColor);
        updateButtonStyle(binding.btnFilterUsed, activeFilter == ProductDataSource.StatusFilter.USED, primaryColor, grayColor);
    }
    
    private void updateButtonStyle(com.google.android.material.button.MaterialButton button, boolean isActive, int primaryColor, int grayColor) {
        if (isActive) {
            button.setStrokeColor(ColorStateList.valueOf(primaryColor));
            button.setStrokeWidth(3);
            button.setTextColor(primaryColor);
        } else {
            button.setStrokeColor(ColorStateList.valueOf(grayColor));
            button.setStrokeWidth(1);
            button.setTextColor(grayColor);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
