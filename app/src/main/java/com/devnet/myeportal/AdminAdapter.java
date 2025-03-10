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

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder> {

    private List<AdminItem> user;
    private final Context context;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;

    public AdminAdapter(Context context, List<AdminItem> user) {
        this.context = context;
        this.user = user;
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    public void setItems(List<AdminItem> user) {
        this.user = user;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.button_layout, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        if (user != null && position < user.size()) {
            AdminItem adminItem = user.get(position);
            holder.bind(adminItem);
        }
    }

    @Override
    public int getItemCount() {
        return user != null ? user.size() : 0;
    }

    class AdminViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView titleView;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageview1);
            titleView = itemView.findViewById(R.id.textview1);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        AdminItem clickedItem = user.get(position);
                        openActivityForAdmin(clickedItem);
                    }
                }
            });
        }

        public void bind(AdminItem adminItem) {
            titleView.setText(adminItem.getName());

            Glide.with(itemView.getContext())
                    .load(adminItem.getIconUrl())
                    .placeholder(R.drawable.user_icon)
                    .error(R.drawable.user_icon)
                    .into(imageView);
        }

        private void openActivityForAdmin(AdminItem adminItem) {
            Intent intent;
            switch (adminItem.getName()) {
                case "Leave":
                    intent = new Intent(context, LeaveActivity.class);
                    break;
                case "Person_mgn":
                    intent = new Intent(context, PersonnelActivity.class);
                    break;
                case "Tasks":
                    intent = new Intent(context, TaskActivity.class);
                    break;
                default:
                    Toast.makeText(context, "No activity found for " + adminItem.getName(),
                            Toast.LENGTH_SHORT).show();
                    return;
            }
            intent.putExtra("adminItem", adminItem.getName());
            context.startActivity(intent);
        }
    }
}

