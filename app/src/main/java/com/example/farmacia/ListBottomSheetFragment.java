package com.example.farmacia;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class ListBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_TITLE = "bottom_sheet_title";
    private static final String ARG_OPTIONS = "bottom_sheet_options";

    private List<String> options;
    private String title;
    private OnOptionClickListener listener;

    public interface OnOptionClickListener {
        void onOptionClick(String option, int position);
    }

    public static ListBottomSheetFragment newInstance(String title, ArrayList<String> options) {
        ListBottomSheetFragment fragment = new ListBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putStringArrayList(ARG_OPTIONS, options);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnOptionClickListener) {
            listener = (OnOptionClickListener) context;
        } else if (getParentFragment() instanceof OnOptionClickListener) {
            listener = (OnOptionClickListener) getParentFragment();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            options = getArguments().getStringArrayList(ARG_OPTIONS);
        }
        setStyle(STYLE_NORMAL, R.style.AppTheme_TransparentBottomSheetDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_bottom_sheet, container, false);

        TextView tvTitle = view.findViewById(R.id.tvBottomSheetTitle);
        RecyclerView rvOptions = view.findViewById(R.id.rvOptions);

        tvTitle.setText(title);
        rvOptions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOptions.setAdapter(new OptionsAdapter(options, listener, this));

        return view;
    }

    private static class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.ViewHolder> {
        private final List<String> options;
        private final OnOptionClickListener listener;
        private final BottomSheetDialogFragment fragment;

        OptionsAdapter(List<String> options, OnOptionClickListener listener, BottomSheetDialogFragment fragment) {
            this.options = options;
            this.listener = listener;
            this.fragment = fragment;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bottom_sheet_option, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String option = options.get(position);
            holder.tvOption.setText(option);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOptionClick(option, position);
                }
                fragment.dismiss();
            });
        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOption;

            ViewHolder(View view) {
                super(view);
                tvOption = view.findViewById(R.id.tvOption);
            }
        }
    }
}