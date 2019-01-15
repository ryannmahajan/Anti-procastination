package com.ryannm.android.sam;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PackageSelectDialogFragment extends DialogFragment {
    public static final String TAG = "packageSelectDialog";
    private Callback callback;
    public static List<ResolveInfo> pkgAppsList;
  //  private TextView nextTextView;
    private Set<Integer> selectedPositions = new HashSet<>();
    RecyclerView mPackagesRecyclerView;

    public PackageSelectDialogFragment onPackageSelected(Callback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        pkgAppsList = getActivity().getPackageManager().queryIntentActivities(mainIntent, 0);

        Collections.sort(pkgAppsList, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo o1, ResolveInfo o2) {
                return o1.loadLabel(getActivity().getPackageManager()).toString().compareToIgnoreCase(o2.loadLabel(getActivity().getPackageManager()).toString());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.package_select_dialog, null);
        /*nextTextView = (TextView) v.findViewById(R.id.next_text_view);
        nextTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition!=null) {
                    ResolveInfo selected = pkgAppsList.get(selectedPosition);
                    callback.onPackageSelect(selected.activityInfo.packageName, PackageSelectDialogFragment.this);
                }
            }
        }); */
        mPackagesRecyclerView = (RecyclerView) v.findViewById(R.id.packages_recyclerview);
        mPackagesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPackagesRecyclerView.setAdapter(new PackageAdapter());

        return v;
    }

    private class PackageAdapter extends RecyclerView.Adapter<PackageHolder> {

        @Override
        public PackageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PackageHolder(LayoutInflater.from(getActivity()).inflate(R.layout.package_individual_view, parent, false));
        }

        @Override
        public void onBindViewHolder(PackageHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return pkgAppsList.size();
        }

    }

    private class PackageHolder extends RecyclerView.ViewHolder{
        private ImageView mIconView;
        private TextView mAppName;

        PackageHolder(View itemView) {
            super(itemView);

            mIconView = (ImageView) itemView.findViewById(R.id.package_icon);
            mAppName = (TextView) itemView.findViewById(R.id.package_name);

        }

        void bind(final int position) {
            final ResolveInfo curr = pkgAppsList.get(position);
            mIconView.setImageDrawable(curr.loadIcon(getActivity().getPackageManager()));
            mAppName.setText(curr.loadLabel(getActivity().getPackageManager()));
            if (!selectedPositions.isEmpty()) {
                itemView.setSelected(selectedPositions.contains(position));
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        boolean alreadySelected = v.isSelected();
                        if (alreadySelected) {
                            selectedPositions.remove(getLayoutPosition());
                        } else {
                            selectedPositions.add(getLayoutPosition());
                        }
                        /*Integer prevPos = null;
                        if (selectedPosition!=null) prevPos = selectedPosition;
                        selectedPosition = getLayoutPosition(); */
                        mPackagesRecyclerView.getAdapter().notifyItemChanged(getLayoutPosition());
                        if (selectedPositions.isEmpty()) v.setSelected(false);
                        callback.onPackageClick(curr.activityInfo.packageName, PackageSelectDialogFragment.this, !alreadySelected);

                        //   mPackagesRecyclerView.getAdapter().notifyItemChanged(prevPos);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    //itemView.setSelected(true);
                //    nextTextView.setVisibility(View.VISIBLE);
                }
            });

        }
    }

    interface Callback {
        void onPackageClick(String packageName, DialogFragment dialogFragment, boolean selected);
    }
}

// Ashes, One day I'll ..love , Addicted to you