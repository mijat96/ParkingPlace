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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.AddOrEditFavoritePlaceActivity;
import com.rmj.parking_place.model.FavoritePlace;
import com.rmj.parking_place.model.FavoritePlaceType;
import com.rmj.parking_place.model.Location;
import com.rmj.parking_place.service.ParkingPlaceServerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FavoritePlace} and makes a call to the
 * specified {@link FavoritePlacesFragment.OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class FavoritePlacesListAdapter extends RecyclerView.Adapter<FavoritePlacesListAdapter.ViewHolder> {

    private List<FavoritePlace> mValues;
    private final FavoritePlacesFragment.OnListFragmentInteractionListener mListener;
    private final int MAX_FAVORITE_PLACES = 3;

    private FavoritePlace itemForEditing;

    private FavoritePlacesFragment mFavoritePlacesFragment;

    private static HashMap<String, Integer> markerIcons;
    static {
        markerIcons = new HashMap<String, Integer>();
        markerIcons.put("HOME", R.drawable.baseline_home_blue_36);
        markerIcons.put("WORK", R.drawable.baseline_work_blue_36);
        markerIcons.put("OTHER", R.drawable.baseline_favorite_blue_36);
    }

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

        String type = holder.mItem.getType().name();
        int resourceId = markerIcons.get(type);
        holder.mImageTypeView.setImageResource(resourceId);

        holder.mTypeView.setText(type);

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
                removeItem(id);
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
        intent.putExtra("favoritePlaceType", itemForEditing.getType().name());
        intent.putExtra("selected_location", itemForEditing.getLocation());
        mFavoritePlacesFragment.startActivityForResult(intent, FavoritePlacesFragment.ADD_OR_EDIT_FAVORITE_PLACE_REQUEST);
    }

    public void removeItem(Long id) {
        removeOnServerFavoritePlace(id);
    }

    private void removeOnServerFavoritePlace(Long favoritePlaceId) {
        Call<ResponseBody> call = ParkingPlaceServerUtils.userService.removeFavoritePlace(favoritePlaceId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    Toast.makeText(mFavoritePlacesFragment.getActivity(), "Remove failed", Toast.LENGTH_SHORT).show();
                }
                else if (response.isSuccessful()) {
                    completeRemovingFavoritePlace(favoritePlaceId);
                }
                else {
                    Toast.makeText(mFavoritePlacesFragment.getActivity(), "Remove failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(mFavoritePlacesFragment.getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void completeRemovingFavoritePlace(Long favoritePlaceId) {
        int position = getPositionOfItem(getItem(favoritePlaceId));
        mValues.remove(position);
        notifyItemRemoved(position);
    }

    public boolean canAdd() {
        return getItemCount() < MAX_FAVORITE_PLACES;
    }

    public void addOrUpdateItem(String favoritePlaceName, FavoritePlaceType favoritePlaceType, Location location) {
        FavoritePlace favoritePlace = new FavoritePlace(favoritePlaceName, favoritePlaceType, location);
        if (itemForEditing != null) {
            favoritePlace.setId(itemForEditing.getId());
        }
        else {
            favoritePlace.setId(-1L);
        }
        addOrUpdateFavoritePlaceOnServer(favoritePlace);

    }

    private void addOrUpdateFavoritePlaceOnServer(FavoritePlace favoritePlace) {
        Call<Long> call = ParkingPlaceServerUtils.userService.addOrUpdateFavoritePlace(favoritePlace);
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response == null) {
                    Toast.makeText(mFavoritePlacesFragment.getActivity(), "Add or update failed",
                            Toast.LENGTH_SHORT).show();
                }
                else if (response.isSuccessful()) {
                    Long returnedId = response.body();
                    completeAddingOrUpdatingFavoritePlace(returnedId, favoritePlace);
                }
                else {
                    Toast.makeText(mFavoritePlacesFragment.getActivity(), "Add or update failed",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Toast.makeText(mFavoritePlacesFragment.getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void completeAddingOrUpdatingFavoritePlace(Long returnedId, FavoritePlace favoritePlace) {
        if (returnedId == null) {
            return;
        }
        else if (returnedId.longValue() > -1) {
            if (itemForEditing != null) {
                itemForEditing.setName(favoritePlace.getName());
                itemForEditing.setType(favoritePlace.getType());
                itemForEditing.setLocation(favoritePlace.getLocation());
                int position = getPositionOfItem(itemForEditing);
                itemForEditing.setId(returnedId);
                itemForEditing = null;
                notifyItemChanged(position);
            }
            else {
                if (getItemCount() == MAX_FAVORITE_PLACES) {
                    Toast.makeText(mFavoritePlacesFragment.getActivity(), "getItemCount() == MAX_FAVORITE_PLACES",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    favoritePlace.setId(returnedId);
                    mValues.add(favoritePlace);
                    notifyDataSetChanged();
                }
            }
        }
        else if (returnedId.longValue() == -1) {
            itemForEditing.setName(favoritePlace.getName());
            itemForEditing.setType(favoritePlace.getType());
            itemForEditing.setLocation(favoritePlace.getLocation());
            int position = getPositionOfItem(itemForEditing);
            itemForEditing = null;
            notifyItemChanged(position);
        }
    }

    public void setItems(ArrayList<FavoritePlace> items) {
        mValues = items;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mTypeView;
        public final ImageView mImageTypeView;
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
            mImageTypeView = (ImageView) view.findViewById(R.id.imageType);
            mTypeView = (TextView) view.findViewById(R.id.type);
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
