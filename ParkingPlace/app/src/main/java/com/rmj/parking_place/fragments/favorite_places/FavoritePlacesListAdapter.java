package com.rmj.parking_place.fragments.favorite_places;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.AddOrEditFavoritePlaceActivity;
import com.rmj.parking_place.model.FavoritePlace;
import com.rmj.parking_place.model.Location;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FavoritePlace} and makes a call to the
 * specified {@link FavoritePlacesFragment.OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class FavoritePlacesListAdapter extends RecyclerView.Adapter<FavoritePlacesListAdapter.ViewHolder> {

    private final List<FavoritePlace> mValues;
    private final FavoritePlacesFragment.OnListFragmentInteractionListener mListener;
    private final int MAX_FAVORITE_PLACES = 3;

    private FavoritePlace itemForEditing;

    private FavoritePlacesFragment mFavoritePlacesFragment;

    public FavoritePlacesListAdapter(List<FavoritePlace> items, FavoritePlacesFragment.OnListFragmentInteractionListener listener,
                                     FavoritePlacesFragment favoritePlacesFragment) {
        mValues = items;
        mListener = listener;
        mFavoritePlacesFragment = favoritePlacesFragment;
    }

     private FavoritePlace getItem(Long id) {
         for (FavoritePlace item : mValues) {
             if (item.getId().equals(id)) {
                 return item;
             }
         }

         return null;
     }

     private int getPositionOfItem(FavoritePlace item) {
        return mValues.indexOf(item);
     }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favorite_place_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        SpannableString nameSpanString = new SpannableString(holder.mItem.getName());
        nameSpanString.setSpan(new StyleSpan(Typeface.BOLD), 0, nameSpanString.length(), 0);
        holder.mNameView.setText(nameSpanString);

        Location location = holder.mItem.getLocation();

        holder.mLatitudeView.setText(""+location.getLatitude());
        holder.mLongitudeView.setText(""+location.getLongitude());
        holder.mAddressView.setText(location.getAddress());

        holder.mEditBtn.setTag(holder.mItem.getId());
        holder.mEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long id = (Long) v.getTag();
                editItem(getItem(id));
            }
        });

        holder.mRemoveBtn.setTag(holder.mItem.getId());
        holder.mRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long id = (Long) v.getTag();
                int position = getPositionOfItem(getItem(id));
                removeItem(position);
            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private void editItem(FavoritePlace item) {
        itemForEditing = item;
        goToAddOrEditFavoritePlaceActivity();
    }

    private void goToAddOrEditFavoritePlaceActivity() {
        Intent intent = new Intent(mFavoritePlacesFragment.getActivity(), AddOrEditFavoritePlaceActivity.class);
        intent.putExtra("favorite_place_name", itemForEditing.getName());
        intent.putExtra("selected_location", itemForEditing.getLocation());
        mFavoritePlacesFragment.startActivityForResult(intent, FavoritePlacesFragment.ADD_OR_EDIT_FAVORITE_PLACE_REQUEST);
    }

    public void removeItem(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
    }

    public boolean canAdd() {
        return getItemCount() < MAX_FAVORITE_PLACES;
    }

    public boolean addOrUpdateItem(String favoritePlaceName, Location location) {
        if (itemForEditing != null) {
            itemForEditing.setName(favoritePlaceName);
            itemForEditing.setLocation(location);
            int position = getPositionOfItem(itemForEditing);
            itemForEditing = null;
            notifyItemChanged(position);
            return true;
        }
        else {
            if (getItemCount() == MAX_FAVORITE_PLACES) {
                return false;
            }
            else {
                FavoritePlace newFavoritePlace = new FavoritePlace(favoritePlaceName, location);
                mValues.add(newFavoritePlace);
                notifyDataSetChanged();
                return true;
            }
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mLatitudeView;
        public final TextView mLongitudeView;
        public final TextView mAddressView;
        public final ImageButton mEditBtn;
        public final ImageButton mRemoveBtn;
        public FavoritePlace mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.name);
            mLatitudeView = (TextView) view.findViewById(R.id.latitude);
            mLongitudeView = (TextView) view.findViewById(R.id.longitude);
            mAddressView = (TextView) view.findViewById(R.id.address);
            mEditBtn = (ImageButton) view.findViewById(R.id.editBtn);
            mRemoveBtn = (ImageButton) view.findViewById(R.id.removeBtn);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
