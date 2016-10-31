package net.droidlabs.mvvm.recyclerview.adapter;

import android.support.annotation.Nullable;
import android.view.View;

import net.droidlabs.mvvm.recyclerview.adapter.binder.ItemBinder;

import java.util.Collection;

/**
 * Created by q on 30/10/16.
 */

public class MixedBindedViewAdapter<T>  extends BindingRecyclerViewAdapter<T> {


    private View.OnClickListener mixedHandler;
    private boolean showMixed = true;
    private int mixedLayout;


    public MixedBindedViewAdapter(ItemBinder<T> binder, @Nullable Collection<T> items, int mixedLayout) {
        super(binder, items);
        this.mixedLayout = mixedLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return isMixedItem(position) ?
                this.mixedLayout :
                super.getItemViewType(position);
    }

    @Override
    public final void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (isMixedItem(position)) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mixedHandler.onClick(v);
                }
            });
            onBindMixedViewHolder(viewHolder, position);
        } else {
            super.onBindViewHolder(viewHolder, position);
        }
    }

    protected void onBindMixedViewHolder(ViewHolder viewHolder, int position) {
        // noting by default
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + (this.showMixed ? 1 : 0);
    }

    boolean isMixedItem(int position) {
        return this.showMixed && position == getItemCount() - 1;
    }

    public void setMixedHandler(View.OnClickListener mixedHandler) {
        this.mixedHandler = mixedHandler;
    }

    public void setShowMixed(boolean show) {
        if (this.showMixed != show) {
            this.showMixed = show;
            notifyDataSetChanged();
        }
    }
}
