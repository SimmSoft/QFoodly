package com.example.qfoodly.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qfoodly.R;
import com.example.qfoodly.data.Product;
import com.example.qfoodly.databinding.ProductItemBinding;
import com.example.qfoodly.databinding.ProductItemGridBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

public class ProductAdapter extends ListAdapter<Product, RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_LIST = 0;
    public static final int VIEW_TYPE_GRID = 1;

    private int currentViewType = VIEW_TYPE_LIST;
    private final Consumer<Product> onProductClicked;
    private final Consumer<Product> onProductUsedChanged;

    public ProductAdapter(Consumer<Product> onProductClicked) {
        this(onProductClicked, null);
    }

    public ProductAdapter(Consumer<Product> onProductClicked, Consumer<Product> onProductUsedChanged) {
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
                        oldItem.getExpirationDate().equals(newItem.getExpirationDate()) &&
                        oldItem.isUsed() == newItem.isUsed();
            }
        });
        this.onProductClicked = onProductClicked;
        this.onProductUsedChanged = onProductUsedChanged;
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
            ((ListViewHolder) holder).bind(product, onProductUsedChanged);
        } else if (holder instanceof GridViewHolder) {
            ((GridViewHolder) holder).bind(product, onProductUsedChanged);
        }
    }

    abstract static class BaseProductViewHolder extends RecyclerView.ViewHolder {
        public BaseProductViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bind(Product product);

        protected boolean isProductExpired(Product product) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
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
            bind(product, null);
        }

        void bind(Product product, Consumer<Product> onUsedChanged) {
            binding.productNameText.setText(product.getName());
            binding.productPriceText.setText(String.format(Locale.getDefault(), "%.2f zł", product.getPrice()));
            binding.productCategoryText.setText(product.getCategory());
            binding.productExpirationDateText.setText(product.getExpirationDate());

            binding.productUsedCheckbox.setOnCheckedChangeListener(null);
            binding.productUsedCheckbox.setChecked(product.isUsed());
            binding.productUsedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                product.setUsed(isChecked);
                if (onUsedChanged != null) {
                    onUsedChanged.accept(product);
                }
                updateCardColor(product);
            });

            updateCardColor(product);
        }

        private void updateCardColor(Product product) {
            int backgroundColor;
            if (product.isUsed()) {
                backgroundColor = R.color.card_background_used;
            } else if (isProductExpired(product)) {
                backgroundColor = R.color.card_background_expired;
            } else {
                backgroundColor = R.color.card_background_dark;
            }
            binding.getRoot().setCardBackgroundColor(itemView.getContext().getColor(backgroundColor));
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
            bind(product, null);
        }

        void bind(Product product, Consumer<Product> onUsedChanged) {
            binding.productNameText.setText(product.getName());
            binding.productPriceText.setText(String.format(Locale.getDefault(), "%.2f zł", product.getPrice()));
            binding.productExpirationDateText.setText("Expires: " + product.getExpirationDate());

            binding.productUsedCheckbox.setOnCheckedChangeListener(null);
            binding.productUsedCheckbox.setChecked(product.isUsed());
            binding.productUsedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                product.setUsed(isChecked);
                if (onUsedChanged != null) {
                    onUsedChanged.accept(product);
                }
                updateCardColor(product);
            });

            updateCardColor(product);
        }

        private void updateCardColor(Product product) {
            int backgroundColor;
            if (product.isUsed()) {
                backgroundColor = R.color.card_background_used;
            } else if (isProductExpired(product)) {
                backgroundColor = R.color.card_background_expired;
            } else {
                backgroundColor = R.color.card_background_dark;
            }
            binding.getRoot().setCardBackgroundColor(itemView.getContext().getColor(backgroundColor));
        }
    }
}