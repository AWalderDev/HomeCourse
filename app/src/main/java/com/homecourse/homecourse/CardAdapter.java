package com.homecourse.homecourse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import org.w3c.dom.Document;

public class CardAdapter extends FirestoreRecyclerAdapter<Card, CardAdapter.CardHolder> {
    private OnItemClickListener listener;

    public CardAdapter(@NonNull FirestoreRecyclerOptions<Card> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull CardHolder holder, int position, @NonNull Card model) {
        holder.textViewFirstName.setText(model.getFirstname());
        holder.textViewLastName.setText(model.getLastname());
        holder.textViewBagSlot.setText(model.getBagslot());
    }

    @NonNull
    @Override
    public CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_card, parent, false);
        return new CardHolder(v);
    }

    class CardHolder extends RecyclerView.ViewHolder{
        TextView textViewFirstName;
        TextView textViewLastName;
        TextView textViewBagSlot;


        public CardHolder(@NonNull View itemView) {
            super(itemView);
            textViewFirstName = itemView.findViewById(R.id.text_view_first_name_card);
            textViewLastName = itemView.findViewById(R.id.text_view_last_name_card);
            textViewBagSlot = itemView.findViewById(R.id.text_view_bag_slot_card);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position),position);
                    }

                }
            });
        }

    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
