package com.devnet.myeportal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;
    private final Context context;

    public UserAdapter(Context context, List<User> user) {
        this.context = context;
        this.users = user;
    }

    public void setItems(List<User> user) {
        this.users = user;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        if (users != null && position < users.size()) {
            User user = users.get(position);
            holder.bind(user);
        }
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameView;
        private final TextView rankView;
        private final TextView svcNumberView;
        private final TextView tradeView;
        private final TextView formationView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageview1);
            nameView = itemView.findViewById(R.id.textview1);
            rankView = itemView.findViewById(R.id.textView);
            svcNumberView = itemView.findViewById(R.id.textView5);
            tradeView = itemView.findViewById(R.id.textView6);
            formationView = itemView.findViewById(R.id.textView7);
        }

        public void bind(User adminItem) {
            nameView.setText(adminItem.getFirstName() + " " + adminItem.getSurname());
            rankView.setText(adminItem.getRank());
            svcNumberView.setText(adminItem.getSvcNumber());
            tradeView.setText(adminItem.getTrade());
            formationView.setText(adminItem.getFormation());

            Glide.with(itemView.getContext())
                    .load(adminItem.getProfileImageUrl())
                    .placeholder(R.drawable.user_icon)
                    .error(R.drawable.user_icon)
                    .into(imageView);
        }
    }
}
