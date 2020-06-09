package com.rmj.parking_place.fragments.reports;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rmj.parking_place.R;
import com.rmj.parking_place.dto.ReportDTO;
import com.rmj.parking_place.service.ParkingPlaceServerUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A fragment representing a list of Items.
 */
public class ReportsFragment extends Fragment {

    List<ReportDTO> reportDTOList;
    MyReportDTORecyclerViewAdapter myReportDTORecyclerViewAdapter;
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ReportsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ReportsFragment newInstance(int columnCount) {
        ReportsFragment fragment = new ReportsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reportDTOList = new ArrayList<ReportDTO>();
        ParkingPlaceServerUtils.reportService.getReports().enqueue(new Callback<List<ReportDTO>>() {
            @Override
            public void onResponse(Call<List<ReportDTO>> call, Response<List<ReportDTO>> response) {
                if(response == null || (response != null && !response.isSuccessful())){
                    reportDTOList = new ArrayList<ReportDTO>();
                }else{
                    reportDTOList = response.body();
                }
                myReportDTORecyclerViewAdapter.setItems(reportDTOList);
            }

            @Override
            public void onFailure(Call<List<ReportDTO>> call, Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                reportDTOList = new ArrayList<ReportDTO>();
                myReportDTORecyclerViewAdapter.setItems(reportDTOList);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            myReportDTORecyclerViewAdapter = new MyReportDTORecyclerViewAdapter(reportDTOList);
            recyclerView.setAdapter(myReportDTORecyclerViewAdapter);
        }
        return view;
    }
}