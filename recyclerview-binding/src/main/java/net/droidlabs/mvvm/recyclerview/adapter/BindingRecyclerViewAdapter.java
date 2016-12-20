package net.droidlabs.mvvm.recyclerview.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.databinding.ViewDataBinding;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.droidlabs.mvvm.recyclerview.adapter.binder.ItemBinder;

import java.lang.ref.WeakReference;
import java.util.Collection;

public class BindingRecyclerViewAdapter<T> extends RecyclerView.Adapter<BindingRecyclerViewAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private static final int ITEM_MODEL = -124;
    private final WeakReferenceOnListChangedCallback onListChangedCallback;
    private final ItemBinder<T> itemBinder;
    private ObservableList<T> items;
    private LayoutInflater inflater;
    private ClickHandler<T> clickHandler;
    private LongClickHandler<T> longClickHandler;

    public BindingRecyclerViewAdapter(ItemBinder<T> itemBinder, @Nullable Collection<T> items) {
        this.itemBinder = itemBinder;
        this.onListChangedCallback = new WeakReferenceOnListChangedCallback<>(this);
        setItems(items);
    }

    public ObservableList<T> getItems() {
        return items;
    }

    public void setItems(@Nullable Collection<T> items) {
        if (this.items == items) {
            return;
        }

        if (this.items != null) {
            this.items.removeOnListChangedCallback(onListChangedCallback);
            notifyItemRangeRemoved(0, this.items.size());
        }

        if (items instanceof ObservableList) {
            this.items = (ObservableList<T>) items;
            notifyItemRangeInserted(0, this.items.size());
            this.items.addOnListChangedCallback(onListChangedCallback);
        } else if (items != null) {
            this.items = new ObservableArrayList<>();
            this.items.addOnListChangedCallback(onListChangedCallback);
            this.items.addAll(items);
        } else {
            this.items = null;
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        if (items != null) {
            items.removeOnListChangedCallback(onListChangedCallback);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int layoutId) {
        ViewDataBinding binding = DataBindingUtil.inflate(obtainInflater(viewGroup), layoutId, viewGroup, false);
        return new ViewHolder(binding);
    }

    protected LayoutInflater obtainInflater(ViewGroup viewGroup) {
        if (inflater == null) {
            inflater = LayoutInflater.from(viewGroup.getContext());
        }
        return inflater;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final T item = position < items.size() ? items.get(adapterPositionToListPosition(position)) : null;
        viewHolder.binding.setVariable(itemBinder.getBindingVariable(item), item);
        View itemView = viewHolder.binding.getRoot();
        itemView.setTag(ITEM_MODEL, item);
        viewHolder.binding.executePendingBindings();
        configureItemView(itemView, item);
    }

    protected void configureItemView(View itemView, T item) {
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    @Override
    public int getItemViewType(int position) {
        return itemBinder.getLayoutRes(items.get(listPositionToAdapterPosition(position)));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : listPositionToAdapterPosition(items.size());
    }

    @Override
    public void onClick(View v) {
        if (clickHandler != null) {
            T item = getData(v);
            clickHandler.onClick(item);
        }
    }

    protected T getData(View v) {
        return (T) v.getTag(ITEM_MODEL);
    }

    @Override
    public boolean onLongClick(View v) {
        if (longClickHandler != null) {
            T item = getData(v);
            longClickHandler.onLongClick(item);
            return true;
        }
        return false;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewDataBinding getBinding() {
            return binding;
        }

        protected final ViewDataBinding binding;

        protected ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ViewHolder(View v) {
            super(v);
            this.binding = null;
        }
    }

    // return true if data should NOT be displayed
    protected boolean filter(T data) {
        return false;
    }

    private int listPositionToAdapterPosition(int listPosition) {
        int res = 0;
        for (int i = 0; i < listPosition; i++) {
            T item = items.get(i);
            if (filter(item) == false) {
                res ++;
            }
        }
        return res;
    }

    int adapterPositionToListPosition(int adapterPosition) {
        int i = 0;
        for (int res = -1; i < items.size(); i++) {
            T item = items.get(i);
            if (filter(item)) {
                continue;
            }
            if (++res >= adapterPosition) {
                break;
            }
        }

        int adapt = listPositionToAdapterPosition(i);
        if (adapterPosition != adapt) {
            throw  new RuntimeException("learn to code:" + adapterPosition + " != " + adapt);
        }
        return i;
    }

    private static class WeakReferenceOnListChangedCallback<T> extends ObservableList.OnListChangedCallback {

        private final WeakReference<BindingRecyclerViewAdapter<T>> adapterReference;

        public WeakReferenceOnListChangedCallback(BindingRecyclerViewAdapter<T> bindingRecyclerViewAdapter) {
            this.adapterReference = new WeakReference<>(bindingRecyclerViewAdapter);
        }

        @Override
        public void onChanged(ObservableList sender) {
            RecyclerView.Adapter adapter = adapterReference.get();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        private int adapterPositionToListPosition(int adapterPosition) {
            BindingRecyclerViewAdapter adapter = adapterReference.get();
            if (adapter != null) {
                return adapter.adapterPositionToListPosition(adapterPosition);
            }
            return adapterPosition;
        }

        private int listPositionToAdapterPosition(int adapterPosition) {
            BindingRecyclerViewAdapter adapter = adapterReference.get();
            if (adapter != null) {
                return adapter.listPositionToAdapterPosition(adapterPosition);
            }
            return adapterPosition;
        }

        @Override
        public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
            RecyclerView.Adapter adapter = adapterReference.get();
            if (adapter != null) {
                adapter.notifyItemRangeChanged(listPositionToAdapterPosition(positionStart), itemCount);
            }
        }

        @Override
        public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
            RecyclerView.Adapter adapter = adapterReference.get();
            if (adapter != null) {
                adapter.notifyItemRangeInserted(listPositionToAdapterPosition(positionStart), itemCount);
            }
        }

        @Override
        public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {
            RecyclerView.Adapter adapter = adapterReference.get();
            if (adapter != null) {
                adapter.notifyItemMoved(listPositionToAdapterPosition(fromPosition), listPositionToAdapterPosition(toPosition));
            }
        }

        @Override
        public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
            RecyclerView.Adapter adapter = adapterReference.get();
            if (adapter != null) {
                adapter.notifyItemRangeRemoved(listPositionToAdapterPosition(positionStart), itemCount);
            }
        }
    }

    public void setClickHandler(ClickHandler<T> clickHandler) {
        this.clickHandler = clickHandler;
    }

    public void setLongClickHandler(LongClickHandler<T> clickHandler) {
        this.longClickHandler = clickHandler;
    }
}