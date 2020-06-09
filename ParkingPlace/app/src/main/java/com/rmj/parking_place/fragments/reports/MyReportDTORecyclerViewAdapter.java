package com.rmj.parking_place.fragments.reports;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rmj.parking_place.R;
import com.rmj.parking_place.dto.ReportDTO;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ReportDTO}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyReportDTORecyclerViewAdapter extends RecyclerView.Adapter<MyReportDTORecyclerViewAdapter.ViewHolder> {

    private List<ReportDTO> mValues;

    public MyReportDTORecyclerViewAdapter(List<ReportDTO> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_report_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.address.setText(holder.mItem.address);
        holder.status.setText(holder.mItem.status);
        holder.reason.setText(holder.mItem.reason);
        holder.dateTime.setText(holder.mItem.dateTime);
        holder.parkingPlaceId.setText("" + holder.mItem.parkingPlaceId);
        holder.zoneId.setText("" + holder.mItem.zoneId);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setItems(List<ReportDTO> reportDTOList) {
        mValues = reportDTOList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView address;
        public final TextView zoneId;
        public final TextView parkingPlaceId;
        public final TextView status;
        public final TextView reason;
        public final TextView dateTime;
        public ReportDTO mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            address = (TextView) view.findViewById(R.id.address);
            parkingPlaceId = (TextView) view.findViewById(R.id.parkingPlaceId);
            status = (TextView) view.findViewById(R.id.status);
            reason = (TextView) view.findViewById(R.id.reason);
            dateTime = (TextView) view.findViewById(R.id.dateTime);
            zoneId = (TextView) view.findViewById(R.id.zoneId);
        }

        @Override
        public String toString() {
            return parkingPlaceId.toString();
        }
    }
}