package com.example.ScrollView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a02_exercise.R;
import com.example.routes.DatabaseHandler;
import com.example.routes.Route;

import java.util.ArrayList;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.ViewHolder> {

    private ArrayList<Route> routes;
    private ItemClickListener mClickListener;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private final TextView itemId;
        private final TextView content;
        public Route route;


        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            // Should st
            this.view = view;

            itemId = (TextView) view.findViewById(R.id.item_ID);
            content = (TextView) view.findViewById(R.id.content);
        }

        public View getTextView() {
            return view;
        }


    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param routes List<Route> containing the data to populate views to be used
     * by RecyclerView.
     */
    public RoutesAdapter(ArrayList<Route> routes) {
        this.routes = routes;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.route_item, viewGroup, false);
        return new ViewHolder(view);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.route = routes.get(position);
        viewHolder.itemId.setText((position + 1) + "");
        viewHolder.content.setText(viewHolder.route.timeStart() + "");

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClickListener != null) mClickListener.onItemClick(view, position);
            }
        });

    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // convenience method for getting data at click position
    public Route getRoute(int id) {
        return routes.get(id);
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return routes.size();
    }
}

