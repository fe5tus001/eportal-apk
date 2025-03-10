package com.devnet.myeportal;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class FinanceAdapter extends RecyclerView.Adapter<FinanceAdapter.FinanceViewHolder> {

    private List<FinanceItem> items;
    private final Context context;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;

    public FinanceAdapter(Context context, List<FinanceItem> items) {
        this.context = context;
        this.items = items;
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    public void setItems(List<FinanceItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FinanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.button_layout, parent, false);
        return new FinanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FinanceViewHolder holder, int position) {
        if (items != null && position < items.size()) {
            FinanceItem financeItem = items.get(position);
            holder.bind(financeItem);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    class FinanceViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView titleView;

        public FinanceViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageview1);
            titleView = itemView.findViewById(R.id.textview1);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && items != null) {
                    FinanceItem clickedItem = items.get(position);
                    openActivityForFinance(clickedItem);
                }
            });
        }

        public void bind(FinanceItem financeItem) {
            titleView.setText(financeItem.getName());

            Glide.with(itemView.getContext())
                    .load(financeItem.getIconUrl())
                    .placeholder(R.drawable.user_icon)
                    .error(R.drawable.user_icon)
                    .into(imageView);
        }

        private void openActivityForFinance(FinanceItem financeItem) {
            Intent intent;
            switch (financeItem.getName()) {
                case "Advance":
                    intent = new Intent(context, SalaryAdvanceActivity.class);
                    break;
                case "Payslip":
                    intent = new Intent(context, SalaryAdvanceActivity.class);
                    break;
                case "Tasks":
                    intent = new Intent(context, TaskActivity.class);
                    break;
                default:
                    Toast.makeText(context, "No activity found for " + financeItem.getName(),
                            Toast.LENGTH_SHORT).show();
                    return;
            }
            intent.putExtra("financeItem", financeItem.getName());
            context.startActivity(intent);
        }
    }
}
