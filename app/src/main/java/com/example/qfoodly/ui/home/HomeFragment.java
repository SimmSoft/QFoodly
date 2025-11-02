package com.example.qfoodly.ui.home;

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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qfoodly.R;
import com.example.qfoodly.data.Product;
import com.example.qfoodly.data.ProductDataSource;
import com.example.qfoodly.databinding.FragmentHomeBinding;
import com.example.qfoodly.databinding.ProductItemBinding;
import com.example.qfoodly.databinding.ProductItemGridBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ProductDataSource dataSource;
    private ProductAdapter adapter;
    private boolean isGridView = false;
    private ProductDataSource.SortOrder currentSortOrder = ProductDataSource.SortOrder.DEFAULT;

    private String currentQuery = null;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dataSource = new ProductDataSource(getContext());
        dataSource.open();
        adapter = new ProductAdapter(product -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("product", product);
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_nav_home_to_productDetailFragment, bundle);
        });

        setupRecyclerView();
        setupFab();
        setupMenu();
        setupSortButton();
        setupSearch();

        loadProducts();
    }

    private void setupSearch() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                currentQuery = charSequence.toString();
                loadProducts();
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
            loadProducts();
        });
    }

    private void loadProducts() {
        if(dataSource != null) {
            List<Product> products = dataSource.getAllProducts(currentSortOrder, currentQuery);
            adapter.submitList(products);
            binding.totalProductsText.setText("Total Products: " + products.size());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProducts();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dataSource.close();
        binding = null;
    }

    public static class ProductAdapter extends ListAdapter<Product, RecyclerView.ViewHolder> {

        public static final int VIEW_TYPE_LIST = 0;
        public static final int VIEW_TYPE_GRID = 1;

        private int currentViewType = VIEW_TYPE_LIST;
        private final Consumer<Product> onProductClicked;

        public ProductAdapter(Consumer<Product> onProductClicked) {
            super(new DiffUtil.ItemCallback<Product>() {
                @Override
                public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                    return oldItem.getId() == newItem.getId();
                }
                @Override
                public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                    return oldItem.getName().equals(newItem.getName()) &&
                            oldItem.getPrice() == newItem.getPrice() &&
                            oldItem.getCategory().equals(newItem.getCategory()) &&
                            oldItem.getExpirationDate().equals(newItem.getExpirationDate());
                }
            });
            this.onProductClicked = onProductClicked;
        }

        public void setViewType(int viewType) {
            this.currentViewType = viewType;
            notifyItemRangeChanged(0, getItemCount());
        }

        @Override
        public int getItemViewType(int position) {
            return currentViewType;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == VIEW_TYPE_GRID) {
                ProductItemGridBinding gridBinding = ProductItemGridBinding.inflate(inflater, parent, false);
                return new GridViewHolder(gridBinding);
            } else {
                ProductItemBinding listBinding = ProductItemBinding.inflate(inflater, parent, false);
                return new ListViewHolder(listBinding);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Product product = getItem(position);

            holder.itemView.setOnClickListener(v -> {
                if (onProductClicked != null) {
                    onProductClicked.accept(product);
                }
            });

            if (holder instanceof ListViewHolder) {
                ((ListViewHolder) holder).bind(product);
            } else if (holder instanceof GridViewHolder) {
                ((GridViewHolder) holder).bind(product);
            }
        }

        abstract static class BaseProductViewHolder extends RecyclerView.ViewHolder {
            public BaseProductViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            abstract void bind(Product product);

            protected boolean isProductExpired(Product product) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    Date expirationDate = sdf.parse(product.getExpirationDate());
                    return expirationDate != null && expirationDate.before(new java.util.Date());
                } catch (ParseException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        static class ListViewHolder extends BaseProductViewHolder {
            private final ProductItemBinding binding;

            ListViewHolder(ProductItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            @Override
            void bind(Product product) {
                binding.productNameText.setText(product.getName());
                binding.productPriceText.setText(String.format(Locale.getDefault(), "%.2f zł", product.getPrice()));
                binding.productCategoryText.setText(product.getCategory());
                binding.productExpirationDateText.setText(product.getExpirationDate());

                if(isProductExpired(product)) {
                    binding.getRoot().setCardBackgroundColor(itemView.getContext().getColor(R.color.card_background_expired));
                } else {
                    binding.getRoot().setCardBackgroundColor(itemView.getContext().getColor(R.color.card_background_dark));
                }
            }
        }

        static class GridViewHolder extends BaseProductViewHolder {
            private final ProductItemGridBinding binding;

            GridViewHolder(ProductItemGridBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            @Override
            void bind(Product product) {
                binding.productNameText.setText(product.getName());
                binding.productPriceText.setText(String.format(Locale.getDefault(), "%.2f zł", product.getPrice()));
                binding.productExpirationDateText.setText("Expires: " + product.getExpirationDate());

                if(isProductExpired(product)) {
                    binding.getRoot().setCardBackgroundColor(itemView.getContext().getColor(R.color.card_background_expired));
                } else {
                    binding.getRoot().setCardBackgroundColor(itemView.getContext().getColor(R.color.card_background_dark));
                }
            }
        }
    }
}
