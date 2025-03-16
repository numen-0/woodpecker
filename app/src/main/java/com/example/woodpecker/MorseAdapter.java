package com.example.woodpecker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MorseAdapter extends RecyclerView.Adapter<MorseAdapter.MorseViewHolder>  {
    private final Context context;
    private ArrayList<MorseCharSet.MorseChar> morseChars;
    private final OnItemClickListener listener; // Add a click listener


    public interface OnItemClickListener {
        void onItemClick(MorseCharSet.MorseChar morseChar);
    }

    public MorseAdapter(Context context, ArrayList<MorseCharSet.MorseChar> morseChars, OnItemClickListener listener) {
        this.context = context;
        this.morseChars = morseChars;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MorseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.morse_item, parent, false);
        return new MorseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MorseViewHolder holder, int position) {
        MorseCharSet.MorseChar morseChar = morseChars.get(position);
        holder.repText.setText(morseChar.rep);
        holder.sequenceText.setText(morseChar.sequence);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(morseChar));
    }

    @Override
    public int getItemCount() {
        return morseChars.size();
    }

    public static class MorseViewHolder extends RecyclerView.ViewHolder {
        TextView repText;
        TextView sequenceText;

        public MorseViewHolder(View itemView) {
            super(itemView);
            repText = itemView.findViewById(R.id.repText);
            sequenceText = itemView.findViewById(R.id.sequenceText);
        }
    }

    public void setMorseCharset(ArrayList<MorseCharSet.MorseChar> newCharset) {
        this.morseChars = newCharset;
        notifyDataSetChanged();
    }
}
