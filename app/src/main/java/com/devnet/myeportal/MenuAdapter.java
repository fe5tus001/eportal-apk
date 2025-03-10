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

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private List<Menu> menuItems;
    private final Context context;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;

    public MenuAdapter(Context context, List<Menu> menuItems) {
        this.context = context;
        this.menuItems = menuItems;
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    public void setItems(List<Menu> menuItems) {
        this.menuItems = menuItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.button_layout, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        if (menuItems != null && position < menuItems.size()) {
            Menu menu = menuItems.get(position);
            holder.bind(menu);
        }
    }

    @Override
    public int getItemCount() {
        return menuItems != null ? menuItems.size() : 0;
    }

    class MenuViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textView;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageview1);
            textView = itemView.findViewById(R.id.textview1);

            // Set the click listener on the itemView
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Menu clickedMenu = menuItems.get(position);
                        openActivityForMenu(clickedMenu);
                    }
                }
            });
        }

        public void bind(Menu menu) {
            textView.setText(menu.getName());

            Glide.with(itemView.getContext())
                    .load(menu.getIconUrl())
                    .placeholder(R.drawable.notification_26_512)
                    .error(R.drawable.notification_26_512)
                    .into(imageView);
        }

        // Method to open the respective activity for each menu item
        private void openActivityForMenu(Menu menu) {
            Intent intent;
            switch (menu.getName()) {
                case "Admin":
                    intent = new Intent(context, AdminActivity.class);
                    break;
                case "CUSA":
                    intent = new Intent(context, CusaActivity.class);
                    break;
                case "Mail":
                    intent = new Intent(context, MailActivity.class);
                    break;
                case "Finance":
                    intent = new Intent(context, FinanceActivity.class);
                    break;
                case "Fault Center":
                    intent = new Intent(context, FaultActivity.class);
                    break;
                default:
                    Toast.makeText(context, "No activity found for " + menu.getName(), Toast.LENGTH_SHORT).show();
                    return;
            }
            intent.putExtra("menuName", menu.getName());
            context.startActivity(intent);

        }
    }
}
